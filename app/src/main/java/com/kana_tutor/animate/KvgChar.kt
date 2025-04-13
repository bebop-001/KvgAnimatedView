/*
 * Copyright 2020 Steven Smith kana-tutor.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("FunctionName", "LocalVariableName", "CascadeIf")

package com.kana_tutor.animate

import java.io.BufferedReader

// our own personal exception.
class SvgConvertException(message:String) : Exception (message)

private const val TAG = "KvgChar"
@Suppress("unused")
class KvgChar (
    fileHandle : BufferedReader
) {
    private var name = ""
    private var renderChar = ""
    // width/height
    lateinit var dimensions : Pair<Float,Float>
        private set
    // A Kvg character is composed of strokes
    // and paths.
    class KvgStrokePath(val op: String, val coord: Array<Float>) {
        override fun toString(): String {
            return op + coord.joinToString(",")
        }
    }
    data class KvgAnnotation(
        val point : Pair<Float,Float>, val text : String
    ) {
        override fun toString(): String {
            val(x,y) = point
            return "X$x,$y,$text"
        }
    }
    data class KvgStroke (
        var path: KvgCharPath? = null,
        var annotation: KvgAnnotation? = null
    )
    // This class puts the strokes and annotations together.
    // It also allows the paths which are identified by a
    // stroke number to be accessed as an ordered
    // zero indexed list.
    @Suppress("MemberVisibilityCanBePrivate")
    class KvgStrokeInfo {
        private val info = mutableMapOf<Int, KvgStroke>()
        val size: Int
            get() = info.size
        private val fromInfo = mutableListOf<KvgStroke>()
        private fun toInfoUpdate() {
            fromInfo.clear()
            for(i in info.keys.sorted()) {
                fromInfo.add(info[i]!!)
            }
        }
        private fun isEmpty(idx:Int) = info[idx] == null
        fun hasPath(idx:Int) : Boolean
            = idx < fromInfo.size && fromInfo[idx].path != null
        fun hasAnnotation(idx:Int) : Boolean
                = idx < fromInfo.size && fromInfo[idx].annotation != null
        fun putPath(
            strokeId: Int, path: KvgCharPath
        ):Boolean {
            val rv = when {
                (hasPath(strokeId)) -> false
                (isEmpty(strokeId)) -> {
                    info[strokeId] = KvgStroke(path = path)
                    true
                }
                else -> {
                    info[strokeId]!!.path = path
                    true
                }
            }
            if (rv)
                toInfoUpdate()
            return rv
        }
        fun putAnnotation(
            strokeId: Int, annotation: KvgAnnotation
        ):Boolean {
            val rv =  when {
                (hasAnnotation(strokeId)) -> false
                (isEmpty(strokeId)) -> {
                    info[strokeId] = KvgStroke(annotation = annotation)
                    true
                }
                else -> {
                    info[strokeId]!!.annotation = annotation
                    true
                }
            }
            if (rv)
                toInfoUpdate()
            return rv
        }
        fun getKvgStrokeInfo(idx:Int): KvgStroke? =
            if (idx <= fromInfo.lastIndex) fromInfo[idx]
                else null
        fun getKvgStrokesInfo(
            range: IntRange = 0..fromInfo.size
        ): List<KvgStroke> =
            range.map{fromInfo[it]}.toList()
        fun getAnnotation(idx: Int): KvgAnnotation?
                = getKvgStrokeInfo(idx)?.annotation
        fun getAnnotations(
            range: IntRange = 0..fromInfo.size
        ): List<KvgAnnotation> =
            range.mapNotNull { getAnnotation(it) }.toList()
        fun getKvgPath(idx: Int): KvgCharPath?
                = getKvgStrokeInfo(idx)?.path
        fun getKvgPaths(
            range: IntRange = 0..fromInfo.size
        ): List<KvgCharPath> =
            range.mapNotNull { getKvgPath(it) }.toList()
    }
    class KvgCharPath (strokeIn : String) {
        val absSegments = mutableListOf<KvgStrokePath>()
        init {
            var absX = 0f; var absY = 0f
            fun Array<Float>.toAbs() : Array<Float> {
                val rv = this.copyOf()
                for (i in 0..this.lastIndex step 2) {
                    rv[i] += absX; rv[i + 1] += absY
                }
                return rv
            }
            // for destructuring.
            operator fun <Float> Array<Float>.component6() = this[5]
            var xReflection = 0f; var yReflection = 0f
            @Suppress("UNUSED_VARIABLE")
            fun saveToAbsSeg(op: String, coord:Array<Float>) {
                when (op) {
                    "L", "M" -> {
                        absX = coord[0]; absY = coord[1]
                        absSegments.add(
                            KvgStrokePath(
                            op, coord)
                        )
                    }
                    "l", "m" -> {
                        saveToAbsSeg(op.uppercase(), coord.toAbs())
                    }
                    "c" -> {
                        var cc = coord.copyOf()
                        do {
                            val c = cc.sliceArray(0..5)
                            if (cc.isNotEmpty()) cc = cc.sliceArray(6..cc.lastIndex)
                            saveToAbsSeg("C", c.toAbs())
                        } while (cc.isNotEmpty())
                    }
                    "C" -> {
                        var cc = coord
                        do {
                            val c = cc.sliceArray(0..5)
                            if (cc.isNotEmpty()) cc = cc.sliceArray(6..cc.lastIndex)
                            val (x0,y0,x1,y1,x2,y2) = c
                            // calculate reflection in case next op is svg
                            // s/S shorthand bezier.
                            xReflection = (2*x2) - x1
                            yReflection = (2*y2) - y1
                            absX =  coord[coord.lastIndex - 1]; absY = coord[coord.lastIndex]
                            absSegments.add(
                                KvgStrokePath(
                                op, c)
                            )
                        } while (cc.isNotEmpty())
                    }
                    "s" -> saveToAbsSeg("S", coord.toAbs())
                    "S" -> {
                        val (x0,y0,x1,y1) = coord
                        saveToAbsSeg("C",
                            arrayOf(xReflection,yReflection,x0,y0,x1,y1))
                    }
                    else -> throw SvgConvertException (
                        "saveAbsSeg: unrecognized operator: \"$op\"")
                }
            }
            val segments = """([a-zA-Z][\d+.,-]+)""".toRegex()
                .findAll(strokeIn)
                .map { it.value }
                .toList()
            if (segments.isEmpty()) {
                throw SvgConvertException(
                    "KvgCharPath: no segments found in \"$segments\"")
            }
            for (seg in segments) {
                val (op, floatStr) = "\\s*([A-Za-z])\\s*([\\s\\d+.,-]+)".toRegex()
                    .find(seg)!!
                    .destructured
                val coord = "(-*\\d+(?:\\.\\d+)*)".toRegex()
                    .findAll(floatStr)
                    .map{it.value.toFloat()}
                    .toList().toTypedArray()
                saveToAbsSeg(op, coord)
            }
        }
        override fun toString(): String {
            return absSegments.joinToString("")
        }
    }
    val kvgStrokeInfo = KvgStrokeInfo()

    init {
        // Parse the input .abs file from the buffered
        // reader passed in and save it to thr
        // KvgStrokeInfo class.
        var line = ""
        var lineNumber = 1
        fun BufferedReader.nextLine (): Boolean {
            val l = readLine()
            line = l ?: ""
            lineNumber++
            return l != null
        }
        val opNoIdRegex = """(.)(.*)""".toRegex()
        val argToPathRegex ="""(^\d+)(.*)""".toRegex()
        val commasSplitRegex = """\s*,\s*""".toRegex()
        while (fileHandle.nextLine()) {
            if (line.isNotEmpty()){
                val (op, arg) = opNoIdRegex.find(line)!!.destructured
                when (op) {
                    "N" -> { name = arg }
                    "C" -> { renderChar = arg }
                    "W" -> {// dimensions
                        val (posX, posY) = arg
                            .split(",")
                            .map { it.toFloat() }
                            .toFloatArray()
                        dimensions = Pair(posX, posY)
                    }
                    "S" -> {
                        val (id, path) = argToPathRegex.find(arg)!!.destructured
                        kvgStrokeInfo.putPath(id.toInt(), KvgCharPath(path))
                    }
                    "X" -> {// text
                        val (id, posX, posY, text) = arg.split(commasSplitRegex)
                        kvgStrokeInfo.putAnnotation(id.toInt(), KvgAnnotation(
                            Pair(posX.toFloat(), posY.toFloat()), text
                        )
                        )
                    }
                }
            }
        }
    }
    override fun toString() : String {
        val pathsSize = kvgStrokeInfo.getKvgPaths().size
        val annotationsSize = kvgStrokeInfo.getAnnotations().size
        if (pathsSize != annotationsSize)
            throw RuntimeException("$TAG: expected same annotation and stroke count.\n" +
                    "Found $pathsSize paths vs" +
                    " $annotationsSize annotations")
        return arrayOf(
            "N$name",
            "C$renderChar",
            "W" + dimensions.toList().joinToString(","),
            "path count: $pathsSize",
            ""
        ).joinToString("\n")
    }
}

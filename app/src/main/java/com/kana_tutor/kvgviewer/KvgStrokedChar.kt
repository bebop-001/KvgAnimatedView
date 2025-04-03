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

package com.kana_tutor.kvgviewer

import java.io.BufferedReader

// our own personal exception.
class SvgConvertException(message:String) : Exception (message)

private const val TAG = "KvgStrokedChar"
// based on https://www.baeldung.com/kotlin-builder-pattern
class KvgStrokedChar (
    private val name : String,
    private val renderChar : Char,
    fileHandle : BufferedReader
) {
    // width/height
    private var _dimensions :  Pair<Float,Float>? = null
    val dimensions : Pair<Float,Float>
        get() {
            if (_dimensions == null) throw RuntimeException(
                "KvgStrokedChar $name: dimensions uninitialized"
            )
            return _dimensions!!
        }
    private val _strokes = mutableListOf<KvgStroke>()
    class KvgStrokeSegment(val op: String, val coord: Array<Float>) {
        override fun toString(): String {
            return op + coord.joinToString(",")
        }
    }
    val strokes :Array<KvgStroke>
        get() = _strokes.toTypedArray()
    data class KvgAnnotation(val point : Pair<Float,Float>, val text : String) {
        override fun toString(): String {
            val(x,y) = point
            return "X$x,$y,$text"
        }
    }
    private val _annotations = mutableListOf<KvgAnnotation>()
    val annotations : Array<KvgAnnotation>
        get() = _annotations.toTypedArray()
    class KvgStroke (strokeIn : String) {
        val segments = mutableListOf<KvgStrokeSegment>()
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
            fun saveAbsSeg(op: String, coord:Array<Float>) {
                when (op) {
                    "L", "M" -> {
                        absX = coord[0]; absY = coord[1]
                        segments.add(
                            KvgStrokeSegment(
                            op, coord)
                        )
                    }
                    "l", "m" -> {
                        saveAbsSeg(op.uppercase(), coord.toAbs())
                    }
                    "c" -> {
                        var cc = coord.copyOf()
                        do {
                            val c = cc.sliceArray(0..5)
                            if (cc.isNotEmpty()) cc = cc.sliceArray(6..cc.lastIndex)
                            saveAbsSeg("C", c.toAbs())
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
                            segments.add(
                                KvgStrokeSegment(
                                op, c)
                            )
                        } while (cc.isNotEmpty())
                    }
                    "s" -> saveAbsSeg("S", coord.toAbs())
                    "S" -> {
                        val (x0,y0,x1,y1) = coord
                        saveAbsSeg("C",
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
                    "KvgStroke: no segments found in \"$segments\"")
            }
            for (seg in segments) {
                val (op, floatStr) = "\\s*([A-Za-z])\\s*([\\s\\d+.,-]+)".toRegex()
                    .find(seg)!!
                    .destructured
                val coord = "(-*\\d+(?:\\.\\d+)*)".toRegex()
                    .findAll(floatStr)
                    .map{it.value.toFloat()}
                    .toList().toTypedArray()
                saveAbsSeg(op, coord)
                // println("nextLine" + "KvgStroke:Segments:${segments.map { it }}")
            }
        }
        override fun toString(): String {
            return segments.joinToString("")
        }
    }

    init {
        var line = ""
        var lineNumber = 1
        fun BufferedReader.nextLine (): Boolean {
            val l = readLine()
            line = l ?: ""
            lineNumber++
            return l != null
        }
        while (fileHandle.nextLine()) {
            if (line.isNotEmpty()){
                val (op, arg) = "(.)(.*)".toRegex()
                    .find(line)!!.destructured
                when (op) {
                    "N" -> {/* name */}
                    "C" -> {/* renderChar */}
                    "W" -> {// dimensions
                        val (posX, posY) = arg
                            .split(",")
                            .map { it.toFloat() }
                            .toFloatArray()
                        _dimensions = Pair(posX, posY)
                    }
                    "S" -> _strokes.add(KvgStroke(arg))
                    "X" -> {// text
                        val (posX, posY, text) = arg.split(",")
                        _annotations.add(KvgAnnotation(
                            Pair(posX.toFloat(), posY.toFloat()), text
                        ))
                    }
                }
            }
        }
    }
    override fun toString() : String {
        if (strokes.size != annotations.size)
            throw RuntimeException("$TAG: expected same annotation and stroke count.\n" +
                    "Found ${strokes.size} strokes vs" +
                    " ${annotations.size} annotations")
        val out = mutableListOf<String>()
        for(i in 0 until strokes.lastIndex) {
            out.add(strokes[i].toString())
            out.add(annotations[i].toString())
        }
        return arrayOf(
            "N$name",
            "C$renderChar",
            "W" + dimensions.toList().joinToString(","),
            out.joinToString("\n"),
            ""
        ).joinToString("\n")
    }
}

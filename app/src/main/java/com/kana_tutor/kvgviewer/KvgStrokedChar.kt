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
@file:Suppress("FunctionName", "LocalVariableName")

package com.kana_tutor.kvgviewer

import android.annotation.SuppressLint
import java.io.BufferedReader

// our own personal exception.
class SvgConvertException(message:String) : Exception (message)

// based on https://www.baeldung.com/kotlin-builder-pattern
class KvgStrokedChar (
    val name : String,
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
            // for destructure.
            operator fun <Float> Array<Float>.component6() = this[5]
            var xReflection = 0f; var yReflection = 0f
            @SuppressLint("DefaultLocale")
            fun saveAbsSeg(op: String, coords:Array<Float>) {
                when (op) {
                    "L", "M" -> {
                        absX = coords[0]; absY = coords[1]
                        segments.add(KvgStrokeSegment(
                            op, coords))
                    }
                    "l", "m" -> {
                        saveAbsSeg(op.toUpperCase(), coords.toAbs())
                    }
                    "c" -> {
                        var cc = coords.copyOf()
                        do {
                            val c = cc.sliceArray(0..5)
                            if (cc.isNotEmpty()) cc = cc.sliceArray(6..cc.lastIndex)
                            saveAbsSeg("C", c.toAbs())
                        } while (cc.isNotEmpty())
                    }
                    "C" -> {
                        var cc = coords
                        do {
                            val c = cc.sliceArray(0..5)
                            if (cc.isNotEmpty()) cc = cc.sliceArray(6..cc.lastIndex)
                            val (x0,y0,x1,y1,x2,y2) = c
                            // calculate reflection in case next op is svg
                            // s/S shorthand bezier.
                            xReflection = (2*x2) - x1
                            yReflection = (2*y2) - y1
                            absX =  coords[coords.lastIndex - 1]; absY = coords[coords.lastIndex]
                            segments.add(KvgStrokeSegment(
                                op, c))
                        } while (cc.isNotEmpty())
                    }
                    "s" -> saveAbsSeg("S", coords.toAbs())
                    "S" -> {
                        val (x0,y0,x1,y1) = coords
                        saveAbsSeg("C",
                            arrayOf(xReflection,yReflection,x0,y0,x1,y1))
                    }
                    else -> throw SvgConvertException (
                        "saveAbsSeg: unrecognized operator: \"$op\"")
                }
            }
            val segments = "(\\s*[a-zA-Z]\\s*[\\s\\d+\\.,-]+)".toRegex()
                .findAll(strokeIn)
                .map { it.value }
                .toList()
            if (segments.isEmpty()) {
                throw SvgConvertException(
                        "KvgStroke: no segments found in \"$segments\"")
            }
            for (seg in segments) {
                val (op, floatStr) = "\\s*([A-Za-z])\\s*([\\s\\d+\\.,-]+)".toRegex()
                    .find(seg)!!
                    .destructured
                val coords = "(-*\\d+(?:\\.\\d+)*)".toRegex()
                    .findAll(floatStr)
                    .map{it.value.toFloat()}
                    .toList().toTypedArray()
                saveAbsSeg(op, coords)
                // println("nextLine" + "KvgStroke:Segments:${segments.map { it }}")
            }
        }
        override fun toString(): String {
            return segments.joinToString("")
        }
    }

    init {
        val widthHeightRegex = "^\\s*<svg.*\\s+width=\"(\\d+).*height=\"(\\d+)".toRegex()
        val pathRegex = "^\\s*<path.*=\"([^\"]+)\"".toRegex()
        val  textRegex = arrayOf(
                "^\\s*<text.*matrix\\([^)]+",   // text starts with "<text transform="
                "\\s+(\\d+(?:\\.\\d+.)*)",          // Followed by the a transform matrix.
                "\\s+(\\d+(?:\\.\\d+)*)\\)[^>]+>", // the last values in the matrix are x,y
                "([^<]+)")                      // and the text.
            .joinToString("").toRegex()
        var _findResult : MatchResult? = null
        fun Regex._find(str: String) : Boolean {
            _findResult = find(str)
            return _findResult != null
        }
        var line = ""
        var lineNumber = 1
        fun BufferedReader.nextLine (): Boolean {
            val l = readLine()
            line = l ?: ""
            lineNumber++
            return l != null
        }
        var isXml : Boolean? = null
        while (fileHandle.nextLine()) {
            if (isXml == null)
                isXml = "^<\\?xml\\s+".toRegex().find(line) != null
            else if (isXml) {
                // println("nextLine" + "$lineNumber:$line")
                when {
                    widthHeightRegex._find(line) -> {
                        val (width, height) = _findResult!!.destructured
                        _dimensions = Pair(width.toFloat(), height.toFloat())
                        // println("nextLine" + "dimensions: $dimensions")
                    }
                    pathRegex._find(line) -> {
                        // println("strokedChar" + ">>${_findResult!!.groupValues[1]}")
                        val stroke = KvgStroke(_findResult!!.groupValues[1])
                        _strokes.add(stroke)
                        // println("strokedChar" + "<<${stroke}")
                    }
                    textRegex._find(line) -> {
                        val (posX, posY, text) =
                            _findResult!!.destructured
                        _annotations.add(KvgAnnotation(
                            Pair(posX.toFloat(), posY.toFloat()), text
                        ))
                    }
                }
            }
        }
    }
    override fun toString() : String {
        return arrayOf(
            "N" + name,
            "C" + renderChar,
            "W" + dimensions.toList().joinToString(","),
            strokes.map{"S" + it.toString()}.toList().joinToString("\n"),
            annotations.map{it.toString()}.toList().joinToString("\n"),
            ""
        ).joinToString("\n")
    }
}

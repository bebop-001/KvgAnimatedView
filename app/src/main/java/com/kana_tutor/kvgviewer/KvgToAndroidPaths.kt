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
@file:Suppress("LiftReturnOrAssignment", "SpellCheckingInspection", "LocalVariableName", "CanBeVal",
    "CascadeIf"
)

package com.kana_tutor.kvgviewer

import android.content.Context
import android.graphics.Path
import android.util.Log
import android.widget.Toast
import java.io.BufferedReader

import kotlin.text.RegexOption.IGNORE_CASE

// our own personal exception.
class SvgConvertException(message:String)
    : Exception (message) {
    constructor(op:String, found:Int, expected:Int, segment:String) :
            this(String.format("op:%s expected %d args found %d, segment:%s",
                op, found, expected, segment))
    constructor(renderChar:Char, isPathFile: Boolean, error:String) :
            this(String.format("KvgToAndroidPaths filed for \"%c\" %s file:" +
                    "Exception:%s", renderChar,
                        if (isPathFile) "path file " else "svg file ",
                error))
}
// to destructure more than 5 args.
private operator fun FloatArray.component6() = this[5]

// class for conversion of SVG path operators to Android Graphics 
// path operators.
private class SvgPath : Path() {
    // reflection control points.  Used for SVG s/S bezier shorthand.
    var xR  = 0f; var yR = 0f
    // Current absolute x/y point.  Used for conversion from
    // relative to absolute coordinate system.
    var absX = 0f; var absY = 0f

    // convert the SVG opp passed in and add it to the path.
    fun convert(op : String, coords : FloatArray) {
        // error check.  Throw an exception unless the array passed in is the
        // expected size.
        fun FloatArray.check(op : String,  expected:Int) : FloatArray {
            if (this.size != expected)
                throw SvgConvertException(op, expected, this.size,
                    "$op${this.joinToString(",")}")
            return this
        }
        // because of "s" svg shorthand bezier operator, it's easier
        // to convert everything to absolute coordinate space.  To do
        // that, add the last absolute x y value to the input array.
        fun FloatArray.relToAbs(x:Float, y:Float) : FloatArray {
            // odd index is x, even is y.
            for (i in this.indices step 2) {
                this[i] += x; this[i+1] += y
            }
            return this
        }

        Log.d("convert", "$op ${coords.map{it.toString()}}")
        when (op) {
            "l" -> {
                convert("L", coords.relToAbs(absX, absY))
            }
            "L" -> {
                val (x,y) = coords.check(op, 2)
                super.lineTo(x,y)
                absX = x; absY = y
            }
            "m" -> {
                convert("M", coords.relToAbs(absX, absY))
            }
            "M" -> {
                val(x,y) = coords.check(op, 2)
                super.moveTo(x,y)
                absX = x; absY = y
            }
            "c" -> {
                var cc = coords
                do {
                    val c = cc.sliceArray(0..5)
                    if (cc.isNotEmpty()) cc = cc.sliceArray(6..cc.lastIndex)
                    Log.d("convert", "c:${c.map{it.toString()}}")
                    convert("C", c.relToAbs(absX, absY))
                } while (cc.isNotEmpty())
            }
            "C" -> {
                var cc = coords
                do {
                    val c = cc.sliceArray(0..5)
                    Log.d("convert", "C:${c.map{it.toString()}}")
                    if (cc.isNotEmpty()) cc = cc.sliceArray(6..cc.lastIndex)
                    val (x0,y0,x1,y1,x2,y2) = c
                    super.cubicTo(x0,y0,x1,y1,x2,y2)
                    // calculate reflection in case next op is svg
                    // s/S shorthand bezier.
                    xR = (2*x2) - x1
                    yR = (2*y2) - y1
                    absX =  coords[coords.lastIndex - 1]; absY = coords[coords.lastIndex]
                } while (cc.isNotEmpty())
            }
            "s" -> {
                convert("S", coords.relToAbs(absX, absY))
            }
            "S" -> {
                val (x0,y0,x1,y1) = coords.check(op, 4)
                convert("C", floatArrayOf(xR,yR,x0,y0,x1,y1))
            }
            else -> throw SvgConvertException ("unrecognized operator: \"$op\"")
        }
    }
}
data class PositionedTextInfo(val x : Float, val y:Float, val text:String)

// these read*File routines are separated so we can debug outside of Android.
// path files are pre-parsed SVG files.
fun readPathFile(reader:BufferedReader) :
        Pair<Array<String>,Array<PositionedTextInfo>>
{
    val pathInfo = mutableListOf<String>()
    val textInfo = mutableListOf<PositionedTextInfo>()
    var line : String
    nextLine@while (reader.readLine().also{line = it} != null) {
        if (line.matches("^\\s*#.*".toRegex())) continue@nextLine
        if (line.startsWith("x",true)) {
            val(x, y, text) = "^x([^,]+),(^,]+),(.*)"
                .toRegex(IGNORE_CASE)
                .find(line)!!
                .destructured.toList()
            textInfo.add(PositionedTextInfo(x.toFloat(), y.toFloat(), text))
        }
        else
            pathInfo.add(line)
    }
    return Pair(pathInfo.toTypedArray(),textInfo.toTypedArray())
}
val widthHeightRegex = "^\\s*<svg.*\\s+width=\"(\\d+).*height=\"(\\d+)".toRegex()
val pathRegex = "^\\s*<path.*=\"([^\"]+)\"".toRegex()
const val tr = "^\\s*<text.*matrix\\([^)]+" +  // text starts with "<text transform="
    "\\s+((?:\\d+.)\\d+)" +                     // Followed by the a transform matrix.
    "\\s+((?:\\d+.)\\d+)\\)[^>]+>" +            // the last values in the matrix are x,y
    "([^<]+)"                                   // and the text.
val textRegex = tr.toRegex()
// Parse the svg file.  We pull out path and text info here.
fun readSvgFile(reader: BufferedReader) :
        Pair<Array<String>,Array<PositionedTextInfo>>
{
    val pathInfo = mutableListOf<String>()
    val textInfo = mutableListOf<PositionedTextInfo>()
    var lineCounter = 0
    var matchResult : MatchResult?
    var inXmlComment = false
    val commentOpen = "<!--".toRegex()
    val commentClose = "-->".toRegex()
    var line : String?
    do {
        line = reader.readLine()
        lineCounter++
        Log.d("lineCounter", "$lineCounter")
        if (line.isNullOrEmpty())
            continue
        else if (inXmlComment) {
            if (commentClose.matches(line)) {
                inXmlComment = false
            }
            continue
        } else if (commentOpen.matches(line)) {
            inXmlComment = true
            continue
        } else if (pathRegex.find(line).also { matchResult = it } != null) {
            pathInfo.add(matchResult!!.groupValues[1])
        } else if (widthHeightRegex.find(line).also { matchResult = it } != null) {
            val (width, height) = matchResult!!.destructured
            pathInfo.add("$width $height")
        } else if (textRegex.find(line).also { matchResult = it } != null) {
            // text is <text, a translation matrix, then text.
            // I'm rerpresenting that with 'x$matrix:$text where matrix is the
            // comma separated values from the matrix
            val (x, y, text) = matchResult!!.destructured
            textInfo.add(PositionedTextInfo(x.toFloat(), y.toFloat(), text))
        }
    } while (line != null)
    Log.d("lineCounter", "got here")
    return Pair(pathInfo.toTypedArray(),textInfo.toTypedArray())
}

const val USE_PATH_FILES = false
class KvgToAndroidPaths(context: Context, val renderChar: Char) {
    var strokePaths : Array<Path>? = null
    var strokeIdText : Array<PositionedTextInfo>? = null
    var width = 0f
        private set
    var height = 0f
        private set

    // convert a string with kanjiVg paths to Android paths.
    private fun List<String>.strokesToPath( ): Path {
        val svgPath = SvgPath()
        for (stroke in this) {
            val mr = "([mltsc])([^mltsc]+)".toRegex(IGNORE_CASE).find(stroke)
            val (op, floatString) = mr!!.destructured
            val coord = "(-?\\d+(?:\\.\\d+)*)".toRegex()
                .findAll(floatString)
                .map { it.groupValues[1].toFloat() }
                .toList()
                .toFloatArray()
            svgPath.convert(op, coord)
        }
        return svgPath
    }
    private fun svgInfoToGraphicInfo(svgInfo : Array<String>)
            : Array<Path>
    {
        var matchResult : MatchResult? = null
        var sequenceMatch : Sequence<MatchResult>? = null
        fun Regex.sequenceFind(string:String) : Boolean {
            sequenceMatch = this.findAll(string)
            return sequenceMatch!!.count() > 0
        }
        val strokes = mutableListOf<Path>()
        for(l in svgInfo) {
            var line = l
            // for .pat files, first char is char for file.
            // discard for now.
            if ("^[^\\u0000-\\u007F]\\s+(.*)".toRegex()
                    .find(line)
                    .also{matchResult} != null) {
                line = matchResult!!.groupValues[2]
            }
            if (width == 0f) {
                if ("\\d+".toRegex().sequenceFind(line)) {
                    val w_h = sequenceMatch!!
                        .map{it.value.toFloat()}
                        .toList()
                    if (w_h.size == 2) {
                        width = w_h[0]
                        height = w_h[1]
                    }
                }
                if (width == 0f) throw SvgConvertException(
                    "failed to find width/height at first non-comment line."
                )
            }
            // unfortunately Kotlin Lint doesn't grok it's own .also crap.
            else if ("(?:([mltsc])([^mltsc]+))".toRegex(IGNORE_CASE)
                    .sequenceFind(line)) {
                val pathOps = sequenceMatch!!.map{it.value}.toList()
                if (pathOps.isEmpty()) {
                    throw throw SvgConvertException(
                        "No SVG path operations found in \"$line\"")
                }
                try {
                    strokes.add(pathOps.strokesToPath())
                }
                catch (e : Exception) {
                    throw SvgConvertException(renderChar, USE_PATH_FILES,
                        "path exception for \"$line\":${e}")
                }
            }
            // Log.d("kvgToAndroidPath", "found ${strokes.size}")
        }
        return strokes.toTypedArray()
    }
    init {
        var pathInfo : Pair<Array<String>,Array<PositionedTextInfo>>? = null
        try {
            if (USE_PATH_FILES) {
                val reader = context.assets.open(
                    String.format("paths/%05x.pat", renderChar.toInt())
                ).bufferedReader()
                pathInfo = readPathFile(reader)
            } else {
                val reader = context.assets.open(
                    String.format("svg/%05x.svg", renderChar.toInt())
                ).bufferedReader()
                pathInfo = readSvgFile(reader)
            }
        }
        catch (e: java.lang.Exception) {
            Toast.makeText(
                context, "Read for \"$renderChar FAILED:${e.message}", Toast.LENGTH_LONG
            ).show()
        }
        if (pathInfo != null) {
            val (strokePathInfo, strokeTextInfo) = pathInfo
            KvgToAndroidPaths@ this.strokePaths = svgInfoToGraphicInfo(strokePathInfo)
            KvgToAndroidPaths@ this.strokeIdText = strokeTextInfo
        }
    }
}

@file:Suppress("LiftReturnOrAssignment", "SpellCheckingInspection")

package com.kana_tutor.kvgviewer

import android.content.Context
import android.graphics.Matrix
import android.graphics.Path
import android.util.Log
import java.io.BufferedReader

// our own personal exception.
class SvgConvertException(message:String)
    : Exception (message) {
    constructor(op:String, found:Int, expected:Int) :
            this(String.format("op:%s expected %d args found %d",
                op, found, expected))
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
                throw SvgConvertException(op, expected, this.size)
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
                convert("C", coords.relToAbs(absX, absY))
            }
            "C" -> {
                val (x0,y0,x1,y1,
                    x2,y2) = coords.check(op, 6)
                super.cubicTo(x0,y0,x1,y1,x2,y2)
                // calculate reflection in case next op is svg
                // s/S shorthand bezier.
                xR = (2*x2) - x1
                yR = (2*y2) - y1
                absX =  coords[coords.lastIndex - 1]; absY = coords[coords.lastIndex]
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
data class PositionedTextInfo(val text:String, val tMatrix: Matrix) {

}
// these read*File routines are separated so we can debug outside of Android.
// path files are pre-parsed SVG files.
private fun readPathFile(reader:BufferedReader) : MutableList<String> {
    var line = reader.readLine()
    val svgInfo = mutableListOf<String>()
    while (line != null) {
        if (!line.matches("^\\s*#.*".toRegex()))
            svgInfo.add(line)
        line = reader.readLine()
    }
    return svgInfo
}
// Parse the svg file.  We pull out path and text info here.
private fun readSvgFile(reader: BufferedReader) : MutableList<String> {
    var line = reader.readLine()
    val svgInfo = mutableListOf<String>()
    while (line != null) {
        if (!line.matches("^\\s*#.*".toRegex()))
            svgInfo.add(line)
        line = reader.readLine()
    }
    return svgInfo
}

const val USE_PATH_FILES = true
class KvgToAndroidPaths(context: Context, private val renderChar: Char) {
    val strokePaths : Array<Path>
    private val strokeIdText : Array<PositionedTextInfo>?
    val charPath = Path()
    var width = 0f
        private set
    var height = 0f
        private set

    // convert a string with kanjiVg paths to Android paths.
    private fun List<String>.strokesToPath( ): Path {
        val svgPath = SvgPath()
        for (stroke in this) {
            val mr = "([a-zA-Z])([^a-zA-Z]+)".toRegex().find(stroke)
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
    private fun MutableList<String>.svgInfoToGraphicInfo()
            : Pair<Array<Path>, Array<PositionedTextInfo>?>
    {
        var line = removeAt(0)
        var matchResult : MatchResult? = null
        fun Regex.matchFind(string:String) : Boolean {
            matchResult = this.find(string)
            return matchResult == null
        }
        var sequenceMatch : Sequence<MatchResult>? = null
        fun Regex.sequenceFind(string:String) : Boolean {
            sequenceMatch = this.findAll(string)
            return sequenceMatch!!.count() > 0
        }
        val strokes = mutableListOf<Path>()
        while (isNotEmpty()) {
            // for .pat files, first char is char for file.
            // discard for now.
            if ("^(\\S+)\\s+(.*)".toRegex().matchFind(line)) {
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
            else if ("(?:([mltsc])([^mltsc]+))".toRegex(RegexOption.IGNORE_CASE)
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
            Log.d("kvgToAndroidPath", "found ${strokes.size}")
            line = removeAt(0)
        }
        return Pair(strokes.toTypedArray(), null)
    }
    private val svgInfo : List<String>
    init {
        if(USE_PATH_FILES) {
                val reader = context.assets.open(
                    String.format("paths/%05x.pat", renderChar.toInt())
                ).bufferedReader()
                svgInfo = readPathFile(reader)
        }
        else {
            val reader = context.assets.open(
                String.format("svg/%05x.svg", renderChar.toInt())
            ).bufferedReader()

            svgInfo = readSvgFile(reader)
        }
        val graphInfo = svgInfo.svgInfoToGraphicInfo()
        val(strokePaths, strokeIdText) = graphInfo
        KvgToAndroidPaths@this.strokePaths = strokePaths
        KvgToAndroidPaths@this.strokeIdText = strokeIdText
    }
}

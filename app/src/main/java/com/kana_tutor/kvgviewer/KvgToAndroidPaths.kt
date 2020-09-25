package com.kana_tutor.kvgviewer

import android.content.Context
import android.graphics.Path
import android.util.Log

// our own personal exception.
class SvgConvertException(message:String)
    : Exception (message) {
    constructor(op:String, found:Int, expected:Int) :
            this(String.format("op:%s expected %d args found %d",
                op, found, expected))
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

    // error check.  Throw an exception unless the array passed in is the
    // expected size.
    private fun FloatArray.check(op : String,  expected:Int) : FloatArray {
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
    // convert the SVG opp passed in and add it to the path.
    fun convert(op : String, coords : FloatArray) {
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

class KvgToAndroidPaths(context: Context, private val renderChar: String) {
    var strokePaths:Array<Path>
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
    init {
        val paths = mutableListOf<Path>()
        val charsIn = renderChar.toCharArray()
        val fnameIn = String.format("paths/%05x.pat", charsIn[0].toInt())
        val reader = context.assets.open(fnameIn).bufferedReader()
        var lineNumber = 0
        var line = reader.readLine()
        var strokeCounter = 1
        while (line != null) {
            lineNumber++
            if (!line.matches("^\\s*#.*".toRegex())) {
                val match = "^(\\S+)\\s+(.*)".toRegex().find(line)
                if (match != null) {
                    val (ch, strokesStr) = match.destructured
                    if (width == 0f) {
                        val m = "\\d+".toRegex().findAll(strokesStr).toList()
                        if (m.size == 2) {
                            width = m[0].value.toFloat()
                            height = m[1].value.toFloat()
                        }
                        else throw java.lang.RuntimeException(
                            "KvgToAndroidPaths:Failed to find width/height on first line.")
                    }
                    else {
                        val strokes = "([a-zA-Z][^a-zA-Z]+)".toRegex()
                            .findAll(strokesStr)
                            .map { it.groupValues[0] }
                            .toList()
                        try {
                            Log.d("convert", "Stroke ${strokeCounter++}")
                            paths.add(strokes.strokesToPath())
                        }
                        catch (e : Exception) {
                            throw RuntimeException(
                                "File $fnameIn, line $lineNumber, path exception:${e}")
                        }
                    }
                }
            }
            line = reader.readLine()
        }
        Log.d("kvgToAndroidPath", "found ${paths.size}")
        strokePaths = paths.toTypedArray()
        strokePaths.forEach { charPath.addPath(it) }
    }
}

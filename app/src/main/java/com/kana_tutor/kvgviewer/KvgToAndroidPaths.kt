package com.kana_tutor.kvgviewer

import android.content.Context
import android.graphics.Path
import android.util.Log

class BadArgsException(message:String)
    : Exception (message) {
    constructor(fname:String, op:String, found:Int, expected:Int) :
            this(String.format("file:%s op:%s expected %d args found %d",
                fname, op, found, expected))
    constructor(op:String, found:Int, expected:Int) :
            this(String.format("op:%s expected %d args found %d",
                op, found, expected))
}
private operator fun FloatArray.component6() = this[5]

private class SvgPath : Path() {
    private var previousOp = ""
    var xR  = 0f; var yR = 0f; var xM = 0f; var yM = 0f

    private fun FloatArray.check(op : String,  expected:Int) : FloatArray {
        if (this.size != expected)
            throw BadArgsException(op, expected, this.size)
        return this
    }
    fun apply(op : String, coords : FloatArray) {
        when (op) {
            "l" -> {
                val (x,y) = coords.check(op, 2)
                super.rLineTo(x,y)
                previousOp = op
            }
            "L" -> {
                val (x,y) = coords.check(op, 2)
                super.lineTo(x,y)
                previousOp = op
            }
            "m" -> {
                val(x,y) = coords.check(op, 2)
                super.rMoveTo(x,y)
                previousOp = op
            }
            "M" -> {
                val(x,y) = coords.check(op, 2)
                super.moveTo(x,y)
                previousOp = op
            }
            "c" -> {
                val (x0,y0,x1,y1,
                    x2,y2) = coords.check(op, 6)
                super.rCubicTo(x0,y0,x1,y1,x2,y2)
                previousOp = op
                xR = (2*x2) - x1
                yR = (2*y2) - y1
                xM = x2
                yM = y2
            }
            "C" -> {
                val (x0,y0,x1,y1,
                    x2,y2) = coords.check(op, 6)
                super.cubicTo(x0,y0,x1,y1,x2,y2)
                previousOp = op
                xR = (2*x2) - x1
                yR = (2*y2) - y1
                xM = x2
                yM = y2
            }
            "s" -> {
                if (previousOp != "c")
                    throw BadArgsException(
                        "attempt to call \"s\" after non- \"c\" operation")
                val (x0,y0,x1,y1) = coords.check(op, 4)
                super.rMoveTo(xM, yM)
                super.rCubicTo(xR,yR,x0,y0,x1,y1)
                previousOp = op
            }
            "S" -> {
                if (previousOp != "C")
                    throw BadArgsException(
                        "attempt to call \"s\" after non- \"c\" operation")
                val (x0,y0,x1,y1) = coords.check(op, 4)
                super.moveTo(xM, yM)
                super.cubicTo(xR,yR,x0,y0,x1,y1)
                previousOp = op
            }
            else -> throw BadArgsException ("unrecognized operator: \"$op\"")
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
            svgPath.apply(op, coord)
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

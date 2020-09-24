package com.kana_tutor.kvgviewer

import android.content.Context
import android.graphics.Path
import android.util.Log
import java.lang.Exception
import java.util.*


private class BezierCurve(
    val x0:Float, val y0:Float,
    val x1:Float, val y1:Float,
    val x2:Float, val y2:Float,
    val isRelative: Boolean)
{
    val xR = (2*x2) - x1
    val yR = (2*y2) - y1
    val xM = x2; val yM = y2
    var isEmpty = false
    fun apply(path:Path) : BezierCurve {
        with (path) {
            if (isRelative) {
                rCubicTo(x0,y0,x1,y1,x2,y2)
            }
            else {
                cubicTo(x0,y0,x1,y1,x2,y2)
            }
            this@BezierCurve.isEmpty = false
        }
        return this
    }
    fun applyShortHand(xx0:Float, yy0:Float, xx1:Float, yy1:Float,
                       path:Path, isRelative:Boolean) : BezierCurve
    {
        if (isEmpty) {
            throw java.lang.RuntimeException(
                "Attempt to \"applyShorthand\" with empty object")
        }
        if (isRelative != this.isRelative) {
            throw java.lang.RuntimeException(
                if (isRelative) "Attempt to absolute\"applyShorthand\" with relative object"
                else "Attempt to absolute\"applyShorthand\" with relative object")
        }
        with (path) {
            if (isRelative) {
                rMoveTo(xM, yM)
                rCubicTo(xR,yR,xx0,yy0,xx1,yy1)
            }
            else {
                moveTo(xM,yM)
                cubicTo(x0,y0,x1,y1,x2,y2)
            }
            this@BezierCurve.isEmpty = true
        }
        return this
    }

    constructor() : this(0f,0f,0f,0f,
        0f,0f, false) {isEmpty = true}

}

class KvgToAndroidPaths(context: Context, val renderChar: String) {
    var strokePaths:Array<Path>
    val charPath = Path()
    var width = 0f
        private set
    var height = 0f
        private set

    // convert a string with kanjiVg paths to Android paths.
    private fun List<String>.toPath( ): Path {
        val path = Path()
        for (stroke in this) {
            val mr = "([a-zA-Z])([^a-zA-Z]+)".toRegex().find(stroke)
            val (op, floatString) = mr!!.destructured
            val coord = "(-?\\d+(?:\\.\\d+)*)".toRegex()
                .findAll(floatString)
                .map { it.groupValues[1].toFloat() }
                .toList()
                .toFloatArray()
            var previous = BezierCurve()
            with (path) {
                when (op) {
                    "m" -> rMoveTo(coord[0], coord[1])
                    "M" -> moveTo(coord[0], coord[1])
                    "c" -> {
                        previous = BezierCurve(
                            coord[0], coord[1],
                            coord[2], coord[3],
                            coord[4], coord[5],
                            true)
                        .apply(this)
                    }
                    "C" -> {
                        previous = BezierCurve(
                            coord[0], coord[1],
                            coord[2], coord[3],
                            coord[4], coord[5],
                            false)
                        .apply(this)
                    }
                    "s" -> {
                        previous.applyShortHand(
                            coord[0], coord[1],
                            coord[2], coord[3],
                            this, true
                        )
                    }
                    "S" -> {
                        previous.applyShortHand(
                            coord[0], coord[1],
                            coord[2], coord[3],
                            this, false
                        )
                    }
                    "l" -> rLineTo(coord[0], coord[1])
                    "L" -> lineTo(coord[0], coord[1])
                    else -> throw MissingResourceException (
                        "${renderChar}: operator:$op, Stroke:\"$stroke\"", javaClass.simpleName,
                        "Stroke:$stroke")
                }
            }
        }
        return path
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
                            paths.add(strokes.toPath())
                        }
                        catch (e : Exception) {
                            throw RuntimeException(
                                "File $fnameIn, line $lineNumber, path exception:${e.message}")
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

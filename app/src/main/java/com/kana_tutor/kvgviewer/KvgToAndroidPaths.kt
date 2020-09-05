package com.kana_tutor.kvgviewer

import android.content.Context
import android.graphics.Path
import android.util.Log

// Convert the input Kvg file to a set of Android graphic paths.
// Each output path corresponds to a path in the Kvg file.
class KvgToAndroidPaths(context: Context, renderChar: String) {
    val strokePaths:Array<Path>
    val charPath = Path()
    init {
        val paths = mutableListOf<Path>()
        val charsIn = renderChar.toCharArray()
        val fnameIn = String.format("paths/kanjivg.kana.%04x.pat", charsIn[0].toInt())
        val reader = context.assets.open(fnameIn).bufferedReader()
        var lineNumber = 0
        var line = reader.readLine()
        while (line != null) {
            lineNumber++
            if (!line.matches("^\\s*#.*".toRegex())) {
                val lineSplit = line.split("\\s+".toRegex())
                if (lineSplit[0] != renderChar) {
                    throw java.lang.RuntimeException(
                        "parsePathsFile: Bad line $lineNumber: expected $renderChar but found ${lineSplit[0]}"
                    )
                }
                if (lineSplit.size == 2) {
                    // First line of path.  Consists of path and number of paths in file.
                    Log.d("kvgToAndroidPath", "char  = \"${lineSplit[0]}\"")
                }
                else {
                    paths.add(convertPath(lineSplit.slice(1..lineSplit.lastIndex)))
                }
            }
            line = reader.readLine()
        }
        Log.d("kvgToAndroidPath", "found ${paths.size}")
        strokePaths = paths.toTypedArray()
    }
    // convert a string with kanjiVg paths to Android paths.
    private fun convertPath(strokes: List<String>): Path {
        val path = Path()
        for (stroke in strokes) {
            val (op, floatStr) = stroke.split(":".toRegex())
            val coord = floatStr
                .split(",".toRegex())
                .map { it.toFloat() }
                .toFloatArray()
            for (p in arrayOf(path, charPath)) {
                when (op) {
                    "m" -> p.rMoveTo(coord[0], coord[1])
                    "M" -> p.moveTo(coord[0], coord[1])
                    "c" -> p.rCubicTo(
                        coord[0], coord[1],
                        coord[2], coord[3], coord[4], coord[5]
                    )
                    "C" -> p.cubicTo(
                        coord[0], coord[1], coord[2], coord[3],
                        coord[4], coord[5]
                    )
                    "l" -> p.rLineTo(coord[0], coord[1])
                    "L" -> p.lineTo(coord[0], coord[1])
                    else -> throw RuntimeException(
                        "Translate paths: Unrecognized operator: \"$op\""
                    )
                }
            }
        }
        return path
    }
}

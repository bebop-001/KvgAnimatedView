package com.kana_tutor.kvgviewer

import java.io.File

class TestFileReader (val renderChar : Char) {
    val svgFile = File(
        String.format("app/src/main/assets/svg/%05x.svg", renderChar.toInt())
    )
    val bufferedReader = svgFile.bufferedReader()
    val svgInfo = readSvgFile(bufferedReader)
}

fun main(args: Array<String>) {
    println("hello world")
    val t = TestFileReader('仍')
}

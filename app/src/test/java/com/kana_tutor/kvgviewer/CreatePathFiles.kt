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
package com.kana_tutor.kvgviewer

import java.io.BufferedReader
import java.io.File

fun getStrokedChar(svgFileName:String) : KvgStrokedChar? {
    val matchResult = "^.*/([\\d+a-fA-f]+)"
        .toRegex()
        .find(svgFileName)
    var renderChar : Char ? = null
    var name :String? = null
    if (matchResult != null) {
        name = matchResult.groups[1]!!.value
        val int  = name.toInt(16)
        renderChar = int.toChar()
    }
    val reader : BufferedReader? = null
    val fileHandle = File(svgFileName)
    var bufferedReader : BufferedReader? = null
    if (fileHandle.exists()) {
        bufferedReader = fileHandle.bufferedReader()
    }
    return KvgStrokedChar(name!!, renderChar!!, bufferedReader!!)
}
// create a path file in resources/path for each kanji svg file
var sortHash = mutableMapOf<String,Int>()
var sortedList = mutableListOf<String>()

fun main(args: Array<String>) {
    for (svgName in File("resources/kanji/").list()) {
        if (svgName.endsWith(".svg")) {
            var matchResult = "^([\\da-fA-F]+)".toRegex().find(svgName)
            val idx = matchResult!!.groups[1]!!.value!!.toInt(16)
            if (idx in 0x3400..0x4DB5 || idx in 0x4E00..0x9FCB || idx in 0xF900..0xFA6A) {
                sortHash[svgName] = idx
                sortedList.add(svgName)
            }
        }
    }
    // output list in sorted order to make it a bit easier to see what's going on.
    sortedList.sortWith(compareBy{it -> sortHash[it]})
    var i = 1
    for (svgName in sortedList) {
        val matchResult = "^(.*)\\.svg$".toRegex().find(svgName)
        val pathFileName = "resources/path/" +
            (matchResult!!.groups[1]!!.value) + ".pat";
        val strokedChar =
            getStrokedChar("resources/kanji/$svgName")
        File(pathFileName).writeText(strokedChar.toString())
        println("${i++}:${sortHash[svgName]!!.toChar()}:$svgName")
    }
}

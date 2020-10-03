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

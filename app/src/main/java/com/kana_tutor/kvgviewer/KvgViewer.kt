/*
 * Copyright 2025 Steven Smith kana-tutor.com
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

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Environment
import androidx.core.content.ContextCompat
import java.io.File

private const val TAG = "KvgViewer"
class KvgViewer: Application() {
    companion object {
        lateinit var appContext: Context
        lateinit var appFilesDir: File

        lateinit var notoSansBold: Typeface
        lateinit var notoSansRegular: Typeface

        lateinit var externalStorageRoot: File
        lateinit var homeDir: File
        lateinit var tmpDir: File
        lateinit var userPreferences: SharedPreferences
    }
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        appFilesDir = filesDir

        userPreferences = getSharedPreferences("$TAG.user_prefs", MODE_PRIVATE)

        notoSansRegular =
            Typeface.createFromAsset(assets,
                "fonts/noto-sans.regular.ttf")
        notoSansBold =
            Typeface.createFromAsset(assets,
                "fonts/noto-sans.bold.ttf")

        if (
            Environment.getExternalStorageState() in
            setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY) &&
            Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        ) {
            // last() seems to get sd ram if any.
            externalStorageRoot =
                ContextCompat.getExternalFilesDirs(applicationContext, null).last()
            homeDir = externalStorageRoot
            tmpDir = File(externalStorageRoot, "/tmp")
            if (!tmpDir.exists() && !tmpDir.mkdir())
                throw RuntimeException("$TAG: create $tmpDir FAILED")
        }
        else {
            throw RuntimeException("$TAG: Can't access SDK Memory.")
        }
    }
}

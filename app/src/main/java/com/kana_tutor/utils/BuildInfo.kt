package com.kana_tutor.utils
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

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.TypedValue
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.kana_tutor.kvgviewer.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import com.kana_tutor.kvgviewer.R

fun Activity.displayBuildInfo() : Boolean {
    val appInfo: ApplicationInfo =
        if (Build.VERSION.SDK_INT >= 33) {
            packageManager.getApplicationInfo(
                packageName,
                PackageManager.ApplicationInfoFlags.of(0)
            )
        }
        else {
            packageManager.getApplicationInfo(
                packageName, PackageManager.GET_META_DATA)
        }


    val installTimestamp = File(appInfo.sourceDir).lastModified()

    val buildInfoStr = getString(
        R.string.build_info_query,
        getString(R.string.app_name),
        BuildConfig.APPLICATION_ID,
        BuildConfig.VERSION_CODE,
        BuildConfig.VERSION_NAME,
        BuildConfig.BRANCH_NAME,
        BuildConfig.COMMIT_DATE,
        SimpleDateFormat.getInstance().format(
            java.util.Date(BuildConfig.BUILD_TIMESTAMP)
        ),
        SimpleDateFormat.getInstance().format(
            java.util.Date(installTimestamp)
        ),
        if (BuildConfig.DEBUG) "debug" else "release"
    )
    // I tried to get the text view from the dialog but
    // couldn't so I created one.
    val tv = TextView(this)
    with(tv) {
        text = buildInfoStr
        val dp10 = 10F.dpToPx().toInt()
        setPadding(dp10, dp10, dp10, dp10)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
        setTextIsSelectable(true)
    }
    AlertDialog.Builder(this)
        .setTitle("Build Info")
        .setView(tv)
        .show()
    return true
}

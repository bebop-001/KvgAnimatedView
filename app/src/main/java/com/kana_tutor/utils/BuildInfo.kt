package com.kana_tutor.utils

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.util.TypedValue
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.kana_tutor.kvgviewer.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import com.kana_tutor.kvgviewer.R

fun dpToPix(dp: Int): Int =
    (dp * Resources.getSystem().displayMetrics.density).toInt()
@Suppress("unused", "DEPRECATION")
fun spToPix(sp: Int): Int =
    (sp * Resources.getSystem().displayMetrics.scaledDensity).toInt()

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
        val dp10 = dpToPix(10)
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

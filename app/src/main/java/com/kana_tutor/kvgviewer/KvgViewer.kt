package com.kana_tutor.kvgviewer

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import androidx.core.content.ContextCompat
import java.io.File

private const val TAG = "KvgViewer"
class KvgViewer: Application() {
    companion object {
        lateinit var appContext: Context
        lateinit var appFilesDir: File

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
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

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.core.text.toSpanned
import com.kana_tutor.animate.AnimatorActivity
import com.kana_tutor.animate.AnimatorInfo
import com.kana_tutor.kvgviewer.KvgViewer.Companion.externalStorageRoot
import com.kana_tutor.kvgviewer.KvgViewer.Companion.userPreferences
import com.kana_tutor.utils.baseName
import com.kana_tutor.utils.cpErrorMap
import com.kana_tutor.utils.displayBuildInfo
import com.kana_tutor.utils.getMenuItem
import com.kana_tutor.utils.getUnzipFromRemote
import com.kana_tutor.utils.getZipFromRemote
import com.kana_tutor.utils.getZipRequest
import com.kana_tutor.utils.uriCp
import com.kana_tutor.utils.uriCpIoError
import com.kana_tutor.utils.uriToFileName
import com.kana_tutor.utils.zipToRemote
import java.io.File


private const val TAG = "MainActivity"

/*
 * This is where the display theme is set up.  This is probably a google-nono
 * but I'm mixing up the UI resources stuff and the App default stuff.  I'm
 * using the (Activity) resources to determine the current theme and if it's
 * not desired, changing it with the app-delegate set default.
 * From the docs: "An Activity is an application component..."
 */
enum class DisplayTheme(val titleId: Int, val menuId: Int, val displayMode: Int) {
    Dark(R.string.display_theme_dark, R.id.display_dark_theme, AppCompatDelegate.MODE_NIGHT_YES),
    Light(R.string.display_theme_light, R.id.display_light_theme, AppCompatDelegate.MODE_NIGHT_NO),
    Unspecified(R.string.display_theme_unspecified, -1, AppCompatDelegate.MODE_NIGHT_UNSPECIFIED)
}
// Set by the activity at its startup.
fun appDisplayTheme(): String =
    when (AppCompatDelegate.getDefaultNightMode()) {
        AppCompatDelegate.MODE_NIGHT_YES -> "MODE_NIGHT_YES"
        AppCompatDelegate.MODE_NIGHT_NO -> "MODE_NIGHT_NO"
        AppCompatDelegate.MODE_NIGHT_UNSPECIFIED -> "MODE_NIGHT_UNSPECIFIED"
        else -> "Other"
    }
@Suppress("ObjectPropertyName")
private var _currentDisplayTheme = DisplayTheme.valueOf(
    userPreferences.getString("currentDisplayTheme", DisplayTheme.Dark.name)!!
)
val currentDisplayTheme: DisplayTheme
    get() = _currentDisplayTheme
fun selectDisplayTheme(theme: DisplayTheme) {
    if (theme != currentDisplayTheme) {
        AppCompatDelegate.setDefaultNightMode(theme.displayMode)
        _currentDisplayTheme = theme
        userPreferences.edit().putString("currentDisplayTheme", theme.name).apply()
    }
    else {
        Log.d(TAG, "selectDisplayTheme: theme is already $theme " +
                "appCompatMode = ${appDisplayTheme()}")
    }
}
val uiThemeIsDark: Boolean
    get() = currentDisplayTheme == DisplayTheme.Dark



class MainActivity : AppCompatActivity() {
    companion object {
        private var scrollState: Parcelable? = null
    }


    private lateinit var selectorGrid: GridView
    private lateinit var downloadPromptBtn : Button

    class ViewHolder (
        var position: Int,
        var text: String
    )

    inner class GridAdapter: BaseAdapter() {
        private val localList = mutableListOf<String>()
        fun update(newStuff: Set<String>) {
            localList.clear()
            localList.addAll(newStuff.sorted())
            notifyDataSetChanged()
        }
        override fun getCount(): Int = localList.size
        override fun getItem(position: Int): String = localList[position]
        override fun getItemId(position: Int): Long = localList[position].hashCode().toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var button = convertView as Button?
            Log.d(TAG, "getView:position:$position")
            if (button == null) {
                val inflater = LayoutInflater.from(parent!!.context)
                button = inflater.inflate(
                    R.layout.animate_select_button,
                    parent,
                    false) as Button
                button.setOnClickListener(OnClickListener { v ->
                    val b = v as Button
                    // Text is the avg file name for the
                    // kanji we want to animate.
                    startAnimatorActivity(b.text.toString())
                })
            }
            val itemText = getItem(position)
            var vh = button.tag as ViewHolder?
            if (vh == null) {
                vh = ViewHolder(position, itemText)
                button.tag = vh
                button.text = itemText
            }
            else if (vh.position != position) {
                vh.position = position
                vh.text = itemText
                button.text = itemText
            }
            return button
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        selectorGrid = findViewById(R.id.animate_select_grid)
        downloadPromptBtn = findViewById(R.id.downloae_prompt_btn)
        downloadPromptBtn.setOnClickListener {
            AnimatorInfo.initialize() }

        val gridAdapter = GridAdapter()
        AnimatorInfo.initResults.observe { val (success, mess) = it
                Toast.makeText(this,
                "AnimatorInfo init results:" +
                    (if(success) "Success" else "FAIL") +
                    "\n$mess",
                    Toast.LENGTH_LONG
                ).show()
            if (success) {
                gridAdapter.update(AnimatorInfo.recordsById.keys)
                downloadPromptBtn.visibility = View.GONE
            }
            else
                downloadPromptBtn.visibility = View.VISIBLE
        }
        AnimatorInfo.initialize()
        gridAdapter.update(AnimatorInfo.recordsById.keys)
        selectorGrid.adapter = gridAdapter
    }
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        scrollState = selectorGrid.onSaveInstanceState()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        if (scrollState != null)
            selectorGrid.onRestoreInstanceState(scrollState)
    }

    private fun startAnimatorActivity(avgPathName: String) {
        val animateIntent = Intent(applicationContext, AnimatorActivity::class.java)
        animateIntent.putExtra("avgPathName", avgPathName)
        startActivity(animateIntent)
    }

    //=================================

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        fun displayModeTitle(groupId: Int, itemResId: Int): Spanned =
            HtmlCompat.fromHtml(
                getString(groupId, getString(itemResId)),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            ).toSpanned()
        menu.getMenuItem(currentDisplayTheme.menuId)!!.isChecked = true
        menu.getMenuItem(R.id.select_display_theme)!!.title =
            displayModeTitle(R.string.display_theme, currentDisplayTheme.titleId)
        return true
    }
    // the ActivityResultContracts "contract"
    private val getAndUnzipFromRemote = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            getUnzipFromRemote(uri)
        }
    }
    // the ActivityResultContracts "contract"
    private val getZipFromRemote = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            getZipFromRemote(uri)
        }
    }
    private val putZipToRemote = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            try {
                zipToRemote(uri)
            }
            catch (e: Exception) {
                Log.d(TAG, "Exception in zipToRemote: $e")
            }
        }
    }
    private fun updateAnimationsFiles() :Boolean{
        val currentAniFiles = externalStorageRoot.list()!!.filter{
            it.contains("""^kvg.*\.(toc|zip)$""".toRegex()) &&
                    File(externalStorageRoot, it).isFile
        }
        currentAniFiles.map{
            if (!File(externalStorageRoot, it).delete())
                throw RuntimeException("updateAnimationsFiles: " +
                        "Unable to delete($it)")
        }
        val updateId = "updateAnime"
        getZipRequest.value = Pair(updateId, "")
        getZipFromRemote.launch(arrayOf("application/zip"))
        getZipRequest.observe { val (id, file) = it
            if (id == updateId && file.isNotEmpty()) {
                AnimatorInfo.initialize()
            }
        }
        return true
    }
    private val getTxtFromRemote = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uriIn ->
        val externFile = File(uriToFileName(uriIn!!))
        val uriOut = externFile.toUri()
        val bytesRead = uriCp(uriIn, uriOut)
        val mess = if (bytesRead < 0)
            "${cpErrorMap[bytesRead]}${uriCpIoError}"
        else
            "Copied $bytesRead bytes from ${
                uriIn.baseName()
            } to ${uriOut.baseName()}"
        Toast.makeText(this, mess, Toast.LENGTH_LONG).show()
    }
    private val putTxtToRemote = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uriOut ->
        if (uriOut != null) {
            val externFile = uriToFileName(uriOut)
            val uriIn = externFile.toUri()
            val bytesRead = uriCp(uriIn, uriOut)
            val mess = if (bytesRead < 0)
                "${cpErrorMap[bytesRead]}${uriCpIoError}"
            else
                "Copied $bytesRead bytes from ${uriIn.baseName()
                } to ${uriOut.baseName()}"
            Toast.makeText(this, mess, Toast.LENGTH_LONG).show()
        }
    }

    private fun importExport(menuItem: MenuItem):Boolean {
        with (menuItem) {
            Log.d(
                TAG, "menuItem:0x%08x:\"%s\"".format(
                    itemId, title
                ))
            when (itemId) {
                R.id.import_and_unzip ->
                    getAndUnzipFromRemote.launch(arrayOf("application/zip"))
                R.id.export_zip -> {
                    Log.d(TAG, "Export Zip")
                    putZipToRemote.launch("File.zip")
                }
                R.id.import_zip -> {
                    Log.d(TAG, "Import Zip: request = \"${getZipRequest.value.first}\"")
                    getZipFromRemote.launch(arrayOf("application/zip"))
                }
                R.id.import_file ->
                    getTxtFromRemote.launch(arrayOf("text/plain"))
                R.id.export_file ->
                    putTxtToRemote.launch("File.txt")
            }
        }
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.export_file, R.id.import_file,
            R.id.zip_and_export, R.id.import_and_unzip,
            R.id.export_zip, R.id.import_zip ->
                    importExport(item)
            R.id.update_ani_zip -> updateAnimationsFiles()
            R.id.build_info_item -> return displayBuildInfo()
            R.id.display_dark_theme  -> {
                selectDisplayTheme(DisplayTheme.Dark)
                true
            }
            R.id.display_light_theme -> {
                selectDisplayTheme(DisplayTheme.Light)
                true
            }
            else -> {
                Log.d(TAG, "Unexpected menuItem:" +
                        "0x%08x:\"%s\"".format(
                            item.itemId, item.title
                        ))
                super.onOptionsItemSelected(item)
            }
        }
    }

    //=================================
}

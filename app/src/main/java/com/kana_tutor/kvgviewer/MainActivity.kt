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
@file:Suppress("PrivatePropertyName", "UNUSED_ANONYMOUS_PARAMETER")

package com.kana_tutor.kvgviewer

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Parcelable
import android.text.Spanned
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.core.text.isDigitsOnly
import androidx.core.text.toSpanned
import com.kana_tutor.animate.AnimatorActivity
import com.kana_tutor.animate.AnimatorInfo
import com.kana_tutor.animate.AnimatorInfo.Companion.PathRecord
import com.kana_tutor.animate.AnimatorInfo.Companion.indexedKanjiSort
import com.kana_tutor.animate.AnimatorInfo.Companion.pathIdByKanji
import com.kana_tutor.kvgviewer.KvgViewer.Companion.notoSansBold
import com.kana_tutor.kvgviewer.KvgViewer.Companion.notoSansRegular
import com.kana_tutor.kvgviewer.KvgViewer.Companion.userPreferences
import com.kana_tutor.utils.DoubleClick
import com.kana_tutor.utils.codePointSplit
import com.kana_tutor.utils.displayBuildInfo
import com.kana_tutor.utils.getMenuItem
import com.kana_tutor.utils.pxToSp
import com.kana_tutor.utils.selectTypeSize
import com.kana_tutor.utils.webviewAlertDialog

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
private var displayTheme = DisplayTheme.valueOf(
    userPreferences.getString("currentDisplayTheme", DisplayTheme.Dark.name)!!
)
val currentDisplayTheme: DisplayTheme
    get() = displayTheme
fun selectDisplayTheme(theme: DisplayTheme) {
    if (theme != currentDisplayTheme) {
        AppCompatDelegate.setDefaultNightMode(theme.displayMode)
        displayTheme = theme
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
    private lateinit var kanjiSelectEt: EditText

    fun copyToClipboard(kanji: String) {
        val cb = this.getSystemService(Context.CLIPBOARD_SERVICE)
                as ClipboardManager
        val clip = ClipData.newPlainText(
            ClipDescription.MIMETYPE_TEXT_PLAIN, kanji
        )
        cb.setPrimaryClip(clip)
        Toast.makeText(this, "Copied $kanji to clipboard", Toast.LENGTH_LONG).show()
    }

    @Suppress("RedundantSamConstructor")
    inner class GridAdapter: BaseAdapter() {
        private val localList = mutableListOf<String>()
        // Take what ever comes in, join to a string,update
        // extract the kanji and if kanji only has one
        // style, ad the kanji otherwise add all style keys
        // for the kanji
        fun update(newStuff: String) {
            var kanji = newStuff.codePointSplit()
                .toSet()
            if (kanji.isEmpty())
                kanji = pathIdByKanji.keys
            localList.clear()
            localList.addAll(indexedKanjiSort(kanji))
            notifyDataSetChanged()
        }
        fun update(newStuff: Set<String>) =
            update(newStuff.joinToString(""))
        // unit = dp
        var buttonTextSizeSP: Int = userPreferences.getInt("buttonTextSize", 25)
            set(newVal) {
                userPreferences.edit().putInt("buttonTextSize", newVal).apply()
                field = newVal
                notifyDataSetChanged()
            }
        private fun Context.avgSelect(selectableAvg: List<String>, animator: (String)->Unit) {
            if (selectableAvg.size > 1) {
                val alertDialog = AlertDialog.Builder(this)
                    .setTitle(getString(R.string.which_avg))
                    .setSingleChoiceItems(
                        selectableAvg.toTypedArray(),
                        0
                    ) /* no initial selection */
                    { dialog, which ->
                        dialog.dismiss()
                        animator.invoke(selectableAvg[which])
                    }
                alertDialog.show()
            }
            else animator.invoke(selectableAvg[0])
        }
        private fun Context.avgSelect(selectableAvg: Set<String>, animator: (String)->Unit) =
            avgSelect(selectableAvg.sortedDescending(), animator)

        private val INDEX = 0
        private val NORMAL = INDEX + 1
        private val MULTI_STYLE = NORMAL + 1
        private fun String.toType(): Int = when {
                isDigitsOnly() -> INDEX
                pathIdByKanji[this]!!.size > 1 -> MULTI_STYLE
                else -> NORMAL
            }
        private fun String.toButtonColor() : Int =
            when(this.toType()) {
                INDEX -> Color.RED
                NORMAL -> if(currentDisplayTheme == DisplayTheme.Dark)
                    Color.WHITE else Color.BLACK
                else -> Color.GREEN
            }
        private fun CharSequence.toButtonColor() =
            this.toString().toButtonColor()
        @Suppress("NAME_SHADOWING")
        private fun ViewGroup.newButton(
            text: String, position: Int
        ): Button {
            val button = Button(this.context)
            val clickHandler = DoubleClick(
                clientData = position,
                singleClickListener = { v, cd ->
                    val text = (v as Button).text
                    if (pathIdByKanji.containsKey(text)) {
                        val pathIds: Map<String, PathRecord> =
                            pathIdByKanji[text]!!
                        avgSelect(pathIds.keys) { pathId ->
                            startAnimatorActivity(pathId)
                        }
                    }
                },
                doubleClickListener = {v, cd ->
                    copyToClipboard((v as Button).text.toString())
                }
            )
            button.tag = clickHandler
            with (button) {
                button.tag = clickHandler
                this.text = text
                setTextColor(text.toButtonColor())
                setTypeface(notoSansBold, Typeface.BOLD)
                textSize = buttonTextSizeSP.toFloat()
                setBackgroundResource(
                    R.drawable.border_bg
                )
                setPadding(1,1,1,1)
                setTypeface(notoSansBold, Typeface.NORMAL)
                /*
                layoutParams = LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                 */
                setOnClickListener(OnClickListener { v ->
                    val text = (v as Button).text
                    Log.d(TAG, "onClick:$text")
                    if (pathIdByKanji.containsKey(text)) {
                        val pathIds: Map<String, PathRecord> =
                            pathIdByKanji[text]!!
                        avgSelect(pathIds.keys) { pathId ->
                            startAnimatorActivity(pathId)
                        }
                    }
                })
            }
            button.setOnClickListener { v -> clickHandler.onClick(v) }
            return button
        }
        override fun getCount(): Int = localList.size
        override fun getItem(position: Int): String =
            localList[position]
        override fun getItemId(position: Int): Long =
            localList[position].hashCode().toLong()
        override fun getView(
            position: Int, convertView: View?, parent: ViewGroup?
        ): View {
            var button = convertView as Button?
            // if button resized, make a new one.
            if (button != null && button.textSize.pxToSp().toInt() != buttonTextSizeSP)
                button = null
            if (button == null) {
                button = parent!!.newButton(
                    getItem(position), position
                )
            }
            else if (button.text != getItem(position)
                    || button.tag != position
                ) {
                // RE: textSize, it makes absolutely no sense but when
                //     you set button size it should be SP but get textSize
                //     returns pixels.
                button.text = getItem(position)
                button.textSize = buttonTextSizeSP.toFloat()
                button.setTextColor(
                    button.text.toButtonColor())
                button.tag = position
            }
            return button
        }
    }
    private var sentString = ""
    private val gridAdapter = GridAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Calculate grid numColumns once layout is complete.
        findViewById<ConstraintLayout>(R.id.root_view)
            .viewTreeObserver.addOnGlobalLayoutListener {
                // button width is set by the grid view.  button
                // height is determined by button padding, margins,
                // etc.  Ideally we want a button aspect ratio of 1.2x1.
                // based on that, calculate the width (assuming it is good first
                // try through) and calculate a number of columns.  If the
                // current number of columns != the calculated value, select
                // the new value.
                with (selectorGrid) {
                    val numRows = if(childCount == 0) 0 else  (childCount/ numColumns) + 1
                    if (numRows > 1) {
                        val gridButton = getChildAt(0)
                        val maxWidth = gridButton.measuredWidth * numColumns
                        val desiredButtonWidth = (1.2 * gridButton.measuredHeight + 0.5).toInt()
                        val desiredNumColumns = maxWidth / desiredButtonWidth
                        if (desiredNumColumns != numColumns)
                            numColumns = desiredNumColumns
                        Log.d(
                            TAG, "viewTreeObserver: \n\t" +
                            "selectorGrid: ${numColumns}:" +
                            "$measuredWidth x $measuredHeight\n\t" +
                            "button: ${gridButton.measuredWidth} x" +
                            " ${gridButton.measuredHeight}"
                        )
                    }
                }
            }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        AppCompatDelegate.setDefaultNightMode(displayTheme.displayMode)

        selectorGrid = findViewById(R.id.animate_select_grid)

        kanjiSelectEt = findViewById(R.id.kanji_select_et)
        kanjiSelectEt.setOnClickListener{ val et = it as EditText
            gridAdapter.update(et.text.toString())
        }
        kanjiSelectEt.setTypeface(notoSansRegular, Typeface.NORMAL)

        AnimatorInfo.initialize()
        // Check for code sent by another app using
        // intent.SEND and putting the kanji into
        // Intent.EXTRA_TEXT as data mime type
        // text/plain
        if (intent != null && intent.action == Intent.ACTION_SEND
            && intent.type == "text/plain" &&
            intent.extras != null) {

            sentString = intent.extras!!
                .getString(Intent.EXTRA_TEXT).toString()
        }
        selectorGrid.adapter = gridAdapter
    }
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        scrollState = selectorGrid.onSaveInstanceState()
    }
    private fun startAnimatorActivity(avgPathName: String) {
        val animateIntent = Intent(applicationContext, AnimatorActivity::class.java)
        animateIntent.putExtra("avgPathName", avgPathName)
        startActivity(animateIntent)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        if (scrollState != null)
            selectorGrid.onRestoreInstanceState(scrollState)

        // sentString will not be empty iff the app was
        // started as the result of a "share" from
        // another app.
        if (sentString.isNotEmpty()) {
            val kanji = sentString.codePointSplit()
                .filter{pathIdByKanji.contains(it)}
                .toSet().sorted()
            // For a "shared" kanji string, tart the
            // animator with the first path record of
            // the first kanji we find.
            if (kanji.isNotEmpty()) {
                kanjiSelectEt.setText(sentString)
                val pathId = pathIdByKanji[kanji[0]]!!
                    .keys.minOf { it }
                startAnimatorActivity(pathId)
            }
            sentString = ""
        }
        else gridAdapter.update(pathIdByKanji.keys)

        val currentText = kanjiSelectEt.text.toString()
        if (currentText.isNotEmpty()) gridAdapter.update(currentText)
        else gridAdapter.update(pathIdByKanji.keys)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    private var appMenuTitle = ""
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
        appMenuTitle = getString(R.string.about_app, getString(R.string.app_name))
        menu.getMenuItem(R.id.about_app)!!.title = appMenuTitle
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.build_info_item -> return displayBuildInfo()
            R.id.about_app -> webviewAlertDialog(
                "file:///android_asset" +
                        "/www/about_app.html",
                    appMenuTitle)
            R.id.display_dark_theme  -> {
                selectDisplayTheme(DisplayTheme.Dark)
                true
            }
            R.id.display_light_theme -> {
                selectDisplayTheme(DisplayTheme.Light)
                true
            }
            R.id.select_font_size -> {
                selectTypeSize(gridAdapter.buttonTextSizeSP,
                    15,35,
                ) { newSp ->
                    Log.d(TAG, "New font size: $newSp")
                    gridAdapter.buttonTextSizeSP = newSp
                }
            /*
                fontChangeListener = {fontSize: Int ->
                    gridAdapter.buttonTextSize
             */
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
}

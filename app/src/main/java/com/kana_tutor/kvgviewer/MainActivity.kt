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
@file:Suppress("PrivatePropertyName")

package com.kana_tutor.kvgviewer

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Parcelable
import android.text.Spanned
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
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
import com.kana_tutor.utils.codePointSplit
import com.kana_tutor.utils.displayBuildInfo
import com.kana_tutor.utils.getMenuItem
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
        fun Context.avgSelect(selectableAvg: List<String>, animator: (String)->Unit) {
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
        fun Context.avgSelect(selectableAvg: Set<String>, animator: (String)->Unit) =
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
            with (button) {
                this.text = text
                setTextColor(text.toButtonColor())
                setTypeface(notoSansBold, Typeface.BOLD)
                setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(
                        R.dimen.grid_btn_text_size)
                )
                setBackgroundResource(
                    R.drawable.border_bg
                )
                setPadding(1,1,1,1)
                setTypeface(notoSansBold, Typeface.BOLD)
                setOnClickListener(OnClickListener { v ->
                    val text = (v as Button).text
                    if (pathIdByKanji.containsKey(text)) {
                        val pathIds: Map<String, PathRecord> =
                            pathIdByKanji[text]!!
                        avgSelect(pathIds.keys) { pathId ->
                            startAnimatorActivity(pathId)
                        }
                    }
                })
                tag = position
            }
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
            if (button == null) {
                button = parent!!.newButton(
                    getItem(position), position
                )
            }
            else if (button.text != getItem(position) || button.tag != position) {
                button.text = getItem(position)
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
        // for grid column recalculate...
        findViewById<ConstraintLayout>(R.id.root_view)
            .viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        val buttpn = selectorGrid.getChildAt(0)
                        /*
                            On LG 322DL:
                            viewTreeObserver:
                                selectorGrid: 6:720 x 1138
                                button: 84 x 112
                            On Samsung J7
                            viewTreeObserver:
                                selectorGrid: 6:720 x 544
                                button: 96 x 111
                            On Samsung T-820
                            viewTreeObserver:
                                selectorGrid: 6:1536 x 1783
                                button: 96 x 247
                         */
                        Log.d(TAG, "viewTreeObserver: \n\t" +
                            "selectorGrid: ${selectorGrid.numColumns}:" +
                                "${selectorGrid.measuredWidth} x" +
                                " ${selectorGrid.measuredHeight}\n\t" +
                            "button: ${buttpn.measuredHeight} x" +
                                        " ${buttpn.measuredWidth}"
                        )
                    }
                }
            )

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

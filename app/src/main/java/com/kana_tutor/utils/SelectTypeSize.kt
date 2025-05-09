package com.kana_tutor.utils

/*
 * Copyright 2019 Steven Smith kana-tutor.com
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
import android.app.AlertDialog
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import com.kana_tutor.kvgviewer.R

@Suppress("unused")
private const val TAG = "SelectFontSize"
// dialog used to select font size for textview.
fun Activity.selectTypeSize(initialSP: Int, rangeSP: IntRange): Int {
    var rv = initialSP
    val selectView = View.inflate(
        this, R.layout.font_size_select, null
    )
    val selectBtn =     selectView.findViewById<Button>(R.id.select_btn)
    selectBtn.setTextColor(Color.GREEN)
    val cancelBtn =  selectView.findViewById<Button>(R.id.cancel_btn)
    cancelBtn.setTextColor(Color.RED)
    val sampleText = selectView.findViewById<TextView>(R.id.size_sample_view)
    selectView.findViewById<SeekBar>(R.id.font_size_slider)
        .setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val x0 = initialSP - rangeSP.first // for progress == 0
                val x1 = initialSP + rangeSP.last // foe progress == 100
                val newSP = (((x0 - x1) / (progress /100f)) + 0.5f) // new size in sp
                sampleText.textSize = newSP
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        }
    )

    val builder = AlertDialog.Builder(this)
    val dialog = builder.setTitle(R.string.select_font_size)
        .setView(selectView)
        .create()
    dialog.show()
    cancelBtn.setOnClickListener {
            rv = initialSP
            dialog.cancel()
        }
    selectBtn.setOnClickListener {
            rv = sampleText.textSize.pxToSp().toInt()
            dialog.cancel()
        }
    return rv
}
/*
// sp
    fontChangeListener : (Float)-> Unit) {

    val dm = resources.displayMetrics

    val sampleView = selectView.findViewById<TextView>(R.ilayoutInflaterd.size_sample_view)
    // You set the text size using SP units, but you get the size back in pixel units.
    sampleView.textSize = initialDpSize
    sampleView.setBackgroundColor(ContextCompat.getColor(this, R.color.file_edit_window_bg))
    sampleView.setTextColor(ContextCompat.getColor(this, R.color.file_edit_window_font_color))

    val slider = selectView.findViewById<SeekBar>(R.id.font_size_slider)
    slider.setBackgroundColor(ContextCompat.getColor(this, R.color.file_edit_window_bg))

    AlertDialog.Builder(
        this,
        R.style.rounded_corner_dialog
    )
        .setTitle(R.string.select_font_size)
        // positive is the left button which I associate with cancel
        .setPositiveButton(R.string.cancel, null)
        .setNegativeButton(R.string.select) { dialog, _ ->
            with(sampleView) {
                Log.d(TAG, "new font size = $textSize pixels = ${(textSize/dm.density).toInt()} dp")
                fontChangeListener.invoke(sampleView.textSize.pxToSp())
            }
            dialog.dismiss()
        }
        .setView(selectView)
        .show()

    slider.progress = 50 // seeker initial value
    slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        // We want range of initialDpSize -4 to initialDpSize +4 with
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            val currentSP = sampleView.textSize.pxToSp()
            sampleView.textSize = ((progress/100f) * 8.0f) * (initialDpSize - 4.0f)
            /*
            Log.d("SeekBar:changed", String.format("%f:%f:%f:%d",
                was, sampleView.textSize, (progress / 50.0f), progress))
             */

        }
        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            //To change body of created functions use File | Settings | File Templates.
        }
        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            // Log.d("SeekBar:stop", seekBar!!.progress.toString())
        }
    })
    return initialDpSize
}

 */

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
// Allow user to select a text size given min and max ranges.
// seek bar shows current selection.
fun Activity.selectTypeSize(
    initialSP: Int, minSP: Int, maxSP: Int, updateSP: (Int) -> Unit
) {
    val selectView = View.inflate(
        this, R.layout.font_size_select, null
    )
    val selectBtn = selectView.findViewById<Button>(R.id.select_btn)
    selectBtn.setTextColor(Color.GREEN)
    val cancelBtn = selectView.findViewById<Button>(R.id.cancel_btn)
    cancelBtn.setTextColor(Color.RED)
    val sampleText = selectView.findViewById<TextView>(R.id.size_sample_view)
    val seekBar = selectView.findViewById<SeekBar>(R.id.font_size_slider)
    with (seekBar) {
        // if current selection is outside min/max bound, reset it.
        val iSP = when {
            initialSP < minSP -> minSP
            initialSP > maxSP -> maxSP
            else -> initialSP
        }.toFloat()
        sampleText.textSize = iSP
        val grad = (maxSP - minSP) / 100f
        progress = ((iSP - minSP)/(maxSP - minSP) * 100).toInt()
        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                sampleText.textSize = minSP + (grad * progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })
    }

    val builder = AlertDialog.Builder(this)
    val dialog = builder.setTitle(R.string.select_font_size)
        .setView(selectView)
        .create()
    dialog.show()
    cancelBtn.setOnClickListener {
        dialog.cancel()
    }
    selectBtn.setOnClickListener {
        val newSize = sampleText.textSize.pxToSp().toInt()
        if (newSize != initialSP)
            updateSP.invoke(newSize)
        dialog.cancel()
    }
}
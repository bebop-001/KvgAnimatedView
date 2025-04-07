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

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var sharedPreferences: SharedPreferences
            private set
    }
    private val pathFiles = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE)
        pathFiles.addAll(
            assets.list("paths")!!
                .map{"paths/$it"}
        )
        val animateButton = findViewById<Button>(R.id.animate_char_button)
        animateButton.setOnClickListener { animationOnClick() }
        // for debugging...
        startAnimator("一")
    }

    private fun startAnimator(renderChar: String) {
        val renderFile = pathFiles.firstOrNull() {
            it.contains(renderChar)
        }
        if (renderFile != null) {
            val startAnimator = Intent(applicationContext, Animator::class.java)
            startAnimator.putExtra("renderChar", renderChar)
            startAnimator.putExtra("renderFile", renderFile)
            startActivity(startAnimator)
        }
        else {
            Toast.makeText(
                this,
                if (renderChar.isEmpty()) "skipping empty input"
                else "Can't render \"$renderChar\". Char not found.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private fun animationOnClick() {
        val tv = findViewById<TextView>(R.id.renderChar_TXT)
        startAnimator(tv.text.toString().trim())
    }
}

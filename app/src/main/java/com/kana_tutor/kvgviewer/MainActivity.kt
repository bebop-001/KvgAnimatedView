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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kana_tutor.animate.AnimateInfo
import com.kana_tutor.animate.Animator

class MainActivity : AppCompatActivity() {
    private val pathFiles = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AnimateInfo.initResults.observe { val (success, mess) = it
                Toast.makeText(this,
                "AnimateInfo init results:" +
                    (if(success) "Success" else "FAIL") +
                    "\n$mess",
                    Toast.LENGTH_LONG
                ).show()
        }
        val animateButton = findViewById<Button>(R.id.animate_char_button)
        animateButton.setOnClickListener { animationOnClick() }
    }

    private fun startAnimator(renderChar: String) {
        val renderFile = pathFiles.firstOrNull {
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

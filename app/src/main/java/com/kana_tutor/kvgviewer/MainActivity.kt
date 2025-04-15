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
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kana_tutor.animate.AnimatorInfo
import com.kana_tutor.animate.Animator
import com.kana_tutor.animate.AnimatorInfo.Companion.supportedKanji
import com.kana_tutor.animate.AnimatorInfo.Companion.supportedtyles

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    private val pathFiles = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AnimatorInfo.initResults.observe { val (success, mess) = it
                Toast.makeText(this,
                "AnimatorInfo init results:" +
                    (if(success) "Success" else "FAIL") +
                    "\n$mess",
                    Toast.LENGTH_LONG
                ).show()
        }
        val animateButton = findViewById<Button>(R.id.animate_char_button)
        animateButton.setOnClickListener { animationOnClick() }
    }

    private fun startAnimator(renderChar: Char, renderStyle: String) {
        if (supportedKanji.contains(renderChar) &&
            supportedtyles.contains(renderStyle)) {
            val startAnimator = Intent(applicationContext, Animator::class.java)
            startAnimator.putExtra("renderChar", renderChar)
            startAnimator.putExtra("renderStyle", renderStyle)
            startActivity(startAnimator)
        }
        else {
            val mess = listOf (if (supportedKanji.contains(renderChar)) ""
            else "$renderChar: Not supported character",
                if (supportedtyles.contains(renderStyle)) ""
                else "$renderStyle: Not supported style"
            ).joinToString(", ")
            Toast.makeText(this, mess, Toast.LENGTH_LONG).show()
            Log.d(TAG, "StartAnimate FAILED: $mess")
        }
    }
    private fun animationOnClick() {
        val tv = findViewById<TextView>(R.id.renderChar_TXT)
        val renderChar = tv.text.toString().trim()[0]
        val renderStyle = ""
        startAnimator(renderChar, renderStyle)
    }
}

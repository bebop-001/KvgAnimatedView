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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kana_tutor.animate.Animator
import com.kana_tutor.animate.AnimatorInfo
import com.kana_tutor.animate.AnimatorInfo.Companion.supportedKanji
import com.kana_tutor.animate.AnimatorInfo.Companion.supportedStyles


private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    companion object {
        private var scrollState: Parcelable? = null
    }
    private lateinit var selectorGrid: GridView

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
                    null,
                    false) as Button
                button.setOnClickListener(OnClickListener { v ->
                    val b = v as Button
                    startAnimator(b.text.toString())
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
        AnimatorInfo.initResults.observe { val (success, mess) = it
                Toast.makeText(this,
                "AnimatorInfo init results:" +
                    (if(success) "Success" else "FAIL") +
                    "\n$mess",
                    Toast.LENGTH_LONG
                ).show()
        }
        val gridAdapter = GridAdapter()
        selectorGrid.adapter = gridAdapter
        gridAdapter.update(AnimatorInfo.animatorFiles)

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

    private fun startAnimator(renderChar: Char, renderStyle: String) {
        if (supportedKanji.contains(renderChar) &&
            supportedStyles.contains(renderStyle)) {
            val startAnimator = Intent(applicationContext, Animator::class.java)
            startAnimator.putExtra("renderChar", renderChar)
            startAnimator.putExtra("renderStyle", renderStyle)
            startActivity(startAnimator)
        }
        else {
            val mess = listOf (if (supportedKanji.contains(renderChar)) ""
            else "$renderChar: Not supported character",
                if (supportedStyles.contains(renderStyle)) ""
                else "$renderStyle: Not supported style"
            ).joinToString(", ")
            Toast.makeText(this, mess, Toast.LENGTH_LONG).show()
            Log.d(TAG, "StartAnimate FAILED: $mess")
        }
    }
    private fun startAnimator(animateName:String) {
        val (char, style) = """^(.)-*([^.]+)*.avg$""".toRegex(RegexOption.IGNORE_CASE)
            .find(animateName)!!.groupValues.takeLast(2)
        startAnimator(char[0], style)
    }
}

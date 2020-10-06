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

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT

// interface to the AnimatorView.
class KanaAnimator : Activity() {
    companion object {
        private var showSpeedToast = true
        private var renderChar = ""
    }

    private var animateSpeed = 0
    private lateinit var prefs: SharedPreferences
    private lateinit var animatorView : AnimatorView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.animator_view)
        prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        animateSpeed = prefs.getInt("animateSpeed", 1)
        if (prefs.getString("renderChar", null) != null)
            renderChar = prefs.getString("renderChar", null)!!

        // cause display properties to init.
        // DisplayProperties(this, R.id.animate_layout)
        animatorView = findViewById(R.id.animator_view)
        animatorView.setAnimateSpeed(animateSpeed)
        animatorView.setOnClickListener {
            // render char is only first char in string.
            val kp  = KvgToAndroidPaths(this, renderChar.toCharArray()[0])

            animatorView.setRenderCharacter(kp)
            animatorView.invalidate()
            // animateKana.show(this, animateView)
        }
        // If user touches screen outside of the animate view, exit.
        findViewById<View>(R.id.animate_layout).setOnClickListener { v: View ->
            Toast.makeText(v.context, "exit animator", LENGTH_SHORT).show()
            finish()
        }

        // If we received an intent from the main app. set up for animation.
        if (intent != null) {
            if (intent.getStringExtra("renderChar") != null) {
                renderChar = intent.getStringExtra("renderChar")!!
                prefs.edit()
                    .putString("renderChar", renderChar)
                    .apply()
                intent = null
                // reduce string in to first character only.
                val kp  = KvgToAndroidPaths(this, renderChar.toCharArray()[0])
                animatorView.setRenderCharacter(kp)
            }
        }
        // register for the speed-set context menu.
        registerForContextMenu(findViewById(R.id.animator_view))
        registerForContextMenu(findViewById(R.id.animate_layout))
    }

    override fun onStart() {
        super.onStart()
        if (showSpeedToast) {
            // show a reminder of how to get to the animation
            // speed control first time in the session animation
            // is run.
            showSpeedToast = false
            val t = Toast.makeText(
                    applicationContext,
                    R.string.show_speed_hint,
                    LENGTH_SHORT)
                    t.setGravity(
                        Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
                    0,0)
                    t.show()
        }
    }

    // set the animate speed to user prefs and in the animator view.
    private fun setAnimateSpeed(speed: Int) {
        val e = prefs.edit()
        e.putInt("animateSpeed", speed)
        e.apply()
        animateSpeed = speed
        animatorView.setAnimateSpeed(speed)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View,
                                     menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        // I don't understand why but -- I'm receiving two callbacks on
        // the animator view: one from the view and the other from its
        // layout.  Since I want callbacks from view and layout and both
        // are registered, throwing away the one from the animator view
        // fixes my problem.
        if (v.id == R.id.animator_view) return
        // Log.d("KanaAnimator", String.format("onCreateContextMenu: id = 0x%08x", v.id))
        val inflater = menuInflater
        inflater.inflate(R.menu.kana_animator_menu, menu)
        menu.getItem(animateSpeed).isChecked = true
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        var rv = true
        when (itemId) {
            R.id.animate_slow -> setAnimateSpeed(0)
            R.id.animate_normal -> setAnimateSpeed(1)
            R.id.animate_fast -> setAnimateSpeed(2)
            else -> {
                // Log.d("KanaAnimator", String.format(
                //        "menu item unhandled:0x%08x", itemId))
                rv = false
            }
        }
        return rv
    }
}

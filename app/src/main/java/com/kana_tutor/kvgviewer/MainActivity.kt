package com.kana_tutor.kvgviewer

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var sharedPreferences: SharedPreferences
            private set
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE)
    }

    fun animationOnClick(view: View) {
        val button = view as Button
        val startAnimator = Intent(applicationContext, KanaAnimator::class.java)
        startAnimator.putExtra("renderChar", button.text)
        startActivity(startAnimator)
    }
}
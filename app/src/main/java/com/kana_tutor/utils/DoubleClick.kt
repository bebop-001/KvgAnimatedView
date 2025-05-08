package com.kana_tutor.utils

import android.os.Handler
import android.os.Looper
import android.view.View

@Suppress("unused")
private const val TAG = "DoubleClick"
private var id = 0
class DoubleClick (
    private var singleClickListener: ((View, Any?) ->Unit)? = null,
    private var doubleClickListener: ((View, Any?) ->Unit)? = null,
    private var clientData: Any? = null
){
    init{ id++ }
    private var isActive = false
    private lateinit var scHandler : Handler
    private var clickCounter = 0
    private val isSingleClick: Boolean
        get() = clickCounter == 1

    @Suppress("PrivatePropertyName")
    private val DOUBLE_CLICK_TIME = 350L // milliseconds
    fun onClick (view: View) {
        clickCounter++
        val runnable: () -> Unit = fun() {
            if (isSingleClick)
                singleClickListener?.invoke(view, clientData)
            else
                doubleClickListener?.invoke(view, clientData)
        }
        if (isActive) {
            scHandler.removeCallbacks(runnable)
            runnable.invoke()
            isActive = false
            clickCounter = 0
        }
        else {
            isActive = true
            scHandler = Handler(Looper.getMainLooper())
            scHandler.postDelayed(runnable, DOUBLE_CLICK_TIME)
        }
    }
}
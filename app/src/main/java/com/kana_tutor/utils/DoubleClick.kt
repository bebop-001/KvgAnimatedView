package com.kana_tutor.utils

import android.os.Handler
import android.view.View

class DoubleClick (
    val onSingleClick: View.OnClickListener? = null,
    val onDoubleClick: ((View) ->Unit)? = null,
    val onLongClick: ((View) ->Unit)? = null,
){
    val validator : Set<String>? = (v.tag as ViewHolder).validator
    if (v is Button) {
        // this is for the radical selected kanji radicals view.
        // When view-holder is created a validator is supplied that
        // filters selectable radicals. Without this, you can end up
        // with an empty rad-select view.
        // Use view holder kanji for filter rather than button kanji
        // because some radicals use image to represent the radical kanji.
        if (validator != null && !validator.contains(this.kanji)) return
        else if (v.text.contains("^\\d+$".toRegex())) return
    }
    val runnable : () -> Unit  = fun () {
        if (isActive) {
            isActive = false
            if (isSingleClick) {
                doubleClickListener.onSingleClick(v, this)
            }
            else {
                doubleClickListener.onDoubleClick(v, this)
            }
            isSingleClick = true
        }
    }
    if (isActive) {
        h.removeCallbacks(runnable)
        isSingleClick = false
        runnable.invoke()
    }
    else {
        isActive = true
        // Start a timer to catch the single-click case.
        h = Handler(Looper.getMainLooper())
        h.postDelayed(runnable, DOUBLE_CLICK_TIME)
    }
}
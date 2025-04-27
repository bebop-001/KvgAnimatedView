package com.kana_tutor.kvgviewer

import android.content.Context
import androidx.appcompat.app.AlertDialog


fun Context.avgSelect(selectableAvg: List<String>, animator: (String)->Unit) {
    if (selectableAvg.size > 1) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.which_avg))
            .setSingleChoiceItems(
                selectableAvg.toTypedArray(),
                0
            ) /* no initial selection */
            { dialog, which ->
                dialog.dismiss()
                animator.invoke(selectableAvg[which])
            }
        alertDialog.show()
    }
    else animator.invoke(selectableAvg[0])
}
fun Context.avgSelect(selectableAvg: Set<String>, animator: (String)->Unit) =
    avgSelect(selectableAvg.sortedDescending(), animator)


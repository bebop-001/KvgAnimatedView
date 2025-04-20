@file:Suppress("unused")

package com.kana_tutor.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.text.InputType
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import com.kana_tutor.kvgviewer.KvgViewer.Companion.appContext
import com.kana_tutor.kvgviewer.R


@Suppress("unused")
private const val TAG = "DialogUtils"

fun dpToPix(dp: Int): Int =
    (dp * Resources.getSystem().displayMetrics.density).toInt()
@Suppress("unused", "DEPRECATION")
fun spToPix(sp: Int): Int =
    (sp * Resources.getSystem().displayMetrics.scaledDensity).toInt()



// button enable/disable.  Dims button bg
// when disabled.
@Suppress("unused")
fun ImageButton.enable(enabled: Boolean) {
    if (enabled) {
        alpha = 1.0f
        isEnabled = true
    }
    else {
        alpha = 0.3f
        isEnabled = false
    }
}
fun Button.enable(on:Boolean): Button {
    if (on) {
        alpha = 1.0f
        isEnabled = true
    }
    else {
        alpha = 0.5f
        isEnabled = false
    }
    return this
}
fun ImageButton.showButton(show: Boolean) {
    visibility = if(show) VISIBLE else GONE }
fun Context.hideKeyboard(tv: TextView, hide:Boolean = true): TextView {
    try {
        val im = getSystemService(Activity.INPUT_METHOD_SERVICE)
            as InputMethodManager
        if (hide) {
            im.hideSoftInputFromWindow(tv.windowToken, 0)
            tv.clearFocus()
        }
        else {
            tv.requestFocus()
            im.showSoftInput(tv, InputMethodManager.SHOW_IMPLICIT)
        }
    }
    catch (e:Exception) {
        // this sometimes happens during color change & edit.  Ignore...
        Log.d(TAG, "hideKeyboard exception: ${e.message}")
    }
    return tv
}

fun Context.copyToClipboard(string: String) {
    val cb = getSystemService(Context.CLIPBOARD_SERVICE)
            as ClipboardManager
    val clip = ClipData.newPlainText(
        ClipDescription.MIMETYPE_TEXT_PLAIN, string
    )
    cb.setPrimaryClip(clip)
    Toast.makeText(
        this,
        getString(R.string.to_clipboard, string),
        Toast.LENGTH_SHORT
    ).show()

}
/*
    could also use view.setText but toast.getView is deprecated.
    val toast = Toast.makeText(textView.context, "match:$t", Toast.LENGTH_LONG)
    val toastLayout = toast.view as ViewGroup?
    val toastTV = toastLayout!!.getChildAt(0) as TextView
    toastTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.toFloat())
    toast.show()
 */
fun Context.toastPlus(
    mess: String, // in sp
    fontSize: Int = 20,
    displayTime: Int = Toast.LENGTH_LONG,
    color: Int? = null // color resource id
) {
    val ssMess = SpannableString(mess)
    ssMess.setSpan(
        AbsoluteSizeSpan(spToPix(fontSize)),
        0, ssMess.length, 0
    )
    if (color != null)
        ssMess.setSpan( ForegroundColorSpan(color),
            0, ssMess.length, 0)
    Toast.makeText(this, ssMess, displayTime).show()
}
fun Context.toastPlus(
    messId: Int, // in sp
    fontSize: Int = 20,
    displayTime: Int = Toast.LENGTH_LONG,
    color: Int? = null // color resource id
) = toastPlus(getString(messId), fontSize, displayTime, color)
@Suppress("UNUSED_ANONYMOUS_PARAMETER")
fun Context.selectableMonospaceDialog(
    title: String, mess: String, fontSize:Int = 12,
    makeTextSelectable: Boolean = false,
    showDone: Boolean = false,
    isHtml:Boolean = false
) : Dialog {
    val tv = AppCompatTextView(this)
    tv.movementMethod = ScrollingMovementMethod()
    tv.typeface = Typeface.MONOSPACE
    // it seems that to be scrollable, setTextIsSelectable
    // must be set.  If isFocusableInTouchMode is false, you
    // can still scroll but you can't select.
    tv.isFocusable = true
    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
    tv.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
    tv.isSingleLine = false
    tv.background = AppCompatResources.getDrawable(
        this, R.drawable.border_bg_gray_1)
    // tv has a bug where padding gets stepped on if set
    // before background.
    tv.setPadding(20, 20, 20, 20)
    if (makeTextSelectable) {
        tv.setTextIsSelectable(true)
    }
    else {
        tv.setTextIsSelectable(false)
        tv.isLongClickable = false
        // intercept copy-paste menu.
        // https://stackoverflow.com/questions/6275299/how-to-disable-copy-paste-from-to-edittext/12331404#12331404
        tv.customSelectionActionModeCallback = object : android.view.ActionMode.Callback {
            override fun onCreateActionMode(mode: android.view.ActionMode?, menu: Menu?): Boolean {
                Log.d(TAG, "ActionModeCallback.onCreateActionMode: mode = $mode")
                return true
            }

            override fun onPrepareActionMode(mode: android.view.ActionMode?, menu: Menu?): Boolean {
                Log.d(TAG, "ActionModeCallback.onPrepareActionMode: mode = $mode")
                return true
            }

            override fun onActionItemClicked(mode: android.view.ActionMode?,
                item: MenuItem?): Boolean {
                Log.d(TAG, "ActionModeCallback.onActionItemClicked: mode = $mode")
                return true
            }

            override fun onDestroyActionMode(mode: android.view.ActionMode?) {
                Log.d(TAG, "ActionModeCallback.onDestroyActionMode: mode = $mode")
            }
        }
    }

    tv.text = if (isHtml)
        HtmlCompat.fromHtml(mess, HtmlCompat.FROM_HTML_MODE_LEGACY)
        else mess
    val dialogBuilder = AlertDialog.Builder(this)
        .setTitle(title)
        .setView(tv)
    if (showDone)
        dialogBuilder.setPositiveButton(getString(R.string.done)){ dialog, which ->
            dialog.dismiss()
        }
    val dialog = dialogBuilder.create()

    dialog.setCanceledOnTouchOutside(true)
    dialog.setOnCancelListener { it.cancel() }
    dialog.show()
    // must be after dialog show or button instance doesn't exist.
    val doneButton: Button = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
    doneButton.setTextColor(Color.GREEN)
    doneButton.isAllCaps = false
    doneButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
    return dialog
}
fun Context.monospaceDialog(
    title: String, mess: String, fontSize:Int = 12,
    showDone:Boolean = false
) : Dialog = this.selectableMonospaceDialog(
    title, mess, fontSize, false, showDone = showDone
)
fun Context.errorWarningDialog(
    title: String, mess: String, fontSize:Int = 12,
    showDone:Boolean = false
) : Dialog = this.selectableMonospaceDialog(
    title, mess, fontSize,
    makeTextSelectable = true,
    showDone = showDone
)
fun Context.errorWarningDialog(
    title: String, messId: Int, fontSize:Int = 12,
    showDone:Boolean = false) {
        errorWarningDialog(title, getString(messId), fontSize, showDone)
}

class MultiChoice (
    val actionPrompt:String,
    var isChecked:Boolean = false,
    var mutexGroup:Int? = null,
    val action: (()->Unit)? = null,
) {
    constructor(actionResId: Int, isChecked: Boolean = false,
        mutexGroup:Int? = null, action: (() -> Unit)? = null) :
        this(
            appContext.getString(actionResId),
            isChecked, mutexGroup, action
        )
    val title: String
        get() = actionPrompt
    override fun toString() =
        "MultiChoice = \"$actionPrompt:$isChecked:" +
            "action = \"${if (action != null) "assigned" else "NULL"}\""
}
fun Activity.multiChoiceDialog(
    prompt: String,
    vararg choices: MultiChoice,
    // if set and no choices use mutex
    // groups, put all choices in same group.
    isMutex: Boolean = true,
    noSelectionOk:Boolean = false,
    onComplete: ((Boolean) -> Unit)? = null
) {
    val multiChoice : ConstraintLayout =
        layoutInflater.inflate(
            R.layout.multi_choice_dialog, null) as ConstraintLayout
    val buttonView = multiChoice.findViewById<LinearLayout>(R.id.buttons_layout)
    val mutexMap = mutableMapOf<Int, RadioGroup>()
    val promptTv: TextView = multiChoice.findViewById(R.id.prompt_tv)
    val selectBtn : Button = multiChoice.findViewById(R.id.select_btn)
    selectBtn.setTextColor(Color.GREEN)
    selectBtn.enable(noSelectionOk)
    val cancelBtn : Button = multiChoice.findViewById(R.id.cancel_btn)
    cancelBtn.setTextColor(Color.RED)
    if (isMutex)
        choices.map{it.mutexGroup = 100}
    selectBtn.enable(choices.any{it.isChecked} || noSelectionOk )
    promptTv.text = prompt
    var buttonId = 100
    choices.mapIndexed {i,  it ->
        val rb = RadioButton(this)
        rb.id = buttonId++
        rb.id = i
        rb.text = HtmlCompat.fromHtml(
            it.actionPrompt, HtmlCompat.FROM_HTML_MODE_LEGACY)
        rb.isChecked = it.isChecked
        rb.tag = it
        val parent = rb.parent
        if (parent != null)
            (parent as ViewGroup).removeView(rb)
        rb.setOnClickListener { view ->
            val radioButton = view as RadioButton
            val choice = radioButton.tag as MultiChoice
            if (choice.mutexGroup != null) {
                choices.filter{
                    it.mutexGroup != null && it.mutexGroup == choice.mutexGroup
                }.map { if (it != choice) it.isChecked = false }
                choice.isChecked = true
            }
            else {
                choice.isChecked = !choice.isChecked
                radioButton.isChecked = choice.isChecked
            }
            selectBtn.enable(true)
        }
        if (it.mutexGroup != null) {
            val key = it.mutexGroup!!
            if (!mutexMap.containsKey(key)) {
                val rg = RadioGroup(this)
                mutexMap[key] = rg
                buttonView.addView(rg)
            }
            mutexMap[key]!!.addView(rb)
        }
        else
            buttonView.addView(rb)
    }
    val dialog = AlertDialog.Builder(this)
        .setView(multiChoice)
        .create()
    var wasSelected = false
    cancelBtn.setOnClickListener{
        Toast.makeText(this,
            getString(R.string.op_canceled),
            Toast.LENGTH_SHORT).show()
        dialog.dismiss()
    }
    selectBtn.setOnClickListener {
        val button = it as AppCompatButton
        try {
            choices.map { choice ->
                if ( choice.isChecked)
                     choice.action?.invoke()
            }
            wasSelected = true
        }
        catch (e: Exception) {
            Log.e(TAG, "Edit card exception", e)
            button.context.monospaceDialog("Error in SetCard",
                "Exception in setCard: $e")
        }

        dialog.dismiss()
    }
    dialog.setCanceledOnTouchOutside(false)
    dialog.setOnDismissListener{
        onComplete?.invoke(wasSelected)
    }
    dialog.show()
}
fun Activity.multiChoiceDialog(
    promptResId: Int,
    vararg choices: MultiChoice,
    // if set and no choices use mutex
    // groups, put all choices in same group.
    isMutex: Boolean = true,
    noSelectionOk:Boolean = false,
    onComplete: ((Boolean) -> Unit)? = null
) = multiChoiceDialog(
    getString(promptResId), *choices,
    isMutex = isMutex, noSelectionOk = noSelectionOk,
    onComplete = onComplete
)

fun Activity.yesNo(prompt:String, doThis: (Boolean)->Unit) {
    val y = MultiChoice(R.string.yes_str)
    val n = MultiChoice(R.string.no_str)
    multiChoiceDialog(prompt, y, n, onComplete = {
        selected -> doThis(selected && y.isChecked)
    })
}
fun Activity.yesNo(resId:Int, doThis: (Boolean)->Unit) =
    yesNo(getString(resId), doThis)



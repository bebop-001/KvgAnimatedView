package com.kana_tutor.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import com.kana_tutor.kvgviewer.R

fun Activity.webViewAlert (webView : WebView, title:String) {
    // this as context necessary or you can get "need app compatible..."
    // app.applicationContext or app.baseContext won't work.
    val dialog = AlertDialog.Builder(this as Context)
        .setView(webView)
        .setNegativeButton(
            R.string.done
        ) { dialogInterface, _ -> dialogInterface.dismiss() }
        .setCancelable(false)
    if (title.isNotEmpty()) dialog.setTitle(title)
    dialog.show()
}

fun Activity.webviewAlert(uri:String, title:String = "") : Boolean{
    val webView = WebView(this)
    webView.webViewClient = object : WebViewClient() {
        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            Log.i("WebView", "Attempting to load URL: $url")
            // if external link, start with a browser.  Should only hit
            // from the about google play link.
            if (url.startsWith("https://") || url.startsWith("http://")) {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }
            else
                view.loadUrl(url)
            return true
        }
    }
    webView.loadUrl(uri)
    webViewAlert(webView, title)
    return true
}
fun Activity.webviewAlert(uri:String, resId:Int) : Boolean{
    return webviewAlert(uri, getString(resId))
}
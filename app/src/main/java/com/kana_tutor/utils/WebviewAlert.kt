package com.kana_tutor.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.kana_tutor.kvgviewer.R

fun Activity.webViewAlert (webView : ConstraintLayout, title:String) {
    // this as context necessary or you can get "need app compatibwebContainerle..."
    // app.applicationContext or app.baseContext won't work.
    val dialog = AlertDialog.Builder(this as Context)
        .setView(webView)
        .setCancelable(false)
        .create()
    if (title.isNotEmpty()) dialog.setTitle(title)
    dialog.show()
    webView.findViewById<Button>(R.id.done_btn).setOnClickListener {
        dialog.cancel()
    }
}
fun Activity.webviewAlert(uri:String, title:String = "") : Boolean{
    val webContainer = layoutInflater.inflate(
        R.layout.web_dialog, null) as ConstraintLayout
    val webView = webContainer.findViewById<WebView>(R.id.webview)
    // webview doesn't know it's height so normal approach
    // to setting height in xml won't work if it can't
    // take full view.  Instead, get height of main
    // display and subtract a fudge factor as
    // a calculated height.
    val h = resources.displayMetrics.heightPixels
    webView.layoutParams.height = h - 350
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
    webContainer.findViewById<Button>(R.id.go_back_btn).setOnClickListener {
        if (webView.canGoBack()) {
            webView.goBack();
        }
    }
    webViewAlert(webContainer, title)
    return true
}
fun Activity.webviewAlert(uri:String, resId:Int) : Boolean{
    return webviewAlert(uri, getString(resId))
}
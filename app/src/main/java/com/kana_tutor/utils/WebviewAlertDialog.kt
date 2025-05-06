/*
 * Copyright 2025 Steven Smith kana-tutor.com
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
package com.kana_tutor.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.kana_tutor.kvgviewer.R

fun Activity.webviewAlertDialog(uri:String, title:String = "") : Boolean{
    val webContainer = layoutInflater.inflate(
        R.layout.web_dialog, null) as ConstraintLayout
    val webFrame = webContainer.findViewById<FrameLayout>(R.id.webframe)
    val webView = webFrame.findViewById<WebView>(R.id.webview)
    // webview doesn't know it's height so normal approach
    // to setting height in xml won't work if it can't
    // take full view.  Instead, get height of main
    // display and subtract a fudge factor as
    // a calculated height.
    val h = resources.displayMetrics.heightPixels - 350
    webFrame.layoutParams.height = h
    webView.layoutParams.height = h
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
            webView.goBack()
        }
    }
    // this as context necessary or you can get "need app compatible..."
    // app.applicationContext or app.baseContext won't work.
    val dialog = AlertDialog.Builder(this as Context)
        .setView(webContainer)
        .setCancelable(false)
        .create()
    if (title.isNotEmpty()) dialog.setTitle(title)
    dialog.show()
    webContainer.findViewById<Button>(R.id.done_btn).setOnClickListener {
        dialog.cancel()
    }
    return true
}
@Suppress("unused")
fun Activity.webviewAlertDialog(uri:String, resId:Int) : Boolean{
    return webviewAlertDialog(uri, getString(resId))
}

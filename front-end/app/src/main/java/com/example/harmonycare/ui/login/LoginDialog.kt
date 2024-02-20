package com.example.harmonycare.ui.login
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.harmonycare.R

class LoginDialog (
    context: Context,
    private val oauthUrl: String,
    private val callback: (String?) -> Unit // 콜백 함수
    ) : Dialog(context) {

        private lateinit var webView: WebView

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.dialog_login)
            webView = findViewById(R.id.gooLogin)
            setupWebView()
            loadLoginPage()
        }

        private fun setupWebView() {
            webView.settings.javaScriptEnabled = true
            webView.webViewClient = WebViewClient()
            webView.webChromeClient = WebChromeClient()

        }

        private fun loadLoginPage() {
            webView.loadUrl(oauthUrl)
        }

        override fun onBackPressed() {
            super.onBackPressed()
            callback(null) // 취소 시 콜백 호출
        }
    }
package com.example.harmonycare

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.example.harmonycare.data.SharedPreferencesManager
import com.example.harmonycare.retrofit.ApiManager
import com.example.harmonycare.retrofit.ApiService
import com.example.harmonycare.retrofit.RetrofitClient
import com.example.harmonycare.ui.addbaby.AddBabyActivity
import java.util.regex.Pattern


class LoginActivity : AppCompatActivity() {

    private lateinit var loginButton: ImageButton
    private lateinit var webView: WebView
    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // SharedPreferences 초기화
        SharedPreferencesManager.init(this)

        webView = findViewById(R.id.webView)
        loginButton = findViewById(R.id.GoogleLoginButton)

        webView.settings.apply {
            // 웹뷰 설정 초기화
            userAgentString = null
            javaScriptEnabled = false

            // 새로운 설정 적용
            userAgentString = "Mozilla/5.0 AppleWebKit/535.19 Chrome/120.0.0 Mobile Safari/535.19"
            javaScriptEnabled = true
        }

        webView.webViewClient = object : WebViewClient() {

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null && url.startsWith("http://localhost:8080")) {
                    // 리다이렉션 링크를 가져와서 파라미터 추출
                    val authCode = extractCodeFromUrl(url)
                    Log.d("kkang", "url:$url")
                    Log.d("kkang", "authCode:$authCode")
                    if (authCode != null) {

                        val apiService = RetrofitClient.createService(ApiService::class.java)
                        val apiManager = ApiManager(apiService)
                        // 여기서 POST요청
                        apiManager.loginUser(authCode,
                            onResponse = { accessToken, hasBaby ->
                                // accessToken을 저장하거나 필요한 작업을 수행합니다.
                                showToast("accessToken 저장됨: $accessToken")
                                Log.d("kkang", "accessToken:$accessToken")
                                SharedPreferencesManager.saveAccessToken(accessToken)

                                // hasBaby가 true이면 MainActivity로, false이면 AddBabyActivity로 이동
                                val destinationActivity = if (hasBaby) MainActivity::class.java else AddBabyActivity::class.java
                                startActivity(Intent(this@LoginActivity, destinationActivity))

                                // 현재 액티비티 종료
                                //finish()
                            },
                            onFailure = {
                                // 실패한 경우 처리
                                // 실패할 경우에도 반드시 로그를 남기고, 그에 따른 예외 처리를 수행해야 합니다.
                                Log.e("LoginActivity", "Login failed")
                            }
                        )
                        //아기 정보 조회
                        //아기 정보가 있는 경우 바로 MainActivity로 이동
                        // MainActivity로 이동
                        // startActivity(Intent(this@LoginActivity, MainActivity::class.java))

                        //아기 정보가 없는 경우, AddBabyActivity로 이동하여 아기 생성


                    } else {
                        showToast("인증 코드가 없습니다.")
                    }
                    return true // 리다이렉션된 링크 처리를 위해 true 반환
                }
                // 다른 URL은 기본적인 방식으로 처리
                return super.shouldOverrideUrlLoading(view, url)
            }

            //로그아웃

        }

        val url = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "scope=https://www.googleapis.com/auth/userinfo.email%20https://www.googleapis.com/auth/userinfo.profile&" +
                "access_type=offline&" +
                "include_granted_scopes=true&" +
                "response_type=code&" +
                "redirect_uri=http://localhost:8080&" +
                "client_id=185976520158-phphtutm302clototd3rqgecng4a4bg2.apps.googleusercontent.com"



        /*// Google 로그인 페이지 URL 로드
        val url = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "scope=https://www.googleapis.com/auth/userinfo.email%20https://www.googleapis.com/auth/userinfo.profile&" +
                "access_type=offline&" +
                "include_granted_scopes=true&" +
                "response_type=code&" +
                "redirect_uri=http://localhost:8080&" +
                "client_id=185976520158-phphtutm302clototd3rqgecng4a4bg2.apps.googleusercontent.com"*/

        loginButton.setOnClickListener {
            // SharedPreferences에서 로그인 정보 삭제하여 로그아웃 처리
            //logout()

            // 이미지 버튼 클릭 시 웹뷰 로드
            webView.visibility = View.VISIBLE
            webView.loadUrl(url)
        }
    }


    private fun extractCodeFromUrl(url: String): String? {
        val pattern = Pattern.compile("\\bcode=([^&]+)")
        val matcher = pattern.matcher(url)

        return if (matcher.find()) {
            matcher.group(1)
        } else {
            null
        }
    }
    private fun logout() {
        val editor = sharedPreferences.edit()
        editor.remove("accessToken") // accessToken 제거
        editor.apply()

        // 여기서 로그아웃 후의 동작을 정의할 수 있음
    }

    private fun saveAccessTokenCode(accessToken: String) {
        // SharedPreferences에 accessToken 저장
        val editor = sharedPreferences.edit()
        editor.putString("accessToken", accessToken)
        editor.apply()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
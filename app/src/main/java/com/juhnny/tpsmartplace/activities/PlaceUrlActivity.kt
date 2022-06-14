package com.juhnny.tpsmartplace.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import com.juhnny.tpsmartplace.databinding.ActivityPlaceUrlBinding

class PlaceUrlActivity : AppCompatActivity() {

    val b by lazy { ActivityPlaceUrlBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)

        var placeUrl = intent.getStringExtra("placeUrl") ?: "" //빈 문자열이면 화면에 아무것도 안 보일 것
        //아래 세 설정은 필수라고 보면 된다.
        b.webView.webViewClient = WebViewClient() //웹사이트를 브라우저앱을 띄우지 않고 웹뷰 안에서 띄우기
        b.webView.webChromeClient = WebChromeClient() //팝업이 뜨도록 하기
        b.webView.settings.javaScriptEnabled = true //JS가 기본적으로 막혀있어서
        b.webView.loadUrl(placeUrl)
    }

    override fun onBackPressed() {
        if(b.webView.canGoBack()) b.webView.goBack()
        else super.onBackPressed()
    }
}
package com.juhnny.tpsmartplace

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        //Kakao SDK 초기화
        KakaoSdk.init(this, "4767d361acf7080b8b6817f70dd5e840")
    }
}
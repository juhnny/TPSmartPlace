package com.juhnny.tpsmartplace.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView()가 아닌 테마를 이용해 화면 만들어보기
        //동적 반응할 필요가 없으면 테마로만 만들어도 된다.
        //themes.xml에 테마를 새로 만든다.

        //일정 시간이 지나 작업이 되도록 하는 방법 - timer, alarm manager, looper
        //스레드가 메인 스레드에 뭔가 전달할 때 사용하는 게 handler
//        Handler(Looper.getMainLooper()).postDelayed(object : Runnable{
//            override fun run() {
//                TODO("Not yet implemented")
//            }
//        }, 1500) //메인 스레드의 Looper를 가져와서 뭔가 전달할 수 있다.

        //람다 표기로 줄여쓰기
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 1500)
    }
}
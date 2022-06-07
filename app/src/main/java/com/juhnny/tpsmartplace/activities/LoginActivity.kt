package com.juhnny.tpsmartplace.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.juhnny.tpsmartplace.G
import com.juhnny.tpsmartplace.databinding.ActivityLoginBinding
import com.juhnny.tpsmartplace.model.UserAccount
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient

class LoginActivity : AppCompatActivity() {

    val b by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)

        //둘러보기 누르면 MainActivity로 이동
        b.tvLookaround.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        //회원가입 버튼 클릭시 회원가입 화면으로 이동
        b.tvSignup.setOnClickListener{
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        //이메일로 로그인 버튼 클릭
        b.layoutLoginEmail.setOnClickListener{
            startActivity(Intent(this, EmailSignInActivity::class.java))
        }

        //간편로그인 버튼들
        b.btnLoginKakao.setOnClickListener { loginWithKakao() }
        b.btnLoginGoogle.setOnClickListener { loginWithGoogle() }
        b.btnLoginNaver.setOnClickListener { loginWithNaver() }
    }

    //이메일 로그인과 간편로그인을 통틀어서 email 하나만으로 식별자를 쓸 수 있으면 편하겠지만
    //아쉽게도 간편로그인을 할 경우 사용자가 이메일 제공을 거부할 수도 있어서 그런 경우 적용 불가
    //이메일 대신 유저를 구분할 유니크한 식별자가 필요하다.
    //다행히 간편로그인 시 카카오나 구글이나 네이버나 ID(내부 로직에 의한 id)를 제공해주고 이건 사용자가 거부할 수 없어서 예외 없이 받을 수 있다.
    //이메일 로그인 시에는 어차피 document(각 유저 정보)가 만들어질 때 이름(식별자)이 Firebase에서 랜덤한 유니크값(아마도 해시값)으로 지정되므로
    //(원한다면 사용자 정의로 규칙을 만들어서 이름을 지정해줘도 된다.)
    //이메일 로그인 시에는 document 이름을 식별자로 사용하는 것으로 한다.

    fun loginWithKakao(){
        //개발자 문서를 참고해서 진행
        //참고로 키 해시를 등록해야 하는데
        //android sdk에 발급된 디버그용 키를 사용하면 컴퓨터마다 키를 다 등록해줘야 한다.
        //카카오 SDK 키 해시를 사용하면 하나의 키로 여러 컴퓨터에서 다 사용할 수 있어서 추천
        //결국에는 릴리즈 시 릴리즈용 키도 등록하게 될 것
        var keyHash = Utility.getKeyHash(this)
        Log.i("Kakao key hash", "$keyHash")

        //카카오 개발문서의 로그인 구현 예제를 참고해 기능을 구현해보자. (좀 더 간단하게 구현하였음)

        //카카오 로그인 시도 완료 시 반응하는 callback 객체 생성
//        val callback: (OAuthToken?, Throwable?) -> Unit = fun(token:OAuthToken?, error:Throwable?){}
        val callback: (OAuthToken?, Throwable?) -> Unit = {token, error ->
            if(error != null){
                Toast.makeText(this, "카카오 로그인 실패", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "카카오 로그인 성공", Toast.LENGTH_SHORT).show()

                UserApiClient.instance.me { user, error ->
                    if (user != null){
                        var id:String = user.id.toString() //무조건 id는 제공됨
                        var email:String = user.kakaoAccount?.email ?: "" //email은 사용자가 선택 동의 가능. 거부하면 null이 올 수 있다.
                        //그렇다고 email을 nullable로 만들면 연쇄적으로 email이 쓰이는 곳을 다 nullable로 처리해줘야 하는 게 번거로움
                        //그러니 엘비스 연산자로 null일 때도 String으로 처리해주면 편리
                        //또 email이 없는 사용자라고 해서 null이라고 보여주는 것도 이상하니까.
                        G.userAccount = UserAccount(id, email)

                        //MainActivity로 이동
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }
            }
        }

        //카카오톡이 설치돼 있으면 카톡 로그인, 없으면 카카오 계정 로그인
        if(UserApiClient.instance.isKakaoTalkLoginAvailable(this)){
            UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }

    }

    fun loginWithGoogle(){

    }

    fun loginWithNaver(){

    }
}
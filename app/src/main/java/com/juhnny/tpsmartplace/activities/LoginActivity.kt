package com.juhnny.tpsmartplace.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.juhnny.tpsmartplace.G
import com.juhnny.tpsmartplace.databinding.ActivityLoginBinding
import com.juhnny.tpsmartplace.model.NaverUserInfoResponse
import com.juhnny.tpsmartplace.model.UserAccount
import com.juhnny.tpsmartplace.network.RetrofitHelper
import com.juhnny.tpsmartplace.network.RetrofitService
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

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

    //로그인 정보를 가져오는 각각 다른 방법
    //Kakao는 sdk 안에 있는 me 메소드를 사용
    //Google은 액티비티를 열고 Intent를 이용해 가져옴
    //Naver는 REST 방식으로

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
        //Google login과 Firebase Auth는 기본적으로 별개의 서비스. 라이브러리도 별개다.
        //Firebase Auth에서 지원하는 다른 간편로그인 서비스도 여러개 쓸 때는 Firebase Auth를 쓰는 게 편리하겠지만
        //Google login만 할 거라면 굳이 Firebase auth 라이브러리를 사용하지 않아도 된다.
        //Firebase auth 개발문서를 참고하면 Firebase auth를 써서 구현하도록 돼있으니
        //Google login 개발문서를 참고해서 만들도록 하자. 구현 방법도 더 간단하다.
        //단 Google 프로젝트와 Firebase 프로젝트는 연동이 돼있어서
        //Firebase Auth에서 구글 로그인을 사용하겠다고 하면 구글 플레이 콘솔에도 프로젝트가 만들어지고
        //Firebase Auth에서 SHA-1값을 입력하면 구글 플레이 콘솔에도 사용자 인증 정보가 자동으로 입력된다.
        //Firebase Auth에서 SHA-1 값을 입력한 뒤 google-service.json 파일도 바꿔준 뒤 구글 로그인 문서를 참고해 작성하면 아래와 같다.

        //옵션 객체는 아래 GoogleSign 객체를 만들기 위해 미리 옵션을 설정하는 용도
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //이렇게만 쓰면 id밖에 못 가져온다. 이메일도 요청해야 한다.
            .requestEmail()
            .build()

        //구글 로그인 화면 액티비티를 실행시켜주는 Intent 객체 얻어오기
        //액티비티를 열려면 Intent에 열려는 액티비티 이름을 쓰거나 알맞는 옵션을 써줘야 하는데..
        //액티비티 이름이라던가 Intent. 눌렀을 때 나오는 옵션을 뭘 써주면 구글 로그인 액티비티가 열리는지 구글은 노출이 안돼있다.
        //그래서 구글이 만들어주는 Intent를 써야 한다.
        val intent = GoogleSignIn.getClient(this, gso).signInIntent
        resultLauncher.launch(intent)
    }

    val resultLauncher:ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult>{
        override fun onActivityResult(result: ActivityResult?) {
            //로그인이 잘 됐는지 검사하고 그 다음 작업을 해주자.
            if(result?.resultCode == RESULT_CANCELED) return

            //로그인 결과를 가져온 intent 객체 소환
            //Activity 간에 데이터를 주고 받을 땐 줄 때도 받을 때도 무조건! Intent가 필요하다.
            val intent = result?.data
            //Intent로부터 .getBundleExtra("???") 작업을 해줘야 하는데 그 키값을 모르네?
            //Intent로부터 구글계정 정보를 가져오는 작업 객체 생성
            val account:GoogleSignInAccount = GoogleSignIn.getSignedInAccountFromIntent(intent).result //add 리스너를 할 수도 있고 result를 달라고 할 수도 있다. 알아서 동기식으로 가져온다. 다음 명령들은 대기상태로 기다림
            //계정 정보 저장
            val id = account.id.toString()
            val email = account.email ?: "" //null 처리
            Log.i("GoogleSignInAccount", "$id / $email")
            G.userAccount = UserAccount(id, email)

            //Main 화면으로 이동
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
    })

    fun loginWithNaver(){
        //네이버 아이디로 로그인하기(네아로SDK) - 사용자 정보를 REST API로 받아오는 방식
        //Retrofit 네트워크 라이브러리를 사용하기

        //네이버 개발자 센터의 가이드 문서 참고
        //nid oauth sdk 추가

        //초기화
        NaverIdLoginSDK.initialize(this, "suyX4kJ722_ArMzzqw9I", "8D0kBNHHi8", "빠른 지도찾기")

        //authenticate() 메서드를 이용한 로그인
        //로그인 정보를 받는 것이 아니라 로그인 정보를 받기 위한 REST API의 접근 키(여기서는 토큰)을 발급받는 것임
        //보안 문제나 트래픽 문제 때문에 이런 방식을 택한 듯. 토큰은 (아마도) 30분마다 만료
        //이 토큰으로 네트워크 API를 통해 JSON 데이터를 받아 정보를 얻어오는 것
        NaverIdLoginSDK.authenticate(this, object : OAuthLoginCallback{
            override fun onError(errorCode: Int, message: String) {
                Toast.makeText(this@LoginActivity, "Server error : $message", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(httpStatus: Int, message: String) {
                Toast.makeText(this@LoginActivity, "로그인 실패 : $message", Toast.LENGTH_SHORT).show()
            }

            override fun onSuccess() {
                Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()

                //사용자 정보를 가져오는 REST API의 접속토큰 받아오기
                val accessToken: String? = NaverIdLoginSDK.getAccessToken()
                Toast.makeText(this@LoginActivity, "Token: $accessToken", Toast.LENGTH_SHORT).show()

                //사용자 정보 가져오는 네트워크 작업 - Retrofit2 이용
                //네이버 회원 프로필 조회 API (https://developers.naver.com/docs/login/profile/profile.md#%EB%84%A4%EC%9D%B4%EB%B2%84-%ED%9A%8C%EC%9B%90-%ED%94%84%EB%A1%9C%ED%95%84-%EC%A1%B0%ED%9A%8C-api-%EB%AA%85%EC%84%B8)
                val retrofit = RetrofitHelper.getRetrofitInstance("https://openapi.naver.com")
                val retrofitInterface = retrofit.create(RetrofitService::class.java).getNidUserInfo("Bearer $accessToken")
                    .enqueue(object : Callback<NaverUserInfoResponse>{
                        override fun onResponse(
                            call: Call<NaverUserInfoResponse>,
                            response: Response<NaverUserInfoResponse>
                        ) {
                            val userInfo:NaverUserInfoResponse? = response.body()
                            val id = userInfo?.response?.id ?: ""
                            val email = userInfo?.response?.email ?: ""
                            Toast.makeText(this@LoginActivity, "email : $email", Toast.LENGTH_SHORT).show()
                            G.userAccount = UserAccount(id, email)
                            //main 화면으로 이동
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }

                        override fun onFailure(call: Call<NaverUserInfoResponse>, t: Throwable) {
                            Toast.makeText(this@LoginActivity, "회원정보 읽기 실패 : ${t.message}", Toast.LENGTH_SHORT).show()
                        }

                    })
            }
        })
    }
}
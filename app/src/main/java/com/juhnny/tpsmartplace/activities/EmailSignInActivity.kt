package com.juhnny.tpsmartplace.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.juhnny.tpsmartplace.G
import com.juhnny.tpsmartplace.R
import com.juhnny.tpsmartplace.databinding.ActivityEmailSignInBinding
import com.juhnny.tpsmartplace.model.UserAccount

class EmailSignInActivity : AppCompatActivity() {

    val b by lazy { ActivityEmailSignInBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)

        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)

        b.btnSignin.setOnClickListener { clickSignIn() }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun clickSignIn(){
        val email = b.etEmail.text.toString()
        val pw = b.etPw.text.toString()

        val db = FirebaseFirestore.getInstance()
        db.collection("emailUser")
            .whereEqualTo("email", email)
            .whereEqualTo("password", pw)
            .get()
            .addOnSuccessListener {
                //QuerySnapshot 안에 DocumentSnapshot들이 들어있다.
                //resultSet 테이블과 각 레코드라고 생각하면 된다.
                if(it.documents.size > 0){ //일치하는 도큐먼트가 있으면. 여기서는 한 개만 올 것
                    //로그인 성공
                    //firestore DB의 랜덤한 document 명을 id로 사용
                    val id = it.documents[0].id
                    //유저 정보를 저장해놓고 쓸 UserAccount 클래스를 만들고
                    //Global 클래스를 만들어 정적 변수로 만들어둔다.
                    G.userAccount = UserAccount(id, email)

                    val intent = Intent(this, MainActivity::class.java)
                    //MainActivity로 이동을 하는 건 좋은데 혹시 Back 할지 몰라서 살려뒀던 LoginActivity가 걸린다.
                    // 이대로 두면 나중에 나중에 사용자가 앱을 종료할 때 다시 나타날 것이다.
                    // 여기서는 기존 task를 없애고 새 task를 시작하는 방식을 써본다.

                    //finish()하지 않은 액티비티들은 메모리의 Activity Back Stack 영역에 보관되게 되는데...
                    //이 Activity Back Stack 영역은 task마다 별개로 만들어지기 때문에
                    //task를 새롭게 시작하면서 기존 task를 지우면 기존 액티비티들을 날려버릴 수 있다.
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

                    //참고로 back stack에 A액티비티가 이미 존재하더라도 A액티비티를 스타트 하면 새로 만들어진다.
                    //Intent.FLAG_ACTIVITY_REORDER_TO_FRONT를 쓰면 그 액티비티 객체를 back stack 제일 위로 가져올 수 있다.
                    //Intent.FLAG_ACTIVITY_SINGLE_TOP은 새로 start하는 액티비티 클래스가 지금 보고 있는 화면과 같은 클래스일 때 새 액티비티 객체를 만들지 않는다.
                    //하지만 지금 보고 있는 화면이 그 액티비티 클래스가 아니라면 stack 밑에 같은 액티비티가 있어도 다시 그 액티비티를 만든다.

                } else { //일치하는 도큐먼트가 없으면
                    AlertDialog.Builder(this).setMessage("이메일과 비밀번호를 다시 확인해주세요.").create().show()
                    b.etEmail.requestFocus()
                    b.etEmail.selectAll()
                    //원래는 키패드까지 올리는 작업을 해준다.
                }
            }.addOnFailureListener {

            }

    }
}
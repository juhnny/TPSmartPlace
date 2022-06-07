package com.juhnny.tpsmartplace.activities

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.juhnny.tpsmartplace.R
import com.juhnny.tpsmartplace.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    val b by lazy { ActivitySignUpBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)

        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)

        b.btnSignup.setOnClickListener{ signUp() }
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when(item.itemId){
//            android.R.id.home -> finish()
//        }
//        return super.onOptionsItemSelected(item)
//    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun signUp(){
        //Firestore Firebase에 사용자 정보 저장하기
        val email = b.etEmail.text.toString()
        val pw = b.etPw.text.toString()
        val pwConfirm = b.etPwConfirm.text.toString()

        //유효성 검사

        //비번과 비번 확인이 일치하나?
        //코틀린에서는 문자열 비교 시 equals를 쓰지 않고 그냥 == 을 사용하길 권장.
        // 내부적으로 String에서는 ==이 equals를 사용하게 돼있음(Operator overiding)
        if(pw != pwConfirm){
            AlertDialog.Builder(this).setMessage("패스워드를 동일하게 입력해주세요.").create().show()
            b.etPwConfirm.selectAll() //텍스트를 모두 선택한 상태로 만듦. 마지막에 포커스를 갖고 있던 뷰여야 문제 없이 가능
            return
        }

        //정규표현식 검사도 필요

        //Firebase 사용하기
        val firestore = FirebaseFirestore.getInstance()

        //중복되지 않은 아이디인지 검증
        firestore.collection("emailUser")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener {
            //Success의 의미는 그런 레코드를 찾았다는 게 아니다.
            //Query 작업을 수행했다는 의미. 레코드를 못찾았을 수도 있다.
            //갖고 돌아온 레코드가 여러개일 수 있음
            if(it.documents.size > 0){ //중복된 이메일이 있으면
                AlertDialog.Builder(this).setMessage("중복된 이메일이 존재합니다. 다시 확인 후 입력해주세요.").show()
                //etMail에 마지막에 포커스가 가있던 상황이 아니라면 selectAll()이 동작하지 않을 수 있다.
                b.etEmail.requestFocus()
                b.etEmail.selectAll()

            } else { //중복된 이메일이 없으면 새로 등록

                //저장할 값들을 hashMap에 담는다. 이 user 정보가 하나의 document가 될 것
                val user:MutableMap<String, String> = mutableMapOf<String, String>()
                user.put("email", email)
                user.put("password", pw)

                //firestore.collection("emailUsers").document("record1").set(user) //원래는 이렇게 쓴다. document이름까지 지정할 수 있음
                firestore.collection("emailUser").add(user).addOnCompleteListener{
                    if(it.isSuccessful){
                        AlertDialog.Builder(this)
                            .setMessage("회원가입이 완료되었습니다.")
                            .setPositiveButton("확인", object : DialogInterface.OnClickListener{
                                override fun onClick(p0: DialogInterface?, p1: Int) {
                                    finish()
                                }
                            })
                            .create().show()
                    } else if(it.isCanceled){
                        AlertDialog.Builder(this).setMessage("회원가입에 실패했습니다. 다시 시도해주세요.").create().show()
                    }
                } //이 document의 이름은 랜덤하게 주어짐
            }
        }.addOnFailureListener{
            //Failure의 의미는 그런 레코드가 없다는 게 아니다.
            //어떤 문제(보통 네트워크) 때문에 쿼리 작업을 하지 못했다는 것
            Toast.makeText(this, "네트워크가 불안정합니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
        }
    }
}
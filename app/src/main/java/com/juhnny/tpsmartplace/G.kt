package com.juhnny.tpsmartplace

import com.juhnny.tpsmartplace.model.UserAccount

//선생님이 느끼기에 Global 클래스는 전역 변수이니까 어느 내부 패키지에 속한 느낌이 아니라서 그냥 기본 패키지에 넣는다고..
//개발자에 따라 model에 넣기도 하고 의견이 조금씩 다르다고..
class G {
    companion object{
        var userAccount:UserAccount? = null
    }
}
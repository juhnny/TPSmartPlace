package com.juhnny.tpsmartplace.model

data class NaverUserInfoResponse(val resultCode: String, val message:String, val response:NaverUserInfo)

data class NaverUserInfo(val id:String, val email:String)
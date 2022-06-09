package com.juhnny.tpsmartplace.network

import com.juhnny.tpsmartplace.model.NaverUserInfoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface RetrofitService {

    //Nid 사용자정보 API
    @GET("/v1/nid/me")
    fun getNidUserInfo(@Header("Authorization") authorization:String) : Call<NaverUserInfoResponse>
}
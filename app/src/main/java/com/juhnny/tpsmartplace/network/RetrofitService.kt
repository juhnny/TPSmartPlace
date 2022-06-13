package com.juhnny.tpsmartplace.network

import com.juhnny.tpsmartplace.model.NaverUserInfoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface RetrofitService {

    //Nid 사용자정보 API
    //매번 토큰값이 바뀌므로 헤더값을 파라미터로 받아서 사용
    @GET("/v1/nid/me")
    fun getNidUserInfo(@Header("Authorization") authorization:String) : Call<NaverUserInfoResponse>

    //카카오 키워드 장소검색 API
    //헤더값이 고정된 REST API 키이므로 고정값으로 지정
    @GET("/v2/local/search/keyword.json")
    @Headers("Authorization: KakaoAK 0b52e02d9e6a7899c8a5e7608b8b97e7")
    fun searchPlaceToString(@Query("query") query: String,
                            @Query("y") latitude:String,
                            @Query("x") longitude:String):Call<String>

    @GET("/v2/local/search/keyword.json")
    @Headers("Authorization: KakaoAK 0b52e02d9e6a7899c8a5e7608b8b97e7")
    fun searchPlace(@Query("query") query: String,
                            @Query("y") latitude:String,
                            @Query("x") longitude:String):Call<String>
}
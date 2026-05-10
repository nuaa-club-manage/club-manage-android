package com.clubmgmt.app.data.api
import retrofit2.http.Body
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("api/user/captcha")
    suspend fun getCaptcha(): Response<CaptchaResponse>

    @POST("api/user/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/user/sendCode")
    suspend fun sendCode(@Query("contact") contact: String): Response<SendCodeResponse>
}

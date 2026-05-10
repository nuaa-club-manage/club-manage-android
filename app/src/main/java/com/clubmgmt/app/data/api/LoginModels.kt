package com.clubmgmt.app.data.api

import com.google.gson.annotations.SerializedName

data class CaptchaData(
    @SerializedName("captchaId")
    val captchaId: String,
    @SerializedName("captchaText")
    val captchaText: String
)

data class CaptchaResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: CaptchaData?
)

data class LoginRequest(
    @SerializedName("userId")
    val userID: String,
    @SerializedName("loginType")
    val loginType: Int,
    @SerializedName("userPassword")
    val userPassword: String,
    @SerializedName("contact")
    val contact: String,
    @SerializedName("verifyCode")
    val verifyCode: String,
    @SerializedName("captchaId")
    val captchaId: String,
    @SerializedName("captchaCode")
    val captchaCode: String
)

data class SendCodeResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: String?
)

data class LoginResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: String?
)

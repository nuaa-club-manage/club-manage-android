package com.clubmgmt.app.data.api

import com.google.gson.annotations.SerializedName

// ======================== 通用响应包装 ========================
// 后端全局 Result<T> 结构：{ code: Int, message: String, data: T }
open class ApiResult {
    @SerializedName("code")
    var code: Int = 0

    @SerializedName("message")
    var message: String = ""
}

// data 为字符串的响应
class ApiStringResponse : ApiResult() {
    @SerializedName("data")
    var data: String? = null
}

// ======================== 认证模块 ========================

data class LoginRequest(
    @SerializedName("userId") val userId: String,
    @SerializedName("loginType") val loginType: Int,       // 0-密码登录，1-验证码登录
    @SerializedName("userPassword") val userPassword: String,
    @SerializedName("contact") val contact: String,
    @SerializedName("verifyCode") val verifyCode: String,
    @SerializedName("captchaId") val captchaId: String,
    @SerializedName("captchaCode") val captchaCode: String
)

data class RegisterRequest(
    @SerializedName("userId") val userId: String,
    @SerializedName("contact") val contact: String,
    @SerializedName("userPassword") val userPassword: String,
    @SerializedName("verifyCode") val verifyCode: String
)

data class ResetPasswordRequest(
    @SerializedName("contact") val contact: String,
    @SerializedName("verifyCode") val verifyCode: String,
    @SerializedName("newPassword") val newPassword: String
)

// 验证码响应
data class CaptchaData(
    @SerializedName("captchaId") val captchaId: String,
    @SerializedName("captchaText") val captchaText: String
)

class CaptchaResponse : ApiResult() {
    @SerializedName("data")
    var data: CaptchaData? = null
}

// 管理员登录
data class AdminLoginRequest(
    @SerializedName("userId") val userId: String,
    @SerializedName("userPassword") val userPassword: String
)

// ======================== 用户模块 ========================

// 用户信息响应（对应后端 UserInfoRespDTO）
// 注意：phoneNumber/userMailbox/realName/gender/degree/school 在后端可为 null
data class UserInfoData(
    @SerializedName("userId") val userId: String,
    @SerializedName("userName") val userName: String,
    @SerializedName("phoneNumber") val phoneNumber: String? = null,
    @SerializedName("userMailbox") val userMailbox: String? = null,
    @SerializedName("realName") val realName: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("degree") val degree: String? = null,
    @SerializedName("school") val school: String? = null,
    @SerializedName("registerTime") val registerTime: String? = null
)

class UserInfoResponse : ApiResult() {
    @SerializedName("data")
    var data: UserInfoData? = null
}

// 修改个人信息请求
data class UpdateUserInfoRequest(
    @SerializedName("userName") val userName: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("userMailbox") val userMailbox: String,
    @SerializedName("realName") val realName: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("degree") val degree: String,
    @SerializedName("school") val school: String
)

// 修改密码请求
data class ChangePasswordRequest(
    @SerializedName("oldPassword") val oldPassword: String,
    @SerializedName("newPassword") val newPassword: String
)

// ======================== 社团模块 ========================

// 创建社团请求
data class CreateClubRequest(
    @SerializedName("clubName") val clubName: String,
    @SerializedName("clubInformation") val clubInformation: String
)

// 修改社团信息请求
data class UpdateClubRequest(
    @SerializedName("clubId") val clubId: String,
    @SerializedName("clubName") val clubName: String,
    @SerializedName("clubInformation") val clubInformation: String,
    @SerializedName("school") val school: String
)

// 解散社团请求
data class DissolveClubRequest(
    @SerializedName("clubId") val clubId: String
)

// 社团实体（对应后端 Club 实体）
data class ClubData(
    @SerializedName("clubId") val clubId: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("clubName") val clubName: String,
    @SerializedName("clubInformation") val clubInformation: String? = null,
    @SerializedName("school") val school: String? = null,
    @SerializedName("clubState") val clubState: String? = null,  // pending/approved/rejected
    @SerializedName("establishmentTime") val establishmentTime: String? = null
)

class ClubListResponse : ApiResult() {
    @SerializedName("data")
    var data: List<ClubData>? = null
}

class ClubDetailResponse : ApiResult() {
    @SerializedName("data")
    var data: ClubData? = null
}

// ======================== 社团成员模块 ========================

// 申请加入社团
data class JoinClubRequest(
    @SerializedName("clubId") val clubId: String
)

// 退出社团
data class LeaveClubRequest(
    @SerializedName("clubId") val clubId: String
)

// 审核入社申请
data class AuditMemberRequest(
    @SerializedName("clubId") val clubId: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("pass") val pass: Boolean
)

// 我的入社申请记录（对应后端 ClubMemberApplyRespDTO）
data class ClubMemberApplyData(
    @SerializedName("clubId") val clubId: String,
    @SerializedName("clubName") val clubName: String,
    @SerializedName("reviewState") val reviewState: String? = null   // pending/approved/rejected
)

class ClubMemberApplyListResponse : ApiResult() {
    @SerializedName("data")
    var data: List<ClubMemberApplyData>? = null
}

// 待审核入社申请（对应后端 ClubMemberAuditRespDTO）
data class ClubMemberAuditData(
    @SerializedName("userId") val userId: String,
    @SerializedName("userName") val userName: String,
    @SerializedName("realName") val realName: String? = null,
    @SerializedName("studentId") val studentId: String? = null,
    @SerializedName("school") val school: String? = null,
    @SerializedName("degree") val degree: String? = null,
    @SerializedName("clubId") val clubId: String,
    @SerializedName("clubName") val clubName: String,
    @SerializedName("reviewState") val reviewState: String? = null
)

class ClubMemberAuditListResponse : ApiResult() {
    @SerializedName("data")
    var data: List<ClubMemberAuditData>? = null
}

// 社团成员列表（对应后端 ClubMemberListRespDTO）
data class ClubMemberListData(
    @SerializedName("userId") val userId: String,
    @SerializedName("userName") val userName: String,
    @SerializedName("realName") val realName: String? = null,
    @SerializedName("school") val school: String? = null,
    @SerializedName("degree") val degree: String? = null,
    @SerializedName("phoneNumber") val phoneNumber: String? = null,
    @SerializedName("clubId") val clubId: String,
    @SerializedName("clubName") val clubName: String,
    @SerializedName("clubManager") val clubManager: String? = null,  // 1-是管理员，null/0-普通成员
    @SerializedName("reviewState") val reviewState: String? = null
)

class ClubMemberListResponse : ApiResult() {
    @SerializedName("data")
    var data: List<ClubMemberListData>? = null
}

// ======================== 活动模块 ========================

// 发布活动请求
data class CreateActivityRequest(
    @SerializedName("clubId") val clubId: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("location") val location: String,
    @SerializedName("capacityLimit") val capacityLimit: Int
)

// 修改活动请求
data class UpdateActivityRequest(
    @SerializedName("activityId") val activityId: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("location") val location: String,
    @SerializedName("capacityLimit") val capacityLimit: Int
)

// 活动列表响应（对应后端 ActivityListRespDTO）
data class ActivityData(
    @SerializedName("activityId") val activityId: String,
    @SerializedName("clubId") val clubId: String,
    @SerializedName("clubName") val clubName: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("capacityLimit") val capacityLimit: Int? = null,
    @SerializedName("activityState") val activityState: String? = null,  // pending/approved/rejected/ended
    @SerializedName("publishTime") val publishTime: String? = null
)

class ActivityListResponse : ApiResult() {
    @SerializedName("data")
    var data: List<ActivityData>? = null
}

class ActivityDetailResponse : ApiResult() {
    @SerializedName("data")
    var data: ActivityData? = null
}

// ======================== 活动报名模块 ========================

// 报名活动请求
data class RegisterActivityRequest(
    @SerializedName("activityId") val activityId: String,
    @SerializedName("realName") val realName: String,
    @SerializedName("phoneNumber") val phoneNumber: String
)

// 取消报名请求
data class CancelRegistrationRequest(
    @SerializedName("registrationID") val registrationID: String
)

// 审核报名请求
data class AuditRegistrationRequest(
    @SerializedName("registrationId") val registrationId: String,
    @SerializedName("pass") val pass: Boolean
)

// 待审核报名列表（对应后端 RegistrationAuditViewDTO）
data class RegistrationAuditData(
    @SerializedName("registrationId") val registrationId: String,
    @SerializedName("activityId") val activityId: String,
    @SerializedName("title") val title: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("realName") val realName: String? = null,
    @SerializedName("phoneNumber") val phoneNumber: String? = null,
    @SerializedName("reviewState") val reviewState: String? = null
)

class RegistrationAuditListResponse : ApiResult() {
    @SerializedName("data")
    var data: List<RegistrationAuditData>? = null
}

// 用户个人报名列表（对应后端 UserRegistrationDetailDTO）
data class UserRegistrationData(
    @SerializedName("registrationId") val registrationId: String,
    @SerializedName("activityId") val activityId: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String? = null,
    @SerializedName("reviewState") val reviewState: String? = null,
    @SerializedName("publishTime") val publishTime: String? = null,
    @SerializedName("clubId") val clubId: String? = null
)

class UserRegistrationListResponse : ApiResult() {
    @SerializedName("data")
    var data: List<UserRegistrationData>? = null
}

// 已通过报名的参与者（对应后端 ApprovedParticipantDTO）
data class ApprovedParticipantData(
    @SerializedName("userId") val userId: String,
    @SerializedName("realName") val realName: String? = null,
    @SerializedName("phoneNumber") val phoneNumber: String? = null
)

class ApprovedParticipantListResponse : ApiResult() {
    @SerializedName("data")
    var data: List<ApprovedParticipantData>? = null
}

// ======================== 评分模块 ========================

data class SubmitRatingRequest(
    @SerializedName("clubId") val clubId: String,
    @SerializedName("rating") val rating: String        // 后端评分字段为 String 类型
)

// 用户评分记录（对应后端 UserRatingRecordDTO）
data class UserRatingData(
    @SerializedName("ratingId") val ratingId: String,
    @SerializedName("clubId") val clubId: String,
    @SerializedName("clubName") val clubName: String,
    @SerializedName("rating") val rating: String? = null,
    @SerializedName("ratingTime") val ratingTime: String? = null
)

class UserRatingListResponse : ApiResult() {
    @SerializedName("data")
    var data: List<UserRatingData>? = null
}

class UserRatingDetailResponse : ApiResult() {
    @SerializedName("data")
    var data: UserRatingData? = null
}

// 社团平均分（公开接口，对应后端 ClubAverageScoreDTO）
data class ClubAverageScoreData(
    @SerializedName("clubId") val clubId: String,
    @SerializedName("clubName") val clubName: String,
    @SerializedName("averageScore") val averageScore: Double? = null,
    @SerializedName("ratingCount") val ratingCount: Int? = null
)

class ClubAverageScoreListResponse : ApiResult() {
    @SerializedName("data")
    var data: List<ClubAverageScoreData>? = null
}

// ======================== 管理员模块 ========================

// 管理员查询用户请求（对应后端 AdminUserSearchReqDTO）
data class AdminUserSearchRequest(
    @SerializedName("pageNo") val pageNo: Int = 1,
    @SerializedName("pageSize") val pageSize: Int = 10,
    @SerializedName("search") val search: String? = null
)

// MyBatis-Plus 分页响应
data class PageData(
    @SerializedName("records") val records: List<UserInfoData>,
    @SerializedName("total") val total: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("current") val current: Int,
    @SerializedName("pages") val pages: Int
)

class AdminUserSearchResponse : ApiResult() {
    @SerializedName("data")
    var data: PageData? = null
}

// 管理员修改用户资料请求
data class AdminEditUserRequest(
    @SerializedName("targetUserID") val targetUserID: String,
    @SerializedName("userName") val userName: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("userMailbox") val userMailbox: String,
    @SerializedName("realName") val realName: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("degree") val degree: String,
    @SerializedName("school") val school: String,
    @SerializedName("userPassword") val userPassword: String? = null
)

// 审核社团成立请求
data class AuditClubRequest(
    @SerializedName("clubId") val clubId: String,
    @SerializedName("pass") val pass: Boolean
)

// 设置/取消社团管理员请求
data class SetClubManagerRequest(
    @SerializedName("clubId") val clubId: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("setManager") val setManager: Boolean
)

// 审核活动请求
data class AuditActivityRequest(
    @SerializedName("activityId") val activityId: String,
    @SerializedName("pass") val pass: Boolean
)

package com.clubmgmt.app.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiService {

    // ======================== 用户认证 ========================

    /** 获取图形验证码 */
    @GET("api/user/captcha")
    suspend fun getCaptcha(): Response<CaptchaResponse>

    /** 发送验证码（自动识别手机或邮箱） */
    @POST("api/user/sendCode")
    suspend fun sendCode(@Query("contact") contact: String): Response<ApiStringResponse>

    /** 密码登录 / 验证码登录 */
    @POST("api/user/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiStringResponse>

    /** 用户注册 */
    @POST("api/user/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiStringResponse>

    /** 重置密码 */
    @POST("api/user/resetPassword")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiStringResponse>

    /** 修改密码（已登录状态） */
    @PUT("api/user/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiStringResponse>

    // ======================== 用户信息 ========================

    /** 获取当前登录用户信息 */
    @GET("api/user/info")
    suspend fun getUserInfo(): Response<UserInfoResponse>

    /** 修改个人基本信息 */
    @PUT("api/user/info")
    suspend fun updateUserInfo(@Body request: UpdateUserInfoRequest): Response<ApiStringResponse>

    /** 注销账户 */
    @DELETE("api/user/account")
    suspend fun deleteAccount(): Response<ApiStringResponse>

    // ======================== 社团 ========================

    /** 提交成立社团申请 */
    @POST("api/club/create")
    suspend fun createClub(@Body request: CreateClubRequest): Response<ApiStringResponse>

    /** 提交解散社团申请 */
    @POST("api/club/dissolve")
    suspend fun dissolveClub(@Body request: DissolveClubRequest): Response<ApiStringResponse>

    /** 修改社团信息 */
    @PUT("api/club/update")
    suspend fun updateClub(@Body request: UpdateClubRequest): Response<ApiStringResponse>

    /** 查看所有已成立的社团（支持按名称模糊搜索） */
    @GET("api/club/list")
    suspend fun getClubList(@Query("clubName") clubName: String? = null): Response<ClubListResponse>

    /** 查看单个社团详情 */
    @GET("api/club/detail")
    suspend fun getClubDetail(@Query("clubId") clubId: String): Response<ClubDetailResponse>

    /** 查看当前用户管理的社团 */
    @GET("api/club/managed")
    suspend fun getManagedClubs(): Response<ClubListResponse>

    // ======================== 社团成员 ========================

    /** 提交入社申请 */
    @POST("api/member/join")
    suspend fun applyJoinClub(@Body request: JoinClubRequest): Response<ApiStringResponse>

    /** 查看我的入社申请列表 */
    @GET("api/member/my")
    suspend fun getMyApplications(): Response<ClubMemberApplyListResponse>

    /** 社团管理员查看待审核的入社申请 */
    @GET("api/member/pending")
    suspend fun getPendingApplications(): Response<ClubMemberAuditListResponse>

    /** 社团管理员审核入社申请 */
    @PUT("api/member/audit")
    suspend fun auditMember(@Body request: AuditMemberRequest): Response<ApiStringResponse>

    /** 退出社团 */
    @DELETE("api/member/leave")
    suspend fun leaveClub(@Body request: LeaveClubRequest): Response<ApiStringResponse>

    /** 社团管理员查看成员名单（支持按姓名/学号模糊搜索） */
    @GET("api/member/list")
    suspend fun getClubMembers(
        @Query("clubId") clubId: String? = null,
        @Query("search") search: String? = null
    ): Response<ClubMemberListResponse>

    // ======================== 活动 ========================

    /** 发布活动 */
    @POST("api/activities")
    suspend fun createActivity(@Body request: CreateActivityRequest): Response<ApiStringResponse>

    /** 修改活动信息 */
    @PUT("api/activities")
    suspend fun updateActivity(@Body request: UpdateActivityRequest): Response<ApiStringResponse>

    /** 结束活动 */
    @PUT("api/activities/end")
    suspend fun endActivity(@Query("activityId") activityId: String): Response<ApiStringResponse>

    /** 删除活动 */
    @DELETE("api/activities")
    suspend fun deleteActivity(@Query("activityId") activityId: String): Response<ApiStringResponse>

    /** 查看所有已发布的活动（支持按标题模糊搜索） */
    @GET("api/activities")
    suspend fun getActivities(@Query("title") title: String? = null): Response<ActivityListResponse>

    /** 查看单个活动详情 */
    @GET("api/activities/detail")
    suspend fun getActivityDetail(@Query("activityId") activityId: String): Response<ActivityDetailResponse>

    /** 查看当前用户发布的活动 */
    @GET("api/activities/my")
    suspend fun getMyActivities(): Response<ActivityListResponse>

    // ======================== 活动报名 ========================

    /** 用户报名活动 */
    @POST("api/registration/register")
    suspend fun registerActivity(@Body request: RegisterActivityRequest): Response<ApiStringResponse>

    /** 用户取消报名 */
    @POST("api/registration/cancel")
    suspend fun cancelRegistration(@Body request: CancelRegistrationRequest): Response<ApiStringResponse>

    /** 社团管理员获取待审核报名列表 */
    @GET("api/registration/admin/list")
    suspend fun getPendingRegistrations(): Response<RegistrationAuditListResponse>

    /** 用户查看个人报名列表 */
    @GET("api/registration/my")
    suspend fun getMyRegistrations(): Response<UserRegistrationListResponse>

    /** 社团管理员审核报名 */
    @PUT("api/registration/admin/audit")
    suspend fun auditRegistration(@Body request: AuditRegistrationRequest): Response<ApiStringResponse>

    /** 社团管理员查看已通过报名的用户列表 */
    @GET("api/registration/admin/participants")
    suspend fun getApprovedParticipants(
        @Query("activityID") activityID: String
    ): Response<ApprovedParticipantListResponse>

    // ======================== 评分 ========================

    /** 用户提交/修改社团评分 */
    @POST("api/rating/submit")
    suspend fun submitRating(@Body request: SubmitRatingRequest): Response<ApiStringResponse>

    /** 用户查看个人评分记录 */
    @GET("api/rating/my/list")
    suspend fun getMyRatings(): Response<UserRatingListResponse>

    /** 用户查看对特定社团的评分详情 */
    @GET("api/rating/my/detail")
    suspend fun getMyRatingForClub(@Query("clubID") clubID: String): Response<UserRatingDetailResponse>

    /** 用户取消对社团的评分 */
    @POST("api/rating/cancel")
    suspend fun cancelRating(@Query("clubID") clubID: String): Response<ApiStringResponse>

    /** 获取所有社团的平均分和评价人数（公开接口） */
    @GET("api/rating/public/average-scores")
    suspend fun getAverageScores(): Response<ClubAverageScoreListResponse>

    // ======================== 管理员 ========================

    /** 管理员登录 */
    @POST("api/admin/login")
    suspend fun adminLogin(@Body request: AdminLoginRequest): Response<ApiStringResponse>

    /** 管理员多条件分页查询用户 */
    @POST("api/admin/users/search")
    suspend fun adminSearchUsers(@Body request: AdminUserSearchRequest): Response<AdminUserSearchResponse>

    /** 系统管理员修改用户资料 */
    @PUT("api/admin/user/profile")
    suspend fun adminUpdateUser(@Body request: AdminEditUserRequest): Response<ApiStringResponse>

    /** 查看待审核的社团列表 */
    @GET("api/admin/clubs/pending")
    suspend fun getPendingClubs(): Response<ClubListResponse>

    /** 审核成立社团申请 */
    @POST("api/admin/club/audit")
    suspend fun auditClub(@Body request: AuditClubRequest): Response<ApiStringResponse>

    /** 系统管理员查看所有已成立社团的成员名单 */
    @GET("api/admin/clubs/members")
    suspend fun getAllClubMembers(@Query("search") search: String? = null): Response<ClubMemberListResponse>

    /** 系统管理员设置/取消社团管理员 */
    @PUT("api/admin/club/manager")
    suspend fun setClubManager(@Body request: SetClubManagerRequest): Response<ApiStringResponse>

    /** 系统管理员查看待审核的活动列表 */
    @GET("api/admin/activities/pending")
    suspend fun getPendingActivities(): Response<ActivityListResponse>

    /** 系统管理员审核活动 */
    @POST("api/admin/activity/audit")
    suspend fun auditActivity(@Body request: AuditActivityRequest): Response<ApiStringResponse>
}

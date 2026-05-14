package com.clubmgmt.app.data

/**
 * 领域模型 — 与后端实体对齐
 * 所有 ID 均为 String 类型
 */

// 社团（对应后端 Club 实体）
data class Club(
    val clubId: String,
    val userId: String,
    val clubName: String,
    val clubInformation: String,
    val school: String = "",
    val clubState: String = "",      // pending / approved / rejected
    val establishmentTime: String = ""
)

// 活动（对应后端 ActivityListRespDTO）
data class Activity(
    val activityId: String,
    val clubId: String,
    val clubName: String,
    val userId: String,
    val title: String,
    val content: String,
    val location: String,
    val capacityLimit: Int = 0,
    val activityState: String = "",  // pending / approved / rejected / ended
    val publishTime: String = ""
)

// 社团成员（对应后端 ClubMemberListRespDTO）
data class ClubMember(
    val userId: String,
    val userName: String,
    val realName: String,
    val school: String = "",
    val degree: String = "",
    val phoneNumber: String = "",
    val clubId: String,
    val clubName: String,
    val clubManager: String = "",    // "1" 是管理员
    val reviewState: String = ""
)

// 入社申请（对应后端 ClubMemberApplyRespDTO）
data class ClubApply(
    val clubId: String,
    val clubName: String,
    val reviewState: String = ""
)

// 活动报名（对应后端 UserRegistrationDetailDTO）
data class Registration(
    val registrationId: String,
    val activityId: String,
    val title: String,
    val content: String = "",
    val reviewState: String = "",
    val publishTime: String = "",
    val clubId: String = ""
)

// 评分记录（对应后端 UserRatingRecordDTO）
data class UserRating(
    val ratingId: String,
    val clubId: String,
    val clubName: String,
    val rating: String,              // 后端用字符串存储评分
    val ratingTime: String = ""
)

// 社团平均分（对应后端 ClubAverageScoreDTO）
data class ClubAverageScore(
    val clubId: String,
    val clubName: String,
    val averageScore: Double = 0.0,
    val ratingCount: Int = 0
)

// 用户信息（对应后端 UserInfoRespDTO）
data class UserInfo(
    val userId: String,
    val userName: String,
    val phoneNumber: String,
    val userMailbox: String,
    val realName: String,
    val gender: String,
    val degree: String,
    val school: String,
    val registerTime: String = ""
)

enum class UserRole { USER, ADMIN }

// ======================== API 模型 → 领域模型 映射 ========================
import com.clubmgmt.app.data.api.ClubData
import com.clubmgmt.app.data.api.ActivityData

fun ClubData.toClub() = Club(
    clubId = clubId,
    userId = userId,
    clubName = clubName,
    clubInformation = clubInformation,
    school = school ?: "",
    clubState = clubState ?: "",
    establishmentTime = establishmentTime ?: ""
)

fun ActivityData.toActivity() = Activity(
    activityId = activityId,
    clubId = clubId,
    clubName = clubName,
    userId = userId,
    title = title,
    content = content,
    location = location,
    capacityLimit = capacityLimit ?: 0,
    activityState = activityState,
    publishTime = publishTime ?: ""
)

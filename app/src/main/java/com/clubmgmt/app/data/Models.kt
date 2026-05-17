package com.clubmgmt.app.data

import com.clubmgmt.app.data.api.ActivityData
import com.clubmgmt.app.data.api.ClubData

/**
 * 领域模型 — 与后端实体对齐
 * 所有 ID 均为 String 类型
 */

// 社团（对应后端 Club 实体）
data class Club(
    val clubId: String,
    val userId: String,
    val clubName: String,
    val clubInformation: String? = null,
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
    val content: String? = null,
    val location: String? = null,
    val capacityLimit: Int = 0,
    val activityState: String? = null,  // pending / approved / rejected / ended
    val publishTime: String = ""
)

// 社团成员（对应后端 ClubMemberListRespDTO）
data class ClubMember(
    val userId: String,
    val userName: String,
    val realName: String? = null,
    val school: String = "",
    val degree: String = "",
    val phoneNumber: String = "",
    val clubId: String,
    val clubName: String,
    val clubManager: String = "",    // "1" 是管理员
    val reviewState: String? = null
)

// 入社申请（对应后端 ClubMemberApplyRespDTO）
data class ClubApply(
    val clubId: String,
    val clubName: String,
    val reviewState: String? = null
)

// 活动报名（对应后端 UserRegistrationDetailDTO）
data class Registration(
    val registrationId: String,
    val activityId: String,
    val title: String,
    val content: String = "",
    val reviewState: String? = null,
    val publishTime: String = "",
    val clubId: String = ""
)

// 评分记录（对应后端 UserRatingRecordDTO）
data class UserRating(
    val ratingId: String,
    val clubId: String,
    val clubName: String,
    val rating: String? = null,      // 后端用字符串存储评分
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

package com.clubmgmt.app.data

data class ClubMember(
    val id: Int,
    val name: String,
    val avatarUrl: String
)

data class Club(
    val id: Int,
    val name: String,
    val description: String,
    val memberCount: Int,
    val category: String,
    val imageUrl: String,
    val rating: Rating,
    val members: List<ClubMember>,
    val joinRequests: List<ClubMember>,
    val status: ClubStatus
)

data class Rating(
    val score: Float,
    val count: Int
)

enum class ClubStatus { PENDING, APPROVED }

data class Activity(
    val id: Int,
    val title: String,
    val club: String,
    val clubId: Int,
    val date: String,
    val time: String,
    val location: String,
    val description: String,
    val imageUrl: String,
    val status: ActivityStatus
)

enum class ActivityStatus { PENDING, APPROVED }

data class UserRating(
    val clubId: Int,
    val score: Int
)

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val avatarUrl: String,
    val joinedClubs: List<Int>,
    val registeredActivities: List<Int>,
    val managedClubs: List<Int>,
    val ratings: List<UserRating>,
    val role: UserRole
)

enum class UserRole { USER, ADMIN }

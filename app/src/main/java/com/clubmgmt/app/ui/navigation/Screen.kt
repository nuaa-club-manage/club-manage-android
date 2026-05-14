package com.clubmgmt.app.ui.navigation

sealed class Screen(val route: String) {
    // Main tabs
    data object Home : Screen("home")
    data object Clubs : Screen("clubs")
    data object Activities : Screen("activities")
    data object Profile : Screen("profile")

    // Detail screens
    data object ClubDetail : Screen("clubs/{clubId}") {
        fun createRoute(clubId: String) = "clubs/$clubId"
    }
    data object ActivityDetail : Screen("activities/{activityId}") {
        fun createRoute(activityId: String) = "activities/$activityId"
    }

    // Form screens
    data object CreateClub : Screen("clubs/create")
    data object PublishActivity : Screen("activities/publish")
    data object EditProfile : Screen("profile/edit")
    data object ManageClub : Screen("clubs/{clubId}/manage") {
        fun createRoute(clubId: String) = "clubs/$clubId/manage"
    }

    // Auth screens
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ForgotPassword : Screen("forgot-password")

    // Admin screens
    data object AdminDashboard : Screen("admin/dashboard")
    data object AdminUsers : Screen("admin/users")
    data object AdminUserDetail : Screen("admin/users/{userId}") {
        fun createRoute(userId: String) = "admin/users/$userId"
    }
    data object AdminClubApproval : Screen("admin/clubs")
    data object AdminActivityApproval : Screen("admin/activities")
}

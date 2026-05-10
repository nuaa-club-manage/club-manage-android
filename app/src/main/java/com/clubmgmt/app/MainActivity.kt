package com.clubmgmt.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.clubmgmt.app.data.SessionManager
import com.clubmgmt.app.ui.navigation.Screen
import com.clubmgmt.app.ui.screens.*
import com.clubmgmt.app.ui.theme.ClubManagementTheme
import com.clubmgmt.app.ui.theme.Indigo600

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionManager.init(applicationContext)
        setContent {
            ClubManagementTheme {
                MainApp()
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem("首页", Icons.Filled.Home, Screen.Home.route),
        BottomNavItem("社团", Icons.Filled.Groups, Screen.Clubs.route),
        BottomNavItem("活动", Icons.Filled.CalendarToday, Screen.Activities.route),
        BottomNavItem("我的", Icons.Filled.Person, Screen.Profile.route)
    )

    val bottomNavRoutes = bottomNavItems.map { it.route }
    val showBottomBar = currentRoute in bottomNavRoutes
    val showAuthFlow = currentRoute in listOf(
        Screen.Login.route,
        Screen.Register.route,
        Screen.ForgotPassword.route
    )
    val isAdminRoute = currentRoute?.startsWith("admin") == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = Screen.Login.route
            ) {
                // Auth
                composable(Screen.Login.route) {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onNavigateRegister = {
                            navController.navigate(Screen.Register.route)
                        },
                        onNavigateForgotPassword = {
                            navController.navigate(Screen.ForgotPassword.route)
                        },
                        onNavigateAdmin = {
                            navController.navigate(Screen.AdminDashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Screen.Register.route) {
                    RegisterScreen(
                        onRegisterSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onNavigateLogin = {
                            navController.popBackStack()
                        }
                    )
                }
                composable(Screen.ForgotPassword.route) {
                    ForgotPasswordScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                // Main tabs
                composable(Screen.Home.route) {
                    HomeScreen(
                        onClubClick = { clubId ->
                            navController.navigate(Screen.ClubDetail.createRoute(clubId))
                        },
                        onActivityClick = { activityId ->
                            navController.navigate(Screen.ActivityDetail.createRoute(activityId))
                        },
                        onViewAllClubs = {
                            navController.navigate(Screen.Clubs.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onViewAllActivities = {
                            navController.navigate(Screen.Activities.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(Screen.Clubs.route) {
                    ClubListScreen(
                        onClubClick = { clubId ->
                            navController.navigate(Screen.ClubDetail.createRoute(clubId))
                        }
                    )
                }
                composable(Screen.Activities.route) {
                    ActivityListScreen(
                        onActivityClick = { activityId ->
                            navController.navigate(Screen.ActivityDetail.createRoute(activityId))
                        }
                    )
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onEditProfile = {
                            navController.navigate(Screen.EditProfile.route)
                        },
                        onCreateClub = {
                            navController.navigate(Screen.CreateClub.route)
                        },
                        onPublishActivity = {
                            navController.navigate(Screen.PublishActivity.route)
                        },
                        onClubClick = { clubId ->
                            navController.navigate(Screen.ClubDetail.createRoute(clubId))
                        },
                        onManageClub = { clubId ->
                            navController.navigate(Screen.ManageClub.createRoute(clubId))
                        },
                        onActivityClick = { activityId ->
                            navController.navigate(Screen.ActivityDetail.createRoute(activityId))
                        },
                        onEnterAdmin = {
                            navController.navigate(Screen.AdminDashboard.route)
                        }
                    )
                }

                // Detail screens
                composable(
                    route = Screen.ClubDetail.route,
                    arguments = listOf(navArgument("clubId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val clubId = backStackEntry.arguments?.getInt("clubId") ?: 0
                    ClubDetailScreen(
                        clubId = clubId,
                        onBack = { navController.popBackStack() },
                        onManageClub = { id ->
                            navController.navigate(Screen.ManageClub.createRoute(id))
                        }
                    )
                }
                composable(
                    route = Screen.ActivityDetail.route,
                    arguments = listOf(navArgument("activityId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val activityId = backStackEntry.arguments?.getInt("activityId") ?: 0
                    ActivityDetailScreen(
                        activityId = activityId,
                        onBack = { navController.popBackStack() },
                        onClubClick = { clubId ->
                            navController.navigate(Screen.ClubDetail.createRoute(clubId))
                        }
                    )
                }

                // Form screens
                composable(Screen.CreateClub.route) {
                    CreateClubScreen(onBack = { navController.popBackStack() })
                }
                composable(Screen.PublishActivity.route) {
                    PublishActivityScreen(onBack = { navController.popBackStack() })
                }
                composable(Screen.EditProfile.route) {
                    EditProfileScreen(onBack = { navController.popBackStack() })
                }
                composable(
                    route = Screen.ManageClub.route,
                    arguments = listOf(navArgument("clubId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val clubId = backStackEntry.arguments?.getInt("clubId") ?: 0
                    ManageClubScreen(
                        clubId = clubId,
                        onBack = { navController.popBackStack() }
                    )
                }

                // Admin screens
                composable(Screen.AdminDashboard.route) {
                    AdminScreenWrapper(
                        navController = navController,
                        currentRoute = Screen.AdminDashboard.route
                    ) {
                        AdminDashboardScreen(
                            onNavigateUsers = {
                                navController.navigate(Screen.AdminUsers.route)
                            },
                            onNavigateClubs = {
                                navController.navigate(Screen.AdminClubApproval.route)
                            },
                            onNavigateActivities = {
                                navController.navigate(Screen.AdminActivityApproval.route)
                            }
                        )
                    }
                }
                composable(Screen.AdminUsers.route) {
                    AdminScreenWrapper(
                        navController = navController,
                        currentRoute = Screen.AdminUsers.route
                    ) {
                        AdminUserManagementScreen()
                    }
                }
                composable(Screen.AdminClubApproval.route) {
                    AdminScreenWrapper(
                        navController = navController,
                        currentRoute = Screen.AdminClubApproval.route
                    ) {
                        AdminClubApprovalScreen()
                    }
                }
                composable(Screen.AdminActivityApproval.route) {
                    AdminScreenWrapper(
                        navController = navController,
                        currentRoute = Screen.AdminActivityApproval.route
                    ) {
                        AdminActivityApprovalScreen()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreenWrapper(
    navController: NavHostController,
    currentRoute: String,
    content: @Composable () -> Unit
) {
    val adminNavItems = listOf(
        Triple("仪表盘", Icons.Filled.Dashboard, Screen.AdminDashboard.route),
        Triple("用户管理", Icons.Filled.People, Screen.AdminUsers.route),
        Triple("社团审核", Icons.Filled.Groups, Screen.AdminClubApproval.route),
        Triple("活动审核", Icons.Filled.CalendarToday, Screen.AdminActivityApproval.route)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("管理后台", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                    }
                }) {
                    Text("← 返回", color = Indigo600, fontSize = 14.sp)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        ScrollableTabRow(
            selectedTabIndex = adminNavItems.indexOfFirst { it.third == currentRoute }
                .coerceAtLeast(0),
            edgePadding = 16.dp
        ) {
            adminNavItems.forEach { (label, icon, route) ->
                Tab(
                    selected = currentRoute == route,
                    onClick = {
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(Screen.AdminDashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    text = { Text(label) },
                    icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

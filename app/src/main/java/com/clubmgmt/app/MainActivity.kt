package com.clubmgmt.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import kotlinx.coroutines.launch

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
                            navController.navigate(Screen.AdminDashboard.route) {
                                popUpTo(Screen.Profile.route) { inclusive = true }
                            }
                        },
                        onLogout = {
                            com.clubmgmt.app.data.SessionManager.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                // Detail screens
                composable(
                    route = Screen.ClubDetail.route,
                    arguments = listOf(navArgument("clubId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val clubId = backStackEntry.arguments?.getString("clubId") ?: ""
                    ClubDetailScreen(
                        clubId = clubId,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = Screen.ActivityDetail.route,
                    arguments = listOf(navArgument("activityId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val activityId = backStackEntry.arguments?.getString("activityId") ?: ""
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
                    arguments = listOf(navArgument("clubId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val clubId = backStackEntry.arguments?.getString("clubId") ?: ""
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
                        AdminUserManagementScreen(
                            onUserClick = { userId ->
                                navController.navigate(Screen.AdminUserDetail.createRoute(userId))
                            }
                        )
                    }
                }
                composable(
                    route = Screen.AdminUserDetail.route,
                    arguments = listOf(navArgument("userId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: ""
                    AdminScreenWrapper(
                        navController = navController,
                        currentRoute = Screen.AdminUserDetail.route
                    ) {
                        AdminUserDetailScreen(
                            userId = userId,
                            onBack = { navController.popBackStack() }
                        )
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
                composable(Screen.AdminClubQuery.route) {
                    AdminScreenWrapper(
                        navController = navController,
                        currentRoute = Screen.AdminClubQuery.route
                    ) {
                        AdminClubQueryScreen(
                            onClubClick = { clubId ->
                                navController.navigate(Screen.AdminClubDetail.createRoute(clubId))
                            }
                        )
                    }
                }
                composable(
                    route = Screen.AdminClubDetail.route,
                    arguments = listOf(navArgument("clubId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val clubId = backStackEntry.arguments?.getString("clubId") ?: ""
                    AdminScreenWrapper(
                        navController = navController,
                        currentRoute = Screen.AdminClubDetail.route
                    ) {
                        AdminClubDetailScreen(
                            clubId = clubId,
                            onBack = { navController.popBackStack() }
                        )
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

data class AdminNavItem(val label: String, val icon: ImageVector, val route: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreenWrapper(
    navController: NavHostController,
    currentRoute: String,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val adminNavItems = listOf(
        AdminNavItem("菜单", Icons.Filled.Dashboard, Screen.AdminDashboard.route),
        AdminNavItem("用户管理", Icons.Filled.People, Screen.AdminUsers.route),
        AdminNavItem("社团审核", Icons.Filled.Groups, Screen.AdminClubApproval.route),
        AdminNavItem("社团查询", Icons.Filled.Search, Screen.AdminClubQuery.route),
        AdminNavItem("活动审核", Icons.Filled.CalendarToday, Screen.AdminActivityApproval.route)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "管理后台",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                adminNavItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, null, modifier = Modifier.size(22.dp)) },
                        label = { Text(item.label, fontWeight = FontWeight.Medium) },
                        selected = currentRoute == item.route || (item.route == Screen.AdminUsers.route && currentRoute.startsWith("admin/users/")) || (item.route == Screen.AdminClubQuery.route && currentRoute != null && currentRoute.startsWith("admin/clubs/") && currentRoute != Screen.AdminClubApproval.route),
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.AdminDashboard.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("管理后台", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "菜单")
                        }
                    },
                    actions = {},
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                content()
            }
        }
    }
}

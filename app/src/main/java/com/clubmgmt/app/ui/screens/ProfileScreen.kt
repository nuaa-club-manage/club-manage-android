package com.clubmgmt.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.clubmgmt.app.data.SessionManager
import com.clubmgmt.app.data.api.ActivityData
import com.clubmgmt.app.data.api.ClubData
import com.clubmgmt.app.data.api.ClubMemberApplyData
import com.clubmgmt.app.data.api.RetrofitClient
import com.clubmgmt.app.data.api.UserInfoData
import com.clubmgmt.app.data.api.UserRatingData
import com.clubmgmt.app.data.api.CancelRegistrationRequest
import com.clubmgmt.app.data.api.UserRegistrationData
import com.clubmgmt.app.ui.theme.Indigo600
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onCreateClub: () -> Unit,
    onPublishActivity: () -> Unit,
    onClubClick: (String) -> Unit,
    onManageClub: (String) -> Unit,
    onActivityClick: (String) -> Unit,
    onEnterAdmin: () -> Unit,
    onLogout: () -> Unit = {}
) {
    var activeTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("我加入的社团", "我管理的社团", "我报名的活动", "我发布的活动", "我的评分", "我的申请")
    var userInfo by remember { mutableStateOf<UserInfoData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isAdmin by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Tab data states
    var myApplications by remember { mutableStateOf<List<ClubMemberApplyData>?>(null) }
    var isLoadingApps by remember { mutableStateOf(false) }
    var managedClubs by remember { mutableStateOf<List<ClubData>?>(null) }
    var isLoadingManaged by remember { mutableStateOf(false) }
    var myRegistrations by remember { mutableStateOf<List<UserRegistrationData>?>(null) }
    var isLoadingRegs by remember { mutableStateOf(false) }
    var myActivities by remember { mutableStateOf<List<ActivityData>?>(null) }
    var isLoadingMyActs by remember { mutableStateOf(false) }

    // 我的社团创建申请
    var myClubCreations by remember { mutableStateOf<List<ClubData>?>(null) }
    var isLoadingClubCreations by remember { mutableStateOf(false) }

    // New: 我的评分
    var myRatings by remember { mutableStateOf<List<UserRatingData>?>(null) }
    var isLoadingRatings by remember { mutableStateOf(false) }

    // New: 我的申请 子Tab
    val appSubTabs = listOf("成立社团申请", "发布活动申请", "加入社团申请", "报名活动申请")
    var appSubTab by remember { mutableIntStateOf(0) }

    fun loadUserInfo() {
        scope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.getUserInfo()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.code == 200) {
                        userInfo = body.data
                        isAdmin = SessionManager.userRole == "ADMIN"
                    } else {
                        snackbarHostState.showSnackbar(body?.message ?: "获取信息失败")
                    }
                } else {
                    snackbarHostState.showSnackbar("获取信息失败 (${response.code()})")
                }
            } catch (_: Exception) {
                snackbarHostState.showSnackbar("网络错误，请稍后重试")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadUserInfo() }

    // Load tab data when switching tabs
    LaunchedEffect(activeTab) {
        when (activeTab) {
            0 -> {
                if (myApplications == null) {
                    isLoadingApps = true
                    try {
                        val resp = RetrofitClient.instance.getMyApplications()
                        if (resp.isSuccessful) {
                            val body = resp.body()
                            if (body != null && body.code == 200) {
                                myApplications = body.data ?: emptyList()
                            }
                        }
                    } catch (_: Exception) {
                        snackbarHostState.showSnackbar("网络错误")
                    } finally {
                        isLoadingApps = false
                    }
                }
            }
            1 -> {
                if (managedClubs == null) {
                    isLoadingManaged = true
                    try {
                        val resp = RetrofitClient.instance.getManagedClubs()
                        if (resp.isSuccessful) {
                            val body = resp.body()
                            if (body != null && body.code == 200) {
                                managedClubs = body.data ?: emptyList()
                            }
                        }
                    } catch (_: Exception) {
                        snackbarHostState.showSnackbar("网络错误")
                    } finally {
                        isLoadingManaged = false
                    }
                }
            }
            2 -> {
                if (myRegistrations == null) {
                    isLoadingRegs = true
                    try {
                        val resp = RetrofitClient.instance.getMyRegistrations()
                        if (resp.isSuccessful) {
                            val body = resp.body()
                            if (body != null && body.code == 200) {
                                myRegistrations = body.data ?: emptyList()
                            }
                        }
                    } catch (_: Exception) {
                        snackbarHostState.showSnackbar("网络错误")
                    } finally {
                        isLoadingRegs = false
                    }
                }
            }
            3 -> {
                if (myActivities == null) {
                    isLoadingMyActs = true
                    try {
                        val resp = RetrofitClient.instance.getMyActivities()
                        if (resp.isSuccessful) {
                            val body = resp.body()
                            if (body != null && body.code == 200) {
                                myActivities = body.data ?: emptyList()
                            }
                        }
                    } catch (_: Exception) {
                        snackbarHostState.showSnackbar("网络错误")
                    } finally {
                        isLoadingMyActs = false
                    }
                }
            }
            4 -> {
                if (myRatings == null) {
                    isLoadingRatings = true
                    try {
                        val resp = RetrofitClient.instance.getMyRatings()
                        if (resp.isSuccessful) {
                            val body = resp.body()
                            if (body != null && body.code == 200) {
                                myRatings = body.data ?: emptyList()
                            }
                        }
                    } catch (_: Exception) {
                        snackbarHostState.showSnackbar("网络错误")
                    } finally {
                        isLoadingRatings = false
                    }
                }
            }
            5 -> {
                // 我的申请：确保依赖的数据已加载
                if (myClubCreations == null) {
                    isLoadingClubCreations = true
                    try {
                        val resp = RetrofitClient.instance.getMyClubCreations()
                        if (resp.isSuccessful) {
                            val body = resp.body()
                            if (body != null && body.code == 200) {
                                myClubCreations = body.data ?: emptyList()
                            }
                        }
                    } catch (_: Exception) { } finally {
                        isLoadingClubCreations = false
                    }
                }
                if (myApplications == null) {
                    isLoadingApps = true
                    try {
                        val resp = RetrofitClient.instance.getMyApplications()
                        if (resp.isSuccessful) {
                            val body = resp.body()
                            if (body != null && body.code == 200) {
                                myApplications = body.data ?: emptyList()
                            }
                        }
                    } catch (_: Exception) { } finally {
                        isLoadingApps = false
                    }
                }
                if (myRegistrations == null) {
                    isLoadingRegs = true
                    try {
                        val resp = RetrofitClient.instance.getMyRegistrations()
                        if (resp.isSuccessful) {
                            val body = resp.body()
                            if (body != null && body.code == 200) {
                                myRegistrations = body.data ?: emptyList()
                            }
                        }
                    } catch (_: Exception) { } finally {
                        isLoadingRegs = false
                    }
                }
                if (myActivities == null) {
                    isLoadingMyActs = true
                    try {
                        val resp = RetrofitClient.instance.getMyActivities()
                        if (resp.isSuccessful) {
                            val body = resp.body()
                            if (body != null && body.code == 200) {
                                myActivities = body.data ?: emptyList()
                            }
                        }
                    } catch (_: Exception) { } finally {
                        isLoadingMyActs = false
                    }
                }
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    val data = userInfo
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (data != null) {
                                AvatarPlaceholder(
                                    name = data.userName,
                                    modifier = Modifier.size(80.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(data.userName, style = MaterialTheme.typography.headlineMedium)
                                Text(
                                    data.userMailbox ?: data.phoneNumber ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            } else {
                                AvatarPlaceholder(
                                    name = "?",
                                    modifier = Modifier.size(80.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text("未登录", style = MaterialTheme.typography.headlineMedium)
                            }
                            Spacer(Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onEditProfile,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) { Text("编辑资料") }
                                Button(
                                    onClick = onCreateClub,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                ) {
                                    Icon(Icons.Filled.AddCircle, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("成立社团")
                                }
                            }

                            Button(
                                onClick = onPublishActivity,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Filled.AddCircle, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("发布活动")
                            }

                            if (isAdmin) {
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = onEnterAdmin,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.Settings, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("管理后台")
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            TextButton(
                                onClick = onLogout,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("退出登录", color = Color(0xFFEF4444))
                            }
                        }
                    }
                }

                item {
                    ScrollableTabRow(selectedTabIndex = activeTab, edgePadding = 0.dp) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = activeTab == index,
                                onClick = { activeTab = index },
                                text = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            )
                        }
                    }
                }

                when (activeTab) {
                    0 -> { // 我加入的社团（已通过）
                        val approvedApps = myApplications?.filter {
                            it.reviewState == "通过"
                        }
                        if (isLoadingApps) {
                            item {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(Modifier.size(32.dp))
                                }
                            }
                        } else if (approvedApps.isNullOrEmpty()) {
                            item {
                                Text(
                                    "暂无已加入的社团",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            items(approvedApps) { app ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onClubClick(app.clubId) },
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                app.clubName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            StatusBadge(app.reviewState.orEmpty())
                                        }
                                        Icon(
                                            Icons.Filled.ChevronRight, null,
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    1 -> { // 我管理的社团（不变）
                        if (isLoadingManaged) {
                            item {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(Modifier.size(32.dp))
                                }
                            }
                        } else if (managedClubs.isNullOrEmpty()) {
                            item {
                                Text(
                                    "您尚未管理任何社团",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            items(managedClubs!!, key = { it.clubId }) { club ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onManageClub(club.clubId) },
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(club.clubName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Spacer(Modifier.height(4.dp))
                                            StatusBadge(club.clubState.orEmpty())
                                        }
                                        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                                    }
                                }
                            }
                        }
                    }

                    2 -> { // 我报名的活动（已通过）
                        val approvedRegs = myRegistrations?.filter {
                            it.reviewState == "审核通过"
                        }
                        if (isLoadingRegs) {
                            item {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(Modifier.size(32.dp))
                                }
                            }
                        } else if (approvedRegs.isNullOrEmpty()) {
                            item {
                                Text(
                                    "暂无已报名的活动",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            items(approvedRegs) { reg ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onActivityClick(reg.activityId) },
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    reg.title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                ActivityStateBadge(reg.activityState.orEmpty())
                                                StatusBadge(reg.reviewState.orEmpty())
                                            }
                                            if (reg.clubName != null) {
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    "社团: ${reg.clubName}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                )
                                            }
                                        }
                                        if (reg.activityState != "已结束") {
                                            OutlinedButton(
                                                onClick = {
                                                    scope.launch {
                                                        try {
                                                            val resp = RetrofitClient.instance.cancelRegistration(
                                                                CancelRegistrationRequest(registrationID = reg.registrationId ?: "")
                                                            )
                                                            if (resp.isSuccessful) {
                                                                myRegistrations = myRegistrations?.filter { it.registrationId != reg.registrationId }
                                                                snackbarHostState.showSnackbar("已取消报名")
                                                            } else {
                                                                snackbarHostState.showSnackbar("取消失败")
                                                            }
                                                        } catch (_: Exception) {
                                                            snackbarHostState.showSnackbar("网络错误")
                                                        }
                                                    }
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                                            ) {
                                                Text("取消报名")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> { // 我发布的活动（已发布/已结束）
                        val publishedActs = myActivities?.filter {
                            it.activityState == "已发布" || it.activityState == "已结束"
                        }
                        if (isLoadingMyActs) {
                            item {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(Modifier.size(32.dp))
                                }
                            }
                        } else if (publishedActs.isNullOrEmpty()) {
                            item {
                                Text(
                                    "暂未发布任何活动",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            items(publishedActs, key = { it.activityId }) { act ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onActivityClick(act.activityId) },
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(act.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(4.dp))
                                        StatusBadge(act.activityState.orEmpty())
                                    }
                                }
                            }
                        }
                    }

                    4 -> { // 我的评分
                        if (isLoadingRatings) {
                            item {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(Modifier.size(32.dp))
                                }
                            }
                        } else if (myRatings.isNullOrEmpty()) {
                            item {
                                Text(
                                    "暂未对任何社团进行评分",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            items(myRatings!!, key = { it.ratingId }) { rating ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onClubClick(rating.clubId) },
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                rating.clubName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (rating.ratingTime != null) {
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    "评分时间: ${rating.ratingTime}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                )
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Indigo600.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                "评分: ${rating.rating ?: "0"}",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                color = Indigo600,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            Icons.Filled.ChevronRight, null,
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    5 -> { // 我的申请（待审核/已拒绝）
                        // 子Tab选择器
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                appSubTabs.forEachIndexed { index, title ->
                                    FilterChip(
                                        selected = appSubTab == index,
                                        onClick = { appSubTab = index },
                                        label = { Text(title, maxLines = 1) },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }

                        when (appSubTab) {
                            0 -> { // 成立社团申请
                                val pendingCreations = myClubCreations?.filter {
                                    it.clubState == "待审核" || it.clubState == "未通过"
                                }
                                if (pendingCreations.isNullOrEmpty()) {
                                    item {
                                        Text(
                                            "暂无成立社团的申请记录",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    items(pendingCreations, key = { it.clubId }) { club ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onClubClick(club.clubId) },
                                            shape = RoundedCornerShape(12.dp),
                                            elevation = CardDefaults.cardElevation(1.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(Modifier.weight(1f)) {
                                                    Text(
                                                        club.clubName,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(Modifier.height(4.dp))
                                                    StatusBadge(club.clubState.orEmpty())
                                                }
                                                Icon(
                                                    Icons.Filled.ChevronRight, null,
                                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> { // 发布活动申请
                                val pendingActs = myActivities?.filter {
                                    it.activityState == "待审核" || it.activityState == "未通过"
                                }
                                if (pendingActs.isNullOrEmpty()) {
                                    item {
                                        Text(
                                            "暂无发布活动的申请记录",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    items(pendingActs, key = { it.activityId }) { act ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onActivityClick(act.activityId) },
                                            shape = RoundedCornerShape(12.dp),
                                            elevation = CardDefaults.cardElevation(1.dp)
                                        ) {
                                            Column(Modifier.padding(16.dp)) {
                                                Text(act.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                Spacer(Modifier.height(4.dp))
                                                StatusBadge(act.activityState.orEmpty())
                                            }
                                        }
                                    }
                                }
                            }
                            2 -> { // 加入社团申请
                                val pendingApps = myApplications?.filter {
                                    it.reviewState == "待审核" || it.reviewState == "未通过"
                                }
                                if (pendingApps.isNullOrEmpty()) {
                                    item {
                                        Text(
                                            "暂无入社申请记录",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    items(pendingApps) { app ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onClubClick(app.clubId) },
                                            shape = RoundedCornerShape(12.dp),
                                            elevation = CardDefaults.cardElevation(1.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(Modifier.weight(1f)) {
                                                    Text(
                                                        app.clubName,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(Modifier.height(4.dp))
                                                    StatusBadge(app.reviewState.orEmpty())
                                                }
                                                Icon(
                                                    Icons.Filled.ChevronRight, null,
                                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            3 -> { // 报名活动申请
                                val pendingRegs = myRegistrations?.filter {
                                    it.reviewState == "审核中" || it.reviewState == "审核失败"
                                }
                                if (pendingRegs.isNullOrEmpty()) {
                                    item {
                                        Text(
                                            "暂无报名活动的申请记录",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    items(pendingRegs) { reg ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onActivityClick(reg.activityId) },
                                            shape = RoundedCornerShape(12.dp),
                                            elevation = CardDefaults.cardElevation(1.dp)
                                        ) {
                                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Column(Modifier.weight(1f)) {
                                                    Text(
                                                        reg.title,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    if (reg.clubName != null) {
                                                        Spacer(Modifier.height(2.dp))
                                                        Text(
                                                            "社团: ${reg.clubName}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                        )
                                                    }
                                                    Spacer(Modifier.height(4.dp))
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        StatusBadge(reg.reviewState.orEmpty())
                                                    }
                                                }
                                                if (reg.activityState != "已结束") {
                                                    OutlinedButton(
                                                        onClick = {
                                                            scope.launch {
                                                                try {
                                                                    val resp = RetrofitClient.instance.cancelRegistration(
                                                                        CancelRegistrationRequest(registrationID = reg.registrationId ?: "")
                                                                    )
                                                                    if (resp.isSuccessful) {
                                                                        myRegistrations = myRegistrations?.filter { it.registrationId != reg.registrationId }
                                                                        snackbarHostState.showSnackbar("已取消报名")
                                                                    } else {
                                                                        snackbarHostState.showSnackbar("取消失败")
                                                                    }
                                                                } catch (_: Exception) {
                                                                    snackbarHostState.showSnackbar("网络错误")
                                                                }
                                                            }
                                                        },
                                                        shape = RoundedCornerShape(8.dp),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                                                    ) {
                                                        Text("取消报名")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 审核状态标签（兼容中英文状态值） */
@Composable
private fun StatusBadge(state: String) {
    val (label, color) = when (state) {
        "approved", "已通过", "已发布", "通过", "审核通过" -> "已通过" to Color(0xFF22C55E)
        "pending", "待审核", "审核中" -> "审核中" to Color(0xFFF59E0B)
        "rejected", "未通过", "审核失败" -> "已拒绝" to Color(0xFFEF4444)
        else -> state to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

/** 活动状态标签 */
@Composable
private fun ActivityStateBadge(state: String) {
    val (label, color) = when (state) {
        "已发布" -> "已发布" to Color(0xFF22C55E)
        "已结束" -> "已结束" to Color(0xFF6B7280)
        "待审核" -> "审核中" to Color(0xFFF59E0B)
        "未通过" -> "已拒绝" to Color(0xFFEF4444)
        else -> state to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

/** 取用户名首字作为头像占位 */
@Composable
private fun AvatarPlaceholder(name: String, modifier: Modifier = Modifier) {
    val initial = name.firstOrNull()?.toString() ?: "?"
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(40.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center
        )
    }
}

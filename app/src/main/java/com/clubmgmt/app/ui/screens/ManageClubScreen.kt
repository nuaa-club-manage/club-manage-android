package com.clubmgmt.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clubmgmt.app.data.api.ActivityData
import com.clubmgmt.app.data.api.ActivityEndRequest
import com.clubmgmt.app.data.api.ApprovedParticipantData
import com.clubmgmt.app.data.api.AuditMemberRequest
import com.clubmgmt.app.data.api.AuditRegistrationRequest
import com.clubmgmt.app.data.api.ClubMemberAuditData
import com.clubmgmt.app.data.api.ClubMemberListData
import com.clubmgmt.app.data.api.DissolveClubRequest
import com.clubmgmt.app.data.api.RegistrationAuditData
import com.clubmgmt.app.data.api.RetrofitClient
import com.clubmgmt.app.data.api.SetClubManagerRequest
import com.clubmgmt.app.data.api.UpdateActivityRequest
import com.clubmgmt.app.data.api.UpdateClubRequest
import com.clubmgmt.app.data.toClub
import com.clubmgmt.app.ui.theme.Indigo600
import com.clubmgmt.app.ui.theme.Green500
import com.clubmgmt.app.ui.theme.Red500
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageClubScreen(
    clubId: String,
    onBack: () -> Unit
) {
    var club by remember { mutableStateOf<com.clubmgmt.app.data.Club?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableIntStateOf(0) }

    // 成员管理数据
    var members by remember { mutableStateOf<List<ClubMemberListData>>(emptyList()) }
    var pendingMembers by remember { mutableStateOf<List<ClubMemberAuditData>>(emptyList()) }
    // 活动管理数据
    var activities by remember { mutableStateOf<List<ActivityData>>(emptyList()) }
    // 报名数据
    var pendingRegistrations by remember { mutableStateOf<List<RegistrationAuditData>>(emptyList()) }
    var participants by remember { mutableStateOf<List<ApprovedParticipantData>>(emptyList()) }
    var selectedActId by remember { mutableStateOf("") }
    // 设置数据
    var clubName by remember { mutableStateOf("") }
    var clubDescription by remember { mutableStateOf("") }

    fun loadData() {
        scope.launch {
            try {
                val clubResp = RetrofitClient.instance.getClubDetail(clubId)
                if (clubResp.isSuccessful) {
                    val body = clubResp.body()
                    if (body != null && body.code == 200) {
                        val c = body.data?.toClub()
                        club = c
                        clubName = c?.clubName ?: ""
                        clubDescription = c?.clubInformation ?: ""
                    }
                }
                val memberResp = RetrofitClient.instance.getClubMembers(clubId = clubId)
                if (memberResp.isSuccessful) members = memberResp.body()?.data ?: emptyList()

                val pendingResp = RetrofitClient.instance.getPendingApplications()
                if (pendingResp.isSuccessful) {
                    pendingMembers = pendingResp.body()?.data?.filter { it.clubId == clubId } ?: emptyList()
                }

                val actResp = RetrofitClient.instance.getActivities()
                if (actResp.isSuccessful) {
                    activities = actResp.body()?.data?.filter { it.clubId == clubId } ?: emptyList()
                }

                val regResp = RetrofitClient.instance.getPendingRegistrations()
                if (regResp.isSuccessful) pendingRegistrations = regResp.body()?.data ?: emptyList()
            } catch (_: Exception) {
                snackbarHostState.showSnackbar("网络错误")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(clubId) { loadData() }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (club == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("未找到社团") }
        return
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                Spacer(Modifier.width(8.dp))
                Text("管理: ${club!!.clubName}", style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(16.dp))

            TabRow(selectedTabIndex = activeTab) {
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) { Text("成员", modifier = Modifier.padding(12.dp)) }
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) { Text("活动", modifier = Modifier.padding(12.dp)) }
                Tab(selected = activeTab == 2, onClick = { activeTab = 2 }) { Text("报名", modifier = Modifier.padding(12.dp)) }
                Tab(selected = activeTab == 3, onClick = { activeTab = 3 }) { Text("设置", modifier = Modifier.padding(12.dp)) }
            }

            when (activeTab) {
                0 -> MembersTab(members, pendingMembers, clubId, snackbarHostState, ::loadData)
                1 -> ActivitiesTab(activities, clubId, snackbarHostState, ::loadData)
                2 -> RegistrationsTab(pendingRegistrations, activities, selectedActId, { selectedActId = it }, participants, snackbarHostState, ::loadData)
                3 -> SettingsTab(club!!, clubName, { clubName = it }, clubDescription, { clubDescription = it }, clubId, snackbarHostState, onBack)
            }
        }
    }
}

// ======================== Tab 0: 成员 ========================

@Composable
private fun MembersTab(
    members: List<ClubMemberListData>,
    pendingMembers: List<ClubMemberAuditData>,
    clubId: String,
    snackbarHostState: SnackbarHostState,
    reload: () -> Unit
) {
    val scope = rememberCoroutineScope()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
        // 待审核入社申请
        if (pendingMembers.isNotEmpty()) {
            item {
                Text("入社申请 (${pendingMembers.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            pendingMembers.forEach { request ->
                item {
                    Card(shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(request.realName.orEmpty().ifBlank { request.userName }, style = MaterialTheme.typography.titleMedium)
                                Text("学号: ${request.userId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = {
                                    scope.launch {
                                        try {
                                            RetrofitClient.instance.auditMember(AuditMemberRequest(clubId, request.userId, false))
                                            reload()
                                        } catch (_: Exception) { }
                                    }
                                }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Red500)) { Text("拒绝") }
                                Button(onClick = {
                                    scope.launch {
                                        try {
                                            RetrofitClient.instance.auditMember(AuditMemberRequest(clubId, request.userId, true))
                                            reload()
                                        } catch (_: Exception) { }
                                    }
                                }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Green500)) { Text("批准") }
                            }
                        }
                    }
                }
            }
        }

        // 成员列表
        item {
            Spacer(Modifier.height(8.dp))
            Text("成员列表 (${members.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (members.isEmpty()) {
            item { Text("暂无成员", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) }
        } else {
            members.forEach { m ->
                item {
                    Card(shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(m.realName.orEmpty().ifBlank { m.userName }, style = MaterialTheme.typography.titleMedium)
                                    if (m.clubManager == "是" || m.clubManager == "1") {
                                        Spacer(Modifier.width(6.dp))
                                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF59E0B).copy(alpha = 0.15f)) {
                                            Text("管理员", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text("学号: ${m.userId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                            if (m.clubManager == "是" || m.clubManager == "1") {
                                OutlinedButton(onClick = {
                                    scope.launch {
                                        try {
                                            RetrofitClient.instance.setClubManager(SetClubManagerRequest(clubId, m.userId, false))
                                            reload()
                                        } catch (_: Exception) { }
                                    }
                                }, shape = RoundedCornerShape(8.dp)) { Text("取消管理员") }
                            } else {
                                Button(onClick = {
                                    scope.launch {
                                        try {
                                            RetrofitClient.instance.setClubManager(SetClubManagerRequest(clubId, m.userId, true))
                                            reload()
                                        } catch (_: Exception) { }
                                    }
                                }, shape = RoundedCornerShape(8.dp)) { Text("设为管理员") }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ======================== Tab 1: 活动 ========================

@Composable
private fun ActivitiesTab(
    activities: List<ActivityData>,
    clubId: String,
    snackbarHostState: SnackbarHostState,
    reload: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // 结束活动对话框状态
    var showEndDialog by remember { mutableStateOf(false) }
    var endingActId by remember { mutableStateOf("") }
    var endingActTitle by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var participantList by remember { mutableStateOf("") }

    // 编辑活动对话框状态
    var showEditDialog by remember { mutableStateOf(false) }
    var editActId by remember { mutableStateOf("") }
    var editTitle by remember { mutableStateOf("") }
    var editContent by remember { mutableStateOf("") }
    var editLocation by remember { mutableStateOf("") }
    var editCapacity by remember { mutableStateOf("") }
    var isSavingEdit by remember { mutableStateOf(false) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("社团活动 (${activities.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        if (activities.isEmpty()) {
            item { Text("暂无活动", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) }
        } else {
            activities.forEach { act ->
                item {
                    Card(shape = RoundedCornerShape(12.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(act.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("地点: ${act.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                                when (act.activityState) {
                                    "待审核" -> Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF59E0B).copy(alpha = 0.15f)) { Text("审核中", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFFF59E0B)) }
                                    "已发布" -> Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF22C55E).copy(alpha = 0.15f)) { Text("已发布", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF22C55E)) }
                                    "已结束" -> Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF6B7280).copy(alpha = 0.15f)) { Text("已结束", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B7280)) }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // 编辑按钮（非已结束的活动可编辑）
                                if (act.activityState != "已结束") {
                                    OutlinedButton(
                                        onClick = {
                                            editActId = act.activityId
                                            editTitle = act.title
                                            editContent = act.content ?: ""
                                            editLocation = act.location ?: ""
                                            editCapacity = if ((act.capacityLimit ?: 0) > 0) (act.capacityLimit ?: 0).toString() else ""
                                            showEditDialog = true
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) { Text("编辑") }
                                }
                                if (act.activityState == "已发布") {
                                    Button(
                                        onClick = {
                                            endingActId = act.activityId
                                            endingActTitle = act.title
                                            summary = ""
                                            participantList = ""
                                            showEndDialog = true
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B7280))
                                    ) { Text("结束活动") }
                                }
                                Button(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                val resp = RetrofitClient.instance.deleteActivity(act.activityId)
                                                if (resp.isSuccessful) reload()
                                            } catch (_: Exception) { }
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Red500)
                                ) {
                                    Icon(Icons.Filled.Delete, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("删除")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 编辑活动对话框
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("编辑活动") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("活动标题") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = editContent,
                        onValueChange = { editContent = it },
                        label = { Text("活动内容") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = editLocation,
                        onValueChange = { editLocation = it },
                        label = { Text("活动地点") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = editCapacity,
                        onValueChange = { editCapacity = it.filter { c -> c.isDigit() } },
                        label = { Text("人数限制（0 表示不限制）") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editTitle.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("请输入活动标题") }
                            return@Button
                        }
                        isSavingEdit = true
                        showEditDialog = false
                        scope.launch {
                            try {
                                val resp = RetrofitClient.instance.updateActivity(
                                    UpdateActivityRequest(
                                        activityId = editActId,
                                        title = editTitle,
                                        content = editContent,
                                        location = editLocation,
                                        capacityLimit = editCapacity.toIntOrNull() ?: 0
                                    )
                                )
                                if (resp.isSuccessful) {
                                    snackbarHostState.showSnackbar("活动信息已更新")
                                    reload()
                                } else {
                                    snackbarHostState.showSnackbar("更新失败")
                                }
                            } catch (_: Exception) {
                                snackbarHostState.showSnackbar("网络错误")
                            } finally {
                                isSavingEdit = false
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    enabled = editTitle.isNotBlank() && !isSavingEdit
                ) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("取消") } }
        )
    }

    // 结束活动对话框（省略，保持不变）
    if (showEndDialog) {
        AlertDialog(
            onDismissRequest = { showEndDialog = false },
            title = { Text("结束活动") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("活动: $endingActTitle", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = { Text("活动总结") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = participantList,
                        onValueChange = { participantList = it },
                        label = { Text("实际到场名单（学号用逗号分隔）") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEndDialog = false
                        scope.launch {
                            try {
                                val resp = RetrofitClient.instance.endActivity(
                                    ActivityEndRequest(endingActId, summary, participantList)
                                )
                                if (resp.isSuccessful) {
                                    snackbarHostState.showSnackbar("活动已结束")
                                    reload()
                                }
                            } catch (_: Exception) { }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B7280)),
                    enabled = summary.isNotBlank()
                ) { Text("确认结束") }
            },
            dismissButton = { TextButton(onClick = { showEndDialog = false }) { Text("取消") } }
        )
    }
}

// ======================== Tab 2: 报名 ========================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegistrationsTab(
    pendingRegs: List<RegistrationAuditData>,
    activities: List<ActivityData>,
    selectedActId: String,
    onSelectActivity: (String) -> Unit,
    participants: List<ApprovedParticipantData>,
    snackbarHostState: SnackbarHostState,
    reload: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoadingParticipants by remember { mutableStateOf(false) }
    var localParticipants by remember { mutableStateOf<List<ApprovedParticipantData>>(emptyList()) }

    // 当选择的活动变化时加载参与者
    LaunchedEffect(selectedActId) {
        if (selectedActId.isNotBlank()) {
            isLoadingParticipants = true
            try {
                val resp = RetrofitClient.instance.getApprovedParticipants(selectedActId)
                if (resp.isSuccessful) {
                    localParticipants = resp.body()?.data ?: emptyList()
                }
            } catch (_: Exception) {
                scope.launch { snackbarHostState.showSnackbar("网络错误") }
            } finally {
                isLoadingParticipants = false
            }
        } else {
            localParticipants = emptyList()
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
        // === 待审核报名 ===
        item {
            Text("待审核报名 (${pendingRegs.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (pendingRegs.isEmpty()) {
            item { Text("暂无待审核报名", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) }
        } else {
            pendingRegs.forEach { reg ->
                item {
                    Card(shape = RoundedCornerShape(12.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(reg.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${reg.realName} | ${reg.phoneNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = {
                                    scope.launch {
                                        try {
                                            RetrofitClient.instance.auditRegistration(AuditRegistrationRequest(reg.registrationId, false))
                                            reload()
                                        } catch (_: Exception) { }
                                    }
                                }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Red500)) { Text("拒绝") }
                                Button(onClick = {
                                    scope.launch {
                                        try {
                                            RetrofitClient.instance.auditRegistration(AuditRegistrationRequest(reg.registrationId, true))
                                            reload()
                                        } catch (_: Exception) { }
                                    }
                                }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Green500)) { Text("批准") }
                            }
                        }
                    }
                }
            }
        }

        // === 已报名用户查看 ===
        item {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            Text("查看已报名用户", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            // 活动选择下拉
            var expanded by remember { mutableStateOf(false) }
            val publishedActivities = activities.filter { it.activityState == "已发布" || it.activityState == "已结束" }
            if (publishedActivities.isEmpty()) {
                Text("暂无可选活动", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            } else {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    val label = publishedActivities.find { it.activityId == selectedActId }?.title ?: "请选择活动"
                    OutlinedTextField(
                        value = label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("选择活动") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        publishedActivities.forEach { act ->
                            DropdownMenuItem(
                                text = { Text(act.title) },
                                onClick = {
                                    onSelectActivity(act.activityId)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // 参与者列表
        if (selectedActId.isNotBlank()) {
            item {
                Spacer(Modifier.height(8.dp))
                if (isLoadingParticipants) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(24.dp))
                    }
                } else if (localParticipants.isEmpty()) {
                    Text("暂无已报名的用户", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                } else {
                    Text("已报名用户 (${localParticipants.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            localParticipants.forEach { p ->
                item {
                    Card(shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Person, null, Modifier.size(20.dp), tint = Indigo600)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(p.realName ?: p.userId, style = MaterialTheme.typography.titleMedium)
                                Text("学号: ${p.userId} | 电话: ${p.phoneNumber ?: "无"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ======================== Tab 3: 设置 ========================

@Composable
private fun SettingsTab(
    club: com.clubmgmt.app.data.Club,
    clubName: String,
    onClubNameChange: (String) -> Unit,
    clubDescription: String,
    onClubDescriptionChange: (String) -> Unit,
    clubId: String,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit
) {
    var showDisbandDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Card(shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("修改社团信息", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = clubName, onValueChange = onClubNameChange, label = { Text("社团名称") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = clubDescription, onValueChange = onClubDescriptionChange, label = { Text("社团简介") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = {
                        scope.launch {
                            try {
                                val resp = RetrofitClient.instance.updateClub(UpdateClubRequest(clubId, clubName, clubDescription, club.school))
                                if (resp.isSuccessful) {
                                    snackbarHostState.showSnackbar("修改成功")
                                    onBack()
                                }
                            } catch (_: Exception) { snackbarHostState.showSnackbar("修改失败") }
                        }
                    }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Indigo600)) { Text("保存更改") }
                }
            }
        }

        // 危险区域
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("危险区域", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Text("解散社团是一个不可逆的操作。所有社团数据将被永久删除。")
                Button(onClick = { showDisbandDialog = true }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("解散社团") }
            }
        }

        if (showDisbandDialog) {
            AlertDialog(
                onDismissRequest = { showDisbandDialog = false },
                title = { Text("确认解散") },
                text = { Text("您确定要解散社团 \"${club.clubName}\" 吗？此操作不可撤销。") },
                confirmButton = { Button(onClick = {
                    showDisbandDialog = false
                    scope.launch {
                        try {
                            val resp = RetrofitClient.instance.dissolveClub(DissolveClubRequest(clubId))
                            if (resp.isSuccessful) {
                                snackbarHostState.showSnackbar("社团已解散")
                                onBack()
                            }
                        } catch (_: Exception) { snackbarHostState.showSnackbar("解散失败") }
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("确认解散") } },
                dismissButton = { TextButton(onClick = { showDisbandDialog = false }) { Text("取消") } }
            )
        }
    }
}

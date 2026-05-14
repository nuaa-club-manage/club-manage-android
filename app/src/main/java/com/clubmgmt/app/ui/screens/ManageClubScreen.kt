package com.clubmgmt.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clubmgmt.app.data.api.AuditMemberRequest
import com.clubmgmt.app.data.api.ClubMemberAuditData
import com.clubmgmt.app.data.api.DissolveClubRequest
import com.clubmgmt.app.data.api.RetrofitClient
import com.clubmgmt.app.data.api.UpdateClubRequest
import com.clubmgmt.app.data.toClub
import com.clubmgmt.app.ui.theme.Indigo600
import com.clubmgmt.app.ui.theme.Green500
import com.clubmgmt.app.ui.theme.Red500
import kotlinx.coroutines.launch

@Composable
fun ManageClubScreen(
    clubId: String,
    onBack: () -> Unit
) {
    var club by remember { mutableStateOf<com.clubmgmt.app.data.Club?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var pendingMembers by remember { mutableStateOf<List<ClubMemberAuditData>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableIntStateOf(0) }
    var clubName by remember { mutableStateOf("") }
    var clubDescription by remember { mutableStateOf("") }

    // 加载社团信息和待审核成员
    LaunchedEffect(clubId) {
        try {
            val clubResp = RetrofitClient.instance.getClubDetail(clubId)
            if (clubResp.isSuccessful) {
                val body = clubResp.body()
                if (body != null && body.code == 200) {
                    val c = body.data?.toClub()
                    club = c
                    if (c != null) {
                        clubName = c.clubName
                        clubDescription = c.clubInformation
                    }
                }
            }
            val memberResp = RetrofitClient.instance.getPendingApplications()
            if (memberResp.isSuccessful) {
                val body = memberResp.body()
                if (body != null && body.code == 200) {
                    pendingMembers = body.data?.filter { it.clubId == clubId } ?: emptyList()
                }
            }
        } catch (_: Exception) {
            scope.launch { snackbarHostState.showSnackbar("网络错误") }
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (club == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("未找到社团")
        }
        return
    }

    val safeClub = club!!

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Text("← 返回", color = Indigo600)
                }
                Spacer(Modifier.width(8.dp))
                Text("管理: ${safeClub.clubName}", style = MaterialTheme.typography.headlineMedium)
            }

            Spacer(Modifier.height(16.dp))

            TabRow(selectedTabIndex = activeTab) {
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                    Text("成员管理", modifier = Modifier.padding(12.dp))
                }
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                    Text("信息修改", modifier = Modifier.padding(12.dp))
                }
            }

            when (activeTab) {
                0 -> {
                    MembersTab(
                        pendingMembers = pendingMembers,
                        onApprove = { userId ->
                            scope.launch {
                                try {
                                    val resp = RetrofitClient.instance.auditMember(
                                        AuditMemberRequest(clubId = clubId, userId = userId, pass = true)
                                    )
                                    if (resp.isSuccessful) {
                                        pendingMembers = pendingMembers.filter { it.userId != userId }
                                        snackbarHostState.showSnackbar("已批准入社")
                                    }
                                } catch (_: Exception) {
                                    snackbarHostState.showSnackbar("操作失败")
                                }
                            }
                        },
                        onDeny = { userId ->
                            scope.launch {
                                try {
                                    val resp = RetrofitClient.instance.auditMember(
                                        AuditMemberRequest(clubId = clubId, userId = userId, pass = false)
                                    )
                                    if (resp.isSuccessful) {
                                        pendingMembers = pendingMembers.filter { it.userId != userId }
                                        snackbarHostState.showSnackbar("已拒绝入社")
                                    }
                                } catch (_: Exception) {
                                    snackbarHostState.showSnackbar("操作失败")
                                }
                            }
                        }
                    )
                }
                1 -> {
                    SettingsTab(
                        club = safeClub,
                        clubName = clubName,
                        onClubNameChange = { clubName = it },
                        clubDescription = clubDescription,
                        onClubDescriptionChange = { clubDescription = it },
                        onSave = {
                            scope.launch {
                                try {
                                    val resp = RetrofitClient.instance.updateClub(
                                        UpdateClubRequest(clubId = clubId, clubName = clubName, clubInformation = clubDescription, school = safeClub.school)
                                    )
                                    if (resp.isSuccessful) {
                                        snackbarHostState.showSnackbar("修改成功")
                                        onBack()
                                    }
                                } catch (_: Exception) {
                                    snackbarHostState.showSnackbar("修改失败")
                                }
                            }
                        },
                        onDisband = {
                            scope.launch {
                                try {
                                    val resp = RetrofitClient.instance.dissolveClub(
                                        DissolveClubRequest(clubId = clubId)
                                    )
                                    if (resp.isSuccessful) {
                                        snackbarHostState.showSnackbar("社团已解散")
                                        onBack()
                                    }
                                } catch (_: Exception) {
                                    snackbarHostState.showSnackbar("解散失败")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MembersTab(
    pendingMembers: List<ClubMemberAuditData>,
    onApprove: (String) -> Unit,
    onDeny: (String) -> Unit
) {
    if (pendingMembers.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("当前没有待处理的入团申请。", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("入团申请 (${pendingMembers.size})", style = MaterialTheme.typography.titleMedium)
        }
        items(pendingMembers, key = { it.userId }) { request ->
            Card(shape = RoundedCornerShape(12.dp)) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(request.realName.ifBlank { request.userName }, style = MaterialTheme.typography.titleMedium)
                        Text("学号: ${request.userId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onDeny(request.userId) },
                            colors = ButtonDefaults.buttonColors(containerColor = Red500),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.Close, null, Modifier.size(16.dp))
                            Text("拒绝")
                        }
                        Button(
                            onClick = { onApprove(request.userId) },
                            colors = ButtonDefaults.buttonColors(containerColor = Green500),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.Check, null, Modifier.size(16.dp))
                            Text("批准")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsTab(
    club: com.clubmgmt.app.data.Club,
    clubName: String,
    onClubNameChange: (String) -> Unit,
    clubDescription: String,
    onClubDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
    onDisband: () -> Unit
) {
    var showDisbandDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Card(shape = RoundedCornerShape(12.dp)) {
            Column(
                Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("修改社团信息", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = clubName, onValueChange = onClubNameChange,
                    label = { Text("社团名称") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = clubDescription, onValueChange = onClubDescriptionChange,
                    label = { Text("社团简介") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(8.dp)
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onSave, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Indigo600)) {
                        Text("保存更改")
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "危险区域",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text("解散社团是一个不可逆的操作。所有社团数据将被永久删除。")
                Button(
                    onClick = { showDisbandDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("解散社团")
                }
            }
        }

        if (showDisbandDialog) {
            AlertDialog(
                onDismissRequest = { showDisbandDialog = false },
                title = { Text("确认解散") },
                text = { Text("您确定要解散社团 \"${club.clubName}\" 吗？此操作不可撤销。") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDisbandDialog = false
                            onDisband()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("确认解散") }
                },
                dismissButton = {
                    TextButton(onClick = { showDisbandDialog = false }) { Text("取消") }
                }
            )
        }
    }
}

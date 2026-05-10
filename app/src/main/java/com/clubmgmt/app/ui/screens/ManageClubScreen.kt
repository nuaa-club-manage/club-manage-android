package com.clubmgmt.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.clubmgmt.app.data.Club
import com.clubmgmt.app.data.ClubMember
import com.clubmgmt.app.data.mockClubs
import com.clubmgmt.app.ui.theme.Indigo600
import com.clubmgmt.app.ui.theme.Green500
import com.clubmgmt.app.ui.theme.Red500

@Composable
fun ManageClubScreen(
    clubId: Int,
    onBack: () -> Unit
) {
    var club by remember { mutableStateOf(mockClubs.find { it.id == clubId }) }

    if (club == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("未找到社团")
        }
        return
    }

    val safeClub = club!!

    var activeTab by remember { mutableIntStateOf(0) }
    var clubName by remember { mutableStateOf(safeClub.name) }
    var clubDescription by remember { mutableStateOf(safeClub.description) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Text("← 返回", color = Indigo600)
            }
            Spacer(Modifier.width(8.dp))
            Text("管理: ${safeClub.name}", style = MaterialTheme.typography.headlineMedium)
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
                    club = safeClub,
                    onApprove = { userId ->
                        club = club!!.copy(joinRequests = club!!.joinRequests.filter { it.id != userId })
                    },
                    onDeny = { userId ->
                        club = club!!.copy(joinRequests = club!!.joinRequests.filter { it.id != userId })
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
                    onSave = { onBack() },
                    onDisband = { onBack() }
                )
            }
        }
    }
}

@Composable
private fun MembersTab(
    club: Club,
    onApprove: (Int) -> Unit,
    onDeny: (Int) -> Unit
) {
    if (club.joinRequests.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("当前没有待处理的入团申请。", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    Column(
        modifier = Modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("入团申请 (${club.joinRequests.size})", style = MaterialTheme.typography.titleMedium)
        club.joinRequests.forEach { request ->
            Card(shape = RoundedCornerShape(12.dp)) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = request.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(20.dp))
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(request.name, style = MaterialTheme.typography.titleMedium)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onApprove(request.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = Green500),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.Check, null, Modifier.size(16.dp))
                            Text("批准")
                        }
                        Button(
                            onClick = { onDeny(request.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = Red500),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.Close, null, Modifier.size(16.dp))
                            Text("拒绝")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsTab(
    club: Club,
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
                text = { Text("您确定要解散社团 \"${club.name}\" 吗？此操作不可撤销。") },
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

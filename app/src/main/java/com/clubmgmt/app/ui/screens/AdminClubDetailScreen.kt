package com.clubmgmt.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clubmgmt.app.data.api.ClubMemberListData
import com.clubmgmt.app.data.api.RetrofitClient
import com.clubmgmt.app.data.api.SetClubManagerRequest
import com.clubmgmt.app.data.toClub
import com.clubmgmt.app.ui.theme.Green500
import com.clubmgmt.app.ui.theme.Indigo600
import com.clubmgmt.app.ui.theme.Red500
import kotlinx.coroutines.launch

@Composable
fun AdminClubDetailScreen(
    clubId: String,
    onBack: () -> Unit
) {
    var club by remember { mutableStateOf<com.clubmgmt.app.data.Club?>(null) }
    var members by remember { mutableStateOf<List<ClubMemberListData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun loadData() {
        scope.launch {
            isLoading = true
            try {
                val clubResp = RetrofitClient.instance.getClubDetail(clubId)
                if (clubResp.isSuccessful) {
                    val body = clubResp.body()
                    if (body != null && body.code == 200) {
                        club = body.data?.toClub()
                    }
                }
                val memberResp = RetrofitClient.instance.getAllClubMembers()
                if (memberResp.isSuccessful) {
                    members = memberResp.body()?.data?.filter { it.clubId == clubId } ?: emptyList()
                }
            } catch (_: Exception) {
                snackbarHostState.showSnackbar("网络错误")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(clubId) { loadData() }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 返回 + 标题
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                    Spacer(Modifier.width(8.dp))
                    Text("社团详情", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            }

            // 社团基本信息
            item {
                val c = club
                if (c != null) {
                    Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(c.clubName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                            if (c.school.isNotBlank()) {
                                Text("学校: ${c.school}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Text(
                                "状态: ${
                                    when (c.clubState) {
                                        "已通过" -> "已成立"
                                        "待审核" -> "审核中"
                                        "未通过" -> "已拒绝"
                                        "已解散" -> "已解散"
                                        else -> c.clubState
                                    }
                                }",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (!c.clubInformation.isNullOrBlank()) {
                                HorizontalDivider()
                                Text("社团简介", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(c.clubInformation, style = MaterialTheme.typography.bodyMedium)
                            }
                            if (c.establishmentTime.isNotBlank()) {
                                Text("成立时间: ${c.establishmentTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            // 成员列表标题
            item {
                Text("社团成员 (${members.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (members.isEmpty()) {
                item { Text("暂无成员", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) }
            } else {
                items(members, key = { it.userId }) { m ->
                    Card(shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Person, null, Modifier.size(20.dp), tint = Indigo600)
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text((m.realName ?: m.userName).ifBlank { m.userName }, style = MaterialTheme.typography.titleMedium)
                                    if (m.clubManager == "是" || m.clubManager == "1") {
                                        Spacer(Modifier.width(6.dp))
                                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF59E0B).copy(alpha = 0.15f)) {
                                            Text("管理员", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text("学号: ${m.userId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                if (!m.school.isNullOrBlank()) {
                                    Text(m.school, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }
                            if (m.clubManager == "是" || m.clubManager == "1") {
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                RetrofitClient.instance.setClubManager(SetClubManagerRequest(clubId, m.userId, false))
                                                loadData()
                                            } catch (_: Exception) { }
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) { Text("取消管理员") }
                            } else {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                RetrofitClient.instance.setClubManager(SetClubManagerRequest(clubId, m.userId, true))
                                                loadData()
                                            } catch (_: Exception) { }
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Green500)
                                ) { Text("设为管理员") }
                            }
                        }
                    }
                }
            }
        }
    }
}

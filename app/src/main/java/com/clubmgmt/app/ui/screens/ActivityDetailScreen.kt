package com.clubmgmt.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clubmgmt.app.data.api.RegisterActivityRequest
import com.clubmgmt.app.data.api.RetrofitClient
import com.clubmgmt.app.data.toActivity
import com.clubmgmt.app.ui.theme.Indigo600
import kotlinx.coroutines.launch

@Composable
fun ActivityDetailScreen(
    activityId: String,
    onBack: () -> Unit,
    onClubClick: (String) -> Unit
) {
    var activity by remember { mutableStateOf<com.clubmgmt.app.data.Activity?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(activityId) {
        try {
            val resp = RetrofitClient.instance.getActivityDetail(activityId)
            if (resp.isSuccessful) {
                val body = resp.body()
                if (body != null && body.code == 200) {
                    activity = body.data?.toActivity()
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

    if (activity == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("未找到该活动。")
        }
        return
    }

    val safeActivity = activity!!

    var isRegistering by remember { mutableStateOf(false) }
    var showRegisterDialog by remember { mutableStateOf(false) }
    var realName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Text("← 返回", color = Indigo600)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 活动标题和社团
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    TextButton(
                        onClick = { onClubClick(safeActivity.clubId) },
                        colors = ButtonDefaults.textButtonColors(contentColor = Indigo600)
                    ) {
                        Icon(Icons.Filled.Groups, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(safeActivity.clubName, style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        text = safeActivity.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // 活动详情
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("关于活动", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(safeActivity.content, style = MaterialTheme.typography.bodyMedium)
                }
            }

            // 活动信息
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailRow(Icons.Filled.LocationOn, safeActivity.location, "地点")
                    HorizontalDivider()
                    DetailRow(Icons.Filled.Groups, safeActivity.clubName, "主办社团")
                    if (safeActivity.capacityLimit > 0) {
                        HorizontalDivider()
                        DetailRow(Icons.Filled.Groups, "最多 ${safeActivity.capacityLimit} 人", "人数限制")
                    }
                }
            }

            // 报名按钮
            Button(
                onClick = { showRegisterDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
            ) {
                Text("报名参加")
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // 报名对话框
    if (showRegisterDialog) {
        AlertDialog(
            onDismissRequest = { showRegisterDialog = false },
            title = { Text("报名参加 ${safeActivity.title}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = realName,
                        onValueChange = { realName = it },
                        label = { Text("真实姓名") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("手机号") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (realName.isBlank() || phoneNumber.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("请填写完整信息") }
                            return@Button
                        }
                        scope.launch {
                            isRegistering = true
                            try {
                                val resp = RetrofitClient.instance.registerActivity(
                                    RegisterActivityRequest(
                                        activityId = activityId,
                                        realName = realName,
                                        phoneNumber = phoneNumber
                                    )
                                )
                                if (resp.isSuccessful) {
                                    val body = resp.body()
                                    if (body != null && body.code == 200) {
                                        snackbarHostState.showSnackbar("报名成功，请等待审核")
                                        showRegisterDialog = false
                                    } else {
                                        snackbarHostState.showSnackbar(body?.message ?: "报名失败")
                                    }
                                } else {
                                    snackbarHostState.showSnackbar("报名失败")
                                }
                            } catch (_: Exception) {
                                snackbarHostState.showSnackbar("网络错误")
                            } finally {
                                isRegistering = false
                            }
                        }
                    },
                    enabled = !isRegistering,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("提交报名")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegisterDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Indigo600, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

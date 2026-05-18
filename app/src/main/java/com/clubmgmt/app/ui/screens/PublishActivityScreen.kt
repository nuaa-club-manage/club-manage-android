package com.clubmgmt.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.clubmgmt.app.data.api.CreateActivityRequest
import com.clubmgmt.app.data.api.RetrofitClient
import com.clubmgmt.app.data.toClub
import com.clubmgmt.app.ui.theme.Indigo600
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishActivityScreen(
    onBack: () -> Unit
) {
    var managedClubs by remember { mutableStateOf<List<com.clubmgmt.app.data.Club>>(emptyList()) }
    var isLoadingClubs by remember { mutableStateOf(true) }
    var title by remember { mutableStateOf("") }
    var selectedClubId by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var capacityLimit by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var isPublishing by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val resp = RetrofitClient.instance.getManagedClubs()
            if (resp.isSuccessful) {
                val body = resp.body()
                if (body != null && body.code == 200) {
                    val clubs = body.data?.map { it.toClub() } ?: emptyList()
                    managedClubs = clubs
                    if (clubs.isNotEmpty()) selectedClubId = clubs.first().clubId
                }
            }
        } catch (_: Exception) {
            scope.launch { snackbarHostState.showSnackbar("网络错误") }
        } finally {
            isLoadingClubs = false
        }
    }

    if (isLoadingClubs) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (managedClubs.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("您没有管理任何社团", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("您需要先创建一个社团才能发布活动。")
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBack) { Text("返回个人中心") }
        }
        return
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Spacer(Modifier.width(8.dp))
                Text("发布新活动", style = MaterialTheme.typography.headlineMedium)
            }

            Spacer(Modifier.height(24.dp))

            Card(shape = RoundedCornerShape(12.dp)) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = title, onValueChange = { title = it },
                        label = { Text("活动标题") },
                        placeholder = { Text("例如：年度编程马拉松") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = managedClubs.find { it.clubId == selectedClubId }?.clubName ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("选择社团") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            managedClubs.forEach { club ->
                                DropdownMenuItem(
                                    text = { Text(club.clubName) },
                                    onClick = {
                                        selectedClubId = club.clubId
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = location, onValueChange = { location = it },
                        label = { Text("地点") },
                        placeholder = { Text("例如：将军路校区教学楼 A1-201") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = content, onValueChange = { content = it },
                        label = { Text("活动描述") },
                        placeholder = { Text("详细介绍您的活动内容。") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = capacityLimit, onValueChange = { capacityLimit = it },
                        label = { Text("人数限制") },
                        placeholder = { Text("0 表示不限制") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onBack, shape = RoundedCornerShape(8.dp)) { Text("取消") }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = {
                        scope.launch {
                            isPublishing = true
                            try {
                                val resp = RetrofitClient.instance.createActivity(
                                    CreateActivityRequest(
                                        clubId = selectedClubId,
                                        title = title,
                                        content = content,
                                        location = location,
                                        capacityLimit = capacityLimit.toIntOrNull() ?: 0
                                    )
                                )
                                if (resp.isSuccessful) {
                                    val body = resp.body()
                                    if (body != null && body.code == 200) {
                                        snackbarHostState.showSnackbar("活动发布成功，请等待管理员审核")
                                        onBack()
                                    } else {
                                        snackbarHostState.showSnackbar(body?.message ?: "发布失败")
                                    }
                                } else {
                                    snackbarHostState.showSnackbar("发布失败 (${resp.code()})")
                                }
                            } catch (_: Exception) {
                                snackbarHostState.showSnackbar("网络错误，请稍后重试")
                            } finally {
                                isPublishing = false
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                    enabled = !isPublishing && title.isNotBlank() && selectedClubId.isNotBlank()
                ) {
                    if (isPublishing) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("发布活动")
                }
            }
        }
    }
}

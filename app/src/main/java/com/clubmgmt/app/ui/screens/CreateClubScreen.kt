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
import com.clubmgmt.app.data.api.CreateClubRequest
import com.clubmgmt.app.data.api.RetrofitClient
import com.clubmgmt.app.ui.theme.Indigo600
import kotlinx.coroutines.launch

@Composable
fun CreateClubScreen(
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                Text("成立一个新社团", style = MaterialTheme.typography.headlineMedium)
            }

            Spacer(Modifier.height(24.dp))

            Card(shape = RoundedCornerShape(12.dp)) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("社团名称") },
                        placeholder = { Text("例如：南京航空航天大学编程俱乐部") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = description, onValueChange = { description = it },
                        label = { Text("社团简介") },
                        placeholder = { Text("详细介绍您的社团，包括其宗旨、常规活动等。") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onBack, shape = RoundedCornerShape(8.dp)) {
                    Text("取消")
                }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = {
                        if (name.isBlank() || description.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("请填写社团名称和简介") }
                            return@Button
                        }
                        scope.launch {
                            isSubmitting = true
                            try {
                                val resp = RetrofitClient.instance.createClub(
                                    CreateClubRequest(clubName = name, clubInformation = description)
                                )
                                if (resp.isSuccessful) {
                                    val body = resp.body()
                                    if (body != null && body.code == 200) {
                                        snackbarHostState.showSnackbar("社团成立申请已提交，请等待管理员审核")
                                        onBack()
                                    } else {
                                        snackbarHostState.showSnackbar(body?.message ?: "提交失败")
                                    }
                                } else {
                                    snackbarHostState.showSnackbar("提交失败 (${resp.code()})")
                                }
                            } catch (_: Exception) {
                                snackbarHostState.showSnackbar("网络错误，请稍后重试")
                            } finally {
                                isSubmitting = false
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("提交申请")
                }
            }
        }
    }
}

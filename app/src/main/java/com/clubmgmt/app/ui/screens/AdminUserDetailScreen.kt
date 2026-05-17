package com.clubmgmt.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.clubmgmt.app.data.api.AdminEditUserRequest
import com.clubmgmt.app.data.api.AdminUserSearchRequest
import com.clubmgmt.app.data.api.RetrofitClient
import com.clubmgmt.app.ui.theme.Indigo600
import kotlinx.coroutines.launch

@Composable
fun AdminUserDetailScreen(
    userId: String,
    onBack: () -> Unit
) {
    var targetUserID by remember { mutableStateOf(userId) }
    var userName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var userMailbox by remember { mutableStateOf("") }
    var realName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var degree by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 加载用户信息
    LaunchedEffect(userId) {
        try {
            val resp = RetrofitClient.instance.adminSearchUsers(
                AdminUserSearchRequest(pageNo = 1, pageSize = 10, search = userId)
            )
            if (resp.isSuccessful) {
                val body = resp.body()
                if (body != null && body.code == 200) {
                    val user = body.data?.records?.firstOrNull()
                    if (user != null) {
                        targetUserID = user.userId
                        userName = user.userName
                        phoneNumber = user.phoneNumber.orEmpty()
                        userMailbox = user.userMailbox.orEmpty()
                        realName = user.realName.orEmpty()
                        gender = user.gender.orEmpty()
                        degree = user.degree.orEmpty()
                        school = user.school.orEmpty()
                    } else {
                        snackbarHostState.showSnackbar("未找到该用户")
                    }
                } else {
                    snackbarHostState.showSnackbar(body?.message ?: "加载失败")
                }
            } else {
                snackbarHostState.showSnackbar("加载失败 (${resp.code()})")
            }
        } catch (_: Exception) {
            snackbarHostState.showSnackbar("网络错误，请稍后重试")
        } finally {
            isLoading = false
        }
    }

    // 确认修改对话框
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("确认修改") },
            text = { Text("确认修改用户「$targetUserID」的资料吗？") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    scope.launch {
                        isSaving = true
                        try {
                            val resp = RetrofitClient.instance.adminUpdateUser(
                                AdminEditUserRequest(
                                    targetUserID = targetUserID,
                                    userName = userName,
                                    phoneNumber = phoneNumber,
                                    userMailbox = userMailbox,
                                    realName = realName,
                                    gender = gender,
                                    degree = degree,
                                    school = school,
                                    userPassword = userPassword
                                )
                            )
                            if (resp.isSuccessful) {
                                val body = resp.body()
                                if (body != null && body.code == 200) {
                                    snackbarHostState.showSnackbar(body.message)
                                    onBack()
                                } else {
                                    snackbarHostState.showSnackbar(body?.message ?: "修改失败")
                                }
                            } else {
                                snackbarHostState.showSnackbar("修改失败 (${resp.code()})")
                            }
                        } catch (_: Exception) {
                            snackbarHostState.showSnackbar("网络错误，请稍后重试")
                        } finally {
                            isSaving = false
                        }
                    }
                }) {
                    Text("确认修改")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Text("← 返回", color = Indigo600)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("用户资料 - $userId", style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(Modifier.height(20.dp))

                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("编辑用户信息", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        HorizontalDivider()

                        OutlinedTextField(
                            value = targetUserID,
                            onValueChange = {},
                            label = { Text("用户账号") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            enabled = false
                        )

                        OutlinedTextField(
                            value = userName, onValueChange = { userName = it },
                            label = { Text("用户名") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                            singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        OutlinedTextField(
                            value = phoneNumber, onValueChange = { phoneNumber = it },
                            label = { Text("电话号码") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                            singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        OutlinedTextField(
                            value = userMailbox, onValueChange = { userMailbox = it },
                            label = { Text("邮箱") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                            singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        OutlinedTextField(
                            value = realName, onValueChange = { realName = it },
                            label = { Text("真实姓名") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                            singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        OutlinedTextField(
                            value = gender, onValueChange = { gender = it },
                            label = { Text("性别") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                            singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        OutlinedTextField(
                            value = degree, onValueChange = { degree = it },
                            label = { Text("学历") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                            singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        OutlinedTextField(
                            value = school, onValueChange = { school = it },
                            label = { Text("学校") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                            singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        OutlinedTextField(
                            value = userPassword, onValueChange = { userPassword = it },
                            label = { Text("用户密码（留空不修改）") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                            singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                        )

                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            OutlinedButton(onClick = onBack, shape = RoundedCornerShape(8.dp)) {
                                Text("取消")
                            }
                            Spacer(Modifier.width(12.dp))
                            Button(
                                onClick = { showConfirmDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                                enabled = !isSaving
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text("修改用户信息")
                            }
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

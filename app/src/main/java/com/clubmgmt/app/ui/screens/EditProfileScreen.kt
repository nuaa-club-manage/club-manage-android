package com.clubmgmt.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.clubmgmt.app.data.api.ChangePasswordRequest
import com.clubmgmt.app.data.api.RetrofitClient
import com.clubmgmt.app.data.api.UpdateUserInfoRequest
import com.clubmgmt.app.ui.theme.Indigo600
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    onBack: () -> Unit
) {
    // 个人信息字段
    var userName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var userMailbox by remember { mutableStateOf("") }
    var realName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var degree by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }

    // 修改密码字段
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var pwdVisible by remember { mutableStateOf(false) }

    // 状态
    var isLoading by remember { mutableStateOf(true) }
    var isSavingInfo by remember { mutableStateOf(false) }
    var isSavingPwd by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 加载当前用户信息
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.instance.getUserInfo()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.code == 200) {
                    val d = body.data ?: return@LaunchedEffect
                    userName = d.userName
                    phoneNumber = d.phoneNumber
                    userMailbox = d.userMailbox
                    realName = d.realName
                    gender = d.gender
                    degree = d.degree
                    school = d.school
                } else {
                    snackbarHostState.showSnackbar(body?.message ?: "加载信息失败")
                }
            } else {
                snackbarHostState.showSnackbar("加载信息失败 (${response.code()})")
            }
        } catch (_: Exception) {
            snackbarHostState.showSnackbar("网络错误，请稍后重试")
        } finally {
            isLoading = false
        }
    }

    // 注销确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认注销") },
            text = { Text("您确定要注销账号吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            isDeleting = true
                            try {
                                val resp = RetrofitClient.instance.deleteAccount()
                                if (resp.isSuccessful) {
                                    val body = resp.body()
                                    if (body != null && body.code == 200) {
                                        snackbarHostState.showSnackbar(body.message)
                                        onBack()
                                    } else {
                                        snackbarHostState.showSnackbar(body?.message ?: "注销失败")
                                    }
                                } else {
                                    snackbarHostState.showSnackbar("注销失败 (${resp.code()})")
                                }
                            } catch (_: Exception) {
                                snackbarHostState.showSnackbar("网络错误，请稍后重试")
                            } finally {
                                isDeleting = false
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("确认注销")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
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
                // 顶部返回
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Text("← 返回", color = Indigo600)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("编辑个人资料", style = MaterialTheme.typography.headlineMedium)
                }

                Spacer(Modifier.height(20.dp))

                // ========== 一、修改个人信息 ==========
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("修改个人信息", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        HorizontalDivider()

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
                            singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                        )

                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        isSavingInfo = true
                                        try {
                                            val resp = RetrofitClient.instance.updateUserInfo(
                                                UpdateUserInfoRequest(
                                                    userName = userName,
                                                    phoneNumber = phoneNumber,
                                                    userMailbox = userMailbox,
                                                    realName = realName,
                                                    gender = gender,
                                                    degree = degree,
                                                    school = school
                                                )
                                            )
                                            if (resp.isSuccessful) {
                                                val body = resp.body()
                                                if (body != null && body.code == 200) {
                                                    snackbarHostState.showSnackbar(body.message)
                                                } else {
                                                    snackbarHostState.showSnackbar(body?.message ?: "修改失败")
                                                }
                                            } else {
                                                snackbarHostState.showSnackbar("修改失败 (${resp.code()})")
                                            }
                                        } catch (_: Exception) {
                                            snackbarHostState.showSnackbar("网络错误，请稍后重试")
                                        } finally {
                                            isSavingInfo = false
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                                enabled = !isSavingInfo
                            ) {
                                if (isSavingInfo) {
                                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text("确认修改")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ========== 二、修改密码 ==========
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("修改密码", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        HorizontalDivider()

                        OutlinedTextField(
                            value = oldPassword, onValueChange = { oldPassword = it },
                            label = { Text("旧密码") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
                        )
                        OutlinedTextField(
                            value = newPassword, onValueChange = { newPassword = it },
                            label = { Text("新密码") },
                            trailingIcon = {
                                IconButton(onClick = { pwdVisible = !pwdVisible }) {
                                    Icon(
                                        if (pwdVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        null
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            visualTransformation = if (pwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
                        )
                        OutlinedTextField(
                            value = confirmNewPassword, onValueChange = { confirmNewPassword = it },
                            label = { Text("确认新密码") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
                        )

                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(
                                onClick = {
                                    if (newPassword != confirmNewPassword) {
                                        scope.launch { snackbarHostState.showSnackbar("两次输入的新密码不一致") }
                                        return@Button
                                    }
                                    if (oldPassword.isBlank() || newPassword.isBlank()) {
                                        scope.launch { snackbarHostState.showSnackbar("请填写完整的密码信息") }
                                        return@Button
                                    }
                                    scope.launch {
                                        isSavingPwd = true
                                        try {
                                            val resp = RetrofitClient.instance.changePassword(
                                                ChangePasswordRequest(oldPassword = oldPassword, newPassword = newPassword)
                                            )
                                            if (resp.isSuccessful) {
                                                val body = resp.body()
                                                if (body != null && body.code == 200) {
                                                    snackbarHostState.showSnackbar(body.message)
                                                    oldPassword = ""; newPassword = ""; confirmNewPassword = ""
                                                } else {
                                                    snackbarHostState.showSnackbar(body?.message ?: "修改失败")
                                                }
                                            } else {
                                                snackbarHostState.showSnackbar("修改失败 (${resp.code()})")
                                            }
                                        } catch (_: Exception) {
                                            snackbarHostState.showSnackbar("网络错误，请稍后重试")
                                        } finally {
                                            isSavingPwd = false
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                                enabled = !isSavingPwd
                            ) {
                                if (isSavingPwd) {
                                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text("确认修改")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ========== 三、注销账号 ==========
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(Modifier.padding(20.dp)) {
                        Text("注销账号", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(Modifier.height(8.dp))
                        Text("注销后将无法恢复账号数据。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(
                                onClick = { showDeleteDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                enabled = !isDeleting
                            ) {
                                if (isDeleting) {
                                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onError)
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text("注销账号")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

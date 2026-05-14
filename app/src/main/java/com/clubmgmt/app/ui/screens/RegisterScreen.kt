package com.clubmgmt.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import com.clubmgmt.app.data.api.RetrofitClient
import com.clubmgmt.app.data.api.RegisterRequest
import com.clubmgmt.app.ui.theme.Indigo600
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateLogin: () -> Unit
) {
    var userID by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var verifyCode by remember { mutableStateOf("") }
    var sendCodeEnabled by remember { mutableStateOf(true) }
    var countdown by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun sendVerifyCode() {
        if (contact.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar("请输入手机号/邮箱") }
            return
        }
        scope.launch {
            try {
                val response = RetrofitClient.instance.sendCode(contact)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.code == 200) {
                        snackbarHostState.showSnackbar(body.message)
                        sendCodeEnabled = false
                        countdown = 60
                        while (countdown > 0) {
                            delay(1000)
                            countdown--
                        }
                        sendCodeEnabled = true
                    } else {
                        snackbarHostState.showSnackbar(body?.message ?: "发送失败")
                    }
                } else {
                    snackbarHostState.showSnackbar("发送失败")
                }
            } catch (_: Exception) {
                snackbarHostState.showSnackbar("网络错误，请稍后重试")
            }
        }
    }

    fun doRegister() {
        // 校验密码格式
        val passwordRegex = Regex("^(?=.*[a-zA-Z])(?=.*\\d).{6,20}$")
        if (!passwordRegex.matches(userPassword)) {
            scope.launch { snackbarHostState.showSnackbar("密码格式为6~20位，必须包含数字和字母") }
            return
        }
        // 校验两次密码一致
        if (userPassword != confirmPassword) {
            scope.launch { snackbarHostState.showSnackbar("两次输入的密码不一致") }
            return
        }
        if (userID.isBlank() || contact.isBlank() || verifyCode.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar("请填写完整信息") }
            return
        }

        scope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.register(
                    RegisterRequest(
                        userId = userId,
                        contact = contact,
                        userPassword = userPassword,
                        verifyCode = verifyCode
                    )
                )
                isLoading = false
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.code == 200) {
                        snackbarHostState.showSnackbar(body.message)
                        onRegisterSuccess()
                    } else {
                        snackbarHostState.showSnackbar(body?.message ?: "注册失败")
                    }
                } else {
                    snackbarHostState.showSnackbar("注册失败 (${response.code()})")
                }
            } catch (_: Exception) {
                isLoading = false
                snackbarHostState.showSnackbar("网络错误，请稍后重试")
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                "社团管理",
                style = MaterialTheme.typography.headlineLarge,
                color = Indigo600,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("创建您的新账户", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = userID,
                onValueChange = { userID = it },
                label = { Text("学号") },
                leadingIcon = { Icon(Icons.Filled.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = contact,
                onValueChange = { contact = it },
                label = { Text("手机号/邮箱") },
                leadingIcon = { Icon(Icons.Filled.Phone, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = userPassword,
                onValueChange = { userPassword = it },
                label = { Text("密码") },
                leadingIcon = { Icon(Icons.Filled.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                )
            )
            Text(
                "密码为6~20位，必须包含数字和字母",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 4.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("确认密码") },
                leadingIcon = { Icon(Icons.Filled.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = verifyCode,
                    onValueChange = { verifyCode = it },
                    label = { Text("验证码") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { sendVerifyCode() },
                    enabled = sendCodeEnabled && !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
                ) {
                    Text(
                        text = if (sendCodeEnabled) "发送验证码" else "${countdown}s",
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onNavigateLogin) {
                    Text("已有账号? 前往登录")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { doRegister() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("注册", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

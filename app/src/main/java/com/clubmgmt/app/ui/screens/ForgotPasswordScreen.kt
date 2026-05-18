package com.clubmgmt.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
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
import com.clubmgmt.app.data.api.ResetPasswordRequest
import com.clubmgmt.app.data.api.RetrofitClient
import com.clubmgmt.app.ui.theme.Indigo600
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit
) {
    var contact by remember { mutableStateOf("") }
    var verifyCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
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

    fun doResetPassword() {
        val passwordRegex = Regex("^(?=.*[a-zA-Z])(?=.*\\d).{6,20}$")
        if (!passwordRegex.matches(newPassword)) {
            scope.launch { snackbarHostState.showSnackbar("密码格式为6~20位，必须包含数字和字母") }
            return
        }
        if (contact.isBlank() || verifyCode.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar("请填写完整信息") }
            return
        }

        scope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.resetPassword(
                    ResetPasswordRequest(
                        contact = contact,
                        verifyCode = verifyCode,
                        newPassword = newPassword
                    )
                )
                isLoading = false
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.code == 200) {
                        snackbarHostState.showSnackbar(body.message)
                        onBack()
                    } else {
                        snackbarHostState.showSnackbar(body?.message ?: "重置失败")
                    }
                } else {
                    snackbarHostState.showSnackbar("重置失败 (${response.code()})")
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "社团管理",
                style = MaterialTheme.typography.headlineLarge,
                color = Indigo600,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("重置密码", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "输入您的手机号/邮箱，接收验证码后设置新密码。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(24.dp))

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
                        imeAction = ImeAction.Next
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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("新密码") },
                leadingIcon = { Icon(Icons.Filled.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                        Icon(
                            imageVector = if (newPasswordVisible) Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                visualTransformation = if (newPasswordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
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

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { doResetPassword() },
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
                Text("确认重置", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

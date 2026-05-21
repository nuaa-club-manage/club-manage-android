package com.clubmgmt.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
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
import com.clubmgmt.app.data.SessionManager
import com.clubmgmt.app.data.api.AdminLoginRequest
import com.clubmgmt.app.data.api.CaptchaResponse
import com.clubmgmt.app.data.api.LoginRequest
import com.clubmgmt.app.data.api.RetrofitClient
import com.clubmgmt.app.ui.theme.Indigo600
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateRegister: () -> Unit,
    onNavigateForgotPassword: () -> Unit,
    onNavigateAdmin: () -> Unit
) {
    // 0 = 密码登录, 1 = 验证码登录
    var loginTab by remember { mutableIntStateOf(0) }
    val isPasswordLogin = loginTab == 0

    // 密码登录字段
    var userID by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // 验证码登录字段
    var contact by remember { mutableStateOf("") }
    var verifyCode by remember { mutableStateOf("") }
    var sendCodeEnabled by remember { mutableStateOf(true) }
    var countdown by remember { mutableIntStateOf(0) }

    // 图形验证码
    var captchaData by remember { mutableStateOf<CaptchaResponse?>(null) }
    var captchaCode by remember { mutableStateOf("") }

    // 通用状态
    var isLoading by remember { mutableStateOf(false) }
    var isAdminLogin by remember { mutableStateOf(false) }
    var loadingText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 加载图形验证码
    fun loadCaptcha() {
        scope.launch {
            try {
                val response = RetrofitClient.instance.getCaptcha()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.code == 200) {
                        captchaData = body
                    }
                }
            } catch (_: Exception) { }
        }
    }

    // 发送短信验证码
    fun sendVerifyCode() {
        if (contact.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar("请先输入手机号/邮箱") }
            return
        }
        scope.launch {
            try {
                val response = RetrofitClient.instance.sendCode(contact)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.code == 200) {
                        snackbarHostState.showSnackbar(body.data ?: "验证码已发送")
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

    // 执行登录
    fun doLogin() {
        // 管理员登录：不需要验证码，使用独立接口
        if (isAdminLogin) {
            if (userID.isBlank() || userPassword.isBlank()) {
                scope.launch { snackbarHostState.showSnackbar("请输入管理员账号和密码") }
                return
            }
            scope.launch {
                isLoading = true
                loadingText = "管理员登录中..."
                try {
                    val response = RetrofitClient.instance.adminLogin(
                        AdminLoginRequest(userId = userID, userPassword = userPassword)
                    )
                    isLoading = false
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.code == 200) {
                            SessionManager.token = body.data
                            SessionManager.userId = userID
                            SessionManager.userRole = "ADMIN"
                            snackbarHostState.showSnackbar(body.message)
                            onNavigateAdmin()
                        } else {
                            snackbarHostState.showSnackbar(body?.message ?: "管理员登录失败")
                        }
                    } else {
                        snackbarHostState.showSnackbar("管理员登录失败 (${response.code()})")
                    }
                } catch (_: Exception) {
                    isLoading = false
                    snackbarHostState.showSnackbar("网络错误，请稍后重试")
                }
            }
            return
        }

        // 普通用户登录：需要图形验证码
        if (captchaCode.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar("请输入图形验证码") }
            return
        }
        val captchaId = captchaData?.data?.captchaId
        if (captchaId == null) {
            scope.launch { snackbarHostState.showSnackbar("验证码未加载，请刷新后重试") }
            return
        }
        if (isPasswordLogin && (userID.isBlank() || userPassword.isBlank())) {
            scope.launch { snackbarHostState.showSnackbar("请输入账号和密码") }
            return
        }
        if (isPasswordLogin && !isAdminLogin) {
            val passwordRegex = Regex("^(?=.*[a-zA-Z])(?=.*\\d).{6,20}$")
            if (!passwordRegex.matches(userPassword)) {
                scope.launch { snackbarHostState.showSnackbar("密码格式为6~20位，必须包含数字和字母") }
                return
            }
        }
        if (!isPasswordLogin && contact.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar("请输入手机号/邮箱") }
            return
        }

        val request = if (isPasswordLogin) {
            LoginRequest(
                userId = userID,
                loginType = 0,
                userPassword = userPassword,
                contact = "",
                verifyCode = "",
                captchaId = captchaId,
                captchaCode = captchaCode
            )
        } else {
            LoginRequest(
                userId = "",
                loginType = 1,
                userPassword = "",
                contact = contact,
                verifyCode = verifyCode,
                captchaId = captchaId,
                captchaCode = captchaCode
            )
        }

        scope.launch {
            isLoading = true
            loadingText = "正在登录..."
            try {
                val response = RetrofitClient.instance.login(request)
                isLoading = false
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.code == 200) {
                        SessionManager.token = body.data
                        SessionManager.userId = if (isPasswordLogin) userID else contact
                        SessionManager.userRole = "USER"
                        snackbarHostState.showSnackbar(body.message)
                        onLoginSuccess()
                    } else {
                        snackbarHostState.showSnackbar(body?.message ?: "登录失败")
                        loadCaptcha()
                    }
                } else {
                    snackbarHostState.showSnackbar("登录失败 (${response.code()})")
                    loadCaptcha()
                }
            } catch (_: Exception) {
                isLoading = false
                snackbarHostState.showSnackbar("网络错误，请稍后重试")
            }
        }
    }

    // 页面加载时获取验证码
    LaunchedEffect(Unit) {
        loadCaptcha()
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "社团管理",
                style = MaterialTheme.typography.headlineLarge,
                color = Indigo600,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "登录您的账户",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 登录方式 Tab
            TabRow(
                selectedTabIndex = loginTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = loginTab == 0,
                    onClick = { loginTab = 0 },
                    text = { Text("密码登录") }
                )
                Tab(
                    selected = loginTab == 1,
                    onClick = { loginTab = 1 },
                    text = { Text("验证码登录") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 密码登录表单
            if (isPasswordLogin) {
                OutlinedTextField(
                    value = userID,
                    onValueChange = { userID = it },
                    label = { Text("账号") },
                    leadingIcon = { Icon(Icons.Filled.Person, null) },
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
                        imeAction = ImeAction.Done
                    )
                )

            }

            // 验证码登录表单
            if (!isPasswordLogin) {
                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("手机号/邮箱") },
                    leadingIcon = { Icon(Icons.Filled.Phone, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
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
                        label = { Text("短信验证码") },
                        leadingIcon = { Icon(Icons.Filled.Lock, null) },
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
                        enabled = sendCodeEnabled,
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 图形验证码（管理员登录时不需要）
            if (!isAdminLogin) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = captchaCode,
                        onValueChange = { captchaCode = it },
                        label = { Text("图形验证码") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { loadCaptcha() },
                        contentAlignment = Alignment.Center
                    ) {
                        val text = captchaData?.data?.captchaText
                        if (text != null) {
                            CaptchaCanvas(text = text)
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }

            // 管理员登录勾选
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Checkbox(
                    checked = isAdminLogin,
                    onCheckedChange = { isAdminLogin = it }
                )
                TextButton(onClick = { isAdminLogin = !isAdminLogin }) {
                    Text("管理员登录")
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onNavigateForgotPassword) {
                    Text("忘记密码?")
                }
                TextButton(onClick = onNavigateRegister) {
                    Text("注册新账号")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { doLogin() },
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
                    Text(loadingText, style = MaterialTheme.typography.titleMedium)
                } else {
                    Text("登录", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun CaptchaCanvas(text: String) {
    val textMeasurer = rememberTextMeasurer()
    val baseStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)

    // 在 @Composable 上下文中预先测量所有字符
    data class CharMeasure(val result: androidx.compose.ui.text.TextLayoutResult,
                          val width: Float,
                          val height: Float,
                          val color: Color,
                          val rotation: Float,
                          val offsetY: Float)

    val charMeasures = remember(text) {
        val rng = kotlin.random.Random(text.hashCode())
        text.map { char ->
            val r = rng.nextInt(256)
            val g = rng.nextInt(256)
            val b = rng.nextInt(256)
            val charColor = Color(r, g, b, 0xCC)
            val result = textMeasurer.measure(
                text = char.toString(),
                style = baseStyle.copy(color = charColor)
            )
            CharMeasure(
                result = result,
                width = result.size.width.toFloat(),
                height = result.size.height.toFloat(),
                color = charColor,
                rotation = rng.nextFloat() * 30f - 15f,
                offsetY = rng.nextFloat() * 6f - 3f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val rng = kotlin.random.Random(text.hashCode())

        // 背景
        drawRect(color = Color(0xFFE8ECF4))

        // 干扰点
        repeat(60) {
            drawCircle(
                color = Color(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256), 0x88),
                radius = rng.nextFloat() * 2.5f + 1f,
                center = Offset(rng.nextFloat() * w, rng.nextFloat() * h)
            )
        }

        // 干扰线
        repeat(3) {
            val sx = rng.nextFloat() * w
            val sy = rng.nextFloat() * h
            drawLine(
                color = Color(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256), 0x66),
                start = Offset(sx, sy),
                end = Offset(
                    sx + rng.nextFloat() * w * 0.6f - w * 0.3f,
                    sy + rng.nextFloat() * h * 0.6f - h * 0.3f
                ),
                strokeWidth = rng.nextFloat() * 2f + 0.5f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(rng.nextFloat() * 8f + 4f, rng.nextFloat() * 6f + 2f)
                )
            )
        }

        // 居中绘制字符
        val totalWidth = charMeasures.sumOf { it.width.toDouble() }.toFloat()
        val maxHeight = charMeasures.maxOf { it.height }
        var charX = (w - totalWidth) / 2f
        val textY = (h + maxHeight) / 2f

        for (cm in charMeasures) {
            rotate(degrees = cm.rotation, pivot = Offset(charX + cm.width / 2f, textY - cm.height / 2f)) {
                drawText(
                    textLayoutResult = cm.result,
                    color = cm.color,
                    topLeft = Offset(charX, textY - cm.height + cm.offsetY)
                )
            }
            charX += cm.width
        }
    }
}

package com.clubmgmt.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.clubmgmt.app.data.api.LeaveClubRequest
import com.clubmgmt.app.data.api.RetrofitClient
import com.clubmgmt.app.data.api.SubmitRatingRequest
import com.clubmgmt.app.data.toClub
import com.clubmgmt.app.ui.components.StarRating
import com.clubmgmt.app.ui.theme.Indigo600
import kotlinx.coroutines.launch

@Composable
fun ClubDetailScreen(
    clubId: String,
    onBack: () -> Unit
) {
    var club by remember { mutableStateOf<com.clubmgmt.app.data.Club?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var currentRating by remember { mutableIntStateOf(0) }
    var isSubmittingRating by remember { mutableStateOf(false) }
    var isJoining by remember { mutableStateOf(false) }
    var averageScore by remember { mutableStateOf(0.0) }
    var ratingCount by remember { mutableStateOf(0) }
    var isMember by remember { mutableStateOf(false) }
    var isLeaving by remember { mutableStateOf(false) }

    LaunchedEffect(clubId) {
        try {
            val resp = RetrofitClient.instance.getClubDetail(clubId)
            if (resp.isSuccessful) {
                val body = resp.body()
                if (body != null && body.code == 200) {
                    club = body.data?.toClub()
                }
            }
            // 检查当前用户是否已是该社团成员
            val appResp = RetrofitClient.instance.getMyApplications()
            if (appResp.isSuccessful) {
                val appBody = appResp.body()
                if (appBody != null && appBody.code == 200) {
                    val match = appBody.data?.find { it.clubId == clubId }
                    if (match != null && match.reviewState == "通过") {
                        isMember = true
                    }
                }
            }
            // 获取平均分
            val avgResp = RetrofitClient.instance.getAverageScores()
            if (avgResp.isSuccessful) {
                val avgBody = avgResp.body()
                if (avgBody != null && avgBody.code == 200) {
                    val match = avgBody.data?.find { it.clubId == clubId }
                    if (match != null) {
                        averageScore = match.averageScore ?: 0.0
                        ratingCount = match.ratingCount ?: 0
                    }
                }
            }
            // 获取当前用户的评分
            val myRatingResp = RetrofitClient.instance.getMyRatingForClub(clubId)
            if (myRatingResp.isSuccessful) {
                val myRatingBody = myRatingResp.body()
                if (myRatingBody != null && myRatingBody.code == 200) {
                    val data = myRatingBody.data
                    if (data != null && data.rating != null) {
                        currentRating = data.rating.toIntOrNull() ?: 0
                    }
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

    if (club == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("未找到该社团。")
        }
        return
    }

    val safeClub = club!!

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
            // 社团名称和学校
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = safeClub.clubName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (safeClub.school.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = safeClub.school,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // 关于社团
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("关于社团", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(safeClub.clubInformation.orEmpty(), style = MaterialTheme.typography.bodyMedium)
                }
            }

            // 社团状态
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Person, null, tint = Indigo600)
                    Spacer(Modifier.width(8.dp))
                    Text("状态: ", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = when (safeClub.clubState) {
                            "已通过" -> "已成立"
                            "待审核" -> "审核中"
                            "未通过" -> "已拒绝"
                            "已解散" -> "已解散"
                            else -> safeClub.clubState
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (safeClub.clubState) {
                            "已通过" -> Color(0xFF22C55E)
                            "待审核" -> Color(0xFFF59E0B)
                            "未通过", "已解散" -> Color(0xFFEF4444)
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            // 平均分展示
            if (ratingCount > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("社团评分", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        StarRating(
                            rating = averageScore.toFloat(),
                            onRatingChange = {},
                            starSize = 28.dp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "%.1f 分（%d 人评价）".format(averageScore, ratingCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // 用户评分
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (currentRating > 0) "更新您的评分" else "评价此社团",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    StarRating(
                        rating = currentRating.toFloat(),
                        onRatingChange = { currentRating = it },
                        starSize = 32.dp
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (currentRating > 0) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        isSubmittingRating = true
                                        try {
                                            val resp = RetrofitClient.instance.submitRating(
                                                SubmitRatingRequest(clubId = clubId, rating = currentRating.toString())
                                            )
                                            if (resp.isSuccessful) {
                                                val body = resp.body()
                                                if (body != null && body.code == 200) {
                                                    snackbarHostState.showSnackbar("评分成功")
                                                } else {
                                                    snackbarHostState.showSnackbar(body?.message ?: "评分失败")
                                                }
                                            } else {
                                                snackbarHostState.showSnackbar("评分失败")
                                            }
                                        } catch (_: Exception) {
                                            snackbarHostState.showSnackbar("网络错误")
                                        } finally {
                                            isSubmittingRating = false
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                enabled = !isSubmittingRating
                            ) {
                                Text("提交评分")
                            }
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        try {
                                            val resp = RetrofitClient.instance.cancelRating(clubId)
                                            if (resp.isSuccessful) {
                                                currentRating = 0
                                                snackbarHostState.showSnackbar("已取消评分")
                                            } else {
                                                snackbarHostState.showSnackbar("取消评分失败")
                                            }
                                        } catch (_: Exception) {
                                            snackbarHostState.showSnackbar("网络错误")
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                            ) {
                                Text("取消评分")
                            }
                        }
                    }
                }
            }

            // 根据成员状态显示退出/加入按钮
            if (isMember) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isLeaving = true
                            try {
                                val resp = RetrofitClient.instance.leaveClub(LeaveClubRequest(clubId = clubId))
                                if (resp.isSuccessful) {
                                    isMember = false
                                    snackbarHostState.showSnackbar("已退出社团")
                                } else {
                                    snackbarHostState.showSnackbar("退出失败")
                                }
                            } catch (_: Exception) {
                                snackbarHostState.showSnackbar("网络错误")
                            } finally {
                                isLeaving = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    enabled = !isLeaving
                ) {
                    if (isLeaving) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color(0xFFEF4444))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("退出社团")
                }
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            isJoining = true
                            try {
                                val resp = RetrofitClient.instance.applyJoinClub(
                                    com.clubmgmt.app.data.api.JoinClubRequest(clubId = clubId)
                                )
                                if (resp.isSuccessful) {
                                    val body = resp.body()
                                    if (body != null && body.code == 200) {
                                        snackbarHostState.showSnackbar("入社申请已提交，请等待审核")
                                    } else {
                                        snackbarHostState.showSnackbar(body?.message ?: "申请失败")
                                    }
                                } else {
                                    snackbarHostState.showSnackbar("申请失败")
                                }
                            } catch (_: Exception) {
                                snackbarHostState.showSnackbar("网络错误")
                            } finally {
                                isJoining = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                    enabled = !isJoining
                ) {
                    if (isJoining) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("申请加入社团")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

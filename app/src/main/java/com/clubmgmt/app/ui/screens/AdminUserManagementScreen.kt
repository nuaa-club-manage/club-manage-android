package com.clubmgmt.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clubmgmt.app.data.api.AdminUserSearchRequest
import com.clubmgmt.app.data.api.RetrofitClient
import com.clubmgmt.app.data.api.UserInfoData
import kotlinx.coroutines.launch

@Composable
fun AdminUserManagementScreen(
    onUserClick: (String) -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    var userList by remember { mutableStateOf<List<UserInfoData>>(emptyList()) }
    var total by remember { mutableIntStateOf(0) }
    var currentPage by remember { mutableIntStateOf(1) }
    var totalPages by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun doSearch(page: Int) {
        scope.launch {
            isLoading = true
            hasSearched = true
            try {
                val response = RetrofitClient.instance.adminSearchUsers(
                    AdminUserSearchRequest(
                        pageNo = page,
                        pageSize = 10,
                        search = searchText.trim().ifBlank { null }
                    )
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.code == 200) {
                        val data = body.data
                        if (data != null) {
                            userList = data.records
                            total = data.total
                            currentPage = data.current
                            totalPages = data.pages
                        }
                    } else {
                        snackbarHostState.showSnackbar(body?.message ?: "查询失败")
                    }
                } else {
                    snackbarHostState.showSnackbar("查询失败 (${response.code()})")
                }
            } catch (_: Exception) {
                snackbarHostState.showSnackbar("网络错误，请稍后重试")
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("用户管理", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("搜索账号/姓名/手机号") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { searchText = ""; doSearch(1) }) {
                                Text("✕", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                )
                IconButton(
                    onClick = { doSearch(1) },
                    enabled = !isLoading
                ) {
                    Icon(Icons.Filled.Search, contentDescription = "搜索", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(12.dp))

            if (hasSearched && !isLoading) {
                Text(
                    "共找到 $total 条记录",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(8.dp))
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (!hasSearched) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("请输入关键词搜索用户", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            } else if (userList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("没有找到匹配的用户", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(userList, key = { it.userId }) { user ->
                        UserInfoCard(user = user, onClick = { onUserClick(user.userId) })
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (currentPage > 1) doSearch(currentPage - 1) },
                        enabled = currentPage > 1 && !isLoading
                    ) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "上一页")
                    }

                    Text(
                        "第 $currentPage / $totalPages 页",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    IconButton(
                        onClick = { if (currentPage < totalPages) doSearch(currentPage + 1) },
                        enabled = currentPage < totalPages && !isLoading
                    ) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "下一页")
                    }
                }
            }
        }
    }
}

@Composable
private fun UserInfoCard(user: UserInfoData, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(user.realName.orEmpty().ifBlank { user.userName }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(user.gender.orEmpty(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text("账号: ${user.userId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text("手机: ${user.phoneNumber.orEmpty()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text("邮箱: ${user.userMailbox.orEmpty()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            if (!user.school.orEmpty().isBlank() || !user.degree.orEmpty().isBlank()) {
                Text("${user.school.orEmpty()} · ${user.degree.orEmpty()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Text("注册时间: ${user.registerTime ?: ""}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}

package com.clubmgmt.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clubmgmt.app.data.api.ClubData
import com.clubmgmt.app.data.api.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun AdminClubQueryScreen(
    onClubClick: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var clubList by remember { mutableStateOf<List<ClubData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun doSearch() {
        scope.launch {
            isLoading = true
            hasSearched = true
            try {
                val resp = RetrofitClient.instance.getClubList(searchText.trim().ifBlank { null })
                if (resp.isSuccessful) {
                    val body = resp.body()
                    if (body != null && body.code == 200) {
                        clubList = body.data ?: emptyList()
                    }
                }
            } catch (_: Exception) {
                snackbarHostState.showSnackbar("网络错误")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        doSearch()
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("社团查询", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("输入社团名称模糊搜索") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { searchText = ""; clubList = emptyList(); hasSearched = false }) {
                                Text("✕", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                )
                IconButton(onClick = { doSearch() }, enabled = !isLoading) {
                    Icon(Icons.Filled.Search, contentDescription = "搜索", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(12.dp))

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (!hasSearched) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("请输入社团名称进行模糊搜索", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            } else if (clubList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("没有找到匹配的社团", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            } else {
                Text("共找到 ${clubList.size} 个社团", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(clubList, key = { it.clubId }) { club ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onClubClick(club.clubId) },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(club.clubName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    if (!club.school.isNullOrBlank()) {
                                        Text(club.school, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        when (club.clubState) {
                                            "已通过" -> "已成立"
                                            "待审核" -> "审核中"
                                            "未通过" -> "已拒绝"
                                            "已解散" -> "已解散"
                                            else -> club.clubState ?: ""
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

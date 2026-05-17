package com.clubmgmt.app.ui.screens

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
import androidx.compose.ui.unit.dp
import com.clubmgmt.app.data.api.RetrofitClient
import com.clubmgmt.app.data.toClub
import com.clubmgmt.app.ui.components.ClubCard
import kotlinx.coroutines.launch

@Composable
fun ClubListScreen(
    onClubClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var clubs by remember { mutableStateOf<List<com.clubmgmt.app.data.Club>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val resp = RetrofitClient.instance.getClubList()
            if (resp.isSuccessful) {
                val body = resp.body()
                if (body != null && body.code == 200) {
                    clubs = body.data?.map { it.toClub() } ?: emptyList()
                }
            }
        } catch (_: Exception) {
            scope.launch { snackbarHostState.showSnackbar("网络错误") }
        } finally {
            isLoading = false
        }
    }

    val filteredClubs = remember(searchQuery, clubs) {
        clubs?.filter { club ->
            searchQuery.isEmpty() ||
            club.clubName.contains(searchQuery, ignoreCase = true) ||
            club.clubInformation?.contains(searchQuery, ignoreCase = true) ?: false
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "发现我们的社团",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "浏览各种类型的社团，找到最适合您的一个。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("搜索社团...") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (filteredClubs.isNullOrEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        if (searchQuery.isNotEmpty()) "未找到匹配的社团" else "暂无可用社团",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            items(filteredClubs, key = { it.clubId }) { club ->
                ClubCard(club = club, onClick = { onClubClick(club.clubId) })
            }
        }
    }
}

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
import com.clubmgmt.app.data.toActivity
import com.clubmgmt.app.ui.components.ActivityCard
import kotlinx.coroutines.launch

@Composable
fun ActivityListScreen(
    onActivityClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var activities by remember { mutableStateOf<List<com.clubmgmt.app.data.Activity>>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val resp = RetrofitClient.instance.getActivities()
            if (resp.isSuccessful) {
                val body = resp.body()
                if (body != null && body.code == 200) {
                    activities = body.data?.map { it.toActivity() } ?: emptyList()
                }
            }
        } catch (_: Exception) {
            scope.launch { snackbarHostState.showSnackbar("网络错误") }
        } finally {
            isLoading = false
        }
    }

    val filtered = remember(searchQuery, activities) {
        activities?.filter { activity ->
            searchQuery.isEmpty() ||
            activity.title.contains(searchQuery, ignoreCase = true) ||
            activity.clubName.contains(searchQuery, ignoreCase = true)
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
            Text(text = "近期活动", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "不要错过即将举行的精彩活动。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("搜索活动...") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (filtered.isNullOrEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        if (searchQuery.isNotEmpty()) "未找到匹配的活动" else "暂无可用活动",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            items(filtered, key = { it.activityId }) { activity ->
                ActivityCard(activity = activity, onClick = { onActivityClick(activity.activityId) })
            }
        }
    }
}

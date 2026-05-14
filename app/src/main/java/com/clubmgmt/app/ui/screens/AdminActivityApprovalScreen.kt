package com.clubmgmt.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.clubmgmt.app.data.api.ActivityData
import com.clubmgmt.app.data.api.AuditActivityRequest
import com.clubmgmt.app.data.api.RetrofitClient
import com.clubmgmt.app.ui.theme.Green500
import com.clubmgmt.app.ui.theme.Red500
import kotlinx.coroutines.launch

@Composable
fun AdminActivityApprovalScreen() {
    var searchText by remember { mutableStateOf("") }
    var pendingActivities by remember { mutableStateOf<List<ActivityData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun loadPendingActivities() {
        scope.launch {
            isLoading = true
            try {
                val resp = RetrofitClient.instance.getPendingActivities()
                if (resp.isSuccessful) {
                    val body = resp.body()
                    if (body != null && body.code == 200) {
                        pendingActivities = body.data?.filter { it.activityState == "待审核" } ?: emptyList()
                    }
                }
            } catch (_: Exception) { }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadPendingActivities() }

    val filteredActivities = remember(searchText, pendingActivities) {
        if (searchText.isBlank()) pendingActivities
        else pendingActivities.filter {
            it.title.contains(searchText, ignoreCase = true) ||
            it.clubName.contains(searchText, ignoreCase = true)
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("活动发布审核", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("搜索活动标题或社团名称") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { searchText = "" }) {
                            Text("✕", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        Icon(Icons.Filled.Search, contentDescription = "搜索")
                    }
                }
            )
            Spacer(Modifier.height(12.dp))

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredActivities.isEmpty()) {
                Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (searchText.isNotEmpty()) "没有找到匹配的活动" else "当前没有待审核的活动。",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredActivities, key = { it.activityId }) { activity ->
                        PendingActivityCard(
                            activity = activity,
                            onApprove = {
                                scope.launch {
                                    try {
                                        val resp = RetrofitClient.instance.auditActivity(
                                            AuditActivityRequest(activityId = activity.activityId, pass = true)
                                        )
                                        if (resp.isSuccessful) {
                                            pendingActivities = pendingActivities.filter { it.activityId != activity.activityId }
                                            snackbarHostState.showSnackbar("已批准: ${activity.title}")
                                        }
                                    } catch (_: Exception) { }
                                }
                            },
                            onReject = {
                                scope.launch {
                                    try {
                                        val resp = RetrofitClient.instance.auditActivity(
                                            AuditActivityRequest(activityId = activity.activityId, pass = false)
                                        )
                                        if (resp.isSuccessful) {
                                            pendingActivities = pendingActivities.filter { it.activityId != activity.activityId }
                                            snackbarHostState.showSnackbar("已拒绝: ${activity.title}")
                                        }
                                    } catch (_: Exception) { }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingActivityCard(activity: ActivityData, onApprove: () -> Unit, onReject: () -> Unit) {
    Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(activity.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(activity.clubName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(activity.content, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(activity.location, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onReject, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Red500)) {
                    Icon(Icons.Filled.Close, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("拒绝")
                }
                Spacer(Modifier.width(12.dp))
                Button(onClick = onApprove, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Green500)) {
                    Icon(Icons.Filled.Check, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("批准")
                }
            }
        }
    }
}

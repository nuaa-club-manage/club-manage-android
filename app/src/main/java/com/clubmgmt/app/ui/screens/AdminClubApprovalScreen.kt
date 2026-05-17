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
import com.clubmgmt.app.data.api.AuditClubRequest
import com.clubmgmt.app.data.api.ClubData
import com.clubmgmt.app.data.api.RetrofitClient
import com.clubmgmt.app.ui.theme.Green500
import com.clubmgmt.app.ui.theme.Red500
import kotlinx.coroutines.launch

@Composable
fun AdminClubApprovalScreen() {
    var searchText by remember { mutableStateOf("") }
    var pendingClubs by remember { mutableStateOf<List<ClubData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun loadPendingClubs() {
        scope.launch {
            isLoading = true
            try {
                val resp = RetrofitClient.instance.getPendingClubs()
                if (resp.isSuccessful) {
                    val body = resp.body()
                    if (body != null && body.code == 200) {
                        pendingClubs = body.data?.filter { it.clubState == "待审核" } ?: emptyList()
                    }
                }
            } catch (_: Exception) { }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadPendingClubs() }

    val filteredClubs = remember(searchText, pendingClubs) {
        if (searchText.isBlank()) pendingClubs
        else pendingClubs.filter {
            it.clubName.contains(searchText, ignoreCase = true) ||
            it.clubInformation?.contains(searchText, ignoreCase = true) ?: false
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("社团成立审核", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("搜索社团名称") },
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
            } else if (filteredClubs.isEmpty()) {
                Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (searchText.isNotEmpty()) "没有找到匹配的社团" else "当前没有待审核的社团。",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredClubs, key = { it.clubId }) { club ->
                        PendingClubCard(
                            club = club,
                            onApprove = {
                                scope.launch {
                                    try {
                                        val resp = RetrofitClient.instance.auditClub(
                                            AuditClubRequest(clubId = club.clubId, pass = true)
                                        )
                                        if (resp.isSuccessful) {
                                            pendingClubs = pendingClubs.filter { it.clubId != club.clubId }
                                            snackbarHostState.showSnackbar("已批准: ${club.clubName}")
                                        }
                                    } catch (_: Exception) { }
                                }
                            },
                            onReject = {
                                scope.launch {
                                    try {
                                        val resp = RetrofitClient.instance.auditClub(
                                            AuditClubRequest(clubId = club.clubId, pass = false)
                                        )
                                        if (resp.isSuccessful) {
                                            pendingClubs = pendingClubs.filter { it.clubId != club.clubId }
                                            snackbarHostState.showSnackbar("已拒绝: ${club.clubName}")
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
private fun PendingClubCard(club: ClubData, onApprove: () -> Unit, onReject: () -> Unit) {
    Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(club.clubName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (club.school != null) {
                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(club.school, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(club.clubInformation.orEmpty(), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 2, overflow = TextOverflow.Ellipsis)
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

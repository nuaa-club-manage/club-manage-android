package com.clubmgmt.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.clubmgmt.app.data.mockActivities
import com.clubmgmt.app.data.mockClubs
import com.clubmgmt.app.ui.components.ActivityCard
import com.clubmgmt.app.ui.components.FilterChipRow

@Composable
fun ActivityListScreen(
    onActivityClick: (Int) -> Unit
) {
    val clubNames = remember { mockClubs.map { it.name }.distinct() }
    var searchQuery by remember { mutableStateOf("") }
    var selectedClub by remember { mutableStateOf("所有社团") }

    val filtered = remember(searchQuery, selectedClub) {
        mockActivities.filter { activity ->
            (selectedClub == "所有社团" || activity.club == selectedClub) &&
            (searchQuery.isEmpty() || activity.title.contains(searchQuery, ignoreCase = true))
        }
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("搜索活动...") },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChipRow(
                    options = listOf("所有社团") + clubNames,
                    selected = selectedClub,
                    onSelected = { selectedClub = it }
                )
            }
        }

        items(filtered, key = { it.id }) { activity ->
            ActivityCard(activity = activity, onClick = { onActivityClick(activity.id) })
        }
    }
}

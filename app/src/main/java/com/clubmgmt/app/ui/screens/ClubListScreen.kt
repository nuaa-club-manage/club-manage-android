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
import com.clubmgmt.app.data.mockClubs
import com.clubmgmt.app.ui.components.ClubCard
import com.clubmgmt.app.ui.components.FilterChipRow

@Composable
fun ClubListScreen(
    onClubClick: (Int) -> Unit
) {
    val categories = remember { mockClubs.map { it.category }.distinct() }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("所有分类") }

    val filteredClubs = remember(searchQuery, selectedCategory) {
        mockClubs.filter { club ->
            (selectedCategory == "所有分类" || club.category == selectedCategory) &&
            (searchQuery.isEmpty() || club.name.contains(searchQuery, ignoreCase = true))
        }
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

        // Search & Filter
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("搜索社团...") },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChipRow(
                    options = listOf("所有分类") + categories,
                    selected = selectedCategory,
                    onSelected = { selectedCategory = it }
                )
            }
        }

        items(filteredClubs, key = { it.id }) { club ->
            ClubCard(club = club, onClick = { onClubClick(club.id) })
        }
    }
}

package com.clubmgmt.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AdminDashboardScreen(
    onNavigateUsers: () -> Unit,
    onNavigateClubs: () -> Unit,
    onNavigateActivities: () -> Unit,
    onNavigateClubQuery: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "管理菜单",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(
                title = "用户管理",
                icon = Icons.Filled.People,
                color = Color(0xFFA855F7),
                onClick = onNavigateUsers,
                modifier = Modifier.fillMaxWidth()
            )
            StatCard(
                title = "社团审核",
                icon = Icons.Filled.Groups,
                color = Color(0xFF3B82F6),
                onClick = onNavigateClubs,
                modifier = Modifier.fillMaxWidth()
            )
            StatCard(
                title = "社团查询",
                icon = Icons.Filled.Search,
                color = Color(0xFFF59E0B),
                onClick = onNavigateClubQuery,
                modifier = Modifier.fillMaxWidth()
            )
            StatCard(
                title = "活动审核",
                icon = Icons.Filled.CalendarToday,
                color = Color(0xFF22C55E),
                onClick = onNavigateActivities,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = color
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.padding(10.dp),
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "前往管理",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

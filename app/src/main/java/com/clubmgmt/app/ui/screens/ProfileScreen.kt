package com.clubmgmt.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.clubmgmt.app.data.*
import com.clubmgmt.app.ui.components.StarRating

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onCreateClub: () -> Unit,
    onPublishActivity: () -> Unit,
    onClubClick: (Int) -> Unit,
    onManageClub: (Int) -> Unit,
    onActivityClick: (Int) -> Unit,
    onEnterAdmin: () -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("我加入的社团", "我创建的社团", "我报名的活动", "我发布的活动", "我的评分")

    val userJoinedClubs = remember { mockClubs.filter { mockUser.joinedClubs.contains(it.id) } }
    val userManagedClubs = remember { mockClubs.filter { mockUser.managedClubs.contains(it.id) } }
    val userRegisteredActivities = remember { mockActivities.filter { mockUser.registeredActivities.contains(it.id) } }
    val userPublishedActivities = remember { mockActivities.filter { mockUser.managedClubs.contains(it.clubId) } }
    val userRatings = remember {
        mockUser.ratings.mapNotNull { rating ->
            mockClubs.find { it.id == rating.clubId }?.let { club ->
                Triple(rating.clubId, rating.score, club)
            }
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile header
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = mockUser.avatarUrl,
                        contentDescription = mockUser.name,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(40.dp))
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(mockUser.name, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        mockUser.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onEditProfile,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("编辑资料")
                        }
                        Button(
                            onClick = onCreateClub,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Filled.AddCircle, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("成立社团")
                        }
                    }
                    if (mockUser.managedClubs.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = onPublishActivity,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Filled.AddCircle, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("发布活动")
                        }
                    }
                    if (mockUser.role == UserRole.ADMIN) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onEnterAdmin,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.Settings, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("管理后台")
                        }
                    }
                }
            }
        }

        // Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }
        }

        // Tab content
        when (activeTab) {
            0 -> { // Joined clubs
                items(userJoinedClubs, key = { it.id }) { club ->
                    ProfileClubCard(club, onClubClick, null)
                }
            }
            1 -> { // Managed clubs
                items(userManagedClubs, key = { it.id }) { club ->
                    ProfileClubCard(club, onClubClick, onManageClub)
                }
            }
            2 -> { // Registered activities
                items(userRegisteredActivities, key = { it.id }) { activity ->
                    ProfileActivityCard(activity, onActivityClick, "取消")
                }
            }
            3 -> { // Published activities
                if (userPublishedActivities.isEmpty()) {
                    item { Text("您还没有发布任何活动。", style = MaterialTheme.typography.bodyMedium) }
                } else {
                    items(userPublishedActivities, key = { it.id }) { activity ->
                        ProfileActivityCard(activity, onActivityClick, "取消")
                    }
                }
            }
            4 -> { // Ratings
                items(userRatings, key = { it.first }) { (clubId, score, club) ->
                    Card(shape = RoundedCornerShape(12.dp)) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = club.imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(club.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                StarRating(rating = score.toFloat(), readOnly = true)
                            }
                            TextButton(onClick = {}) { Text("取消评分") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileClubCard(
    club: Club,
    onClubClick: (Int) -> Unit,
    onManageClub: ((Int) -> Unit)?
) {
    Card(
        onClick = { onClubClick(club.id) },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row {
                AsyncImage(
                    model = club.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(club.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        club.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (onManageClub != null) {
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = { onManageClub(club.id) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Filled.Settings, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("管理社团")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileActivityCard(
    activity: Activity,
    onActivityClick: (Int) -> Unit,
    actionLabel: String
) {
    Card(
        onClick = { onActivityClick(activity.id) },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = activity.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(activity.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(activity.club, style = MaterialTheme.typography.bodySmall)
                Text("${activity.date} ${activity.time}", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = {}) { Text(actionLabel) }
        }
    }
}

package com.clubmgmt.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.clubmgmt.app.data.mockClubs
import com.clubmgmt.app.data.mockUser
import com.clubmgmt.app.ui.components.ApplicationDialog
import com.clubmgmt.app.ui.components.StarRating

@Composable
fun ClubDetailScreen(
    clubId: Int,
    onBack: () -> Unit,
    onManageClub: (Int) -> Unit
) {
    val club = mockClubs.find { it.id == clubId }

    if (club == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("未找到该社团。")
        }
        return
    }

    val userRating = remember { mockUser.ratings.find { it.clubId == club.id }?.score ?: 0 }
    var currentRating by remember { mutableIntStateOf(userRating) }
    var isMember by remember { mutableStateOf(mockUser.joinedClubs.contains(club.id)) }
    val isManager = remember { mockUser.managedClubs.contains(club.id) }
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top bar with back
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Text("← 返回", color = MaterialTheme.colorScheme.primary)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Hero image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = club.imageUrl,
                    contentDescription = club.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        ))
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF4F46E5)
                    ) {
                        Text(
                            text = club.category,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = club.name,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // About section
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("关于社团", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(club.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // Stats card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.People, null, tint = Color(0xFF4F46E5))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text("${club.memberCount}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text("成员", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                StarRating(rating = club.rating.score, readOnly = true)
                                Text("${club.rating.score} (${club.rating.count} 评价)", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Rating section
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (currentRating > 0) "更新您的评分" else "评价此社团",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        StarRating(
                            rating = currentRating.toFloat(),
                            onRatingChange = { currentRating = it },
                            starSize = 32.dp
                        )
                        if (currentRating > 0) {
                            Text(
                                "您给此社团的评分为 $currentRating / 5 星。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // Members section (if manager)
                if (isManager && club.members.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("社团成员 (${club.members.size})", style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                club.members.forEach { member ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        AsyncImage(
                                            model = member.avatarUrl,
                                            contentDescription = member.name,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(24.dp))
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(member.name, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }

                // Action buttons
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isManager) {
                        Button(
                            onClick = { onManageClub(club.id) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151))
                        ) {
                            Icon(Icons.Filled.Settings, null)
                            Spacer(Modifier.width(8.dp))
                            Text("管理社团")
                        }
                    }

                    Button(
                        onClick = {
                            if (isMember) {
                                isMember = false
                            } else {
                                showDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMember) Color(0xFFEF4444) else Color(0xFF4F46E5)
                        )
                    ) {
                        Text(if (isMember) "退出社团" else "加入社团")
                    }
                }
            }
        }
    }

    ApplicationDialog(
        isOpen = showDialog,
        onClose = { showDialog = false },
        onSubmit = { _, _, _ ->
            isMember = true
            showDialog = false
        },
        title = "申请加入 ${club.name}"
    )
}

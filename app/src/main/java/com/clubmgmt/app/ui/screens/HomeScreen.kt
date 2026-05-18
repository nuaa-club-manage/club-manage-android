package com.clubmgmt.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clubmgmt.app.data.api.RetrofitClient
import com.clubmgmt.app.data.toActivity
import com.clubmgmt.app.data.toClub
import com.clubmgmt.app.ui.components.ActivityCard
import com.clubmgmt.app.ui.components.ClubCard

@Composable
fun HomeScreen(
    onClubClick: (String) -> Unit,
    onActivityClick: (String) -> Unit,
    onViewAllClubs: () -> Unit,
    onViewAllActivities: () -> Unit
) {
    var featuredClubs by remember { mutableStateOf<List<com.clubmgmt.app.data.Club>>(emptyList()) }
    var featuredActivities by remember { mutableStateOf<List<com.clubmgmt.app.data.Activity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val clubResp = RetrofitClient.instance.getClubList()
            val activityResp = RetrofitClient.instance.getActivities()
            if (clubResp.isSuccessful) {
                val body = clubResp.body()
                if (body != null && body.code == 200) {
                    featuredClubs = body.data?.map { it.toClub() }?.take(4) ?: emptyList()
                }
            }
            if (activityResp.isSuccessful) {
                val body = activityResp.body()
                if (body != null && body.code == 200) {
                    featuredActivities = body.data?.map { it.toActivity() }?.take(3) ?: emptyList()
                }
            }
        } catch (_: Exception) { }
        finally { isLoading = false }
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center
            ) {
                // 背景图 + 暗色遮罩
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = com.clubmgmt.app.R.drawable.bg_community),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind { drawRect(Color.Black.copy(alpha = 0.5f)) }
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "发现你的社群",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "发现、加入并参与符合您热情的社团和活动。\n您的下一次冒险从这里开始。",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onViewAllClubs,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("立刻探索社团", modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }

        item {
            SectionHeader(title = "精选社团", onViewAll = onViewAllClubs)
        }
        item {
            if (isLoading) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featuredClubs, key = { it.clubId }) { club ->
                        ClubCard(
                            club = club,
                            onClick = { onClubClick(club.clubId) },
                            modifier = Modifier.width(260.dp)
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(title = "近期活动", onViewAll = onViewAllActivities)
        }
        item {
            if (isLoading) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featuredActivities, key = { it.activityId }) { activity ->
                        ActivityCard(
                            activity = activity,
                            onClick = { onActivityClick(activity.activityId) },
                            modifier = Modifier.width(280.dp)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun SectionHeader(title: String, onViewAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
        TextButton(onClick = onViewAll) {
            Text("查看全部")
        }
    }
}

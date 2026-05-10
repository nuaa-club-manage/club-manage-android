package com.clubmgmt.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.clubmgmt.app.data.mockClubs
import com.clubmgmt.app.data.mockUser
import com.clubmgmt.app.ui.theme.Indigo600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishActivityScreen(
    onBack: () -> Unit
) {
    val managedClubs = remember { mockClubs.filter { mockUser.managedClubs.contains(it.id) } }
    var title by remember { mutableStateOf("") }
    var selectedClubId by remember { mutableIntStateOf(managedClubs.firstOrNull()?.id ?: 0) }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    if (managedClubs.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("您没有管理任何社团", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("您需要先创建一个社团才能发布活动。")
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBack) { Text("返回个人中心") }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Text("← 返回", color = Indigo600)
            }
            Spacer(Modifier.width(8.dp))
            Text("发布新活动", style = MaterialTheme.typography.headlineMedium)
        }

        Spacer(Modifier.height(24.dp))

        Card(shape = RoundedCornerShape(12.dp)) {
            Column(
                Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("活动标题") },
                    placeholder = { Text("例如：年度编程马拉松") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = managedClubs.find { it.id == selectedClubId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("选择社团") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        managedClubs.forEach { club ->
                            DropdownMenuItem(
                                text = { Text(club.name) },
                                onClick = {
                                    selectedClubId = club.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = date, onValueChange = { date = it },
                        label = { Text("日期") },
                        placeholder = { Text("2024-09-01") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = time, onValueChange = { time = it },
                        label = { Text("时间") },
                        placeholder = { Text("18:00") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                OutlinedTextField(
                    value = location, onValueChange = { location = it },
                    label = { Text("地点") },
                    placeholder = { Text("例如：将军路校区教学楼 A1-201") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("活动描述") },
                    placeholder = { Text("详细介绍您的活动内容。") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = imageUrl, onValueChange = { imageUrl = it },
                    label = { Text("活动宣传图片 URL") },
                    placeholder = { Text("https://...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = onBack, shape = RoundedCornerShape(8.dp)) { Text("取消") }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = onBack,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
            ) { Text("发布活动") }
        }
    }
}

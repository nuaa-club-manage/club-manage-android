package com.clubmgmt.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ApplicationDialog(
    isOpen: Boolean,
    onClose: () -> Unit,
    onSubmit: (studentId: String, phone: String, reason: String) -> Unit,
    title: String
) {
    var studentId by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    if (isOpen) {
        AlertDialog(
            onDismissRequest = onClose,
            title = { Text(title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = studentId,
                        onValueChange = { studentId = it },
                        label = { Text("学号") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("手机号") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("申请理由") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onSubmit(studentId, phone, reason) },
                    enabled = studentId.isNotBlank() && phone.isNotBlank() && reason.isNotBlank(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("提交申请")
                }
            },
            dismissButton = {
                TextButton(onClick = onClose) {
                    Text("取消")
                }
            }
        )
    }
}

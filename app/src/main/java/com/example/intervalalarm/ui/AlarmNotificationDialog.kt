package com.example.intervalalarm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun AlarmNotificationDialog(
    isVisible: Boolean,
    currentTime: LocalTime = LocalTime.now(),
    onStopAlarm: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    // 現在時刻表示
                    Text(
                        text = currentTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // アラームラベル
                    Text(
                        text = "アラーム",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(64.dp))
                    
                    // ストップボタン
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onStopAlarm,
                            modifier = Modifier
                                .size(120.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(
                                text = "ストップ",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                }
            }
        }
    }
} 
package com.example.intervalalarm.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.intervalalarm.data.AlarmSetting
import com.example.intervalalarm.viewmodel.AlarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditScreen(
    alarm: AlarmSetting?,
    viewModel: AlarmViewModel,
    onNavigateBack: () -> Unit,
    onSelectRingtone: () -> Unit
) {
    var isVibrationEnabled by remember { mutableStateOf(alarm?.isVibrationEnabled ?: true) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (alarm == null) "新規アラーム" else "アラーム編集") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (alarm == null) {
                                // 新規作成
                                val newAlarm = AlarmSetting(
                                    isVibrationEnabled = isVibrationEnabled
                                )
                                viewModel.addAlarm(newAlarm)
                            } else {
                                // 編集
                                val updatedAlarm = alarm.copy(
                                    isVibrationEnabled = isVibrationEnabled
                                )
                                viewModel.updateAlarm(updatedAlarm)
                            }
                            onNavigateBack()
                        }
                    ) {
                        Text("保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // バイブレーション設定
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "バイブレーション",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "アラーム時に振動する",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isVibrationEnabled,
                        onCheckedChange = { isVibrationEnabled = it }
                    )
                }
            }
            
            // アラーム音選択
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSelectRingtone
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "アラーム音",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = alarm?.ringtoneUri?.let { "カスタム音" } ?: "デフォルト",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
} 
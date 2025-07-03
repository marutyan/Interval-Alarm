package com.example.intervalalarm.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.intervalalarm.data.AlarmData
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditScreen(
    onNavigateBack: () -> Unit,
    onSave: (AlarmData) -> Unit,
    alarmData: AlarmData? = null
) {
    val context = LocalContext.current
    var startTime by remember { mutableStateOf(alarmData?.startTime ?: LocalTime.of(6, 30)) }
    var endTime by remember { mutableStateOf(alarmData?.endTime ?: LocalTime.of(22, 0)) }
    var interval by remember { mutableStateOf(alarmData?.interval ?: 60) }
    var isVibrationEnabled by remember { mutableStateOf(alarmData?.isVibrationEnabled ?: true) }
    var alarmSoundUri by remember { mutableStateOf(alarmData?.alarmSoundUri ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("アラーム設定") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 開始時刻設定
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "開始時刻",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${String.format("%02d", startTime.hour)}:${String.format("%02d", startTime.minute)}",
                    style = MaterialTheme.typography.headlineMedium
                )
                // TODO: TimePicker実装
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 終了時刻設定
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "終了時刻",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${String.format("%02d", endTime.hour)}:${String.format("%02d", endTime.minute)}",
                    style = MaterialTheme.typography.headlineMedium
                )
                // TODO: TimePicker実装
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 間隔設定
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "間隔（分）",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { if (interval > 1) interval-- },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Text("-")
                    }
                    Text(
                        text = "$interval",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Button(
                        onClick = { if (interval < 1440) interval++ },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Text("+")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 15, 30, 60).forEach { presetInterval ->
                        Button(
                            onClick = { interval = presetInterval },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("${presetInterval}分")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                Text(
                    text = "バイブレーション",
                    style = MaterialTheme.typography.titleMedium
                )
                Switch(
                    checked = isVibrationEnabled,
                    onCheckedChange = { isVibrationEnabled = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // アラーム音設定
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "アラーム音",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { /* TODO: アラーム音選択 */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("アラーム音を選択")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 保存ボタン
        Button(
            onClick = {
                val newAlarmData = AlarmData(
                    id = alarmData?.id ?: "",
                    startTime = startTime,
                    endTime = endTime,
                    interval = interval,
                    isVibrationEnabled = isVibrationEnabled,
                    alarmSoundUri = alarmSoundUri,
                    isEnabled = true
                )
                onSave(newAlarmData)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存")
        }
    }
} 
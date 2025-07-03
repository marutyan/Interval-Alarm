package com.example.intervalalarm.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intervalalarm.viewmodel.TimerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    modifier: Modifier = Modifier,
    viewModel: TimerViewModel = viewModel()
) {
    val timerState by viewModel.timerState.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedHours by remember { mutableIntStateOf(0) }
    var selectedMinutes by remember { mutableIntStateOf(5) }
    var selectedSeconds by remember { mutableIntStateOf(0) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // タイトル
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "タイマー",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // タイマー表示
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = viewModel.formatTime(timerState.remainingSeconds),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (timerState.isFinished) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (timerState.totalSeconds > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { viewModel.getProgress() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // 時間設定ボタン
        if (timerState.totalSeconds == 0 || !timerState.isRunning) {
            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("時間を設定")
            }
        }
        
        // 制御ボタン
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 開始/一時停止ボタン
            Button(
                onClick = {
                    if (timerState.isRunning) {
                        viewModel.pauseTimer()
                    } else {
                        if (timerState.totalSeconds > 0) {
                            viewModel.startTimer()
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = timerState.totalSeconds > 0
            ) {
                Icon(
                    imageVector = if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (timerState.isRunning) "一時停止" else "開始")
            }
            
            // リセットボタン
            OutlinedButton(
                onClick = { viewModel.resetTimer() },
                modifier = Modifier.weight(1f),
                enabled = timerState.totalSeconds > 0
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("リセット")
            }
        }
        
        // クリアボタン
        if (timerState.totalSeconds > 0) {
            TextButton(
                onClick = { viewModel.clearTimer() }
            ) {
                Text("クリア")
            }
        }
        
        // 完了メッセージ
        if (timerState.isFinished) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "時間になりました！",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    
    // 時間選択ダイアログ
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("タイマー時間を設定") },
            text = {
                Column {
                    Text("時間", style = MaterialTheme.typography.labelMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 時間選択
                        OutlinedTextField(
                            value = selectedHours.toString(),
                            onValueChange = { 
                                selectedHours = it.toIntOrNull()?.coerceIn(0, 23) ?: 0
                            },
                            label = { Text("時") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = selectedMinutes.toString(),
                            onValueChange = { 
                                selectedMinutes = it.toIntOrNull()?.coerceIn(0, 59) ?: 0
                            },
                            label = { Text("分") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = selectedSeconds.toString(),
                            onValueChange = { 
                                selectedSeconds = it.toIntOrNull()?.coerceIn(0, 59) ?: 0
                            },
                            label = { Text("秒") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setTimer(selectedHours, selectedMinutes, selectedSeconds)
                        showTimePicker = false
                    }
                ) {
                    Text("設定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
} 
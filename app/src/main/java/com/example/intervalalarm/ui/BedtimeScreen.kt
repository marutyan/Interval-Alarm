package com.example.intervalalarm.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intervalalarm.viewmodel.BedtimeViewModel
import java.time.DayOfWeek
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BedtimeScreen(
    modifier: Modifier = Modifier,
    viewModel: BedtimeViewModel = viewModel()
) {
    val bedtimeState by viewModel.bedtimeState.collectAsState()
    var showBedtimeTimePicker by remember { mutableStateOf(false) }
    var showWakeupTimePicker by remember { mutableStateOf(false) }
    var showSleepGoalDialog by remember { mutableStateOf(false) }
    var showSleepSoundDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // タイトル
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "就寝時刻",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        
        // スケジュール有効/無効スイッチ
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "就寝スケジュール",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Switch(
                    checked = bedtimeState.schedule.isEnabled,
                    onCheckedChange = { viewModel.toggleScheduleEnabled() }
                )
            }
        }
        
        // 就寝時刻と起床時刻
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 就寝時刻
            Card(
                modifier = Modifier.weight(1f),
                onClick = { showBedtimeTimePicker = true }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                                         Icon(
                         imageVector = Icons.Default.NightsStay,
                         contentDescription = null,
                         tint = MaterialTheme.colorScheme.primary
                     )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "就寝時刻",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = viewModel.formatTime(bedtimeState.schedule.bedtime),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // 起床時刻
            Card(
                modifier = Modifier.weight(1f),
                onClick = { showWakeupTimePicker = true }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "起床時刻",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = viewModel.formatTime(bedtimeState.schedule.wakeupTime),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // 睡眠時間表示
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (viewModel.isGoalMet()) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "睡眠時間",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = viewModel.calculateSleepDuration(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (viewModel.isGoalMet()) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "目標: ${viewModel.getSleepGoalText()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // 曜日選択
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "繰り返し",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(DayOfWeek.values().toList()) { day ->
                        val isSelected = bedtimeState.schedule.selectedDays.contains(day)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val newDays = if (isSelected) {
                                    bedtimeState.schedule.selectedDays - day
                                } else {
                                    bedtimeState.schedule.selectedDays + day
                                }
                                viewModel.updateSelectedDays(newDays)
                            },
                            label = { Text(viewModel.getDayOfWeekText(day)) }
                        )
                    }
                }
            }
        }
        
        // 設定オプション
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 睡眠目標設定
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showSleepGoalDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "睡眠目標",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = viewModel.getSleepGoalText(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // おやすみモード
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
                            text = "おやすみモード",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (bedtimeState.windDownEnabled) {
                            Text(
                                text = "${viewModel.formatTime(viewModel.getWindDownStartTime())}から開始",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Switch(
                        checked = bedtimeState.windDownEnabled,
                        onCheckedChange = { viewModel.toggleWindDown() }
                    )
                }
            }
            
            // サイレントモード
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoNotDisturb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "サイレントモード",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Switch(
                        checked = bedtimeState.doNotDisturbEnabled,
                        onCheckedChange = { viewModel.toggleDoNotDisturb() }
                    )
                }
            }
            
            // 睡眠音
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showSleepSoundDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "睡眠音",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(
                        text = viewModel.getAvailableSleepSounds()
                            .find { it.first == bedtimeState.selectedSleepSound }?.second ?: "なし",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
    
    // 時刻選択ダイアログ（簡易版）
    if (showBedtimeTimePicker) {
        TimePickerDialog(
            title = "就寝時刻を設定",
            initialTime = bedtimeState.schedule.bedtime,
            onTimeSelected = { time ->
                viewModel.updateBedtime(time)
                showBedtimeTimePicker = false
            },
            onDismiss = { showBedtimeTimePicker = false }
        )
    }
    
    if (showWakeupTimePicker) {
        TimePickerDialog(
            title = "起床時刻を設定",
            initialTime = bedtimeState.schedule.wakeupTime,
            onTimeSelected = { time ->
                viewModel.updateWakeupTime(time)
                showWakeupTimePicker = false
            },
            onDismiss = { showWakeupTimePicker = false }
        )
    }
    
    // 睡眠目標設定ダイアログ
    if (showSleepGoalDialog) {
        var hours by remember { mutableIntStateOf(bedtimeState.sleepGoal.targetSleepHours) }
        var minutes by remember { mutableIntStateOf(bedtimeState.sleepGoal.targetSleepMinutes) }
        
        AlertDialog(
            onDismissRequest = { showSleepGoalDialog = false },
            title = { Text("睡眠目標を設定") },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = hours.toString(),
                        onValueChange = { hours = it.toIntOrNull()?.coerceIn(0, 12) ?: 8 },
                        label = { Text("時間") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minutes.toString(),
                        onValueChange = { minutes = it.toIntOrNull()?.coerceIn(0, 59) ?: 0 },
                        label = { Text("分") },
                        modifier = Modifier.weight(1f)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateSleepGoal(hours, minutes)
                        showSleepGoalDialog = false
                    }
                ) {
                    Text("設定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSleepGoalDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
    
    // 睡眠音選択ダイアログ
    if (showSleepSoundDialog) {
        AlertDialog(
            onDismissRequest = { showSleepSoundDialog = false },
            title = { Text("睡眠音を選択") },
            text = {
                Column {
                    viewModel.getAvailableSleepSounds().forEach { (soundId, soundName) ->
                        TextButton(
                            onClick = {
                                viewModel.updateSleepSound(soundId)
                                showSleepSoundDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = soundName,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSleepSoundDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}

@Composable
private fun TimePickerDialog(
    title: String,
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    var hours by remember { mutableIntStateOf(initialTime.hour) }
    var minutes by remember { mutableIntStateOf(initialTime.minute) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = String.format("%02d", hours),
                    onValueChange = { hours = it.toIntOrNull()?.coerceIn(0, 23) ?: 0 },
                    label = { Text("時") },
                    modifier = Modifier.weight(1f)
                )
                Text(":")
                OutlinedTextField(
                    value = String.format("%02d", minutes),
                    onValueChange = { minutes = it.toIntOrNull()?.coerceIn(0, 59) ?: 0 },
                    label = { Text("分") },
                    modifier = Modifier.weight(1f)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(LocalTime.of(hours, minutes))
                }
            ) {
                Text("設定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
} 
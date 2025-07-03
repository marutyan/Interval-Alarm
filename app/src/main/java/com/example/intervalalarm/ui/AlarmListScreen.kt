package com.example.intervalalarm.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.intervalalarm.data.AlarmData
import com.example.intervalalarm.viewmodel.AlarmViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    viewModel: AlarmViewModel,
    onNavigateToEdit: (AlarmData?) -> Unit
) {
    val context = LocalContext.current
    val alarmSettings by viewModel.alarmSettings.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("アラーム") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEdit(null) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "新しいアラーム")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(alarmSettings) { alarmData ->
                AlarmCard(
                    alarmData = alarmData,
                    onToggle = { viewModel.toggleAlarmEnabled(context, alarmData.id) },
                    onEdit = { onNavigateToEdit(alarmData) },
                    onDelete = { viewModel.deleteAlarm(context, alarmData.id) }
                )
            }
            
            if (alarmSettings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "アラームがありません\n右下の + ボタンで追加",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                item {
                    Button(
                        onClick = { viewModel.stopAllAlarms(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("すべてのアラームを停止")
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmCard(
    alarmData: AlarmData,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${alarmData.startTime.format(timeFormatter)} - ${alarmData.endTime.format(timeFormatter)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${alarmData.interval}分間隔",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (alarmData.isVibrationEnabled) {
                    Text(
                        text = "バイブレーション ON",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Switch(
                checked = alarmData.isEnabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
} 
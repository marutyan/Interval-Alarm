package com.example.intervalalarm.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intervalalarm.viewmodel.StopwatchViewModel
import com.example.intervalalarm.viewmodel.LapTime

@Composable
fun StopwatchScreen(
    modifier: Modifier = Modifier,
    viewModel: StopwatchViewModel = viewModel()
) {
    val stopwatchState by viewModel.stopwatchState.collectAsState()
    
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
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "ストップウォッチ",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        
        // 時間表示
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
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
                    text = viewModel.formatTimeWithHours(stopwatchState.elapsedTime),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                    if (stopwatchState.isRunning) {
                        viewModel.pauseStopwatch()
                    } else {
                        viewModel.startStopwatch()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (stopwatchState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (stopwatchState.isRunning) "一時停止" else "開始")
            }
            
            // ラップ/リセットボタン
            OutlinedButton(
                onClick = {
                    if (stopwatchState.isRunning) {
                        viewModel.addLapTime()
                    } else {
                        viewModel.resetStopwatch()
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = stopwatchState.isRunning || stopwatchState.elapsedTime > 0
            ) {
                Icon(
                    imageVector = if (stopwatchState.isRunning) Icons.Default.Flag else Icons.Default.Stop,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (stopwatchState.isRunning) "ラップ" else "リセット")
            }
        }
        
        // ラップタイム表示
        if (stopwatchState.lapTimes.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "ラップタイム",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(stopwatchState.lapTimes.reversed()) { lapTime ->
                            LapTimeItem(
                                lapTime = lapTime,
                                viewModel = viewModel,
                                isFirst = lapTime.id == stopwatchState.lapTimes.size,
                                isBest = viewModel.getBestLapTime()?.id == lapTime.id,
                                isWorst = viewModel.getWorstLapTime()?.id == lapTime.id
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LapTimeItem(
    lapTime: LapTime,
    viewModel: StopwatchViewModel,
    isFirst: Boolean,
    isBest: Boolean,
    isWorst: Boolean
) {
    val backgroundColor = when {
        isBest && !isFirst -> MaterialTheme.colorScheme.primaryContainer
        isWorst && !isFirst -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface
    }
    
    val textColor = when {
        isBest && !isFirst -> MaterialTheme.colorScheme.onPrimaryContainer
        isWorst && !isFirst -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ラップ ${lapTime.id}",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = if (isBest || isWorst) FontWeight.Bold else FontWeight.Normal
            )
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = viewModel.formatTime(lapTime.time),
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = viewModel.formatTime(lapTime.totalTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
    }
} 
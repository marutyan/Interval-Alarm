package com.example.intervalalarm.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intervalalarm.viewmodel.WorldClockViewModel
import com.example.intervalalarm.viewmodel.WorldClock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldClockScreen(
    modifier: Modifier = Modifier,
    viewModel: WorldClockViewModel = viewModel()
) {
    val worldClockState by viewModel.worldClockState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTimeZone by remember { mutableStateOf("") }
    var selectedCityName by remember { mutableStateOf("") }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // タイトル
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "世界時計",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "時計を追加"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 世界時計リスト
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(worldClockState.clocks) { clock ->
                WorldClockItem(
                    worldClock = clock,
                    onDelete = { viewModel.removeWorldClock(clock.id) },
                    viewModel = viewModel
                )
            }
        }
    }
    
    // 時計追加ダイアログ
    if (showAddDialog) {
        val availableTimeZones = viewModel.getAvailableTimeZones()
        
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("世界時計を追加") },
            text = {
                Column {
                    Text("都市を選択してください", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(availableTimeZones) { (timeZoneId, cityName) ->
                            TextButton(
                                onClick = {
                                    selectedTimeZone = timeZoneId
                                    selectedCityName = cityName
                                    viewModel.addWorldClock(cityName, timeZoneId)
                                    showAddDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = cityName,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}

@Composable
private fun WorldClockItem(
    worldClock: WorldClock,
    onDelete: () -> Unit,
    viewModel: WorldClockViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (worldClock.isLocal) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
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
                    text = worldClock.cityName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (worldClock.isLocal) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = viewModel.formatDay(worldClock.currentTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (worldClock.isLocal) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                
                if (!worldClock.isLocal) {
                    Text(
                        text = viewModel.getTimeDifference(worldClock.timeZone),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = viewModel.formatTime(worldClock.currentTime),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (worldClock.isLocal) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = viewModel.formatDate(worldClock.currentTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (worldClock.isLocal) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                
                // 日付変更の表示
                when {
                    viewModel.isNextDay(worldClock.currentTime) -> {
                        Text(
                            text = "翌日",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    viewModel.isPreviousDay(worldClock.currentTime) -> {
                        Text(
                            text = "前日",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // 削除ボタン（現在地以外）
            if (!worldClock.isLocal) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "削除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
} 
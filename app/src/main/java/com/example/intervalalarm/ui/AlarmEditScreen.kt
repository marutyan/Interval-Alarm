package com.example.intervalalarm.ui

import android.app.TimePickerDialog
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

    // TimePicker用のダイアログ状態
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    // アラーム音選択用のランチャー
    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            alarmSoundUri = uri?.toString() ?: ""
        }
    }

    // 選択されたアラーム音の名前を取得
    val selectedRingtoneName = remember(alarmSoundUri) {
        if (alarmSoundUri.isNotEmpty()) {
            try {
                val uri = Uri.parse(alarmSoundUri)
                val ringtone = RingtoneManager.getRingtone(context, uri)
                ringtone?.getTitle(context) ?: "カスタム音"
            } catch (e: Exception) {
                "カスタム音"
            }
        } else {
            "デフォルト"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (alarmData == null) "新しいアラーム" else "アラーム編集")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 開始時刻設定
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showStartTimePicker = true }
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
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "タップして変更",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 終了時刻設定
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showEndTimePicker = true }
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
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "タップして変更",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

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
                    Text(
                        text = selectedRingtoneName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "アラーム音を選択")
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                if (alarmSoundUri.isNotEmpty()) {
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(alarmSoundUri))
                                }
                            }
                            ringtonePickerLauncher.launch(intent)
                        },
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

    // TimePicker ダイアログ
    if (showStartTimePicker) {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                startTime = LocalTime.of(hour, minute)
                showStartTimePicker = false
            },
            startTime.hour,
            startTime.minute,
            true
        ).show()
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                endTime = LocalTime.of(hour, minute)
                showEndTimePicker = false
            },
            endTime.hour,
            endTime.minute,
            true
        ).show()
    }
} 
package com.example.intervalalarm.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.example.intervalalarm.data.AlarmData
import com.example.intervalalarm.data.AlarmRepository
import com.example.intervalalarm.AlarmReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import android.os.Build

class AlarmViewModel(private val repository: AlarmRepository) : ViewModel() {
    val alarmSettings: StateFlow<List<AlarmData>> = repository.alarms
    
    private val _selectedAlarm = MutableStateFlow<AlarmData?>(null)
    val selectedAlarm: StateFlow<AlarmData?> = _selectedAlarm.asStateFlow()
    
    fun addAlarm(context: Context, alarmData: AlarmData) {
        val newAlarm = alarmData.copy(id = UUID.randomUUID().toString())
        repository.addAlarm(newAlarm)
        if (newAlarm.isEnabled) {
            scheduleAlarm(context, newAlarm)
        }
    }
    
    fun updateAlarm(context: Context, updatedAlarm: AlarmData) {
        repository.updateAlarm(updatedAlarm)
        // 既存のアラームをキャンセルして新しいアラームを設定
        cancelAlarm(context, updatedAlarm.id)
        if (updatedAlarm.isEnabled) {
            scheduleAlarm(context, updatedAlarm)
        }
    }
    
    fun deleteAlarm(context: Context, alarmId: String) {
        cancelAlarm(context, alarmId)
        repository.deleteAlarm(alarmId)
    }
    
    fun selectAlarm(alarmData: AlarmData) {
        _selectedAlarm.value = alarmData
    }
    
    fun clearSelection() {
        _selectedAlarm.value = null
    }
    
    fun toggleAlarmEnabled(context: Context, alarmId: String) {
        val alarm = repository.getAlarmById(alarmId)
        if (alarm != null) {
            val updatedAlarm = alarm.copy(isEnabled = !alarm.isEnabled)
            repository.updateAlarm(updatedAlarm)
            if (updatedAlarm.isEnabled) {
                scheduleAlarm(context, updatedAlarm)
            } else {
                cancelAlarm(context, alarmId)
            }
        }
    }
    
    fun stopAllAlarms(context: Context) {
        repository.alarms.value.forEach { alarm ->
            cancelAlarm(context, alarm.id)
        }
        repository.stopAllAlarms()
    }
    
    private fun scheduleAlarm(context: Context, alarmData: AlarmData) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Android 12以降では正確なアラームの権限をチェック
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // 権限がない場合は設定画面を開くIntentを作成することもできる
                // ここでは単純にリターンする
                return
            }
        }
        
        val alarmTimes = calculateAlarmTimes(alarmData.startTime, alarmData.endTime, alarmData.interval)
        
        alarmTimes.forEachIndexed { index, alarmTime ->
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("alarm_id", alarmData.id)
                putExtra("alarm_index", index)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                "${alarmData.id}_$index".hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarmTime.hour)
                set(Calendar.MINUTE, alarmTime.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                
                // 今日の時刻が過ぎていたら明日に設定
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                // 権限がない場合の処理
                // ログ出力やユーザーへの通知など
            }
        }
    }
    
    private fun cancelAlarm(context: Context, alarmId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // 最大24個のアラームをキャンセル（1日の最大アラーム数を想定）
        for (i in 0 until 24) {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                "${alarmId}_$i".hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
    
    private fun calculateAlarmTimes(startTime: LocalTime, endTime: LocalTime, interval: Int): List<LocalTime> {
        val alarmTimes = mutableListOf<LocalTime>()
        var currentTime = startTime
        
        while (currentTime <= endTime) {
            alarmTimes.add(currentTime)
            currentTime = currentTime.plusMinutes(interval.toLong())
        }
        
        return alarmTimes
    }
}

class AlarmViewModelFactory(private val repository: AlarmRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlarmViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 
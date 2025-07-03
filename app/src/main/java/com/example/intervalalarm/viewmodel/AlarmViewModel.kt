package com.example.intervalalarm.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.example.intervalalarm.data.AlarmData
import com.example.intervalalarm.AlarmReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class AlarmViewModel : ViewModel() {
    private val _alarmSettings = MutableStateFlow<List<AlarmData>>(emptyList())
    val alarmSettings: StateFlow<List<AlarmData>> = _alarmSettings.asStateFlow()
    
    private val _selectedAlarm = MutableStateFlow<AlarmData?>(null)
    val selectedAlarm: StateFlow<AlarmData?> = _selectedAlarm.asStateFlow()
    
    fun addAlarm(context: Context, alarmData: AlarmData) {
        val newAlarm = alarmData.copy(id = UUID.randomUUID().toString())
        _alarmSettings.value = _alarmSettings.value + newAlarm
        if (newAlarm.isEnabled) {
            scheduleAlarm(context, newAlarm)
        }
    }
    
    fun updateAlarm(context: Context, updatedAlarm: AlarmData) {
        _alarmSettings.value = _alarmSettings.value.map { alarm ->
            if (alarm.id == updatedAlarm.id) updatedAlarm else alarm
        }
        // 既存のアラームをキャンセルして新しいアラームを設定
        cancelAlarm(context, updatedAlarm.id)
        if (updatedAlarm.isEnabled) {
            scheduleAlarm(context, updatedAlarm)
        }
    }
    
    fun deleteAlarm(context: Context, alarmId: String) {
        cancelAlarm(context, alarmId)
        _alarmSettings.value = _alarmSettings.value.filter { it.id != alarmId }
    }
    
    fun selectAlarm(alarmData: AlarmData) {
        _selectedAlarm.value = alarmData
    }
    
    fun clearSelection() {
        _selectedAlarm.value = null
    }
    
    fun toggleAlarmEnabled(context: Context, alarmId: String) {
        _alarmSettings.value = _alarmSettings.value.map { alarm ->
            if (alarm.id == alarmId) {
                val updatedAlarm = alarm.copy(isEnabled = !alarm.isEnabled)
                if (updatedAlarm.isEnabled) {
                    scheduleAlarm(context, updatedAlarm)
                } else {
                    cancelAlarm(context, alarmId)
                }
                updatedAlarm
            } else alarm
        }
    }
    
    fun stopAllAlarms(context: Context) {
        _alarmSettings.value.forEach { alarm ->
            cancelAlarm(context, alarm.id)
        }
        _alarmSettings.value = _alarmSettings.value.map { alarm ->
            alarm.copy(isEnabled = false)
        }
    }
    
    private fun scheduleAlarm(context: Context, alarmData: AlarmData) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
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
            
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
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
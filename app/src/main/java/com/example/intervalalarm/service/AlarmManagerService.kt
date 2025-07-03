package com.example.intervalalarm.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.example.intervalalarm.AlarmReceiver
import com.example.intervalalarm.data.AlarmData
import com.example.intervalalarm.domain.AlarmUseCase
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class AlarmManagerService(
    private val context: Context,
    private val alarmUseCase: AlarmUseCase
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    fun scheduleAlarm(alarmData: AlarmData) {
        // 権限チェック
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(context, "正確なアラーム設定の権限が必要です", Toast.LENGTH_LONG).show()
                return
            }
        }
        
        val alarmTimes = alarmUseCase.calculateAlarmTimes(
            alarmData.startTime, 
            alarmData.endTime, 
            alarmData.interval
        )
        
        alarmTimes.forEachIndexed { index, time ->
            scheduleIndividualAlarm(alarmData, time, index)
        }
    }
    
    private fun scheduleIndividualAlarm(alarmData: AlarmData, time: LocalTime, index: Int) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarmData.id)
            putExtra("alarm_index", index)
            putExtra("vibration_enabled", alarmData.isVibrationEnabled)
            putExtra("alarm_sound_uri", alarmData.alarmSoundUri)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            "${alarmData.id}_$index".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val alarmTime = now.with(time).let { zonedTime ->
            if (zonedTime.isBefore(now)) {
                zonedTime.plusDays(1)
            } else {
                zonedTime
            }
        }
        
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime.toInstant().toEpochMilli(),
                pendingIntent
            )
        } catch (e: Exception) {
            Toast.makeText(context, "アラーム設定エラー: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun cancelAlarm(alarmId: String) {
        // 最大100個のアラームをキャンセル（十分な数）
        for (index in 0 until 100) {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                "${alarmId}_$index".hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            
            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }
    }
    
    fun cancelAllAlarms(alarmIds: List<String>) {
        alarmIds.forEach { alarmId ->
            cancelAlarm(alarmId)
        }
    }
} 
package com.example.intervalalarm.domain

import com.example.intervalalarm.data.AlarmData
import com.example.intervalalarm.data.AlarmRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

class AlarmUseCase(private val repository: AlarmRepository) {
    
    fun getAllAlarms(): Flow<List<AlarmData>> {
        return repository.alarms
    }
    
    suspend fun saveAlarm(alarmData: AlarmData) {
        repository.insertAlarm(alarmData)
    }
    
    suspend fun deleteAlarm(id: String) {
        repository.deleteAlarm(id)
    }
    
    suspend fun toggleAlarmEnabled(id: String, isEnabled: Boolean) {
        repository.updateAlarmEnabled(id, isEnabled)
    }
    
    suspend fun deleteAllAlarms() {
        repository.deleteAllAlarms()
    }
    
    suspend fun getAlarmById(id: String): AlarmData? {
        return repository.getAlarmById(id)
    }
    
    fun calculateAlarmTimes(startTime: LocalTime, endTime: LocalTime, intervalMinutes: Int): List<LocalTime> {
        val alarmTimes = mutableListOf<LocalTime>()
        var currentTime = startTime
        
        while (currentTime <= endTime) {
            alarmTimes.add(currentTime)
            currentTime = currentTime.plusMinutes(intervalMinutes.toLong())
        }
        
        return alarmTimes
    }
    
    fun validateAlarmData(alarmData: AlarmData): ValidationResult {
        return when {
            alarmData.startTime >= alarmData.endTime -> 
                ValidationResult.Error("終了時刻は開始時刻より後に設定してください")
            alarmData.interval <= 0 -> 
                ValidationResult.Error("間隔は1分以上に設定してください")
            alarmData.interval > 1440 -> 
                ValidationResult.Error("間隔は1日以内に設定してください")
            else -> ValidationResult.Success
        }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
} 
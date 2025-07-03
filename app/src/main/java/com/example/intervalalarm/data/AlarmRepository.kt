package com.example.intervalalarm.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AlarmRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    
    private val _alarms = MutableStateFlow<List<AlarmData>>(emptyList())
    val alarms: StateFlow<List<AlarmData>> = _alarms.asStateFlow()
    
    init {
        loadAlarms()
    }
    
    private fun loadAlarms() {
        val alarmsJson = prefs.getString("alarms", "[]") ?: "[]"
        try {
            val alarmsList = json.decodeFromString(kotlinx.serialization.builtins.ListSerializer(AlarmData.serializer()), alarmsJson)
            _alarms.value = alarmsList
        } catch (e: Exception) {
            _alarms.value = emptyList()
        }
    }
    
    private fun saveAlarms() {
        val alarmsJson = json.encodeToString(kotlinx.serialization.builtins.ListSerializer(AlarmData.serializer()), _alarms.value)
        prefs.edit().putString("alarms", alarmsJson).apply()
    }
    
    fun addAlarm(alarm: AlarmData) {
        _alarms.value = _alarms.value + alarm
        saveAlarms()
    }
    
    fun updateAlarm(updatedAlarm: AlarmData) {
        _alarms.value = _alarms.value.map { alarm ->
            if (alarm.id == updatedAlarm.id) updatedAlarm else alarm
        }
        saveAlarms()
    }
    
    fun deleteAlarm(alarmId: String) {
        _alarms.value = _alarms.value.filter { it.id != alarmId }
        saveAlarms()
    }
    
    fun toggleAlarmEnabled(alarmId: String) {
        _alarms.value = _alarms.value.map { alarm ->
            if (alarm.id == alarmId) alarm.copy(isEnabled = !alarm.isEnabled) else alarm
        }
        saveAlarms()
    }
    
    fun stopAllAlarms() {
        _alarms.value = _alarms.value.map { alarm ->
            alarm.copy(isEnabled = false)
        }
        saveAlarms()
    }
    
    fun getAlarmById(alarmId: String): AlarmData? {
        return _alarms.value.find { it.id == alarmId }
    }
} 
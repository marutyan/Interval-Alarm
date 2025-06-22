package com.example.intervalalarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.example.intervalalarm.data.AlarmSetting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AlarmViewModel : ViewModel() {
    private val _alarmSettings = MutableStateFlow<List<AlarmSetting>>(emptyList())
    val alarmSettings: StateFlow<List<AlarmSetting>> = _alarmSettings.asStateFlow()
    
    private val _selectedAlarm = MutableStateFlow<AlarmSetting?>(null)
    val selectedAlarm: StateFlow<AlarmSetting?> = _selectedAlarm.asStateFlow()
    
    fun addAlarm(alarmSetting: AlarmSetting) {
        _alarmSettings.value = _alarmSettings.value + alarmSetting
    }
    
    fun updateAlarm(updatedAlarm: AlarmSetting) {
        _alarmSettings.value = _alarmSettings.value.map { alarm ->
            if (alarm.id == updatedAlarm.id) updatedAlarm else alarm
        }
    }
    
    fun deleteAlarm(alarmId: String) {
        _alarmSettings.value = _alarmSettings.value.filter { it.id != alarmId }
    }
    
    fun selectAlarm(alarmSetting: AlarmSetting) {
        _selectedAlarm.value = alarmSetting
    }
    
    fun clearSelection() {
        _selectedAlarm.value = null
    }
    
    fun toggleAlarmEnabled(alarmId: String) {
        _alarmSettings.value = _alarmSettings.value.map { alarm ->
            if (alarm.id == alarmId) alarm.copy(isEnabled = !alarm.isEnabled) else alarm
        }
    }
    
    fun stopAllAlarms() {
        _alarmSettings.value = _alarmSettings.value.map { alarm ->
            alarm.copy(isEnabled = false)
        }
    }
} 
package com.example.intervalalarm.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class AlarmRepository(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
    
    private val database = AlarmDatabase.getDatabase(context)
    private val alarmDao = database.alarmDao()
    
    private val json = Json {
        prettyPrint = true
        isLenient = true
    }
    
    private val _alarms = MutableStateFlow<List<AlarmData>>(emptyList())
    val alarms: Flow<List<AlarmData>> = _alarms.asStateFlow()
    
    // Room Database用のFlow
    val alarmsFromRoom: Flow<List<AlarmData>> = alarmDao.getAllAlarmsFlow().map { entities ->
        entities.map { it.toAlarmData() }
    }
    
    init {
        loadAlarms()
    }
    
    private fun loadAlarms() {
        val alarmsJson = sharedPreferences.getString("alarms", null)
        if (alarmsJson != null) {
            try {
                val alarmsList = json.decodeFromString<List<AlarmData>>(alarmsJson)
                _alarms.value = alarmsList
            } catch (e: Exception) {
                _alarms.value = emptyList()
            }
        }
    }
    
    private fun saveAlarms() {
        val alarmsJson = json.encodeToString(_alarms.value)
        sharedPreferences.edit().putString("alarms", alarmsJson).apply()
    }
    
    // SharedPreferences版
    suspend fun insertAlarm(alarmData: AlarmData) {
        val newAlarm = if (alarmData.id.isEmpty()) {
            alarmData.copy(id = UUID.randomUUID().toString())
        } else {
            alarmData
        }
        
        val currentAlarms = _alarms.value.toMutableList()
        val existingIndex = currentAlarms.indexOfFirst { it.id == newAlarm.id }
        
        if (existingIndex != -1) {
            currentAlarms[existingIndex] = newAlarm
        } else {
            currentAlarms.add(newAlarm)
        }
        
        _alarms.value = currentAlarms
        saveAlarms()
        
        // Room Databaseにも保存
        try {
            alarmDao.insertAlarm(newAlarm.toAlarmEntity())
        } catch (e: Exception) {
            // Room Database保存失敗時はSharedPreferencesを使用
        }
    }
    
    // Room Database版
    suspend fun insertAlarmToRoom(alarmData: AlarmData) {
        val newAlarm = if (alarmData.id.isEmpty()) {
            alarmData.copy(id = UUID.randomUUID().toString())
        } else {
            alarmData
        }
        alarmDao.insertAlarm(newAlarm.toAlarmEntity())
    }
    
    suspend fun deleteAlarm(id: String) {
        val currentAlarms = _alarms.value.toMutableList()
        currentAlarms.removeAll { it.id == id }
        _alarms.value = currentAlarms
        saveAlarms()
        
        // Room Databaseからも削除
        try {
            alarmDao.deleteAlarmById(id)
        } catch (e: Exception) {
            // Room Database削除失敗時はSharedPreferencesを使用
        }
    }
    
    suspend fun updateAlarm(alarmData: AlarmData) {
        insertAlarm(alarmData)
    }
    
    suspend fun getAlarmById(id: String): AlarmData? {
        // まずSharedPreferencesから取得を試す
        val fromPrefs = _alarms.value.find { it.id == id }
        if (fromPrefs != null) {
            return fromPrefs
        }
        
        // Room Databaseから取得を試す
        return try {
            alarmDao.getAlarmById(id)?.toAlarmData()
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getAllAlarms(): List<AlarmData> {
        return _alarms.value
    }

    suspend fun deleteAllAlarms() {
        _alarms.value = emptyList()
        saveAlarms()
        
        // Room Databaseからも削除
        try {
            alarmDao.deleteAllAlarms()
        } catch (e: Exception) {
            // Room Database削除失敗時はSharedPreferencesを使用
        }
    }

    suspend fun getEnabledAlarms(): List<AlarmData> {
        return _alarms.value.filter { it.isEnabled }
    }

    suspend fun updateAlarmEnabled(id: String, isEnabled: Boolean) {
        val currentAlarms = _alarms.value.toMutableList()
        val existingIndex = currentAlarms.indexOfFirst { it.id == id }
        
        if (existingIndex != -1) {
            currentAlarms[existingIndex] = currentAlarms[existingIndex].copy(isEnabled = isEnabled)
        }
        
        _alarms.value = currentAlarms
        saveAlarms()
        
        // Room Databaseも更新
        try {
            alarmDao.updateAlarmEnabled(id, isEnabled)
        } catch (e: Exception) {
            // Room Database更新失敗時はSharedPreferencesを使用
        }
    }
} 
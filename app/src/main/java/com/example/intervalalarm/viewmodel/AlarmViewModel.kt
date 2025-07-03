package com.example.intervalalarm.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.intervalalarm.data.AlarmData
import com.example.intervalalarm.domain.AlarmUseCase
import com.example.intervalalarm.domain.ValidationResult
import com.example.intervalalarm.service.AlarmManagerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlarmViewModel(
    private val alarmUseCase: AlarmUseCase,
    private val alarmManagerService: AlarmManagerService
) : ViewModel() {
    
    private val _alarmSettings = MutableStateFlow<List<AlarmData>>(emptyList())
    val alarmSettings: StateFlow<List<AlarmData>> = _alarmSettings.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        viewModelScope.launch {
            alarmUseCase.getAllAlarms().collect { alarms ->
                _alarmSettings.value = alarms
            }
        }
    }
    
    fun addOrUpdateAlarm(context: Context, alarmData: AlarmData) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                // バリデーション
                when (val validationResult = alarmUseCase.validateAlarmData(alarmData)) {
                    is ValidationResult.Success -> {
                        alarmUseCase.saveAlarm(alarmData)
                        alarmManagerService.scheduleAlarm(alarmData)
                        Toast.makeText(context, "アラームを保存しました", Toast.LENGTH_SHORT).show()
                    }
                    is ValidationResult.Error -> {
                        _errorMessage.value = validationResult.message
                        Toast.makeText(context, validationResult.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                val message = "アラームの保存に失敗しました: ${e.message}"
                _errorMessage.value = message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteAlarm(context: Context, alarmId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                alarmUseCase.deleteAlarm(alarmId)
                alarmManagerService.cancelAlarm(alarmId)
                Toast.makeText(context, "アラームを削除しました", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                val message = "アラームの削除に失敗しました: ${e.message}"
                _errorMessage.value = message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun toggleAlarmEnabled(context: Context, alarmId: String) {
        viewModelScope.launch {
            try {
                val alarm = alarmUseCase.getAlarmById(alarmId)
                if (alarm != null) {
                    val newEnabledState = !alarm.isEnabled
                    alarmUseCase.toggleAlarmEnabled(alarmId, newEnabledState)
                    
                    if (newEnabledState) {
                        alarmManagerService.scheduleAlarm(alarm.copy(isEnabled = true))
                    } else {
                        alarmManagerService.cancelAlarm(alarmId)
                    }
                }
            } catch (e: Exception) {
                val message = "アラーム設定の変更に失敗しました: ${e.message}"
                _errorMessage.value = message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    fun stopAllAlarms(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val alarmIds = _alarmSettings.value.map { it.id }
                alarmManagerService.cancelAllAlarms(alarmIds)
                alarmUseCase.deleteAllAlarms()
                Toast.makeText(context, "すべてのアラームを停止しました", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                val message = "アラームの停止に失敗しました: ${e.message}"
                _errorMessage.value = message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}

class AlarmViewModelFactory(
    private val alarmUseCase: AlarmUseCase,
    private val alarmManagerService: AlarmManagerService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlarmViewModel(alarmUseCase, alarmManagerService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 
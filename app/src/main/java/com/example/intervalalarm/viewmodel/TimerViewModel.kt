package com.example.intervalalarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TimerState(
    val totalSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false
)

class TimerViewModel : ViewModel() {
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    
    private var timerJob: Job? = null
    
    fun setTimer(hours: Int, minutes: Int, seconds: Int) {
        val totalSeconds = hours * 3600 + minutes * 60 + seconds
        _timerState.value = TimerState(
            totalSeconds = totalSeconds,
            remainingSeconds = totalSeconds,
            isRunning = false,
            isFinished = false
        )
    }
    
    fun startTimer() {
        if (_timerState.value.remainingSeconds > 0) {
            _timerState.value = _timerState.value.copy(isRunning = true, isFinished = false)
            timerJob = viewModelScope.launch {
                while (_timerState.value.remainingSeconds > 0 && _timerState.value.isRunning) {
                    delay(1000)
                    val currentState = _timerState.value
                    val newRemainingSeconds = currentState.remainingSeconds - 1
                    
                    if (newRemainingSeconds <= 0) {
                        _timerState.value = currentState.copy(
                            remainingSeconds = 0,
                            isRunning = false,
                            isFinished = true
                        )
                        onTimerFinished()
                    } else {
                        _timerState.value = currentState.copy(remainingSeconds = newRemainingSeconds)
                    }
                }
            }
        }
    }
    
    fun pauseTimer() {
        _timerState.value = _timerState.value.copy(isRunning = false)
        timerJob?.cancel()
    }
    
    fun resetTimer() {
        timerJob?.cancel()
        val currentState = _timerState.value
        _timerState.value = currentState.copy(
            remainingSeconds = currentState.totalSeconds,
            isRunning = false,
            isFinished = false
        )
    }
    
    fun clearTimer() {
        timerJob?.cancel()
        _timerState.value = TimerState()
    }
    
    private fun onTimerFinished() {
        // タイマー完了時の処理（アラーム音、通知など）
        // 実際のアプリでは、ここでアラーム音を再生したり通知を送信したりする
    }
    
    fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        
        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, secs)
            else -> String.format("%02d:%02d", minutes, secs)
        }
    }
    
    fun getProgress(): Float {
        val currentState = _timerState.value
        return if (currentState.totalSeconds > 0) {
            (currentState.totalSeconds - currentState.remainingSeconds).toFloat() / currentState.totalSeconds.toFloat()
        } else {
            0f
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
} 
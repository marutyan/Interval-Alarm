package com.example.intervalalarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LapTime(
    val id: Int,
    val time: Long,
    val totalTime: Long
)

data class StopwatchState(
    val elapsedTime: Long = 0L,
    val isRunning: Boolean = false,
    val lapTimes: List<LapTime> = emptyList()
)

class StopwatchViewModel : ViewModel() {
    private val _stopwatchState = MutableStateFlow(StopwatchState())
    val stopwatchState: StateFlow<StopwatchState> = _stopwatchState.asStateFlow()
    
    private var stopwatchJob: Job? = null
    private var startTime: Long = 0L
    private var pausedTime: Long = 0L
    
    fun startStopwatch() {
        if (!_stopwatchState.value.isRunning) {
            startTime = System.currentTimeMillis() - pausedTime
            _stopwatchState.value = _stopwatchState.value.copy(isRunning = true)
            
            stopwatchJob = viewModelScope.launch {
                while (_stopwatchState.value.isRunning) {
                    val currentTime = System.currentTimeMillis()
                    val elapsedTime = currentTime - startTime
                    _stopwatchState.value = _stopwatchState.value.copy(elapsedTime = elapsedTime)
                    delay(10) // 10ms間隔で更新
                }
            }
        }
    }
    
    fun pauseStopwatch() {
        if (_stopwatchState.value.isRunning) {
            pausedTime = _stopwatchState.value.elapsedTime
            _stopwatchState.value = _stopwatchState.value.copy(isRunning = false)
            stopwatchJob?.cancel()
        }
    }
    
    fun resetStopwatch() {
        stopwatchJob?.cancel()
        startTime = 0L
        pausedTime = 0L
        _stopwatchState.value = StopwatchState()
    }
    
    fun addLapTime() {
        val currentState = _stopwatchState.value
        if (currentState.isRunning || currentState.elapsedTime > 0) {
            val lapTime = if (currentState.lapTimes.isEmpty()) {
                currentState.elapsedTime
            } else {
                currentState.elapsedTime - currentState.lapTimes.last().totalTime
            }
            
            val newLap = LapTime(
                id = currentState.lapTimes.size + 1,
                time = lapTime,
                totalTime = currentState.elapsedTime
            )
            
            _stopwatchState.value = currentState.copy(
                lapTimes = currentState.lapTimes + newLap
            )
        }
    }
    
    fun formatTime(timeInMillis: Long): String {
        val totalSeconds = timeInMillis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val milliseconds = (timeInMillis % 1000) / 10
        
        return String.format("%02d:%02d.%02d", minutes, seconds, milliseconds)
    }
    
    fun formatTimeWithHours(timeInMillis: Long): String {
        val totalSeconds = timeInMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        val milliseconds = (timeInMillis % 1000) / 10
        
        return if (hours > 0) {
            String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, milliseconds)
        } else {
            String.format("%02d:%02d.%02d", minutes, seconds, milliseconds)
        }
    }
    
    fun getBestLapTime(): LapTime? {
        val currentState = _stopwatchState.value
        return if (currentState.lapTimes.size > 1) {
            currentState.lapTimes.minByOrNull { it.time }
        } else {
            null
        }
    }
    
    fun getWorstLapTime(): LapTime? {
        val currentState = _stopwatchState.value
        return if (currentState.lapTimes.size > 1) {
            currentState.lapTimes.maxByOrNull { it.time }
        } else {
            null
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopwatchJob?.cancel()
    }
} 
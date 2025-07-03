package com.example.intervalalarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

data class WorldClock(
    val id: String,
    val cityName: String,
    val timeZone: ZoneId,
    val currentTime: ZonedDateTime,
    val isLocal: Boolean = false
)

data class WorldClockState(
    val clocks: List<WorldClock> = emptyList(),
    val currentTime: Long = System.currentTimeMillis()
)

class WorldClockViewModel : ViewModel() {
    private val _worldClockState = MutableStateFlow(WorldClockState())
    val worldClockState: StateFlow<WorldClockState> = _worldClockState.asStateFlow()
    
    private val defaultTimeZones = listOf(
        "Asia/Tokyo" to "東京",
        "America/New_York" to "ニューヨーク",
        "Europe/London" to "ロンドン",
        "Asia/Shanghai" to "上海",
        "America/Los_Angeles" to "ロサンゼルス",
        "Europe/Paris" to "パリ",
        "Asia/Seoul" to "ソウル",
        "Australia/Sydney" to "シドニー",
        "Europe/Berlin" to "ベルリン",
        "Asia/Dubai" to "ドバイ",
        "America/Chicago" to "シカゴ",
        "Asia/Singapore" to "シンガポール"
    )
    
    init {
        // デフォルトの時計を追加
        addDefaultClocks()
        startTimeUpdates()
    }
    
    private fun addDefaultClocks() {
        val localTimeZone = ZoneId.systemDefault()
        val localClock = WorldClock(
            id = "local",
            cityName = "現在地",
            timeZone = localTimeZone,
            currentTime = ZonedDateTime.now(localTimeZone),
            isLocal = true
        )
        
        val initialClocks = listOf(
            localClock,
            createWorldClock("tokyo", "東京", "Asia/Tokyo"),
            createWorldClock("newyork", "ニューヨーク", "America/New_York"),
            createWorldClock("london", "ロンドン", "Europe/London")
        )
        
        _worldClockState.value = _worldClockState.value.copy(clocks = initialClocks)
    }
    
    private fun createWorldClock(id: String, cityName: String, timeZoneId: String): WorldClock {
        val timeZone = ZoneId.of(timeZoneId)
        return WorldClock(
            id = id,
            cityName = cityName,
            timeZone = timeZone,
            currentTime = ZonedDateTime.now(timeZone)
        )
    }
    
    private fun startTimeUpdates() {
        viewModelScope.launch {
            while (true) {
                updateAllTimes()
                delay(1000) // 1秒ごとに更新
            }
        }
    }
    
    private fun updateAllTimes() {
        val currentState = _worldClockState.value
        val updatedClocks = currentState.clocks.map { clock ->
            clock.copy(currentTime = ZonedDateTime.now(clock.timeZone))
        }
        
        _worldClockState.value = currentState.copy(
            clocks = updatedClocks,
            currentTime = System.currentTimeMillis()
        )
    }
    
    fun addWorldClock(cityName: String, timeZoneId: String) {
        val currentState = _worldClockState.value
        val newClock = createWorldClock(
            id = UUID.randomUUID().toString(),
            cityName = cityName,
            timeZoneId = timeZoneId
        )
        
        _worldClockState.value = currentState.copy(
            clocks = currentState.clocks + newClock
        )
    }
    
    fun removeWorldClock(clockId: String) {
        val currentState = _worldClockState.value
        val updatedClocks = currentState.clocks.filter { it.id != clockId }
        
        _worldClockState.value = currentState.copy(clocks = updatedClocks)
    }
    
    fun getAvailableTimeZones(): List<Pair<String, String>> {
        return defaultTimeZones
    }
    
    fun formatTime(zonedDateTime: ZonedDateTime): String {
        return zonedDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    }
    
    fun formatDate(zonedDateTime: ZonedDateTime): String {
        return zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))
    }
    
    fun formatDay(zonedDateTime: ZonedDateTime): String {
        return zonedDateTime.format(DateTimeFormatter.ofPattern("EEEE", Locale.JAPANESE))
    }
    
    fun getTimeDifference(targetTimeZone: ZoneId): String {
        val localTime = ZonedDateTime.now(ZoneId.systemDefault())
        val targetTime = ZonedDateTime.now(targetTimeZone)
        
        val offsetDiff = targetTime.offset.totalSeconds - localTime.offset.totalSeconds
        val hoursDiff = offsetDiff / 3600
        
        return when {
            hoursDiff == 0 -> "同じ時刻"
            hoursDiff > 0 -> "+${hoursDiff}時間"
            else -> "${hoursDiff}時間"
        }
    }
    
    fun isNextDay(zonedDateTime: ZonedDateTime): Boolean {
        val localTime = ZonedDateTime.now(ZoneId.systemDefault())
        return zonedDateTime.dayOfYear != localTime.dayOfYear || 
               zonedDateTime.year != localTime.year
    }
    
    fun isPreviousDay(zonedDateTime: ZonedDateTime): Boolean {
        val localTime = ZonedDateTime.now(ZoneId.systemDefault())
        return zonedDateTime.dayOfYear < localTime.dayOfYear || 
               zonedDateTime.year < localTime.year
    }
} 
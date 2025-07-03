package com.example.intervalalarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class BedtimeSchedule(
    val bedtime: LocalTime = LocalTime.of(22, 0),
    val wakeupTime: LocalTime = LocalTime.of(7, 0),
    val isEnabled: Boolean = false,
    val selectedDays: Set<DayOfWeek> = setOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY
    )
)

data class SleepGoal(
    val targetSleepHours: Int = 8,
    val targetSleepMinutes: Int = 0
)

data class BedtimeState(
    val schedule: BedtimeSchedule = BedtimeSchedule(),
    val sleepGoal: SleepGoal = SleepGoal(),
    val windDownEnabled: Boolean = false,
    val windDownDuration: Int = 30, // 分
    val doNotDisturbEnabled: Boolean = false,
    val sleepSoundsEnabled: Boolean = false,
    val selectedSleepSound: String = "none"
)

class BedtimeViewModel : ViewModel() {
    private val _bedtimeState = MutableStateFlow(BedtimeState())
    val bedtimeState: StateFlow<BedtimeState> = _bedtimeState.asStateFlow()
    
    private val availableSleepSounds = listOf(
        "none" to "なし",
        "rain" to "雨音",
        "ocean" to "海の音",
        "forest" to "森の音",
        "white_noise" to "ホワイトノイズ",
        "pink_noise" to "ピンクノイズ",
        "brown_noise" to "ブラウンノイズ"
    )
    
    fun updateBedtime(bedtime: LocalTime) {
        val currentState = _bedtimeState.value
        val updatedSchedule = currentState.schedule.copy(bedtime = bedtime)
        _bedtimeState.value = currentState.copy(schedule = updatedSchedule)
    }
    
    fun updateWakeupTime(wakeupTime: LocalTime) {
        val currentState = _bedtimeState.value
        val updatedSchedule = currentState.schedule.copy(wakeupTime = wakeupTime)
        _bedtimeState.value = currentState.copy(schedule = updatedSchedule)
    }
    
    fun toggleScheduleEnabled() {
        val currentState = _bedtimeState.value
        val updatedSchedule = currentState.schedule.copy(isEnabled = !currentState.schedule.isEnabled)
        _bedtimeState.value = currentState.copy(schedule = updatedSchedule)
    }
    
    fun updateSelectedDays(days: Set<DayOfWeek>) {
        val currentState = _bedtimeState.value
        val updatedSchedule = currentState.schedule.copy(selectedDays = days)
        _bedtimeState.value = currentState.copy(schedule = updatedSchedule)
    }
    
    fun updateSleepGoal(hours: Int, minutes: Int) {
        val currentState = _bedtimeState.value
        val updatedGoal = SleepGoal(targetSleepHours = hours, targetSleepMinutes = minutes)
        _bedtimeState.value = currentState.copy(sleepGoal = updatedGoal)
    }
    
    fun toggleWindDown() {
        val currentState = _bedtimeState.value
        _bedtimeState.value = currentState.copy(windDownEnabled = !currentState.windDownEnabled)
    }
    
    fun updateWindDownDuration(duration: Int) {
        val currentState = _bedtimeState.value
        _bedtimeState.value = currentState.copy(windDownDuration = duration)
    }
    
    fun toggleDoNotDisturb() {
        val currentState = _bedtimeState.value
        _bedtimeState.value = currentState.copy(doNotDisturbEnabled = !currentState.doNotDisturbEnabled)
    }
    
    fun toggleSleepSounds() {
        val currentState = _bedtimeState.value
        _bedtimeState.value = currentState.copy(sleepSoundsEnabled = !currentState.sleepSoundsEnabled)
    }
    
    fun updateSleepSound(soundId: String) {
        val currentState = _bedtimeState.value
        _bedtimeState.value = currentState.copy(selectedSleepSound = soundId)
    }
    
    fun getAvailableSleepSounds(): List<Pair<String, String>> {
        return availableSleepSounds
    }
    
    fun formatTime(time: LocalTime): String {
        return time.format(DateTimeFormatter.ofPattern("HH:mm"))
    }
    
    fun calculateSleepDuration(): String {
        val currentState = _bedtimeState.value
        val bedtime = currentState.schedule.bedtime
        val wakeupTime = currentState.schedule.wakeupTime
        
        val sleepDuration = if (wakeupTime.isAfter(bedtime)) {
            // 同じ日の場合
            java.time.Duration.between(bedtime, wakeupTime)
        } else {
            // 翌日の場合
            java.time.Duration.between(bedtime, wakeupTime.plusHours(24))
        }
        
        val hours = sleepDuration.toHours()
        val minutes = sleepDuration.toMinutes() % 60
        
        return "${hours}時間${minutes}分"
    }
    
    fun getSleepGoalText(): String {
        val goal = _bedtimeState.value.sleepGoal
        return if (goal.targetSleepMinutes == 0) {
            "${goal.targetSleepHours}時間"
        } else {
            "${goal.targetSleepHours}時間${goal.targetSleepMinutes}分"
        }
    }
    
    fun getDayOfWeekText(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> "月"
            DayOfWeek.TUESDAY -> "火"
            DayOfWeek.WEDNESDAY -> "水"
            DayOfWeek.THURSDAY -> "木"
            DayOfWeek.FRIDAY -> "金"
            DayOfWeek.SATURDAY -> "土"
            DayOfWeek.SUNDAY -> "日"
        }
    }
    
    fun getWindDownStartTime(): LocalTime {
        val currentState = _bedtimeState.value
        return currentState.schedule.bedtime.minusMinutes(currentState.windDownDuration.toLong())
    }
    
    fun isGoalMet(): Boolean {
        val currentState = _bedtimeState.value
        val bedtime = currentState.schedule.bedtime
        val wakeupTime = currentState.schedule.wakeupTime
        val goal = currentState.sleepGoal
        
        val actualSleepDuration = if (wakeupTime.isAfter(bedtime)) {
            java.time.Duration.between(bedtime, wakeupTime)
        } else {
            java.time.Duration.between(bedtime, wakeupTime.plusHours(24))
        }
        
        val goalDuration = java.time.Duration.ofHours(goal.targetSleepHours.toLong())
            .plusMinutes(goal.targetSleepMinutes.toLong())
        
        return actualSleepDuration >= goalDuration
    }
} 
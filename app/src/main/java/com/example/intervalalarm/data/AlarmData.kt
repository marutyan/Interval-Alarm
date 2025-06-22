package com.example.intervalalarm.data

import java.time.LocalTime
import java.util.UUID

data class AlarmSetting(
    val id: String = UUID.randomUUID().toString(),
    val startTime: LocalTime = LocalTime.of(6, 30),
    val endTime: LocalTime = LocalTime.of(7, 0),
    val intervalMinutes: Int = 5,
    val daysOfWeek: Set<Int> = setOf(1, 2, 3, 4, 5), // 1=月曜日, 7=日曜日
    val isEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val ringtoneUri: String? = null
) {
    fun getFormattedTimeRange(): String {
        return "${startTime.hour.toString().padStart(2, '0')}:${startTime.minute.toString().padStart(2, '0')} - " +
                "${endTime.hour.toString().padStart(2, '0')}:${endTime.minute.toString().padStart(2, '0')}"
    }
    
    fun getFormattedDaysOfWeek(): String {
        val dayNames = mapOf(
            1 to "月", 2 to "火", 3 to "水", 4 to "木", 5 to "金", 6 to "土", 7 to "日"
        )
        return if (daysOfWeek.size == 7) {
            "毎日"
        } else if (daysOfWeek == setOf(1, 2, 3, 4, 5)) {
            "平日"
        } else if (daysOfWeek == setOf(6, 7)) {
            "週末"
        } else {
            daysOfWeek.sorted().joinToString("") { dayNames[it] ?: "" }
        }
    }
} 
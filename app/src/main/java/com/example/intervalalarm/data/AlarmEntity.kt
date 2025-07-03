package com.example.intervalalarm.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalTime

@Entity(tableName = "alarms")
@TypeConverters(Converters::class)
data class AlarmEntity(
    @PrimaryKey val id: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val interval: Int,
    val isVibrationEnabled: Boolean,
    val alarmSoundUri: String,
    val isEnabled: Boolean
)

// Extension functions for conversion
fun AlarmEntity.toAlarmData(): AlarmData {
    return AlarmData(
        id = id,
        startTime = startTime,
        endTime = endTime,
        interval = interval,
        isVibrationEnabled = isVibrationEnabled,
        alarmSoundUri = alarmSoundUri,
        isEnabled = isEnabled
    )
}

fun AlarmData.toAlarmEntity(): AlarmEntity {
    return AlarmEntity(
        id = id,
        startTime = startTime,
        endTime = endTime,
        interval = interval,
        isVibrationEnabled = isVibrationEnabled,
        alarmSoundUri = alarmSoundUri,
        isEnabled = isEnabled
    )
} 
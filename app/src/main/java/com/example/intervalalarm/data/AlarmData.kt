package com.example.intervalalarm.data

import java.time.LocalTime
import java.util.UUID

data class AlarmData(
    val id: String = UUID.randomUUID().toString(),
    val startTime: LocalTime = LocalTime.of(6, 30),
    val endTime: LocalTime = LocalTime.of(22, 0),
    val interval: Int = 60, // 分
    val isEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val alarmSoundUri: String = ""
) 
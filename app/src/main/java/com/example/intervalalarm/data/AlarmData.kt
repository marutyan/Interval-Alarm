package com.example.intervalalarm.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.time.LocalTime
import java.util.UUID

object LocalTimeSerializer : KSerializer<LocalTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: LocalTime) {
        encoder.encodeString("${value.hour}:${value.minute}")
    }
    
    override fun deserialize(decoder: Decoder): LocalTime {
        val timeString = decoder.decodeString()
        val parts = timeString.split(":")
        return LocalTime.of(parts[0].toInt(), parts[1].toInt())
    }
}

@Serializable
data class AlarmData(
    val id: String = UUID.randomUUID().toString(),
    @Serializable(with = LocalTimeSerializer::class)
    val startTime: LocalTime = LocalTime.of(6, 30),
    @Serializable(with = LocalTimeSerializer::class)
    val endTime: LocalTime = LocalTime.of(22, 0),
    val interval: Int = 60, // 分
    val isEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val alarmSoundUri: String = ""
) {
    companion object {
        fun fromJson(json: String): AlarmData? {
            return try {
                Json.decodeFromString<AlarmData>(json)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    fun toJson(): String {
        return Json.encodeToString(serializer(), this)
    }
} 
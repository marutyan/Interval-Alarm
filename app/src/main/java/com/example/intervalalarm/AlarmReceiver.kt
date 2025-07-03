package com.example.intervalalarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.os.VibrationEffect
import androidx.core.app.NotificationCompat
import com.example.intervalalarm.data.AlarmRepository

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        var mediaPlayer: MediaPlayer? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra("alarm_id") ?: return
        val alarmIndex = intent.getIntExtra("alarm_index", 0)
        
        // AlarmRepositoryからアラーム設定を取得
        val repository = AlarmRepository(context)
        val alarmData = repository.getAlarmById(alarmId) ?: return
        
        // アラームが無効になっている場合は何もしない
        if (!alarmData.isEnabled) return
        
        createNotificationChannel(context)
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // アラーム停止用のIntent
        val stopIntent = Intent(context, AlarmStopReceiver::class.java).apply {
            putExtra("alarm_id", alarmId)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.hashCode(),
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationBuilder = NotificationCompat.Builder(context, "alarm_channel")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("アラーム")
            .setContentText("${alarmData.startTime} - ${alarmData.endTime} (${alarmData.interval}分間隔)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_media_pause,
                "停止",
                stopPendingIntent
            )
        
        // アラーム音を再生
        playAlarmSound(context, alarmData.alarmSoundUri)
        
        // バイブレーション
        if (alarmData.isVibrationEnabled) {
            vibrate(context)
        }
        
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
    
    private fun playAlarmSound(context: Context, customSoundUri: String) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer()
            
            val soundUri = if (customSoundUri.isNotEmpty()) {
                Uri.parse(customSoundUri)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }
            
            mediaPlayer?.apply {
                setDataSource(context, soundUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            // フォールバック：デフォルトアラーム音
            try {
                val ringtone = RingtoneManager.getRingtone(
                    context,
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                )
                ringtone?.play()
            } catch (e2: Exception) {
                // 何もしない
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "alarm_channel",
                "アラーム通知",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "アラーム通知用チャンネル"
                enableVibration(true)
                setSound(null, null) // 音はMediaPlayerで再生
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun vibrate(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12以降
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
            val effect = VibrationEffect.createWaveform(pattern, 0)
            vibrator.vibrate(effect)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0以降
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
            val effect = VibrationEffect.createWaveform(pattern, 0)
            vibrator.vibrate(effect)
        } else {
            // Android 8.0未満
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, 0)
        }
    }
} 
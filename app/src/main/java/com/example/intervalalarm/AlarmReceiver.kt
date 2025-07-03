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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "alarm_channel"
        private const val NOTIFICATION_ID = 1
        var mediaPlayer: MediaPlayer? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra("alarm_id") ?: return
        val vibrationEnabled = intent.getBooleanExtra("vibration_enabled", false)
        val alarmSoundUri = intent.getStringExtra("alarm_sound_uri")
        
        // コルーチンスコープで非同期処理を実行
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val repository = AlarmRepository(context)
            val alarmData = repository.getAlarmById(alarmId)
            
            if (alarmData?.isEnabled == true) {
                // メインスレッドで通知とアラーム音を再生
                CoroutineScope(Dispatchers.Main).launch {
                    showNotification(context, alarmId)
                    playAlarmSound(context, alarmSoundUri)
                    
                    if (vibrationEnabled) {
                        vibrateDevice(context)
                    }
                }
            }
        }
    }

    private fun showNotification(context: Context, alarmId: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // 通知チャンネルを作成（Android 8.0以降）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "アラーム通知",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "インターバルアラームの通知"
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // アラーム停止用のPendingIntent
        val stopIntent = Intent(context, AlarmStopReceiver::class.java).apply {
            putExtra("alarm_id", alarmId)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.hashCode(),
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 通知を作成
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("アラーム")
            .setContentText("インターバルアラームが鳴っています")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher_foreground, "停止", stopPendingIntent)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun playAlarmSound(context: Context, alarmSoundUri: String?) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                val uri = if (alarmSoundUri.isNullOrEmpty()) {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                } else {
                    Uri.parse(alarmSoundUri)
                }
                
                setDataSource(context, uri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun vibrateDevice(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            val vibrationEffect = VibrationEffect.createWaveform(
                longArrayOf(0, 1000, 1000),
                intArrayOf(0, 255, 0),
                0
            )
            vibrator.vibrate(vibrationEffect)
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createWaveform(
                    longArrayOf(0, 1000, 1000),
                    0
                )
                vibrator.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 1000, 1000), 0)
            }
        }
    }
} 
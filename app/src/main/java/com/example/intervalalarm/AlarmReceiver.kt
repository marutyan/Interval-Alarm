package com.example.intervalalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.media.RingtoneManager
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.os.VibrationEffect
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "interval_alarm_channel"
        val channelName = "Interval Alarm"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            channel.enableVibration(true)
            channel.enableLights(true)
            notificationManager.createNotificationChannel(channel)
        }

        // 選択されたアラーム音を取得
        val prefs = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        val ringtoneUriString = prefs.getString("ringtone_uri", null)
        val soundUri = if (ringtoneUriString != null) android.net.Uri.parse(ringtoneUriString) else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        // アプリを開くためのPendingIntent
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val appPendingIntent = PendingIntent.getActivity(
            context, 0, appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ストップアクション用のPendingIntent
        val stopIntent = Intent(context, AlarmStopReceiver::class.java)
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("アラーム")
            .setContentText("アラームの時間です！")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSound(soundUri)
            .setContentIntent(appPendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                "ストップ",
                stopPendingIntent
            )
            .setFullScreenIntent(appPendingIntent, true)

        // バイブレーション設定の確認（デフォルトはtrue）
        val isVibrationEnabled = prefs.getBoolean("vibration_enabled", true)
        if (isVibrationEnabled) {
            vibrate(context)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
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
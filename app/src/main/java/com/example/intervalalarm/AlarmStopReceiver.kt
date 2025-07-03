package com.example.intervalalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager

class AlarmStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // バイブレーションを停止
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12以降
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.cancel()
        } else {
            // Android 12未満
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.cancel()
        }
        
        // 通知を削除
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
        
        // TODO: 個別アラームの停止処理（AlarmManagerから該当アラームを削除）
        // 現在は全通知をキャンセルするシンプルな実装
    }
} 
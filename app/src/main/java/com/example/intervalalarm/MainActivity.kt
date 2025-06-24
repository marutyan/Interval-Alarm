package com.example.intervalalarm

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TimePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.intervalalarm.ui.theme.IntervalAlarmTheme
import android.os.Build
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.content.Intent
import android.app.Activity
import android.net.Uri

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Android 13以降は通知パーミッションの実行時リクエストが必要
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val startTimePicker = findViewById<TimePicker>(R.id.startTimePicker)
        val endTimePicker = findViewById<TimePicker>(R.id.endTimePicker)
        val intervalEditText = findViewById<EditText>(R.id.intervalEditText)
        val setAlarmButton = findViewById<Button>(R.id.setAlarmButton)
        val stopAllAlarmsButton = findViewById<Button>(R.id.stopAllAlarmsButton)
        val selectRingtoneButton = findViewById<Button>(R.id.selectRingtoneButton)

        setAlarmButton.setOnClickListener {
            val startHour = startTimePicker.hour
            val startMinute = startTimePicker.minute
            val endHour = endTimePicker.hour
            val endMinute = endTimePicker.minute
            val intervalText = intervalEditText.text.toString()
            val intervalMinutes = intervalText.toIntOrNull() ?: -1

            if (intervalText.isBlank()) {
                android.widget.Toast.makeText(this, "間隔を入力してください", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (intervalMinutes < 1) {
                android.widget.Toast.makeText(this, "間隔は1以上の整数で入力してください", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val startTotalMinutes = startHour * 60 + startMinute
            val endTotalMinutes = endHour * 60 + endMinute

            if (endTotalMinutes <= startTotalMinutes) {
                android.widget.Toast.makeText(this, "終了時刻は開始時刻より後にしてください", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val alarmTimes = mutableListOf<Pair<Int, Int>>()
            var current = startTotalMinutes
            while (current <= endTotalMinutes) {
                val hour = current / 60
                val minute = current % 60
                alarmTimes.add(Pair(hour, minute))
                current += intervalMinutes
            }

            if (alarmTimes.isEmpty()) {
                android.widget.Toast.makeText(this, "アラーム時刻が設定できません", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- フォアグラウンドサービスを起動 ---
            val serviceIntent = android.content.Intent(this, AlarmForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }

            // --- AlarmManagerでアラームをスケジューリング ---
            val alarmManager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
            for ((i, time) in alarmTimes.withIndex()) {
                val intent = android.content.Intent(this, AlarmReceiver::class.java)
                val pendingIntent = android.app.PendingIntent.getBroadcast(
                    this,
                    i, // 複数アラームを区別するためリクエストコードを変える
                    intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
                val now = java.util.Calendar.getInstance()
                val alarmTime = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, time.first)
                    set(java.util.Calendar.MINUTE, time.second)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                    if (before(now)) {
                        add(java.util.Calendar.DATE, 1)
                    }
                }
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    alarmTime.timeInMillis,
                    pendingIntent
                )
            }
            android.widget.Toast.makeText(this, "アラームを設定しました", android.widget.Toast.LENGTH_SHORT).show()
        }

        stopAllAlarmsButton.setOnClickListener {
            val alarmManager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
            // 最大20個のアラームをキャンセル（リクエストコード0〜19）
            for (i in 0 until 20) {
                val intent = android.content.Intent(this, AlarmReceiver::class.java)
                val pendingIntent = android.app.PendingIntent.getBroadcast(
                    this,
                    i,
                    intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            }
            android.widget.Toast.makeText(this, "すべてのアラームを停止しました", android.widget.Toast.LENGTH_SHORT).show()

            // サービスも停止
            val serviceIntent = android.content.Intent(this, AlarmForegroundService::class.java)
            stopService(serviceIntent)
        }

        selectRingtoneButton.setOnClickListener {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "アラーム音を選択")
            val currentUri = getSharedPreferences("alarm_prefs", MODE_PRIVATE).getString("ringtone_uri", null)
            if (currentUri != null) {
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(currentUri))
            }
            startActivityForResult(intent, 200)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (uri != null) {
                getSharedPreferences("alarm_prefs", MODE_PRIVATE).edit().putString("ringtone_uri", uri.toString()).apply()
                android.widget.Toast.makeText(this, "アラーム音を設定しました", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    IntervalAlarmTheme {
        Greeting("Android")
    }
}
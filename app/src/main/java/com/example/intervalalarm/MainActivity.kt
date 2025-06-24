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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val startTimePicker = findViewById<TimePicker>(R.id.startTimePicker)
        val endTimePicker = findViewById<TimePicker>(R.id.endTimePicker)
        val intervalEditText = findViewById<EditText>(R.id.intervalEditText)
        val setAlarmButton = findViewById<Button>(R.id.setAlarmButton)

        setAlarmButton.setOnClickListener {
            val startHour = startTimePicker.hour
            val startMinute = startTimePicker.minute
            val endHour = endTimePicker.hour
            val endMinute = endTimePicker.minute
            val intervalText = intervalEditText.text.toString()
            val intervalMinutes = intervalText.toIntOrNull() ?: 0

            Log.d("IntervalAlarm", "開始時刻: $startHour:$startMinute")
            Log.d("IntervalAlarm", "終了時刻: $endHour:$endMinute")
            Log.d("IntervalAlarm", "間隔: $intervalMinutes 分")

            if (intervalMinutes <= 0) {
                Log.d("IntervalAlarm", "間隔は1分以上にしてください")
                return@setOnClickListener
            }

            val startTotalMinutes = startHour * 60 + startMinute
            val endTotalMinutes = endHour * 60 + endMinute

            if (endTotalMinutes <= startTotalMinutes) {
                Log.d("IntervalAlarm", "終了時刻は開始時刻より後にしてください")
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

            for ((i, time) in alarmTimes.withIndex()) {
                Log.d("IntervalAlarm", "アラーム${i+1}: %02d:%02d".format(time.first, time.second))
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
                // 現在時刻からアラーム時刻までの差分を計算
                val now = java.util.Calendar.getInstance()
                val alarmTime = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, time.first)
                    set(java.util.Calendar.MINUTE, time.second)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                    if (before(now)) {
                        add(java.util.Calendar.DATE, 1) // すでに過ぎていたら翌日に設定
                    }
                }
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    alarmTime.timeInMillis,
                    pendingIntent
                )
                Log.d("IntervalAlarm", "アラーム${i+1}をスケジューリング: %02d:%02d".format(time.first, time.second))
            }
        }

        setContent {
            IntervalAlarmTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
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
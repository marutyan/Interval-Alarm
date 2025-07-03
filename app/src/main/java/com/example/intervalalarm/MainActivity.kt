package com.example.intervalalarm

import android.os.Bundle
import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.intervalalarm.ui.theme.IntervalAlarmTheme
import com.example.intervalalarm.ui.AlarmListScreen
import com.example.intervalalarm.ui.AlarmEditScreen
import com.example.intervalalarm.viewmodel.AlarmViewModel
import com.example.intervalalarm.data.AlarmData

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Android 13以降の通知権限をリクエスト
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
        
        setContent {
            IntervalAlarmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: AlarmViewModel = viewModel()
                    val context = LocalContext.current
                    
                    NavHost(
                        navController = navController,
                        startDestination = "alarm_list"
                    ) {
                        composable("alarm_list") {
                            AlarmListScreen(
                                viewModel = viewModel,
                                onNavigateToEdit = { alarmData ->
                                    viewModel.selectAlarm(alarmData ?: AlarmData())
                                    navController.navigate("alarm_edit")
                                }
                            )
                        }
                        
                        composable("alarm_edit") {
                            val selectedAlarm = viewModel.selectedAlarm.collectAsState().value
                            AlarmEditScreen(
                                onNavigateBack = {
                                    viewModel.clearSelection()
                                    navController.popBackStack()
                                },
                                onSave = { alarmData ->
                                    if (selectedAlarm?.id?.isNotEmpty() == true) {
                                        // 既存のアラームを更新
                                        viewModel.updateAlarm(context, alarmData.copy(id = selectedAlarm.id))
                                    } else {
                                        // 新しいアラームを追加
                                        viewModel.addAlarm(context, alarmData)
                                    }
                                    viewModel.clearSelection()
                                    navController.popBackStack()
                                },
                                alarmData = selectedAlarm
                            )
                        }
                    }
                }
            }
        }
    }
}
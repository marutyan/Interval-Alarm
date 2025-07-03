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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.intervalalarm.ui.theme.IntervalAlarmTheme
import com.example.intervalalarm.ui.AlarmListScreen
import com.example.intervalalarm.ui.AlarmEditScreen
import com.example.intervalalarm.viewmodel.AlarmViewModel

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
        setContent {
            IntervalAlarmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: AlarmViewModel = viewModel()
                    
                    NavHost(
                        navController = navController,
                        startDestination = "alarm_list"
                    ) {
                        composable("alarm_list") {
                            AlarmListScreen(
                                viewModel = viewModel,
                                onNavigateToEdit = { alarm ->
                                    if (alarm != null) {
                                        viewModel.selectAlarm(alarm)
                                        navController.navigate("alarm_edit")
                                    } else {
                                        viewModel.clearSelection()
                                        navController.navigate("alarm_edit")
                                    }
                                }
                            )
                        }
                        composable("alarm_edit") {
                            val selectedAlarm = viewModel.selectedAlarm.collectAsState().value
                            AlarmEditScreen(
                                alarm = selectedAlarm,
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onSelectRingtone = {
                                    // TODO: アラーム音選択の実装
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
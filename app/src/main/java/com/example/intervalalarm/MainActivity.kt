package com.example.intervalalarm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.intervalalarm.data.AlarmData
import com.example.intervalalarm.data.AlarmRepository
import com.example.intervalalarm.domain.AlarmUseCase
import com.example.intervalalarm.service.AlarmManagerService
import com.example.intervalalarm.ui.AlarmEditScreen
import com.example.intervalalarm.ui.AlarmListScreen
import com.example.intervalalarm.ui.theme.IntervalAlarmTheme
import com.example.intervalalarm.viewmodel.AlarmViewModel
import com.example.intervalalarm.viewmodel.AlarmViewModelFactory

class MainActivity : ComponentActivity() {
    
    private lateinit var repository: AlarmRepository
    private lateinit var alarmUseCase: AlarmUseCase
    private lateinit var alarmManagerService: AlarmManagerService
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // 権限が拒否された場合の処理
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 依存関係の初期化
        repository = AlarmRepository(this)
        alarmUseCase = AlarmUseCase(repository)
        alarmManagerService = AlarmManagerService(this, alarmUseCase)
        
        // 通知権限の確認
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        setContent {
            IntervalAlarmTheme {
                val navController = rememberNavController()
                val viewModel: AlarmViewModel = viewModel(
                    factory = AlarmViewModelFactory(alarmUseCase, alarmManagerService)
                )
                
                AlarmNavigation(navController = navController, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AlarmNavigation(navController: NavHostController, viewModel: AlarmViewModel) {
    NavHost(
        navController = navController,
        startDestination = "alarm_list"
    ) {
        composable("alarm_list") {
            AlarmListScreen(
                viewModel = viewModel,
                onNavigateToEdit = { alarmData ->
                    if (alarmData != null) {
                        navController.navigate("alarm_edit/${alarmData.id}")
                    } else {
                        navController.navigate("alarm_edit/new")
                    }
                }
            )
        }
        
        composable("alarm_edit/{alarmId}") { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getString("alarmId")
            val alarmData = if (alarmId != "new") {
                viewModel.alarmSettings.value.find { it.id == alarmId }
            } else {
                null
            }
            
            AlarmEditScreen(
                onNavigateBack = { navController.popBackStack() },
                onSave = { alarm ->
                    viewModel.addOrUpdateAlarm(navController.context, alarm)
                    navController.popBackStack()
                },
                alarmData = alarmData
            )
        }
    }
}
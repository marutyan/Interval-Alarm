package com.example.intervalalarm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intervalalarm.data.AlarmRepository
import com.example.intervalalarm.domain.AlarmUseCase
import com.example.intervalalarm.service.AlarmManagerService
import com.example.intervalalarm.ui.AlarmListScreen
import com.example.intervalalarm.ui.theme.IntervalAlarmTheme
import com.example.intervalalarm.viewmodel.AlarmViewModel
import com.example.intervalalarm.viewmodel.AlarmViewModelFactory
import com.example.intervalalarm.ui.BedtimeScreen
import com.example.intervalalarm.ui.StopwatchScreen
import com.example.intervalalarm.ui.TimerScreen
import com.example.intervalalarm.ui.WorldClockScreen

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
        
        enableEdgeToEdge()
        setContent {
            IntervalAlarmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DeskClockApp(
                        alarmViewModelFactory = AlarmViewModelFactory(alarmUseCase, alarmManagerService)
                    )
                }
            }
        }
    }
}

data class TabItem(
    val title: String,
    val icon: ImageVector,
    val badgeCount: Int? = null
)

@Composable
fun DeskClockApp(
    alarmViewModelFactory: AlarmViewModelFactory
) {
    val alarmViewModel: AlarmViewModel = viewModel(factory = alarmViewModelFactory)
    
    val tabs = listOf(
        TabItem(
            title = stringResource(R.string.alarm),
            icon = Icons.Default.Alarm
        ),
        TabItem(
            title = stringResource(R.string.timer),
            icon = Icons.Default.Schedule
        ),
        TabItem(
            title = stringResource(R.string.stopwatch),
            icon = Icons.Default.AccessTime
        ),
        TabItem(
            title = stringResource(R.string.clock),
            icon = Icons.Default.Language
        ),
        TabItem(
            title = stringResource(R.string.bedtime),
            icon = Icons.Default.Home
        )
    )
    
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (tab.badgeCount != null) {
                                        Badge {
                                            Text(tab.badgeCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title
                                )
                            }
                        },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedTabIndex) {
            0 -> AlarmListScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                viewModel = alarmViewModel
            )
            1 -> TimerScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            2 -> StopwatchScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            3 -> WorldClockScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            4 -> BedtimeScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}
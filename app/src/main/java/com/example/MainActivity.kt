package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CrimeLensBottomNavBar
import com.example.ui.components.CrimeLensTopBar
import com.example.ui.components.ScamSimulatorDialog
import com.example.ui.components.ScreenRoute
import com.example.ui.screens.AnalyzersScreen
import com.example.ui.screens.CommunityHeatmapScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmergencyModeScreen
import com.example.ui.screens.LearningGamificationScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ScanDetailDialog
import com.example.ui.theme.CrimeLensTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            CrimeLensTheme(isDarkTheme = isDarkTheme) {
                CrimeLensApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CrimeLensApp(viewModel: MainViewModel) {
    var currentRoute by remember { mutableStateOf(ScreenRoute.DASHBOARD.route) }

    val scanHistory by viewModel.scanHistory.collectAsStateWithLifecycle()
    val scanCount by viewModel.scanCount.collectAsStateWithLifecycle()
    val highRiskCount by viewModel.highRiskCount.collectAsStateWithLifecycle()
    val reportsState by viewModel.reportsState.collectAsStateWithLifecycle()
    val threatDensity by viewModel.threatDensity.collectAsStateWithLifecycle()
    val timeFilter by viewModel.timeFilter.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    val analysisState by viewModel.analysisState.collectAsStateWithLifecycle()
    val selectedScanDetail by viewModel.selectedScanDetail.collectAsStateWithLifecycle()
    val userPoints by viewModel.userPoints.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val showSimulatorDialog by viewModel.showSimulatorDialog.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CrimeLensTopBar(
                currentRoute = currentRoute,
                isDarkTheme = isDarkTheme,
                onToggleTheme = { viewModel.toggleTheme() },
                onEmergencyClick = { currentRoute = ScreenRoute.EMERGENCY.route },
                onOpenSimulator = { viewModel.openSimulator() }
            )
        },
        bottomBar = {
            CrimeLensBottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { screen -> currentRoute = screen.route }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentRoute, label = "ScreenTransition") { route ->
                when (route) {
                    ScreenRoute.DASHBOARD.route -> {
                        DashboardScreen(
                            scanHistory = scanHistory,
                            totalScanCount = scanCount,
                            highRiskCount = highRiskCount,
                            userPoints = userPoints,
                            onNavigateToRoute = { navRoute -> currentRoute = navRoute.route },
                            onSelectScanDetail = { scan -> viewModel.selectScanDetail(scan) },
                            onTriggerEmergencyMode = { currentRoute = ScreenRoute.EMERGENCY.route },
                            onOpenSimulator = { viewModel.openSimulator() }
                        )
                    }

                    ScreenRoute.ANALYZERS.route -> {
                        AnalyzersScreen(
                            analysisState = analysisState,
                            onAnalyze = { type, content, b64 -> viewModel.analyzeEvidence(type, content, b64) },
                            onResetState = { viewModel.resetAnalysisState() },
                            onTriggerEmergencyMode = { currentRoute = ScreenRoute.EMERGENCY.route },
                            onOpenSimulator = { viewModel.openSimulator() }
                        )
                    }

                    ScreenRoute.HEATMAP.route -> {
                        CommunityHeatmapScreen(
                            reportsState = reportsState,
                            threatDensity = threatDensity,
                            selectedTimeFilter = timeFilter,
                            onSelectTimeFilter = { filter -> viewModel.setTimeFilter(filter) },
                            onUpvoteReport = { id -> viewModel.upvoteReport(id) },
                            onSubmitReport = { title, cat, city, desc ->
                                viewModel.submitCommunityReport(title, cat, city, desc)
                            }
                        )
                    }

                    ScreenRoute.LEARNING.route -> {
                        LearningGamificationScreen(
                            achievements = achievements,
                            learningModules = viewModel.learningModules,
                            userPoints = userPoints,
                            onCompleteQuiz = { score -> viewModel.completeQuiz(score) }
                        )
                    }

                    ScreenRoute.PROFILE.route -> {
                        ProfileScreen(
                            userPoints = userPoints,
                            scanCount = scanCount,
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = { viewModel.toggleTheme() },
                            onNavigateEmergency = { currentRoute = ScreenRoute.EMERGENCY.route },
                            onOpenSimulator = { viewModel.openSimulator() }
                        )
                    }

                    ScreenRoute.EMERGENCY.route -> {
                        EmergencyModeScreen()
                    }
                }
            }

            if (selectedScanDetail != null) {
                ScanDetailDialog(
                    scan = selectedScanDetail!!,
                    onDismiss = { viewModel.selectScanDetail(null) }
                )
            }

            if (showSimulatorDialog) {
                ScamSimulatorDialog(
                    onDismiss = { viewModel.closeSimulator() },
                    onTriggerEmergency = {
                        viewModel.closeSimulator()
                        currentRoute = ScreenRoute.EMERGENCY.route
                    }
                )
            }
        }
    }
}

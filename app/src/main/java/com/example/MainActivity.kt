package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AuthDialog
import com.example.ui.components.CrimeLensBottomNavBar
import com.example.ui.components.CrimeLensTopBar
import com.example.ui.components.ScamSimulatorDialog
import com.example.ui.components.ScreenRoute
import com.example.ui.screens.AnalyzersScreen
import com.example.ui.screens.CommunityHeatmapScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmergencyModeScreen
import com.example.ui.screens.EvidenceGalleryScreen
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

    BackHandler(enabled = currentRoute != ScreenRoute.DASHBOARD.route) {
        currentRoute = ScreenRoute.DASHBOARD.route
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { /* Handled gracefully */ }
        LaunchedEffect(Unit) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val scanHistory by viewModel.scanHistory.collectAsStateWithLifecycle()
    val scanCount by viewModel.scanCount.collectAsStateWithLifecycle()
    val highRiskCount by viewModel.highRiskCount.collectAsStateWithLifecycle()
    val reportsState by viewModel.reportsState.collectAsStateWithLifecycle()
    val threatDensity by viewModel.threatDensity.collectAsStateWithLifecycle()
    val timeFilter by viewModel.timeFilter.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    val evidenceList by viewModel.evidenceItems.collectAsStateWithLifecycle()
    val analysisLogs by viewModel.analysisLogs.collectAsStateWithLifecycle()
    val analysisState by viewModel.analysisState.collectAsStateWithLifecycle()
    val selectedScanDetail by viewModel.selectedScanDetail.collectAsStateWithLifecycle()
    val userPoints by viewModel.userPoints.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val showSimulatorDialog by viewModel.showSimulatorDialog.collectAsStateWithLifecycle()
    val authUser by viewModel.authUser.collectAsStateWithLifecycle()
    val showAuthDialog by viewModel.showAuthDialog.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CrimeLensTopBar(
                currentRoute = currentRoute,
                isDarkTheme = isDarkTheme,
                authUser = authUser,
                onAuthClick = { viewModel.openAuthDialog() },
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
                            onAnalyze = { type, content, b64, filePath -> viewModel.analyzeEvidence(type, content, b64, filePath) },
                            onResetState = { viewModel.resetAnalysisState() },
                            onTriggerEmergencyMode = { currentRoute = ScreenRoute.EMERGENCY.route },
                            onOpenSimulator = { viewModel.openSimulator() }
                        )
                    }

                    ScreenRoute.GALLERY.route -> {
                        EvidenceGalleryScreen(
                            scanRecords = scanHistory
                        )
                    }

                    ScreenRoute.HEATMAP.route -> {
                        CommunityHeatmapScreen(
                            reportsState = reportsState,
                            threatDensity = threatDensity,
                            selectedTimeFilter = timeFilter,
                            onSelectTimeFilter = { filter -> viewModel.setTimeFilter(filter) },
                            onUpvoteReport = { id -> viewModel.upvoteReport(id) },
                            onSubmitReport = { title, cat, city, desc, imageBase64 ->
                                viewModel.submitCommunityReport(title, cat, city, desc, imageBase64 = imageBase64)
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
                            evidenceList = evidenceList,
                            analysisLogs = analysisLogs,
                            authUser = authUser,
                            onOpenAuth = { viewModel.openAuthDialog() },
                            onSignOut = { viewModel.signOut() },
                            onDeleteEvidence = { id -> viewModel.deleteEvidenceItem(id) },
                            onDeleteLog = { id -> viewModel.deleteAnalysisLog(id) },
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

            if (showAuthDialog) {
                AuthDialog(
                    onDismiss = { viewModel.closeAuthDialog() },
                    onSignIn = { email, pass, onResult -> viewModel.signIn(email, pass, onResult) },
                    onRegister = { email, pass, name, onResult -> viewModel.register(email, pass, name, onResult) },
                    onAnonymousSignIn = { onResult -> viewModel.signInAnonymously(onResult) }
                )
            }
        }
    }
}

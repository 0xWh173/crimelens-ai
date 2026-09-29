package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.CityHeatData
import com.example.data.model.CommunityReport
import com.example.data.model.EvidenceType
import com.example.data.model.LearningModule
import com.example.data.model.QuizQuestion
import com.example.data.model.ScamCategory
import com.example.data.model.ScanRecord
import com.example.data.model.UiState
import com.example.data.model.UserAchievement
import com.example.ui.screens.CommunityHeatmapScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmergencyModeScreen
import com.example.ui.screens.LearningGamificationScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.CrimeLensTheme
import com.example.util.TimeUtils
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class CrimeLensScreenshotsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun capture_dashboard_screen() {
        val mockScans = listOf(
            ScanRecord(
                id = 1,
                title = "Bank SMS Analysis",
                evidenceType = EvidenceType.SCREENSHOT,
                timestamp = System.currentTimeMillis() - 8 * 60 * 1000L,
                riskScore = 89,
                category = ScamCategory.BANK_IMPERSONATION,
                summary = "Suspicious KYC block link",
                redFlagsJson = "Threatens account suspension",
                recommendationsJson = "Do not click link"
            )
        )

        composeTestRule.setContent {
            CrimeLensTheme(isDarkTheme = false) {
                DashboardScreen(
                    scanHistory = mockScans,
                    totalScanCount = 1,
                    highRiskCount = 1,
                    userPoints = 120,
                    onNavigateToRoute = {},
                    onSelectScanDetail = {},
                    onTriggerEmergencyMode = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dashboard_screen.png")
    }

    @Test
    fun capture_reports_screen() {
        val mockReports = listOf(
            CommunityReport(
                id = "rep_1",
                scamTitle = "Fake SBI KYC Account Block SMS",
                category = ScamCategory.BANK_IMPERSONATION,
                locationCity = "Mumbai",
                reportDate = System.currentTimeMillis() - 15 * 60 * 1000L,
                description = "Received SMS stating netbanking blocked today. Update KYC at sbi-net-kyc.top",
                upvotes = 142,
                verifiedScam = true
            )
        )

        val mockDensity = listOf(
            CityHeatData("Mumbai", "High Risk", 12, "Fake KYC", System.currentTimeMillis())
        )

        composeTestRule.setContent {
            CrimeLensTheme(isDarkTheme = false) {
                CommunityHeatmapScreen(
                    reportsState = UiState.Success(mockReports),
                    threatDensity = mockDensity,
                    selectedTimeFilter = TimeUtils.TimeFilter.ALL_TIME,
                    onSelectTimeFilter = {},
                    onUpvoteReport = {},
                    onSubmitReport = { _, _, _, _ -> }
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/reports_screen.png")
    }

    @Test
    fun capture_emergency_screen() {
        composeTestRule.setContent {
            CrimeLensTheme(isDarkTheme = false) {
                EmergencyModeScreen()
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/emergency_screen.png")
    }
}

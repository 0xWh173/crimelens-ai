package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.flow.Flow
import java.util.UUID

enum class ScamCategory(val displayName: String, val iconName: String) {
    BANK_IMPERSONATION("Bank Scam", "account_balance"),
    UPI_FRAUD("UPI Scam", "qr_code_scanner"),
    JOB_OFFER("Job Scam", "work"),
    INVESTMENT_CRYPTO("Investment Scam", "trending_up"),
    LOTTERY_PRIZE("Lottery Scam", "emoji_events"),
    COURIER_PARCEL("Courier Scam", "local_shipping"),
    OTP_HARVESTING("OTP Scam", "pin"),
    TECH_SUPPORT("Tech Support Scam", "laptop"),
    ROMANCE("Romance Scam", "favorite"),
    FAKE_POLICE("Fake Police Scam", "gavel"),
    FAKE_KYC("Fake KYC Scam", "badge"),
    GOVERNMENT_SCHEME("Fake Govt Scam", "policy"),
    UNKNOWN("Digital Scam", "shield")
}

enum class EvidenceType(val label: String) {
    SCREENSHOT("Screenshot"),
    URL("Website URL"),
    QR_CODE("QR Code"),
    VOICE_AUDIO("Voice Recording"),
    EMAIL("Email Body/Header")
}

@Entity(tableName = "scan_history")
data class ScanRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val evidenceType: EvidenceType,
    val timestamp: Long = System.currentTimeMillis(),
    val riskScore: Int, // 0 - 100
    val category: ScamCategory,
    val summary: String,
    val redFlagsJson: String, // Stored as JSON or delimited by ||
    val recommendationsJson: String,
    val extractedText: String = "",
    val rawContent: String = ""
)

@Entity(tableName = "community_reports")
data class CommunityReport(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val scamTitle: String = "",
    val category: ScamCategory = ScamCategory.UNKNOWN,
    val locationCity: String = "",
    val reportDate: Long = System.currentTimeMillis(),
    val description: String = "",
    val upvotes: Int = 0,
    val verifiedScam: Boolean = true,
    val riskLevel: String = "HIGH",
    val severity: String = "HIGH",
    val source: String = "Community Report"
)

data class CityHeatData(
    val cityName: String,
    val threatLevel: String, // "High Risk", "Moderate Risk", "Low Risk"
    val scamCount: Int,
    val topCategory: String,
    val lastActiveTimestamp: Long
)

@Entity(tableName = "achievements")
data class UserAchievement(
    @PrimaryKey val badgeId: String,
    val title: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean = false,
    val progress: Int = 0,
    val maxProgress: Int = 100,
    val unlockedTimestamp: Long = 0
)

data class QuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

data class LearningModule(
    val id: String,
    val title: String,
    val description: String,
    val category: ScamCategory,
    val readTimeMinutes: Int,
    val isCompleted: Boolean = false,
    val lessonContent: String,
    val quiz: List<QuizQuestion>
)

data class ScamAnalysisResult(
    val riskScore: Int,
    val scamCategory: ScamCategory,
    val primarySummary: String,
    val observedEvidence: String = "",
    val inference: String = "",
    val externalIntelligence: String = "",
    val recommendation: String = "",
    val redFlags: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val extractedContent: String = "",
    val technicalDetails: Map<String, String> = emptyMap()
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Empty(val message: String) : UiState<Nothing>
    data class Error(val message: String) : UiState<Nothing>
    data class Offline<T>(val data: T, val message: String = "You're offline. Showing saved data.") : UiState<T>
}

interface ScamReportRepository {
    fun getCommunityReports(): Flow<List<CommunityReport>>
    suspend fun addCommunityReport(report: CommunityReport)
    suspend fun upvoteReport(reportId: String)
}

interface ThreatIntelligenceRepository {
    suspend fun analyzeUrl(url: String): ScamAnalysisResult
    suspend fun analyzeEvidence(
        evidenceType: EvidenceType,
        content: String,
        base64Image: String? = null
    ): ScamAnalysisResult
}

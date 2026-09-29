package com.example.data.repository

import com.example.data.local.AchievementDao
import com.example.data.local.AnalysisLogDao
import com.example.data.local.EvidenceDao
import com.example.data.local.ReportDao
import com.example.data.local.ScanDao
import com.example.data.model.AnalysisLog
import com.example.data.model.CityHeatData
import com.example.data.model.CommunityReport
import com.example.data.model.EvidenceItem
import com.example.data.model.EvidenceType
import com.example.data.model.LearningModule
import com.example.data.model.QuizQuestion
import com.example.data.model.ScamAnalysisResult
import com.example.data.model.ScamCategory
import com.example.data.model.ScamReportRepository
import com.example.data.model.ScanRecord
import com.example.data.model.ThreatIntelligenceRepository
import com.example.data.model.UserAchievement
import com.example.data.remote.FirestoreScamRepository
import com.example.data.remote.GeminiRepository
import com.example.util.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class AppRepository(
    private val scanDao: ScanDao,
    private val reportDao: ReportDao,
    private val achievementDao: AchievementDao,
    private val evidenceDao: EvidenceDao,
    private val analysisLogDao: AnalysisLogDao,
    private val threatIntelRepo: ThreatIntelligenceRepository = GeminiRepository(),
    private val firestoreScamRepo: ScamReportRepository = FirestoreScamRepository()
) {

    // User's actual scan history from local Room database
    val allScans: Flow<List<ScanRecord>> = scanDao.getAllScans()
    val scanCount: Flow<Int> = scanDao.getScanCount()
    val highRiskScanCount: Flow<Int> = scanDao.getHighRiskScanCount()
    val achievements: Flow<List<UserAchievement>> = achievementDao.getAllAchievements()

    // Identified evidence items and detailed forensic analysis logs in Room
    val allEvidence: Flow<List<EvidenceItem>> = evidenceDao.getAllEvidence()
    val evidenceCount: Flow<Int> = evidenceDao.getEvidenceCount()
    val allAnalysisLogs: Flow<List<AnalysisLog>> = analysisLogDao.getAllLogs()
    val highRiskAnalysisLogs: Flow<List<AnalysisLog>> = analysisLogDao.getHighRiskLogs()
    val analysisLogCount: Flow<Int> = analysisLogDao.getLogCount()

    /**
     * Highly optimized real-time community reports flow.
     * Emits cached Room data immediately for instant 0ms UI render,
     * then seamlessly updates when live Firestore snapshots arrive.
     */
    val communityReports: Flow<List<CommunityReport>> = flow {
        // Step 1: Emit local room cache instantly for zero latency
        val localCache = try { reportDao.getAllCommunityReports().firstOrNull() ?: emptyList() } catch (e: Exception) { emptyList() }
        emit(localCache)

        // Step 2: Collect real-time updates from Firestore
        firestoreScamRepo.getCommunityReports().collect { firestoreReports ->
            if (firestoreReports.isNotEmpty()) {
                emit(firestoreReports)
                // Cache asynchronously to Room without blocking UI flow thread
                try { reportDao.insertReports(firestoreReports) } catch (e: Exception) {}
            } else if (localCache.isEmpty()) {
                emit(emptyList())
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Calculate threat density dynamically from actual community reports.
     */
    fun getThreatDensity(reports: List<CommunityReport>, timeFilter: TimeUtils.TimeFilter = TimeUtils.TimeFilter.ALL_TIME): List<CityHeatData> {
        val filteredReports = reports.filter { TimeUtils.isWithinFilter(it.reportDate, timeFilter) }
        if (filteredReports.isEmpty()) return emptyList()

        val cityGroups = filteredReports.groupBy { it.locationCity.trim() }

        return cityGroups.map { (city, cityReports) ->
            val count = cityReports.size
            val topCategory = cityReports.groupBy { it.category.displayName }
                .maxByOrNull { it.value.size }?.key ?: ScamCategory.UNKNOWN.displayName

            val hasHighRisk = cityReports.any { it.severity.equals("HIGH", ignoreCase = true) || it.riskLevel.equals("HIGH", ignoreCase = true) }

            val threatLevel = when {
                count >= 5 || (count >= 2 && hasHighRisk) -> "High Risk"
                count >= 2 -> "Moderate Risk"
                else -> "Low Risk"
            }

            val lastActiveTime = cityReports.maxOfOrNull { it.reportDate } ?: System.currentTimeMillis()

            CityHeatData(
                cityName = if (city.isBlank()) "General Region" else city,
                threatLevel = threatLevel,
                scamCount = count,
                topCategory = topCategory,
                lastActiveTimestamp = lastActiveTime
            )
        }.sortedByDescending { it.scamCount }
    }

    suspend fun analyzeEvidence(
        evidenceType: EvidenceType,
        content: String,
        base64Image: String? = null,
        filePath: String? = null
    ): ScamAnalysisResult {
        val startTime = System.currentTimeMillis()
        val result = threatIntelRepo.analyzeEvidence(evidenceType, content, base64Image)
        val durationMs = System.currentTimeMillis() - startTime

        // Compute digital forensic SHA-256 fingerprint
        val hash = try {
            val md = MessageDigest.getInstance("SHA-256")
            val inputBytes = (content + (base64Image ?: "")).toByteArray(Charsets.UTF_8)
            md.digest(inputBytes).joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }

        // 1. Persist EvidenceItem in Room
        val evidenceItem = EvidenceItem(
            title = "${evidenceType.label} Evidence #${System.currentTimeMillis() % 10000}",
            evidenceType = evidenceType,
            filePath = filePath,
            extractedContent = result.extractedContent.ifBlank { content },
            mimeType = if (base64Image != null) "image/jpeg" else "text/plain",
            fileSizeBytes = content.toByteArray().size.toLong() + (base64Image?.length ?: 0),
            sha256Hash = hash,
            collectedTimestamp = System.currentTimeMillis(),
            sourceDescription = "Collected via CrimeLens ${evidenceType.label} Inspector",
            associatedRiskScore = result.riskScore,
            associatedCategory = result.scamCategory
        )
        val evidenceId = evidenceDao.insertEvidence(evidenceItem)

        // 2. Persist AnalysisLog in Room
        val threatLevel = when {
            result.riskScore >= 80 -> "CRITICAL"
            result.riskScore >= 60 -> "HIGH"
            result.riskScore >= 40 -> "MODERATE"
            else -> "LOW"
        }
        val analysisLog = AnalysisLog(
            evidenceItemId = evidenceId,
            timestamp = System.currentTimeMillis(),
            evidenceType = evidenceType,
            riskScore = result.riskScore,
            scamCategory = result.scamCategory,
            threatLevel = threatLevel,
            primarySummary = result.primarySummary,
            observedEvidence = result.observedEvidence,
            inference = result.inference,
            externalIntelligence = result.externalIntelligence,
            recommendation = result.recommendation,
            redFlagsJson = result.redFlags.joinToString("||"),
            recommendationsJson = result.recommendations.joinToString("||"),
            extractedContent = result.extractedContent,
            engineName = "Gemini-2.5-Flash (Forensic Threat Intel)",
            executionTimeMs = durationMs
        )
        analysisLogDao.insertLog(analysisLog)

        // 3. Save ScanRecord for compatibility with dashboard
        val scanRecord = ScanRecord(
            title = "${evidenceType.label} Analysis",
            evidenceType = evidenceType,
            riskScore = result.riskScore,
            category = result.scamCategory,
            summary = result.primarySummary,
            redFlagsJson = result.redFlags.joinToString("||"),
            recommendationsJson = result.recommendations.joinToString("||"),
            extractedText = result.extractedContent,
            rawContent = content
        )
        scanDao.insertScan(scanRecord)

        updateScanAchievements()
        return result
    }

    suspend fun insertEvidenceItem(evidence: EvidenceItem): Long = evidenceDao.insertEvidence(evidence)

    suspend fun insertAnalysisLog(log: AnalysisLog): Long = analysisLogDao.insertLog(log)

    suspend fun deleteEvidenceById(id: Long) = evidenceDao.deleteEvidenceById(id)

    suspend fun deleteAnalysisLogById(id: Long) = analysisLogDao.deleteLogById(id)

    suspend fun getScanById(id: Long): ScanRecord? = scanDao.getScanById(id)

    suspend fun upvoteReport(reportId: String) {
        firestoreScamRepo.upvoteReport(reportId)
        try { reportDao.upvoteReport(reportId) } catch (e: Exception) {}
    }

    suspend fun addCommunityReport(
        title: String,
        category: ScamCategory,
        city: String,
        description: String,
        imageBase64: String? = null,
        imageUrl: String? = null,
        reporterName: String = "Investigator",
        reporterId: String = ""
    ) {
        val report = CommunityReport(
            scamTitle = title,
            category = category,
            locationCity = city,
            description = description,
            reportDate = System.currentTimeMillis(),
            upvotes = 1,
            verifiedScam = true,
            severity = "HIGH",
            source = "User Report",
            imageBase64 = imageBase64,
            imageUrl = imageUrl,
            reporterName = reporterName,
            reporterId = reporterId
        )

        // Write directly to Firestore real-time backend
        firestoreScamRepo.addCommunityReport(report)
        try { reportDao.insertReport(report) } catch (e: Exception) {}

        achievementDao.unlockAchievement("fraud_fighter", System.currentTimeMillis())
    }

    private suspend fun updateScanAchievements() {
        val currentCount = scanDao.getScanCount().firstOrNull() ?: 0
        if (currentCount >= 1) {
            achievementDao.unlockAchievement("first_scan", System.currentTimeMillis())
        }
        if (currentCount >= 5) {
            achievementDao.unlockAchievement("cyber_detective", System.currentTimeMillis())
        }
    }

    suspend fun seedAchievementsIfEmpty() = withContext(Dispatchers.IO) {
        val existingAchievements = achievementDao.getAllAchievements().firstOrNull() ?: emptyList()
        if (existingAchievements.isEmpty()) {
            val initialBadges = listOf(
                UserAchievement(
                    badgeId = "first_scan",
                    title = "Scam Hunter",
                    description = "Analyze your first digital evidence using CrimeLens AI",
                    icon = "search",
                    isUnlocked = false
                ),
                UserAchievement(
                    badgeId = "cyber_detective",
                    title = "Cyber Detective",
                    description = "Perform 5 or more digital evidence security checks",
                    icon = "badge",
                    isUnlocked = false
                ),
                UserAchievement(
                    badgeId = "fraud_fighter",
                    title = "Fraud Fighter",
                    description = "Submit an anonymous scam report to protect the community",
                    icon = "shield",
                    isUnlocked = false
                ),
                UserAchievement(
                    badgeId = "digital_guardian",
                    title = "Digital Guardian",
                    description = "Complete all cybersecurity interactive learning modules",
                    icon = "verified_user",
                    isUnlocked = false
                )
            )
            achievementDao.insertAchievements(initialBadges)
        }
    }

    fun getLearningModules(): List<LearningModule> {
        return listOf(
            LearningModule(
                id = "mod_1",
                title = "UPI & QR Code Traps",
                description = "Master how UPI PIN works and spot QR refund scams instantly",
                category = ScamCategory.UPI_FRAUD,
                readTimeMinutes = 3,
                lessonContent = "GOLDEN RULE: You NEVER enter your UPI PIN to RECEIVE money. Entering your UPI PIN ALWAYS DEDUCTS money from your bank account. Scammers send fake QR codes labelled 'Scan to Receive Refund'. Scanning and entering PIN transfers money directly to the fraudster.",
                quiz = listOf(
                    QuizQuestion(
                        id = 1,
                        question = "Someone sends you a QR code claiming 'Scan to receive ₹1,000 refund'. What happens if you enter your UPI PIN?",
                        options = listOf(
                            "₹1,000 is credited to your bank account",
                            "₹1,000 is DEDUCTED from your bank account",
                            "Nothing happens",
                            "Your bank account is frozen"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Entering your UPI PIN ALWAYS approves a payment FROM your account. You NEVER need a PIN to receive money."
                    ),
                    QuizQuestion(
                        id = 2,
                        question = "Which VPA handle format looks suspicious?",
                        options = listOf(
                            "merchant@sbi",
                            "customer-support-sbi-refund@okaxis.top",
                            "user@okicici",
                            "store@paytm"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "Scammers create misleading long handles with bogus top-level domains like .top or .xyz."
                    )
                )
            ),
            LearningModule(
                id = "mod_2",
                title = "Digital Arrest & Fake Police Scams",
                description = "Recognize fear tactics, fake arrest warrants, and video call impersonation",
                category = ScamCategory.FAKE_POLICE,
                readTimeMinutes = 4,
                lessonContent = "Police, CBI, Customs, or Enforcement Directorate will NEVER conduct arrests over WhatsApp video calls, nor will they ask you to transfer money to 'clear your name'. Real law enforcement follows formal legal notice procedures.",
                quiz = listOf(
                    QuizQuestion(
                        id = 1,
                        question = "A caller claiming to be a Police Officer demands ₹50,000 on Google Pay to stop your arrest over a seized parcel. What should you do?",
                        options = listOf(
                            "Pay immediately to avoid legal trouble",
                            "Disconnect, block the number, and dial Cybercrime Helpline 1930",
                            "Negotiate for a lower amount",
                            "Share your Aadhaar details"
                        ),
                        correctAnswerIndex = 1,
                        explanation = "No genuine police officer demands money over UPI to stop arrest. It is a 100% digital arrest scam."
                    )
                )
            ),
            LearningModule(
                id = "mod_3",
                title = "Part-Time Job & Task Scams",
                description = "How fraudsters lure victims with simple tasks before prepaid traps",
                category = ScamCategory.JOB_OFFER,
                readTimeMinutes = 3,
                lessonContent = "Scammers offer 'Like YouTube videos for ₹150'. After paying ₹300 to gain trust, they add you to a Telegram group and demand ₹10,000 'prepaid investment' to unlock earnings, stealing all funds.",
                quiz = listOf(
                    QuizQuestion(
                        id = 1,
                        question = "What is a major red flag in online work-from-home job offers?",
                        options = listOf(
                            "They ask you to pay money upfront to get assigned tasks",
                            "Communication is exclusively on unverified Telegram channels",
                            "Promises exorbitant earnings for trivial tasks",
                            "All of the above"
                        ),
                        correctAnswerIndex = 3,
                        explanation = "All three are classic indicators of prepaid job scams."
                    )
                )
            )
        )
    }
}

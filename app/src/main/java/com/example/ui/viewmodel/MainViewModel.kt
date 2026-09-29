package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AnalysisLog
import com.example.data.model.AuthUser
import com.example.data.model.CityHeatData
import com.example.data.model.CommunityReport
import com.example.data.model.EvidenceItem
import com.example.data.model.EvidenceType
import com.example.data.model.LearningModule
import com.example.data.model.ScamAnalysisResult
import com.example.data.model.ScamCategory
import com.example.data.model.ScanRecord
import com.example.data.model.UiState
import com.example.data.model.UserAchievement
import com.example.data.remote.FirebaseAuthRepository
import com.example.data.repository.AppRepository
import com.example.util.CrimeLensNotificationManager
import com.example.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AnalysisState {
    data object Idle : AnalysisState
    data object Analyzing : AnalysisState
    data class Success(val result: ScamAnalysisResult) : AnalysisState
    data class Error(val message: String) : AnalysisState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val authRepository = FirebaseAuthRepository()
    private val repository = AppRepository(
        db.scanDao(),
        db.reportDao(),
        db.achievementDao(),
        db.evidenceDao(),
        db.analysisLogDao()
    )

    // Real-time Firebase Auth User StateFlow
    val authUser: StateFlow<AuthUser> = authRepository.currentUser

    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

    val scanHistory: StateFlow<List<ScanRecord>> = repository.allScans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scanCount: StateFlow<Int> = repository.scanCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val highRiskCount: StateFlow<Int> = repository.highRiskScanCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val evidenceItems: StateFlow<List<EvidenceItem>> = repository.allEvidence
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val evidenceCount: StateFlow<Int> = repository.evidenceCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val analysisLogs: StateFlow<List<AnalysisLog>> = repository.allAnalysisLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val highRiskAnalysisLogs: StateFlow<List<AnalysisLog>> = repository.highRiskAnalysisLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _timeFilter = MutableStateFlow(TimeUtils.TimeFilter.ALL_TIME)
    val timeFilter: StateFlow<TimeUtils.TimeFilter> = _timeFilter.asStateFlow()

    // Real-time Community Reports StateFlow with UiState wrapping
    val reportsState: StateFlow<UiState<List<CommunityReport>>> = repository.communityReports
        .map { reports ->
            if (reports.isEmpty()) {
                UiState.Empty("No recent reports available")
            } else {
                UiState.Success(reports)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    // Dynamic Threat Density calculated from actual real-time Firestore reports
    val threatDensity: StateFlow<List<CityHeatData>> = combine(repository.communityReports, _timeFilter) { reports, filter ->
        repository.getThreatDensity(reports, filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<UserAchievement>> = repository.achievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _analysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analysisState: StateFlow<AnalysisState> = _analysisState.asStateFlow()

    private val _selectedScanDetail = MutableStateFlow<ScanRecord?>(null)
    val selectedScanDetail: StateFlow<ScanRecord?> = _selectedScanDetail.asStateFlow()

    val learningModules: List<LearningModule> = repository.getLearningModules()

    private val _userPoints = MutableStateFlow(0)
    val userPoints: StateFlow<Int> = _userPoints.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _showSimulatorDialog = MutableStateFlow(false)
    val showSimulatorDialog: StateFlow<Boolean> = _showSimulatorDialog.asStateFlow()

    private var knownReportIds = setOf<String>()

    init {
        viewModelScope.launch {
            repository.seedAchievementsIfEmpty()
        }

        // Monitor real-time report additions for notifications
        viewModelScope.launch {
            try {
                repository.communityReports.collect { reports ->
                    try {
                        if (knownReportIds.isNotEmpty()) {
                            val newReports = reports.filter { it.id !in knownReportIds }
                            for (newReport in newReports) {
                                if (newReport.verifiedScam || newReport.severity.equals("HIGH", ignoreCase = true)) {
                                    CrimeLensNotificationManager.sendScamReportNotification(
                                        getApplication(),
                                        newReport
                                    )
                                }
                            }
                        }
                        knownReportIds = reports.map { it.id }.toSet()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setTimeFilter(filter: TimeUtils.TimeFilter) {
        _timeFilter.value = filter
    }

    fun openSimulator() {
        _showSimulatorDialog.value = true
    }

    fun closeSimulator() {
        _showSimulatorDialog.value = false
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }

    fun analyzeEvidence(
        evidenceType: EvidenceType,
        content: String,
        base64Image: String? = null,
        filePath: String? = null
    ) {
        if (content.isBlank() && base64Image.isNullOrBlank()) {
            _analysisState.value = AnalysisState.Error("Please provide text, URL, audio transcript, or image evidence to analyze.")
            return
        }

        viewModelScope.launch {
            _analysisState.value = AnalysisState.Analyzing
            try {
                val result = repository.analyzeEvidence(evidenceType, content, base64Image, filePath)
                _analysisState.value = AnalysisState.Success(result)
                _userPoints.value += 20 // Award points for completing forensic analysis
            } catch (e: Exception) {
                _analysisState.value = AnalysisState.Error(e.localizedMessage ?: "Evidence analysis failed.")
            }
        }
    }

    fun deleteEvidenceItem(id: Long) {
        viewModelScope.launch {
            repository.deleteEvidenceById(id)
        }
    }

    fun deleteAnalysisLog(id: Long) {
        viewModelScope.launch {
            repository.deleteAnalysisLogById(id)
        }
    }

    fun resetAnalysisState() {
        _analysisState.value = AnalysisState.Idle
    }

    fun selectScanDetail(record: ScanRecord?) {
        _selectedScanDetail.value = record
    }

    fun upvoteReport(id: String) {
        viewModelScope.launch {
            repository.upvoteReport(id)
        }
    }

    fun openAuthDialog() {
        _showAuthDialog.value = true
    }

    fun closeAuthDialog() {
        _showAuthDialog.value = false
    }

    fun signIn(email: String, password: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val res = authRepository.signIn(email, password)
            onResult(res.map { })
        }
    }

    fun register(email: String, password: String, displayName: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val res = authRepository.register(email, password, displayName)
            onResult(res.map { })
        }
    }

    fun signInAnonymously(onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val res = authRepository.signInAnonymously()
            onResult(res.map { })
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun submitCommunityReport(
        title: String,
        category: ScamCategory,
        city: String,
        description: String,
        imageBase64: String? = null,
        imageUrl: String? = null
    ) {
        val user = authUser.value
        val reporter = if (user.isAuthenticated) user.displayLabel else "Investigator"
        val reporterId = user.uid

        viewModelScope.launch {
            repository.addCommunityReport(
                title = title,
                category = category,
                city = city,
                description = description,
                imageBase64 = imageBase64,
                imageUrl = imageUrl,
                reporterName = reporter,
                reporterId = reporterId
            )
            _userPoints.value += 50
        }
    }

    fun completeQuiz(score: Int) {
        _userPoints.value += score * 10
    }
}

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analysis_logs")
data class AnalysisLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val evidenceItemId: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val evidenceType: EvidenceType,
    val riskScore: Int,
    val scamCategory: ScamCategory,
    val threatLevel: String, // "CRITICAL", "HIGH", "MODERATE", "LOW"
    val primarySummary: String,
    val observedEvidence: String = "",
    val inference: String = "",
    val externalIntelligence: String = "",
    val recommendation: String = "",
    val redFlagsJson: String = "",
    val recommendationsJson: String = "",
    val extractedContent: String = "",
    val engineName: String = "Gemini-2.5-Flash",
    val executionTimeMs: Long = 0L
)

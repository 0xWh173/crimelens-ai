package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "evidence_items")
data class EvidenceItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val evidenceType: EvidenceType,
    val filePath: String? = null,
    val extractedContent: String = "",
    val mimeType: String = "image/jpeg",
    val fileSizeBytes: Long = 0L,
    val sha256Hash: String = "",
    val collectedTimestamp: Long = System.currentTimeMillis(),
    val sourceDescription: String = "",
    val associatedRiskScore: Int = 0,
    val associatedCategory: ScamCategory = ScamCategory.UNKNOWN
)

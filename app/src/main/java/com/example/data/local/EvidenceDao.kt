package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.EvidenceItem
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenceDao {
    @Query("SELECT * FROM evidence_items ORDER BY collectedTimestamp DESC")
    fun getAllEvidence(): Flow<List<EvidenceItem>>

    @Query("SELECT * FROM evidence_items WHERE id = :id")
    suspend fun getEvidenceById(id: Long): EvidenceItem?

    @Query("SELECT COUNT(*) FROM evidence_items")
    fun getEvidenceCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidence(evidence: EvidenceItem): Long

    @Query("DELETE FROM evidence_items WHERE id = :id")
    suspend fun deleteEvidenceById(id: Long)

    @Query("DELETE FROM evidence_items")
    suspend fun clearAllEvidence()
}

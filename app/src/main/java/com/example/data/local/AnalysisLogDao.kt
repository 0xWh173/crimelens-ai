package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AnalysisLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisLogDao {
    @Query("SELECT * FROM analysis_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AnalysisLog>>

    @Query("SELECT * FROM analysis_logs WHERE evidenceItemId = :evidenceId ORDER BY timestamp DESC")
    fun getLogsForEvidence(evidenceId: Long): Flow<List<AnalysisLog>>

    @Query("SELECT * FROM analysis_logs WHERE riskScore >= 70 ORDER BY timestamp DESC")
    fun getHighRiskLogs(): Flow<List<AnalysisLog>>

    @Query("SELECT * FROM analysis_logs WHERE id = :id")
    suspend fun getLogById(id: Long): AnalysisLog?

    @Query("SELECT COUNT(*) FROM analysis_logs")
    fun getLogCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AnalysisLog): Long

    @Query("DELETE FROM analysis_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM analysis_logs")
    suspend fun clearAllLogs()
}

package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CommunityReport
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM community_reports ORDER BY reportDate DESC")
    fun getAllCommunityReports(): Flow<List<CommunityReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<CommunityReport>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: CommunityReport): Long

    @Query("UPDATE community_reports SET upvotes = upvotes + 1 WHERE id = :id")
    suspend fun upvoteReport(id: String)
}

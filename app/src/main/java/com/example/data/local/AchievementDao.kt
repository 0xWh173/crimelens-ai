package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.UserAchievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<UserAchievement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<UserAchievement>)

    @Query("UPDATE achievements SET isUnlocked = 1, progress = maxProgress, unlockedTimestamp = :timestamp WHERE badgeId = :badgeId")
    suspend fun unlockAchievement(badgeId: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE achievements SET progress = :progress WHERE badgeId = :badgeId")
    suspend fun updateProgress(badgeId: String, progress: Int)
}

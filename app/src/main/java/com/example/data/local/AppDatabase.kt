package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.AnalysisLog
import com.example.data.model.CommunityReport
import com.example.data.model.EvidenceItem
import com.example.data.model.ScanRecord
import com.example.data.model.UserAchievement

@Database(
    entities = [
        ScanRecord::class,
        CommunityReport::class,
        UserAchievement::class,
        EvidenceItem::class,
        AnalysisLog::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
    abstract fun reportDao(): ReportDao
    abstract fun achievementDao(): AchievementDao
    abstract fun evidenceDao(): EvidenceDao
    abstract fun analysisLogDao(): AnalysisLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "crimelens_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

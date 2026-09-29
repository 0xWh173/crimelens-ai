package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.EvidenceType
import com.example.data.model.ScamCategory

class Converters {
    @TypeConverter
    fun fromScamCategory(value: ScamCategory): String = value.name

    @TypeConverter
    fun toScamCategory(value: String): ScamCategory = try {
        ScamCategory.valueOf(value)
    } catch (e: Exception) {
        ScamCategory.UNKNOWN
    }

    @TypeConverter
    fun fromEvidenceType(value: EvidenceType): String = value.name

    @TypeConverter
    fun toEvidenceType(value: String): EvidenceType = try {
        EvidenceType.valueOf(value)
    } catch (e: Exception) {
        EvidenceType.SCREENSHOT
    }
}

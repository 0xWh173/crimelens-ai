package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeUtils {

    enum class TimeFilter(val displayName: String) {
        LAST_HOUR("Last Hour"),
        TODAY("Today"),
        LAST_7_DAYS("Last 7 Days"),
        LAST_30_DAYS("Last 30 Days"),
        ALL_TIME("All Time")
    }

    /**
     * Converts epoch milliseconds into human-readable relative time strings.
     * Examples: "Just now", "8 min ago", "2 hr ago", "Yesterday", "3 days ago"
     */
    fun formatRelativeTime(timestampMs: Long): String {
        if (timestampMs <= 0) return "Unknown time"
        val now = System.currentTimeMillis()
        val diffMs = now - timestampMs

        if (diffMs < 0) return "Just now"

        val seconds = diffMs / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 45 -> "Just now"
            minutes < 60 -> "${minutes} min ago"
            hours < 24 -> "${hours} hr ago"
            days == 1L -> "Yesterday"
            days < 7 -> "${days} days ago"
            else -> {
                val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                sdf.format(Date(timestampMs))
            }
        }
    }

    /**
     * Checks whether a given timestamp falls within the specified time filter range.
     */
    fun isWithinFilter(timestampMs: Long, filter: TimeFilter): Boolean {
        if (filter == TimeFilter.ALL_TIME) return true
        val now = System.currentTimeMillis()
        val diffMs = now - timestampMs
        if (diffMs < 0) return true // Future or clock skew

        val oneHourMs = 60 * 60 * 1000L
        val oneDayMs = 24 * 60 * 60 * 1000L

        return when (filter) {
            TimeFilter.LAST_HOUR -> diffMs <= oneHourMs
            TimeFilter.TODAY -> diffMs <= oneDayMs
            TimeFilter.LAST_7_DAYS -> diffMs <= 7 * oneDayMs
            TimeFilter.LAST_30_DAYS -> diffMs <= 30 * oneDayMs
            TimeFilter.ALL_TIME -> true
        }
    }
}

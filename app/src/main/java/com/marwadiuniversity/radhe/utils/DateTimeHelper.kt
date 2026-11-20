package com.marwadiuniversity.radhe.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeHelper {
    private val displayFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val dateOnlyIsoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getCurrentIsoTimestamp(): String {
        return isoFormat.format(Date())
    }

    fun getCurrentDateIso(): String {
        return dateOnlyIsoFormat.format(Date())
    }

    fun formatTimestampForDisplay(timestamp: Long): String {
        return displayFormat.format(Date(timestamp))
    }

    fun formatDateForDisplay(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun parseIsoTimestamp(iso: String): Long {
        return try {
            isoFormat.parse(iso)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}

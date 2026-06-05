package com.example.ui

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val formats = listOf(
        "dd-MM-yyyy",
        "yyyy-MM-dd"
    )

    fun parseDate(dateStr: String): Date {
        if (dateStr.isBlank()) return Date()
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                sdf.isLenient = false
                val parsed = sdf.parse(dateStr.trim())
                if (parsed != null) return parsed
            } catch (e: Exception) {
                // Ignore and try next format
            }
        }
        return Date()
    }

    fun formatToIndianDate(date: Date): String {
        return SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date)
    }

    fun formatToIndianDate(dateStr: String): String {
        if (dateStr.isBlank()) return ""
        val date = parseDate(dateStr)
        return formatToIndianDate(date)
    }
    
    fun getTodayIndianDate(): String {
        return formatToIndianDate(Date())
    }
}

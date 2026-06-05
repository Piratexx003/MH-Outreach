package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doctors")
data class Doctor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mobile: String
)

@Entity(tableName = "attenders")
data class Attender(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mobile: String
)

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val regNo: String,
    val date: String, // YYYY-MM-DD
    val outreach: String,
    val doctorName: String,
    val attenderName: String,
    val patientName: String,
    val age: Int,
    val sex: String,
    val type: String, // "New" or "Old"
    val diagnosis: String,
    val categoriesString: String // Comma separated list (e.g., "CMD,SMD")
) {
    fun getCategoryList(): List<String> {
        if (categoriesString.isBlank()) return emptyList()
        return categoriesString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}

@Entity(tableName = "schedules")
data class OutreachSchedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String,      // YYYY-MM-DD
    val targetMonth: String,     // e.g. "August 2026"
    val creationMonth: String,   // e.g. "June 2026"
    val doctorName: String,
    val attenderName: String,
    val location: String
)

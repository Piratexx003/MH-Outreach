package com.example.ui

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.Patient
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object ReportExporter {

    private fun escapeCsv(value: String): String {
        val cleanValue = value.replace("\n", " ").replace("\r", " ").trim()
        return if (cleanValue.contains(",") || cleanValue.contains("\"")) {
            "\"" + cleanValue.replace("\"", "\"\"") + "\""
        } else {
            cleanValue
        }
    }

    private fun getFilteredPatients(patients: List<Patient>, fromDate: String, toDate: String): List<Patient> {
        val start = if (fromDate.isNotBlank()) DateUtils.parseDate(fromDate) else null
        val end = if (toDate.isNotBlank()) DateUtils.parseDate(toDate) else null

        return patients.filter { p ->
            try {
                val pDate = DateUtils.parseDate(p.date)
                val matchesStart = start == null || !pDate.before(start)
                val matchesEnd = end == null || !pDate.after(end)
                matchesStart && matchesEnd
            } catch (e: Exception) {
                true // Include if parsing fails
            }
        }
    }

    fun shareReport(context: Context, type: String, patientsList: List<Patient>, fromDate: String, toDate: String) {
        val filtered = getFilteredPatients(patientsList, fromDate, toDate)
        if (filtered.isEmpty()) {
            return
        }

        val dateSuffix = "${if(fromDate.isBlank()) "Start" else fromDate}_to_${if(toDate.isBlank()) "End" else toDate}"
        val csvHeader: String
        val csvContent = StringBuilder()
        val filename: String

        when (type) {
            "log" -> {
                filename = "Complete_Patient_Log_$dateSuffix.csv"
                csvHeader = "Date,Registration No.,Patient Name,Age,Sex,Visit Type,Outreach Centre,Doctor,Attender,Diagnosis,Mental Health Categories"
                csvContent.append(csvHeader).append("\n")
                filtered.forEach { p ->
                    csvContent.append(escapeCsv(DateUtils.formatToIndianDate(p.date))).append(",")
                        .append(escapeCsv(p.regNo)).append(",")
                        .append(escapeCsv(p.patientName)).append(",")
                        .append(p.age).append(",")
                        .append(escapeCsv(p.sex)).append(",")
                        .append(escapeCsv(p.type)).append(",")
                        .append(escapeCsv(p.outreach)).append(",")
                        .append(escapeCsv(p.doctorName)).append(",")
                        .append(escapeCsv(p.attenderName)).append(",")
                        .append(escapeCsv(p.diagnosis)).append(",")
                        .append(escapeCsv(p.categoriesString))
                        .append("\n")
                }
            }
            "category" -> {
                filename = "Category_Summary_$dateSuffix.csv"
                csvHeader = "Mental Health Category,Total Cases"
                csvContent.append(csvHeader).append("\n")

                val counts = mapOf("CMD" to 0, "SMD" to 0, "SUD" to 0, "Epilepsy" to 0, "Dementia" to 0).toMutableMap()
                filtered.forEach { p ->
                    val cats = p.getCategoryList()
                    cats.forEach { c ->
                        if (counts.containsKey(c)) {
                            counts[c] = counts[c]!! + 1
                        }
                    }
                }
                counts.forEach { (cat, count) ->
                    csvContent.append(escapeCsv(cat)).append(",").append(count).append("\n")
                }
            }
            "outreach" -> {
                filename = "Outreach_Summary_$dateSuffix.csv"
                csvHeader = "Outreach Centre,Total Patients"
                csvContent.append(csvHeader).append("\n")

                val counts = mutableMapOf<String, Int>()
                filtered.forEach { p ->
                    counts[p.outreach] = (counts[p.outreach] ?: 0) + 1
                }
                counts.forEach { (centre, count) ->
                    csvContent.append(escapeCsv(centre)).append(",").append(count).append("\n")
                }
            }
            "doctor" -> {
                filename = "Doctor_Summary_$dateSuffix.csv"
                csvHeader = "Doctor,Total Patients Attended"
                csvContent.append(csvHeader).append("\n")

                val counts = mutableMapOf<String, Int>()
                filtered.forEach { p ->
                    counts[p.doctorName] = (counts[p.doctorName] ?: 0) + 1
                }
                counts.forEach { (doc, count) ->
                    csvContent.append(escapeCsv(doc)).append(",").append(count).append("\n")
                }
            }
            "date-summary" -> {
                filename = "Camp_Date_Summary_$dateSuffix.csv"
                // Match SheetJS multi-row header model layout exactly as requested
                csvContent.append("Camp Date,New Patient,,Old Patient,,Total Patient,\n")
                csvContent.append(",Male,Female,Male,Female,Male,Female\n")

                data class DailyStats(
                    var newM: Int = 0, var newF: Int = 0,
                    var oldM: Int = 0, var oldF: Int = 0,
                    var totM: Int = 0, var totF: Int = 0
                )

                val statsMap = mutableMapOf<String, DailyStats>()
                filtered.forEach { p ->
                    val indianDateStr = DateUtils.formatToIndianDate(p.date)
                    val stats = statsMap.getOrPut(indianDateStr) { DailyStats() }
                    if (p.type == "New") {
                        if (p.sex == "Male") stats.newM++ else if (p.sex == "Female") stats.newF++
                    } else if (p.type == "Old") {
                        if (p.sex == "Male") stats.oldM++ else if (p.sex == "Female") stats.oldF++
                    }
                    if (p.sex == "Male") stats.totM++ else if (p.sex == "Female") stats.totF++
                }

                statsMap.keys.sortedWith { d1, d2 ->
                    val date1 = DateUtils.parseDate(d1)
                    val date2 = DateUtils.parseDate(d2)
                    date2.compareTo(date1)
                }.forEach { d ->
                    val s = statsMap[d]!!
                    csvContent.append(escapeCsv(d)).append(",")
                        .append(s.newM).append(",")
                        .append(s.newF).append(",")
                        .append(s.oldM).append(",")
                        .append(s.oldF).append(",")
                        .append(s.totM).append(",")
                        .append(s.totF)
                        .append("\n")
                }
            }
            else -> return
        }

        try {
            val cacheDir = File(context.cacheDir, "exports")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val file = File(cacheDir, filename)
            file.writeText(csvContent.toString())

            val uri = FileProvider.getUriForFile(context, "com.example.fileprovider", file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                setType("text/csv")
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "MH Outreach Report: ${type.uppercase(Locale.getDefault())}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Report via"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareJsonBackup(context: Context, backupJson: String) {
        val filename = "MH_Outreach_Backup_${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}.json"
        try {
            val cacheDir = File(context.cacheDir, "exports")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val file = File(cacheDir, filename)
            file.writeText(backupJson)

            val uri = FileProvider.getUriForFile(context, "com.example.fileprovider", file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "MH Outreach System Data Backup")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Save or Send Backup"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exportBackupToDownloads(context: Context, backupJson: String): String? {
        val filename = "MH_Outreach_Backup_${SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault()).format(Date())}.json"
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/MH Outreach")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(backupJson.toByteArray())
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    return "MH Outreach/$filename"
                }
            } else {
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = File(downloadDir, "MH Outreach")
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }
                val file = File(targetDir, filename)
                file.writeText(backupJson)
                return "MH Outreach/$filename"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}

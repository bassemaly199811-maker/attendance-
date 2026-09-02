package com.example.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.AttendanceRecord
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility to export attendance records into a well-formed RFC 4180 CSV file,
 * saving to the app's cache directory and returning a secure FileProvider content Uri.
 */
object CsvExporter {

  /**
   * Builds and writes attendance records into a CSV file in cache, returning a FileProvider content URI.
   */
  fun exportAttendanceToCsv(context: Context, records: List<AttendanceRecord>): Uri {
    require(records.isNotEmpty()) { "Cannot export an empty list of attendance records." }

    val exportsDir = File(context.cacheDir, "exports").apply {
      if (!exists()) {
        mkdirs()
      }
    }

    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val file = File(exportsDir, "attendance_$timestamp.csv")

    val headers = listOf(
      "Worker Name",
      "Site",
      "Date",
      "Check-In Time",
      "Check-Out Time",
      "Status",
      "Hours Worked",
      "Check-In Location",
      "Check-Out Location",
      "Late Arrival",
      "Early Departure",
      "Notes",
    )

    BufferedWriter(FileWriter(file)).use { writer ->
      // Write UTF-8 BOM for Microsoft Excel compatibility
      writer.write("\uFEFF")

      // Write Header
      writer.write(headers.joinToString(",") { escapeCsvField(it) })
      writer.newLine()

      // Write Records
      for (record in records) {
        val workerName = record.workerName.ifBlank { "Primary Staff" }
        val site = record.siteName.ifBlank { "N/A" }
        val date = record.workDate
        val checkIn = record.checkInTime ?: "—"
        val checkOut = record.checkOutTime ?: "—"
        val status = record.status.name.replace("_", " ").lowercase(Locale.ROOT).replaceFirstChar {
          if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }
        val hoursWorked = calculateHoursWorked(record.checkInTime, record.checkOutTime)

        val checkInLocation = if (record.checkInLat != 0.0 || record.checkInLng != 0.0) {
          String.format(Locale.US, "%.5f, %.5f", record.checkInLat, record.checkInLng)
        } else "—"

        val checkOutLocation = if (record.checkOutLat != null && record.checkOutLng != null && (record.checkOutLat != 0.0 || record.checkOutLng != 0.0)) {
          String.format(Locale.US, "%.5f, %.5f", record.checkOutLat, record.checkOutLng)
        } else "—"

        val isLate = if (record.isLate) "Yes" else "No"
        val isEarly = if (record.isEarlyDeparture) "Yes" else "No"
        val notes = record.notes ?: ""

        val row = listOf(
          workerName,
          site,
          date,
          checkIn,
          checkOut,
          status,
          hoursWorked,
          checkInLocation,
          checkOutLocation,
          isLate,
          isEarly,
          notes,
        )

        writer.write(row.joinToString(",") { escapeCsvField(it) })
        writer.newLine()
      }
    }

    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, file)
  }

  /**
   * Properly escapes CSV fields per RFC 4180:
   * Wraps in double quotes and doubles up any interior double quotes.
   */
  fun escapeCsvField(value: String?): String {
    if (value == null) return "\"\""
    val escaped = value.replace("\"", "\"\"")
    return "\"$escaped\""
  }

  /**
   * Calculates the difference in hours between checkIn and checkOut.
   */
  fun calculateHoursWorked(checkInTime: String?, checkOutTime: String?): String {
    if (checkInTime.isNullOrBlank() || checkOutTime.isNullOrBlank()) {
      return "—"
    }

    val formats = listOf(
      SimpleDateFormat("hh:mm a", Locale.ENGLISH),
      SimpleDateFormat("h:mm a", Locale.ENGLISH),
      SimpleDateFormat("HH:mm", Locale.ENGLISH),
      SimpleDateFormat("hh:mma", Locale.ENGLISH),
      SimpleDateFormat("h:mma", Locale.ENGLISH),
    )

    var parsedIn: Date? = null
    var parsedOut: Date? = null

    for (format in formats) {
      if (parsedIn == null) {
        try { parsedIn = format.parse(checkInTime.trim()) } catch (_: Exception) {}
      }
      if (parsedOut == null) {
        try { parsedOut = format.parse(checkOutTime.trim()) } catch (_: Exception) {}
      }
    }

    if (parsedIn == null || parsedOut == null) {
      return "—"
    }

    var diffMillis = parsedOut.time - parsedIn.time
    if (diffMillis < 0) {
      // Crossed midnight
      diffMillis += 24 * 60 * 60 * 1000
    }

    val totalMinutes = diffMillis / (1000 * 60)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return if (hours > 0 && minutes > 0) {
      "${hours}h ${minutes}m"
    } else if (hours > 0) {
      "${hours}h"
    } else {
      "${minutes}m"
    }
  }
}

/**
 * Top-level function for exporting attendance records to CSV.
 */
fun exportAttendanceToCsv(context: Context, records: List<AttendanceRecord>): Uri {
  return CsvExporter.exportAttendanceToCsv(context, records)
}

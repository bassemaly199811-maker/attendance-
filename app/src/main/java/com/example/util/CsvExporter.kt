package com.example.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility to export attendance records into a comprehensive, well-formed RFC 4180 CSV file,
 * with detailed late arrival duration, overtime hours, day status, geofencing,
 * and a summary of total working days and hours per employee for the selected period.
 */
object CsvExporter {

  /**
   * Builds and writes attendance records into a CSV file in cache, returning a FileProvider content URI.
   */
  fun exportAttendanceToCsv(
    context: Context,
    records: List<AttendanceRecord>,
    periodLabel: String = "الفترة المحددة",
    workerDepartmentMap: Map<String, String> = emptyMap(),
  ): Uri {
    require(records.isNotEmpty()) { "Cannot export an empty list of attendance records." }

    val exportsDir = File(context.cacheDir, "exports").apply {
      if (!exists()) {
        mkdirs()
      }
    }

    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val file = File(exportsDir, "attendance_report_$timestamp.csv")

    val headers = listOf(
      "Worker Name",
      "Department",
      "Work Site",
      "Date",
      "Check-In",
      "Check-Out",
      "Late Duration",
      "Overtime",
      "Day Status",
      "Hours Worked",
      "Geofence",
      "Notes",
    )

    // Per-worker summary accumulator
    class WorkerSummary(
      var totalDays: Int = 0,
      var totalMinutesWorked: Long = 0,
      var totalLateMinutes: Long = 0,
      var totalOvertimeMinutes: Long = 0,
    )

    val summaries = mutableMapOf<String, WorkerSummary>()

    BufferedWriter(FileWriter(file)).use { writer ->
      // Write UTF-8 BOM for Microsoft Excel compatibility
      writer.write("\uFEFF")

      // Write Title & Metadata
      writer.write(escapeCsvField("Comprehensive Attendance Report - Smart Work"))
      writer.newLine()
      writer.write(escapeCsvField("Period: $periodLabel | Exported At: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).format(Date())}"))
      writer.newLine()
      writer.newLine()

      // Write Header
      writer.write(headers.joinToString(",") { escapeCsvField(it) })
      writer.newLine()

      val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())

      // Write Records
      for (record in records) {
        val workerName = record.workerName.ifBlank { "Staff" }
        val department = workerDepartmentMap[workerName] ?: "Operations"
        val site = record.siteName.ifBlank { "Main Site" }
        val date = record.workDate
        val checkIn = record.checkInTime ?: "—"
        val checkOut = record.checkOutTime ?: "—"

        // Calculate hours worked
        val minutesWorked = calculateMinutesWorked(record.checkInTime, record.checkOutTime)
        val hoursWorkedText = formatMinutesToHours(minutesWorked)

        // Calculate Late Duration (minutes)
        val lateMinutes = calculateLateMinutes(record)
        val lateText = if (lateMinutes > 0) "${lateMinutes} mins" else if (record.isLate) "Late" else "On Time"

        // Calculate Overtime (minutes above 8 hours = 480 mins)
        val standardShiftMinutes = 8 * 60
        val overtimeMinutes = if (minutesWorked > standardShiftMinutes) minutesWorked - standardShiftMinutes else 0
        val overtimeText = if (overtimeMinutes > 0) {
          String.format(Locale.ENGLISH, "%.1f hrs (%d mins)", overtimeMinutes / 60.0, overtimeMinutes)
        } else "0"

        val hasMissingCheckout = record.checkOutTime.isNullOrBlank() ||
          record.notes?.contains("لم يتم تسجيل الخروج", ignoreCase = true) == true ||
          record.notes?.contains("Auto-Closed", ignoreCase = true) == true ||
          record.notes?.contains("Missing Check-Out", ignoreCase = true) == true ||
          record.notes?.contains("غير مكتمل", ignoreCase = true) == true

        // Determine Day Status
        val dayStatus = when {
          record.notes?.contains("إجازة", ignoreCase = true) == true || record.notes?.contains("Leave", ignoreCase = true) == true -> "On Leave"
          record.status == AttendanceStatus.NOT_CHECKED_IN && record.checkInTime.isNullOrBlank() -> "Absent"
          hasMissingCheckout && record.workDate < todayStr -> if (record.isLate) "Incomplete (Late / No Exit)" else "Incomplete (No Check-Out)"
          hasMissingCheckout && record.status == AttendanceStatus.CHECKED_IN -> if (record.isLate) "Late Arrival (Active)" else "Checked In (Active)"
          hasMissingCheckout -> "Incomplete"
          record.isLate && record.isEarlyDeparture -> "Late & Early Exit"
          record.isLate -> "Late Arrival"
          record.isEarlyDeparture -> "Early Departure"
          record.status == AttendanceStatus.CHECKED_OUT -> "Checked Out (Complete)"
          record.status == AttendanceStatus.CHECKED_IN -> "Checked In"
          else -> "Present"
        }

        val geofenceStatus = if (record.checkInDistanceMeters <= 200.0 && (record.checkInLat != 0.0 || record.checkInLng != 0.0)) {
          "Inside Geofence (${String.format(Locale.ENGLISH, "%.0fm", record.checkInDistanceMeters)})"
        } else if (record.checkInLat != 0.0 || record.checkInLng != 0.0) {
          "Outside Geofence (${String.format(Locale.ENGLISH, "%.0fm", record.checkInDistanceMeters)})"
        } else "—"

        val notes = record.notes ?: ""

        // Update Worker Summary
        val summary = summaries.getOrPut(workerName) { WorkerSummary() }
        if (minutesWorked > 0 || record.status == AttendanceStatus.CHECKED_IN || record.status == AttendanceStatus.CHECKED_OUT) {
          summary.totalDays += 1
          summary.totalMinutesWorked += minutesWorked
          summary.totalLateMinutes += lateMinutes
          summary.totalOvertimeMinutes += overtimeMinutes
        }

        val row = listOf(
          workerName,
          department,
          site,
          date,
          checkIn,
          checkOut,
          lateText,
          overtimeText,
          dayStatus,
          hoursWorkedText,
          geofenceStatus,
          notes,
        )

        writer.write(row.joinToString(",") { escapeCsvField(it) })
        writer.newLine()
      }

      // Add Summary Section at the End
      writer.newLine()
      writer.newLine()
      writer.write(escapeCsvField("=== Worker Performance & Hours Summary ($periodLabel) ==="))
      writer.newLine()

      val summaryHeaders = listOf(
        "Worker Name",
        "Department",
        "Days Worked",
        "Total Hours",
        "Total Late Mins",
        "Overtime Hours",
      )
      writer.write(summaryHeaders.joinToString(",") { escapeCsvField(it) })
      writer.newLine()

      for ((wName, s) in summaries) {
        val dept = workerDepartmentMap[wName] ?: "Operations"
        val totalHoursFormatted = String.format(Locale.ENGLISH, "%.2f hrs", s.totalMinutesWorked / 60.0)
        val totalOtFormatted = String.format(Locale.ENGLISH, "%.2f hrs", s.totalOvertimeMinutes / 60.0)

        val sumRow = listOf(
          wName,
          dept,
          "${s.totalDays} days",
          totalHoursFormatted,
          "${s.totalLateMinutes} mins",
          totalOtFormatted,
        )
        writer.write(sumRow.joinToString(",") { escapeCsvField(it) })
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
   * Calculates difference in minutes between checkIn and checkOut.
   */
  fun calculateMinutesWorked(checkInTime: String?, checkOutTime: String?): Long {
    if (checkInTime.isNullOrBlank() || checkOutTime.isNullOrBlank()) {
      return 0L
    }

    val formats = listOf(
      SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH),
      SimpleDateFormat("h:mm:ss a", Locale.ENGLISH),
      SimpleDateFormat("hh:mm a", Locale.ENGLISH),
      SimpleDateFormat("h:mm a", Locale.ENGLISH),
      SimpleDateFormat("HH:mm:ss", Locale.ENGLISH),
      SimpleDateFormat("HH:mm", Locale.ENGLISH),
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
      return 0L
    }

    var diffMillis = parsedOut.time - parsedIn.time
    if (diffMillis < 0) {
      // Crossed midnight
      diffMillis += 24 * 60 * 60 * 1000
    }

    return diffMillis / (1000 * 60)
  }

  fun formatMinutesToHours(totalMinutes: Long): String {
    if (totalMinutes <= 0) return "—"
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

  /**
   * Calculates late duration in minutes if check-in was after 08:15 AM
   */
  private fun calculateLateMinutes(record: AttendanceRecord): Long {
    val checkIn = record.checkInTime ?: return 0L
    val formats = listOf(
      SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH),
      SimpleDateFormat("h:mm:ss a", Locale.ENGLISH),
      SimpleDateFormat("hh:mm a", Locale.ENGLISH),
      SimpleDateFormat("h:mm a", Locale.ENGLISH),
      SimpleDateFormat("HH:mm", Locale.ENGLISH),
    )

    var parsedIn: Date? = null
    for (format in formats) {
      if (parsedIn == null) {
        try { parsedIn = format.parse(checkIn.trim()) } catch (_: Exception) {}
      }
    }

    if (parsedIn == null) return if (record.isLate) 15L else 0L

    val cal = java.util.Calendar.getInstance().apply { time = parsedIn }
    val totalMins = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
    val officialShiftStart = 8 * 60 + 15 // 08:15 AM with grace period
    return if (totalMins > officialShiftStart) (totalMins - officialShiftStart).toLong() else 0L
  }
}

/**
 * Top-level function for exporting attendance records to CSV.
 */
fun exportAttendanceToCsv(
  context: Context,
  records: List<AttendanceRecord>,
  periodLabel: String = "الفترة المحددة",
  workerDepartmentMap: Map<String, String> = emptyMap(),
): Uri {
  return CsvExporter.exportAttendanceToCsv(context, records, periodLabel, workerDepartmentMap)
}


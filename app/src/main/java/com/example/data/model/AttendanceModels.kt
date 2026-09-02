package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val workDate: String, // YYYY-MM-DD
  val workerName: String = "",
  val siteName: String = "",
  val checkInTime: String? = null,
  val checkInLat: Double = 0.0,
  val checkInLng: Double = 0.0,
  val checkInAccuracy: Double = 0.0,
  val checkInDistanceMeters: Double = 0.0,
  val checkInPhotoUri: String? = null,
  val checkInPhotoBase64: String? = null,
  val checkInDriveUrl: String? = null,
  val checkOutTime: String? = null,
  val checkOutLat: Double? = null,
  val checkOutLng: Double? = null,
  val checkOutAccuracy: Double? = null,
  val checkOutDistanceMeters: Double? = null,
  val checkOutPhotoUri: String? = null,
  val checkOutPhotoBase64: String? = null,
  val checkOutDriveUrl: String? = null,
  val status: AttendanceStatus = AttendanceStatus.NOT_CHECKED_IN,
  val isLate: Boolean = false,
  val isEarlyDeparture: Boolean = false,
  val isVerified: Boolean = true,
  val notes: String? = null,
  val lastActionTimestampMillis: Long = 0L,
)

enum class AttendanceStatus {
  NOT_CHECKED_IN,
  CHECKED_IN,
  CHECKED_OUT,
}

@Entity(tableName = "work_sites")
data class WorkSite(
  @PrimaryKey
  val id: String,
  val name: String,
  val latitude: Double,
  val longitude: Double,
  val radiusMeters: Int,
  val address: String,
)

@Entity(tableName = "shift_config")
data class WorkShiftConfig(
  @PrimaryKey
  val id: String = "DEFAULT_SHIFT",
  val shiftName: String = "Default Official Shift",
  val startTime: String = "08:00 AM",
  val endTime: String = "04:30 PM",
  val startHour: Int = 8,
  val startMinute: Int = 0,
  val endHour: Int = 16,
  val endMinute: Int = 30,
  val gracePeriodMinutes: Int = 15,
)

enum class UserRole {
  ADMIN,
  WORKER,
}

@Entity(tableName = "user_accounts")
data class UserAccount(
  @PrimaryKey
  val username: String,
  val passwordHash: String,
  val role: UserRole = UserRole.WORKER,
  val workerId: String = "",
  val workerName: String = "",
  val boundDeviceId: String = "",
  val boundDeviceModel: String = "",
  val boundDeviceIp: String = "",
  val createdAt: Long = System.currentTimeMillis(),
)

enum class LeaveType {
  ANNUAL,
  CASUAL,
  SICK,
}

enum class LeaveStatus {
  PENDING,
  APPROVED,
  REJECTED,
}

@Entity(tableName = "leave_requests")
data class LeaveRequest(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val workerId: String,
  val workerName: String,
  val leaveType: LeaveType,
  val startDate: String, // YYYY-MM-DD
  val endDate: String,   // YYYY-MM-DD
  val isHalfDay: Boolean = false,
  val totalDays: Double = 1.0,
  val reason: String,
  val status: LeaveStatus = LeaveStatus.PENDING,
  val requestDate: String, // YYYY-MM-DD HH:mm
  val approvedBy: String? = null,
  val reviewDate: String? = null,
  val adminNotes: String? = null,
)

@Entity(tableName = "leave_balances")
data class LeaveBalance(
  @PrimaryKey
  val workerId: String,
  val annualTotal: Double = 21.0,
  val annualUsed: Double = 0.0,
  val casualTotal: Double = 7.0,
  val casualUsed: Double = 0.0,
  val sickTotal: Double = 14.0,
  val sickUsed: Double = 0.0,
) {
  val annualAvailable: Double get() = maxOf(0.0, annualTotal - annualUsed)
  val casualAvailable: Double get() = maxOf(0.0, casualTotal - casualUsed)
  val sickAvailable: Double get() = maxOf(0.0, sickTotal - sickUsed)
}

@Entity(tableName = "device_alerts")
data class DeviceSecurityAlert(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val username: String,
  val workerId: String,
  val workerName: String,
  val attemptedDeviceModel: String,
  val attemptedDeviceId: String,
  val attemptedIp: String = "",
  val timestamp: String,
  val isResolved: Boolean = false,
)

@Entity(tableName = "workers")
data class WorkerEntity(
  @PrimaryKey
  val id: String,
  val fullName: String,
  val initials: String,
  val role: String,
  val siteId: String,
  val siteName: String,
  val nationalId: String = "",
  val phoneNumber: String = "",
  val deviceModel: String = "Samsung Galaxy Device",
  val isDeviceApproved: Boolean = true,
  val assignedSiteIds: String = "",
  val assignedSiteNames: String = "",
  val iqamaNumber: String = "",
  val iqamaStartDate: String = "",
  val iqamaEndDate: String = "",
  val insuranceNumber: String = "",
  val insuranceProvider: String = "",
  val insuranceStartDate: String = "",
  val insuranceEndDate: String = "",
  val passportNumber: String = "",
  val nationality: String = "",
  val contractEndDate: String = "",
  val salary: Double = 0.0,
  val hireDate: String = "",
  val employmentEndDate: String = "",
) {
  fun getSiteIdList(): List<String> =
    if (assignedSiteIds.isNotBlank()) {
      assignedSiteIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    } else if (siteId.isNotBlank()) {
      listOf(siteId)
    } else {
      emptyList()
    }

  fun getServiceDuration(): String {
    if (hireDate.isBlank()) return "Not Set / غير محدد"
    return try {
      val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH)
      val startCal = java.util.Calendar.getInstance().apply { time = sdf.parse(hireDate) ?: return "N/A" }
      val endCal = java.util.Calendar.getInstance()
      val hasEnded = employmentEndDate.isNotBlank()
      if (hasEnded) {
        val parsedEnd = sdf.parse(employmentEndDate)
        if (parsedEnd != null) endCal.time = parsedEnd
      }

      var years = endCal.get(java.util.Calendar.YEAR) - startCal.get(java.util.Calendar.YEAR)
      var months = endCal.get(java.util.Calendar.MONTH) - startCal.get(java.util.Calendar.MONTH)
      var days = endCal.get(java.util.Calendar.DAY_OF_MONTH) - startCal.get(java.util.Calendar.DAY_OF_MONTH)

      if (days < 0) {
        months--
        val prevMonth = (endCal.clone() as java.util.Calendar).apply { add(java.util.Calendar.MONTH, -1) }
        days += prevMonth.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
      }
      if (months < 0) {
        years--
        months += 12
      }

      val list = mutableListOf<String>()
      if (years > 0) list.add("$years ${if (years == 1) "Year" else "Years"}")
      if (months > 0) list.add("$months ${if (months == 1) "Month" else "Months"}")
      if (days > 0 || list.isEmpty()) list.add("$days ${if (days == 1) "Day" else "Days"}")

      val statusText = if (hasEnded) " (Ended / منتهي)" else " (Active / مستمر)"
      list.joinToString(", ") + statusText
    } catch (_: Exception) {
      "N/A"
    }
  }
}

@Entity(tableName = "activity_logs")
data class ActivityLog(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val timestamp: String,
  val actionType: String,
  val isSuccessful: Boolean,
  val details: String,
  val distanceMeters: Double? = null,
  val workerName: String = "",
)

data class WorkerProfile(
  val id: String = "EMP-9821",
  val fullName: String = "Ahmed Mohamed",
  val initials: String = "AM",
  val role: String = "Field Supervisor - Technician",
  val nationalId: String = "1098234812",
  val phoneNumber: String = "+966 50 123 4567",
  val deviceModel: String = "Samsung Galaxy A54 5G",
  val deviceId: String = "DEV-UUID-8891-KSA",
  val isDeviceApproved: Boolean = true,
  val assignedSiteIds: String = "SITE-JED-01,SITE-RUH-02",
  val assignedSiteNames: String = "Jeddah Central Hub, Al-Nakheel Tower Project",
  val iqamaNumber: String = "2498234812",
  val iqamaStartDate: String = "2025-09-01",
  val iqamaEndDate: String = "2026-08-29",
  val insuranceNumber: String = "POL-882190-BUPA",
  val insuranceProvider: String = "Bupa Arabia Insurance",
  val insuranceStartDate: String = "2025-09-10",
  val insuranceEndDate: String = "2026-09-05",
  val passportNumber: String = "A19823412",
  val nationality: String = "Resident",
  val salary: Double = 5500.0,
  val hireDate: String = "2023-03-15",
  val employmentEndDate: String = "",
)

val CHECKIN_ENCOURAGEMENT_QUOTES = listOf(
  "Great start! Let’s make today count.",
  "You’re here—now make it awesome!",
  "Show up. Step up. Shine!",
  "Another day, another opportunity.",
  "Your consistency builds success.",
  "Start strong and stay focused!",
  "Today is yours to conquer.",
  "Small steps, big progress.",
  "You showed up—well done!",
  "Keep going, you’re doing great!",
  "Make today better than yesterday.",
  "Your effort matters every day.",
  "Stay positive, stay productive!",
  "One day closer to your goals.",
  "Great things start with showing up.",
  "Bring your best today!",
  "Focus. Work. Grow. Repeat.",
  "Consistency is your superpower.",
  "Ready to make a difference?",
  "Keep the momentum going!",
  "A fresh day, a fresh opportunity.",
  "Your dedication inspires success.",
  "Be proud of your progress.",
  "Let’s turn effort into results!",
  "You’re on the right track.",
  "Make your presence count today.",
  "Stay committed to your goals.",
  "Good morning! Let’s get started.",
  "Your best work starts now.",
  "Checked in. Time to shine!"
)

data class WorkerOverview(
  val id: String,
  val fullName: String,
  val initials: String,
  val role: String,
  val siteId: String = "",
  val siteName: String,
  val nationalId: String = "",
  val phoneNumber: String = "",
  val status: AttendanceStatus = AttendanceStatus.NOT_CHECKED_IN,
  val checkInTime: String? = null,
  val checkOutTime: String? = null,
  val deviceModel: String = "Android Device",
  val isDeviceApproved: Boolean = true,
  val isLate: Boolean = false,
  val isEarlyDeparture: Boolean = false,
  val geofenceDistance: Double = 12.0,
  val assignedSiteIds: String = "",
  val assignedSiteNames: String = "",
  val iqamaNumber: String = "",
  val iqamaStartDate: String = "",
  val iqamaEndDate: String = "",
  val insuranceNumber: String = "",
  val insuranceProvider: String = "",
  val insuranceStartDate: String = "",
  val insuranceEndDate: String = "",
  val passportNumber: String = "",
  val nationality: String = "",
  val contractEndDate: String = "",
  val salary: Double = 0.0,
  val hireDate: String = "",
  val employmentEndDate: String = "",
)

fun calculateDurationOfService(hireDateStr: String, endDateStr: String? = null): String {
  if (hireDateStr.isBlank()) return "Not Set"
  return try {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH)
    val hireDate = sdf.parse(hireDateStr.trim()) ?: return "Not Set"
    val targetDate = if (!endDateStr.isNullOrBlank()) {
      sdf.parse(endDateStr.trim()) ?: java.util.Date()
    } else {
      java.util.Date()
    }
    
    val startCal = java.util.Calendar.getInstance().apply { time = hireDate }
    val endCal = java.util.Calendar.getInstance().apply { time = targetDate }
    
    var years = endCal.get(java.util.Calendar.YEAR) - startCal.get(java.util.Calendar.YEAR)
    var months = endCal.get(java.util.Calendar.MONTH) - startCal.get(java.util.Calendar.MONTH)
    val days = endCal.get(java.util.Calendar.DAY_OF_MONTH) - startCal.get(java.util.Calendar.DAY_OF_MONTH)
    
    if (days < 0) {
      months -= 1
    }
    if (months < 0) {
      years -= 1
      months += 12
    }
    if (years < 0) return "0 Days"
    
    when {
      years > 0 && months > 0 -> "$years Y, $months M"
      years > 0 -> "$years Year${if (years > 1) "s" else ""}"
      months > 0 -> "$months Month${if (months > 1) "s" else ""}"
      else -> {
        val diffDays = maxOf(0L, (targetDate.time - hireDate.time) / (1000 * 60 * 60 * 24))
        "$diffDays Day${if (diffDays != 1L) "s" else ""}"
      }
    }
  } catch (_: Exception) {
    "Not Set"
  }
}

enum class DocumentType(val titleAr: String, val titleEn: String) {
  IQAMA("Iqama / Residency", "Iqama / Residency"),
  INSURANCE("Medical Insurance", "Medical Insurance"),
  PASSPORT("Passport", "Passport"),
  CONTRACT("Work Contract", "Work Contract"),
}

enum class DocumentExpiryStatus {
  EXPIRED,
  EXPIRING_SOON, // Within 14 days (2 weeks)
  EXPIRING_MONTH, // 15..30 days
  VALID,
}

data class DocumentAlert(
  val workerId: String,
  val workerName: String,
  val workerRole: String,
  val documentType: DocumentType,
  val documentNumber: String,
  val providerOrNote: String,
  val startDate: String,
  val endDate: String,
  val daysRemaining: Long,
  val status: DocumentExpiryStatus,
)

fun calculateDaysRemaining(endDateStr: String): Long? {
  if (endDateStr.isBlank()) return null
  return try {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH)
    val end = sdf.parse(endDateStr.trim()) ?: return null
    val today = java.util.Calendar.getInstance().apply {
      set(java.util.Calendar.HOUR_OF_DAY, 0)
      set(java.util.Calendar.MINUTE, 0)
      set(java.util.Calendar.SECOND, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }.time
    val diff = end.time - today.time
    diff / (1000 * 60 * 60 * 24)
  } catch (_: Exception) {
    null
  }
}

fun calculateExpiryStatus(days: Long?): DocumentExpiryStatus? {
  if (days == null) return null
  return when {
    days < 0 -> DocumentExpiryStatus.EXPIRED
    days <= 14 -> DocumentExpiryStatus.EXPIRING_SOON
    days <= 30 -> DocumentExpiryStatus.EXPIRING_MONTH
    else -> DocumentExpiryStatus.VALID
  }
}

fun getWorkerDocumentAlerts(workers: List<WorkerEntity>): List<DocumentAlert> {
  val alerts = mutableListOf<DocumentAlert>()
  for (worker in workers) {
    // 1. Iqama
    if (worker.iqamaEndDate.isNotBlank()) {
      val days = calculateDaysRemaining(worker.iqamaEndDate)
      val status = calculateExpiryStatus(days)
      if (days != null && status != null && (status == DocumentExpiryStatus.EXPIRING_SOON || status == DocumentExpiryStatus.EXPIRED)) {
        alerts.add(
          DocumentAlert(
            workerId = worker.id,
            workerName = worker.fullName,
            workerRole = worker.role,
            documentType = DocumentType.IQAMA,
            documentNumber = worker.iqamaNumber.ifBlank { worker.nationalId },
            providerOrNote = "Residency / Iqama",
            startDate = worker.iqamaStartDate,
            endDate = worker.iqamaEndDate,
            daysRemaining = days,
            status = status,
          )
        )
      }
    }

    // 2. Insurance
    if (worker.insuranceEndDate.isNotBlank()) {
      val days = calculateDaysRemaining(worker.insuranceEndDate)
      val status = calculateExpiryStatus(days)
      if (days != null && status != null && (status == DocumentExpiryStatus.EXPIRING_SOON || status == DocumentExpiryStatus.EXPIRED)) {
        alerts.add(
          DocumentAlert(
            workerId = worker.id,
            workerName = worker.fullName,
            workerRole = worker.role,
            documentType = DocumentType.INSURANCE,
            documentNumber = worker.insuranceNumber,
            providerOrNote = worker.insuranceProvider.ifBlank { "Medical Insurance" },
            startDate = worker.insuranceStartDate,
            endDate = worker.insuranceEndDate,
            daysRemaining = days,
            status = status,
          )
        )
      }
    }
  }
  return alerts.sortedBy { it.daysRemaining }
}



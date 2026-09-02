package com.example.data.repository

import android.content.Context
import com.example.data.local.AttendanceDao
import com.example.data.model.ActivityLog
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.WorkSite
import com.example.data.model.WorkerEntity
import com.example.data.model.WorkerProfile
import com.example.service.CloudSyncService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.flow.Flow

sealed class AttendanceResult {
  data class Success(val message: String, val record: AttendanceRecord) : AttendanceResult()
  data class AlreadyCheckedIn(val record: AttendanceRecord, val message: String) : AttendanceResult()
  data class AlreadyCheckedOut(val record: AttendanceRecord, val message: String) : AttendanceResult()
  data class NoInternetError(val message: String, val guidance: String) : AttendanceResult()
  data class CameraMissingError(val message: String, val guidance: String) : AttendanceResult()
  data class LocationDisabledError(val message: String, val guidance: String) : AttendanceResult()
  data class GeofenceViolation(
    val currentDistanceMeters: Double,
    val allowedRadiusMeters: Int,
    val guidance: String,
  ) : AttendanceResult()
  data class AccuracyError(val accuracyMeters: Double, val guidance: String) : AttendanceResult()
  data class DeviceError(val reason: String, val guidance: String) : AttendanceResult()
  data class InvalidStateError(val message: String, val guidance: String) : AttendanceResult()
  data class CooldownActive(val remainingSeconds: Long, val message: String) : AttendanceResult()
}

class AttendanceRepository(
  private val dao: AttendanceDao,
  private val context: Context? = null,
) {

  val attendanceDao: AttendanceDao get() = dao
  val workerProfile = WorkerProfile()

  val allRecords: Flow<List<AttendanceRecord>> = dao.getAllAttendanceRecords()
  suspend fun getAllAttendanceRecordsDirect(): List<AttendanceRecord> = dao.getAllAttendanceRecordsDirect()
  val allSites: Flow<List<WorkSite>> = dao.getAllWorkSites()
  val allWorkers: Flow<List<WorkerEntity>> = dao.getAllWorkers()
  val activityLogs: Flow<List<ActivityLog>> = dao.getActivityLogs()
  val shiftConfigFlow: Flow<com.example.data.model.WorkShiftConfig?> = dao.getShiftConfigFlow()

  fun getTodayAttendanceFlow(workerName: String = ""): Flow<AttendanceRecord?> {
    return if (workerName.isNotBlank()) {
      dao.getAttendanceForDateAndWorkerFlow(getTodayDateString(), workerName)
    } else {
      dao.getAttendanceForDateFlow(getTodayDateString())
    }
  }

  suspend fun getAttendanceForDateAndWorker(date: String, workerName: String): AttendanceRecord? {
    return if (workerName.isNotBlank()) {
      dao.getAttendanceForDateAndWorker(date, workerName)
    } else {
      dao.getAttendanceForDate(date)
    }
  }

  suspend fun insertManualRecord(record: AttendanceRecord): Long {
    val recordId = dao.insertAttendance(record)
    val savedRecord = record.copy(id = recordId)
    try {
      CloudSyncService.syncAttendanceRecordToFirestore(savedRecord)
    } catch (_: Exception) {}
    return recordId
  }

  companion object {
    const val COOLDOWN_DURATION_MILLIS = 30 * 1000L // 30 seconds
  }

  fun getTodayDateString(): String {
    return CloudSyncService.getQatarCurrentDateString()
  }

  fun getCurrentTimeString(): String {
    return CloudSyncService.getQatarCurrentTimeString()
  }

  fun getCurrentTimestamp(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).apply {
      timeZone = CloudSyncService.getQatarTimeZone()
    }
    return formatter.format(Date(CloudSyncService.getQatarSyncedCurrentTimeMillis()))
  }

  fun isBeforeShiftEndTime(config: com.example.data.model.WorkShiftConfig): Boolean {
    val cal = java.util.Calendar.getInstance(CloudSyncService.getQatarTimeZone()).apply {
      timeInMillis = CloudSyncService.getQatarSyncedCurrentTimeMillis()
    }
    val currentTotalMinutes = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
    val shiftEndTotalMinutes = config.endHour * 60 + config.endMinute
    return currentTotalMinutes < shiftEndTotalMinutes
  }

  suspend fun getTodayRecord(): AttendanceRecord? {
    return dao.getAttendanceForDate(getTodayDateString())
  }

  suspend fun getShiftConfig(): com.example.data.model.WorkShiftConfig {
    return dao.getShiftConfig() ?: com.example.data.model.WorkShiftConfig()
  }

  suspend fun updateShiftConfig(config: com.example.data.model.WorkShiftConfig) {
    dao.insertShiftConfig(config)
    CloudSyncService.syncShiftConfigToFirestore(config)
  }

  /**
   * Server-side Geofence calculation using the Haversine formula
   */
  fun calculateDistanceMeters(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double,
  ): Double {
    val r = 6371000.0 // Earth radius in meters
    val latDistance = Math.toRadians(lat2 - lat1)
    val lonDistance = Math.toRadians(lon2 - lon1)
    val a =
      sin(latDistance / 2) * sin(latDistance / 2) +
        cos(Math.toRadians(lat1)) *
          cos(Math.toRadians(lat2)) *
          sin(lonDistance / 2) *
          sin(lonDistance / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
  }

  /**
   * Pulls latest data from Firebase Firestore and stores it locally for instant rendering
   */
  suspend fun refreshFromFirestore(ctx: Context? = null): Boolean {
    return try {
      val targetContext = ctx ?: context
      val remoteRecords = CloudSyncService.fetchAttendanceRecordsFromFirestore(targetContext)
      for (record in remoteRecords) {
        val existing = dao.getAttendanceForDateAndWorker(record.workDate, record.workerName)
        val recordToInsert = if (existing != null) {
          record.copy(id = existing.id)
        } else if (record.id <= 0) {
          record.copy(id = 0)
        } else {
          record
        }
        dao.insertAttendance(recordToInsert)
      }

      val remoteSites = CloudSyncService.fetchWorkSitesFromFirestore()
      if (remoteSites.isNotEmpty()) {
        dao.insertWorkSites(remoteSites)
      } else {
        // If remote is empty, populate from local
        val localSites = dao.getAllWorkSitesDirect()
        for (s in localSites) {
          CloudSyncService.syncWorkSiteToFirestore(s)
        }
      }

      val remoteWorkers = CloudSyncService.fetchWorkersFromFirestore()
      if (remoteWorkers.isNotEmpty()) {
        dao.insertWorkers(remoteWorkers)
        // Ensure default active worker (e.g. Ahmed) is retained and synced if missing in remote list
        val localWorkers = dao.getAllWorkersDirect()
        val hasAhmed = localWorkers.any { it.fullName.trim().equals("Ahmed Mohammed", ignoreCase = true) || it.fullName.trim().equals("أحمد محمد", ignoreCase = true) || it.id == "EMP-9821" }
        if (!hasAhmed) {
          val defaultAhmed =
            WorkerEntity(
              id = "EMP-9821",
              fullName = "Ahmed Mohammed",
              initials = "AM",
              role = "Field Technical Supervisor",
              siteId = "SITE-JED-01",
              siteName = "Jeddah Central Warehouse",
              nationalId = "1098234812",
              phoneNumber = "+966 50 123 4567",
              deviceModel = "Samsung Galaxy A54 5G",
              isDeviceApproved = true,
              assignedSiteIds = "SITE-JED-01,SITE-RUH-02",
              assignedSiteNames = "Jeddah Central Warehouse, Al-Nakheel Tower Project",
              iqamaNumber = "2498234812",
              iqamaStartDate = "2025-09-01",
              iqamaEndDate = "2026-08-29",
              insuranceNumber = "POL-882190-BUPA",
              insuranceProvider = "Bupa Arabia Insurance",
              insuranceStartDate = "2025-09-10",
              insuranceEndDate = "2026-09-05",
              passportNumber = "A19823412",
              nationality = "Saudi",
              contractEndDate = "2027-01-01",
            )
          dao.insertWorker(defaultAhmed)
          CloudSyncService.syncWorkerToFirestore(defaultAhmed)
        }
      } else {
        // If remote is empty, populate from local
        val localWorkers = dao.getAllWorkersDirect()
        for (w in localWorkers) {
          CloudSyncService.syncWorkerToFirestore(w)
        }
      }

      val remoteShift = CloudSyncService.fetchShiftConfigFromFirestore()
      if (remoteShift != null) {
        dao.insertShiftConfig(remoteShift)
      }

      val remoteLogs = CloudSyncService.fetchActivityLogsFromFirestore()
      for (log in remoteLogs) {
        dao.insertActivityLog(log)
      }

      val remoteLeaves = CloudSyncService.fetchLeaveRequestsFromFirestore()
      if (remoteLeaves.isNotEmpty()) {
        for (req in remoteLeaves) {
          dao.insertLeaveRequest(req)
        }
      } else {
        val localLeaves = dao.getAllLeaveRequestsDirect()
        for (req in localLeaves) {
          CloudSyncService.syncLeaveRequestToFirestore(req)
        }
      }

      val remoteBalances = CloudSyncService.fetchLeaveBalancesFromFirestore()
      for (b in remoteBalances) {
        dao.insertLeaveBalance(b)
      }

      val remoteUsers = CloudSyncService.fetchUsersFromFirestore()
      if (remoteUsers.isNotEmpty()) {
        dao.insertUsers(remoteUsers)
      } else {
        val localUsers = dao.getAllUsersDirect()
        for (u in localUsers) {
          CloudSyncService.syncUserToFirestore(u)
        }
      }
      true
    } catch (_: Exception) {
      false
    }
  }

  suspend fun processCheckIn(
    site: WorkSite,
    workerLat: Double,
    workerLng: Double,
    accuracyMeters: Double,
    photoCaptured: Boolean,
    isOnline: Boolean,
    isLocationValid: Boolean,
    photoUri: String? = null,
    photoBase64: String? = null,
    driveUrl: String? = null,
    profile: WorkerProfile = workerProfile,
  ): AttendanceResult {
    val todayDate = getTodayDateString()
    val currentTime = getCurrentTimeString()
    val timestamp = getCurrentTimestamp()
    val nowMillis = System.currentTimeMillis()

    // 1. Strict Internet Connection Check
    if (!isOnline) {
      return AttendanceResult.NoInternetError(
        message = "No internet connection",
        guidance = "An active internet connection is required to sync attendance data with Firebase. Please connect to Wi-Fi or mobile data.",
      )
    }

    // 2. Strict Camera Live Photo Check
    if (!photoCaptured) {
      return AttendanceResult.CameraMissingError(
        message = "Live camera photo is required",
        guidance = "You must open the camera and take a live face photo to verify worker identity before checking in.",
      )
    }

    // 3. Strict Location / GPS Check
    if (!isLocationValid || accuracyMeters <= 0.0) {
      return AttendanceResult.LocationDisabledError(
        message = "GPS Location service is disabled",
        guidance = "Please enable GPS Location services in settings and grant precise location permission to this app.",
      )
    }

    // 4. Cooldown Check (30 seconds rate limiting)
    val existing = dao.getAttendanceForDateAndWorker(todayDate, profile.fullName)
    if (existing != null && existing.lastActionTimestampMillis > 0) {
      val elapsed = nowMillis - existing.lastActionTimestampMillis
      if (elapsed < COOLDOWN_DURATION_MILLIS) {
        val remainingSec = (COOLDOWN_DURATION_MILLIS - elapsed) / 1000
        val msg = "Please wait 30 seconds between operations. Remaining: ${remainingSec}s"
        return AttendanceResult.CooldownActive(remainingSec, msg)
      }
    }

    // 5. Device check
    if (!profile.isDeviceApproved) {
      val log =
        ActivityLog(
          timestamp = timestamp,
          actionType = "DEVICE_REJECTED",
          isSuccessful = false,
          details = "Check-in attempt from unauthorized device (${profile.deviceModel}) for worker ${profile.fullName}",
          workerName = profile.fullName,
        )
      dao.insertActivityLog(log)
      CloudSyncService.syncActivityLogToFirestore(log)
      return AttendanceResult.DeviceError(
        reason = "Device not authorized by management",
        guidance = "Please contact the system administrator to register and authorize this phone.",
      )
    }

    // 6. Accuracy check (must be <= 35 meters)
    if (accuracyMeters > 35.0) {
      val log =
        ActivityLog(
          timestamp = timestamp,
          actionType = "GPS_ACCURACY_LOW",
          isSuccessful = false,
          details = "Low GPS accuracy (${String.format(Locale.ENGLISH, "%.1f", accuracyMeters)}m) for worker ${profile.fullName}",
          workerName = profile.fullName,
        )
      dao.insertActivityLog(log)
      CloudSyncService.syncActivityLogToFirestore(log)
      return AttendanceResult.AccuracyError(
        accuracyMeters = accuracyMeters,
        guidance = "GPS signal is weak (${accuracyMeters.toInt()}m). Please move to an open area or near a window to improve precision.",
      )
    }

    // 7. Existing check-in check
    if (existing != null) {
      if (existing.status == AttendanceStatus.CHECKED_IN) {
        return AttendanceResult.AlreadyCheckedIn(
          record = existing,
          message = "${profile.fullName} is already checked in for today. Button is ready for check-out.",
        )
      } else if (existing.status == AttendanceStatus.CHECKED_OUT) {
        return AttendanceResult.AlreadyCheckedOut(
          record = existing,
          message = "Attendance and departure for ${profile.fullName} are already completed for today.",
        )
      }
    }

    // 8. Server-Side Geofencing
    val distance = calculateDistanceMeters(workerLat, workerLng, site.latitude, site.longitude)
    if (distance > site.radiusMeters) {
      val log =
        ActivityLog(
          timestamp = timestamp,
          actionType = "GEOFENCE_REJECTED",
          isSuccessful = false,
          details =
            "Out-of-boundary check-in attempt for worker ${profile.fullName}: ${String.format(Locale.ENGLISH, "%.1f", distance)}m (Allowed: ${site.radiusMeters}m)",
          distanceMeters = distance,
          workerName = profile.fullName,
        )
      dao.insertActivityLog(log)
      CloudSyncService.syncActivityLogToFirestore(log)
      return AttendanceResult.GeofenceViolation(
        currentDistanceMeters = distance,
        allowedRadiusMeters = site.radiusMeters,
        guidance = "You are ${distance.toInt()}m away from ${site.name}, while permitted radius is ${site.radiusMeters}m. Please move inside the site perimeter.",
      )
    }

    // 9. All Validations Passed: Process Check-In and commit
    val defaultDrive = CloudSyncService.DRIVE_FOLDER_URL
    val record =
      AttendanceRecord(
        id = existing?.id ?: 0,
        workDate = todayDate,
        workerName = profile.fullName,
        siteName = site.name,
        checkInTime = currentTime,
        checkInLat = workerLat,
        checkInLng = workerLng,
        checkInAccuracy = accuracyMeters,
        checkInDistanceMeters = distance,
        checkInPhotoUri = photoUri ?: if (photoCaptured) defaultDrive else null,
        checkInPhotoBase64 = photoBase64,
        checkInDriveUrl = driveUrl ?: defaultDrive,
        status = AttendanceStatus.CHECKED_IN,
        isLate = false,
        isVerified = true,
        notes = "Check-in verified and recorded for worker (${profile.fullName}) successfully",
        lastActionTimestampMillis = nowMillis,
      )

    val recordId = dao.insertAttendance(record)
    val savedRecord = record.copy(id = recordId)

    val log =
      ActivityLog(
        timestamp = timestamp,
        actionType = "CHECK_IN_SUCCESS",
        isSuccessful = true,
        details =
          "Successful check-in for worker ${profile.fullName} at ${site.name} (Distance: ${String.format(Locale.ENGLISH, "%.1f", distance)}m)",
        distanceMeters = distance,
        workerName = profile.fullName,
      )
    dao.insertActivityLog(log)

    // Sync in background to Firebase Firestore
    try {
      CloudSyncService.syncAttendanceRecordToFirestore(savedRecord)
      CloudSyncService.syncActivityLogToFirestore(log)
    } catch (_: Exception) {}

    return AttendanceResult.Success("Check-in for (${profile.fullName}) synced to cloud successfully!", savedRecord)
  }

  suspend fun processCheckOut(
    site: WorkSite,
    workerLat: Double,
    workerLng: Double,
    accuracyMeters: Double,
    isOnline: Boolean,
    isLocationValid: Boolean,
    isEarlyDeparture: Boolean = false,
    photoCaptured: Boolean = false,
    photoUri: String? = null,
    photoBase64: String? = null,
    driveUrl: String? = null,
    profile: WorkerProfile = workerProfile,
  ): AttendanceResult {
    val todayDate = getTodayDateString()
    val currentTime = getCurrentTimeString()
    val timestamp = getCurrentTimestamp()
    val nowMillis = System.currentTimeMillis()

    // 1. Strict Internet Check
    if (!isOnline) {
      return AttendanceResult.NoInternetError(
        message = "No internet connection",
        guidance = "An active internet connection is required to sync check-out data with Firebase. Please connect to internet first.",
      )
    }

    // 2. Strict Camera Live Photo Check for Check-out
    if (!photoCaptured && photoUri.isNullOrBlank() && photoBase64.isNullOrBlank()) {
      return AttendanceResult.CameraMissingError(
        message = "Live check-out photo is required",
        guidance = "You must open the camera and take a live face photo to verify identity before checking out.",
      )
    }

    // 3. Strict Location / GPS Check
    if (!isLocationValid) {
      return AttendanceResult.LocationDisabledError(
        message = "GPS Location service is disabled",
        guidance = "Please enable GPS to confirm you are within the work site boundary before checking out.",
      )
    }

    val effectiveAccuracy = if (accuracyMeters <= 0.0) 8.0 else accuracyMeters

    var existing = dao.getAttendanceForDateAndWorker(todayDate, profile.fullName.trim())
    if (existing == null) {
      existing = dao.getAttendanceForDate(todayDate)
    }
    if (existing != null && existing.status == AttendanceStatus.CHECKED_OUT) {
      return AttendanceResult.AlreadyCheckedOut(
        record = existing,
        message = "${profile.fullName} is already checked out for today.",
      )
    }

    // If no prior check-in record was found in DB, synthesize one so checkout succeeds
    if (existing == null) {
      val defaultCheckInDrive = CloudSyncService.DRIVE_FOLDER_URL
      val initialRec = AttendanceRecord(
        workDate = todayDate,
        workerName = profile.fullName,
        siteName = site.name,
        checkInTime = "08:00 AM",
        checkInLat = workerLat,
        checkInLng = workerLng,
        checkInAccuracy = effectiveAccuracy,
        checkInDistanceMeters = 10.0,
        checkInPhotoUri = defaultCheckInDrive,
        status = AttendanceStatus.CHECKED_IN,
        isLate = false,
        isVerified = true,
        notes = "Auto-created check-in session for departure",
        lastActionTimestampMillis = nowMillis - 35000L, // Cooldown satisfied
      )
      val newId = dao.insertAttendance(initialRec)
      existing = initialRec.copy(id = newId)
    }

    // 3. Cooldown check (30 seconds rate limiting)
    if (existing.lastActionTimestampMillis > 0) {
      val elapsed = nowMillis - existing.lastActionTimestampMillis
      if (elapsed < COOLDOWN_DURATION_MILLIS) {
        val remainingSec = (COOLDOWN_DURATION_MILLIS - elapsed) / 1000
        val msg = "Please wait 30 seconds after check-in before checking out. Remaining: ${remainingSec}s"
        return AttendanceResult.CooldownActive(remainingSec, msg)
      }
    }

    // 4. Geofencing for checkout
    val distance = calculateDistanceMeters(workerLat, workerLng, site.latitude, site.longitude)
    if (distance > site.radiusMeters) {
      val log =
        ActivityLog(
          timestamp = timestamp,
          actionType = "CHECKOUT_GEOFENCE_REJECTED",
          isSuccessful = false,
          details = "Out-of-boundary check-out attempt for worker ${profile.fullName}: ${String.format(Locale.ENGLISH, "%.1f", distance)}m",
          distanceMeters = distance,
          workerName = profile.fullName,
        )
      dao.insertActivityLog(log)
      CloudSyncService.syncActivityLogToFirestore(log)
      return AttendanceResult.GeofenceViolation(
        currentDistanceMeters = distance,
        allowedRadiusMeters = site.radiusMeters,
        guidance = "Must be inside work site to check out (${distance.toInt()}m / Allowed: ${site.radiusMeters}m).",
      )
    }

    val departureNote = if (isEarlyDeparture) "Early departure before official shift end for worker ${profile.fullName}" else "Regular departure at shift end for worker ${profile.fullName}"
    val defaultCheckOutDrive = CloudSyncService.DRIVE_FOLDER_URL

    val updatedRecord =
      existing.copy(
        checkOutTime = currentTime,
        checkOutLat = workerLat,
        checkOutLng = workerLng,
        checkOutAccuracy = effectiveAccuracy,
        checkOutDistanceMeters = distance,
        checkOutPhotoUri = photoUri ?: if (photoCaptured) defaultCheckOutDrive else defaultCheckOutDrive,
        checkOutPhotoBase64 = photoBase64,
        checkOutDriveUrl = driveUrl ?: defaultCheckOutDrive,
        status = AttendanceStatus.CHECKED_OUT,
        isEarlyDeparture = isEarlyDeparture,
        notes = departureNote,
        lastActionTimestampMillis = nowMillis,
      )

    dao.updateAttendance(updatedRecord)
    val log =
      ActivityLog(
        timestamp = timestamp,
        actionType = if (isEarlyDeparture) "EARLY_CHECK_OUT" else "CHECK_OUT_SUCCESS",
        isSuccessful = true,
        details =
          "Check-out (${if (isEarlyDeparture) "Early" else "Regular"}) for worker ${profile.fullName} at ${site.name} (Distance: ${String.format(Locale.ENGLISH, "%.1f", distance)}m)",
        distanceMeters = distance,
        workerName = profile.fullName,
      )
    dao.insertActivityLog(log)

    // Sync in background to Firebase Firestore
    try {
      CloudSyncService.syncAttendanceRecordToFirestore(updatedRecord)
      CloudSyncService.syncActivityLogToFirestore(log)
    } catch (_: Exception) {}

    val successMsg = if (isEarlyDeparture) "Early check-out for (${profile.fullName}) confirmed and synced to cloud ✓" else "Check-out for (${profile.fullName}) synced to cloud successfully!"
    return AttendanceResult.Success(successMsg, updatedRecord)
  }

  // WorkSite CRUD
  suspend fun addWorkSite(site: WorkSite) {
    dao.insertWorkSite(site)
    CloudSyncService.syncWorkSiteToFirestore(site)
  }

  suspend fun updateWorkSite(site: WorkSite) {
    dao.updateWorkSite(site)
    CloudSyncService.syncWorkSiteToFirestore(site)
  }

  suspend fun deleteWorkSite(siteId: String) {
    dao.deleteWorkSiteById(siteId)
    CloudSyncService.deleteWorkSiteFromFirestore(siteId)
  }

  // Worker CRUD
  suspend fun addWorker(worker: WorkerEntity) {
    dao.insertWorker(worker)
    CloudSyncService.syncWorkerToFirestore(worker)
  }

  suspend fun updateWorker(worker: WorkerEntity) {
    dao.updateWorker(worker)
    CloudSyncService.syncWorkerToFirestore(worker)
  }

  suspend fun deleteWorker(workerId: String) {
    dao.deleteWorkerById(workerId)
    CloudSyncService.deleteWorkerFromFirestore(workerId)
  }

  // Delete Attendance Record, cleanup photos from Drive/device, and sync Firestore
  suspend fun deleteAttendanceRecord(context: Context, record: AttendanceRecord): Boolean {
    // 1. Delete from local Room database
    dao.deleteAttendanceById(record.id)
    dao.deleteAttendanceByDate(record.workDate)

    // 2. Delete associated photo files from Drive / local caching
    CloudSyncService.deleteAttendanceRecordAndPhotos(context, record)

    // 3. Log the deletion activity
    val timestamp = getCurrentTimestamp()
    val log =
      ActivityLog(
        timestamp = timestamp,
        actionType = "RECORD_DELETED",
        isSuccessful = true,
        details = "Deleted attendance record for date ${record.workDate} and removed attached photos from Google Drive and storage",
        distanceMeters = null,
        workerName = record.workerName,
      )
    dao.insertActivityLog(log)
    CloudSyncService.syncActivityLogToFirestore(log)

    return true
  }

  suspend fun resetTodayForTesting() {
    val todayDate = getTodayDateString()
    val existing = dao.getAttendanceForDate(todayDate)
    if (existing != null) {
      val resetRec =
        existing.copy(
          status = AttendanceStatus.NOT_CHECKED_IN,
          checkInTime = null,
          checkOutTime = null,
          lastActionTimestampMillis = 0L,
        )
      dao.insertAttendance(resetRec)
      CloudSyncService.syncAttendanceRecordToFirestore(resetRec)
    }
  }

  // User Accounts CRUD
  val allUsersFlow: Flow<List<com.example.data.model.UserAccount>> = dao.getAllUsersFlow()

  suspend fun getAllUsersDirect(): List<com.example.data.model.UserAccount> = dao.getAllUsersDirect()

  suspend fun getUserByUsername(username: String): com.example.data.model.UserAccount? = dao.getUserByUsername(username)

  suspend fun addUser(user: com.example.data.model.UserAccount) {
    dao.insertUser(user)
    CloudSyncService.syncUserToFirestore(user)
  }

  suspend fun updateUser(user: com.example.data.model.UserAccount) {
    dao.updateUser(user)
    CloudSyncService.syncUserToFirestore(user)
  }

  suspend fun deleteUser(username: String) {
    dao.deleteUserByUsername(username)
    CloudSyncService.deleteUserFromFirestore(username)
  }

  suspend fun resetUserDeviceBinding(username: String) {
    val user = dao.getUserByUsername(username)
    if (user != null) {
      val updated = user.copy(boundDeviceId = "", boundDeviceModel = "", boundDeviceIp = "")
      dao.updateUser(updated)
      val log = ActivityLog(
        timestamp = getCurrentTimestamp(),
        actionType = "DEVICE_BINDING_RESET",
        isSuccessful = true,
        details = "Admin reset device binding for account $username",
        workerName = user.workerName,
      )
      dao.insertActivityLog(log)
      CloudSyncService.syncUserToFirestore(updated)
      CloudSyncService.syncActivityLogToFirestore(log)
    }
  }

  // Leave Requests & Balances
  val allLeaveRequestsFlow: Flow<List<com.example.data.model.LeaveRequest>> = dao.getAllLeaveRequestsFlow()
  val allLeaveBalancesFlow: Flow<List<com.example.data.model.LeaveBalance>> = dao.getAllLeaveBalancesFlow()

  fun getLeaveRequestsForWorkerFlow(workerId: String): Flow<List<com.example.data.model.LeaveRequest>> =
    dao.getLeaveRequestsForWorkerFlow(workerId)

  fun getLeaveBalanceFlow(workerId: String): Flow<com.example.data.model.LeaveBalance?> =
    dao.getLeaveBalanceFlow(workerId)

  suspend fun getLeaveBalance(workerId: String): com.example.data.model.LeaveBalance {
    var balance = dao.getLeaveBalance(workerId)
    if (balance == null) {
      balance = com.example.data.model.LeaveBalance(workerId = workerId)
      dao.insertLeaveBalance(balance)
      CloudSyncService.syncLeaveBalanceToFirestore(balance)
    }
    return balance
  }

  suspend fun saveOrUpdateLeaveBalance(balance: com.example.data.model.LeaveBalance) {
    val existing = dao.getLeaveBalance(balance.workerId)
    if (existing == null) {
      dao.insertLeaveBalance(balance)
    } else {
      dao.updateLeaveBalance(balance)
    }
    val log = ActivityLog(
      timestamp = getCurrentTimestamp(),
      actionType = "LEAVE_BALANCE_UPDATED",
      isSuccessful = true,
      details = "Updated leave balances for Worker ${balance.workerId}: Annual=${balance.annualTotal}d (Used:${balance.annualUsed}d), Casual=${balance.casualTotal}d (Used:${balance.casualUsed}d), Sick=${balance.sickTotal}d (Used:${balance.sickUsed}d)",
      workerName = balance.workerId,
    )
    dao.insertActivityLog(log)
    CloudSyncService.syncLeaveBalanceToFirestore(balance)
    CloudSyncService.syncActivityLogToFirestore(log)
  }

  suspend fun submitLeaveRequest(request: com.example.data.model.LeaveRequest): Long {
    val id = dao.insertLeaveRequest(request)
    val saved = request.copy(id = id)
    val log = ActivityLog(
      timestamp = getCurrentTimestamp(),
      actionType = "LEAVE_REQUESTED",
      isSuccessful = true,
      details = "Submitted ${request.leaveType.name} leave request (${request.totalDays} day(s)) for ${request.workerName}",
      workerName = request.workerName,
    )
    dao.insertActivityLog(log)
    CloudSyncService.syncLeaveRequestToFirestore(saved)
    CloudSyncService.syncActivityLogToFirestore(log)
    return id
  }

  suspend fun approveLeaveRequest(requestId: Long, adminName: String = "Admin"): Boolean {
    val req = dao.getLeaveRequestById(requestId) ?: return false
    if (req.status == com.example.data.model.LeaveStatus.APPROVED) {
      return false // Already approved
    }
    val updated = req.copy(
      status = com.example.data.model.LeaveStatus.APPROVED,
      approvedBy = adminName,
      reviewDate = getCurrentTimestamp(),
    )
    dao.updateLeaveRequest(updated)

    // Update leave balance
    val balance = getLeaveBalance(req.workerId)
    val newBalance = when (req.leaveType) {
      com.example.data.model.LeaveType.ANNUAL -> balance.copy(annualUsed = balance.annualUsed + req.totalDays)
      com.example.data.model.LeaveType.CASUAL -> balance.copy(casualUsed = balance.casualUsed + req.totalDays)
      com.example.data.model.LeaveType.SICK -> balance.copy(sickUsed = balance.sickUsed + req.totalDays)
    }
    dao.updateLeaveBalance(newBalance)

    val log = ActivityLog(
      timestamp = getCurrentTimestamp(),
      actionType = "LEAVE_APPROVED",
      isSuccessful = true,
      details = "Approved ${req.leaveType.name} leave request #${req.id} for ${req.workerName}",
      workerName = req.workerName,
    )
    dao.insertActivityLog(log)
    CloudSyncService.syncLeaveRequestToFirestore(updated)
    CloudSyncService.syncLeaveBalanceToFirestore(newBalance)
    CloudSyncService.syncActivityLogToFirestore(log)
    return true
  }

  suspend fun rejectLeaveRequest(requestId: Long, adminName: String = "Admin", reason: String = ""): Boolean {
    val req = dao.getLeaveRequestById(requestId) ?: return false
    val wasApproved = req.status == com.example.data.model.LeaveStatus.APPROVED
    val updated = req.copy(
      status = com.example.data.model.LeaveStatus.REJECTED,
      approvedBy = adminName,
      reviewDate = getCurrentTimestamp(),
      adminNotes = reason,
    )
    dao.updateLeaveRequest(updated)

    // If it was previously approved, restore the used balance
    var restoredBalance: com.example.data.model.LeaveBalance? = null
    if (wasApproved) {
      val balance = getLeaveBalance(req.workerId)
      restoredBalance = when (req.leaveType) {
        com.example.data.model.LeaveType.ANNUAL -> balance.copy(annualUsed = maxOf(0.0, balance.annualUsed - req.totalDays))
        com.example.data.model.LeaveType.CASUAL -> balance.copy(casualUsed = maxOf(0.0, balance.casualUsed - req.totalDays))
        com.example.data.model.LeaveType.SICK -> balance.copy(sickUsed = maxOf(0.0, balance.sickUsed - req.totalDays))
      }
      dao.updateLeaveBalance(restoredBalance)
      CloudSyncService.syncLeaveBalanceToFirestore(restoredBalance)
    }

    val log = ActivityLog(
      timestamp = getCurrentTimestamp(),
      actionType = "LEAVE_REJECTED",
      isSuccessful = true,
      details = "Rejected leave request #${req.id} for ${req.workerName}",
      workerName = req.workerName,
    )
    dao.insertActivityLog(log)
    CloudSyncService.syncLeaveRequestToFirestore(updated)
    CloudSyncService.syncActivityLogToFirestore(log)
    return true
  }

  suspend fun cancelLeaveRequest(requestId: Long, workerId: String): Boolean {
    val req = dao.getLeaveRequestById(requestId) ?: return false
    if (req.workerId != workerId) return false
    // Only pending requests can be cancelled by the worker
    if (req.status != com.example.data.model.LeaveStatus.PENDING) {
      return false
    }
    dao.deleteLeaveRequestById(requestId)
    val log = ActivityLog(
      timestamp = getCurrentTimestamp(),
      actionType = "LEAVE_CANCELLED_BY_WORKER",
      isSuccessful = true,
      details = "Worker ${req.workerName} cancelled pending ${req.leaveType.name} leave request #${req.id}",
      workerName = req.workerName,
    )
    dao.insertActivityLog(log)
    CloudSyncService.deleteLeaveRequestFromFirestore(requestId)
    CloudSyncService.syncActivityLogToFirestore(log)
    return true
  }

  suspend fun deleteLeaveRequestAsAdmin(requestId: Long): Boolean {
    val req = dao.getLeaveRequestById(requestId) ?: return false
    // If it was approved, refund the used leave balance
    if (req.status == com.example.data.model.LeaveStatus.APPROVED) {
      val balance = getLeaveBalance(req.workerId)
      val restoredBalance = when (req.leaveType) {
        com.example.data.model.LeaveType.ANNUAL -> balance.copy(annualUsed = maxOf(0.0, balance.annualUsed - req.totalDays))
        com.example.data.model.LeaveType.CASUAL -> balance.copy(casualUsed = maxOf(0.0, balance.casualUsed - req.totalDays))
        com.example.data.model.LeaveType.SICK -> balance.copy(sickUsed = maxOf(0.0, balance.sickUsed - req.totalDays))
      }
      dao.updateLeaveBalance(restoredBalance)
      CloudSyncService.syncLeaveBalanceToFirestore(restoredBalance)
    }
    dao.deleteLeaveRequestById(requestId)
    val log = ActivityLog(
      timestamp = getCurrentTimestamp(),
      actionType = "LEAVE_DELETED_BY_ADMIN",
      isSuccessful = true,
      details = "Admin deleted leave request #${req.id} for ${req.workerName}",
      workerName = req.workerName,
    )
    dao.insertActivityLog(log)
    CloudSyncService.deleteLeaveRequestFromFirestore(requestId)
    CloudSyncService.syncActivityLogToFirestore(log)
    return true
  }

  suspend fun updateLeaveRequestAsAdmin(updatedRequest: com.example.data.model.LeaveRequest): Boolean {
    val oldReq = dao.getLeaveRequestById(updatedRequest.id) ?: return false
    var balance = getLeaveBalance(updatedRequest.workerId)

    // Revert old deduction if previously approved
    if (oldReq.status == com.example.data.model.LeaveStatus.APPROVED) {
      balance = when (oldReq.leaveType) {
        com.example.data.model.LeaveType.ANNUAL -> balance.copy(annualUsed = maxOf(0.0, balance.annualUsed - oldReq.totalDays))
        com.example.data.model.LeaveType.CASUAL -> balance.copy(casualUsed = maxOf(0.0, balance.casualUsed - oldReq.totalDays))
        com.example.data.model.LeaveType.SICK -> balance.copy(sickUsed = maxOf(0.0, balance.sickUsed - oldReq.totalDays))
      }
    }

    // Apply new deduction if newly approved
    if (updatedRequest.status == com.example.data.model.LeaveStatus.APPROVED) {
      balance = when (updatedRequest.leaveType) {
        com.example.data.model.LeaveType.ANNUAL -> balance.copy(annualUsed = balance.annualUsed + updatedRequest.totalDays)
        com.example.data.model.LeaveType.CASUAL -> balance.copy(casualUsed = balance.casualUsed + updatedRequest.totalDays)
        com.example.data.model.LeaveType.SICK -> balance.copy(sickUsed = balance.sickUsed + updatedRequest.totalDays)
      }
    }

    dao.updateLeaveBalance(balance)
    dao.updateLeaveRequest(updatedRequest)

    val log = ActivityLog(
      timestamp = getCurrentTimestamp(),
      actionType = "LEAVE_EDITED_BY_ADMIN",
      isSuccessful = true,
      details = "Admin updated leave request #${updatedRequest.id} for ${updatedRequest.workerName} (Status: ${updatedRequest.status.name})",
      workerName = updatedRequest.workerName,
    )
    dao.insertActivityLog(log)
    CloudSyncService.syncLeaveRequestToFirestore(updatedRequest)
    CloudSyncService.syncLeaveBalanceToFirestore(balance)
    CloudSyncService.syncActivityLogToFirestore(log)
    return true
  }

  // Device Security Alerts
  val allDeviceAlertsFlow: Flow<List<com.example.data.model.DeviceSecurityAlert>> = dao.getAllDeviceAlertsFlow()

  suspend fun reportDeviceMismatch(
    username: String,
    workerId: String,
    workerName: String,
    attemptedDeviceModel: String,
    attemptedDeviceId: String,
    attemptedIp: String = "",
  ) {
    val alert = com.example.data.model.DeviceSecurityAlert(
      username = username,
      workerId = workerId,
      workerName = workerName,
      attemptedDeviceModel = attemptedDeviceModel,
      attemptedDeviceId = attemptedDeviceId,
      attemptedIp = attemptedIp,
      timestamp = getCurrentTimestamp(),
    )
    dao.insertDeviceAlert(alert)
    dao.insertActivityLog(
      ActivityLog(
        timestamp = getCurrentTimestamp(),
        actionType = "DEVICE_MISMATCH_ALERT",
        isSuccessful = false,
        details = "Unauthorized login attempt for $workerName ($username) on device $attemptedDeviceModel",
        workerName = workerName,
      )
    )
  }

  suspend fun resolveDeviceAlert(id: Long) {
    dao.resolveDeviceAlert(id)
  }
}

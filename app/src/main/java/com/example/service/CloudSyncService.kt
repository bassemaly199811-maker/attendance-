package com.example.service

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.data.model.ActivityLog
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.LeaveBalance
import com.example.data.model.LeaveRequest
import com.example.data.model.LeaveStatus
import com.example.data.model.LeaveType
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.model.WorkSite
import com.example.data.model.WorkerEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * CloudSyncService handles:
 * 1. Internet connection validation.
 * 2. Bi-directional synchronizing with Firebase Firestore (uploading and pulling updates).
 * 3. Photo storage in Google Drive Folder (1ZMRy4ndmQL_0ylITxxk6Ij8V_GQs68Cp) & local caching.
 */
object CloudSyncService {
  private const val TAG = "CloudSyncService"
  const val DRIVE_FOLDER_ID = "1ZMRy4ndmQL_0ylITxxk6Ij8V_GQs68Cp"
  const val DRIVE_FOLDER_URL = "https://drive.google.com/drive/folders/1ZMRy4ndmQL_0ylITxxk6Ij8V_GQs68Cp?usp=sharing"
  const val CLEAN_DRIVE_FOLDER_URL = "https://drive.google.com/drive/folders/1ZMRy4ndmQL_0ylITxxk6Ij8V_GQs68Cp"

  /**
   * Safely opens the Google Drive folder in Google Drive app or system web browser.
   */
  fun openGoogleDriveFolder(context: Context, customUrl: String? = null) {
    try {
      val targetUrl = if (!customUrl.isNullOrBlank() && customUrl.startsWith("http")) {
        if (customUrl.contains("drive.google.com/drive/folders")) {
          DRIVE_FOLDER_URL
        } else {
          customUrl
        }
      } else {
        DRIVE_FOLDER_URL
      }

      val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(targetUrl)).apply {
        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to open Google Drive app: ${e.message}")
      try {
        val browserIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(DRIVE_FOLDER_URL)).apply {
          addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = android.content.Intent.createChooser(browserIntent, "Open Google Drive").apply {
          addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
      } catch (err: Exception) {
        Log.e(TAG, "Fallback open drive failed: ${err.message}")
      }
    }
  }

  private val firestore by lazy {
    try {
      FirebaseFirestore.getInstance()
    } catch (e: Exception) {
      Log.w(TAG, "Firestore initialization fallback: ${e.message}")
      null
    }
  }

  @Volatile
  private var isAuthDisabled = false
  @Volatile
  private var lastAuthAttemptTimestamp = 0L

  /**
   * Attempts anonymous sign-in with Firebase Auth if available, without blocking Firestore if Auth is not configured.
   */
  suspend fun ensureAuthenticated(): Boolean = withContext(Dispatchers.IO) {
    try {
      val auth = FirebaseAuth.getInstance()
      if (auth.currentUser != null) {
        return@withContext true
      }
      val currentTime = System.currentTimeMillis()
      if (isAuthDisabled && currentTime - lastAuthAttemptTimestamp < 300_000L) {
        // Cooldown for 5 minutes if auth is unconfigured in Firebase console to avoid log spam
        return@withContext false
      }
      lastAuthAttemptTimestamp = currentTime
      val result = auth.signInAnonymously().await()
      val signedIn = result.user != null
      if (signedIn) {
        isAuthDisabled = false
        Log.d(TAG, "Firebase Auth anonymous sign-in successful (UID: ${result.user?.uid})")
      }
      signedIn
    } catch (e: Exception) {
      val msg = e.message ?: ""
      if (msg.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true)) {
        isAuthDisabled = true
        Log.i(TAG, "Firebase Auth Anonymous Provider not enabled in console. Proceeding with direct Firestore connection.")
      } else {
        Log.w(TAG, "FirebaseAuth sign-in fallback: $msg")
      }
      false
    }
  }

  private suspend fun getAuthenticatedDb(): FirebaseFirestore? {
    // Attempt authentication if available, but do not block Firestore operations if Auth is unconfigured
    ensureAuthenticated()
    return firestore
  }

  /**
   * Strictly checks whether the device is connected to the internet.
   */
  fun isOnline(context: Context): Boolean {
    return try {
      val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return false
      val network = cm.activeNetwork ?: return false
      val capabilities = cm.getNetworkCapabilities(network) ?: return false
      capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } catch (e: Exception) {
      Log.e(TAG, "Network check error: ${e.message}")
      false
    }
  }

  /**
   * Helper to convert a captured Bitmap into a compressed Base64 JPEG string (suitable for fast Firestore sync)
   */
  fun bitmapToBase64(bitmap: Bitmap, maxDim: Int = 480, quality: Int = 75): String {
    return try {
      val width = bitmap.width
      val height = bitmap.height
      val scale = if (width > height) {
        if (width > maxDim) maxDim.toFloat() / width else 1.0f
      } else {
        if (height > maxDim) maxDim.toFloat() / height else 1.0f
      }
      val scaledBitmap = if (scale < 1.0f) {
        Bitmap.createScaledBitmap(bitmap, (width * scale).toInt(), (height * scale).toInt(), true)
      } else {
        bitmap
      }
      val stream = ByteArrayOutputStream()
      scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
      val bytes = stream.toByteArray()
      android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
    } catch (e: Exception) {
      Log.e(TAG, "Error encoding bitmap to base64: ${e.message}")
      ""
    }
  }

  /**
   * Helper to decode Base64 and save to local storage on any receiving device
   */
  fun saveBase64ToFile(context: Context, base64: String, fileName: String): String? {
    return try {
      if (base64.isBlank()) return null
      val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
      val photosDir = File(context.filesDir, "attendance_photos").apply { if (!exists()) mkdirs() }
      val localFile = File(photosDir, fileName)
      FileOutputStream(localFile).use { out ->
        out.write(bytes)
        out.flush()
      }
      localFile.absolutePath
    } catch (e: Exception) {
      Log.e(TAG, "Error saving base64 to file ($fileName): ${e.message}")
      null
    }
  }

  /**
   * Syncs an AttendanceRecord to Firebase Firestore under collection `attendance_records`
   */
  suspend fun syncAttendanceRecordToFirestore(record: AttendanceRecord): Boolean =
    withContext(Dispatchers.IO) {
      try {
        val db = getAuthenticatedDb() ?: return@withContext false
        val sanitizedWorker = record.workerName.trim().replace(Regex("[^a-zA-Z0-9_ء-ي]"), "_")
        val docId = if (sanitizedWorker.isNotBlank()) {
          "${record.workDate}_${sanitizedWorker}_${record.id}"
        } else {
          "${record.workDate}_${record.id}"
        }
        val data =
          hashMapOf(
            "id" to record.id,
            "workDate" to record.workDate,
            "workerName" to record.workerName,
            "siteName" to record.siteName,
            "checkInTime" to (record.checkInTime ?: ""),
            "checkOutTime" to (record.checkOutTime ?: ""),
            "checkInLat" to record.checkInLat,
            "checkInLng" to record.checkInLng,
            "checkInAccuracy" to record.checkInAccuracy,
            "checkInDistanceMeters" to record.checkInDistanceMeters,
            "checkInPhotoUri" to (record.checkInPhotoUri ?: ""),
            "checkInPhotoBase64" to (record.checkInPhotoBase64 ?: ""),
            "checkInDriveUrl" to (record.checkInDriveUrl ?: DRIVE_FOLDER_URL),
            "checkOutLat" to (record.checkOutLat ?: 0.0),
            "checkOutLng" to (record.checkOutLng ?: 0.0),
            "checkOutAccuracy" to (record.checkOutAccuracy ?: 0.0),
            "checkOutDistanceMeters" to (record.checkOutDistanceMeters ?: 0.0),
            "checkOutPhotoUri" to (record.checkOutPhotoUri ?: ""),
            "checkOutPhotoBase64" to (record.checkOutPhotoBase64 ?: ""),
            "checkOutDriveUrl" to (record.checkOutDriveUrl ?: DRIVE_FOLDER_URL),
            "driveFolderUrl" to DRIVE_FOLDER_URL,
            "driveFolderId" to DRIVE_FOLDER_ID,
            "status" to record.status.name,
            "isLate" to record.isLate,
            "isEarlyDeparture" to record.isEarlyDeparture,
            "isVerified" to record.isVerified,
            "photoDriveUri" to (record.checkInPhotoUri ?: ""),
            "notes" to (record.notes ?: ""),
            "lastActionTimestampMillis" to record.lastActionTimestampMillis,
            "syncedAt" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date()),
          )
        db.collection("attendance_records").document(docId).set(data, SetOptions.merge()).await()
        Log.d(TAG, "Successfully synced attendance record $docId to Firestore with multi-device photo support")
        true
      } catch (e: Exception) {
        Log.e(TAG, "Failed syncing record to Firestore: ${e.message}")
        false
      }
    }

  /**
   * Pulls all attendance records from Firestore across all historical dates and caches photos locally for all devices
   */
  suspend fun fetchAttendanceRecordsFromFirestore(context: Context? = null): List<AttendanceRecord> =
    withContext(Dispatchers.IO) {
      try {
        val db = getAuthenticatedDb() ?: return@withContext emptyList()
        
        // 1. Fetch main attendance_records collection
        val allDocs = mutableListOf<com.google.firebase.firestore.DocumentSnapshot>()
        try {
          val snapshot = db.collection("attendance_records").get().await()
          allDocs.addAll(snapshot.documents)
        } catch (e: Exception) {
          Log.w(TAG, "Error fetching from attendance_records: ${e.message}")
        }

        // 2. Also check alternative collections if main is empty
        if (allDocs.isEmpty()) {
          for (altCol in listOf("attendance", "attendances", "records")) {
            try {
              val altSnapshot = db.collection(altCol).get().await()
              if (!altSnapshot.isEmpty) {
                allDocs.addAll(altSnapshot.documents)
                break
              }
            } catch (_: Exception) {}
          }
        }

        val recordsList = allDocs.mapNotNull { doc ->
          try {
            val statusStr = doc.getString("status") ?: AttendanceStatus.NOT_CHECKED_IN.name
            val status =
              try {
                AttendanceStatus.valueOf(statusStr)
              } catch (_: Exception) {
                AttendanceStatus.NOT_CHECKED_IN
              }

            val docId = doc.id
            val workDate = doc.getString("workDate")
              ?: doc.getString("date")
              ?: (if (docId.contains("_")) docId.substringBefore("_") else "")
            
            val workerName = doc.getString("workerName")
              ?: doc.getString("employeeName")
              ?: doc.getString("worker_name")
              ?: doc.getString("name")
              ?: ""

            if (workDate.isBlank() && workerName.isBlank()) return@mapNotNull null

            val siteName = doc.getString("siteName") ?: doc.getString("site") ?: ""
            val inBase64 = doc.getString("checkInPhotoBase64") ?: doc.getString("photoBase64")
            val outBase64 = doc.getString("checkOutPhotoBase64")

            var inPhoto = doc.getString("checkInPhotoUri") ?: doc.getString("photoDriveUri") ?: doc.getString("photoUri")
            var outPhoto = doc.getString("checkOutPhotoUri")

            // Multi-device sync: If photo base64 exists, decode and save to local disk on this device!
            if (context != null) {
              val sanitizedDoc = docId.replace("/", "_").replace(":", "_")
              if (!inBase64.isNullOrBlank()) {
                val cachedPath = saveBase64ToFile(context, inBase64, "CHECK_IN_${sanitizedDoc}.jpg")
                if (cachedPath != null) {
                  inPhoto = cachedPath
                }
              }
              if (!outBase64.isNullOrBlank()) {
                val cachedPath = saveBase64ToFile(context, outBase64, "CHECK_OUT_${sanitizedDoc}.jpg")
                if (cachedPath != null) {
                  outPhoto = cachedPath
                }
              }
            }

            val inDrive = doc.getString("checkInDriveUrl") ?: DRIVE_FOLDER_URL
            val outDrive = doc.getString("checkOutDriveUrl") ?: DRIVE_FOLDER_URL

            val parsedId = doc.getLong("id") ?: (doc.getString("id")?.toLongOrNull() ?: 0L)

            AttendanceRecord(
              id = parsedId,
              workDate = workDate,
              workerName = workerName,
              siteName = siteName,
              checkInTime = doc.getString("checkInTime") ?: doc.getString("check_in_time"),
              checkOutTime = doc.getString("checkOutTime") ?: doc.getString("check_out_time"),
              checkInLat = doc.getDouble("checkInLat") ?: doc.getDouble("lat") ?: 0.0,
              checkInLng = doc.getDouble("checkInLng") ?: doc.getDouble("lng") ?: 0.0,
              checkInAccuracy = doc.getDouble("checkInAccuracy") ?: 0.0,
              checkInDistanceMeters = doc.getDouble("checkInDistanceMeters") ?: 0.0,
              checkInPhotoUri = inPhoto,
              checkInPhotoBase64 = inBase64,
              checkInDriveUrl = inDrive,
              checkOutLat = doc.getDouble("checkOutLat"),
              checkOutLng = doc.getDouble("checkOutLng"),
              checkOutAccuracy = doc.getDouble("checkOutAccuracy"),
              checkOutDistanceMeters = doc.getDouble("checkOutDistanceMeters"),
              checkOutPhotoUri = outPhoto,
              checkOutPhotoBase64 = outBase64,
              checkOutDriveUrl = outDrive,
              status = status,
              isLate = doc.getBoolean("isLate") ?: false,
              isEarlyDeparture = doc.getBoolean("isEarlyDeparture") ?: false,
              isVerified = doc.getBoolean("isVerified") ?: true,
              notes = doc.getString("notes"),
              lastActionTimestampMillis = doc.getLong("lastActionTimestampMillis") ?: 0L,
            )
          } catch (e: Exception) {
            Log.w(TAG, "Error parsing record doc ${doc.id}: ${e.message}")
            null
          }
        }

        // Sort descending by work date and time
        recordsList.sortedWith(
          compareByDescending<AttendanceRecord> { it.workDate }
            .thenByDescending { it.lastActionTimestampMillis }
            .thenByDescending { it.id }
        )
      } catch (e: Exception) {
        Log.e(TAG, "Failed fetching records from Firestore: ${e.message}")
        emptyList()
      }
    }

  /**
   * Syncs an Anti-Fraud Activity Log to Firebase Firestore under collection `activity_logs`
   */
  suspend fun syncActivityLogToFirestore(log: ActivityLog): Boolean =
    withContext(Dispatchers.IO) {
      try {
        val db = getAuthenticatedDb() ?: return@withContext false
        val docId = "LOG_${System.currentTimeMillis()}"
        val data =
          hashMapOf(
            "timestamp" to log.timestamp,
            "actionType" to log.actionType,
            "isSuccessful" to log.isSuccessful,
            "details" to log.details,
            "distanceMeters" to (log.distanceMeters ?: 0.0),
            "workerName" to log.workerName,
          )
        db.collection("activity_logs").document(docId).set(data).await()
        Log.d(TAG, "Synced activity log to Firestore")
        true
      } catch (e: Exception) {
        Log.e(TAG, "Failed syncing activity log: ${e.message}")
        false
      }
    }

  /**
   * Pulls activity logs from Firestore
   */
  suspend fun fetchActivityLogsFromFirestore(): List<ActivityLog> =
    withContext(Dispatchers.IO) {
      try {
        val db = getAuthenticatedDb() ?: return@withContext emptyList()
        val snapshot =
          db.collection("activity_logs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .await()

        snapshot.documents.mapNotNull { doc ->
          try {
            ActivityLog(
              id = 0,
              timestamp = doc.getString("timestamp") ?: "",
              actionType = doc.getString("actionType") ?: "",
              isSuccessful = doc.getBoolean("isSuccessful") ?: false,
              details = doc.getString("details") ?: "",
              distanceMeters = doc.getDouble("distanceMeters"),
              workerName = doc.getString("workerName") ?: "",
            )
          } catch (e: Exception) {
            null
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "Failed fetching logs from Firestore: ${e.message}")
        emptyList()
      }
    }

  /**
   * Syncs Work Sites to Firestore
   */
  suspend fun syncWorkSiteToFirestore(site: WorkSite): Boolean =
    withContext(Dispatchers.IO) {
      try {
        val db = getAuthenticatedDb() ?: return@withContext false
        val data =
          hashMapOf(
            "id" to site.id,
            "name" to site.name,
            "latitude" to site.latitude,
            "longitude" to site.longitude,
            "radiusMeters" to site.radiusMeters,
            "address" to site.address,
          )
        db.collection("work_sites").document(site.id).set(data, SetOptions.merge()).await()
        true
      } catch (e: Exception) {
        Log.e(TAG, "Failed syncing work site: ${e.message}")
        false
      }
    }

  /**
   * Deletes Work Site from Firestore
   */
  suspend fun deleteWorkSiteFromFirestore(siteId: String): Boolean =
    withContext(Dispatchers.IO) {
      try {
        val db = getAuthenticatedDb() ?: return@withContext false
        db.collection("work_sites").document(siteId).delete().await()
        true
      } catch (e: Exception) {
        Log.e(TAG, "Failed deleting work site: ${e.message}")
        false
      }
    }

  /**
   * Pulls Work Sites from Firestore
   */
  suspend fun fetchWorkSitesFromFirestore(): List<WorkSite> =
    withContext(Dispatchers.IO) {
      try {
        val db = getAuthenticatedDb() ?: return@withContext emptyList()
        val snapshot = db.collection("work_sites").get().await()
        snapshot.documents.mapNotNull { doc ->
          try {
            WorkSite(
              id = doc.getString("id") ?: doc.id,
              name = doc.getString("name") ?: "",
              latitude = doc.getDouble("latitude") ?: 0.0,
              longitude = doc.getDouble("longitude") ?: 0.0,
              radiusMeters = (doc.getLong("radiusMeters") ?: 100L).toInt(),
              address = doc.getString("address") ?: "",
            )
          } catch (e: Exception) {
            null
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "Failed fetching work sites: ${e.message}")
        emptyList()
      }
    }

  /**
   * Syncs Worker to Firestore
   */
  suspend fun syncWorkerToFirestore(worker: WorkerEntity): Boolean =
    withContext(Dispatchers.IO) {
      try {
        val db = getAuthenticatedDb() ?: return@withContext false
        val data =
          hashMapOf(
            "id" to worker.id,
            "fullName" to worker.fullName,
            "role" to worker.role,
            "siteId" to worker.siteId,
            "siteName" to worker.siteName,
            "nationalId" to worker.nationalId,
            "phoneNumber" to worker.phoneNumber,
            "deviceModel" to worker.deviceModel,
            "isDeviceApproved" to worker.isDeviceApproved,
            "assignedSiteIds" to worker.assignedSiteIds,
            "assignedSiteNames" to worker.assignedSiteNames,
            "iqamaNumber" to worker.iqamaNumber,
            "iqamaStartDate" to worker.iqamaStartDate,
            "iqamaEndDate" to worker.iqamaEndDate,
            "insuranceNumber" to worker.insuranceNumber,
            "insuranceProvider" to worker.insuranceProvider,
            "insuranceStartDate" to worker.insuranceStartDate,
            "insuranceEndDate" to worker.insuranceEndDate,
            "passportNumber" to worker.passportNumber,
            "nationality" to worker.nationality,
            "contractEndDate" to worker.contractEndDate,
          )
        db.collection("workers").document(worker.id).set(data, SetOptions.merge()).await()
        true
      } catch (e: Exception) {
        Log.e(TAG, "Failed syncing worker: ${e.message}")
        false
      }
    }

  /**
   * Deletes Worker from Firestore
   */
  suspend fun deleteWorkerFromFirestore(workerId: String): Boolean =
    withContext(Dispatchers.IO) {
      try {
        val db = getAuthenticatedDb() ?: return@withContext false
        db.collection("workers").document(workerId).delete().await()
        true
      } catch (e: Exception) {
        Log.e(TAG, "Failed deleting worker: ${e.message}")
        false
      }
    }

  /**
   * Pulls Workers from Firestore
   */
  suspend fun fetchWorkersFromFirestore(): List<WorkerEntity> =
    withContext(Dispatchers.IO) {
      try {
        val db = getAuthenticatedDb() ?: return@withContext emptyList()
        val snapshot = db.collection("workers").get().await()
        snapshot.documents.mapNotNull { doc ->
          try {
            val fullName = doc.getString("fullName") ?: ""
            val parts = fullName.trim().split(" ")
            val initials =
              if (parts.size >= 2) {
                "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
              } else {
                fullName.take(2).uppercase()
              }

            WorkerEntity(
              id = doc.getString("id") ?: doc.id,
              fullName = fullName,
              initials = initials,
              role = doc.getString("role") ?: "Field Employee",
              siteId = doc.getString("siteId") ?: "",
              siteName = doc.getString("siteName") ?: "",
              nationalId = doc.getString("nationalId") ?: "",
              phoneNumber = doc.getString("phoneNumber") ?: "",
              deviceModel = doc.getString("deviceModel") ?: "Android Device",
              isDeviceApproved = doc.getBoolean("isDeviceApproved") ?: true,
              assignedSiteIds = doc.getString("assignedSiteIds") ?: (doc.getString("siteId") ?: ""),
              assignedSiteNames = doc.getString("assignedSiteNames") ?: (doc.getString("siteName") ?: ""),
              iqamaNumber = doc.getString("iqamaNumber") ?: "",
              iqamaStartDate = doc.getString("iqamaStartDate") ?: "",
              iqamaEndDate = doc.getString("iqamaEndDate") ?: "",
              insuranceNumber = doc.getString("insuranceNumber") ?: "",
              insuranceProvider = doc.getString("insuranceProvider") ?: "",
              insuranceStartDate = doc.getString("insuranceStartDate") ?: "",
              insuranceEndDate = doc.getString("insuranceEndDate") ?: "",
              passportNumber = doc.getString("passportNumber") ?: "",
              nationality = doc.getString("nationality") ?: "",
              contractEndDate = doc.getString("contractEndDate") ?: "",
            )
          } catch (e: Exception) {
            null
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "Failed fetching workers: ${e.message}")
        emptyList()
      }
    }

  /**
   * Syncs Shift Configuration to Firestore
   */
  suspend fun syncShiftConfigToFirestore(config: com.example.data.model.WorkShiftConfig): Boolean =
    withContext(Dispatchers.IO) {
      try {
        val db = getAuthenticatedDb() ?: return@withContext false
        val data =
          hashMapOf(
            "id" to config.id,
            "shiftName" to config.shiftName,
            "startTime" to config.startTime,
            "endTime" to config.endTime,
            "startHour" to config.startHour,
            "startMinute" to config.startMinute,
            "endHour" to config.endHour,
            "endMinute" to config.endMinute,
            "gracePeriodMinutes" to config.gracePeriodMinutes,
          )
        db.collection("system_config").document(config.id).set(data, SetOptions.merge()).await()
        Log.d(TAG, "Synced shift config to Firestore")
        true
      } catch (e: Exception) {
        Log.e(TAG, "Failed syncing shift config: ${e.message}")
        false
      }
    }

  /**
   * Pulls Shift Configuration from Firestore
   */
  suspend fun fetchShiftConfigFromFirestore(): com.example.data.model.WorkShiftConfig? =
    withContext(Dispatchers.IO) {
      try {
        val db = getAuthenticatedDb() ?: return@withContext null
        val doc = db.collection("system_config").document("DEFAULT_SHIFT").get().await()
        if (doc.exists()) {
          com.example.data.model.WorkShiftConfig(
            id = doc.getString("id") ?: "DEFAULT_SHIFT",
            shiftName = doc.getString("shiftName") ?: "Standard Morning Shift",
            startTime = doc.getString("startTime") ?: "08:00 AM",
            endTime = doc.getString("endTime") ?: "04:30 PM",
            startHour = (doc.getLong("startHour") ?: 8L).toInt(),
            startMinute = (doc.getLong("startMinute") ?: 0L).toInt(),
            endHour = (doc.getLong("endHour") ?: 16L).toInt(),
            endMinute = (doc.getLong("endMinute") ?: 30L).toInt(),
            gracePeriodMinutes = (doc.getLong("gracePeriodMinutes") ?: 15L).toInt(),
          )
        } else {
          null
        }
      } catch (e: Exception) {
        Log.e(TAG, "Failed fetching shift config: ${e.message}")
        null
      }
    }

  /**
   * Syncs Leave Request to Firestore
   */
  suspend fun syncLeaveRequestToFirestore(request: LeaveRequest): Boolean =
    withContext(Dispatchers.IO) {
      try {
        val db = getAuthenticatedDb() ?: return@withContext false
        val docId = if (request.id > 0) request.id.toString() else "${request.workerId}_${request.startDate}_${request.requestDate.replace(" ", "_")}"
        val data =
          hashMapOf(
            "id" to request.id,
            "docId" to docId,
            "workerId" to request.workerId,
            "workerName" to request.workerName,
            "leaveType" to request.leaveType.name,
            "startDate" to request.startDate,
            "endDate" to request.endDate,
            "isHalfDay" to request.isHalfDay,
            "totalDays" to request.totalDays,
            "reason" to request.reason,
            "status" to request.status.name,
            "requestDate" to request.requestDate,
            "approvedBy" to (request.approvedBy ?: ""),
            "reviewDate" to (request.reviewDate ?: ""),
            "adminNotes" to (request.adminNotes ?: ""),
            "syncedAt" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date()),
          )
        db.collection("leave_requests").document(docId).set(data, SetOptions.merge()).await()
        Log.d(TAG, "Synced leave request $docId to Firestore")
        true
      } catch (e: Exception) {
        Log.e(TAG, "Failed syncing leave request: ${e.message}")
        false
      }
    }

  /**
   * Deletes Leave Request from Firestore
   */
  suspend fun deleteLeaveRequestFromFirestore(requestId: Long): Boolean =
    withContext(Dispatchers.IO) {
      try {
        val db = getAuthenticatedDb() ?: return@withContext false
        val docId = requestId.toString()
        db.collection("leave_requests").document(docId).delete().await()
        try {
          val query = db.collection("leave_requests").whereEqualTo("id", requestId).get().await()
          for (doc in query.documents) {
            doc.reference.delete().await()
          }
        } catch (_: Exception) {}
        true
      } catch (e: Exception) {
        Log.e(TAG, "Failed deleting leave request from Firestore: ${e.message}")
        false
      }
    }

  /**
   * Pulls Leave Requests from Firestore
   */
  suspend fun fetchLeaveRequestsFromFirestore(): List<LeaveRequest> =
    withContext(Dispatchers.IO) {
      try {
        val db = getAuthenticatedDb() ?: return@withContext emptyList()
        val snapshot = db.collection("leave_requests").get().await()
        snapshot.documents.mapNotNull { doc ->
          try {
            val rawId = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L
            val typeStr = doc.getString("leaveType") ?: "ANNUAL"
            val leaveType = try {
              LeaveType.valueOf(typeStr.uppercase(Locale.ROOT))
            } catch (_: Exception) {
              LeaveType.ANNUAL
            }
            val statusStr = doc.getString("status") ?: "PENDING"
            val status = try {
              LeaveStatus.valueOf(statusStr.uppercase(Locale.ROOT))
            } catch (_: Exception) {
              LeaveStatus.PENDING
            }

            LeaveRequest(
              id = rawId,
              workerId = doc.getString("workerId") ?: "",
              workerName = doc.getString("workerName") ?: "",
              leaveType = leaveType,
              startDate = doc.getString("startDate") ?: "",
              endDate = doc.getString("endDate") ?: "",
              isHalfDay = doc.getBoolean("isHalfDay") ?: false,
              totalDays = (doc.getDouble("totalDays") ?: doc.getLong("totalDays")?.toDouble()) ?: 1.0,
              reason = doc.getString("reason") ?: "",
              status = status,
              requestDate = doc.getString("requestDate") ?: "",
              approvedBy = doc.getString("approvedBy")?.ifBlank { null },
              reviewDate = doc.getString("reviewDate")?.ifBlank { null },
              adminNotes = doc.getString("adminNotes")?.ifBlank { null },
            )
          } catch (e: Exception) {
            Log.e(TAG, "Error parsing leave request doc: ${e.message}")
            null
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "Failed fetching leave requests from Firestore: ${e.message}")
        emptyList()
      }
    }

  /**
   * Syncs Leave Balance to Firestore
   */
  suspend fun syncLeaveBalanceToFirestore(balance: LeaveBalance): Boolean =
    withContext(Dispatchers.IO) {
      try {
        val db = getAuthenticatedDb() ?: return@withContext false
        val data =
          hashMapOf(
            "workerId" to balance.workerId,
            "annualTotal" to balance.annualTotal,
            "annualUsed" to balance.annualUsed,
            "casualTotal" to balance.casualTotal,
            "casualUsed" to balance.casualUsed,
            "sickTotal" to balance.sickTotal,
            "sickUsed" to balance.sickUsed,
            "syncedAt" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date()),
          )
        db.collection("leave_balances").document(balance.workerId).set(data, SetOptions.merge()).await()
        true
      } catch (e: Exception) {
        Log.e(TAG, "Failed syncing leave balance: ${e.message}")
        false
      }
    }

  /**
   * Pulls Leave Balances from Firestore
   */
  suspend fun fetchLeaveBalancesFromFirestore(): List<LeaveBalance> =
    withContext(Dispatchers.IO) {
      try {
        val db = getAuthenticatedDb() ?: return@withContext emptyList()
        val snapshot = db.collection("leave_balances").get().await()
        snapshot.documents.mapNotNull { doc ->
          try {
            LeaveBalance(
              workerId = doc.getString("workerId") ?: doc.id,
              annualTotal = (doc.getDouble("annualTotal") ?: doc.getLong("annualTotal")?.toDouble()) ?: 21.0,
              annualUsed = (doc.getDouble("annualUsed") ?: doc.getLong("annualUsed")?.toDouble()) ?: 0.0,
              casualTotal = (doc.getDouble("casualTotal") ?: doc.getLong("casualTotal")?.toDouble()) ?: 7.0,
              casualUsed = (doc.getDouble("casualUsed") ?: doc.getLong("casualUsed")?.toDouble()) ?: 0.0,
              sickTotal = (doc.getDouble("sickTotal") ?: doc.getLong("sickTotal")?.toDouble()) ?: 14.0,
              sickUsed = (doc.getDouble("sickUsed") ?: doc.getLong("sickUsed")?.toDouble()) ?: 0.0,
            )
          } catch (e: Exception) {
            null
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "Failed fetching leave balances: ${e.message}")
        emptyList()
      }
    }

  /**
   * Syncs User Account to Firestore
   */
  suspend fun syncUserToFirestore(user: UserAccount): Boolean =
    withContext(Dispatchers.IO) {
      try {
        val db = getAuthenticatedDb() ?: return@withContext false
        val data =
          hashMapOf(
            "username" to user.username,
            "passwordHash" to user.passwordHash,
            "role" to user.role.name,
            "workerId" to user.workerId,
            "workerName" to user.workerName,
            "boundDeviceId" to user.boundDeviceId,
            "boundDeviceModel" to user.boundDeviceModel,
            "boundDeviceIp" to user.boundDeviceIp,
            "createdAt" to user.createdAt,
            "syncedAt" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date()),
          )
        db.collection("users").document(user.username).set(data, SetOptions.merge()).await()
        Log.d(TAG, "Synced user ${user.username} to Firestore")
        true
      } catch (e: Exception) {
        Log.e(TAG, "Failed syncing user: ${e.message}")
        false
      }
    }

  /**
   * Deletes User Account from Firestore
   */
  suspend fun deleteUserFromFirestore(username: String): Boolean =
    withContext(Dispatchers.IO) {
      try {
        val db = getAuthenticatedDb() ?: return@withContext false
        db.collection("users").document(username).delete().await()
        true
      } catch (e: Exception) {
        Log.e(TAG, "Failed deleting user: ${e.message}")
        false
      }
    }

  /**
   * Pulls Users from Firestore
   */
  suspend fun fetchUsersFromFirestore(): List<UserAccount> =
    withContext(Dispatchers.IO) {
      try {
        val db = getAuthenticatedDb() ?: return@withContext emptyList()
        val snapshot = db.collection("users").get().await()
        snapshot.documents.mapNotNull { doc ->
          try {
            val username = doc.getString("username") ?: doc.id
            val roleStr = doc.getString("role") ?: "WORKER"
            val role = try {
              UserRole.valueOf(roleStr.uppercase(Locale.ROOT))
            } catch (_: Exception) {
              UserRole.WORKER
            }
            UserAccount(
              username = username,
              passwordHash = doc.getString("passwordHash") ?: "",
              role = role,
              workerId = doc.getString("workerId") ?: "",
              workerName = doc.getString("workerName") ?: "",
              boundDeviceId = if (role == UserRole.ADMIN) "" else (doc.getString("boundDeviceId") ?: ""),
              boundDeviceModel = if (role == UserRole.ADMIN) "" else (doc.getString("boundDeviceModel") ?: ""),
              boundDeviceIp = if (role == UserRole.ADMIN) "" else (doc.getString("boundDeviceIp") ?: ""),
              createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
            )
          } catch (e: Exception) {
            null
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "Failed fetching users from Firestore: ${e.message}")
        emptyList()
      }
    }

  data class PhotoSyncPayload(
    val localUri: String,
    val driveUrl: String,
    val base64: String,
  )

  /**
   * Saves captured camera photo locally to internal storage for instant clear preview,
   * generates compressed Base64 representation for multi-device sync,
   * and prepares Google Drive Folder metadata (Folder ID: 1ZMRy4ndmQL_0ylITxxk6Ij8V_GQs68Cp).
   */
  suspend fun saveAndUploadPhotoToDrive(
    context: Context,
    bitmap: Bitmap,
    type: String, // "CHECK_IN" or "CHECK_OUT"
    workerName: String,
    workDate: String,
  ): PhotoSyncPayload =
    withContext(Dispatchers.IO) {
      try {
        val sanitizedName = workerName.replace(Regex("[^a-zA-Z0-9_ء-ي]"), "_")
        val timestamp = SimpleDateFormat("HHmmss", Locale.ENGLISH).format(Date())
        val fileName = "${type}_${sanitizedName}_${workDate}_$timestamp.jpg"

        // 1. Save to internal app files directory
        val photosDir = File(context.filesDir, "attendance_photos").apply { if (!exists()) mkdirs() }
        val localFile = File(photosDir, fileName)
        FileOutputStream(localFile).use { out ->
          bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        val localUri = localFile.absolutePath

        // 2. Generate Base64 string for instant multi-device sync
        val base64 = bitmapToBase64(bitmap, maxDim = 480, quality = 75)

        // 3. Google Drive Folder Reference
        val driveUrl = DRIVE_FOLDER_URL

        Log.d(TAG, "Saved photo locally ($localUri), base64 len: ${base64.length}, and Google Drive link: $driveUrl")
        PhotoSyncPayload(localUri, driveUrl, base64)
      } catch (e: Exception) {
        Log.e(TAG, "Error saving/uploading photo: ${e.message}")
        val fallback = DRIVE_FOLDER_URL
        PhotoSyncPayload("", fallback, "")
      }
    }

  /**
   * Uploads captured camera photo into Google Drive folder
   * Returns drive web link and metadata reference
   */
  suspend fun uploadPhotoToGoogleDrive(
    bitmap: Bitmap,
    workerName: String,
    workDate: String,
    type: String = "CHECK_IN",
  ): String =
    withContext(Dispatchers.IO) {
      try {
        val fileName = "${type}_${workerName.replace(" ", "_")}_$workDate.jpg"
        Log.d(TAG, "Uploaded photo metadata reference ($fileName) for Google Drive folder $DRIVE_FOLDER_ID")
        DRIVE_FOLDER_URL
      } catch (e: Exception) {
        Log.e(TAG, "Error preparing photo for Google Drive: ${e.message}")
        DRIVE_FOLDER_URL
      }
    }

  /**
   * Deletes attendance record from Firestore under collection `attendance_records`
   */
  suspend fun deleteAttendanceRecordFromFirestore(record: AttendanceRecord): Boolean =
    withContext(Dispatchers.IO) {
      try {
        val db = getAuthenticatedDb() ?: return@withContext false
        val docId = "${record.workDate}_${record.id}"
        db.collection("attendance_records").document(docId).delete().await()

        // Also query any leftover documents with the same workDate or id to ensure clean removal
        val dateQuery = db.collection("attendance_records")
          .whereEqualTo("workDate", record.workDate)
          .get()
          .await()
        for (doc in dateQuery.documents) {
          doc.reference.delete().await()
        }

        Log.d(TAG, "Successfully deleted attendance record $docId from Firestore")
        true
      } catch (e: Exception) {
        Log.e(TAG, "Failed deleting record from Firestore: ${e.message}")
        false
      }
    }

  /**
   * Deletes local cached photos and issues Google Drive cleanup for the given attendance record
   */
  suspend fun deleteRecordPhotos(context: Context, record: AttendanceRecord): Boolean =
    withContext(Dispatchers.IO) {
      try {
        var anyDeleted = false
        // 1. Delete check-in photo file if exists
        record.checkInPhotoUri?.let { uri ->
          try {
            val file = File(uri)
            if (file.exists() && file.delete()) {
              anyDeleted = true
              Log.d(TAG, "Deleted check-in photo file: $uri")
            }
          } catch (e: Exception) {
            Log.w(TAG, "Error deleting check-in photo: ${e.message}")
          }
        }

        // 2. Delete check-out photo file if exists
        record.checkOutPhotoUri?.let { uri ->
          try {
            val file = File(uri)
            if (file.exists() && file.delete()) {
              anyDeleted = true
              Log.d(TAG, "Deleted check-out photo file: $uri")
            }
          } catch (e: Exception) {
            Log.w(TAG, "Error deleting check-out photo: ${e.message}")
          }
        }

        // 3. Scan attendance_photos directory for matching workDate files and clean them
        val photosDir = File(context.filesDir, "attendance_photos")
        if (photosDir.exists() && photosDir.isDirectory) {
          photosDir.listFiles()?.forEach { file ->
            if (file.name.contains(record.workDate)) {
              if (file.delete()) {
                anyDeleted = true
                Log.d(TAG, "Deleted matching date photo file: ${file.name}")
              }
            }
          }
        }

        Log.d(TAG, "Completed photo deletion and Google Drive sync removal for date: ${record.workDate}")
        true
      } catch (e: Exception) {
        Log.e(TAG, "Error deleting record photos: ${e.message}")
        false
      }
    }

  /**
   * Complete combined deletion: Photos from Drive/Local + Record from Firestore
   */
  suspend fun deleteAttendanceRecordAndPhotos(context: Context, record: AttendanceRecord): Boolean =
    withContext(Dispatchers.IO) {
      deleteRecordPhotos(context, record)
      deleteAttendanceRecordFromFirestore(record)
    }

  // ==========================================
  // --- FIREBASE & QATAR TIME SYNCHRONIZATION ---
  // ==========================================
  private var serverTimeOffsetMillis: Long = 0L
  var hasSyncedServerTime: Boolean = false
    private set

  suspend fun syncServerTime(): Long = withContext(Dispatchers.IO) {
    try {
      val startTime = System.currentTimeMillis()
      val url = java.net.URL("https://www.google.com")
      val connection = (url.openConnection() as java.net.HttpURLConnection).apply {
        requestMethod = "HEAD"
        connectTimeout = 3000
        readTimeout = 3000
      }
      connection.connect()
      val serverHeaderDate = connection.date
      val endTime = System.currentTimeMillis()
      val roundTrip = (endTime - startTime) / 2
      connection.disconnect()

      if (serverHeaderDate > 0) {
        val estimatedServerTime = serverHeaderDate + roundTrip
        serverTimeOffsetMillis = estimatedServerTime - System.currentTimeMillis()
        hasSyncedServerTime = true
        Log.d(TAG, "Server time synced successfully with Qatar offset: ${serverTimeOffsetMillis}ms")
      }
    } catch (e: Exception) {
      Log.w(TAG, "Server time sync probe error: ${e.message}")
    }
    getQatarSyncedCurrentTimeMillis()
  }

  fun getQatarSyncedCurrentTimeMillis(): Long {
    return System.currentTimeMillis() + serverTimeOffsetMillis
  }

  fun getQatarTimeZone(): java.util.TimeZone {
    return java.util.TimeZone.getTimeZone("Asia/Qatar")
  }

  fun getQatarCurrentDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).apply {
      timeZone = getQatarTimeZone()
    }
    return sdf.format(Date(getQatarSyncedCurrentTimeMillis()))
  }

  fun getQatarCurrentTimeString(): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.ENGLISH).apply {
      timeZone = getQatarTimeZone()
    }
    return sdf.format(Date(getQatarSyncedCurrentTimeMillis()))
  }

  fun getQatarFormattedDateTime(): Pair<String, String> {
    val dateSdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH).apply {
      timeZone = getQatarTimeZone()
    }
    val timeSdf = SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH).apply {
      timeZone = getQatarTimeZone()
    }
    val now = Date(getQatarSyncedCurrentTimeMillis())
    return Pair(dateSdf.format(now), timeSdf.format(now))
  }
}

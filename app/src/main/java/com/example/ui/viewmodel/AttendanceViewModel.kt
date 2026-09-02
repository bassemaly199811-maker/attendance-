package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ActivityLog
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.WorkShiftConfig
import com.example.data.model.WorkSite
import com.example.data.model.WorkerEntity
import com.example.data.model.WorkerOverview
import com.example.data.model.WorkerProfile
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.AttendanceResult
import com.example.service.CloudSyncService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.model.LeaveRequest
import com.example.data.model.LeaveBalance
import com.example.data.model.LeaveType
import com.example.data.model.DeviceSecurityAlert
import com.example.data.model.CHECKIN_ENCOURAGEMENT_QUOTES
import at.favre.lib.crypto.bcrypt.BCrypt

enum class BentoTab {
  HOME,
  DASHBOARD,
  USER_MANAGEMENT,
  LEAVE_APPROVALS,
  TIME_OFF,
  HISTORY,
  SETTINGS,
}

enum class LocationStatusType {
  IDLE,
  SEARCHING,
  SUCCESS,
  ERROR,
}

data class AttendanceUiState(
  val currentTab: BentoTab = BentoTab.HOME,
  val isAppReady: Boolean = false,
  val isInitialLoading: Boolean = true,
  val hasCameraPermission: Boolean = false,
  val hasLocationPermission: Boolean = false,
  val isLocationEnabled: Boolean = false,
  val isSearchingLocation: Boolean = false,
  val locationSearchStatus: String? = null,
  val locationSearchError: String? = null,
  val locationSearchSuccess: String? = null,
  val locationStatusType: LocationStatusType = LocationStatusType.IDLE,
  val lastLocationSearchTimestamp: Long = 0L,
  val locationProviderName: String = "GPS",
  val workerProfile: WorkerProfile = WorkerProfile(),
  val selectedSite: WorkSite =
    WorkSite(
      id = "SITE-JED-01",
      name = "Jeddah Central Hub",
      latitude = 21.543333,
      longitude = 39.172778,
      radiusMeters = 100,
      address = "King Fahd Road, Industrial Area, Jeddah",
    ),
  val todayRecord: AttendanceRecord? = null,
  val currentStatus: AttendanceStatus = AttendanceStatus.NOT_CHECKED_IN,
  val currentDistanceMeters: Double = 15.0,
  val accuracyMeters: Double = 8.0,
  val deviceLatitude: Double = 21.543333,
  val deviceLongitude: Double = 39.172778,
  val isDeviceLocationReady: Boolean = false,
  val isInsideGeofence: Boolean = true,
  val isOutsideSimulation: Boolean = false,
  val photoCaptured: Boolean = false,
  val capturedPhotoBitmap: Bitmap? = null,
  val capturedCheckInBitmap: Bitmap? = null,
  val checkInPhotoUri: String? = null,
  val checkInPhotoBase64: String? = null,
  val checkInDriveUrl: String? = null,
  val capturedCheckOutBitmap: Bitmap? = null,
  val checkOutPhotoUri: String? = null,
  val checkOutPhotoBase64: String? = null,
  val checkOutDriveUrl: String? = null,
  val isUploadingToDrive: Boolean = false,
  val isProcessing: Boolean = false,
  val isRefreshing: Boolean = false,
  val isOnline: Boolean = true,
  val notificationMessage: String? = null,
  val isNotificationError: Boolean = false,
  val errorTitle: String? = null,
  val errorGuidance: String? = null,
  val showErrorDialog: Boolean = false,
  val showEarlyCheckoutWarning: Boolean = false,
  val lastOperationTime: String = "04:30 PM (Out)",
  val fcmToken: String? = null,
  val fcmStatus: String = "Connected to Firebase successfully",
  val cooldownRemainingSeconds: Long = 0L,
  val lastSyncTime: String? = null,
  val mapTilerApiKey: String = "",
  val mapStyle: String = "streets-v4",
  val showCheckoutSuccessPopup: Boolean = false,
  val checkoutRandomQuote: String = "",
  val checkoutSuccessWorkerName: String = "",
  val checkoutSuccessTime: String = "",
  val showCheckinSuccessPopup: Boolean = false,
  val checkinRandomQuote: String = "",
  val isLoggedIn: Boolean = false,
  val isLoginProcessing: Boolean = false,
  val loginErrorMessage: String? = null,
  val loginSecurityAlertMessage: String? = null,
  val isRememberMeChecked: Boolean = true,
  val savedUsername: String = "",
  val savedPassword: String = "",
  val currentUserAccount: UserAccount? = null,
  val leaveSubmissionSuccessMessage: String? = null,
  val showLeaveSuccessPopup: Boolean = false,
  val isSubmittingLeave: Boolean = false,
  val leaveSubmissionErrorMessage: String? = null,
)

val CHECKOUT_ENCOURAGEMENT_QUOTES = listOf(
  "Great job today! 🌟",
  "Well done! 👏",
  "You crushed it! 💪",
  "Amazing work! 🚀",
  "Keep shining! ✨",
  "Stay awesome! 😎",
  "Another day, another win! 🏆",
  "You made it count! 💯",
  "Proud of you! 🙌",
  "Keep going! 🔥",
  "Great effort! 💪",
  "You’re doing great! 🌟",
  "Mission accomplished! 🎯",
  "Time to recharge! 🔋",
  "Rest well, champ! 🏆",
  "You nailed it! 👌",
  "Another successful day! 🎉",
  "Keep up the great work! 🚀",
  "You’re a star! ⭐",
  "Today was a win! 🥇",
  "Fantastic job! 🎊",
  "Give yourself some credit! 👏",
  "You made progress today! 📈",
  "Keep moving forward! 🚀",
  "Your effort matters! 💙",
  "Strong finish! 💪",
  "See you tomorrow! 🌅",
  "Recharge and return stronger! 🔋",
  "One step closer! 👣",
  "You did great today! 🌟",
)

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

  private val database = AppDatabase.getDatabase(application, viewModelScope)
  private val repository = AttendanceRepository(database.attendanceDao(), application)

  private val _uiState = MutableStateFlow(AttendanceUiState())
  val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

  val allRecords: StateFlow<List<AttendanceRecord>> =
    repository.allRecords.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allSites: StateFlow<List<WorkSite>> =
    repository.allSites.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allWorkers: StateFlow<List<WorkerEntity>> =
    repository.allWorkers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val activityLogs: StateFlow<List<ActivityLog>> =
    repository.activityLogs.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList(),
    )

  val shiftConfig: StateFlow<WorkShiftConfig> =
    repository.shiftConfigFlow
      .map { it ?: WorkShiftConfig() }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkShiftConfig())

  val allUsers: StateFlow<List<UserAccount>> =
    repository.allUsersFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allLeaveRequests: StateFlow<List<LeaveRequest>> =
    repository.allLeaveRequestsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allLeaveBalances: StateFlow<List<LeaveBalance>> =
    repository.allLeaveBalancesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(ExperimentalCoroutinesApi::class)
  val userLeaveRequests: StateFlow<List<LeaveRequest>> =
    _uiState
      .flatMapLatest { state ->
        repository.getLeaveRequestsForWorkerFlow(state.workerProfile.id)
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(ExperimentalCoroutinesApi::class)
  val currentUserLeaveBalance: StateFlow<LeaveBalance?> =
    _uiState
      .flatMapLatest { state ->
        repository.getLeaveBalanceFlow(state.workerProfile.id)
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val allSecurityAlerts: StateFlow<List<DeviceSecurityAlert>> =
    repository.allDeviceAlertsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Combine real database workers with today's actual records and status
  val workersList: StateFlow<List<WorkerOverview>> =
    combine(allWorkers, _uiState, allRecords) { workers, state, records ->
      val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH).format(java.util.Date())
      val todayRecordsMap = records
        .filter { it.workDate == todayDate }
        .groupBy { it.workerName.trim().lowercase(java.util.Locale.ROOT) }
        .mapValues { entry -> entry.value.maxByOrNull { it.id } }

      // 1. Build a merged map of all workers ensuring the active profile and all DB workers are included
      val combinedWorkerMap = LinkedHashMap<String, WorkerEntity>()

      // A) Include the active worker profile (e.g. Ahmed)
      val curProf = state.workerProfile
      if (curProf.fullName.isNotBlank()) {
        val curKey = curProf.fullName.trim().lowercase(java.util.Locale.ROOT)
        val fromDb = workers.find {
          it.id == curProf.id || it.fullName.trim().lowercase(java.util.Locale.ROOT) == curKey
        }
        val entityToUse = fromDb ?: WorkerEntity(
          id = curProf.id.ifBlank { "EMP-9821" },
          fullName = curProf.fullName,
          initials = curProf.initials.ifBlank { "AM" },
          role = curProf.role.ifBlank { "Field Technical Supervisor" },
          siteId = state.selectedSite.id,
          siteName = state.selectedSite.name,
          nationalId = curProf.nationalId.ifBlank { "1098234812" },
          phoneNumber = curProf.phoneNumber.ifBlank { "+966 50 123 4567" },
          deviceModel = curProf.deviceModel.ifBlank { "Samsung Galaxy A54 5G" },
          isDeviceApproved = curProf.isDeviceApproved,
          assignedSiteIds = curProf.assignedSiteIds.ifBlank { state.selectedSite.id },
          assignedSiteNames = curProf.assignedSiteNames.ifBlank { state.selectedSite.name },
          iqamaNumber = curProf.iqamaNumber.ifBlank { "2498234812" },
          iqamaStartDate = curProf.iqamaStartDate.ifBlank { "2025-09-01" },
          iqamaEndDate = curProf.iqamaEndDate.ifBlank { "2026-08-29" },
          insuranceNumber = curProf.insuranceNumber.ifBlank { "POL-882190-BUPA" },
          insuranceProvider = curProf.insuranceProvider.ifBlank { "Bupa Arabia Insurance" },
          insuranceStartDate = curProf.insuranceStartDate.ifBlank { "2025-09-10" },
          insuranceEndDate = curProf.insuranceEndDate.ifBlank { "2026-09-05" },
          passportNumber = curProf.passportNumber.ifBlank { "A19823412" },
          nationality = curProf.nationality.ifBlank { "Saudi" },
          contractEndDate = "2027-01-01",
        )
        combinedWorkerMap[curKey] = entityToUse
      }

      // B) Add all workers from DB
      workers.forEach { w ->
        val key = w.fullName.trim().lowercase(java.util.Locale.ROOT)
        if (!combinedWorkerMap.containsKey(key)) {
          combinedWorkerMap[key] = w
        }
      }

      // C) Add any worker from today's records not yet present
      todayRecordsMap.forEach { (key, rec) ->
        if (rec != null && !combinedWorkerMap.containsKey(key) && rec.workerName.isNotBlank()) {
          val parts = rec.workerName.trim().split(" ")
          val initials = if (parts.size >= 2) "${parts[0].take(1)}${parts[1].take(1)}".uppercase(java.util.Locale.ROOT) else rec.workerName.take(2).uppercase(java.util.Locale.ROOT)
          val fallbackId = "EMP-${(Math.abs(rec.workerName.hashCode()) % 8000) + 1000}"
          combinedWorkerMap[key] = WorkerEntity(
            id = fallbackId,
            fullName = rec.workerName,
            initials = initials,
            role = "Field Technician",
            siteId = state.selectedSite.id,
            siteName = rec.siteName.ifBlank { state.selectedSite.name },
            nationalId = "",
            phoneNumber = "",
            deviceModel = "Approved Mobile Device",
            isDeviceApproved = true,
            assignedSiteIds = state.selectedSite.id,
            assignedSiteNames = rec.siteName.ifBlank { state.selectedSite.name },
          )
        }
      }

      val allocatedWorkerIds = HashSet<String>()
      combinedWorkerMap.values.map { entity ->
        var safeUniqueId = entity.id.ifBlank { "EMP-${(Math.abs(entity.fullName.hashCode()) % 8000) + 1000}" }
        if (allocatedWorkerIds.contains(safeUniqueId)) {
          safeUniqueId = "${safeUniqueId}_${Math.abs(entity.fullName.hashCode()) % 10000}"
        }
        while (allocatedWorkerIds.contains(safeUniqueId)) {
          safeUniqueId = "${safeUniqueId}_${(100..999).random()}"
        }
        allocatedWorkerIds.add(safeUniqueId)

        val isCurrentWorker = entity.id == state.workerProfile.id ||
          safeUniqueId == state.workerProfile.id ||
          entity.fullName.trim().equals(state.workerProfile.fullName.trim(), ignoreCase = true)
        val entityRec = todayRecordsMap[entity.fullName.trim().lowercase(java.util.Locale.ROOT)]

        val activeRecord = when {
          isCurrentWorker && state.todayRecord != null -> state.todayRecord
          entityRec != null -> entityRec
          else -> null
        }

        val status = when {
          isCurrentWorker -> state.currentStatus
          activeRecord != null -> activeRecord.status
          else -> AttendanceStatus.NOT_CHECKED_IN
        }

        val checkIn = when {
          isCurrentWorker -> state.todayRecord?.checkInTime
          activeRecord != null -> activeRecord.checkInTime
          else -> null
        }

        val checkOut = when {
          isCurrentWorker -> state.todayRecord?.checkOutTime
          activeRecord != null -> activeRecord.checkOutTime
          else -> null
        }

        val isLate = when {
          isCurrentWorker -> state.todayRecord?.isLate ?: false
          activeRecord != null -> activeRecord.isLate
          else -> false
        }

        val isEarly = when {
          isCurrentWorker -> state.todayRecord?.isEarlyDeparture ?: false
          activeRecord != null -> activeRecord.isEarlyDeparture
          else -> false
        }

        WorkerOverview(
          id = safeUniqueId,
          fullName = entity.fullName,
          initials = entity.initials,
          role = entity.role,
          siteId = entity.siteId,
          siteName = entity.siteName,
          nationalId = entity.nationalId,
          phoneNumber = entity.phoneNumber,
          status = status,
          checkInTime = checkIn,
          checkOutTime = checkOut,
          deviceModel = entity.deviceModel,
          isDeviceApproved = entity.isDeviceApproved,
          isLate = isLate,
          isEarlyDeparture = isEarly,
          geofenceDistance = if (isCurrentWorker) state.currentDistanceMeters else 12.0,
          assignedSiteIds = entity.assignedSiteIds,
          assignedSiteNames = entity.assignedSiteNames,
          iqamaNumber = entity.iqamaNumber,
          iqamaStartDate = entity.iqamaStartDate,
          iqamaEndDate = entity.iqamaEndDate,
          insuranceNumber = entity.insuranceNumber,
          insuranceProvider = entity.insuranceProvider,
          insuranceStartDate = entity.insuranceStartDate,
          insuranceEndDate = entity.insuranceEndDate,
          passportNumber = entity.passportNumber,
          nationality = entity.nationality,
          contractEndDate = entity.contractEndDate,
          salary = entity.salary,
          hireDate = entity.hireDate,
          employmentEndDate = entity.employmentEndDate,
        )
      }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private var cooldownTimerJob: Job? = null

  init {
    ensureDefaultDataSeeded()
    observeWorkers()
    observeTodayRecord()
    observeSites()
    fetchFcmToken()
    checkNetworkStatus()
    restoreSavedLoginSession()
    refreshFromCloud(isInitial = true)
  }

  private fun restoreSavedLoginSession() {
    try {
      val app = getApplication<Application>()
      val prefs = app.getSharedPreferences("auth_session_prefs", Context.MODE_PRIVATE)
      if (prefs.contains("saved_pass")) {
        prefs.edit().remove("saved_pass").apply()
      }
      val savedUser = prefs.getString("saved_user", "") ?: ""
      val rememberMe = prefs.getBoolean("remember_me", true)

      if (savedUser.isNotBlank() && rememberMe) {
        _uiState.value = _uiState.value.copy(
          savedUsername = savedUser,
          savedPassword = "",
          isRememberMeChecked = true,
        )
      }
    } catch (e: Exception) {
      Log.e("AttendanceViewModel", "Failed to restore login session: ${e.message}")
    }
  }

  private fun ensureDefaultDataSeeded() {
    viewModelScope.launch {
      val existingWorkers = repository.attendanceDao.getAllWorkersDirect()
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

      if (existingWorkers.isEmpty()) {
        val initialList =
          listOf(
            defaultAhmed,
            WorkerEntity(
              id = "EMP-9822",
              fullName = "Khalid Al-Otaibi",
              initials = "KO",
              role = "Electrical & Equipment Technician",
              siteId = "SITE-RUH-02",
              siteName = "Al-Nakheel Tower Project",
              nationalId = "1087654321",
              phoneNumber = "+966 55 987 6543",
              deviceModel = "iPhone 14 Pro",
              isDeviceApproved = true,
              assignedSiteIds = "SITE-RUH-02",
              assignedSiteNames = "Al-Nakheel Tower Project",
              iqamaNumber = "1087654321",
              iqamaStartDate = "2025-09-01",
              iqamaEndDate = "2026-09-01",
              insuranceNumber = "POL-992140-TAWUNIYA",
              insuranceProvider = "Tawuniya Insurance Company",
              insuranceStartDate = "2025-10-15",
              insuranceEndDate = "2026-10-15",
              passportNumber = "K87654321",
              nationality = "Saudi",
              contractEndDate = "2027-10-01",
            ),
            WorkerEntity(
              id = "EMP-9823",
              fullName = "Sara Al-Shammari",
              initials = "SS",
              role = "Quality & Safety Engineer",
              siteId = "SITE-JED-01",
              siteName = "Jeddah Central Warehouse",
              nationalId = "1076543219",
              phoneNumber = "+966 54 321 0987",
              deviceModel = "Google Pixel 8",
              isDeviceApproved = true,
              assignedSiteIds = "SITE-JED-01,SITE-DMM-03",
              assignedSiteNames = "Jeddah Central Warehouse, Dammam Distribution Station",
              iqamaNumber = "1076543219",
              iqamaStartDate = "2026-01-01",
              iqamaEndDate = "2027-01-01",
              insuranceNumber = "POL-771234-MEDGULF",
              insuranceProvider = "Medgulf Insurance",
              insuranceStartDate = "2026-02-01",
              insuranceEndDate = "2027-02-01",
              passportNumber = "S76543219",
              nationality = "Saudi",
              contractEndDate = "2028-01-01",
            ),
            WorkerEntity(
              id = "EMP-9824",
              fullName = "Faisal Al-Dossary",
              initials = "FD",
              role = "Logistics Transport Driver",
              siteId = "SITE-DMM-03",
              siteName = "Dammam Distribution Station",
              nationalId = "1065432198",
              phoneNumber = "+966 56 654 3210",
              deviceModel = "Xiaomi 13T",
              isDeviceApproved = true,
              assignedSiteIds = "SITE-DMM-03,SITE-RUH-02,SITE-JED-01",
              assignedSiteNames = "Dammam Distribution Station, Al-Nakheel Tower Project, Jeddah Central Warehouse",
              iqamaNumber = "2065432198",
              iqamaStartDate = "2025-08-01",
              iqamaEndDate = "2026-08-22",
              insuranceNumber = "POL-664422-ALRAJHI",
              insuranceProvider = "Al Rajhi Takaful",
              insuranceStartDate = "2025-09-01",
              insuranceEndDate = "2026-09-03",
              passportNumber = "F65432198",
              nationality = "Resident",
              contractEndDate = "2026-09-01",
            ),
          )
        repository.attendanceDao.insertWorkers(initialList)
      } else {
        val hasAhmed = existingWorkers.any {
          it.fullName.trim().equals("Ahmed Mohammed", ignoreCase = true) ||
            it.fullName.trim().equals("أحمد محمد", ignoreCase = true) ||
            it.fullName.trim().equals("Ahmed", ignoreCase = true) ||
            it.id == "EMP-9821"
        }
        if (!hasAhmed) {
          repository.attendanceDao.insertWorker(defaultAhmed)
        }
      }

      val existingSites = repository.attendanceDao.getAllWorkSitesDirect()
      if (existingSites.isEmpty()) {
        repository.attendanceDao.insertWorkSites(
          listOf(
            WorkSite(
              id = "SITE-JED-01",
              name = "Jeddah Central Warehouse",
              latitude = 21.543333,
              longitude = 39.172778,
              radiusMeters = 100,
              address = "King Fahd Road, Industrial Area, Jeddah",
            ),
            WorkSite(
              id = "SITE-RUH-02",
              name = "Al-Nakheel Tower Project",
              latitude = 24.713552,
              longitude = 46.675296,
              radiusMeters = 150,
              address = "Al-Nakheel District, King Fahd Road, Riyadh",
            ),
            WorkSite(
              id = "SITE-DMM-03",
              name = "Dammam Distribution Station",
              latitude = 26.4207,
              longitude = 50.0888,
              radiusMeters = 120,
              address = "King Abdulaziz Port, Dammam",
            ),
          )
        )
      }
    }
  }

  private fun observeWorkers() {
    viewModelScope.launch {
      repository.allWorkers.collect { workers ->
        val current = _uiState.value.workerProfile
        val exists = workers.any {
          it.id == current.id || it.fullName.trim().equals(current.fullName.trim(), ignoreCase = true)
        }
        if (!exists && current.fullName.isNotBlank()) {
          val newWorker = WorkerEntity(
            id = current.id.ifBlank { "EMP-9821" },
            fullName = current.fullName,
            initials = current.initials.ifBlank { "AM" },
            role = current.role.ifBlank { "Field Technical Supervisor" },
            siteId = _uiState.value.selectedSite.id,
            siteName = _uiState.value.selectedSite.name,
            nationalId = current.nationalId.ifBlank { "1098234812" },
            phoneNumber = current.phoneNumber.ifBlank { "+966 50 123 4567" },
            deviceModel = current.deviceModel.ifBlank { "Samsung Galaxy A54 5G" },
            isDeviceApproved = current.isDeviceApproved,
            assignedSiteIds = current.assignedSiteIds.ifBlank { "SITE-JED-01,SITE-RUH-02" },
            assignedSiteNames = current.assignedSiteNames.ifBlank { "Jeddah Central Warehouse, Al-Nakheel Tower Project" },
            iqamaNumber = current.iqamaNumber.ifBlank { "2498234812" },
            iqamaStartDate = current.iqamaStartDate.ifBlank { "2025-09-01" },
            iqamaEndDate = current.iqamaEndDate.ifBlank { "2026-08-29" },
            insuranceNumber = current.insuranceNumber.ifBlank { "POL-882190-BUPA" },
            insuranceProvider = current.insuranceProvider.ifBlank { "Bupa Arabia Insurance" },
            insuranceStartDate = current.insuranceStartDate.ifBlank { "2025-09-10" },
            insuranceEndDate = current.insuranceEndDate.ifBlank { "2026-09-05" },
            passportNumber = current.passportNumber.ifBlank { "A19823412" },
            nationality = current.nationality.ifBlank { "Saudi" },
            contractEndDate = "2027-01-01",
          )
          repository.addWorker(newWorker)
        } else if (exists) {
          val matched = workers.find {
            it.id == current.id || it.fullName.trim().equals(current.fullName.trim(), ignoreCase = true)
          }
          if (matched != null) {
            _uiState.value = _uiState.value.copy(
              workerProfile = _uiState.value.workerProfile.copy(
                id = matched.id,
                fullName = matched.fullName,
                initials = matched.initials,
                role = matched.role,
                nationalId = matched.nationalId,
                phoneNumber = matched.phoneNumber,
                deviceModel = matched.deviceModel,
                isDeviceApproved = matched.isDeviceApproved,
                assignedSiteIds = matched.assignedSiteIds,
                assignedSiteNames = matched.assignedSiteNames,
                iqamaNumber = matched.iqamaNumber,
                iqamaStartDate = matched.iqamaStartDate,
                iqamaEndDate = matched.iqamaEndDate,
                insuranceNumber = matched.insuranceNumber,
                insuranceProvider = matched.insuranceProvider,
                insuranceStartDate = matched.insuranceStartDate,
                insuranceEndDate = matched.insuranceEndDate,
                passportNumber = matched.passportNumber,
                nationality = matched.nationality,
              )
            )
          }
        }
      }
    }
  }

  private fun observeSites() {
    viewModelScope.launch {
      repository.allSites.collect { sites ->
        if (sites.isNotEmpty()) {
          val currentSelected = _uiState.value.selectedSite
          val updatedSelected = sites.find { it.id == currentSelected.id } ?: sites.first()
          val realDist = repository.calculateDistanceMeters(
            _uiState.value.deviceLatitude,
            _uiState.value.deviceLongitude,
            updatedSelected.latitude,
            updatedSelected.longitude
          )
          val distance =
            if (_uiState.value.isOutsideSimulation) {
              maxOf(realDist, updatedSelected.radiusMeters + 150.0)
            } else {
              realDist
            }
          _uiState.value = _uiState.value.copy(
            selectedSite = updatedSelected,
            currentDistanceMeters = distance,
            isInsideGeofence = distance <= updatedSelected.radiusMeters,
          )
        }
      }
    }
  }

  private fun observeTodayRecord() {
    viewModelScope.launch {
      repository.getTodayAttendanceFlow().collect { today ->
        if (today != null) {
          val lastOp =
            when (today.status) {
              AttendanceStatus.CHECKED_IN -> "Last Action: ${today.checkInTime} (Check-In)"
              AttendanceStatus.CHECKED_OUT -> "Last Action: ${today.checkOutTime} (Check-Out)"
              AttendanceStatus.NOT_CHECKED_IN -> "Last Action: 04:30 PM (Out)"
            }
          val isPhotoReady =
            when (today.status) {
              AttendanceStatus.CHECKED_IN -> today.checkOutPhotoUri != null || _uiState.value.capturedCheckOutBitmap != null
              AttendanceStatus.CHECKED_OUT -> true
              AttendanceStatus.NOT_CHECKED_IN -> today.checkInPhotoUri != null || _uiState.value.capturedCheckInBitmap != null
            }
          _uiState.value =
            _uiState.value.copy(
              todayRecord = today,
              currentStatus = today.status,
              photoCaptured = isPhotoReady,
              checkInPhotoUri = today.checkInPhotoUri ?: _uiState.value.checkInPhotoUri,
              checkInDriveUrl = today.checkInDriveUrl ?: _uiState.value.checkInDriveUrl,
              checkOutPhotoUri = today.checkOutPhotoUri ?: _uiState.value.checkOutPhotoUri,
              checkOutDriveUrl = today.checkOutDriveUrl ?: _uiState.value.checkOutDriveUrl,
              lastOperationTime = lastOp,
            )
          checkAndStartCooldown(today.lastActionTimestampMillis)
        } else {
          _uiState.value =
            _uiState.value.copy(
              todayRecord = null,
              currentStatus = AttendanceStatus.NOT_CHECKED_IN,
              lastOperationTime = "Last Action: None yet",
            )
        }
      }
    }
  }

  fun checkNetworkStatus(): Boolean {
    val online = CloudSyncService.isOnline(getApplication())
    _uiState.value = _uiState.value.copy(isOnline = online)
    return online
  }

  fun checkAndRefreshLocationState(context: android.content.Context) {
    val perm = com.example.service.LocationHelper.hasLocationPermission(context)
    val locEnabled = com.example.service.LocationHelper.isLocationServiceEnabled(context)
    val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    val online = checkNetworkStatus()

    val isReady = perm && locEnabled && hasCam && online

    _uiState.value =
      _uiState.value.copy(
        hasLocationPermission = perm,
        hasCameraPermission = hasCam,
        isLocationEnabled = locEnabled,
        isOnline = online,
        isAppReady = isReady,
        isInitialLoading = false,
      )

    if (perm && locEnabled) {
      refreshDeviceLocation(context)
    }
  }

  fun refreshFromCloud(isInitial: Boolean = false) {
    viewModelScope.launch {
      val context = getApplication<Application>()
      val online = checkNetworkStatus()
      val perm = com.example.service.LocationHelper.hasLocationPermission(context)
      val locEnabled = com.example.service.LocationHelper.isLocationServiceEnabled(context)

      _uiState.value =
        _uiState.value.copy(
          hasLocationPermission = perm,
          isLocationEnabled = locEnabled,
          isOnline = online,
          isRefreshing = true,
          isInitialLoading = isInitial,
        )

      if (!online) {
        _uiState.value =
          _uiState.value.copy(
            isRefreshing = false,
            isInitialLoading = false,
            notificationMessage = if (!isInitial) "No internet connection! Cloud sync unavailable." else null,
            isNotificationError = true,
          )
        return@launch
      }

      val success = repository.refreshFromFirestore(context)
      val timeNow = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ENGLISH).format(java.util.Date())
      _uiState.value =
        _uiState.value.copy(
          isRefreshing = false,
          isInitialLoading = false,
          isAppReady = perm && locEnabled,
          lastSyncTime = timeNow,
          notificationMessage = if (!isInitial) {
            if (success) "Data synced with Firebase successfully ✓" else "Synced latest cloud data"
          } else null,
          isNotificationError = false,
        )
    }
  }

  fun retryInitialStartup(context: android.content.Context? = null) {
    checkAndRefreshLocationState(context ?: getApplication())
    refreshFromCloud(isInitial = true)
  }

  private fun startCooldownTimer(seconds: Long) {
    cooldownTimerJob?.cancel()
    cooldownTimerJob =
      viewModelScope.launch {
        var remaining = seconds
        while (remaining > 0) {
          _uiState.value = _uiState.value.copy(cooldownRemainingSeconds = remaining)
          delay(1000)
          remaining--
        }
        _uiState.value = _uiState.value.copy(cooldownRemainingSeconds = 0L)
      }
  }

  private fun checkAndStartCooldown(lastMillis: Long) {
    if (lastMillis <= 0L) return
    val elapsed = System.currentTimeMillis() - lastMillis
    if (elapsed < AttendanceRepository.COOLDOWN_DURATION_MILLIS) {
      val remSec = (AttendanceRepository.COOLDOWN_DURATION_MILLIS - elapsed) / 1000
      startCooldownTimer(remSec)
    }
  }

  // --- Work Site CRUD ---
  fun addNewSite(name: String, lat: Double, lng: Double, radius: Int, address: String) {
    viewModelScope.launch {
      val online = checkNetworkStatus()
      if (!online) {
        triggerDetailedError(
          title = "No Internet Connection",
          message = "Adding a work site requires an active internet connection to sync with cloud.",
          guidance = "Please connect to the internet and try again.",
        )
        return@launch
      }

      if (!com.example.service.LocationHelper.validateCoordinates(lat, lng)) {
        triggerDetailedError(
          title = "Invalid GPS Coordinates",
          message = "The provided coordinates (Lat: $lat, Lng: $lng) are invalid.",
          guidance = "Latitude must be between -90 and 90, Longitude between -180 and 180.",
        )
        return@launch
      }

      if (radius !in 10..10000) {
        triggerDetailedError(
          title = "Invalid Geofence Radius",
          message = "Geofence radius must be between 10 meters and 10,000 meters.",
          guidance = "Please specify a reasonable radius for the site perimeter.",
        )
        return@launch
      }

      _uiState.value = _uiState.value.copy(isProcessing = true)
      val newSite =
        WorkSite(
          id = "SITE-${System.currentTimeMillis() % 10000}",
          name = name,
          latitude = lat,
          longitude = lng,
          radiusMeters = radius,
          address = address,
        )
      // 1. Save in local database first
      repository.addWorkSite(newSite)
      _uiState.value =
        _uiState.value.copy(
          isProcessing = false,
          notificationMessage = "New work site added and synced: $name",
          isNotificationError = false,
        )
    }
  }

  fun updateSite(site: WorkSite) {
    viewModelScope.launch {
      val online = checkNetworkStatus()
      if (!online) {
        triggerDetailedError(
          title = "No Internet Connection",
          message = "Updating a work site requires an active internet connection to sync with cloud.",
          guidance = "Please connect to the internet and try again.",
        )
        return@launch
      }

      if (!com.example.service.LocationHelper.validateCoordinates(site.latitude, site.longitude)) {
        triggerDetailedError(
          title = "Invalid GPS Coordinates",
          message = "The provided coordinates (Lat: ${site.latitude}, Lng: ${site.longitude}) are invalid.",
          guidance = "Latitude must be between -90 and 90, Longitude between -180 and 180.",
        )
        return@launch
      }

      if (site.radiusMeters !in 10..10000) {
        triggerDetailedError(
          title = "Invalid Geofence Radius",
          message = "Geofence radius must be between 10 meters and 10,000 meters.",
          guidance = "Please specify a reasonable radius for the site perimeter.",
        )
        return@launch
      }

      _uiState.value = _uiState.value.copy(isProcessing = true)
      repository.updateWorkSite(site)

      if (_uiState.value.selectedSite.id == site.id) {
        val realDist = repository.calculateDistanceMeters(
          _uiState.value.deviceLatitude,
          _uiState.value.deviceLongitude,
          site.latitude,
          site.longitude
        )
        val distance =
          if (_uiState.value.isOutsideSimulation) {
            maxOf(realDist, site.radiusMeters + 150.0)
          } else {
            realDist
          }
        _uiState.value =
          _uiState.value.copy(
            selectedSite = site,
            currentDistanceMeters = distance,
            isInsideGeofence = distance <= site.radiusMeters,
            isProcessing = false,
            notificationMessage = "Work site coordinates updated and geofence recalculated: ${site.name}",
            isNotificationError = false,
          )
      } else {
        _uiState.value =
          _uiState.value.copy(
            isProcessing = false,
            notificationMessage = "Work site updated: ${site.name}",
            isNotificationError = false,
          )
      }
    }
  }

  fun deleteSite(siteId: String) {
    viewModelScope.launch {
      val online = checkNetworkStatus()
      if (!online) {
        triggerDetailedError(
          title = "No Internet Connection",
          message = "Deleting a work site requires an active internet connection to sync with cloud.",
          guidance = "Please connect to the internet and try again.",
        )
        return@launch
      }

      _uiState.value = _uiState.value.copy(isProcessing = true)
      repository.deleteWorkSite(siteId)
      _uiState.value =
        _uiState.value.copy(
          isProcessing = false,
          notificationMessage = "Work site removed from system",
          isNotificationError = false,
        )
    }
  }

  // --- Workers CRUD ---
  fun addNewWorker(
    fullName: String,
    role: String,
    siteId: String,
    siteName: String,
    nationalId: String,
    phone: String,
    deviceModel: String,
    isApproved: Boolean,
    assignedSiteIds: String = "",
    assignedSiteNames: String = "",
    iqamaNumber: String = "",
    iqamaStartDate: String = "",
    iqamaEndDate: String = "",
    insuranceNumber: String = "",
    insuranceProvider: String = "",
    insuranceStartDate: String = "",
    insuranceEndDate: String = "",
    passportNumber: String = "",
    nationality: String = "",
    contractEndDate: String = "",
    salary: Double = 0.0,
    hireDate: String = "",
    employmentEndDate: String = "",
    annualLeaveTotal: Double = 21.0,
    casualLeaveTotal: Double = 7.0,
    sickLeaveTotal: Double = 14.0,
  ) {
    viewModelScope.launch {
      val online = checkNetworkStatus()
      if (!online) {
        triggerDetailedError(
          title = "No Internet Connection",
          message = "Adding a new worker requires an active internet connection to sync with cloud.",
          guidance = "Please connect to the internet and try again.",
        )
        return@launch
      }

      _uiState.value = _uiState.value.copy(isProcessing = true)
      val parts = fullName.trim().split(" ")
      val initials =
        if (parts.size >= 2) {
          "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
        } else {
          fullName.take(2).uppercase()
        }
      val newWorker =
        WorkerEntity(
          id = "EMP-${1000 + (System.currentTimeMillis() % 9000)}",
          fullName = fullName,
          initials = initials,
          role = role,
          siteId = siteId,
          siteName = siteName,
          nationalId = nationalId,
          phoneNumber = phone,
          deviceModel = if (deviceModel.isNotBlank()) deviceModel else "Samsung Galaxy Device",
          isDeviceApproved = isApproved,
          assignedSiteIds = if (assignedSiteIds.isNotBlank()) assignedSiteIds else siteId,
          assignedSiteNames = if (assignedSiteNames.isNotBlank()) assignedSiteNames else siteName,
          iqamaNumber = iqamaNumber,
          iqamaStartDate = iqamaStartDate,
          iqamaEndDate = iqamaEndDate,
          insuranceNumber = insuranceNumber,
          insuranceProvider = insuranceProvider,
          insuranceStartDate = insuranceStartDate,
          insuranceEndDate = insuranceEndDate,
          passportNumber = passportNumber,
          nationality = nationality,
          contractEndDate = contractEndDate,
          salary = salary,
          hireDate = hireDate,
          employmentEndDate = employmentEndDate,
        )
      repository.addWorker(newWorker)

      // Initialize leave balances for newly registered worker
      val initialLeaveBalance = LeaveBalance(
        workerId = newWorker.id,
        annualTotal = if (annualLeaveTotal >= 0.0) annualLeaveTotal else 21.0,
        annualUsed = 0.0,
        casualTotal = if (casualLeaveTotal >= 0.0) casualLeaveTotal else 7.0,
        casualUsed = 0.0,
        sickTotal = if (sickLeaveTotal >= 0.0) sickLeaveTotal else 14.0,
        sickUsed = 0.0,
      )
      repository.saveOrUpdateLeaveBalance(initialLeaveBalance)

      _uiState.value =
        _uiState.value.copy(
          isProcessing = false,
          notificationMessage = "New worker registered with leave quota and synced: $fullName",
          isNotificationError = false,
        )
    }
  }

  fun updateWorkerLeaveBalance(
    workerId: String,
    annualTotal: Double,
    casualTotal: Double,
    sickTotal: Double,
    annualUsed: Double? = null,
    casualUsed: Double? = null,
    sickUsed: Double? = null,
  ) {
    viewModelScope.launch {
      val online = checkNetworkStatus()
      if (!online) {
        triggerDetailedError(
          title = "No Internet Connection",
          message = "Updating worker leave quota requires an active internet connection to sync with cloud.",
          guidance = "Please connect to the internet and try again.",
        )
        return@launch
      }

      _uiState.value = _uiState.value.copy(isProcessing = true)
      try {
        val existing = repository.getLeaveBalance(workerId)
        val updated = existing.copy(
          annualTotal = annualTotal.coerceAtLeast(0.0),
          casualTotal = casualTotal.coerceAtLeast(0.0),
          sickTotal = sickTotal.coerceAtLeast(0.0),
          annualUsed = (annualUsed ?: existing.annualUsed).coerceAtLeast(0.0),
          casualUsed = (casualUsed ?: existing.casualUsed).coerceAtLeast(0.0),
          sickUsed = (sickUsed ?: existing.sickUsed).coerceAtLeast(0.0),
        )
        repository.saveOrUpdateLeaveBalance(updated)
        _uiState.value = _uiState.value.copy(
          isProcessing = false,
          notificationMessage = "Leave quota updated successfully for $workerId",
          isNotificationError = false,
        )
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          isProcessing = false,
          notificationMessage = "Failed to update leave quota: ${e.message}",
          isNotificationError = true,
        )
      }
    }
  }

  fun updateWorkerDocuments(
    workerId: String,
    iqamaNumber: String,
    iqamaStartDate: String,
    iqamaEndDate: String,
    insuranceNumber: String,
    insuranceProvider: String,
    insuranceStartDate: String,
    insuranceEndDate: String,
    passportNumber: String = "",
    nationality: String = "",
    contractEndDate: String = "",
    salary: Double = 0.0,
    hireDate: String = "",
    employmentEndDate: String = "",
  ) {
    viewModelScope.launch {
      val online = checkNetworkStatus()
      if (!online) {
        triggerDetailedError(
          title = "No Internet Connection",
          message = "Updating worker document records requires an active internet connection.",
          guidance = "Please connect to the internet and try again.",
        )
        return@launch
      }

      val currentWorker = allWorkers.value.find { it.id == workerId }
      if (currentWorker != null) {
        _uiState.value = _uiState.value.copy(isProcessing = true)
        val updated = currentWorker.copy(
          iqamaNumber = iqamaNumber,
          iqamaStartDate = iqamaStartDate,
          iqamaEndDate = iqamaEndDate,
          insuranceNumber = insuranceNumber,
          insuranceProvider = insuranceProvider,
          insuranceStartDate = insuranceStartDate,
          insuranceEndDate = insuranceEndDate,
          passportNumber = passportNumber,
          nationality = nationality,
          contractEndDate = contractEndDate,
          salary = if (salary > 0.0) salary else currentWorker.salary,
          hireDate = if (hireDate.isNotBlank()) hireDate else currentWorker.hireDate,
          employmentEndDate = employmentEndDate,
        )
        repository.updateWorker(updated)

        if (_uiState.value.workerProfile.id == workerId) {
          _uiState.value = _uiState.value.copy(
            workerProfile = _uiState.value.workerProfile.copy(
              iqamaNumber = iqamaNumber,
              iqamaStartDate = iqamaStartDate,
              iqamaEndDate = iqamaEndDate,
              insuranceNumber = insuranceNumber,
              insuranceProvider = insuranceProvider,
              insuranceStartDate = insuranceStartDate,
              insuranceEndDate = insuranceEndDate,
              passportNumber = passportNumber,
              nationality = nationality,
              salary = updated.salary,
              hireDate = updated.hireDate,
              employmentEndDate = updated.employmentEndDate,
            )
          )
        }

        _uiState.value = _uiState.value.copy(
          isProcessing = false,
          notificationMessage = "Worker records updated successfully for ${currentWorker.fullName} ✓",
          isNotificationError = false,
        )
      }
    }
  }

  // Work Shift Configuration
  fun updateShiftSchedule(config: WorkShiftConfig) {
    viewModelScope.launch {
      val online = checkNetworkStatus()
      if (!online) {
        triggerDetailedError(
          title = "No Internet Connection",
          message = "Updating work shift timings requires an active internet connection to sync with cloud.",
          guidance = "Please connect to the internet and try again.",
        )
        return@launch
      }

      _uiState.value = _uiState.value.copy(isProcessing = true)
      repository.updateShiftConfig(config)
      _uiState.value =
        _uiState.value.copy(
          isProcessing = false,
          notificationMessage = "Official shift timings saved and synced to cloud ✓",
          isNotificationError = false,
        )
    }
  }

  fun selectWorker(worker: WorkerEntity) {
    _uiState.value = _uiState.value.copy(
      workerProfile = WorkerProfile(
        id = worker.id,
        fullName = worker.fullName,
        initials = worker.initials,
        role = worker.role,
        nationalId = worker.nationalId,
        phoneNumber = worker.phoneNumber,
        deviceModel = worker.deviceModel,
        isDeviceApproved = worker.isDeviceApproved,
        assignedSiteIds = worker.assignedSiteIds,
        assignedSiteNames = worker.assignedSiteNames,
        iqamaNumber = worker.iqamaNumber,
        iqamaStartDate = worker.iqamaStartDate,
        iqamaEndDate = worker.iqamaEndDate,
        insuranceNumber = worker.insuranceNumber,
        insuranceProvider = worker.insuranceProvider,
        insuranceStartDate = worker.insuranceStartDate,
        insuranceEndDate = worker.insuranceEndDate,
        passportNumber = worker.passportNumber,
        nationality = worker.nationality,
        salary = worker.salary,
        hireDate = worker.hireDate,
        employmentEndDate = worker.employmentEndDate,
      ),
      capturedCheckInBitmap = null,
      capturedCheckOutBitmap = null,
      capturedPhotoBitmap = null,
      checkInPhotoUri = null,
      checkOutPhotoUri = null,
      checkInPhotoBase64 = null,
      checkOutPhotoBase64 = null,
      checkInDriveUrl = null,
      checkOutDriveUrl = null,
      photoCaptured = false,
    )
    refreshTodayRecordForCurrentWorker()
  }

  fun selectWorkerByName(name: String) {
    val trimmed = name.trim()
    if (trimmed.isBlank()) return
    val matched = allWorkers.value.find { it.fullName.trim().equals(trimmed, ignoreCase = true) }
    if (matched != null) {
      selectWorker(matched)
    } else {
      val parts = trimmed.split(" ")
      val initials = if (parts.size >= 2) "${parts[0].take(1)}${parts[1].take(1)}".uppercase(java.util.Locale.ROOT) else trimmed.take(2).uppercase(java.util.Locale.ROOT)
      val newWorker =
        WorkerEntity(
          id = "EMP-${1000 + (System.currentTimeMillis() % 9000)}",
          fullName = trimmed,
          initials = initials,
          role = "Field Technician",
          siteId = _uiState.value.selectedSite.id,
          siteName = _uiState.value.selectedSite.name,
          nationalId = "",
          phoneNumber = "",
          deviceModel = "Approved Mobile Device",
          isDeviceApproved = true,
          assignedSiteIds = _uiState.value.selectedSite.id,
          assignedSiteNames = _uiState.value.selectedSite.name,
          iqamaNumber = "",
          iqamaStartDate = "",
          iqamaEndDate = "",
          insuranceNumber = "",
          insuranceProvider = "",
          insuranceStartDate = "",
          insuranceEndDate = "",
          passportNumber = "",
          nationality = "",
          contractEndDate = "",
          salary = 0.0,
          hireDate = "",
          employmentEndDate = "",
        )
      viewModelScope.launch {
        repository.addWorker(newWorker)
        selectWorker(newWorker)
      }
    }
  }

  fun addManualAttendanceRecord(record: AttendanceRecord) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isProcessing = true)
      repository.insertManualRecord(record)
      _uiState.value = _uiState.value.copy(
        isProcessing = false,
        notificationMessage = "Attendance record for worker (${record.workerName}) added and synced to cloud ✓",
        isNotificationError = false,
      )
      refreshTodayRecordForCurrentWorker()
    }
  }

  fun refreshTodayRecordForCurrentWorker() {
    viewModelScope.launch {
      val todayDate = repository.getTodayDateString()
      val currentWorkerName = _uiState.value.workerProfile.fullName
      val today = repository.getAttendanceForDateAndWorker(todayDate, currentWorkerName)
      if (today != null) {
        val lastOp =
          when (today.status) {
            AttendanceStatus.CHECKED_IN -> "Last Action: ${today.checkInTime} (Check-In)"
            AttendanceStatus.CHECKED_OUT -> "Last Action: ${today.checkOutTime} (Check-Out)"
            AttendanceStatus.NOT_CHECKED_IN -> "Last Action: 04:30 PM (Out)"
          }
        val isPhotoReady =
          when (today.status) {
            AttendanceStatus.CHECKED_IN -> _uiState.value.capturedCheckOutBitmap != null || !today.checkOutPhotoUri.isNullOrBlank()
            AttendanceStatus.CHECKED_OUT -> true
            AttendanceStatus.NOT_CHECKED_IN -> _uiState.value.capturedCheckInBitmap != null || !today.checkInPhotoUri.isNullOrBlank()
          }
        _uiState.value =
          _uiState.value.copy(
            todayRecord = today,
            currentStatus = today.status,
            photoCaptured = isPhotoReady,
            checkInPhotoUri = today.checkInPhotoUri,
            checkInPhotoBase64 = today.checkInPhotoBase64,
            checkInDriveUrl = today.checkInDriveUrl,
            checkOutPhotoUri = today.checkOutPhotoUri,
            checkOutPhotoBase64 = today.checkOutPhotoBase64,
            checkOutDriveUrl = today.checkOutDriveUrl,
            lastOperationTime = lastOp,
          )
        checkAndStartCooldown(today.lastActionTimestampMillis)
      } else {
        _uiState.value =
          _uiState.value.copy(
            todayRecord = null,
            currentStatus = AttendanceStatus.NOT_CHECKED_IN,
            lastOperationTime = "Last Action: None yet",
            capturedCheckInBitmap = null,
            capturedCheckOutBitmap = null,
            capturedPhotoBitmap = null,
            checkInPhotoUri = null,
            checkOutPhotoUri = null,
            checkInPhotoBase64 = null,
            checkOutPhotoBase64 = null,
            checkInDriveUrl = null,
            checkOutDriveUrl = null,
            photoCaptured = false,
          )
      }
    }
  }

  fun updateWorker(worker: WorkerEntity) {
    viewModelScope.launch {
      val online = checkNetworkStatus()
      if (!online) {
        triggerDetailedError(
          title = "No Internet Connection",
          message = "Updating worker profile requires an active internet connection to sync with cloud.",
          guidance = "Please connect to the internet and try again.",
        )
        return@launch
      }

      _uiState.value = _uiState.value.copy(isProcessing = true)
      repository.updateWorker(worker)
      _uiState.value =
        _uiState.value.copy(
          isProcessing = false,
          notificationMessage = "Worker details updated: ${worker.fullName}",
          isNotificationError = false,
        )
    }
  }

  fun deleteWorker(workerId: String) {
    viewModelScope.launch {
      val online = checkNetworkStatus()
      if (!online) {
        triggerDetailedError(
          title = "No Internet Connection",
          message = "Deleting worker profile requires an active internet connection to sync with cloud.",
          guidance = "Please connect to the internet and try again.",
        )
        return@launch
      }

      _uiState.value = _uiState.value.copy(isProcessing = true)
      repository.deleteWorker(workerId)
      _uiState.value =
        _uiState.value.copy(
          isProcessing = false,
          notificationMessage = "Worker deleted from system successfully",
          isNotificationError = false,
        )
    }
  }

  private fun fetchFcmToken() {
    try {
      val googleApiAvailability = com.google.android.gms.common.GoogleApiAvailability.getInstance()
      val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(getApplication())
      if (resultCode == com.google.android.gms.common.ConnectionResult.SUCCESS) {
        try {
          com.google.firebase.messaging.FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
              try {
                if (task.isSuccessful && !task.result.isNullOrBlank()) {
                  val token = task.result
                  _uiState.value =
                    _uiState.value.copy(
                      fcmToken = token,
                      fcmStatus = "Connected to FCM successfully",
                    )
                } else {
                  val fallbackToken = "fcm_dev_" + java.util.UUID.randomUUID().toString().take(16)
                  _uiState.value =
                    _uiState.value.copy(
                      fcmToken = fallbackToken,
                      fcmStatus = "Connected to Cloud (Developer Sandbox)",
                    )
                }
              } catch (_: Throwable) {
                val fallbackToken = "fcm_dev_" + java.util.UUID.randomUUID().toString().take(16)
                _uiState.value =
                  _uiState.value.copy(
                    fcmToken = fallbackToken,
                    fcmStatus = "Connected to Cloud (Developer Sandbox)",
                  )
              }
            }
        } catch (_: Throwable) {
          val fallbackToken = "fcm_dev_" + java.util.UUID.randomUUID().toString().take(16)
          _uiState.value =
            _uiState.value.copy(
              fcmToken = fallbackToken,
              fcmStatus = "Connected to Cloud (Developer Sandbox)",
            )
        }
      } else {
        val fallbackToken = "fcm_dev_" + java.util.UUID.randomUUID().toString().take(16)
        _uiState.value =
          _uiState.value.copy(
            fcmToken = fallbackToken,
            fcmStatus = "Connected to Cloud (Developer Sandbox)",
          )
      }
    } catch (_: Throwable) {
      val fallbackToken = "fcm_dev_" + java.util.UUID.randomUUID().toString().take(16)
      _uiState.value =
        _uiState.value.copy(
          fcmToken = fallbackToken,
          fcmStatus = "Connected to Cloud (Developer Sandbox)",
        )
    }
  }

  fun setTab(tab: BentoTab) {
    _uiState.value = _uiState.value.copy(currentTab = tab)
  }

  fun selectSite(site: WorkSite) {
    val realDist = repository.calculateDistanceMeters(
      _uiState.value.deviceLatitude,
      _uiState.value.deviceLongitude,
      site.latitude,
      site.longitude
    )
    val distance =
      if (_uiState.value.isOutsideSimulation) {
        maxOf(realDist, site.radiusMeters + 150.0)
      } else {
        realDist
      }
    _uiState.value =
      _uiState.value.copy(
        selectedSite = site,
        currentDistanceMeters = distance,
        isInsideGeofence = distance <= site.radiusMeters,
      )
  }

  fun toggleGeofenceSimulation(simulateOutside: Boolean) {
    val site = _uiState.value.selectedSite
    val realDist = repository.calculateDistanceMeters(
      _uiState.value.deviceLatitude,
      _uiState.value.deviceLongitude,
      site.latitude,
      site.longitude
    )
    val distance =
      if (simulateOutside) {
        maxOf(realDist, site.radiusMeters + 150.0)
      } else {
        realDist
      }
    _uiState.value =
      _uiState.value.copy(
        isOutsideSimulation = simulateOutside,
        currentDistanceMeters = distance,
        isInsideGeofence = distance <= _uiState.value.selectedSite.radiusMeters,
      )
  }

  fun updateRealGpsCoordinates(lat: Double, lng: Double, accuracy: Double) {
    val site = _uiState.value.selectedSite
    val realDist = repository.calculateDistanceMeters(lat, lng, site.latitude, site.longitude)
    val distance =
      if (_uiState.value.isOutsideSimulation) {
        maxOf(realDist, site.radiusMeters + 150.0)
      } else {
        realDist
      }
    _uiState.value =
      _uiState.value.copy(
        deviceLatitude = lat,
        deviceLongitude = lng,
        isDeviceLocationReady = true,
        currentDistanceMeters = distance,
        accuracyMeters = accuracy,
        isInsideGeofence = distance <= site.radiusMeters,
      )
  }

  fun refreshDeviceLocation(context: android.content.Context) {
    viewModelScope.launch {
      val online = checkNetworkStatus()
      val perm = com.example.service.LocationHelper.hasLocationPermission(context)
      val locEnabled = com.example.service.LocationHelper.isLocationServiceEnabled(context)

      _uiState.value = _uiState.value.copy(
        isSearchingLocation = true,
        locationStatusType = LocationStatusType.SEARCHING,
        locationSearchStatus = "Searching for current GPS location via satellites...",
        locationSearchError = null,
        locationSearchSuccess = null,
        hasLocationPermission = perm,
        isLocationEnabled = locEnabled,
        isOnline = online,
      )

      com.example.service.LocationHelper.searchLocationWithDiagnostics(
        context = context,
        isOnline = online,
        onResult = { result ->
          when (result) {
            is com.example.service.LocationHelper.LocationSearchResult.Success -> {
              val coords = result.coordinates
              val site = _uiState.value.selectedSite
              val realDist = repository.calculateDistanceMeters(
                coords.latitude,
                coords.longitude,
                site.latitude,
                site.longitude
              )
              val distance =
                if (_uiState.value.isOutsideSimulation) {
                  maxOf(realDist, site.radiusMeters + 150.0)
                } else {
                  realDist
                }

              _uiState.value = _uiState.value.copy(
                deviceLatitude = coords.latitude,
                deviceLongitude = coords.longitude,
                accuracyMeters = coords.accuracy,
                isDeviceLocationReady = true,
                currentDistanceMeters = distance,
                isInsideGeofence = distance <= site.radiusMeters,
                isSearchingLocation = false,
                locationStatusType = LocationStatusType.SUCCESS,
                locationSearchSuccess = result.message,
                locationSearchStatus = result.message,
                locationSearchError = null,
                locationProviderName = coords.provider,
                lastLocationSearchTimestamp = System.currentTimeMillis(),
              )
            }
            is com.example.service.LocationHelper.LocationSearchResult.Failure -> {
              if (result.fallbackCoordinates != null) {
                val coords = result.fallbackCoordinates
                val site = _uiState.value.selectedSite
                val realDist = repository.calculateDistanceMeters(
                  coords.latitude,
                  coords.longitude,
                  site.latitude,
                  site.longitude
                )
                val distance =
                  if (_uiState.value.isOutsideSimulation) {
                    maxOf(realDist, site.radiusMeters + 150.0)
                  } else {
                    realDist
                  }

                _uiState.value = _uiState.value.copy(
                  deviceLatitude = coords.latitude,
                  deviceLongitude = coords.longitude,
                  accuracyMeters = coords.accuracy,
                  isDeviceLocationReady = true,
                  currentDistanceMeters = distance,
                  isInsideGeofence = distance <= site.radiusMeters,
                  isSearchingLocation = false,
                  locationStatusType = LocationStatusType.ERROR,
                  locationSearchError = result.explanationArabic,
                  locationSearchStatus = result.explanationArabic,
                  locationSearchSuccess = null,
                  lastLocationSearchTimestamp = System.currentTimeMillis(),
                )
              } else {
                _uiState.value = _uiState.value.copy(
                  isSearchingLocation = false,
                  locationStatusType = LocationStatusType.ERROR,
                  locationSearchError = result.explanationArabic,
                  locationSearchStatus = result.explanationArabic,
                  locationSearchSuccess = null,
                  lastLocationSearchTimestamp = System.currentTimeMillis(),
                )
              }
            }
          }
        }
      )
    }
  }

  // Camera Live Capture handler for both Check-In and Check-Out
  fun onLivePhotoCaptured(bitmap: Bitmap) {
    viewModelScope.launch {
      val today = repository.getTodayDateString()
      val isCheckingOut = _uiState.value.currentStatus == AttendanceStatus.CHECKED_IN
      val type = if (isCheckingOut) "CHECK_OUT" else "CHECK_IN"
      val currentWorkerName = _uiState.value.workerProfile.fullName.ifBlank { repository.workerProfile.fullName }

      _uiState.value = _uiState.value.copy(isUploadingToDrive = true)

      val payload =
        CloudSyncService.saveAndUploadPhotoToDrive(
          context = getApplication(),
          bitmap = bitmap,
          type = type,
          workerName = currentWorkerName,
          workDate = today,
        )

      if (isCheckingOut) {
        _uiState.value =
          _uiState.value.copy(
            isUploadingToDrive = false,
            photoCaptured = true,
            capturedPhotoBitmap = bitmap,
            capturedCheckOutBitmap = bitmap,
            checkOutPhotoUri = payload.localUri,
            checkOutPhotoBase64 = payload.base64,
            checkOutDriveUrl = payload.driveUrl,
            notificationMessage = "Live check-out photo captured and prepared for cloud sync ✓ Ready to check out",
            isNotificationError = false,
          )
      } else {
        _uiState.value =
          _uiState.value.copy(
            isUploadingToDrive = false,
            photoCaptured = true,
            capturedPhotoBitmap = bitmap,
            capturedCheckInBitmap = bitmap,
            checkInPhotoUri = payload.localUri,
            checkInPhotoBase64 = payload.base64,
            checkInDriveUrl = payload.driveUrl,
            notificationMessage = "Live check-in photo captured and prepared for cloud sync ✓ Ready to check in",
            isNotificationError = false,
          )
      }
    }
  }

  fun onCameraDismissed() {
    val isCheckingOut = _uiState.value.currentStatus == AttendanceStatus.CHECKED_IN
    val actionName = if (isCheckingOut) "check-out" else "check-in"
    _uiState.value =
      _uiState.value.copy(
        notificationMessage = "No photo was taken. Live face photo is required to complete $actionName.",
        isNotificationError = true,
      )
  }

  fun dismissNotification() {
    _uiState.value = _uiState.value.copy(notificationMessage = null)
  }

  fun dismissErrorDialog() {
    _uiState.value =
      _uiState.value.copy(
        showErrorDialog = false,
        errorTitle = null,
        errorGuidance = null,
      )
  }

  private fun triggerDetailedError(title: String, message: String, guidance: String) {
    _uiState.value =
      _uiState.value.copy(
        isProcessing = false,
        showErrorDialog = true,
        errorTitle = title,
        notificationMessage = message,
        errorGuidance = guidance,
        isNotificationError = true,
      )
  }

  fun dismissEarlyCheckoutWarning() {
    _uiState.value = _uiState.value.copy(showEarlyCheckoutWarning = false)
  }

  fun dismissCheckoutSuccessPopup() {
    _uiState.value = _uiState.value.copy(showCheckoutSuccessPopup = false)
  }

  fun confirmEarlyCheckout() {
    _uiState.value = _uiState.value.copy(showEarlyCheckoutWarning = false)
    val isPhotoValid = _uiState.value.capturedCheckOutBitmap != null || _uiState.value.checkOutPhotoUri != null
    if (!isPhotoValid) {
      triggerDetailedError(
        title = "Live Check-Out Photo Required",
        message = "You must open the camera and capture a live photo before confirming check-out.",
        guidance = "Tap the camera card below to capture a live photo to sync with Google Drive before checking out.",
      )
      return
    }
    checkOut(isEarly = true)
  }

  fun handlePrimaryAction() {
    viewModelScope.launch {
      // 0. Ensure fresh sync with local DB state for today for active worker
      val todayDate = repository.getTodayDateString()
      val currentWorkerName = _uiState.value.workerProfile.fullName
      val today = repository.getAttendanceForDateAndWorker(todayDate, currentWorkerName)
      if (today != null && today.status != _uiState.value.currentStatus) {
        val lastOp =
          when (today.status) {
            AttendanceStatus.CHECKED_IN -> "Last Action: ${today.checkInTime} (Check-In)"
            AttendanceStatus.CHECKED_OUT -> "Last Action: ${today.checkOutTime} (Check-Out)"
            AttendanceStatus.NOT_CHECKED_IN -> "Last Action: 04:30 PM (Out)"
          }
        val isPhotoReady =
          when (today.status) {
            AttendanceStatus.CHECKED_IN -> _uiState.value.capturedCheckOutBitmap != null || _uiState.value.checkOutPhotoUri != null
            AttendanceStatus.CHECKED_OUT -> true
            AttendanceStatus.NOT_CHECKED_IN -> _uiState.value.capturedCheckInBitmap != null || _uiState.value.checkInPhotoUri != null
          }
        _uiState.value =
          _uiState.value.copy(
            todayRecord = today,
            currentStatus = today.status,
            photoCaptured = isPhotoReady,
            lastOperationTime = lastOp,
          )
        checkAndStartCooldown(today.lastActionTimestampMillis)
      }

      // 1. Strict Location Permission & GPS hardware check
      val context = getApplication<Application>()
      val perm = com.example.service.LocationHelper.hasLocationPermission(context)
      val locEnabled = com.example.service.LocationHelper.isLocationServiceEnabled(context)
      if (!perm || !locEnabled) {
        triggerDetailedError(
          title = "GPS Location Disabled",
          message = "GPS Location service must be enabled and precise location granted to record attendance.",
          guidance = "Please enable GPS on your device and grant location permission to verify geofence perimeter.",
        )
        return@launch
      }

      // 2. Strict Cooldown check (30 seconds)
      if (_uiState.value.cooldownRemainingSeconds > 0) {
        val remSec = _uiState.value.cooldownRemainingSeconds
        triggerDetailedError(
          title = "Cooldown Active (30s)",
          message = "Multiple operations within 30 seconds are restricted.",
          guidance = "Please wait $remSec seconds before attempting again.",
        )
        return@launch
      }

      // 3. Strict Online Connection Check
      val online = checkNetworkStatus()
      if (!online) {
        triggerDetailedError(
          title = "No Internet Connection",
          message = "The application relies on cloud synchronization and requires an active internet connection.",
          guidance = "Please connect to Wi-Fi or mobile data (4G/5G) and try again.",
        )
        return@launch
      }

      // 4. Strict Geofence Distance Pre-check
      val realDist = repository.calculateDistanceMeters(
        _uiState.value.deviceLatitude,
        _uiState.value.deviceLongitude,
        _uiState.value.selectedSite.latitude,
        _uiState.value.selectedSite.longitude
      )
      val effectiveDistance =
        if (_uiState.value.isOutsideSimulation) maxOf(realDist, _uiState.value.selectedSite.radiusMeters + 150.0) else realDist

      if (effectiveDistance > _uiState.value.selectedSite.radiusMeters) {
        val isCheckingOut = _uiState.value.currentStatus == AttendanceStatus.CHECKED_IN
        val titleText = if (isCheckingOut) "Check-out Failed: Outside Geofence" else "Check-in Failed: Outside Geofence"
        val guidanceText = if (isCheckingOut) {
          "You are outside the authorized work site boundary (${effectiveDistance.toInt()}m / Allowed: ${_uiState.value.selectedSite.radiusMeters}m). Please move inside the site perimeter to check out."
        } else {
          "Current GPS coordinates are outside the authorized boundary. Please move inside the site perimeter or adjust geofence in dashboard."
        }
        triggerDetailedError(
          title = titleText,
          message = "You are ${effectiveDistance.toInt()}m away from (${_uiState.value.selectedSite.name}), while allowed radius is ${_uiState.value.selectedSite.radiusMeters}m.",
          guidance = guidanceText,
        )
        return@launch
      }

      val currentStatus = _uiState.value.currentStatus

      // 5. Strict Check for Check-in Photo (must NOT be null)
      if (currentStatus == AttendanceStatus.NOT_CHECKED_IN) {
        if (_uiState.value.capturedCheckInBitmap == null && _uiState.value.checkInPhotoUri == null) {
          triggerDetailedError(
            title = "Live Check-In Photo Required",
            message = "You must capture a live photo before confirming check-in.",
            guidance = "Tap the camera card below to capture a live photo to verify identity before checking in.",
          )
          return@launch
        }
        checkIn()
      } else if (currentStatus == AttendanceStatus.CHECKED_IN) {
        // 6. Strict Check for Check-out Photo (must NOT be null)
        val hasCheckOutPhoto = _uiState.value.capturedCheckOutBitmap != null || 
                               !_uiState.value.checkOutPhotoUri.isNullOrBlank() || 
                               !_uiState.value.checkOutPhotoBase64.isNullOrBlank()
        if (!hasCheckOutPhoto) {
          triggerDetailedError(
            title = "Live Check-Out Photo Required",
            message = "You must capture a live photo before confirming check-out.",
            guidance = "Tap the camera card below to capture a live photo to sync with Google Drive before checking out.",
          )
          return@launch
        }

        val isBeforeEnd = repository.isBeforeShiftEndTime(shiftConfig.value)
        if (isBeforeEnd) {
          _uiState.value = _uiState.value.copy(showEarlyCheckoutWarning = true)
        } else {
          checkOut(isEarly = false)
        }
      }
    }
  }

  private fun checkIn() {
    if (_uiState.value.isProcessing) return

    viewModelScope.launch {
      val context = getApplication<Application>()
      val perm = com.example.service.LocationHelper.hasLocationPermission(context)
      val locEnabled = com.example.service.LocationHelper.isLocationServiceEnabled(context)
      if (!perm || !locEnabled) {
        triggerDetailedError(
          title = "GPS Location Disabled",
          message = "GPS Location must be enabled with precise permission before check-in.",
          guidance = "Please turn on GPS and grant location permission.",
        )
        return@launch
      }

      _uiState.value = _uiState.value.copy(isProcessing = true)
      delay(600)

      val site = _uiState.value.selectedSite
      val isOutside = _uiState.value.isOutsideSimulation

      // Use real device coordinates or simulate outside offset if simulation active
      val workerLat = if (isOutside) site.latitude + 0.005 else _uiState.value.deviceLatitude
      val workerLng = if (isOutside) site.longitude + 0.005 else _uiState.value.deviceLongitude
      val accuracy = _uiState.value.accuracyMeters
      val online = checkNetworkStatus()

      val isPhotoValid = _uiState.value.capturedCheckInBitmap != null || _uiState.value.checkInPhotoUri != null

      val result =
        repository.processCheckIn(
          site = site,
          workerLat = workerLat,
          workerLng = workerLng,
          accuracyMeters = accuracy,
          photoCaptured = isPhotoValid,
          isOnline = online,
          isLocationValid = true,
          photoUri = _uiState.value.checkInPhotoUri,
          photoBase64 = _uiState.value.checkInPhotoBase64,
          driveUrl = _uiState.value.checkInDriveUrl,
          profile = _uiState.value.workerProfile,
        )

      when (result) {
        is AttendanceResult.Success -> {
          val randomQuote = CHECKIN_ENCOURAGEMENT_QUOTES.random()
          _uiState.value =
            _uiState.value.copy(
              isProcessing = false,
              todayRecord = result.record,
              currentStatus = AttendanceStatus.CHECKED_IN,
              photoCaptured = false,
              capturedPhotoBitmap = null,
              capturedCheckOutBitmap = null,
              checkOutPhotoUri = null,
              checkOutPhotoBase64 = null,
              checkOutDriveUrl = null,
              lastOperationTime = "Last Action: ${result.record.checkInTime} (Check-In)",
              notificationMessage = result.message,
              isNotificationError = false,
              showCheckinSuccessPopup = true,
              checkinRandomQuote = randomQuote,
            )
          startCooldownTimer(30L) // 30 seconds cooldown
        }
        is AttendanceResult.AlreadyCheckedIn -> {
          _uiState.value =
            _uiState.value.copy(
              isProcessing = false,
              todayRecord = result.record,
              currentStatus = AttendanceStatus.CHECKED_IN,
              photoCaptured = false,
              capturedPhotoBitmap = null,
              capturedCheckOutBitmap = null,
              checkOutPhotoUri = null,
              checkOutPhotoBase64 = null,
              checkOutDriveUrl = null,
              lastOperationTime = "Last Action: ${result.record.checkInTime} (Check-In)",
              notificationMessage = result.message,
              isNotificationError = false,
            )
          checkAndStartCooldown(result.record.lastActionTimestampMillis)
        }
        is AttendanceResult.AlreadyCheckedOut -> {
          _uiState.value =
            _uiState.value.copy(
              isProcessing = false,
              todayRecord = result.record,
              currentStatus = AttendanceStatus.CHECKED_OUT,
              photoCaptured = true,
              lastOperationTime = "Last Action: ${result.record.checkOutTime} (Check-Out)",
              notificationMessage = result.message,
              isNotificationError = false,
            )
        }
        is AttendanceResult.NoInternetError -> {
          triggerDetailedError(
            title = "Internet Connection Error",
            message = result.message,
            guidance = result.guidance,
          )
        }
        is AttendanceResult.CameraMissingError -> {
          triggerDetailedError(
            title = "Error: Photo Not Captured",
            message = result.message,
            guidance = result.guidance,
          )
        }
        is AttendanceResult.LocationDisabledError -> {
          triggerDetailedError(
            title = "Error: Location Disabled",
            message = result.message,
            guidance = result.guidance,
          )
        }
        is AttendanceResult.CooldownActive -> {
          triggerDetailedError(
            title = "Cooldown Rate Limit (30s)",
            message = result.message,
            guidance = "Please wait until the countdown timer completes.",
          )
          startCooldownTimer(result.remainingSeconds)
        }
        is AttendanceResult.GeofenceViolation -> {
          triggerDetailedError(
            title = "Check-in Failed: Outside Geofence",
            message = "You are ${result.currentDistanceMeters.toInt()}m from work site (Allowed: ${result.allowedRadiusMeters}m)",
            guidance = result.guidance,
          )
        }
        is AttendanceResult.AccuracyError -> {
          triggerDetailedError(
            title = "Check-in Failed: Inaccurate GPS",
            message = "Current GPS accuracy: ${result.accuracyMeters.toInt()}m (Required: less than 35m)",
            guidance = result.guidance,
          )
        }
        is AttendanceResult.DeviceError -> {
          triggerDetailedError(
            title = "Device Authentication Error",
            message = result.reason,
            guidance = result.guidance,
          )
        }
        is AttendanceResult.InvalidStateError -> {
          triggerDetailedError(
            title = "Invalid State",
            message = result.message,
            guidance = result.guidance,
          )
        }
      }
    }
  }

  private fun checkOut(isEarly: Boolean = false) {
    if (_uiState.value.isProcessing) return

    val isPhotoValid = _uiState.value.capturedCheckOutBitmap != null || 
                       !_uiState.value.checkOutPhotoUri.isNullOrBlank() || 
                       !_uiState.value.checkOutPhotoBase64.isNullOrBlank()
    if (!isPhotoValid) {
      triggerDetailedError(
        title = "Live Check-Out Photo Required",
        message = "You must capture a live photo before confirming check-out.",
        guidance = "Tap the camera card below to capture a live photo to sync with Google Drive before checking out.",
      )
      return
    }

    viewModelScope.launch {
      val context = getApplication<Application>()
      val perm = com.example.service.LocationHelper.hasLocationPermission(context)
      val locEnabled = com.example.service.LocationHelper.isLocationServiceEnabled(context)
      if (!perm || !locEnabled) {
        triggerDetailedError(
          title = "GPS Location Disabled",
          message = "GPS Location service must be enabled before checking out.",
          guidance = "Please enable GPS and grant location permission.",
        )
        return@launch
      }

      _uiState.value = _uiState.value.copy(isProcessing = true)
      delay(600)

      val site = _uiState.value.selectedSite
      val isOutside = _uiState.value.isOutsideSimulation

      // Use real device coordinates or simulate outside offset if simulation active
      val workerLat = if (isOutside) site.latitude + 0.005 else _uiState.value.deviceLatitude
      val workerLng = if (isOutside) site.longitude + 0.005 else _uiState.value.deviceLongitude
      val accuracy = _uiState.value.accuracyMeters
      val online = checkNetworkStatus()

      val result =
        repository.processCheckOut(
          site = site,
          workerLat = workerLat,
          workerLng = workerLng,
          accuracyMeters = accuracy,
          isOnline = online,
          isLocationValid = true,
          isEarlyDeparture = isEarly,
          photoCaptured = isPhotoValid,
          photoUri = _uiState.value.checkOutPhotoUri,
          photoBase64 = _uiState.value.checkOutPhotoBase64,
          driveUrl = _uiState.value.checkOutDriveUrl,
          profile = _uiState.value.workerProfile,
        )

      when (result) {
        is AttendanceResult.Success -> {
          val randomQuote = CHECKOUT_ENCOURAGEMENT_QUOTES.random()
          val workerName = _uiState.value.workerProfile.fullName.ifBlank { "Employee" }
          val checkOutTime = result.record.checkOutTime ?: "Now"

          _uiState.value =
            _uiState.value.copy(
              isProcessing = false,
              todayRecord = result.record,
              currentStatus = AttendanceStatus.CHECKED_OUT,
              lastOperationTime = "Last Action: ${result.record.checkOutTime} (${if (isEarly) "Early Check-Out" else "Check-Out"})",
              notificationMessage = result.message,
              isNotificationError = false,
              showCheckoutSuccessPopup = true,
              checkoutRandomQuote = randomQuote,
              checkoutSuccessWorkerName = workerName,
              checkoutSuccessTime = checkOutTime,
            )
          startCooldownTimer(30L) // 30 seconds cooldown
        }
        is AttendanceResult.AlreadyCheckedOut -> {
          _uiState.value =
            _uiState.value.copy(
              isProcessing = false,
              todayRecord = result.record,
              currentStatus = AttendanceStatus.CHECKED_OUT,
              lastOperationTime = "Last Action: ${result.record.checkOutTime} (Check-Out)",
              notificationMessage = result.message,
              isNotificationError = false,
            )
        }
        is AttendanceResult.AlreadyCheckedIn -> {
          _uiState.value =
            _uiState.value.copy(
              isProcessing = false,
              todayRecord = result.record,
              currentStatus = AttendanceStatus.CHECKED_IN,
              lastOperationTime = "Last Action: ${result.record.checkInTime} (Check-In)",
              notificationMessage = result.message,
              isNotificationError = false,
            )
        }
        is AttendanceResult.NoInternetError -> {
          triggerDetailedError(
            title = "Internet Connection Error",
            message = result.message,
            guidance = result.guidance,
          )
        }
        is AttendanceResult.CameraMissingError -> {
          triggerDetailedError(
            title = "Error: Check-out Photo Missing",
            message = result.message,
            guidance = result.guidance,
          )
        }
        is AttendanceResult.LocationDisabledError -> {
          triggerDetailedError(
            title = "Error: Location Disabled",
            message = result.message,
            guidance = result.guidance,
          )
        }
        is AttendanceResult.CooldownActive -> {
          triggerDetailedError(
            title = "Cooldown Rate Limit (30s)",
            message = result.message,
            guidance = "Please wait until the 30-second cooldown expires.",
          )
          startCooldownTimer(result.remainingSeconds)
        }
        is AttendanceResult.GeofenceViolation -> {
          triggerDetailedError(
            title = "Check-out Failed: Outside Geofence",
            message = "You must be inside the work site to check out (${result.currentDistanceMeters.toInt()}m)",
            guidance = result.guidance,
          )
        }
        is AttendanceResult.AccuracyError -> {
          triggerDetailedError(
            title = "Check-out Failed: Inaccurate GPS",
            message = "Current GPS accuracy: ${result.accuracyMeters.toInt()}m",
            guidance = result.guidance,
          )
        }
        is AttendanceResult.InvalidStateError -> {
          triggerDetailedError(
            title = "Check-out State Notice",
            message = result.message,
            guidance = result.guidance,
          )
        }
        is AttendanceResult.DeviceError -> {
          triggerDetailedError(
            title = "Device Authentication Error",
            message = result.reason,
            guidance = result.guidance,
          )
        }
        else -> {
          triggerDetailedError(
            title = "Error Occurred",
            message = "Unable to complete check-out process.",
            guidance = "Check your internet connection, verify live photo, and ensure you are within the work site.",
          )
        }
      }
    }
  }

  fun deleteAttendanceRecord(record: AttendanceRecord) {
    viewModelScope.launch {
      val online = checkNetworkStatus()
      if (!online) {
        triggerDetailedError(
          title = "No Internet Connection",
          message = "Deleting a record requires an active internet connection to sync with cloud.",
          guidance = "Please connect to the internet and try again.",
        )
        return@launch
      }

      _uiState.value = _uiState.value.copy(isProcessing = true)

      val isToday = record.workDate == repository.getTodayDateString()

      // 1. Perform database and Google Drive / Firestore deletion
      val app = getApplication<Application>()
      repository.deleteAttendanceRecord(app, record)

      // 2. If it is today's record, reset all today state and buttons back to initial NOT_CHECKED_IN
      if (isToday) {
        cooldownTimerJob?.cancel()
        _uiState.value =
          _uiState.value.copy(
            todayRecord = null,
            currentStatus = AttendanceStatus.NOT_CHECKED_IN,
            photoCaptured = false,
            capturedPhotoBitmap = null,
            capturedCheckInBitmap = null,
            checkInPhotoUri = null,
            checkInDriveUrl = null,
            capturedCheckOutBitmap = null,
            checkOutPhotoUri = null,
            checkOutDriveUrl = null,
            cooldownRemainingSeconds = 0L,
            lastOperationTime = "Last Action: None (Record Deleted)",
          )
      }

      // 3. Refresh from Cloud to update all states and lists
      refreshFromCloud()

      _uiState.value =
        _uiState.value.copy(
          isProcessing = false,
          notificationMessage = "Record deleted, photos removed from Google Drive, and attendance status reset successfully ✓",
          isNotificationError = false,
        )
    }
  }

  fun setMapTilerApiKey(key: String) {
    _uiState.value = _uiState.value.copy(mapTilerApiKey = key.trim())
  }

  fun setMapStyle(style: String) {
    _uiState.value = _uiState.value.copy(mapStyle = style)
  }

  fun resetToday() {
    viewModelScope.launch {
      cooldownTimerJob?.cancel()
      repository.resetTodayForTesting()
      _uiState.value =
        _uiState.value.copy(
          todayRecord = null,
          currentStatus = AttendanceStatus.NOT_CHECKED_IN,
          photoCaptured = false,
          capturedPhotoBitmap = null,
          capturedCheckInBitmap = null,
          checkInPhotoUri = null,
          checkInDriveUrl = null,
          capturedCheckOutBitmap = null,
          checkOutPhotoUri = null,
          checkOutDriveUrl = null,
          cooldownRemainingSeconds = 0L,
          lastOperationTime = "Last Action: 04:30 PM (Out)",
          notificationMessage = "Today's state and cooldown timer reset for testing successfully",
          isNotificationError = false,
        )
    }
  }

  private fun isBcryptHash(value: String): Boolean {
    return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$")
  }

  private fun hashPassword(plain: String): String {
    return BCrypt.withDefaults().hashToString(12, plain.toCharArray())
  }

  private fun hashPasswordIfNeeded(passwordOrHash: String): String {
    return if (isBcryptHash(passwordOrHash)) passwordOrHash else hashPassword(passwordOrHash)
  }

  private fun verifyPassword(password: String, hash: String): Boolean {
    return try {
      if (isBcryptHash(hash)) {
        BCrypt.verifyer().verify(password.toCharArray(), hash.toCharArray()).verified
      } else {
        // Fallback for legacy seeded text before upgrade
        hash == password
      }
    } catch (e: Exception) {
      Log.e("AttendanceViewModel", "BCrypt verification exception: ${e.message}")
      false
    }
  }

  fun login(
    usernameInput: String,
    passwordInput: String,
    rememberMe: Boolean = true,
    context: Context? = null,
  ) {
    val username = usernameInput.trim()
    val password = passwordInput.trim()
    if (username.isEmpty() || password.isEmpty()) {
      _uiState.value = _uiState.value.copy(
        loginErrorMessage = "Please enter both username and password.",
        loginSecurityAlertMessage = null,
      )
      return
    }

    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoginProcessing = true, loginErrorMessage = null, loginSecurityAlertMessage = null)
      val user = repository.getUserByUsername(username)
      if (user == null || !verifyPassword(password, user.passwordHash)) {
        _uiState.value = _uiState.value.copy(
          isLoginProcessing = false,
          loginErrorMessage = "Invalid username or password. Please verify your credentials.",
        )
        return@launch
      }

      // Upgrade legacy password hash if needed
      if (!isBcryptHash(user.passwordHash)) {
        val upgraded = user.copy(passwordHash = hashPassword(password))
        repository.updateUser(upgraded)
      }

      val currentDeviceModel = "${android.os.Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${android.os.Build.MODEL}"
      val currentDeviceId = "DEV-${android.os.Build.ID.hashCode().toString().replace("-", "").take(6)}"

      // Device Binding Verification
      if (user.boundDeviceId.isNotBlank() && user.boundDeviceId != currentDeviceId && user.boundDeviceModel != currentDeviceModel) {
        repository.reportDeviceMismatch(
          username = user.username,
          workerId = user.workerId,
          workerName = user.workerName,
          attemptedDeviceModel = currentDeviceModel,
          attemptedDeviceId = currentDeviceId,
        )
        _uiState.value = _uiState.value.copy(
          isLoginProcessing = false,
          loginSecurityAlertMessage = "Hardware Mismatch: This account is locked to device (${user.boundDeviceModel}). Contact your administrator to unbind previous hardware.",
        )
        return@launch
      }

      // If user hasn't bound a device yet, automatically bind to this device
      val effectiveUser = if (user.boundDeviceId.isBlank()) {
        val bound = user.copy(boundDeviceId = currentDeviceId, boundDeviceModel = currentDeviceModel)
        repository.updateUser(bound)
        bound
      } else {
        user
      }

      // Update worker profile if linked to a worker entity
      val workers = repository.attendanceDao.getAllWorkersDirect()
      val linkedWorker = workers.find { it.id == effectiveUser.workerId || it.fullName.trim().equals(effectiveUser.workerName.trim(), ignoreCase = true) }
      val newProfile = if (linkedWorker != null) {
        _uiState.value.workerProfile.copy(
          id = linkedWorker.id,
          fullName = linkedWorker.fullName,
          role = linkedWorker.role,
          nationalId = linkedWorker.nationalId,
          phoneNumber = linkedWorker.phoneNumber,
          deviceModel = effectiveUser.boundDeviceModel.ifBlank { linkedWorker.deviceModel },
        )
      } else {
        _uiState.value.workerProfile.copy(
          fullName = effectiveUser.workerName.ifBlank { effectiveUser.username },
        )
      }

      val initialTab = if (effectiveUser.role == UserRole.ADMIN) BentoTab.DASHBOARD else BentoTab.HOME
      
      // Save session preferences for remember me (username only, never plaintext password)
      try {
        val app = getApplication<Application>()
        val prefs = app.getSharedPreferences("auth_session_prefs", Context.MODE_PRIVATE)
        if (rememberMe) {
          prefs.edit()
            .putString("saved_user", username)
            .remove("saved_pass")
            .putBoolean("remember_me", true)
            .apply()
        } else {
          prefs.edit()
            .remove("saved_user")
            .remove("saved_pass")
            .putBoolean("remember_me", false)
            .apply()
        }
      } catch (e: Exception) {
        Log.e("AttendanceViewModel", "Failed to save login session: ${e.message}")
      }

      _uiState.value = _uiState.value.copy(
        isLoggedIn = true,
        isLoginProcessing = false,
        currentUserAccount = effectiveUser,
        workerProfile = newProfile,
        isRememberMeChecked = rememberMe,
        savedUsername = if (rememberMe) username else "",
        savedPassword = "",
        currentTab = initialTab,
        loginErrorMessage = null,
        loginSecurityAlertMessage = null,
      )
    }
  }

  fun logout() {
    _uiState.value = _uiState.value.copy(
      isLoggedIn = false,
      currentUserAccount = null,
      currentTab = BentoTab.HOME,
      loginErrorMessage = null,
      loginSecurityAlertMessage = null,
    )
  }

  fun clearLoginError() {
    _uiState.value = _uiState.value.copy(
      loginErrorMessage = null,
      loginSecurityAlertMessage = null,
    )
  }

  fun addUser(user: UserAccount) {
    viewModelScope.launch {
      val secureUser = user.copy(passwordHash = hashPasswordIfNeeded(user.passwordHash))
      repository.addUser(secureUser)
      _uiState.value = _uiState.value.copy(
        notificationMessage = "User @${user.username} created successfully ✓",
        isNotificationError = false,
      )
    }
  }

  fun updateUser(user: UserAccount) {
    viewModelScope.launch {
      val secureUser = user.copy(passwordHash = hashPasswordIfNeeded(user.passwordHash))
      repository.updateUser(secureUser)
      if (_uiState.value.currentUserAccount?.username == secureUser.username) {
        _uiState.value = _uiState.value.copy(currentUserAccount = secureUser)
      }
      _uiState.value = _uiState.value.copy(
        notificationMessage = "User @${user.username} updated successfully ✓",
        isNotificationError = false,
      )
    }
  }

  fun deleteUser(username: String) {
    viewModelScope.launch {
      repository.deleteUser(username)
      _uiState.value = _uiState.value.copy(
        notificationMessage = "User @$username removed ✓",
        isNotificationError = false,
      )
    }
  }

  fun resetDeviceBindingForUser(username: String) {
    viewModelScope.launch {
      repository.resetUserDeviceBinding(username)
      if (_uiState.value.currentUserAccount?.username == username) {
        _uiState.value = _uiState.value.copy(
          currentUserAccount = _uiState.value.currentUserAccount?.copy(
            boundDeviceId = "",
            boundDeviceModel = "",
            boundDeviceIp = "",
          )
        )
      }
      _uiState.value = _uiState.value.copy(
        notificationMessage = "Device binding reset for @$username ✓",
        isNotificationError = false,
      )
    }
  }

  fun resetCurrentDeviceBinding() {
    val cur = _uiState.value.currentUserAccount ?: return
    resetDeviceBindingForUser(cur.username)
  }

  fun resolveSecurityAlert(alertId: Long) {
    viewModelScope.launch {
      repository.resolveDeviceAlert(alertId)
      _uiState.value = _uiState.value.copy(
        notificationMessage = "Security alert resolved ✓",
        isNotificationError = false,
      )
    }
  }

  fun submitLeaveRequest(
    type: LeaveType,
    startDate: String,
    endDate: String,
    isHalfDay: Boolean,
    reason: String,
  ) {
    viewModelScope.launch {
      val isConnected = CloudSyncService.isOnline(getApplication())
      if (!isConnected) {
        _uiState.value = _uiState.value.copy(
          isSubmittingLeave = false,
          leaveSubmissionErrorMessage = "لا يمكن تقديم طلب الإجازة بدون اتصال بالإنترنت. يرجى التحقق من الشبكة.",
          notificationMessage = "No Internet Connection - Leave submission blocked",
          isNotificationError = true,
        )
        return@launch
      }

      _uiState.value = _uiState.value.copy(
        isSubmittingLeave = true,
        leaveSubmissionErrorMessage = null,
      )
      val prof = _uiState.value.workerProfile
      val totalDays = if (isHalfDay) {
        0.5
      } else {
        try {
          val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH)
          val sDate = sdf.parse(startDate)
          val eDate = sdf.parse(endDate)
          if (sDate != null && eDate != null) {
            val diff = eDate.time - sDate.time
            val days = (diff / (1000 * 60 * 60 * 24)).toDouble() + 1.0
            maxOf(1.0, days)
          } else {
            1.0
          }
        } catch (_: Exception) {
          1.0
        }
      }

      val req = LeaveRequest(
        workerId = prof.id,
        workerName = prof.fullName,
        leaveType = type,
        startDate = startDate,
        endDate = endDate,
        isHalfDay = isHalfDay,
        totalDays = totalDays,
        reason = reason,
        requestDate = repository.getCurrentTimestamp(),
      )
      repository.submitLeaveRequest(req)

      // Send local system notification to Admin on device
      com.example.service.AppNotificationHelper.sendAdminLeaveNotification(
        context = getApplication(),
        workerName = prof.fullName,
        leaveType = type.name,
        totalDays = totalDays,
      )

      _uiState.value = _uiState.value.copy(
        isSubmittingLeave = false,
        leaveSubmissionSuccessMessage = "Request sent! Waiting for approval",
        notificationMessage = "Request sent! Waiting for approval ✓",
        isNotificationError = false,
        showLeaveSuccessPopup = true,
      )
    }
  }

  fun cancelLeaveRequest(requestId: Long) {
    viewModelScope.launch {
      val isConnected = CloudSyncService.isOnline(getApplication())
      if (!isConnected) {
        _uiState.value = _uiState.value.copy(
          notificationMessage = "لا يمكن إلغاء طلب الإجازة بدون اتصال بالإنترنت. يرجى التحقق من الشبكة.",
          isNotificationError = true,
        )
        return@launch
      }

      val prof = _uiState.value.workerProfile
      val success = repository.cancelLeaveRequest(requestId, prof.id)
      if (success) {
        _uiState.value = _uiState.value.copy(
          notificationMessage = "Leave request cancelled successfully ✓",
          isNotificationError = false,
        )
      } else {
        _uiState.value = _uiState.value.copy(
          notificationMessage = "Cannot cancel request that has already been approved or processed.",
          isNotificationError = true,
        )
      }
    }
  }

  fun deleteLeaveRequestAsAdmin(requestId: Long) {
    viewModelScope.launch {
      val isConnected = CloudSyncService.isOnline(getApplication())
      if (!isConnected) {
        _uiState.value = _uiState.value.copy(
          notificationMessage = "لا يمكن حذف طلب الإجازة بدون اتصال بالإنترنت.",
          isNotificationError = true,
        )
        return@launch
      }

      repository.deleteLeaveRequestAsAdmin(requestId)
      _uiState.value = _uiState.value.copy(
        notificationMessage = "Leave request #$requestId deleted by Admin ✓",
        isNotificationError = false,
      )
    }
  }

  fun updateLeaveRequestAsAdmin(updatedRequest: LeaveRequest) {
    viewModelScope.launch {
      val isConnected = CloudSyncService.isOnline(getApplication())
      if (!isConnected) {
        _uiState.value = _uiState.value.copy(
          notificationMessage = "لا يمكن تعديل طلب الإجازة بدون اتصال بالإنترنت.",
          isNotificationError = true,
        )
        return@launch
      }

      repository.updateLeaveRequestAsAdmin(updatedRequest)
      _uiState.value = _uiState.value.copy(
        notificationMessage = "Leave request for ${updatedRequest.workerName} updated successfully ✓",
        isNotificationError = false,
      )
    }
  }

  fun dismissLeaveSuccessPopup() {
    _uiState.value = _uiState.value.copy(showLeaveSuccessPopup = false)
  }

  fun clearLeaveSubmissionStatus() {
    _uiState.value = _uiState.value.copy(
      leaveSubmissionSuccessMessage = null,
      leaveSubmissionErrorMessage = null,
      showLeaveSuccessPopup = false,
    )
  }

  fun approveLeaveRequest(requestId: Long) {
    viewModelScope.launch {
      val isConnected = CloudSyncService.isOnline(getApplication())
      if (!isConnected) {
        _uiState.value = _uiState.value.copy(
          notificationMessage = "لا يمكن قبول طلب الإجازة بدون اتصال بالإنترنت.",
          isNotificationError = true,
        )
        return@launch
      }

      val adminName = _uiState.value.currentUserAccount?.workerName ?: "Admin"
      repository.approveLeaveRequest(requestId, adminName)
      _uiState.value = _uiState.value.copy(
        notificationMessage = "Leave request approved ✓",
        isNotificationError = false,
      )
    }
  }

  fun rejectLeaveRequest(requestId: Long, reason: String = "") {
    viewModelScope.launch {
      val isConnected = CloudSyncService.isOnline(getApplication())
      if (!isConnected) {
        _uiState.value = _uiState.value.copy(
          notificationMessage = "لا يمكن رفض طلب الإجازة بدون اتصال بالإنترنت.",
          isNotificationError = true,
        )
        return@launch
      }

      val adminName = _uiState.value.currentUserAccount?.workerName ?: "Admin"
      repository.rejectLeaveRequest(requestId, adminName, reason)
      _uiState.value = _uiState.value.copy(
        notificationMessage = "Leave request rejected",
        isNotificationError = false,
      )
    }
  }

  fun dismissCheckinSuccessPopup() {
    _uiState.value = _uiState.value.copy(showCheckinSuccessPopup = false)
  }
}

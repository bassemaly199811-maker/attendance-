package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.LeaveStatus
import com.example.data.model.UserRole
import com.example.ui.components.BentoNavigationBar
import com.example.ui.screens.AdminLeaveApprovalScreen
import com.example.ui.screens.AppSplashScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TimeOffScreen
import com.example.ui.screens.UserManagementScreen
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.ui.viewmodel.BentoTab

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Pre-create WebView cache subdirectories to prevent Chromium simple_file_enumerator warnings
    try {
      java.io.File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/js").mkdirs()
      java.io.File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/wasm").mkdirs()
    } catch (_: Exception) {}

    setContent {
      MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
          WorkAttendanceApp(initialIntent = intent)
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
  }
}

@Composable
fun WorkAttendanceApp(
  initialIntent: Intent? = null,
  viewModel: AttendanceViewModel = viewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
  val allSites by viewModel.allSites.collectAsStateWithLifecycle()
  val allWorkers by viewModel.allWorkers.collectAsStateWithLifecycle()
  val activityLogs by viewModel.activityLogs.collectAsStateWithLifecycle()
  val workersList by viewModel.workersList.collectAsStateWithLifecycle()
  val shiftConfig by viewModel.shiftConfig.collectAsStateWithLifecycle()
  val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
  val allLeaveRequests by viewModel.allLeaveRequests.collectAsStateWithLifecycle()
  val allLeaveBalances by viewModel.allLeaveBalances.collectAsStateWithLifecycle()
  val userLeaveRequests by viewModel.userLeaveRequests.collectAsStateWithLifecycle()
  val currentUserLeaveBalance by viewModel.currentUserLeaveBalance.collectAsStateWithLifecycle()
  val allSecurityAlerts by viewModel.allSecurityAlerts.collectAsStateWithLifecycle()

  val pendingLeaveCount = remember(allLeaveRequests) {
    allLeaveRequests.count { it.status == LeaveStatus.PENDING }
  }

  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  // Permission Launcher for Location
  val locationPermissionLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
      viewModel.checkAndRefreshLocationState(context)
    }

  // Permission Launcher for Camera
  val cameraPermissionLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.RequestPermission(),
    ) {
      viewModel.checkAndRefreshLocationState(context)
    }

  // Check and refresh state on startup, and handle navigation from notifications
  LaunchedEffect(initialIntent) {
    viewModel.checkAndRefreshLocationState(context)
    viewModel.refreshFromCloud(isInitial = true)

    val openTab = initialIntent?.getStringExtra("OPEN_TAB")
    when (openTab) {
      "SECURITY_ALERTS" -> {
        viewModel.openUserManagementTab(initialSubTab = 1)
      }
      "USER_MANAGEMENT" -> {
        viewModel.openUserManagementTab(initialSubTab = 0)
      }
      "LEAVE_APPROVALS" -> {
        viewModel.setTab(BentoTab.LEAVE_APPROVALS)
      }
    }
  }

  // Refresh status on resume
  DisposableEffect(lifecycleOwner) {
    val observer =
      LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
          viewModel.checkAndRefreshLocationState(context)
          viewModel.refreshFromCloud(isInitial = false)
        }
      }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  // 1. Splash Screen & Pre-flight Gate: Checks Internet, Camera, Location GPS
  if (!uiState.isAppReady) {
    AppSplashScreen(
      isOnline = uiState.isOnline,
      hasCameraPermission = uiState.hasCameraPermission,
      hasLocationPermission = uiState.hasLocationPermission,
      isLocationEnabled = uiState.isLocationEnabled,
      isLoading = uiState.isInitialLoading,
      onRequestCameraPermission = {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
      },
      onRequestLocationPermission = {
        locationPermissionLauncher.launch(
          arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
          )
        )
      },
      onEnableGpsSettings = {
        try {
          context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        } catch (e: Exception) {
          context.startActivity(Intent(Settings.ACTION_SETTINGS))
        }
      },
      onRetryConnection = { viewModel.retryInitialStartup(context) },
    )
    return
  }

  // 2. Authentication Gate: If not logged in, show LoginScreen
  if (!uiState.isLoggedIn) {
    LoginScreen(
      onLogin = { username, password, rememberMe ->
        viewModel.login(username, password, rememberMe, context)
      },
      savedUsername = uiState.savedUsername,
      savedPassword = uiState.savedPassword,
      errorMessage = uiState.loginErrorMessage ?: uiState.loginSecurityAlertMessage,
      isLoggingIn = uiState.isLoginProcessing,
    )
    return
  }

  // 3. Main Authenticated Experience
  val isAdmin = uiState.currentUserAccount?.role == UserRole.ADMIN

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = BentoBackground,
    contentWindowInsets = WindowInsets.safeDrawing,
    bottomBar = {
      Box(
        modifier =
          Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
      ) {
        BentoNavigationBar(
          currentTab = uiState.currentTab,
          onTabSelected = { viewModel.setTab(it) },
          isAdmin = isAdmin,
          pendingLeaveCount = pendingLeaveCount,
        )
      }
    },
  ) { innerPadding ->
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(innerPadding),
    ) {
      when (uiState.currentTab) {
        BentoTab.HOME -> {
          HomeScreen(
            uiState = uiState,
            allSites = allSites,
            workers = allWorkers,
            shiftEndTime = shiftConfig.endTime,
            onPrimaryAction = { viewModel.handlePrimaryAction() },
            onConfirmEarlyCheckout = { viewModel.confirmEarlyCheckout() },
            onDismissEarlyCheckoutWarning = { viewModel.dismissEarlyCheckoutWarning() },
            onSiteSelect = { viewModel.selectSite(it) },
            onSelectWorker = { viewModel.selectWorker(it) },
            onSelectWorkerByName = { viewModel.selectWorkerByName(it) },
            onToggleSimulator = { viewModel.toggleGeofenceSimulation(it) },
            onLivePhotoCaptured = { viewModel.onLivePhotoCaptured(it) },
            onCameraDismissed = { viewModel.onCameraDismissed() },
            onDismissNotification = { viewModel.dismissNotification() },
            onDismissErrorDialog = { viewModel.dismissErrorDialog() },
            onDismissCheckoutSuccess = { viewModel.dismissCheckoutSuccessPopup() },
            onDismissCheckinSuccess = { viewModel.dismissCheckinSuccessPopup() },
            onNavigateToDashboard = { viewModel.setTab(if (isAdmin) BentoTab.DASHBOARD else BentoTab.TIME_OFF) },
            onUpdateGps = { lat, lng, acc -> viewModel.updateRealGpsCoordinates(lat, lng, acc) },
            onRefreshGps = { viewModel.refreshDeviceLocation(context) },
          )
        }

        BentoTab.DASHBOARD -> {
          DashboardScreen(
            workers = workersList,
            rawWorkers = allWorkers,
            sites = allSites,
            activityLogs = activityLogs,
            records = allRecords,
            shiftConfig = shiftConfig,
            mapTilerApiKey = uiState.mapTilerApiKey,
            deviceLatitude = uiState.deviceLatitude,
            deviceLongitude = uiState.deviceLongitude,
            isDeviceLocationReady = uiState.isDeviceLocationReady,
            isOnline = uiState.isOnline,
            allLeaveRequests = allLeaveRequests,
            allLeaveBalances = allLeaveBalances,
            securityAlerts = allSecurityAlerts,
            users = allUsers,
            isRefreshing = uiState.isRefreshing,
            lastSyncTime = uiState.lastSyncTime,
            onRefresh = { viewModel.refreshFromCloud() },
            onAddNewSite = { name, lat, lng, radius, address ->
              viewModel.addNewSite(name, lat, lng, radius, address)
            },
            onUpdateSite = { viewModel.updateSite(it) },
            onDeleteSite = { viewModel.deleteSite(it) },
            onAddNewWorker = { name, role, siteId, siteName, nationalId, phone, device, isApproved, assignedIds, assignedNames, iqamaNum, iqamaStart, iqamaEnd, insNum, insProvider, insStart, insEnd, passport, nationality, contractEnd, salary, hireDate, employmentEndDate, annualLeave, casualLeave, sickLeave ->
              viewModel.addNewWorker(
                name,
                role,
                siteId,
                siteName,
                nationalId,
                phone,
                device,
                isApproved,
                assignedIds,
                assignedNames,
                iqamaNum,
                iqamaStart,
                iqamaEnd,
                insNum,
                insProvider,
                insStart,
                insEnd,
                passport,
                nationality,
                contractEnd,
                salary,
                hireDate,
                employmentEndDate,
                annualLeave,
                casualLeave,
                sickLeave,
              )
            },
            onAddNewWorkerWithAccount = { name, role, siteId, siteName, nationalId, phone, device, isApproved, assignedIds, assignedNames, iqamaNum, iqamaStart, iqamaEnd, insNum, insProvider, insStart, insEnd, passport, nationality, contractEnd, salary, hireDate, employmentEndDate, annualLeave, casualLeave, sickLeave, username, password, userRole, createAccount ->
              viewModel.addNewWorkerWithAccount(
                fullName = name,
                role = role,
                siteId = siteId,
                siteName = siteName,
                nationalId = nationalId,
                phone = phone,
                deviceModel = device,
                isApproved = isApproved,
                assignedSiteIds = assignedIds,
                assignedSiteNames = assignedNames,
                iqamaNumber = iqamaNum,
                iqamaStartDate = iqamaStart,
                iqamaEndDate = iqamaEnd,
                insuranceNumber = insNum,
                insuranceProvider = insProvider,
                insuranceStartDate = insStart,
                insuranceEndDate = insEnd,
                passportNumber = passport,
                nationality = nationality,
                contractEndDate = contractEnd,
                salary = salary,
                hireDate = hireDate,
                employmentEndDate = employmentEndDate,
                annualLeaveTotal = annualLeave,
                casualLeaveTotal = casualLeave,
                sickLeaveTotal = sickLeave,
                username = if (createAccount && username.isNotBlank()) username else null,
                passwordPlain = if (createAccount && password.isNotBlank()) password else null,
                userRole = userRole,
              )
            },
            onUpdateWorker = { viewModel.updateWorker(it) },
            onUpdateWorkerWithAccount = { worker, _, newUsername, newPassword, newRole, resetDevice ->
              viewModel.updateWorkerWithAccount(
                worker = worker,
                username = newUsername,
                passwordPlain = newPassword,
                userRole = newRole,
                resetDeviceBinding = resetDevice,
              )
            },
            onDeleteWorker = { viewModel.deleteWorker(it) },
            onResetDeviceBinding = { username ->
              viewModel.resetDeviceBindingForUser(username)
            },
            onUpdateLeaveBalance = { workerId, annual, casual, sick, annualUsed, casualUsed, sickUsed ->
              viewModel.updateWorkerLeaveBalance(workerId, annual, casual, sick, annualUsed, casualUsed, sickUsed)
            },
            onUpdateWorkerDocuments = { workerId, iqamaNum, iqamaStart, iqamaEnd, insNum, insProvider, insStart, insEnd, passport, nationality, contractEnd, salary, hireDate, employmentEndDate ->
              viewModel.updateWorkerDocuments(
                workerId,
                iqamaNum,
                iqamaStart,
                iqamaEnd,
                insNum,
                insProvider,
                insStart,
                insEnd,
                passport,
                nationality,
                contractEnd,
                salary,
                hireDate,
                employmentEndDate,
              )
            },
            onUpdateShiftConfig = { viewModel.updateShiftSchedule(it) },
            onNavigateToUserManagement = { viewModel.openUserManagementTab(initialSubTab = 0) },
            onNavigateToSecurityAlerts = { viewModel.openUserManagementTab(initialSubTab = 1) },
            onNavigateToLeaveApprovals = { viewModel.setTab(BentoTab.LEAVE_APPROVALS) },
          )
        }

        BentoTab.USER_MANAGEMENT -> {
          UserManagementScreen(
            users = allUsers,
            workers = allWorkers,
            securityAlerts = allSecurityAlerts,
            onAddUser = { user -> viewModel.addUser(user) },
            onUpdateUser = { user -> viewModel.updateUser(user) },
            onDeleteUser = { username -> viewModel.deleteUser(username) },
            onResetDeviceBinding = { username -> viewModel.resetDeviceBindingForUser(username) },
            onResolveSecurityAlert = { alertId -> viewModel.resolveSecurityAlert(alertId) },
            initialTab = uiState.userManagementInitialTab,
            onClose = { viewModel.setTab(BentoTab.DASHBOARD) },
          )
        }

        BentoTab.LEAVE_APPROVALS -> {
          AdminLeaveApprovalScreen(
            requests = allLeaveRequests,
            onApprove = { requestId -> viewModel.approveLeaveRequest(requestId) },
            onReject = { requestId, reason -> viewModel.rejectLeaveRequest(requestId, reason) },
            onDeleteRequest = { requestId -> viewModel.deleteLeaveRequestAsAdmin(requestId) },
            onUpdateRequest = { updatedRequest -> viewModel.updateLeaveRequestAsAdmin(updatedRequest) },
            onClose = { viewModel.setTab(BentoTab.DASHBOARD) },
          )
        }

        BentoTab.TIME_OFF -> {
          TimeOffScreen(
            profile = uiState.workerProfile,
            leaveBalance = currentUserLeaveBalance,
            leaveRequests = userLeaveRequests,
            isOnline = uiState.isOnline,
            onSubmitLeaveRequest = { type, start, end, isHalfDay, reason ->
              viewModel.submitLeaveRequest(type, start, end, isHalfDay, reason)
            },
            onCancelLeaveRequest = { requestId ->
              viewModel.cancelLeaveRequest(requestId)
            },
            isSubmitting = uiState.isSubmittingLeave,
            submissionSuccessMessage = uiState.leaveSubmissionSuccessMessage,
            submissionErrorMessage = uiState.leaveSubmissionErrorMessage,
            showSuccessPopup = uiState.showLeaveSuccessPopup,
            onDismissSuccessPopup = { viewModel.dismissLeaveSuccessPopup() },
            onClearSubmissionStatus = { viewModel.clearLeaveSubmissionStatus() },
          )
        }

        BentoTab.HISTORY -> {
          val visibleRecords = if (isAdmin) {
            allRecords
          } else {
            val targetName = uiState.currentUserAccount?.workerName?.ifEmpty { uiState.workerProfile.fullName } ?: uiState.workerProfile.fullName
            allRecords.filter { rec ->
              rec.workerName.equals(targetName, ignoreCase = true) ||
                (targetName.isNotBlank() && rec.workerName.isNotBlank() && (targetName.contains(rec.workerName, ignoreCase = true) || rec.workerName.contains(targetName, ignoreCase = true)))
            }
          }
          HistoryScreen(
            records = visibleRecords,
            workers = if (isAdmin) workersList else emptyList(),
            rawWorkers = if (isAdmin) allWorkers else emptyList(),
            sites = allSites,
            isRefreshing = uiState.isRefreshing,
            lastSyncTime = uiState.lastSyncTime,
            isAdmin = isAdmin,
            onRefresh = { viewModel.refreshFromCloud() },
            onAddRecord = { viewModel.addManualAttendanceRecord(it) },
            onDeleteRecord = { viewModel.deleteAttendanceRecord(it) },
          )
        }

        BentoTab.SETTINGS -> {
          SettingsScreen(
            profile = uiState.workerProfile,
            selectedSite = uiState.selectedSite,
            isOutsideSimulation = uiState.isOutsideSimulation,
            fcmStatus = uiState.fcmStatus,
            fcmToken = uiState.fcmToken,
            activityLogs = activityLogs,
            currentUser = uiState.currentUserAccount,
            mapTilerApiKey = uiState.mapTilerApiKey,
            onUpdateMapTilerKey = { viewModel.setMapTilerApiKey(it) },
            onToggleSimulator = { viewModel.toggleGeofenceSimulation(it) },
            onResetToday = { viewModel.resetToday() },
            onResetDeviceBinding = { viewModel.resetCurrentDeviceBinding() },
            onLogout = { viewModel.logout() },
          )
        }
      }
    }
  }
}

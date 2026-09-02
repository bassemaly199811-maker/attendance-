package com.example.ui.screens

import com.example.BuildConfig
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.AttendanceStatus
import com.example.data.model.WorkSite
import com.example.ui.components.BentoActionButton
import com.example.ui.components.BentoEarlyCheckoutWarningDialog
import com.example.ui.components.BentoErrorGuidanceDialog
import com.example.ui.components.BentoGeofenceCoordinatesCard
import com.example.ui.components.BentoGeofenceMapCard
import com.example.ui.components.BentoHeader
import com.example.ui.components.BentoHeroCard
import com.example.ui.components.BentoIdentityCard
import com.example.ui.components.BentoNotificationBanner
import com.example.ui.components.BentoWorkSiteCard
import com.example.ui.components.CheckoutSuccessDialog
import com.example.ui.components.CheckInSuccessDialog
import com.example.ui.viewmodel.AttendanceUiState

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import com.example.data.model.WorkerEntity
import com.example.ui.theme.BentoBlueContainer
import com.example.ui.theme.BentoBluePrimary
import com.example.ui.theme.BentoLilac
import com.example.ui.theme.BentoLilacBorder
import com.example.ui.theme.BentoLilacText
import com.example.ui.theme.BentoOutline
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.BentoTileGray

@Composable
fun HomeScreen(
  uiState: AttendanceUiState,
  allSites: List<WorkSite>,
  workers: List<WorkerEntity> = emptyList(),
  shiftEndTime: String = "04:30 PM",
  onPrimaryAction: () -> Unit,
  onConfirmEarlyCheckout: () -> Unit = {},
  onDismissEarlyCheckoutWarning: () -> Unit = {},
  onSiteSelect: (WorkSite) -> Unit,
  onSelectWorker: (WorkerEntity) -> Unit = {},
  onSelectWorkerByName: (String) -> Unit = {},
  onToggleSimulator: ((Boolean) -> Unit)? = null,
  onLivePhotoCaptured: (Bitmap) -> Unit,
  onCameraDismissed: () -> Unit = {},
  onDismissNotification: () -> Unit,
  onDismissErrorDialog: () -> Unit = {},
  onDismissCheckoutSuccess: () -> Unit = {},
  onDismissCheckinSuccess: () -> Unit = {},
  onNavigateToDashboard: (() -> Unit)? = null,
  onUpdateGps: ((Double, Double, Double) -> Unit)? = null,
  onRefreshGps: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  var showSiteDialog by remember { mutableStateOf(false) }
  val scrollState = rememberScrollState()
  val context = LocalContext.current

  // 0. Location Permission Launcher (Auto GPS sync)
  val locationPermissionLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
      val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
      if (granted) {
        com.example.service.LocationHelper.getCurrentLocation(context) { coords ->
          onUpdateGps?.invoke(coords.latitude, coords.longitude, coords.accuracy)
        }
      }
    }

  androidx.compose.runtime.LaunchedEffect(Unit) {
    if (com.example.service.LocationHelper.hasLocationPermission(context)) {
      com.example.service.LocationHelper.getCurrentLocation(context) { coords ->
        onUpdateGps?.invoke(coords.latitude, coords.longitude, coords.accuracy)
      }
    } else {
      locationPermissionLauncher.launch(
        arrayOf(
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_COARSE_LOCATION
        )
      )
    }
  }

  // 1. Direct Camera Photo Launcher (Taking a picture directly with the camera - NOT gallery)
  val cameraLauncher =
    rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
      if (bitmap != null) {
        onLivePhotoCaptured(bitmap)
      } else {
        // Camera intent closed without taking a picture -> strict non-null enforcement
        onCameraDismissed()
      }
    }

  // 2. Camera Permission Launcher
  val permissionLauncher =
    rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
      if (isGranted) {
        cameraLauncher.launch(null)
      } else {
        onCameraDismissed()
      }
    }

  val handleCameraClick = {
    val permission = Manifest.permission.CAMERA
    if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
      cameraLauncher.launch(null)
    } else {
      permissionLauncher.launch(permission)
    }
  }

  Box(
    modifier = modifier.fillMaxSize().testTag("home_screen")
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .verticalScroll(scrollState)
          .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Spacer(modifier = Modifier.height(4.dp))

      // 1. Bento Header with Online Status
      BentoHeader(
        profile = uiState.workerProfile,
        isOnline = uiState.isOnline,
        onOpenDashboard = onNavigateToDashboard,
      )

      // Notification / Error Banner if any
      BentoNotificationBanner(
        message = uiState.notificationMessage,
        isError = uiState.isNotificationError,
        onDismiss = onDismissNotification,
      )

      // 2. Hero Bento Card (Main Status)
      BentoHeroCard(
        status = uiState.currentStatus,
        lastOperation = uiState.lastOperationTime,
      )

      // 3. Work Site Selection Card
      BentoWorkSiteCard(
        siteName = uiState.selectedSite.name,
        onClick = { showSiteDialog = true },
      )

      // 4. GPS Geofence & Coordinates Telemetry Card (Clean Coordinates & Geofence Status, No Map)
      BentoGeofenceCoordinatesCard(
        siteName = uiState.selectedSite.name,
        latitude = uiState.selectedSite.latitude,
        longitude = uiState.selectedSite.longitude,
        radiusMeters = uiState.selectedSite.radiusMeters,
        isInside = uiState.isInsideGeofence,
        distanceMeters = uiState.currentDistanceMeters,
        deviceLatitude = uiState.deviceLatitude,
        deviceLongitude = uiState.deviceLongitude,
        accuracyMeters = uiState.accuracyMeters,
        isSearchingLocation = uiState.isSearchingLocation,
        locationSearchError = uiState.locationSearchError,
        locationSearchSuccess = uiState.locationSearchSuccess,
        isOnline = uiState.isOnline,
        onRefreshGps = onRefreshGps,
      )

      // 5. Identity Confirmation Bento Card (Direct Live Camera & Google Drive Sync)
      val currentActivePhotoCaptured =
        when (uiState.currentStatus) {
          AttendanceStatus.CHECKED_IN -> {
            uiState.capturedCheckOutBitmap != null || !uiState.checkOutPhotoUri.isNullOrBlank()
          }
          AttendanceStatus.CHECKED_OUT -> {
            true
          }
          AttendanceStatus.NOT_CHECKED_IN -> {
            uiState.capturedCheckInBitmap != null || !uiState.checkInPhotoUri.isNullOrBlank()
          }
        }

      val currentActiveBitmap =
        when (uiState.currentStatus) {
          AttendanceStatus.CHECKED_IN -> {
            uiState.capturedCheckOutBitmap
          }
          AttendanceStatus.CHECKED_OUT -> {
            uiState.capturedCheckOutBitmap ?: uiState.capturedCheckInBitmap
          }
          AttendanceStatus.NOT_CHECKED_IN -> {
            uiState.capturedCheckInBitmap
          }
        }

      val currentActivePhotoUri =
        when (uiState.currentStatus) {
          AttendanceStatus.CHECKED_IN -> uiState.checkOutPhotoUri
          AttendanceStatus.CHECKED_OUT -> uiState.checkOutPhotoUri ?: uiState.checkInPhotoUri
          AttendanceStatus.NOT_CHECKED_IN -> uiState.checkInPhotoUri
        }

      val currentActivePhotoBase64 =
        when (uiState.currentStatus) {
          AttendanceStatus.CHECKED_IN -> uiState.checkOutPhotoBase64
          AttendanceStatus.CHECKED_OUT -> uiState.checkOutPhotoBase64 ?: uiState.checkInPhotoBase64
          AttendanceStatus.NOT_CHECKED_IN -> uiState.checkInPhotoBase64
        }

      BentoIdentityCard(
        photoCaptured = currentActivePhotoCaptured,
        capturedBitmap = currentActiveBitmap,
        photoUri = currentActivePhotoUri,
        photoBase64 = currentActivePhotoBase64,
        status = uiState.currentStatus,
        isUploadingToDrive = uiState.isUploadingToDrive,
        onCaptureClick = handleCameraClick,
      )

      // 5. Action Button with 30-second cooldown & Live Photo Requirement
      BentoActionButton(
        status = uiState.currentStatus,
        photoCaptured = currentActivePhotoCaptured,
        isProcessing = uiState.isProcessing,
        cooldownRemainingSeconds = uiState.cooldownRemainingSeconds,
        onClick = {
          if (!currentActivePhotoCaptured && uiState.currentStatus != AttendanceStatus.CHECKED_OUT) {
            handleCameraClick()
          } else {
            onPrimaryAction()
          }
        },
      )

      Spacer(modifier = Modifier.height(8.dp))
    }

    // Comprehensive Error & Guidance Dialog
    if (uiState.showErrorDialog) {
      BentoErrorGuidanceDialog(
        title = uiState.errorTitle,
        message = uiState.notificationMessage,
        guidance = uiState.errorGuidance,
        onDismiss = onDismissErrorDialog,
      )
    }

    // Early Departure Warning Dialog
    if (uiState.showEarlyCheckoutWarning) {
      BentoEarlyCheckoutWarningDialog(
        shiftEndTime = shiftEndTime,
        onConfirm = onConfirmEarlyCheckout,
        onDismiss = onDismissEarlyCheckoutWarning,
      )
    }

    // Checkout Success Encouragement Pop-up (4 seconds auto-dismiss with random quote)
    if (uiState.showCheckoutSuccessPopup) {
      CheckoutSuccessDialog(
        quote = uiState.checkoutRandomQuote,
        workerName = uiState.checkoutSuccessWorkerName,
        checkOutTime = uiState.checkoutSuccessTime,
        onDismiss = onDismissCheckoutSuccess,
      )
    }

    // Checkin Success Encouragement Pop-up (4 seconds auto-dismiss with random quote)
    if (uiState.showCheckinSuccessPopup) {
      CheckInSuccessDialog(
        quote = uiState.checkinRandomQuote,
        workerName = uiState.checkoutSuccessWorkerName.ifBlank { uiState.workerProfile.fullName },
        checkInTime = uiState.lastOperationTime,
        onDismiss = onDismissCheckinSuccess,
      )
    }

    // Site Picker Dialog
    if (showSiteDialog) {
      AlertDialog(
        onDismissRequest = { showSiteDialog = false },
        containerColor = Color.White,
        shape = RoundedCornerShape(18.dp),
        title = {
          Text(text = "Select Work Site", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        },
        text = {
          Column {
            allSites.forEach { site ->
              Row(
                modifier =
                  Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
              ) {
                RadioButton(
                  selected = site.id == uiState.selectedSite.id,
                  onClick = {
                    onSiteSelect(site)
                    showSiteDialog = false
                  },
                )
                Text(
                  text = "${site.name} (${site.radiusMeters}m)",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Medium,
                  color = Color.Black,
                  modifier = Modifier.padding(start = 8.dp),
                )
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { showSiteDialog = false }) {
            Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
          }
        },
      )
    }
  }
}

package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.util.exportAttendanceToCsv
import com.example.ui.components.EditWorkerLeaveBalanceDialog
import com.example.ui.components.AdminSitePickerMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.DocumentAlert
import com.example.data.model.DocumentExpiryStatus
import com.example.data.model.DocumentType
import com.example.data.model.LeaveBalance
import com.example.data.model.LeaveRequest
import com.example.data.model.WorkShiftConfig
import com.example.data.model.WorkSite
import com.example.data.model.WorkerEntity
import com.example.data.model.WorkerOverview
import com.example.data.model.calculateDaysRemaining
import com.example.data.model.calculateExpiryStatus
import com.example.data.model.getWorkerDocumentAlerts
import com.example.ui.theme.BentoBlueContainer
import com.example.ui.theme.BentoBluePrimary
import com.example.ui.theme.BentoError
import com.example.ui.theme.BentoLilac
import com.example.ui.theme.BentoLilacText
import com.example.ui.theme.BentoOutline
import com.example.ui.theme.BentoSuccess
import com.example.ui.theme.BentoSuccessContainer
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.BentoWarning
import com.example.ui.theme.BentoWarningContainer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Calculates days remaining until target date (yyyy-MM-dd).
 * Returns positive days if in future, 0 or negative if expired.
 */
fun calculateDaysRemaining(dateStr: String): Long? {
  if (dateStr.isBlank()) return null
  return try {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val targetDate = sdf.parse(dateStr.trim()) ?: return null
    val calNow = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val calTarget = Calendar.getInstance().apply {
      time = targetDate
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    (calTarget.timeInMillis - calNow.timeInMillis) / (1000 * 60 * 60 * 24)
  } catch (_: Exception) {
    null
  }
}

/**
 * Helper to scan all workers and extract document expiry alerts.
 */
fun getDocumentAlerts(workers: List<WorkerEntity>): List<DocumentAlert> {
  val alerts = mutableListOf<DocumentAlert>()

  workers.forEach { worker ->
    // 1. Iqama Check
    if (worker.iqamaEndDate.isNotBlank()) {
      val days = calculateDaysRemaining(worker.iqamaEndDate)
      if (days != null) {
        val status = when {
          days <= 0 -> DocumentExpiryStatus.EXPIRED
          days <= 14 -> DocumentExpiryStatus.EXPIRING_SOON // 2 weeks warning!
          days <= 30 -> DocumentExpiryStatus.EXPIRING_MONTH
          else -> DocumentExpiryStatus.VALID
        }
        if (status == DocumentExpiryStatus.EXPIRED || status == DocumentExpiryStatus.EXPIRING_SOON) {
          alerts.add(
            DocumentAlert(
              workerId = worker.id,
              workerName = worker.fullName,
              workerRole = worker.role,
              documentType = DocumentType.IQAMA,
              documentNumber = worker.iqamaNumber.ifBlank { worker.nationalId },
              providerOrNote = "Iqama / Residency",
              startDate = worker.iqamaStartDate,
              endDate = worker.iqamaEndDate,
              daysRemaining = days,
              status = status,
            )
          )
        }
      }
    }

    // 2. Medical Insurance Check
    if (worker.insuranceEndDate.isNotBlank()) {
      val days = calculateDaysRemaining(worker.insuranceEndDate)
      if (days != null) {
        val status = when {
          days <= 0 -> DocumentExpiryStatus.EXPIRED
          days <= 14 -> DocumentExpiryStatus.EXPIRING_SOON // 2 weeks warning!
          days <= 30 -> DocumentExpiryStatus.EXPIRING_MONTH
          else -> DocumentExpiryStatus.VALID
        }
        if (status == DocumentExpiryStatus.EXPIRED || status == DocumentExpiryStatus.EXPIRING_SOON) {
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
  }

  return alerts.sortedBy { it.daysRemaining }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HrManagementScreen(
  workers: List<WorkerOverview>,
  rawWorkers: List<WorkerEntity>,
  sites: List<WorkSite>,
  records: List<AttendanceRecord>,
  shiftConfig: WorkShiftConfig,
  mapTilerApiKey: String = "",
  deviceLatitude: Double = 0.0,
  deviceLongitude: Double = 0.0,
  isDeviceLocationReady: Boolean = false,
  isOnline: Boolean = true,
  initialSearchQuery: String = "",
  initialOpenCard: String? = null,
  leaveRequests: List<LeaveRequest> = emptyList(),
  leaveBalances: List<LeaveBalance> = emptyList(),
  onBack: () -> Unit,
  onAddNewSite: (name: String, lat: Double, lng: Double, radius: Int, address: String) -> Unit,
  onUpdateSite: (WorkSite) -> Unit,
  onDeleteSite: (String) -> Unit,
  onAddNewWorker: (
    fullName: String,
    role: String,
    siteId: String,
    siteName: String,
    nationalId: String,
    phone: String,
    deviceModel: String,
    isApproved: Boolean,
    assignedSiteIds: String,
    assignedSiteNames: String,
    iqamaNumber: String,
    iqamaStartDate: String,
    iqamaEndDate: String,
    insuranceNumber: String,
    insuranceProvider: String,
    insuranceStartDate: String,
    insuranceEndDate: String,
    passportNumber: String,
    nationality: String,
    contractEndDate: String,
    salary: Double,
    hireDate: String,
    employmentEndDate: String,
    annualLeaveTotal: Double,
    casualLeaveTotal: Double,
    sickLeaveTotal: Double,
  ) -> Unit,
  onUpdateWorker: (WorkerEntity) -> Unit,
  onDeleteWorker: (String) -> Unit,
  onUpdateLeaveBalance: (
    workerId: String,
    annualTotal: Double,
    casualTotal: Double,
    sickTotal: Double,
    annualUsed: Double?,
    casualUsed: Double?,
    sickUsed: Double?,
  ) -> Unit = { _, _, _, _, _, _, _ -> },
  onUpdateWorkerDocuments: (
    workerId: String,
    iqamaNumber: String,
    iqamaStartDate: String,
    iqamaEndDate: String,
    insuranceNumber: String,
    insuranceProvider: String,
    insuranceStartDate: String,
    insuranceEndDate: String,
    passportNumber: String,
    nationality: String,
    contractEndDate: String,
    salary: Double,
    hireDate: String,
    employmentEndDate: String,
  ) -> Unit,
  onUpdateShiftConfig: (WorkShiftConfig) -> Unit,
  modifier: Modifier = Modifier,
) {
  val scrollState = rememberScrollState()

  // Pop-up modal visibility states for each card
  var showUsersInfoModal by remember { mutableStateOf(initialOpenCard == "USERS_INFO") }
  var showWorkersModal by remember { mutableStateOf(initialOpenCard == "WORKERS") }
  var showPaperManagementModal by remember { mutableStateOf(initialOpenCard == "PAPER" || initialSearchQuery.isNotBlank()) }
  var showSitesModal by remember { mutableStateOf(initialOpenCard == "SITES") }
  var showReportModal by remember { mutableStateOf(initialOpenCard == "REPORT") }
  var showShiftModal by remember { mutableStateOf(initialOpenCard == "SHIFT") }

  // Sub-dialogs
  var showAddWorkerDialog by remember { mutableStateOf(false) }
  var workerToEdit by remember { mutableStateOf<WorkerEntity?>(null) }
  var workerToDelete by remember { mutableStateOf<WorkerOverview?>(null) }

  var showAddSiteDialog by remember { mutableStateOf(false) }
  var siteToEdit by remember { mutableStateOf<WorkSite?>(null) }
  var siteToDelete by remember { mutableStateOf<WorkSite?>(null) }

  var workerForDocumentEdit by remember { mutableStateOf<WorkerEntity?>(null) }
  var workerForLeaveBalanceEdit by remember { mutableStateOf<Pair<WorkerEntity, LeaveBalance>?>(null) }

  // Expiration Alerts
  val alerts = remember(rawWorkers) { getDocumentAlerts(rawWorkers) }
  val expiringSoonCount = alerts.count { it.status == DocumentExpiryStatus.EXPIRING_SOON }
  val expiredCount = alerts.count { it.status == DocumentExpiryStatus.EXPIRED }

  Column(
    modifier =
      modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Spacer(modifier = Modifier.height(4.dp))

    // 1. Top Header with Back Navigation
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        IconButton(
          onClick = onBack,
          modifier =
            Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(Color.White)
              .testTag("hr_back_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back to Dashboard",
            tint = Color.Black,
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "HR Management",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
          )
          Text(
            text = "Workers, Work Sites, Geofences, Shifts & Paper Records",
            fontSize = 11.sp,
            color = BentoTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = BentoBlueContainer,
        modifier = Modifier.padding(start = 6.dp),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Default.Business,
            contentDescription = null,
            tint = BentoBluePrimary,
            modifier = Modifier.size(14.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${rawWorkers.size} Workers",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = BentoBluePrimary,
          )
        }
      }
    }

    // 2. High-level Summary Pill Grid
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      HrQuickStatPill(
        title = "Total Workers",
        value = "${rawWorkers.size}",
        icon = Icons.Default.People,
        containerColor = BentoBluePrimary,
        contentColor = Color.White,
        modifier = Modifier.weight(1f),
        onClick = { showWorkersModal = true },
      )
      HrQuickStatPill(
        title = "Work Sites",
        value = "${sites.size}",
        icon = Icons.Default.LocationOn,
        containerColor = BentoSuccessContainer,
        contentColor = BentoSuccess,
        modifier = Modifier.weight(1f),
        onClick = { showSitesModal = true },
      )
      HrQuickStatPill(
        title = "Paper Alerts",
        value = "${alerts.size}",
        icon = if (alerts.isNotEmpty()) Icons.Default.Warning else Icons.Default.CheckCircle,
        containerColor = if (alerts.isNotEmpty()) BentoWarningContainer else Color(0xFFE8F5E9),
        contentColor = if (alerts.isNotEmpty()) BentoWarning else BentoSuccess,
        modifier = Modifier.weight(1f),
        onClick = { showPaperManagementModal = true },
      )
    }

    // 3. Document Expirations Alert Banner if any
    if (alerts.isNotEmpty()) {
      Card(
        modifier = Modifier.fillMaxWidth().clickable { showPaperManagementModal = true },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.5f)),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
              modifier = Modifier.size(34.dp).clip(CircleShape).background(Color(0xFFFFECB3)),
              contentAlignment = Alignment.Center,
            ) {
              Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "$expiringSoonCount Expiring in 2 Weeks • $expiredCount Expired Documents",
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                color = Color(0xFFBF360C),
              )
              Text(
                text = "Tap to review worker Iqamas and medical insurance policies",
                fontSize = 10.5.sp,
                color = Color(0xFF8D6E63),
              )
            }
          }
          Icon(Icons.Default.Navigation, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
        }
      }
    }

    // ==========================================
    // --- 4. HR MANAGEMENT MODULAR BENTO CARDS ---
    // ==========================================

    // CARD 0: Users Info & Comprehensive Employee Records
    HrModularMenuCard(
      title = "Users Info & Employee Profiles",
      titleAr = "معلومات المستخدمين وسجلات الموظفين الشاملة",
      description = "Search all employees, view profile details, inspect leave balance and request histories with messages, check attendance records with photos and times, and edit documents.",
      icon = Icons.Default.AccountBox,
      accentColor = Color(0xFF0288D1),
      badgeText = "${rawWorkers.size} Employees",
      actionText = "Open Users Info",
      onActionClick = { showUsersInfoModal = true },
      onCardClick = { showUsersInfoModal = true },
    )

    // CARD 1: Workers Directory & Management
    HrModularMenuCard(
      title = "Workers Directory & Management",
      titleAr = "Staff profiles & biometric binding",
      description = "Add new workers, update roles, assign work sites, and manage approved biometric devices.",
      icon = Icons.Default.People,
      accentColor = BentoBluePrimary,
      badgeText = "${rawWorkers.size} Active",
      actionText = "+ Add New Worker",
      onActionClick = { showAddWorkerDialog = true },
      onCardClick = { showWorkersModal = true },
    )

    // CARD 2: Paper & Document Management
    HrModularMenuCard(
      title = "Paper & Document Management",
      titleAr = "Iqama, insurance & residency expiry",
      description = "Search workers by name, view Iqama issue & expiry dates, medical insurance details, and track 2-week expiration warnings.",
      icon = Icons.Default.Description,
      accentColor = if (alerts.isNotEmpty()) BentoWarning else BentoSuccess,
      badgeText = if (alerts.isNotEmpty()) "${alerts.size} Expirations Tracked" else "All Valid",
      actionText = "Open Paper Records",
      onActionClick = { showPaperManagementModal = true },
      onCardClick = { showPaperManagementModal = true },
    )

    // CARD 3: Work Sites & Geofences
    HrModularMenuCard(
      title = "Work Sites & Geofences",
      titleAr = "Locations, GPS & geofence perimeter",
      description = "Register job sites, set GPS coordinates and geofence radii (meters), and view sites on the live map.",
      icon = Icons.Default.LocationOn,
      accentColor = Color(0xFF00897B),
      badgeText = "${sites.size} Work Sites",
      actionText = "+ Add New Site",
      onActionClick = { showAddSiteDialog = true },
      onCardClick = { showSitesModal = true },
    )

    // CARD 4: Attendance & Performance Analytics Report
    HrModularMenuCard(
      title = "Attendance & Performance Report",
      titleAr = "Analytics, compliance & export logs",
      description = "Analyze daily/monthly work duration, late arrivals, on-site compliance rates, and export attendance analytics.",
      icon = Icons.Default.Assignment,
      accentColor = Color(0xFF7E57C2),
      badgeText = "${records.size} Records",
      actionText = "View Analytics",
      onActionClick = { showReportModal = true },
      onCardClick = { showReportModal = true },
    )

    // CARD 5: Official Shift Schedule & Timings
    HrModularMenuCard(
      title = "Official Shift Hours & Timings",
      titleAr = "Working hours & late arrival tolerance",
      description = "Configure shift start & end times (${shiftConfig.startTime} - ${shiftConfig.endTime}) and the late grace period (${shiftConfig.gracePeriodMinutes} mins).",
      icon = Icons.Default.Schedule,
      accentColor = Color(0xFFE65100),
      badgeText = "${shiftConfig.startTime} - ${shiftConfig.endTime}",
      actionText = "Edit Timings",
      onActionClick = { showShiftModal = true },
      onCardClick = { showShiftModal = true },
    )

    Spacer(modifier = Modifier.height(24.dp))
  }

  // ==========================================
  // --- MODAL DIALOG 0: USERS INFO ---
  // ==========================================
  if (showUsersInfoModal) {
    UsersInfoModalDialog(
      workers = rawWorkers,
      records = records,
      leaveRequests = leaveRequests,
      leaveBalances = leaveBalances,
      isOnline = isOnline,
      onDismiss = { showUsersInfoModal = false },
      onUpdateWorkerDocuments = onUpdateWorkerDocuments,
      onUpdateLeaveBalance = onUpdateLeaveBalance,
    )
  }

  // ==========================================
  // --- MODAL DIALOG 1: WORKERS DIRECTORY ---
  // ==========================================
  if (showWorkersModal) {
    WorkersDirectoryModalDialog(
      workers = rawWorkers,
      overviewWorkers = workers,
      sites = sites,
      leaveBalances = leaveBalances,
      onDismiss = { showWorkersModal = false },
      onAddWorkerClick = { showAddWorkerDialog = true },
      onEditWorkerClick = { workerToEdit = it },
      onDeleteWorkerClick = { overview -> workerToDelete = overview },
      onEditLeaveBalanceClick = { worker, balance ->
        workerForLeaveBalanceEdit = Pair(worker, balance)
      },
    )
  }

  // ==========================================
  // --- MODAL DIALOG 2: PAPER MANAGEMENT ---
  // ==========================================
  if (showPaperManagementModal) {
    PaperManagementModalDialog(
      workers = rawWorkers,
      initialSearch = initialSearchQuery,
      onDismiss = { showPaperManagementModal = false },
      onEditDocuments = { worker -> workerForDocumentEdit = worker },
    )
  }

  // ==========================================
  // --- MODAL DIALOG 3: SITES & GEOFENCES ---
  // ==========================================
  if (showSitesModal) {
    WorkSitesModalDialog(
      sites = sites,
      mapTilerApiKey = mapTilerApiKey,
      onDismiss = { showSitesModal = false },
      onAddSiteClick = { showAddSiteDialog = true },
      onEditSiteClick = { siteToEdit = it },
      onDeleteSiteClick = { siteToDelete = it },
    )
  }

  // ==========================================
  // --- MODAL DIALOG 4: ATTENDANCE REPORT ---
  // ==========================================
  if (showReportModal) {
    AttendanceReportModalDialog(
      records = records,
      workers = rawWorkers,
      shiftConfig = shiftConfig,
      onDismiss = { showReportModal = false },
    )
  }

  // ==========================================
  // --- MODAL DIALOG 5: SHIFT CONFIGURATION ---
  // ==========================================
  if (showShiftModal) {
    ShiftScheduleConfigDialog(
      initialConfig = shiftConfig,
      onDismiss = { showShiftModal = false },
      onConfirm = { newConfig ->
        onUpdateShiftConfig(newConfig)
        showShiftModal = false
      },
    )
  }

  // ==========================================
  // --- SUB-DIALOG: ADD WORKER FORM ---
  // ==========================================
  if (showAddWorkerDialog) {
    WorkerFormDialog(
      title = "Register New Field Worker",
      sites = sites,
      initialWorker = null,
      initialLeaveBalance = null,
      onDismiss = { showAddWorkerDialog = false },
      onConfirm = { name, role, siteId, siteName, nationalId, phone, device, isApproved, assignedIds, assignedNames, iqamaNum, iqamaStart, iqamaEnd, insNum, insProvider, insStart, insEnd, passport, nationality, contractEnd, salary, hireDate, employmentEndDate, annualLeave, casualLeave, sickLeave ->
        onAddNewWorker(
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
        showAddWorkerDialog = false
      },
    )
  }

  // ==========================================
  // --- SUB-DIALOG: EDIT WORKER FORM ---
  // ==========================================
  workerToEdit?.let { worker ->
    val currentWorkerLeave = leaveBalances.find { it.workerId == worker.id } ?: LeaveBalance(workerId = worker.id)
    WorkerFormDialog(
      title = "Edit Worker Details",
      sites = sites,
      initialWorker = worker,
      initialLeaveBalance = currentWorkerLeave,
      onDismiss = { workerToEdit = null },
      onConfirm = { name, role, siteId, siteName, nationalId, phone, device, isApproved, assignedIds, assignedNames, iqamaNum, iqamaStart, iqamaEnd, insNum, insProvider, insStart, insEnd, passport, nationality, contractEnd, salary, hireDate, employmentEndDate, annualLeave, casualLeave, sickLeave ->
        onUpdateWorker(
          worker.copy(
            fullName = name,
            role = role,
            siteId = siteId,
            siteName = siteName,
            nationalId = nationalId,
            phoneNumber = phone,
            deviceModel = device,
            isDeviceApproved = isApproved,
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
          )
        )
        onUpdateLeaveBalance(
          worker.id,
          annualLeave,
          casualLeave,
          sickLeave,
          null,
          null,
          null,
        )
        workerToEdit = null
      },
    )
  }

  // ==========================================
  // --- SUB-DIALOG: EDIT LEAVE BALANCE ---
  // ==========================================
  workerForLeaveBalanceEdit?.let { (worker, balance) ->
    EditWorkerLeaveBalanceDialog(
      worker = worker,
      currentBalance = balance,
      onDismiss = { workerForLeaveBalanceEdit = null },
      onSave = { annualTotal, casualTotal, sickTotal, annualUsed, casualUsed, sickUsed ->
        onUpdateLeaveBalance(
          worker.id,
          annualTotal,
          casualTotal,
          sickTotal,
          annualUsed,
          casualUsed,
          sickUsed,
        )
        workerForLeaveBalanceEdit = null
      },
    )
  }

  // ==========================================
  // --- SUB-DIALOG: DELETE WORKER ---
  // ==========================================
  workerToDelete?.let { worker ->
    AlertDialog(
      onDismissRequest = { workerToDelete = null },
      title = { Text("Delete Worker", fontWeight = FontWeight.Bold) },
      text = { Text("Are you sure you want to delete worker (${worker.fullName}) from the system?") },
      confirmButton = {
        Button(
          onClick = {
            onDeleteWorker(worker.id)
            workerToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = BentoError),
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { workerToDelete = null }) {
          Text("Cancel")
        }
      },
    )
  }

  // ==========================================
  // --- SUB-DIALOG: EDIT WORKER DOCUMENTS ---
  // ==========================================
  workerForDocumentEdit?.let { worker ->
    EditWorkerDocumentsDialog(
      worker = worker,
      onDismiss = { workerForDocumentEdit = null },
      onSave = { iqamaNum, iqamaStart, iqamaEnd, insNum, insProvider, insStart, insEnd, passport, nationality, contractEnd, salary, hireDate, employmentEndDate ->
        onUpdateWorkerDocuments(
          worker.id,
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
        workerForDocumentEdit = null
      },
    )
  }

  // ==========================================
  // --- SUB-DIALOG: ADD SITE FORM ---
  // ==========================================
  if (showAddSiteDialog) {
    WorkSiteFormDialog(
      title = "Add New Work Site",
      initialSite = null,
      currentDeviceLat = deviceLatitude,
      currentDeviceLng = deviceLongitude,
      isLocationReady = isDeviceLocationReady,
      isOnline = isOnline,
      onDismiss = { showAddSiteDialog = false },
      onConfirm = { name, lat, lng, radius, address ->
        onAddNewSite(name, lat, lng, radius, address)
        showAddSiteDialog = false
      },
    )
  }

  // ==========================================
  // --- SUB-DIALOG: EDIT SITE FORM ---
  // ==========================================
  siteToEdit?.let { site ->
    WorkSiteFormDialog(
      title = "Edit Work Site",
      initialSite = site,
      currentDeviceLat = deviceLatitude,
      currentDeviceLng = deviceLongitude,
      isLocationReady = isDeviceLocationReady,
      isOnline = isOnline,
      onDismiss = { siteToEdit = null },
      onConfirm = { name, lat, lng, radius, address ->
        onUpdateSite(
          site.copy(
            name = name,
            latitude = lat,
            longitude = lng,
            radiusMeters = radius,
            address = address,
          )
        )
        siteToEdit = null
      },
    )
  }

  // ==========================================
  // --- SUB-DIALOG: DELETE SITE ---
  // ==========================================
  siteToDelete?.let { site ->
    AlertDialog(
      onDismissRequest = { siteToDelete = null },
      title = { Text("Delete Work Site", fontWeight = FontWeight.Bold) },
      text = { Text("Are you sure you want to delete work site (${site.name})? Workers assigned to this site will need reassignment.") },
      confirmButton = {
        Button(
          onClick = {
            onDeleteSite(site.id)
            siteToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = BentoError),
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { siteToDelete = null }) {
          Text("Cancel")
        }
      },
    )
  }
}

// ==========================================
// --- REUSABLE HR MODULAR CARD ---
// ==========================================
@Composable
private fun HrModularMenuCard(
  title: String,
  titleAr: String,
  description: String,
  icon: ImageVector,
  accentColor: Color,
  badgeText: String,
  actionText: String,
  onActionClick: () -> Unit,
  onCardClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.fillMaxWidth().clickable { onCardClick() },
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, BentoOutline),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Box(
            modifier = Modifier.size(38.dp).clip(CircleShape).background(accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = title,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color.Black,
            )
            Text(
              text = titleAr,
              fontSize = 11.sp,
              color = BentoTextSecondary,
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = accentColor.copy(alpha = 0.1f),
        ) {
          Text(
            text = badgeText,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
      }

      Text(
        text = description,
        fontSize = 12.sp,
        color = BentoTextSecondary,
        lineHeight = 16.sp,
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Tap to open modal window ➔",
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          color = accentColor,
        )

        Button(
          onClick = onActionClick,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = accentColor),
          modifier = Modifier.height(34.dp),
        ) {
          Text(actionText, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
      }
    }
  }
}

@Composable
private fun HrQuickStatPill(
  title: String,
  value: String,
  icon: ImageVector,
  containerColor: Color,
  contentColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.clickable { onClick() },
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = contentColor)
      }
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = title,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Medium,
        color = contentColor.copy(alpha = 0.9f),
        textAlign = TextAlign.Center,
        maxLines = 1,
      )
    }
  }
}

// ==========================================
// --- POP-UP MODAL 1: WORKERS DIRECTORY ---
// ==========================================
@Composable
fun WorkersDirectoryModalDialog(
  workers: List<WorkerEntity>,
  overviewWorkers: List<WorkerOverview>,
  sites: List<WorkSite>,
  leaveBalances: List<LeaveBalance> = emptyList(),
  onDismiss: () -> Unit,
  onAddWorkerClick: () -> Unit,
  onEditWorkerClick: (WorkerEntity) -> Unit,
  onDeleteWorkerClick: (WorkerOverview) -> Unit,
  onEditLeaveBalanceClick: (WorkerEntity, LeaveBalance) -> Unit = { _, _ -> },
) {
  var searchQuery by remember { mutableStateOf("") }
  var isWorkerDropdownExpanded by remember { mutableStateOf(false) }

  val filteredWorkers = remember(workers, searchQuery) {
    if (searchQuery.isBlank()) workers
    else workers.filter {
      it.fullName.contains(searchQuery, ignoreCase = true) ||
        it.role.contains(searchQuery, ignoreCase = true) ||
        it.siteName.contains(searchQuery, ignoreCase = true) ||
        it.nationalId.contains(searchQuery, ignoreCase = true)
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = true),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.5f))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onDismiss,
        ),
      contentAlignment = Alignment.Center,
    ) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp)
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { /* keep dialog open when clicking inside */ },
          ),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8F9FA),
        border = BorderStroke(1.dp, BentoOutline),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          // Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(BentoBlueContainer),
                contentAlignment = Alignment.Center,
              ) {
                Icon(Icons.Default.People, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Workers Directory (${workers.size})",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Black,
                )
                Text(
                  text = "Add, edit, or manage worker site assignments",
                  fontSize = 11.sp,
                  color = BentoTextSecondary,
                )
              }
            }

            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
            }
          }

          // Search Bar & Worker Picker Dropdown
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            OutlinedTextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              placeholder = { Text("Search by name or role...", fontSize = 12.sp) },
              leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BentoTextSecondary) },
              trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                  IconButton(onClick = { searchQuery = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = BentoTextSecondary)
                  }
                }
              },
              shape = RoundedCornerShape(14.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
              ),
              singleLine = true,
              modifier = Modifier.weight(1f),
            )

            // Worker Quick Picker Selector Button
            OutlinedButton(
              onClick = { isWorkerDropdownExpanded = true },
              shape = RoundedCornerShape(14.dp),
              border = BorderStroke(1.dp, BentoBluePrimary.copy(alpha = 0.4f)),
              colors = ButtonDefaults.outlinedButtonColors(containerColor = BentoBlueContainer.copy(alpha = 0.5f)),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
              modifier = Modifier.height(52.dp),
            ) {
              Icon(Icons.Default.People, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Staff", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = BentoBluePrimary)
              Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(16.dp))
            }

            if (isWorkerDropdownExpanded) {
              AlertDialog(
                onDismissRequest = { isWorkerDropdownExpanded = false },
                properties = DialogProperties(dismissOnClickOutside = true, dismissOnBackPress = true),
                containerColor = Color.White,
                title = {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier.size(34.dp).clip(CircleShape).background(BentoBlueContainer),
                      contentAlignment = Alignment.Center,
                    ) {
                      Icon(Icons.Default.People, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Staff Member", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                  }
                },
                text = {
                  Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    // "Show All" option
                    Surface(
                      shape = RoundedCornerShape(10.dp),
                      color = if (searchQuery.isEmpty()) BentoBlueContainer else Color(0xFFF5F5F5),
                      border = BorderStroke(1.dp, if (searchQuery.isEmpty()) BentoBluePrimary else Color(0xFFE0E0E0)),
                      modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                          searchQuery = ""
                          isWorkerDropdownExpanded = false
                        },
                    ) {
                      Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                      ) {
                        Text(
                          "All Workers (${workers.size})",
                          fontSize = 12.sp,
                          fontWeight = FontWeight.Bold,
                          color = if (searchQuery.isEmpty()) BentoBluePrimary else Color.Black,
                        )
                      }
                    }

                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    LazyColumn(
                      modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                      verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                      items(workers) { worker ->
                        val isSelected = searchQuery.equals(worker.fullName, ignoreCase = true)
                        Surface(
                          shape = RoundedCornerShape(10.dp),
                          color = if (isSelected) BentoBlueContainer else Color.White,
                          border = BorderStroke(1.dp, if (isSelected) BentoBluePrimary else Color(0xFFE0E0E0)),
                          modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                              searchQuery = worker.fullName
                              isWorkerDropdownExpanded = false
                            },
                        ) {
                          Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                          ) {
                            Box(
                              modifier = Modifier.size(32.dp).clip(CircleShape).background(BentoBlueContainer),
                              contentAlignment = Alignment.Center,
                            ) {
                              Text(worker.initials, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoBluePrimary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                              Text(worker.fullName, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.Black)
                              Text("${worker.role} • ${worker.siteName.ifBlank { "Main Site" }}", fontSize = 10.5.sp, color = BentoTextSecondary)
                            }
                            if (isSelected) {
                              Icon(Icons.Default.Check, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(18.dp))
                            }
                          }
                        }
                      }
                    }
                  }
                },
                confirmButton = {
                  TextButton(onClick = { isWorkerDropdownExpanded = false }) {
                    Text("Close", color = BentoBluePrimary, fontWeight = FontWeight.Bold)
                  }
                },
              )
            }

            Button(
              onClick = onAddWorkerClick,
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
              modifier = Modifier.height(52.dp),
            ) {
              Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }

          // Workers List
          if (filteredWorkers.isEmpty()) {
            Box(
              modifier = Modifier.fillMaxWidth().weight(1f, fill = false).padding(vertical = 24.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = if (searchQuery.isBlank()) "No workers registered yet." else "No workers match \"$searchQuery\"",
                color = BentoTextSecondary,
                fontSize = 13.sp,
              )
            }
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              items(filteredWorkers) { worker ->
                val overview = overviewWorkers.find { it.id == worker.id } ?: WorkerOverview(
                  id = worker.id,
                  fullName = worker.fullName,
                  initials = worker.initials,
                  role = worker.role,
                  siteId = worker.siteId,
                  siteName = worker.siteName,
                  nationalId = worker.nationalId,
                  phoneNumber = worker.phoneNumber,
                  assignedSiteIds = worker.assignedSiteIds,
                  assignedSiteNames = worker.assignedSiteNames,
                )
                val workerLeave = leaveBalances.find { it.workerId == worker.id } ?: LeaveBalance(workerId = worker.id)

                WorkerDirectoryItemCard(
                  worker = worker,
                  overview = overview,
                  leaveBalance = workerLeave,
                  onEdit = { onEditWorkerClick(worker) },
                  onDelete = { onDeleteWorkerClick(overview) },
                  onEditLeaveBalance = { onEditLeaveBalanceClick(worker, workerLeave) },
                )
              }
            }
          }

          // Close Button
          OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BentoOutline),
            modifier = Modifier.fillMaxWidth().height(42.dp),
          ) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Close Modal", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
          }
        }
      }
    }
  }
}

@Composable
private fun WorkerDirectoryItemCard(
  worker: WorkerEntity,
  overview: WorkerOverview,
  leaveBalance: LeaveBalance? = null,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onEditLeaveBalance: () -> Unit = {},
) {
  val balance = leaveBalance ?: LeaveBalance(workerId = worker.id)

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, BentoOutline),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(BentoBlueContainer),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = worker.initials,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = BentoBluePrimary,
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = worker.fullName,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color.Black,
            )
            Text(
              text = worker.role,
              fontSize = 11.sp,
              color = BentoTextSecondary,
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = BentoBluePrimary, modifier = Modifier.size(18.dp))
          }
          IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = BentoError, modifier = Modifier.size(18.dp))
          }
        }
      }

      // Details Row (National ID, Phone, Assigned Site)
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF5F5F5),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          if (worker.nationalId.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Person, contentDescription = null, tint = BentoTextSecondary, modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("National ID / Iqama: ${worker.nationalId}", fontSize = 11.sp, color = Color.Black)
            }
          }
          if (worker.phoneNumber.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Phone, contentDescription = null, tint = BentoTextSecondary, modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Phone: ${worker.phoneNumber}", fontSize = 11.sp, color = Color.Black)
            }
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = BentoSuccess, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Assigned Site: ${worker.assignedSiteNames.ifBlank { worker.siteName.ifBlank { "Primary Site" } }}",
              fontSize = 11.sp,
              color = BentoSuccess,
              fontWeight = FontWeight.Medium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }

      // Leave Quota Row (Annual, Casual, Sick Available)
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF0FDF4),
        border = BorderStroke(0.8.dp, BentoSuccess.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f),
          ) {
            Icon(Icons.Default.DateRange, contentDescription = null, tint = BentoSuccess, modifier = Modifier.size(14.dp))
            Text(
              text = "رصيد الإجازات: سنوية ${balance.annualAvailable.toInt()}/${balance.annualTotal.toInt()} • عارضة ${balance.casualAvailable.toInt()}/${balance.casualTotal.toInt()} • مرضية ${balance.sickAvailable.toInt()}/${balance.sickTotal.toInt()}",
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF1B5E20),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }

          TextButton(
            onClick = onEditLeaveBalance,
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
            modifier = Modifier.height(26.dp),
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("تعديل", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BentoBluePrimary)
          }
        }
      }
    }
  }
}

// ==========================================
// --- POP-UP MODAL 2: PAPER MANAGEMENT ---
// ==========================================
@Composable
fun PaperManagementModalDialog(
  workers: List<WorkerEntity>,
  initialSearch: String = "",
  onDismiss: () -> Unit,
  onEditDocuments: (WorkerEntity) -> Unit,
) {
  var searchQuery by remember { mutableStateOf(initialSearch) }
  var filterCategory by remember { mutableStateOf("ALL") } // ALL, EXPIRING_SOON, EXPIRED, VALID
  var isWorkerDropdownExpanded by remember { mutableStateOf(false) }

  val workerDocList = remember(workers, searchQuery, filterCategory) {
    workers.filter { worker ->
      val matchesSearch = searchQuery.isBlank() ||
        worker.fullName.contains(searchQuery, ignoreCase = true) ||
        worker.nationalId.contains(searchQuery, ignoreCase = true) ||
        worker.iqamaNumber.contains(searchQuery, ignoreCase = true) ||
        worker.insuranceNumber.contains(searchQuery, ignoreCase = true)

      val iqamaDays = calculateDaysRemaining(worker.iqamaEndDate)
      val insDays = calculateDaysRemaining(worker.insuranceEndDate)

      val hasExpired = (iqamaDays != null && iqamaDays <= 0) || (insDays != null && insDays <= 0)
      val hasExpiringSoon = ((iqamaDays != null && iqamaDays in 1..14) || (insDays != null && insDays in 1..14))
      val isValid = !hasExpired && !hasExpiringSoon

      val matchesFilter = when (filterCategory) {
        "ALL" -> true
        "EXPIRING_SOON" -> hasExpiringSoon
        "EXPIRED" -> hasExpired
        "VALID" -> isValid
        else -> true
      }

      matchesSearch && matchesFilter
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = true),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.5f))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onDismiss,
        ),
      contentAlignment = Alignment.Center,
    ) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp)
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { /* keep dialog open when clicking inside */ },
          ),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8F9FA),
        border = BorderStroke(1.dp, BentoOutline),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          // Modal Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFFFF3E0)),
                contentAlignment = Alignment.Center,
              ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = BentoWarning, modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Paper & Document Management",
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Black,
                )
                Text(
                  text = "Iqama & Medical Insurance dossiers & 2-week expiry warnings",
                  fontSize = 11.sp,
                  color = BentoTextSecondary,
                )
              }
            }

            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
            }
          }

          // Search Box with Dropdown Selector for Available Worker Names
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            OutlinedTextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              placeholder = { Text("Search by name, Iqama, insurance...", fontSize = 12.sp) },
              leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BentoTextSecondary) },
              trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                  IconButton(onClick = { searchQuery = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = BentoTextSecondary)
                  }
                }
              },
              shape = RoundedCornerShape(14.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
              ),
              singleLine = true,
              modifier = Modifier.weight(1f),
            )

            // Worker Selection Quick Picker Button
            OutlinedButton(
              onClick = { isWorkerDropdownExpanded = true },
              shape = RoundedCornerShape(14.dp),
              border = BorderStroke(1.dp, BentoBluePrimary.copy(alpha = 0.4f)),
              colors = ButtonDefaults.outlinedButtonColors(containerColor = BentoBlueContainer.copy(alpha = 0.5f)),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
              modifier = Modifier.height(52.dp),
            ) {
              Icon(Icons.Default.People, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Staff", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = BentoBluePrimary)
              Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(16.dp))
            }

            if (isWorkerDropdownExpanded) {
              AlertDialog(
                onDismissRequest = { isWorkerDropdownExpanded = false },
                properties = DialogProperties(dismissOnClickOutside = true, dismissOnBackPress = true),
                containerColor = Color.White,
                title = {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier.size(34.dp).clip(CircleShape).background(BentoBlueContainer),
                      contentAlignment = Alignment.Center,
                    ) {
                      Icon(Icons.Default.People, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Staff Member", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                  }
                },
                text = {
                  Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    // "Show All" option
                    Surface(
                      shape = RoundedCornerShape(10.dp),
                      color = if (searchQuery.isEmpty()) BentoBlueContainer else Color(0xFFF5F5F5),
                      border = BorderStroke(1.dp, if (searchQuery.isEmpty()) BentoBluePrimary else Color(0xFFE0E0E0)),
                      modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                          searchQuery = ""
                          isWorkerDropdownExpanded = false
                        },
                    ) {
                      Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                      ) {
                        Text(
                          "All Workers (${workers.size})",
                          fontSize = 12.sp,
                          fontWeight = FontWeight.Bold,
                          color = if (searchQuery.isEmpty()) BentoBluePrimary else Color.Black,
                        )
                      }
                    }

                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    LazyColumn(
                      modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                      verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                      items(workers) { worker ->
                        val isSelected = searchQuery.equals(worker.fullName, ignoreCase = true)
                        Surface(
                          shape = RoundedCornerShape(10.dp),
                          color = if (isSelected) BentoBlueContainer else Color.White,
                          border = BorderStroke(1.dp, if (isSelected) BentoBluePrimary else Color(0xFFE0E0E0)),
                          modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                              searchQuery = worker.fullName
                              isWorkerDropdownExpanded = false
                            },
                        ) {
                          Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                          ) {
                            Box(
                              modifier = Modifier.size(32.dp).clip(CircleShape).background(BentoBlueContainer),
                              contentAlignment = Alignment.Center,
                            ) {
                              Text(worker.initials, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoBluePrimary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                              Text(worker.fullName, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.Black)
                              Text(
                                text = "Iqama: ${worker.iqamaNumber.ifBlank { "Not Set" }} • ${worker.siteName.ifBlank { "Main Site" }}",
                                fontSize = 10.5.sp,
                                color = BentoTextSecondary,
                              )
                            }
                            if (isSelected) {
                              Icon(Icons.Default.Check, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(18.dp))
                            }
                          }
                        }
                      }
                    }
                  }
                },
                confirmButton = {
                  TextButton(onClick = { isWorkerDropdownExpanded = false }) {
                    Text("Close", color = BentoBluePrimary, fontWeight = FontWeight.Bold)
                  }
                },
              )
            }
          }

          // Filter Category Chips
          Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            FilterChip(
              selected = filterCategory == "ALL",
              onClick = { filterCategory = "ALL" },
              label = { Text("All Workers (${workers.size})", fontSize = 11.sp) },
            )
            FilterChip(
              selected = filterCategory == "EXPIRING_SOON",
              onClick = { filterCategory = "EXPIRING_SOON" },
              label = { Text("⚠️ Expiring in 2 Weeks", fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = BentoWarningContainer,
                selectedLabelColor = BentoWarning,
              ),
            )
            FilterChip(
              selected = filterCategory == "EXPIRED",
              onClick = { filterCategory = "EXPIRED" },
              label = { Text("⛔ Expired", fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFFFFEBEE),
                selectedLabelColor = BentoError,
              ),
            )
            FilterChip(
              selected = filterCategory == "VALID",
              onClick = { filterCategory = "VALID" },
              label = { Text("✓ Valid Documents", fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = BentoSuccessContainer,
                selectedLabelColor = BentoSuccess,
              ),
            )
          }

          // List of Worker Document Dossiers
          if (workerDocList.isEmpty()) {
            Box(
              modifier = Modifier.fillMaxWidth().weight(1f, fill = false).padding(vertical = 24.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = if (searchQuery.isBlank()) "No records in this filter category." else "No worker document matches \"$searchQuery\"",
                color = BentoTextSecondary,
                fontSize = 13.sp,
              )
            }
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              items(workerDocList) { worker ->
                WorkerDocumentDossierCard(
                  worker = worker,
                  onEditDocuments = { onEditDocuments(worker) },
                )
              }
            }
          }

          // Close Button
          OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BentoOutline),
            modifier = Modifier.fillMaxWidth().height(42.dp),
          ) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Close Modal", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
          }
        }
      }
    }
  }
}

@Composable
private fun WorkerDocumentDossierCard(
  worker: WorkerEntity,
  onEditDocuments: () -> Unit,
) {
  val iqamaDays = calculateDaysRemaining(worker.iqamaEndDate)
  val insDays = calculateDaysRemaining(worker.insuranceEndDate)

  val iqamaStatus = when {
    iqamaDays == null -> null
    iqamaDays <= 0 -> DocumentExpiryStatus.EXPIRED
    iqamaDays <= 14 -> DocumentExpiryStatus.EXPIRING_SOON
    iqamaDays <= 30 -> DocumentExpiryStatus.EXPIRING_MONTH
    else -> DocumentExpiryStatus.VALID
  }

  val insStatus = when {
    insDays == null -> null
    insDays <= 0 -> DocumentExpiryStatus.EXPIRED
    insDays <= 14 -> DocumentExpiryStatus.EXPIRING_SOON
    insDays <= 30 -> DocumentExpiryStatus.EXPIRING_MONTH
    else -> DocumentExpiryStatus.VALID
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, BentoOutline),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      // Header with Worker Name & Edit Action
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Box(
            modifier = Modifier.size(38.dp).clip(CircleShape).background(BentoBlueContainer),
            contentAlignment = Alignment.Center,
          ) {
            Text(text = worker.initials, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoBluePrimary)
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(text = worker.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            Text(text = "${worker.role} • ${worker.siteName.ifBlank { "Main Site" }}", fontSize = 11.sp, color = BentoTextSecondary)
          }
        }

        Button(
          onClick = onEditDocuments,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
          modifier = Modifier.height(32.dp),
        ) {
          Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Edit Dates", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }

      // 1. Iqama / Residency Details Card
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFAFAFA),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Person, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Iqama / Residency Details", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            if (iqamaStatus != null) {
              ExpiryBadge(status = iqamaStatus, days = iqamaDays)
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column {
              Text("Iqama Number:", fontSize = 10.sp, color = BentoTextSecondary)
              Text(
                text = worker.iqamaNumber.ifBlank { worker.nationalId.ifBlank { "Not Recorded" } },
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
              )
            }
            Column {
              Text("Issue Date:", fontSize = 10.sp, color = BentoTextSecondary)
              Text(text = worker.iqamaStartDate.ifBlank { "—" }, fontSize = 11.5.sp, color = Color.Black)
            }
            Column {
              Text("Expiry Date:", fontSize = 10.sp, color = BentoTextSecondary)
              Text(
                text = worker.iqamaEndDate.ifBlank { "—" },
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (iqamaStatus == DocumentExpiryStatus.EXPIRED) BentoError else if (iqamaStatus == DocumentExpiryStatus.EXPIRING_SOON) BentoWarning else Color.Black,
              )
            }
          }
        }
      }

      // 2. Medical Insurance Details Card
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFAFAFA),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = BentoSuccess, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Medical Insurance Details", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            if (insStatus != null) {
              ExpiryBadge(status = insStatus, days = insDays)
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Provider & Policy:", fontSize = 10.sp, color = BentoTextSecondary)
              Text(
                text = "${worker.insuranceProvider.ifBlank { "Insurance Co." }} (${worker.insuranceNumber.ifBlank { "—" }})",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text("Start Date:", fontSize = 10.sp, color = BentoTextSecondary)
              Text(text = worker.insuranceStartDate.ifBlank { "—" }, fontSize = 11.5.sp, color = Color.Black)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text("Expiry Date:", fontSize = 10.sp, color = BentoTextSecondary)
              Text(
                text = worker.insuranceEndDate.ifBlank { "—" },
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (insStatus == DocumentExpiryStatus.EXPIRED) BentoError else if (insStatus == DocumentExpiryStatus.EXPIRING_SOON) BentoWarning else Color.Black,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ExpiryBadge(status: DocumentExpiryStatus, days: Long?) {
  val (bgColor, textColor, label) = when (status) {
    DocumentExpiryStatus.EXPIRED -> {
      val absDays = if (days != null) kotlin.math.abs(days) else 0
      Triple(Color(0xFFFFEBEE), BentoError, "Expired $absDays days ago")
    }
    DocumentExpiryStatus.EXPIRING_SOON -> {
      Triple(BentoWarningContainer, BentoWarning, "Expires in $days days (Action Needed)")
    }
    DocumentExpiryStatus.EXPIRING_MONTH -> {
      Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "Expires in $days days")
    }
    DocumentExpiryStatus.VALID -> {
      Triple(BentoSuccessContainer, BentoSuccess, "Active & Valid ✓")
    }
  }

  Surface(
    shape = RoundedCornerShape(8.dp),
    color = bgColor,
  ) {
    Text(
      text = label,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      color = textColor,
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
    )
  }
}

// ==========================================
// --- POP-UP MODAL 3: WORK SITES ---
// ==========================================
@Composable
fun WorkSitesModalDialog(
  sites: List<WorkSite>,
  mapTilerApiKey: String,
  onDismiss: () -> Unit,
  onAddSiteClick: () -> Unit,
  onEditSiteClick: (WorkSite) -> Unit,
  onDeleteSiteClick: (WorkSite) -> Unit,
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = true),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.5f))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onDismiss,
        ),
      contentAlignment = Alignment.Center,
    ) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp)
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { /* keep dialog open when clicking inside */ },
          ),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8F9FA),
        border = BorderStroke(1.dp, BentoOutline),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          // Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFE0F2F1)),
                contentAlignment = Alignment.Center,
              ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF00897B), modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Work Sites & Geofences (${sites.size})",
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Black,
                )
                Text(
                  text = "GPS Coordinates and geofence boundary radii",
                  fontSize = 11.sp,
                  color = BentoTextSecondary,
                )
              }
            }

            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
            }
          }

          Button(
            onClick = onAddSiteClick,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
            modifier = Modifier.fillMaxWidth().height(46.dp),
          ) {
            Icon(Icons.Default.AddLocation, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("+ Add New Work Site & Geofence", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }

          // Sites List
          if (sites.isEmpty()) {
            Box(
              modifier = Modifier.fillMaxWidth().weight(1f, fill = false).padding(vertical = 24.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text("No work sites configured yet.", color = BentoTextSecondary, fontSize = 13.sp)
            }
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              items(sites) { site ->
                Card(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(16.dp),
                  colors = CardDefaults.cardColors(containerColor = Color.White),
                  border = BorderStroke(1.dp, BentoOutline),
                ) {
                  Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Text(text = site.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                      Row {
                        IconButton(onClick = { onEditSiteClick(site) }, modifier = Modifier.size(30.dp)) {
                          Icon(Icons.Default.Edit, contentDescription = "Edit", tint = BentoBluePrimary, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { onDeleteSiteClick(site) }, modifier = Modifier.size(30.dp)) {
                          Icon(Icons.Default.Delete, contentDescription = "Delete", tint = BentoError, modifier = Modifier.size(16.dp))
                        }
                      }
                    }

                    Text(text = site.address, fontSize = 11.5.sp, color = BentoTextSecondary)

                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                      Text(
                        text = "Radius: ${site.radiusMeters} meters",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoSuccess,
                      )
                      Text(
                        text = "GPS: ${String.format(Locale.ENGLISH, "%.4f, %.4f", site.latitude, site.longitude)}",
                        fontSize = 11.sp,
                        color = BentoTextSecondary,
                      )
                    }
                  }
                }
              }
            }
          }

          // Close Button
          OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BentoOutline),
            modifier = Modifier.fillMaxWidth().height(42.dp),
          ) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Close Modal", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
          }
        }
      }
    }
  }
}

// ==========================================
// --- POP-UP MODAL 4: ATTENDANCE REPORT ---
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceReportModalDialog(
  records: List<AttendanceRecord>,
  workers: List<WorkerEntity>,
  shiftConfig: WorkShiftConfig,
  onDismiss: () -> Unit,
) {
  var selectedWorkerName by remember { mutableStateOf("ALL") }
  var isWorkerDropdownExpanded by remember { mutableStateOf(false) }
  var isExportingCsv by remember { mutableStateOf(false) }
  var exportErrorMessage by remember { mutableStateOf<String?>(null) }
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  val filtered = remember(records, selectedWorkerName) {
    if (selectedWorkerName == "ALL") records
    else records.filter { it.workerName.equals(selectedWorkerName, ignoreCase = true) }
  }

  val totalDays = filtered.size
  val lateCount = filtered.count { it.isLate }
  val onTimeRate = if (totalDays > 0) (((totalDays - lateCount).toFloat() / totalDays) * 100).toInt() else 100

  fun handleExportCsv() {
    if (filtered.isEmpty()) {
      exportErrorMessage = "No attendance records available to export."
      return
    }
    exportErrorMessage = null
    isExportingCsv = true
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
    coroutineScope.launch {
      try {
        val uri = withContext(Dispatchers.IO) {
          exportAttendanceToCsv(context, filtered)
        }
        isExportingCsv = false
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
          type = "text/csv"
          putExtra(Intent.EXTRA_SUBJECT, "Attendance Report Export - $todayStr")
          putExtra(Intent.EXTRA_TEXT, "Exported attendance report for ${if (selectedWorkerName == "ALL") "All Workers" else selectedWorkerName} (${filtered.size} records).")
          putExtra(Intent.EXTRA_STREAM, uri)
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Export Attendance CSV")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
      } catch (e: Exception) {
        isExportingCsv = false
        exportErrorMessage = "Export failed: ${e.localizedMessage ?: "Unknown error"}"
      }
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = true),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.5f))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onDismiss,
        ),
      contentAlignment = Alignment.Center,
    ) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp)
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { /* keep dialog open when clicking inside */ },
          ),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8F9FA),
        border = BorderStroke(1.dp, BentoOutline),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          // Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f, fill = false),
            ) {
              Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFEDE7F6)),
                contentAlignment = Alignment.Center,
              ) {
                Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(0xFF7E57C2), modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Attendance Reports",
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Black,
                )
                Text(
                  text = "Attendance logs, duration metrics & compliance",
                  fontSize = 11.sp,
                  color = BentoTextSecondary,
                )
              }
            }

            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
            }
          }

          // Worker Filter Dropdown
          val workerNames = remember(workers) {
            listOf("ALL") + workers.map { it.fullName }.filter { it.isNotBlank() }.distinct()
          }
          if (workerNames.size > 1) {
            ExposedDropdownMenuBox(
              expanded = isWorkerDropdownExpanded,
              onExpandedChange = { isWorkerDropdownExpanded = !isWorkerDropdownExpanded },
              modifier = Modifier.fillMaxWidth(),
            ) {
              OutlinedTextField(
                value = if (selectedWorkerName == "ALL") "All Employees (Full Company)" else selectedWorkerName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Filter Worker", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(16.dp)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isWorkerDropdownExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = BentoOutline,
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color.Black),
              )

              ExposedDropdownMenu(
                expanded = isWorkerDropdownExpanded,
                onDismissRequest = { isWorkerDropdownExpanded = false },
                modifier = Modifier.background(Color.White),
              ) {
                workerNames.forEach { name ->
                  DropdownMenuItem(
                    text = {
                      Text(
                        text = if (name == "ALL") "All Employees (Full Company)" else name,
                        fontSize = 13.sp,
                        fontWeight = if (name == selectedWorkerName) FontWeight.Bold else FontWeight.Normal,
                        color = Color.Black,
                      )
                    },
                    onClick = {
                      selectedWorkerName = name
                      isWorkerDropdownExpanded = false
                    },
                  )
                }
              }
            }
          }

          // Stats Pill Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color.White,
              border = BorderStroke(1.dp, BentoOutline),
              modifier = Modifier.weight(1f),
            ) {
              Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$totalDays", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BentoBluePrimary)
                Text(text = "Total Shifts", fontSize = 10.sp, color = BentoTextSecondary)
              }
            }
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color.White,
              border = BorderStroke(1.dp, BentoOutline),
              modifier = Modifier.weight(1f),
            ) {
              Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$lateCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BentoWarning)
                Text(text = "Late Count", fontSize = 10.sp, color = BentoTextSecondary)
              }
            }
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color.White,
              border = BorderStroke(1.dp, BentoOutline),
              modifier = Modifier.weight(1f),
            ) {
              Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$onTimeRate%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BentoSuccess)
                Text(text = "Compliance", fontSize = 10.sp, color = BentoTextSecondary)
              }
            }
          }

          // Error banner if export fails
          if (exportErrorMessage != null) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = BentoError.copy(alpha = 0.1f),
              border = BorderStroke(1.dp, BentoError.copy(alpha = 0.3f)),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(text = exportErrorMessage ?: "", fontSize = 11.sp, color = BentoError)
                IconButton(onClick = { exportErrorMessage = null }, modifier = Modifier.size(18.dp)) {
                  Icon(Icons.Default.Close, contentDescription = null, tint = BentoError, modifier = Modifier.size(12.dp))
                }
              }
            }
          }

          // Records Table / List
          if (filtered.isEmpty()) {
            Box(
              modifier = Modifier.fillMaxWidth().weight(1f, fill = false).padding(vertical = 24.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text("No attendance records logged yet.", color = BentoTextSecondary, fontSize = 13.sp)
            }
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              items(filtered) { record ->
                Card(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = Color.White),
                  border = BorderStroke(1.dp, BentoOutline),
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Column {
                      Text(text = record.workerName.ifBlank { "Employee" }, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                      Text(text = "Date: ${record.workDate}  •  Site: ${record.siteName}", fontSize = 11.sp, color = BentoTextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                      Text(
                        text = "In: ${record.checkInTime ?: "—"}  •  Out: ${record.checkOutTime ?: "—"}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                      )
                      Text(
                        text = if (record.isLate) "Late arrival" else "On time ✓",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (record.isLate) BentoWarning else BentoSuccess,
                      )
                    }
                  }
                }
              }
            }
          }

          // Actions: Export CSV and Close Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Button(
              onClick = { handleExportCsv() },
              enabled = !isExportingCsv && filtered.isNotEmpty(),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2)),
              modifier = Modifier.weight(1f).height(44.dp).testTag("export_attendance_report_csv_button"),
            ) {
              if (isExportingCsv) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Exporting...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
              } else {
                Icon(Icons.Default.Share, contentDescription = "Export CSV", modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }
            }

            OutlinedButton(
              onClick = onDismiss,
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
              border = BorderStroke(1.dp, BentoOutline),
              modifier = Modifier.weight(1f).height(44.dp),
            ) {
              Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Close", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            }
          }
        }
      }
    }
  }
}

// ==========================================
// --- EDIT WORKER DOCUMENTS DIALOG ---
// ==========================================
@Composable
fun EditWorkerDocumentsDialog(
  worker: WorkerEntity,
  onDismiss: () -> Unit,
  onSave: (
    iqamaNumber: String,
    iqamaStartDate: String,
    iqamaEndDate: String,
    insuranceNumber: String,
    insuranceProvider: String,
    insuranceStartDate: String,
    insuranceEndDate: String,
    passportNumber: String,
    nationality: String,
    contractEndDate: String,
    salary: Double,
    hireDate: String,
    employmentEndDate: String,
  ) -> Unit,
) {
  var iqamaNum by remember { mutableStateOf(worker.iqamaNumber.ifBlank { worker.nationalId }) }
  var iqamaStart by remember { mutableStateOf(worker.iqamaStartDate) }
  var iqamaEnd by remember { mutableStateOf(worker.iqamaEndDate) }

  var insNum by remember { mutableStateOf(worker.insuranceNumber) }
  var insProvider by remember { mutableStateOf(worker.insuranceProvider) }
  var insStart by remember { mutableStateOf(worker.insuranceStartDate) }
  var insEnd by remember { mutableStateOf(worker.insuranceEndDate) }

  var passport by remember { mutableStateOf(worker.passportNumber) }
  var nationality by remember { mutableStateOf(worker.nationality) }
  var contractEnd by remember { mutableStateOf(worker.contractEndDate) }

  var salaryStr by remember { mutableStateOf(if (worker.salary > 0) worker.salary.toInt().toString() else "") }
  var hireDate by remember { mutableStateOf(worker.hireDate) }
  var employmentEndDate by remember { mutableStateOf(worker.employmentEndDate) }

  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnClickOutside = true, dismissOnBackPress = true),
    containerColor = Color.White,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier.size(36.dp).clip(CircleShape).background(BentoBlueContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(Icons.Default.Description, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text("Edit Documents: ${worker.fullName}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text("Update Iqama, Medical Insurance, Passport, Contract, and Compensation details.", fontSize = 11.sp, color = Color.Black)

        // Section 1: Iqama (Light Blue Container)
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFF0F7FF),
          border = BorderStroke(1.dp, BentoBluePrimary.copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Assignment, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("1. Iqama / Residency Details:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            OutlinedTextField(
              value = iqamaNum,
              onValueChange = { iqamaNum = it },
              label = { Text("Iqama / National ID Number", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
              textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedBorderColor = BentoBluePrimary,
                unfocusedBorderColor = Color(0xFFCCCCCC),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
              ),
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = iqamaStart,
                onValueChange = { iqamaStart = it },
                label = { Text("Start Date", fontSize = 9.5.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                placeholder = { Text("2025-09-01", fontSize = 10.sp, color = Color.DarkGray) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
              OutlinedTextField(
                value = iqamaEnd,
                onValueChange = { iqamaEnd = it },
                label = { Text("Expiration Date *", fontSize = 9.5.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                placeholder = { Text("2026-09-01", fontSize = 10.sp, color = Color.DarkGray) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        // Section 2: Medical Insurance (Light Green Container)
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFF0FFF4),
          border = BorderStroke(1.dp, BentoSuccess.copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("2. Medical Insurance Details:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            OutlinedTextField(
              value = insProvider,
              onValueChange = { insProvider = it },
              label = { Text("Insurance Company", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
              placeholder = { Text("Bupa / Tawuniya / Medgulf", fontSize = 10.sp, color = Color.DarkGray) },
              textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedBorderColor = BentoBluePrimary,
                unfocusedBorderColor = Color(0xFFCCCCCC),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
              ),
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
              value = insNum,
              onValueChange = { insNum = it },
              label = { Text("Policy / Insurance Number", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
              textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedBorderColor = BentoBluePrimary,
                unfocusedBorderColor = Color(0xFFCCCCCC),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
              ),
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = insStart,
                onValueChange = { insStart = it },
                label = { Text("Start Date", fontSize = 9.5.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                placeholder = { Text("2025-10-01", fontSize = 10.sp, color = Color.DarkGray) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
              OutlinedTextField(
                value = insEnd,
                onValueChange = { insEnd = it },
                label = { Text("Expiration Date *", fontSize = 9.5.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                placeholder = { Text("2026-10-01", fontSize = 10.sp, color = Color.DarkGray) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        // Section 3: Passport & Contract (Light Purple Container)
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFFBF5FF),
          border = BorderStroke(1.dp, Color(0xFF9C27B0).copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Description, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("3. Passport & Contract Details:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = passport,
                onValueChange = { passport = it },
                label = { Text("Passport Number", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.5.sp, fontWeight = FontWeight.Medium),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
              OutlinedTextField(
                value = nationality,
                onValueChange = { nationality = it },
                label = { Text("Nationality", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.5.sp, fontWeight = FontWeight.Medium),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
            }

            OutlinedTextField(
              value = contractEnd,
              onValueChange = { contractEnd = it },
              label = { Text("Contract End Date", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
              placeholder = { Text("2027-09-01", fontSize = 10.sp, color = Color.DarkGray) },
              textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedBorderColor = BentoBluePrimary,
                unfocusedBorderColor = Color(0xFFCCCCCC),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
              ),
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )
          }
        }

        // Section 4: Compensation & Employment (Light Amber Container)
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFFFFDF5),
          border = BorderStroke(1.dp, BentoWarning.copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Info, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("4. Compensation & Employment Tenure:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            OutlinedTextField(
              value = salaryStr,
              onValueChange = { salaryStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
              label = { Text("Monthly Salary (SAR / QAR)", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
              placeholder = { Text("e.g. 4500", fontSize = 10.sp, color = Color.DarkGray) },
              textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedBorderColor = BentoBluePrimary,
                unfocusedBorderColor = Color(0xFFCCCCCC),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
              ),
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = hireDate,
                onValueChange = { hireDate = it },
                label = { Text("Hire Date", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                placeholder = { Text("2023-01-15", fontSize = 10.sp, color = Color.DarkGray) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
              OutlinedTextField(
                value = employmentEndDate,
                onValueChange = { employmentEndDate = it },
                label = { Text("End of Employment", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                placeholder = { Text("Optional", fontSize = 10.sp, color = Color.DarkGray) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val parsedSalary = salaryStr.toDoubleOrNull() ?: 0.0
          onSave(
            iqamaNum.trim(),
            iqamaStart.trim(),
            iqamaEnd.trim(),
            insNum.trim(),
            insProvider.trim(),
            insStart.trim(),
            insEnd.trim(),
            passport.trim(),
            nationality.trim(),
            contractEnd.trim(),
            parsedSalary,
            hireDate.trim(),
            employmentEndDate.trim(),
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
      ) {
        Text("Save & Update Documents")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel", color = Color.Black)
      }
    },
  )
}

// ==========================================
// --- WORKER FORM DIALOG (CREATE/EDIT) ---
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerFormDialog(
  title: String,
  sites: List<WorkSite>,
  initialWorker: WorkerEntity? = null,
  initialLeaveBalance: LeaveBalance? = null,
  onDismiss: () -> Unit,
  onConfirm: (
    fullName: String,
    role: String,
    siteId: String,
    siteName: String,
    nationalId: String,
    phone: String,
    deviceModel: String,
    isApproved: Boolean,
    assignedSiteIds: String,
    assignedSiteNames: String,
    iqamaNumber: String,
    iqamaStartDate: String,
    iqamaEndDate: String,
    insuranceNumber: String,
    insuranceProvider: String,
    insuranceStartDate: String,
    insuranceEndDate: String,
    passportNumber: String,
    nationality: String,
    contractEndDate: String,
    salary: Double,
    hireDate: String,
    employmentEndDate: String,
    annualLeaveTotal: Double,
    casualLeaveTotal: Double,
    sickLeaveTotal: Double,
  ) -> Unit,
) {
  var fullName by remember { mutableStateOf(initialWorker?.fullName ?: "") }
  var role by remember { mutableStateOf(initialWorker?.role ?: "Field Technician") }
  var nationalId by remember { mutableStateOf(initialWorker?.nationalId ?: "") }
  var phone by remember { mutableStateOf(initialWorker?.phoneNumber ?: "") }
  var deviceModel by remember { mutableStateOf(initialWorker?.deviceModel ?: "Samsung Galaxy A54 5G") }
  var isApproved by remember { mutableStateOf(initialWorker?.isDeviceApproved ?: true) }

  // Document fields
  var iqamaNumber by remember { mutableStateOf(initialWorker?.iqamaNumber ?: "") }
  var iqamaStartDate by remember { mutableStateOf(initialWorker?.iqamaStartDate ?: "2025-09-01") }
  var iqamaEndDate by remember { mutableStateOf(initialWorker?.iqamaEndDate ?: "2026-09-01") }

  var insuranceNumber by remember { mutableStateOf(initialWorker?.insuranceNumber ?: "") }
  var insuranceProvider by remember { mutableStateOf(initialWorker?.insuranceProvider ?: "Bupa Arabia Insurance") }
  var insuranceStartDate by remember { mutableStateOf(initialWorker?.insuranceStartDate ?: "2025-10-01") }
  var insuranceEndDate by remember { mutableStateOf(initialWorker?.insuranceEndDate ?: "2026-10-01") }

  var passportNumber by remember { mutableStateOf(initialWorker?.passportNumber ?: "") }
  var nationality by remember { mutableStateOf(initialWorker?.nationality ?: "Resident") }
  var contractEndDate by remember { mutableStateOf(initialWorker?.contractEndDate ?: "2027-09-01") }

  var salaryStr by remember { mutableStateOf(if ((initialWorker?.salary ?: 0.0) > 0) initialWorker!!.salary.toInt().toString() else "") }
  var hireDate by remember { mutableStateOf(initialWorker?.hireDate ?: "2024-01-01") }
  var employmentEndDate by remember { mutableStateOf(initialWorker?.employmentEndDate ?: "") }

  // Leave Quota Fields
  var annualLeavesStr by remember {
    mutableStateOf(
      initialLeaveBalance?.annualTotal?.toInt()?.toString()
        ?: if (initialWorker != null) "21" else "21"
    )
  }
  var casualLeavesStr by remember {
    mutableStateOf(
      initialLeaveBalance?.casualTotal?.toInt()?.toString()
        ?: if (initialWorker != null) "7" else "7"
    )
  }
  var sickLeavesStr by remember {
    mutableStateOf(
      initialLeaveBalance?.sickTotal?.toInt()?.toString()
        ?: if (initialWorker != null) "14" else "14"
    )
  }

  // Multi-site selection
  val initialSiteIds = remember {
    if (initialWorker != null) {
      if (initialWorker.assignedSiteIds.isNotBlank()) {
        initialWorker.assignedSiteIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
      } else if (initialWorker.siteId.isNotBlank()) {
        listOf(initialWorker.siteId)
      } else {
        sites.take(1).map { it.id }
      }
    } else {
      sites.take(1).map { it.id }
    }
  }
  var selectedSiteIds by remember { mutableStateOf(initialSiteIds.toSet()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnClickOutside = true, dismissOnBackPress = true),
    containerColor = Color.White,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier.size(36.dp).clip(CircleShape).background(BentoBlueContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(Icons.Default.PersonAdd, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
      ) {
        // 1. Basic Info Section
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFFAFAFA),
          border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Personal & Professional Information", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            
            OutlinedTextField(
              value = fullName,
              onValueChange = { fullName = it },
              label = { Text("Worker Full Name *", fontSize = 11.5.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
              textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedBorderColor = BentoBluePrimary,
                unfocusedBorderColor = Color(0xFFCCCCCC),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
              ),
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
              value = role,
              onValueChange = { role = it },
              label = { Text("Job Title / Role *", fontSize = 11.5.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
              textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedBorderColor = BentoBluePrimary,
                unfocusedBorderColor = Color(0xFFCCCCCC),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
              ),
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = nationalId,
                onValueChange = { nationalId = it },
                label = { Text("National ID / Iqama", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.5.sp, fontWeight = FontWeight.Medium),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
              OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.5.sp, fontWeight = FontWeight.Medium),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        // 2. Assigned Sites Checkboxes
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFFAFAFA),
          border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Assigned Work Sites:", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            sites.forEach { site ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .clickable {
                    selectedSiteIds = if (selectedSiteIds.contains(site.id)) {
                      if (selectedSiteIds.size > 1) selectedSiteIds - site.id else selectedSiteIds
                    } else {
                      selectedSiteIds + site.id
                    }
                  }
                  .padding(vertical = 2.dp),
              ) {
                Checkbox(
                  checked = selectedSiteIds.contains(site.id),
                  onCheckedChange = { checked ->
                    selectedSiteIds = if (checked) {
                      selectedSiteIds + site.id
                    } else {
                      if (selectedSiteIds.size > 1) selectedSiteIds - site.id else selectedSiteIds
                    }
                  },
                  colors = CheckboxDefaults.colors(checkedColor = BentoBluePrimary),
                )
                Text(text = site.name, fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }

        // 3. Separate Iqama / Residency Details Card (Light Blue Container)
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFF0F7FF),
          border = BorderStroke(1.dp, BentoBluePrimary.copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Assignment, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Iqama & National Residency Details", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            OutlinedTextField(
              value = iqamaNumber,
              onValueChange = { iqamaNumber = it },
              label = { Text("Iqama Number", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
              textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedBorderColor = BentoBluePrimary,
                unfocusedBorderColor = Color(0xFFCCCCCC),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
              ),
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = iqamaStartDate,
                onValueChange = { iqamaStartDate = it },
                label = { Text("Iqama Issue Date", fontSize = 9.5.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                placeholder = { Text("2025-09-01", fontSize = 10.sp, color = Color.DarkGray) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
              OutlinedTextField(
                value = iqamaEndDate,
                onValueChange = { iqamaEndDate = it },
                label = { Text("Iqama Expiry Date", fontSize = 9.5.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                placeholder = { Text("2026-09-01", fontSize = 10.sp, color = Color.DarkGray) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        // 4. Separate Medical Insurance Details Card (Light Green Container)
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFF0FFF4),
          border = BorderStroke(1.dp, BentoSuccess.copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Medical Insurance Details", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            OutlinedTextField(
              value = insuranceProvider,
              onValueChange = { insuranceProvider = it },
              label = { Text("Insurance Company", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
              placeholder = { Text("Bupa / Tawuniya / Medgulf", fontSize = 10.sp, color = Color.DarkGray) },
              textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedBorderColor = BentoBluePrimary,
                unfocusedBorderColor = Color(0xFFCCCCCC),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
              ),
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
              value = insuranceNumber,
              onValueChange = { insuranceNumber = it },
              label = { Text("Policy / Insurance Number", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
              textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedBorderColor = BentoBluePrimary,
                unfocusedBorderColor = Color(0xFFCCCCCC),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
              ),
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = insuranceStartDate,
                onValueChange = { insuranceStartDate = it },
                label = { Text("Insurance Start Date", fontSize = 9.5.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                placeholder = { Text("2025-10-01", fontSize = 10.sp, color = Color.DarkGray) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
              OutlinedTextField(
                value = insuranceEndDate,
                onValueChange = { insuranceEndDate = it },
                label = { Text("Insurance Expiry Date", fontSize = 9.5.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                placeholder = { Text("2026-10-01", fontSize = 10.sp, color = Color.DarkGray) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        // 5. Passport, Nationality & Contract End Date (Light Purple Container)
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFFBF5FF),
          border = BorderStroke(1.dp, Color(0xFF9C27B0).copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Description, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Passport & Work Contract", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = passportNumber,
                onValueChange = { passportNumber = it },
                label = { Text("Passport Number", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.5.sp, fontWeight = FontWeight.Medium),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
              OutlinedTextField(
                value = nationality,
                onValueChange = { nationality = it },
                label = { Text("Nationality", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.5.sp, fontWeight = FontWeight.Medium),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
            }

            OutlinedTextField(
              value = contractEndDate,
              onValueChange = { contractEndDate = it },
              label = { Text("Contract End Date", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
              textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedBorderColor = BentoBluePrimary,
                unfocusedBorderColor = Color(0xFFCCCCCC),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
              ),
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )
          }
        }

        // 6. Salary & Employment Tenure (Light Amber Container)
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFFFFDF5),
          border = BorderStroke(1.dp, BentoWarning.copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Info, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Compensation & Employment Tenure", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            OutlinedTextField(
              value = salaryStr,
              onValueChange = { salaryStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
              label = { Text("Monthly Base Salary (SAR / QAR)", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
              placeholder = { Text("e.g. 5000", fontSize = 10.sp, color = Color.DarkGray) },
              textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedBorderColor = BentoBluePrimary,
                unfocusedBorderColor = Color(0xFFCCCCCC),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
              ),
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = hireDate,
                onValueChange = { hireDate = it },
                label = { Text("Hire Date", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                placeholder = { Text("2024-01-01", fontSize = 10.sp, color = Color.DarkGray) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
              OutlinedTextField(
                value = employmentEndDate,
                onValueChange = { employmentEndDate = it },
                label = { Text("Employment End", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                placeholder = { Text("Optional", fontSize = 10.sp, color = Color.DarkGray) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        // 5. Leave Quota & Available Days Card (Soft Mint/Teal Container)
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFF0FDF4),
          border = BorderStroke(1.dp, BentoSuccess.copy(alpha = 0.45f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.DateRange, contentDescription = null, tint = BentoSuccess, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text(
                  "Leave Days Quota / رصيد الإجازات المتاحة",
                  fontSize = 12.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Black,
                )
                Text(
                  "Set yearly available leave days for this worker",
                  fontSize = 10.sp,
                  color = BentoTextSecondary,
                )
              }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = annualLeavesStr,
                onValueChange = { annualLeavesStr = it.filter { ch -> ch.isDigit() } },
                label = { Text("Annual (سنوية)", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                placeholder = { Text("21", fontSize = 10.sp, color = Color.DarkGray) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
              OutlinedTextField(
                value = casualLeavesStr,
                onValueChange = { casualLeavesStr = it.filter { ch -> ch.isDigit() } },
                label = { Text("Casual (عارضة)", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                placeholder = { Text("7", fontSize = 10.sp, color = Color.DarkGray) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
              OutlinedTextField(
                value = sickLeavesStr,
                onValueChange = { sickLeavesStr = it.filter { ch -> ch.isDigit() } },
                label = { Text("Sick (مرضية)", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                placeholder = { Text("14", fontSize = 10.sp, color = Color.DarkGray) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        // 7. Device Model Binding
        OutlinedTextField(
          value = deviceModel,
          onValueChange = { deviceModel = it },
          label = { Text("Approved Device Model", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
          textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedLabelColor = Color.Black,
            unfocusedLabelColor = Color.Black,
            focusedBorderColor = BentoBluePrimary,
            unfocusedBorderColor = Color(0xFFCCCCCC),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
          ),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (fullName.isNotBlank()) {
            val selectedSites = sites.filter { selectedSiteIds.contains(it.id) }
            val assignedIdsStr = if (selectedSites.isNotEmpty()) selectedSites.joinToString(",") { it.id } else sites.firstOrNull()?.id ?: ""
            val assignedNamesStr = if (selectedSites.isNotEmpty()) selectedSites.joinToString(", ") { it.name } else sites.firstOrNull()?.name ?: ""
            val primarySite = selectedSites.firstOrNull() ?: sites.firstOrNull()
            val primId = primarySite?.id ?: ""
            val primName = primarySite?.name ?: ""

            val generatedNationalId = nationalId.ifBlank { "10" + (10000000..99999999).random() }.trim()
            val finalIqamaNum = iqamaNumber.ifBlank { generatedNationalId }.trim()
            val finalIqamaStart = iqamaStartDate.ifBlank { "2025-09-01" }.trim()
            val finalIqamaEnd = iqamaEndDate.ifBlank { "2026-09-01" }.trim()

            val finalInsNumber = insuranceNumber.ifBlank { "POL-" + (100000..999999).random() + "-BUPA" }.trim()
            val finalInsProvider = insuranceProvider.ifBlank { "Bupa Arabia Insurance" }.trim()
            val finalInsStart = insuranceStartDate.ifBlank { "2025-10-01" }.trim()
            val finalInsEnd = insuranceEndDate.ifBlank { "2026-10-01" }.trim()

            val finalPassport = passportNumber.ifBlank { "P" + (10000000..99999999).random() }.trim()
            val finalNationality = nationality.ifBlank { "Saudi" }.trim()
            val finalContractEnd = contractEndDate.ifBlank { "2027-09-01" }.trim()
            val finalPhone = phone.ifBlank { "+966 5" + (10000000..99999999).random() }.trim()
            val finalRole = role.ifBlank { "Field Technician" }.trim()
            val finalDeviceModel = deviceModel.ifBlank { "Samsung Galaxy A54 5G" }.trim()
            val finalSalary = salaryStr.toDoubleOrNull() ?: 0.0
            val finalHireDate = hireDate.ifBlank { "2024-01-01" }.trim()
            val finalAnnualLeaves = annualLeavesStr.toDoubleOrNull() ?: 21.0
            val finalCasualLeaves = casualLeavesStr.toDoubleOrNull() ?: 7.0
            val finalSickLeaves = sickLeavesStr.toDoubleOrNull() ?: 14.0

            onConfirm(
              fullName.trim(),
              finalRole,
              primId,
              primName,
              generatedNationalId,
              finalPhone,
              finalDeviceModel,
              isApproved,
              assignedIdsStr,
              assignedNamesStr,
              finalIqamaNum,
              finalIqamaStart,
              finalIqamaEnd,
              finalInsNumber,
              finalInsProvider,
              finalInsStart,
              finalInsEnd,
              finalPassport,
              finalNationality,
              finalContractEnd,
              finalSalary,
              finalHireDate,
              employmentEndDate.trim(),
              finalAnnualLeaves,
              finalCasualLeaves,
              finalSickLeaves,
            )
          }
        },
        enabled = fullName.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
      ) {
        Text("Save Staff Profile")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel", color = Color.Black)
      }
    },
  )
}

// ==========================================
// --- WORK SITE FORM DIALOG (CREATE/EDIT) ---
// ==========================================
@Composable
fun WorkSiteFormDialog(
  title: String,
  initialSite: WorkSite? = null,
  currentDeviceLat: Double = 0.0,
  currentDeviceLng: Double = 0.0,
  isLocationReady: Boolean = false,
  isOnline: Boolean = true,
  onDismiss: () -> Unit,
  onConfirm: (name: String, lat: Double, lng: Double, radius: Int, address: String) -> Unit,
) {
  val context = LocalContext.current
  var name by remember { mutableStateOf(initialSite?.name ?: "") }
  var latStr by remember { mutableStateOf(initialSite?.latitude?.toString() ?: "21.543333") }
  var lngStr by remember { mutableStateOf(initialSite?.longitude?.toString() ?: "39.172778") }
  var radiusStr by remember { mutableStateOf(initialSite?.radiusMeters?.toString() ?: "100") }
  var address by remember { mutableStateOf(initialSite?.address ?: "") }
  var isSearchingGps by remember { mutableStateOf(false) }
  var gpsErrorMessage by remember { mutableStateOf<String?>(null) }
  var gpsSuccessMessage by remember { mutableStateOf<String?>(null) }

  // Location permission request launcher
  val locationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
    contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
    val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
    if (fineGranted || coarseGranted) {
      isSearchingGps = true
      com.example.service.LocationHelper.searchLocationWithDiagnostics(
        context = context,
        isOnline = isOnline,
        onResult = { result ->
          isSearchingGps = false
          when (result) {
            is com.example.service.LocationHelper.LocationSearchResult.Success -> {
              latStr = String.format(Locale.ENGLISH, "%.6f", result.coordinates.latitude)
              lngStr = String.format(Locale.ENGLISH, "%.6f", result.coordinates.longitude)
              gpsSuccessMessage = "Real GPS acquired: ${String.format(Locale.ENGLISH, "%.4f, %.4f", result.coordinates.latitude, result.coordinates.longitude)} (±${result.coordinates.accuracy.toInt()}m) ✓"
              gpsErrorMessage = null
            }
            is com.example.service.LocationHelper.LocationSearchResult.Failure -> {
              gpsErrorMessage = result.explanationArabic
              gpsSuccessMessage = null
            }
          }
        }
      )
    } else {
      isSearchingGps = false
      gpsErrorMessage = "تم رفض إذن تحديد الموقع. يرجى تفعيل إذن الموقع من إعدادات الهاتف."
    }
  }

  val performFetchGps: () -> Unit = {
    gpsErrorMessage = null
    gpsSuccessMessage = null
    isSearchingGps = true
    if (!com.example.service.LocationHelper.hasLocationPermission(context)) {
      locationPermissionLauncher.launch(
        arrayOf(
          android.Manifest.permission.ACCESS_FINE_LOCATION,
          android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
      )
    } else {
      com.example.service.LocationHelper.searchLocationWithDiagnostics(
        context = context,
        isOnline = isOnline,
        onResult = { result ->
          isSearchingGps = false
          when (result) {
            is com.example.service.LocationHelper.LocationSearchResult.Success -> {
              latStr = String.format(Locale.ENGLISH, "%.6f", result.coordinates.latitude)
              lngStr = String.format(Locale.ENGLISH, "%.6f", result.coordinates.longitude)
              gpsSuccessMessage = "Real GPS acquired: ${String.format(Locale.ENGLISH, "%.4f, %.4f", result.coordinates.latitude, result.coordinates.longitude)} (±${result.coordinates.accuracy.toInt()}m) ✓"
              gpsErrorMessage = null
            }
            is com.example.service.LocationHelper.LocationSearchResult.Failure -> {
              gpsErrorMessage = result.explanationArabic
              gpsSuccessMessage = null
            }
          }
        }
      )
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
    containerColor = Color.White,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.AddLocation, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
      ) {
        // GPS Searching Banner
        if (isSearchingGps) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = BentoBlueContainer.copy(alpha = 0.7f),
            border = BorderStroke(1.dp, BentoBluePrimary.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = BentoBluePrimary,
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "Searching for GPS coordinates and evaluating signal...",
                color = BentoBluePrimary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
              )
            }
          }
        }

        // GPS Error Banner
        if (!isSearchingGps && gpsErrorMessage != null) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFFFEBEE),
            border = BorderStroke(1.dp, BentoError.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = BentoError, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = gpsErrorMessage!!,
                color = BentoError,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        // GPS Success Banner
        if (!isSearchingGps && gpsSuccessMessage != null) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = BentoSuccessContainer,
            border = BorderStroke(1.dp, BentoSuccess.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(Icons.Default.LocationOn, contentDescription = null, tint = BentoSuccess, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = gpsSuccessMessage!!,
                color = BentoSuccess,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Site / Project Name *", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
          textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedLabelColor = Color.Black,
            unfocusedLabelColor = Color.Black,
            focusedBorderColor = BentoBluePrimary,
            unfocusedBorderColor = Color(0xFFCCCCCC),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
          ),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )

        // 1. Interactive 2D Map for Site Geofence & Location Picking
        val currentLatNum = latStr.toDoubleOrNull() ?: 21.543333
        val currentLngNum = lngStr.toDoubleOrNull() ?: 39.172778
        val currentRadiusNum = radiusStr.toIntOrNull()?.coerceIn(10, 10000) ?: 100

        AdminSitePickerMap(
          initialLat = currentLatNum,
          initialLng = currentLngNum,
          radiusMeters = currentRadiusNum,
          siteName = name.ifBlank { "Work Site" },
          onCoordinatesChanged = { newLat, newLng ->
            latStr = String.format(Locale.ENGLISH, "%.6f", newLat)
            lngStr = String.format(Locale.ENGLISH, "%.6f", newLng)
            gpsErrorMessage = null
            gpsSuccessMessage = "Location set on map: ${String.format(Locale.ENGLISH, "%.4f, %.4f", newLat, newLng)}"
          },
          onMyLocationClick = performFetchGps,
          isSearchingLocation = isSearchingGps,
          modifier = Modifier.fillMaxWidth(),
        )

        // GPS Fetch Current Location Button
        Button(
          onClick = performFetchGps,
          enabled = !isSearchingGps,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = BentoBluePrimary,
            contentColor = Color.White,
          ),
          modifier = Modifier.fillMaxWidth().height(44.dp),
        ) {
          if (isSearchingGps) {
            CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
              strokeWidth = 2.dp,
              color = Color.White,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Fetching GPS Coordinates...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
          } else {
            Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("📍 Fetch Current GPS Location", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = latStr,
            onValueChange = {
              latStr = it
              gpsErrorMessage = null
              gpsSuccessMessage = null
            },
            label = { Text("Latitude *", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Medium),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.Black,
              unfocusedTextColor = Color.Black,
              focusedLabelColor = Color.Black,
              unfocusedLabelColor = Color.Black,
              focusedBorderColor = BentoBluePrimary,
              unfocusedBorderColor = Color(0xFFCCCCCC),
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
            ),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = lngStr,
            onValueChange = {
              lngStr = it
              gpsErrorMessage = null
              gpsSuccessMessage = null
            },
            label = { Text("Longitude *", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Medium),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.Black,
              unfocusedTextColor = Color.Black,
              focusedLabelColor = Color.Black,
              unfocusedLabelColor = Color.Black,
              focusedBorderColor = BentoBluePrimary,
              unfocusedBorderColor = Color(0xFFCCCCCC),
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
            ),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
        }

        // Quick Radius Presets (50m, 100m, 200m, 500m)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          listOf(50, 100, 200, 500).forEach { presetRadius ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (radiusStr == presetRadius.toString()) BentoBluePrimary else Color(0xFFF1F5F9),
              border = BorderStroke(
                1.dp,
                if (radiusStr == presetRadius.toString()) BentoBluePrimary else Color(0xFFCBD5E1)
              ),
              modifier = Modifier
                .weight(1f)
                .clickable { radiusStr = presetRadius.toString() },
            ) {
              Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                Text(
                  text = "${presetRadius}m",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (radiusStr == presetRadius.toString()) Color.White else Color.Black,
                )
              }
            }
          }
        }

        OutlinedTextField(
          value = radiusStr,
          onValueChange = { radiusStr = it },
          label = { Text("Geofence Radius (meters) *", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
          textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedLabelColor = Color.Black,
            unfocusedLabelColor = Color.Black,
            focusedBorderColor = BentoBluePrimary,
            unfocusedBorderColor = Color(0xFFCCCCCC),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
          ),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
          value = address,
          onValueChange = { address = it },
          label = { Text("Physical Address / City *", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
          textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedLabelColor = Color.Black,
            unfocusedLabelColor = Color.Black,
            focusedBorderColor = BentoBluePrimary,
            unfocusedBorderColor = Color(0xFFCCCCCC),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
          ),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val lat = latStr.toDoubleOrNull()
          val lng = lngStr.toDoubleOrNull()
          val radius = radiusStr.toIntOrNull()
          if (name.isBlank()) {
            gpsErrorMessage = "Please enter site / project name."
          } else if (lat == null || lng == null || !com.example.service.LocationHelper.validateCoordinates(lat, lng)) {
            gpsErrorMessage = "Invalid coordinates. Latitude must be between -90 and 90, Longitude between -180 and 180."
          } else if (radius == null || radius !in 10..10000) {
            gpsErrorMessage = "Geofence radius must be an integer between 10 and 10,000 meters."
          } else {
            onConfirm(name.trim(), lat, lng, radius, address.trim())
          }
        },
        enabled = name.isNotBlank() && !isSearchingGps,
        colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
      ) {
        Text("Save Work Site")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel", color = Color.Black)
      }
    },
  )
}

// ==========================================
// --- SHIFT SCHEDULE CONFIG DIALOG ---
// ==========================================
@Composable
fun ShiftScheduleConfigDialog(
  initialConfig: WorkShiftConfig,
  onDismiss: () -> Unit,
  onConfirm: (WorkShiftConfig) -> Unit,
) {
  var shiftName by remember { mutableStateOf(initialConfig.shiftName) }
  var startTime by remember { mutableStateOf(initialConfig.startTime) }
  var endTime by remember { mutableStateOf(initialConfig.endTime) }
  var gracePeriod by remember { mutableStateOf(initialConfig.gracePeriodMinutes.toString()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = Color.White,
    shape = RoundedCornerShape(20.dp),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier.size(38.dp).clip(CircleShape).background(BentoBlueContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(Icons.Default.Schedule, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text("Configure Official Working Hours", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
      ) {
        Text(
          text = "These times are used to calculate late arrivals and early departure warnings before the end of the shift.",
          fontSize = 12.sp,
          color = Color(0xFF444444),
          lineHeight = 17.sp,
        )

        OutlinedTextField(
          value = shiftName,
          onValueChange = { shiftName = it },
          label = { Text("Shift / Schedule Name", fontSize = 12.sp, color = Color.Black) },
          textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontWeight = FontWeight.SemiBold),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedLabelColor = Color.Black,
            unfocusedLabelColor = Color.Black,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
          ),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = startTime,
            onValueChange = { startTime = it },
            label = { Text("Start Time (e.g. 08:00 AM)", fontSize = 10.sp, color = Color.Black) },
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontWeight = FontWeight.Bold),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.Black,
              unfocusedTextColor = Color.Black,
              focusedLabelColor = Color.Black,
              unfocusedLabelColor = Color.Black,
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
            ),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = endTime,
            onValueChange = { endTime = it },
            label = { Text("End Time (e.g. 04:30 PM)", fontSize = 10.sp, color = Color.Black) },
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontWeight = FontWeight.Bold),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.Black,
              unfocusedTextColor = Color.Black,
              focusedLabelColor = Color.Black,
              unfocusedLabelColor = Color.Black,
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
            ),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
        }

        OutlinedTextField(
          value = gracePeriod,
          onValueChange = { gracePeriod = it },
          label = { Text("Grace Period after start (minutes)", fontSize = 12.sp, color = Color.Black) },
          textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontWeight = FontWeight.SemiBold),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedLabelColor = Color.Black,
            unfocusedLabelColor = Color.Black,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
          ),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )

        // Presets Chips
        Text("Quick Shift Presets:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoBluePrimary)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          FilterChip(
            selected = startTime == "08:00 AM" && endTime == "04:30 PM",
            onClick = {
              startTime = "08:00 AM"
              endTime = "04:30 PM"
              gracePeriod = "15"
            },
            label = { Text("08:00 AM - 04:30 PM", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = BentoBluePrimary,
              selectedLabelColor = Color.White,
              containerColor = Color(0xFFF1F5F9),
              labelColor = Color.Black,
            ),
          )
          FilterChip(
            selected = startTime == "07:30 AM" && endTime == "03:30 PM",
            onClick = {
              startTime = "07:30 AM"
              endTime = "03:30 PM"
              gracePeriod = "15"
            },
            label = { Text("07:30 AM - 03:30 PM", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = BentoBluePrimary,
              selectedLabelColor = Color.White,
              containerColor = Color(0xFFF1F5F9),
              labelColor = Color.Black,
            ),
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val grace = gracePeriod.toIntOrNull() ?: 15
          val finalConfig = initialConfig.copy(
            shiftName = if (shiftName.isNotBlank()) shiftName else "Morning Shift",
            startTime = if (startTime.isNotBlank()) startTime else "08:00 AM",
            endTime = if (endTime.isNotBlank()) endTime else "04:30 PM",
            gracePeriodMinutes = grace,
          )
          onConfirm(finalConfig)
        },
        colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
        shape = RoundedCornerShape(10.dp),
      ) {
        Text("Save Working Hours", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
        Text("Cancel", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
  )
}

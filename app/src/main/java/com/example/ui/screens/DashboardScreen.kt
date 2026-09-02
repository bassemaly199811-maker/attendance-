package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ActivityLog
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.DocumentAlert
import com.example.data.model.DocumentExpiryStatus
import com.example.data.model.DocumentType
import com.example.data.model.LeaveBalance
import com.example.data.model.LeaveRequest
import com.example.data.model.LeaveStatus
import com.example.data.model.WorkShiftConfig
import com.example.data.model.WorkSite
import com.example.data.model.WorkerEntity
import com.example.data.model.WorkerOverview
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
  workers: List<WorkerOverview>,
  rawWorkers: List<WorkerEntity>,
  sites: List<WorkSite>,
  activityLogs: List<ActivityLog>,
  records: List<AttendanceRecord> = emptyList(),
  shiftConfig: WorkShiftConfig = WorkShiftConfig(),
  mapTilerApiKey: String = "",
  deviceLatitude: Double = 0.0,
  deviceLongitude: Double = 0.0,
  isDeviceLocationReady: Boolean = false,
  isOnline: Boolean = true,
  allLeaveRequests: List<LeaveRequest> = emptyList(),
  allLeaveBalances: List<LeaveBalance> = emptyList(),
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
    annualLeave: Double,
    casualLeave: Double,
    sickLeave: Double,
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
  ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
  onUpdateShiftConfig: (WorkShiftConfig) -> Unit = {},
  isRefreshing: Boolean = false,
  lastSyncTime: String? = null,
  onRefresh: () -> Unit = {},
  onNavigateToUserManagement: () -> Unit = {},
  onNavigateToLeaveApprovals: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  // Navigation State for HR Management
  var isHrManagementOpen by remember { mutableStateOf(false) }
  var hrInitialSearchQuery by remember { mutableStateOf("") }
  var hrInitialOpenCard by remember { mutableStateOf<String?>(null) }

  // If HR Management is opened, show HR Management Screen
  if (isHrManagementOpen) {
    HrManagementScreen(
      workers = workers,
      rawWorkers = rawWorkers,
      sites = sites,
      records = records,
      shiftConfig = shiftConfig,
      mapTilerApiKey = mapTilerApiKey,
      deviceLatitude = deviceLatitude,
      deviceLongitude = deviceLongitude,
      isDeviceLocationReady = isDeviceLocationReady,
      isOnline = isOnline,
      initialSearchQuery = hrInitialSearchQuery,
      initialOpenCard = hrInitialOpenCard,
      leaveRequests = allLeaveRequests,
      leaveBalances = allLeaveBalances,
      onBack = {
        isHrManagementOpen = false
        hrInitialSearchQuery = ""
        hrInitialOpenCard = null
      },
      onAddNewSite = onAddNewSite,
      onUpdateSite = onUpdateSite,
      onDeleteSite = onDeleteSite,
      onAddNewWorker = onAddNewWorker,
      onUpdateWorker = onUpdateWorker,
      onDeleteWorker = onDeleteWorker,
      onUpdateLeaveBalance = onUpdateLeaveBalance,
      onUpdateWorkerDocuments = onUpdateWorkerDocuments,
      onUpdateShiftConfig = onUpdateShiftConfig,
      modifier = modifier,
    )
    return
  }

  val scrollState = rememberScrollState()

  // Attendance Metrics
  val presentCount = workers.count { it.status == AttendanceStatus.CHECKED_IN }
  val lateCount = workers.count { it.isLate }
  val totalCount = workers.size
  val attendanceRate = if (totalCount > 0) ((presentCount.toFloat() / totalCount) * 100).toInt() else 0

  // 2-Week Expiry Alerts
  val alerts = remember(rawWorkers) { getWorkerDocumentAlerts(rawWorkers) }
  val expiringSoonAlerts = remember(alerts) {
    alerts.filter { it.status == DocumentExpiryStatus.EXPIRING_SOON || it.status == DocumentExpiryStatus.EXPIRED }
  }

  // Search & Filter for Live Attendance
  var attendanceSearchQuery by remember { mutableStateOf("") }
  var selectedAttendanceStatus by remember { mutableStateOf<AttendanceStatus?>(null) }
  var isWorkerDropdownExpanded by remember { mutableStateOf(false) }

  val filteredWorkers = remember(workers, attendanceSearchQuery, selectedAttendanceStatus) {
    workers.filter { worker ->
      val matchesSearch = attendanceSearchQuery.isBlank() ||
        worker.fullName.contains(attendanceSearchQuery, ignoreCase = true) ||
        worker.role.contains(attendanceSearchQuery, ignoreCase = true) ||
        worker.siteName.contains(attendanceSearchQuery, ignoreCase = true)

      val matchesStatus = selectedAttendanceStatus == null || worker.status == selectedAttendanceStatus

      matchesSearch && matchesStatus
    }
  }

  PullToRefreshBox(
    isRefreshing = isRefreshing,
    onRefresh = onRefresh,
    modifier = modifier.fillMaxSize().testTag("dashboard_screen"),
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

      // 1. Top Bar / Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Dashboard Overview",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
          )
          Text(
            text = "Live operations & automated 2-week document alerts",
            fontSize = 11.5.sp,
            color = BentoTextSecondary,
          )
        }

        Button(
          onClick = {
            hrInitialSearchQuery = ""
            hrInitialOpenCard = null
            isHrManagementOpen = true
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
          modifier = Modifier.height(38.dp).testTag("open_hr_management_button"),
        ) {
          Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("HR Portal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }

      // Quick Admin Shortcuts Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedButton(
          onClick = onNavigateToUserManagement,
          modifier = Modifier.weight(1f).height(42.dp),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, BentoBluePrimary.copy(alpha = 0.5f)),
        ) {
          Icon(Icons.Default.People, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("User Accounts", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoBluePrimary)
        }

        val pendingLeaveCount = remember(allLeaveRequests) {
          allLeaveRequests.count { it.status == LeaveStatus.PENDING }
        }

        OutlinedButton(
          onClick = onNavigateToLeaveApprovals,
          modifier = Modifier.weight(1f).height(42.dp),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, Color(0xFFE65100).copy(alpha = 0.5f)),
        ) {
          Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Leave Requests", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
          if (pendingLeaveCount > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = CircleShape,
              color = BentoError,
              modifier = Modifier.size(18.dp),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = if (pendingLeaveCount > 99) "99+" else "$pendingLeaveCount",
                  color = Color.White,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }
        }
      }


      // 2. Cloud Sync & Firestore Status Bar
      Card(
        modifier = Modifier.fillMaxWidth().clickable { onRefresh() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BentoBlueContainer.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, BentoBluePrimary.copy(alpha = 0.2f)),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
          ) {
            Box(
              modifier =
                Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(BentoBluePrimary),
              contentAlignment = Alignment.Center,
            ) {
              if (isRefreshing) {
                CircularProgressIndicator(
                  modifier = Modifier.size(16.dp),
                  color = Color.White,
                  strokeWidth = 2.dp,
                )
              } else {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = "Refresh",
                  tint = Color.White,
                  modifier = Modifier.size(18.dp),
                )
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = if (isRefreshing) "Syncing with Firebase Firestore..." else "Cloud Synced with Firebase Firestore",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = BentoBluePrimary,
              )
              Text(
                text = if (lastSyncTime != null) "Last update: $lastSyncTime (Tap to refresh)" else "Pull or tap to refresh operations live",
                fontSize = 10.5.sp,
                color = BentoTextSecondary,
              )
            }
          }
        }
      }

      // ==========================================
      // --- 3. BENTO 2x2 OPERATIONS STATS GRID (MOVED UP) ---
      // ==========================================
      BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val cardSpacing = 10.dp
        Column(verticalArrangement = Arrangement.spacedBy(cardSpacing)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(cardSpacing),
          ) {
            BentoStatCard(
              title = "Today's Attendance",
              value = "$attendanceRate%",
              subtitle = "$presentCount of $totalCount workers",
              icon = Icons.Default.People,
              containerColor = BentoBluePrimary,
              contentColor = Color.White,
              modifier = Modifier.weight(1f),
            )
            BentoStatCard(
              title = "On-Site Now",
              value = "$presentCount",
              subtitle = "Inside Geofence Zone",
              icon = Icons.Default.LocationOn,
              containerColor = BentoSuccessContainer,
              contentColor = BentoSuccess,
              modifier = Modifier.weight(1f),
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(cardSpacing),
          ) {
            BentoStatCard(
              title = "Late Arrivals",
              value = "$lateCount",
              subtitle = "After ${shiftConfig.startTime}",
              icon = Icons.Default.Schedule,
              containerColor = Color(0xFFFFF3E0),
              contentColor = BentoWarning,
              modifier = Modifier.weight(1f),
            )
            BentoStatCard(
              title = "Device Security",
              value = "100%",
              subtitle = "Approved Biometrics",
              icon = Icons.Default.Shield,
              containerColor = BentoLilac,
              contentColor = BentoLilacText,
              modifier = Modifier.weight(1f),
            )
          }
        }
      }

      // ==========================================
      // --- 4. 2-WEEK EXPIRATION ALERTS BOX ---
      // ==========================================
      DashboardDocumentAlertsSection(
        alerts = expiringSoonAlerts,
        onAlertClick = { alert ->
          hrInitialSearchQuery = alert.workerName
          hrInitialOpenCard = "PAPER"
          isHrManagementOpen = true
        },
        onViewAllClick = {
          hrInitialSearchQuery = ""
          hrInitialOpenCard = "PAPER"
          isHrManagementOpen = true
        },
      )

      // ==========================================
      // --- 5. HR MANAGEMENT MASTER BENTO CARD ---
      // ==========================================
      Card(
        modifier =
          Modifier
            .fillMaxWidth()
            .clickable {
              hrInitialSearchQuery = ""
              hrInitialOpenCard = null
              isHrManagementOpen = true
            }
            .testTag("hr_management_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, BentoBluePrimary.copy(alpha = 0.4f)),
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Box(
                modifier = Modifier.size(46.dp).clip(CircleShape).background(BentoBluePrimary),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Default.Business,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(24.dp),
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "HR Management Portal",
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Black,
                )
                Text(
                  text = "Human resources, staff records, work sites & documents",
                  fontSize = 11.5.sp,
                  color = BentoTextSecondary,
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = BentoBlueContainer,
            ) {
              Text(
                text = "Open Portal ➔",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = BentoBluePrimary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              )
            }
          }

          Text(
            text = "Full administrative hub: Register new workers, configure GPS work sites & geofences, track Iqama & Insurance papers, view attendance reports, and set shift hours.",
            fontSize = 12.sp,
            color = BentoTextSecondary,
            lineHeight = 16.sp,
          )

          // Sub-feature pill chips
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = BentoBlueContainer.copy(alpha = 0.5f),
              modifier = Modifier.weight(1f),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
              ) {
                Icon(Icons.Default.People, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${rawWorkers.size} Workers", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BentoBluePrimary)
              }
            }

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = BentoSuccessContainer.copy(alpha = 0.5f),
              modifier = Modifier.weight(1f),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
              ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = BentoSuccess, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${sites.size} Sites", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BentoSuccess)
              }
            }

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (expiringSoonAlerts.isNotEmpty()) BentoWarningContainer else Color(0xFFE8F5E9),
              modifier = Modifier.weight(1f),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
              ) {
                Icon(
                  if (expiringSoonAlerts.isNotEmpty()) Icons.Default.Warning else Icons.Default.Description,
                  contentDescription = null,
                  tint = if (expiringSoonAlerts.isNotEmpty()) BentoWarning else BentoSuccess,
                  modifier = Modifier.size(13.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  if (expiringSoonAlerts.isNotEmpty()) "${expiringSoonAlerts.size} Alerts" else "Papers OK",
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (expiringSoonAlerts.isNotEmpty()) BentoWarning else BentoSuccess,
                )
              }
            }
          }
        }
      }

      // ==========================================
      // --- 6. LIVE WORKER ATTENDANCE ROSTER ---
      // ==========================================
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoOutline),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier.size(34.dp).clip(CircleShape).background(BentoBlueContainer),
                contentAlignment = Alignment.Center,
              ) {
                Icon(Icons.Default.People, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(18.dp))
              }
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Live Worker Attendance ($totalCount)",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.5.sp,
                  color = Color.Black,
                )
                Text(
                  text = "Today's check-ins, check-outs, and geofence locations",
                  fontSize = 10.5.sp,
                  color = BentoTextSecondary,
                )
              }
            }
          }

          // Search Box with Dropdown Selector for Available Names
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            OutlinedTextField(
              value = attendanceSearchQuery,
              onValueChange = { attendanceSearchQuery = it },
              placeholder = { Text("Search by name, role, site...", fontSize = 12.sp) },
              leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BentoTextSecondary) },
              trailingIcon = {
                if (attendanceSearchQuery.isNotEmpty()) {
                  IconButton(onClick = { attendanceSearchQuery = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = BentoTextSecondary)
                  }
                }
              },
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color(0xFFF9F9F9),
                unfocusedContainerColor = Color(0xFFF9F9F9),
              ),
              singleLine = true,
              modifier = Modifier.weight(1f),
            )

            // Worker Selection Quick Picker Button
            OutlinedButton(
              onClick = { isWorkerDropdownExpanded = true },
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, BentoBluePrimary.copy(alpha = 0.4f)),
              colors = ButtonDefaults.outlinedButtonColors(containerColor = BentoBlueContainer.copy(alpha = 0.5f)),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
              modifier = Modifier.height(52.dp),
            ) {
              Icon(
                imageVector = Icons.Default.People,
                contentDescription = "Select Worker",
                tint = BentoBluePrimary,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Staff", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = BentoBluePrimary)
              Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = BentoBluePrimary,
                modifier = Modifier.size(16.dp),
              )
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
                    Text("Staff List (Quick Filter)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
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
                      color = if (attendanceSearchQuery.isEmpty()) BentoBlueContainer else Color(0xFFF5F5F5),
                      border = BorderStroke(1.dp, if (attendanceSearchQuery.isEmpty()) BentoBluePrimary else Color(0xFFE0E0E0)),
                      modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                          attendanceSearchQuery = ""
                          isWorkerDropdownExpanded = false
                        },
                    ) {
                      Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                      ) {
                        Text(
                          "All Staff (${rawWorkers.size})",
                          fontSize = 12.sp,
                          fontWeight = FontWeight.Bold,
                          color = if (attendanceSearchQuery.isEmpty()) BentoBluePrimary else Color.Black,
                        )
                      }
                    }

                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    LazyColumn(
                      modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                      verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                      items(rawWorkers) { worker ->
                        val isSelected = attendanceSearchQuery.equals(worker.fullName, ignoreCase = true)
                        Surface(
                          shape = RoundedCornerShape(10.dp),
                          color = if (isSelected) BentoBlueContainer else Color.White,
                          border = BorderStroke(1.dp, if (isSelected) BentoBluePrimary else Color(0xFFE0E0E0)),
                          modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                              attendanceSearchQuery = worker.fullName
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
          }

          // Status Filter Chips
          Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            FilterChip(
              selected = selectedAttendanceStatus == null,
              onClick = { selectedAttendanceStatus = null },
              label = { Text("All ($totalCount)", fontSize = 11.sp) },
            )
            FilterChip(
              selected = selectedAttendanceStatus == AttendanceStatus.CHECKED_IN,
              onClick = { selectedAttendanceStatus = AttendanceStatus.CHECKED_IN },
              label = { Text("Present ($presentCount)", fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = BentoSuccessContainer,
                selectedLabelColor = BentoSuccess,
              ),
            )
            FilterChip(
              selected = selectedAttendanceStatus == AttendanceStatus.NOT_CHECKED_IN,
              onClick = { selectedAttendanceStatus = AttendanceStatus.NOT_CHECKED_IN },
              label = { Text("Not In", fontSize = 11.sp) },
            )
            FilterChip(
              selected = selectedAttendanceStatus == AttendanceStatus.CHECKED_OUT,
              onClick = { selectedAttendanceStatus = AttendanceStatus.CHECKED_OUT },
              label = { Text("Checked Out", fontSize = 11.sp) },
            )
          }

          // Workers List
          if (filteredWorkers.isEmpty()) {
            Box(
              modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = if (attendanceSearchQuery.isBlank()) "No workers logged yet." else "No worker matches \"$attendanceSearchQuery\"",
                color = BentoTextSecondary,
                fontSize = 12.sp,
              )
            }
          } else {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp),
            ) {
              items(filteredWorkers, key = { "${it.id}_${it.fullName}" }) { worker ->
                DashboardWorkerAttendanceItem(worker = worker)
              }
            }
          }
        }
      }

      // ==========================================
      // --- 7. FIELD ACTIVITY AUDIT LOG ---
      // ==========================================
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoOutline),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier.size(34.dp).clip(CircleShape).background(BentoLilac),
                contentAlignment = Alignment.Center,
              ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = BentoLilacText, modifier = Modifier.size(18.dp))
              }
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Live Activity & Security Logs",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.5.sp,
                  color = Color.Black,
                )
                Text(
                  text = "Audited system actions, biometrics, and cloud events",
                  fontSize = 10.5.sp,
                  color = BentoTextSecondary,
                )
              }
            }
          }

          if (activityLogs.isEmpty()) {
            Text("No activity logs recorded yet.", fontSize = 12.sp, color = BentoTextSecondary)
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              activityLogs.take(5).forEach { log ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                      modifier = Modifier.size(8.dp).clip(CircleShape).background(if (log.isSuccessful) BentoSuccess else BentoWarning),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = log.details,
                      fontSize = 11.5.sp,
                      color = Color.Black,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }
                  Text(text = log.timestamp, fontSize = 10.sp, color = BentoTextSecondary)
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

// ==========================================
// --- 2-WEEK DOCUMENT ALERTS COMPONENT ---
// ==========================================
@Composable
private fun DashboardDocumentAlertsSection(
  alerts: List<DocumentAlert>,
  onAlertClick: (DocumentAlert) -> Unit,
  onViewAllClick: () -> Unit,
) {
  if (alerts.isEmpty()) return

  Card(
    modifier = Modifier.fillMaxWidth().testTag("dashboard_document_alerts_box"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
    border = BorderStroke(1.5.dp, Color(0xFFFFB300)),
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
            modifier = Modifier.size(34.dp).clip(CircleShape).background(Color(0xFFFFECB3)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = Color(0xFFE65100),
              modifier = Modifier.size(20.dp),
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Document Expiration Warnings (Within 14 Days)",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color(0xFFBF360C),
            )
            Text(
              text = "Action required for upcoming expirations",
              fontSize = 10.5.sp,
              color = Color(0xFF8D6E63),
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFFFFECB3),
          modifier = Modifier.clickable { onViewAllClick() },
        ) {
          Text(
            text = "View All ➔",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFBF360C),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
      }

      // List of Alert Cards
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        alerts.take(4).forEach { alert ->
          Surface(
            modifier = Modifier.fillMaxWidth().clickable { onAlertClick(alert) },
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFFFE082)),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                  modifier = Modifier.size(30.dp).clip(CircleShape).background(
                    if (alert.status == DocumentExpiryStatus.EXPIRED) Color(0xFFFFEBEE) else BentoWarningContainer
                  ),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector = if (alert.documentType == DocumentType.IQAMA) Icons.Default.Person else Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = if (alert.status == DocumentExpiryStatus.EXPIRED) BentoError else BentoWarning,
                    modifier = Modifier.size(16.dp),
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = alert.workerName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.Black,
                  )
                  Text(
                    text = "${alert.documentType.titleEn}: ${alert.documentNumber.ifBlank { "—" }} • Expires: ${alert.endDate}",
                    fontSize = 10.5.sp,
                    color = BentoTextSecondary,
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (alert.status == DocumentExpiryStatus.EXPIRED) Color(0xFFFFEBEE) else BentoWarningContainer,
              ) {
                Text(
                  text = if (alert.status == DocumentExpiryStatus.EXPIRED) "Expired!" else "In ${alert.daysRemaining} days",
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (alert.status == DocumentExpiryStatus.EXPIRED) BentoError else BentoWarning,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DashboardWorkerAttendanceItem(worker: WorkerOverview) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    color = Color(0xFFF9F9F9),
    border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Box(
          modifier = Modifier.size(34.dp).clip(CircleShape).background(BentoBlueContainer),
          contentAlignment = Alignment.Center,
        ) {
          Text(text = worker.initials, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoBluePrimary)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(text = worker.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
          Text(
            text = "${worker.role} • ${worker.siteName.ifBlank { "Primary Site" }}",
            fontSize = 10.5.sp,
            color = BentoTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        val (tagBg, tagText, label) = when (worker.status) {
          AttendanceStatus.CHECKED_IN -> Triple(BentoSuccessContainer, BentoSuccess, "On-Site ✓")
          AttendanceStatus.CHECKED_OUT -> Triple(Color(0xFFEDE7F6), Color(0xFF7E57C2), "Checked Out")
          AttendanceStatus.NOT_CHECKED_IN -> Triple(Color(0xFFEEEEEE), Color.Black, "Not In")
        }
        Surface(shape = RoundedCornerShape(6.dp), color = tagBg) {
          Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = tagText,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
          )
        }
        if (worker.checkInTime != null) {
          Text(text = "In: ${worker.checkInTime}", fontSize = 10.sp, color = BentoTextSecondary)
        }
      }
    }
  }
}

/**
 * 2x2 Bento Stat Card
 */
@Composable
private fun BentoStatCard(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  containerColor: Color,
  contentColor: Color,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = title,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          color = contentColor.copy(alpha = 0.85f),
        )
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = contentColor,
          modifier = Modifier.size(18.dp),
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = value,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = contentColor,
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        fontSize = 10.sp,
        color = contentColor.copy(alpha = 0.75f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

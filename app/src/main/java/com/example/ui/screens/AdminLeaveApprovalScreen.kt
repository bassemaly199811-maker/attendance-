package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaveRequest
import com.example.data.model.LeaveStatus
import com.example.data.model.LeaveType
import com.example.ui.theme.BentoBlueContainer
import com.example.ui.theme.BentoBluePrimary
import com.example.ui.theme.BentoError
import com.example.ui.theme.BentoErrorContainer
import com.example.ui.theme.BentoOutline
import com.example.ui.theme.BentoSuccess
import com.example.ui.theme.BentoSuccessContainer
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.BentoTileGray
import com.example.ui.theme.BentoWarning
import com.example.ui.theme.BentoWarningContainer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun AdminLeaveApprovalScreen(
  requests: List<LeaveRequest>,
  onApprove: (requestId: Long) -> Unit,
  onReject: (requestId: Long, reason: String) -> Unit,
  onDeleteRequest: (requestId: Long) -> Unit = {},
  onUpdateRequest: (updatedRequest: LeaveRequest) -> Unit = {},
  onClose: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  BackHandler {
    onClose()
  }

  var filterStatus by remember { mutableStateOf<LeaveStatus?>(null) }
  var searchQuery by remember { mutableStateOf("") }
  var requestToReject by remember { mutableStateOf<LeaveRequest?>(null) }
  var rejectReason by remember { mutableStateOf("") }

  var requestToDelete by remember { mutableStateOf<LeaveRequest?>(null) }
  var requestToEdit by remember { mutableStateOf<LeaveRequest?>(null) }
  var visibleLeaveCount by remember { androidx.compose.runtime.mutableIntStateOf(5) }
  var isLoadingMoreLeaves by remember { mutableStateOf(false) }
  val leaveCoroutineScope = androidx.compose.runtime.rememberCoroutineScope()

  val filteredRequests =
    requests.filter { req ->
      (filterStatus == null || req.status == filterStatus) &&
        (searchQuery.isBlank() ||
          req.workerName.contains(searchQuery, ignoreCase = true) ||
          req.reason.contains(searchQuery, ignoreCase = true) ||
          req.leaveType.name.contains(searchQuery, ignoreCase = true))
    }

  val visibleRequests = remember(filteredRequests, visibleLeaveCount) {
    filteredRequests.take(visibleLeaveCount)
  }

  val pendingCount = requests.count { it.status == LeaveStatus.PENDING }

  Column(
    modifier = modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("admin_leave_approval_screen"),
  ) {
    Spacer(modifier = Modifier.height(8.dp))

    // Top Navigation & Dismiss Bar (Back to Dashboard / Close X)
    Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable { onClose() },
      ) {
        Surface(
          shape = CircleShape,
          color = BentoTileGray,
          modifier = Modifier.size(36.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to Dashboard",
              tint = Color.Black,
              modifier = Modifier.size(18.dp),
            )
          }
        }
        Text(
          text = "Back to Dashboard",
          fontSize = 13.5.sp,
          fontWeight = FontWeight.Bold,
          color = BentoBluePrimary,
        )
      }

      Surface(
        onClick = onClose,
        shape = CircleShape,
        color = BentoTileGray,
        modifier = Modifier.size(36.dp).testTag("close_leave_screen_btn"),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = Color.Black,
            modifier = Modifier.size(18.dp),
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = "Leave Approvals & Management",
      fontSize = 22.sp,
      fontWeight = FontWeight.Bold,
      color = Color.Black,
    )
    Text(
      text = "Review, approve, edit, or delete employee leave requests",
      fontSize = 12.5.sp,
      color = Color.Black,
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Search bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search by worker name, reason, leave type...", color = BentoTextSecondary) },
      leadingIcon = {
        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Black)
      },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedBorderColor = BentoBluePrimary,
        unfocusedBorderColor = BentoOutline,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
      ),
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Filter Chips
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      FilterChip(
        selected = filterStatus == null,
        onClick = { filterStatus = null },
        label = { Text("All (${requests.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BentoBluePrimary, selectedLabelColor = Color.White),
        shape = RoundedCornerShape(10.dp),
      )
      FilterChip(
        selected = filterStatus == LeaveStatus.PENDING,
        onClick = { filterStatus = LeaveStatus.PENDING },
        label = { Text("Pending ($pendingCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BentoWarning, selectedLabelColor = Color.White),
        shape = RoundedCornerShape(10.dp),
      )
      FilterChip(
        selected = filterStatus == LeaveStatus.APPROVED,
        onClick = { filterStatus = LeaveStatus.APPROVED },
        label = { Text("Approved", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BentoSuccess, selectedLabelColor = Color.White),
        shape = RoundedCornerShape(10.dp),
      )
      FilterChip(
        selected = filterStatus == LeaveStatus.REJECTED,
        onClick = { filterStatus = LeaveStatus.REJECTED },
        label = { Text("Rejected", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BentoError, selectedLabelColor = Color.White),
        shape = RoundedCornerShape(10.dp),
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(bottom = 32.dp),
    ) {
      if (filteredRequests.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Icon(imageVector = Icons.Default.EventNote, contentDescription = null, tint = BentoTextSecondary, modifier = Modifier.size(40.dp))
              Spacer(modifier = Modifier.height(8.dp))
              Text("No leave requests found", fontWeight = FontWeight.Bold, color = Color.Black)
            }
          }
        }
      } else {
        items(visibleRequests, key = { it.id }) { req ->
          AdminLeaveItemCard(
            request = req,
            onApprove = { onApprove(req.id) },
            onReject = {
              requestToReject = req
              rejectReason = ""
            },
            onEdit = { requestToEdit = req },
            onDelete = { requestToDelete = req },
          )
        }

        if (visibleLeaveCount < filteredRequests.size) {
          item {
            Box(
              modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
              contentAlignment = Alignment.Center,
            ) {
              if (isLoadingMoreLeaves) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier.padding(12.dp),
                ) {
                  androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = BentoBluePrimary,
                    strokeWidth = 2.dp,
                  )
                  Text("Loading next 5 leave requests...", fontSize = 12.sp, color = BentoBluePrimary, fontWeight = FontWeight.Bold)
                }
              } else {
                OutlinedButton(
                  onClick = {
                    isLoadingMoreLeaves = true
                    leaveCoroutineScope.launch {
                      kotlinx.coroutines.delay(250)
                      visibleLeaveCount += 5
                      isLoadingMoreLeaves = false
                    }
                  },
                  shape = RoundedCornerShape(12.dp),
                  border = BorderStroke(1.dp, BentoBluePrimary),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoBluePrimary),
                ) {
                  Icon(
                    imageVector = Icons.Default.EventNote,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Load More Leave Requests (+5 of ${filteredRequests.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // Reject Reason Dialog
  if (requestToReject != null) {
    val req = requestToReject!!
    AlertDialog(
      onDismissRequest = { requestToReject = null },
      icon = { Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = BentoError) },
      title = { Text("Reject Leave Request / رفض طلب الإجازة", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 18.sp) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Are you sure you want to reject the leave request for ${req.workerName} (${req.totalDays} days)?",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
          )
          OutlinedTextField(
            value = rejectReason,
            onValueChange = { rejectReason = it },
            label = { Text("Reason for Rejection / سبب الرفض (Optional)", color = Color.Black, fontWeight = FontWeight.SemiBold) },
            placeholder = { Text("e.g. Project critical deadline / overlapping leave", color = Color.DarkGray) },
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 14.sp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.Black,
              unfocusedTextColor = Color.Black,
              focusedLabelColor = Color.Black,
              unfocusedLabelColor = Color.Black,
              focusedBorderColor = Color.Black,
              unfocusedBorderColor = Color.DarkGray,
              cursorColor = Color.Black,
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onReject(req.id, rejectReason.trim())
            requestToReject = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = BentoError),
          shape = RoundedCornerShape(10.dp),
        ) {
          Text("Confirm Reject", fontWeight = FontWeight.Bold, color = Color.White)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { requestToReject = null }, shape = RoundedCornerShape(10.dp)) {
          Text("Cancel / إلغاء", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      },
      containerColor = Color.White,
      shape = RoundedCornerShape(18.dp),
    )
  }

  // Delete Confirmation Dialog for Admin
  if (requestToDelete != null) {
    val req = requestToDelete!!
    AlertDialog(
      onDismissRequest = { requestToDelete = null },
      icon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = BentoError, modifier = Modifier.size(28.dp)) },
      title = { Text("Delete Leave Request", fontWeight = FontWeight.Bold, color = Color.Black) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Are you sure you want to permanently delete this leave request for ${req.workerName}?",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "• Type: ${req.leaveType.name} Leave\n• Period: ${if (req.startDate == req.endDate) req.startDate else "${req.startDate} to ${req.endDate}"}\n• Days: ${req.totalDays} day(s)\n• Current Status: ${req.status.name}",
            fontSize = 12.5.sp,
            color = Color.Black,
          )
          if (req.status == LeaveStatus.APPROVED) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Note: Since this request is currently Approved, deleting it will automatically refund the ${req.totalDays} day(s) back to the employee's available leave balance.",
              fontSize = 11.5.sp,
              color = BentoBluePrimary,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onDeleteRequest(req.id)
            requestToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = BentoError),
          shape = RoundedCornerShape(10.dp),
        ) {
          Text("Confirm Delete", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { requestToDelete = null }, shape = RoundedCornerShape(10.dp)) {
          Text("Cancel", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      },
      containerColor = Color.White,
      shape = RoundedCornerShape(18.dp),
    )
  }

  // Admin Edit Leave Dialog
  if (requestToEdit != null) {
    AdminEditLeaveDialog(
      request = requestToEdit!!,
      onDismiss = { requestToEdit = null },
      onSave = { updated ->
        onUpdateRequest(updated)
        requestToEdit = null
      },
    )
  }
}

@Composable
private fun AdminLeaveItemCard(
  request: LeaveRequest,
  onApprove: () -> Unit,
  onReject: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, BentoOutline),
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier =
              Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(
                  when (request.leaveType) {
                    LeaveType.ANNUAL -> BentoBlueContainer
                    LeaveType.CASUAL -> Color(0xFFFFF3E0)
                    LeaveType.SICK -> Color(0xFFE0F2F1)
                  }
                ),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector =
                when (request.leaveType) {
                  LeaveType.ANNUAL -> Icons.Default.CalendarMonth
                  LeaveType.CASUAL -> Icons.Default.EventAvailable
                  LeaveType.SICK -> Icons.Default.MedicalServices
                },
              contentDescription = null,
              tint =
                when (request.leaveType) {
                  LeaveType.ANNUAL -> BentoBluePrimary
                  LeaveType.CASUAL -> Color(0xFFE65100)
                  LeaveType.SICK -> Color(0xFF00897B)
                },
              modifier = Modifier.size(20.dp),
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = request.workerName,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color.Black,
            )
            Text(
              text = "${request.leaveType.name} Leave • ${if (request.totalDays % 1.0 == 0.0) request.totalDays.toInt() else request.totalDays} day(s) ${if (request.isHalfDay) "(Half Day)" else ""}",
              fontSize = 12.sp,
              color = Color.Black,
            )
          }
        }

        // Status Badge
        Box(
          modifier =
            Modifier
              .clip(RoundedCornerShape(50.dp))
              .background(
                when (request.status) {
                  LeaveStatus.APPROVED -> BentoSuccessContainer
                  LeaveStatus.REJECTED -> BentoErrorContainer
                  LeaveStatus.PENDING -> BentoWarningContainer
                }
              )
              .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
          Text(
            text = request.status.name,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color =
              when (request.status) {
                LeaveStatus.APPROVED -> BentoSuccess
                LeaveStatus.REJECTED -> BentoError
                LeaveStatus.PENDING -> BentoWarning
              },
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Dates
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BentoTileGray)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = Color.Black, modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (request.startDate == request.endDate) request.startDate else "${request.startDate} to ${request.endDate}",
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Bold,
          color = Color.Black,
        )
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Reason: ${request.reason}",
        fontSize = 12.sp,
        color = Color.Black,
      )

      if (!request.adminNotes.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Note: ${request.adminNotes}",
          fontSize = 11.sp,
          color = BentoWarning,
          fontWeight = FontWeight.Bold,
        )
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Submitted: ${request.requestDate}",
        fontSize = 10.5.sp,
        color = Color.Black,
      )

      Spacer(modifier = Modifier.height(8.dp))
      HorizontalDivider(color = Color(0xFFF1F5F9))
      Spacer(modifier = Modifier.height(8.dp))

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Admin Edit and Delete controls (available on ALL requests)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          OutlinedButton(
            onClick = onEdit,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.height(34.dp),
          ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp), tint = Color.Black)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Edit", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
          }

          OutlinedButton(
            onClick = onDelete,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoError),
            border = BorderStroke(1.dp, BentoError.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.height(34.dp),
          ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(14.dp), tint = BentoError)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Delete", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = BentoError)
          }
        }

        // Approve / Reject buttons for PENDING requests
        if (request.status == LeaveStatus.PENDING) {
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(
              onClick = onApprove,
              colors = ButtonDefaults.buttonColors(containerColor = BentoSuccess),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              modifier = Modifier.height(34.dp),
            ) {
              Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Approve", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
            }

            OutlinedButton(
              onClick = onReject,
              colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoError),
              border = BorderStroke(1.dp, BentoError.copy(alpha = 0.5f)),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              modifier = Modifier.height(34.dp),
            ) {
              Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Reject", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminEditLeaveDialog(
  request: LeaveRequest,
  onDismiss: () -> Unit,
  onSave: (updatedRequest: LeaveRequest) -> Unit,
) {
  val context = LocalContext.current
  val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH) }

  var selectedType by remember { mutableStateOf(request.leaveType) }
  var startDate by remember { mutableStateOf(request.startDate) }
  var endDate by remember { mutableStateOf(request.endDate) }
  var isHalfDay by remember { mutableStateOf(request.isHalfDay) }
  var selectedStatus by remember { mutableStateOf(request.status) }
  var reason by remember { mutableStateOf(request.reason) }
  var adminNotes by remember { mutableStateOf(request.adminNotes ?: "") }
  var typeExpanded by remember { mutableStateOf(false) }
  var statusExpanded by remember { mutableStateOf(false) }

  // Auto-calculated days
  val calculatedDays = remember(startDate, endDate, isHalfDay) {
    if (isHalfDay) {
      0.5
    } else {
      try {
        val s = sdf.parse(startDate)
        val e = sdf.parse(endDate)
        if (s != null && e != null) {
          val diff = e.time - s.time
          val d = (diff / (1000 * 60 * 60 * 24)).toDouble() + 1.0
          maxOf(1.0, d)
        } else {
          1.0
        }
      } catch (_: Exception) {
        1.0
      }
    }
  }

  // Date Pickers
  val startCal = remember {
    Calendar.getInstance().apply {
      try {
        val parsed = sdf.parse(startDate)
        if (parsed != null) time = parsed
      } catch (_: Exception) {}
    }
  }
  val startDatePickerDialog =
    remember {
      DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
          val formatted = String.format(Locale.ENGLISH, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
          startDate = formatted
          if (endDate < formatted) {
            endDate = formatted
          }
        },
        startCal.get(Calendar.YEAR),
        startCal.get(Calendar.MONTH),
        startCal.get(Calendar.DAY_OF_MONTH),
      )
    }

  val endCal = remember {
    Calendar.getInstance().apply {
      try {
        val parsed = sdf.parse(endDate)
        if (parsed != null) time = parsed
      } catch (_: Exception) {}
    }
  }
  val endDatePickerDialog =
    remember {
      DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
          val formatted = String.format(Locale.ENGLISH, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
          endDate = formatted
        },
        endCal.get(Calendar.YEAR),
        endCal.get(Calendar.MONTH),
        endCal.get(Calendar.DAY_OF_MONTH),
      )
    }

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Box(
        modifier = Modifier.size(44.dp).clip(CircleShape).background(BentoBlueContainer),
        contentAlignment = Alignment.Center,
      ) {
        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(24.dp))
      }
    },
    title = {
      Text(
        text = "Edit Leave Request (#${request.id})",
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        textAlign = TextAlign.Center,
      )
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        item {
          Text(
            text = "Worker: ${request.workerName}",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Black,
          )
        }

        // Leave Type Dropdown
        item {
          ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = !typeExpanded },
          ) {
            OutlinedTextField(
              value = "${selectedType.name} Leave",
              onValueChange = {},
              readOnly = true,
              label = { Text("Leave Type", color = Color.Black, fontWeight = FontWeight.SemiBold) },
              textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontWeight = FontWeight.Bold),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
              ),
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
              modifier = Modifier.menuAnchor().fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
            )
            ExposedDropdownMenu(
              expanded = typeExpanded,
              onDismissRequest = { typeExpanded = false },
              modifier = Modifier.background(Color.White),
            ) {
              LeaveType.entries.forEach { type ->
                DropdownMenuItem(
                  text = { Text("${type.name} Leave", color = Color.Black, fontWeight = FontWeight.Bold) },
                  onClick = {
                    selectedType = type
                    typeExpanded = false
                  },
                )
              }
            }
          }
        }

        // Status Dropdown
        item {
          ExposedDropdownMenuBox(
            expanded = statusExpanded,
            onExpandedChange = { statusExpanded = !statusExpanded },
          ) {
            OutlinedTextField(
              value = selectedStatus.name,
              onValueChange = {},
              readOnly = true,
              label = { Text("Approval Status", color = Color.Black, fontWeight = FontWeight.SemiBold) },
              textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontWeight = FontWeight.Bold),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
              ),
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
              modifier = Modifier.menuAnchor().fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
            )
            ExposedDropdownMenu(
              expanded = statusExpanded,
              onDismissRequest = { statusExpanded = false },
              modifier = Modifier.background(Color.White),
            ) {
              LeaveStatus.entries.forEach { status ->
                DropdownMenuItem(
                  text = {
                    Text(
                      text = status.name,
                      color = when (status) {
                        LeaveStatus.APPROVED -> BentoSuccess
                        LeaveStatus.REJECTED -> BentoError
                        LeaveStatus.PENDING -> BentoWarning
                      },
                      fontWeight = FontWeight.Bold,
                    )
                  },
                  onClick = {
                    selectedStatus = status
                    statusExpanded = false
                  },
                )
              }
            }
          }
        }

        // Half Day Toggle
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Checkbox(
              checked = isHalfDay,
              onCheckedChange = { checked ->
                isHalfDay = checked
                if (checked) {
                  endDate = startDate
                }
              },
              colors = CheckboxDefaults.colors(checkedColor = BentoBluePrimary),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Half Day (0.5 day)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
          }
        }

        // Start & End Dates
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            OutlinedButton(
              onClick = { startDatePickerDialog.show() },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp),
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Start Date", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
                Text(startDate, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
              }
            }

            OutlinedButton(
              onClick = { endDatePickerDialog.show() },
              enabled = !isHalfDay,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp),
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("End Date", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
                Text(if (isHalfDay) startDate else endDate, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
              }
            }
          }
        }

        // Calculated Total Days
        item {
          Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(BentoTileGray).padding(8.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = "Total Days: ${if (calculatedDays % 1.0 == 0.0) calculatedDays.toInt() else calculatedDays} day(s)",
              fontWeight = FontWeight.Bold,
              fontSize = 12.5.sp,
              color = Color.Black,
            )
          }
        }

        // Reason
        item {
          OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            label = { Text("Reason for Leave", color = Color.Black, fontWeight = FontWeight.SemiBold) },
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 14.sp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.Black,
              unfocusedTextColor = Color.Black,
              focusedLabelColor = Color.Black,
              unfocusedLabelColor = Color.Black,
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
          )
        }

        // Admin Notes
        item {
          OutlinedTextField(
            value = adminNotes,
            onValueChange = { adminNotes = it },
            label = { Text("Admin Notes / Decision Remarks", color = Color.Black, fontWeight = FontWeight.SemiBold) },
            placeholder = { Text("e.g. Approved as per manager request", color = Color.DarkGray) },
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 14.sp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.Black,
              unfocusedTextColor = Color.Black,
              focusedLabelColor = Color.Black,
              unfocusedLabelColor = Color.Black,
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val updated = request.copy(
            leaveType = selectedType,
            startDate = startDate,
            endDate = if (isHalfDay) startDate else endDate,
            isHalfDay = isHalfDay,
            totalDays = calculatedDays,
            status = selectedStatus,
            reason = reason.trim(),
            adminNotes = adminNotes.trim().ifEmpty { null },
            reviewDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).format(Date()),
          )
          onSave(updated)
        },
        colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
        shape = RoundedCornerShape(10.dp),
      ) {
        Text("Save Changes", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
        Text("Cancel", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
    containerColor = Color.White,
    shape = RoundedCornerShape(18.dp),
  )
}

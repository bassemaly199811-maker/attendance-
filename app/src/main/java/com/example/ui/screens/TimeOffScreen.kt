package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Sick
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkHistory
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.LeaveBalance
import com.example.data.model.LeaveRequest
import com.example.data.model.LeaveStatus
import com.example.data.model.LeaveType
import com.example.data.model.WorkerProfile
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeOffScreen(
  profile: WorkerProfile,
  leaveBalance: LeaveBalance?,
  leaveRequests: List<LeaveRequest>,
  isOnline: Boolean,
  onSubmitLeaveRequest: (
    type: LeaveType,
    startDate: String,
    endDate: String,
    isHalfDay: Boolean,
    reason: String,
  ) -> Unit,
  onCancelLeaveRequest: (requestId: Long) -> Unit = {},
  isSubmitting: Boolean = false,
  submissionSuccessMessage: String? = null,
  submissionErrorMessage: String? = null,
  showSuccessPopup: Boolean = false,
  onDismissSuccessPopup: () -> Unit = {},
  onClearSubmissionStatus: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  var showRequestDialog by remember { mutableStateOf(false) }

  val balance = leaveBalance ?: LeaveBalance(workerId = profile.id)

  Scaffold(
    modifier = modifier.fillMaxSize().testTag("time_off_screen"),
    containerColor = Color.Transparent,
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          onClearSubmissionStatus()
          showRequestDialog = true
        },
        containerColor = BentoBluePrimary,
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("request_leave_fab"),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = "Request Time Off")
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "Request Leave", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
      }
    },
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
    ) {
      // 1. Header
      item {
        Column {
          Text(
            text = "Time Off & Leave",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
          )
          Text(
            text = "Manage your leave balance and view request history",
            fontSize = 12.5.sp,
            color = Color.Black,
          )
        }
      }

      // 2. Success/Error Banner
      item {
        AnimatedVisibility(
          visible = !submissionSuccessMessage.isNullOrBlank(),
          enter = fadeIn(),
          exit = fadeOut(),
        ) {
          if (submissionSuccessMessage != null) {
            Box(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(14.dp))
                  .background(BentoSuccessContainer)
                  .padding(14.dp),
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = BentoSuccess,
                  modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "Request Sent!",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoSuccess,
                  )
                  Text(
                    text = "Request sent! Waiting for approval from management.",
                    fontSize = 12.sp,
                    color = Color.Black,
                  )
                }
              }
            }
          }
        }
      }

      // 3. Leave Balances Bento Grid
      item {
        Text(
          text = "Available Leave Balances",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = Color.Black,
        )
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          // Annual Leave
          BalanceCard(
            title = "Annual",
            total = balance.annualTotal,
            used = balance.annualUsed,
            available = balance.annualAvailable,
            color = BentoBluePrimary,
            bgColor = BentoBlueContainer,
            modifier = Modifier.weight(1f),
          )

          // Casual Leave
          BalanceCard(
            title = "Casual",
            total = balance.casualTotal,
            used = balance.casualUsed,
            available = balance.casualAvailable,
            color = Color(0xFFE65100),
            bgColor = Color(0xFFFFF3E0),
            modifier = Modifier.weight(1f),
          )

          // Sick Leave
          BalanceCard(
            title = "Sick",
            total = balance.sickTotal,
            used = balance.sickUsed,
            available = balance.sickAvailable,
            color = Color(0xFF00897B),
            bgColor = Color(0xFFE0F2F1),
            modifier = Modifier.weight(1f),
          )
        }
      }

      // 4. Leave Request History Title
      item {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Leave Request History",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
          )
          Text(
            text = "${leaveRequests.size} total",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
          )
        }
      }

      // 5. Leave Request Items or Empty State
      if (leaveRequests.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BentoOutline),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
            ) {
              Box(
                modifier =
                  Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(BentoTileGray),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Default.EventNote,
                  contentDescription = null,
                  tint = Color.Black,
                  modifier = Modifier.size(28.dp),
                )
              }
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "No record requested",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "You have not submitted any leave requests yet. Tap \"Request Leave\" below to create one.",
                fontSize = 12.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
              )
            }
          }
        }
      } else {
        items(leaveRequests, key = { it.id }) { request ->
          LeaveRequestItemCard(
            request = request,
            onCancel = { onCancelLeaveRequest(request.id) },
          )
        }
      }
    }
  }

  // Success Confirmation Popup
  if (showSuccessPopup) {
    AlertDialog(
      onDismissRequest = {
        onDismissSuccessPopup()
        onClearSubmissionStatus()
      },
      icon = {
        Box(
          modifier = Modifier.size(48.dp).clip(CircleShape).background(BentoSuccessContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = BentoSuccess,
            modifier = Modifier.size(28.dp),
          )
        }
      },
      title = {
        Text(
          text = "Request Submitted",
          fontWeight = FontWeight.Bold,
          color = Color.Black,
          textAlign = TextAlign.Center,
        )
      },
      text = {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Request sent! Waiting for approval",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = BentoSuccess,
            textAlign = TextAlign.Center,
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Your leave request has been sent to management for review. You can track its status or cancel it while it is pending.",
            fontSize = 12.5.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = 17.sp,
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onDismissSuccessPopup()
            onClearSubmissionStatus()
          },
          colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text("Got it", fontWeight = FontWeight.Bold)
        }
      },
      containerColor = Color.White,
      shape = RoundedCornerShape(20.dp),
    )
  }

  // Request Leave Dialog
  if (showRequestDialog) {
    RequestLeaveDialog(
      isOnline = isOnline,
      isSubmitting = isSubmitting,
      errorMessage = submissionErrorMessage,
      onSubmit = { type, start, end, halfDay, reason ->
        showRequestDialog = false
        onSubmitLeaveRequest(type, start, end, halfDay, reason)
      },
      onDismiss = {
        if (!isSubmitting) {
          showRequestDialog = false
        }
      },
    )
  }
}

@Composable
private fun BalanceCard(
  title: String,
  total: Double,
  used: Double,
  available: Double,
  color: Color,
  bgColor: Color,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, BentoOutline),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(
        modifier =
          Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = title.take(1),
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = color,
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "${if (available % 1.0 == 0.0) available.toInt() else available}",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = color,
      )
      Text(
        text = "days left",
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = Color.Black,
      )
      Spacer(modifier = Modifier.height(6.dp))
      HorizontalDivider(color = Color(0xFFE2E8F0))
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Total: ${total.toInt()}d | Used: ${if (used % 1.0 == 0.0) used.toInt() else used}d",
        fontSize = 9.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
      )
    }
  }
}

@Composable
private fun LeaveRequestItemCard(
  request: LeaveRequest,
  onCancel: () -> Unit = {},
) {
  var showCancelConfirmDialog by remember { mutableStateOf(false) }

  val (statusBg, statusColor, statusText, statusIcon) =
    when (request.status) {
      LeaveStatus.APPROVED ->
        Tuple4(BentoSuccessContainer, BentoSuccess, "Approved", Icons.Default.CheckCircle)
      LeaveStatus.REJECTED ->
        Tuple4(BentoErrorContainer, BentoError, "Rejected", Icons.Default.Close)
      LeaveStatus.PENDING ->
        Tuple4(BentoWarningContainer, BentoWarning, "Pending Approval", Icons.Default.PendingActions)
    }

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
                .size(36.dp)
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
              modifier = Modifier.size(18.dp),
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "${request.leaveType.name.replaceFirstChar { it.uppercase() }} Leave",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color.Black,
            )
            Text(
              text = "${if (request.totalDays % 1.0 == 0.0) request.totalDays.toInt() else request.totalDays} day(s) ${if (request.isHalfDay) "(Half Day)" else ""}",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Medium,
              color = Color.Black,
            )
          }
        }

        Box(
          modifier =
            Modifier
              .clip(RoundedCornerShape(50.dp))
              .background(statusBg)
              .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = statusIcon,
              contentDescription = null,
              tint = statusColor,
              modifier = Modifier.size(13.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = statusText,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = statusColor,
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Date Range
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(BentoTileGray)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = Icons.Default.DateRange,
          contentDescription = null,
          tint = Color.Black,
          modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (request.startDate == request.endDate) request.startDate else "${request.startDate} → ${request.endDate}",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = Color.Black,
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Reason / Notes
      Text(
        text = "Reason: ${request.reason}",
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = Color.Black,
        lineHeight = 16.sp,
      )

      if (!request.adminNotes.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Admin Note: ${request.adminNotes}",
          fontSize = 11.sp,
          color = BentoWarning,
          fontWeight = FontWeight.Bold,
        )
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Submitted: ${request.requestDate}",
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Medium,
        color = Color.Black,
      )

      // Worker Cancellation Option (ONLY available if request is PENDING, NOT when APPROVED)
      if (request.status == LeaveStatus.PENDING) {
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Color(0xFFF1F5F9))
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
        ) {
          OutlinedButton(
            onClick = { showCancelConfirmDialog = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoError),
            border = BorderStroke(1.dp, BentoError.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.height(36.dp),
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = null,
              tint = BentoError,
              modifier = Modifier.size(15.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Cancel Request",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = BentoError,
            )
          }
        }
      }
    }
  }

  // Cancel Confirmation Dialog for Worker
  if (showCancelConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showCancelConfirmDialog = false },
      icon = {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = null,
          tint = BentoError,
          modifier = Modifier.size(28.dp),
        )
      },
      title = {
        Text("Cancel Leave Request", fontWeight = FontWeight.Bold, color = Color.Black)
      },
      text = {
        Text(
          "Are you sure you want to cancel this ${request.leaveType.name} leave request (${if (request.startDate == request.endDate) request.startDate else "${request.startDate} to ${request.endDate}"})?",
          color = Color.Black,
          fontSize = 13.5.sp,
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showCancelConfirmDialog = false
            onCancel()
          },
          colors = ButtonDefaults.buttonColors(containerColor = BentoError),
          shape = RoundedCornerShape(10.dp),
        ) {
          Text("Yes, Cancel Request", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = { showCancelConfirmDialog = false },
          shape = RoundedCornerShape(10.dp),
        ) {
          Text("No, Keep", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      },
      containerColor = Color.White,
      shape = RoundedCornerShape(18.dp),
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RequestLeaveDialog(
  isOnline: Boolean,
  isSubmitting: Boolean,
  errorMessage: String?,
  onSubmit: (type: LeaveType, start: String, end: String, halfDay: Boolean, reason: String) -> Unit,
  onDismiss: () -> Unit,
) {
  val context = LocalContext.current
  val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
  var selectedType by remember { mutableStateOf(LeaveType.ANNUAL) }
  var startDate by remember { mutableStateOf(todayStr) }
  var endDate by remember { mutableStateOf(todayStr) }
  var isHalfDay by remember { mutableStateOf(false) }
  var reason by remember { mutableStateOf("") }
  var formValidationError by remember { mutableStateOf<String?>(null) }

  fun showDatePickerDialog(currentValue: String, isForStart: Boolean) {
    val cal = Calendar.getInstance()
    try {
      val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(currentValue)
      if (parsed != null) cal.time = parsed
    } catch (_: Exception) {}

    val datePicker =
      DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
          val selectedDate = String.format(Locale.ENGLISH, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
          if (isForStart) {
            startDate = selectedDate
            if (isHalfDay || endDate < selectedDate) {
              endDate = selectedDate
            }
          } else {
            if (selectedDate >= startDate) {
              endDate = selectedDate
            } else {
              endDate = startDate
            }
          }
          formValidationError = null
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH),
      )
    datePicker.show()
  }

  AlertDialog(
    onDismissRequest = { if (!isSubmitting) onDismiss() },
    properties = DialogProperties(usePlatformDefaultWidth = false),
    modifier = Modifier.padding(20.dp).fillMaxWidth(),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.EventAvailable,
            contentDescription = null,
            tint = BentoBluePrimary,
            modifier = Modifier.size(24.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Request Time Off", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.Black)
        }
        IconButton(onClick = onDismiss, enabled = !isSubmitting) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Leave Type Selector
        Text(text = "Leave Type", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          LeaveType.values().forEach { type ->
            FilterChip(
              selected = selectedType == type,
              onClick = { selectedType = type },
              label = {
                Text(
                  text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Bold,
                )
              },
              colors =
                FilterChipDefaults.filterChipColors(
                  selectedContainerColor = BentoBluePrimary,
                  selectedLabelColor = Color.White,
                  labelColor = Color.Black,
                ),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f),
            )
          }
        }

        // Dates Row with Pop-up Calendar Clickables
        Text(text = "Select Date Range (Tap to Pick from Calendar)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          // Start Date Field (Clickable to open pop-up calendar)
          Box(
            modifier =
              Modifier
                .weight(1f)
                .clickable(enabled = !isSubmitting) {
                  showDatePickerDialog(startDate, true)
                },
          ) {
            OutlinedTextField(
              value = startDate,
              onValueChange = {},
              readOnly = true,
              enabled = false,
              label = { Text("Start Date", color = Color.Black, fontWeight = FontWeight.Bold) },
              trailingIcon = {
                Icon(
                  imageVector = Icons.Default.DateRange,
                  contentDescription = "Pick Start Date",
                  tint = BentoBluePrimary,
                  modifier = Modifier.clickable { showDatePickerDialog(startDate, true) },
                )
              },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors =
                OutlinedTextFieldDefaults.colors(
                  disabledTextColor = Color.Black,
                  disabledLabelColor = Color.Black,
                  disabledBorderColor = BentoOutline,
                  disabledContainerColor = BentoTileGray.copy(alpha = 0.5f),
                ),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            )
          }

          // End Date Field (Clickable to open pop-up calendar)
          Box(
            modifier =
              Modifier
                .weight(1f)
                .clickable(enabled = !isSubmitting && !isHalfDay) {
                  showDatePickerDialog(endDate, false)
                },
          ) {
            OutlinedTextField(
              value = if (isHalfDay) startDate else endDate,
              onValueChange = {},
              readOnly = true,
              enabled = false,
              label = { Text("End Date", color = Color.Black, fontWeight = FontWeight.Bold) },
              trailingIcon = {
                if (!isHalfDay) {
                  Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Pick End Date",
                    tint = BentoBluePrimary,
                    modifier = Modifier.clickable { showDatePickerDialog(endDate, false) },
                  )
                }
              },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors =
                OutlinedTextFieldDefaults.colors(
                  disabledTextColor = Color.Black,
                  disabledLabelColor = Color.Black,
                  disabledBorderColor = BentoOutline,
                  disabledContainerColor = BentoTileGray.copy(alpha = 0.5f),
                ),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            )
          }
        }

        // Half-Day Toggle Option
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Checkbox(
            checked = isHalfDay,
            onCheckedChange = {
              isHalfDay = it
              if (it) {
                endDate = startDate
              }
            },
            colors = CheckboxDefaults.colors(checkedColor = BentoBluePrimary),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Half-day leave (0.5 day duration)",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
          )
        }

        // Mandatory Reason Field
        OutlinedTextField(
          value = reason,
          onValueChange = {
            reason = it
            if (it.isNotBlank()) formValidationError = null
          },
          label = { Text("Reason / Additional Notes *", color = Color.Black, fontWeight = FontWeight.Bold) },
          placeholder = { Text("Please explain the reason for your leave request...", color = Color.Black) },
          minLines = 3,
          maxLines = 4,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors =
            OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.Black,
              unfocusedTextColor = Color.Black,
              focusedBorderColor = BentoBluePrimary,
              unfocusedBorderColor = BentoOutline,
            ),
          textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color.Black),
        )

        // Error message if any
        val displayedError = formValidationError ?: errorMessage
        if (!displayedError.isNullOrBlank()) {
          Box(
            modifier =
              Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(BentoErrorContainer)
                .padding(10.dp),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = BentoError,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = displayedError,
                fontSize = 12.sp,
                color = BentoError,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (reason.isBlank()) {
            formValidationError = "Please provide a reason for the leave request."
            return@Button
          }
          if (startDate.isBlank() || endDate.isBlank()) {
            formValidationError = "Please select valid start and end dates."
            return@Button
          }
          if (!isOnline) {
            formValidationError = "Internet connection is required to submit leave request."
            return@Button
          }
          formValidationError = null
          onSubmit(selectedType, startDate.trim(), endDate.trim(), isHalfDay, reason.trim())
        },
        enabled = !isSubmitting,
        colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
        shape = RoundedCornerShape(12.dp),
      ) {
        if (isSubmitting) {
          CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            color = Color.White,
            strokeWidth = 2.dp,
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Sending...")
        } else {
          Text("Submit Request", fontWeight = FontWeight.Bold)
        }
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        enabled = !isSubmitting,
        shape = RoundedCornerShape(12.dp),
      ) {
        Text("Cancel", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
    shape = RoundedCornerShape(20.dp),
    containerColor = Color.White,
  )
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

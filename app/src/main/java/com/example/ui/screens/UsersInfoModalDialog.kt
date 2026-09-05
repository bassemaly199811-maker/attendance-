package com.example.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.service.CloudSyncService
import com.example.ui.theme.*
import com.example.ui.components.EditWorkerLeaveBalanceDialog
import com.example.ui.components.BentoDatePickerField
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

enum class WorkerDetailTab(val title: String, val titleAr: String, val icon: ImageVector) {
  LEAVES("Leaves", "الإجازات", Icons.Default.EventNote),
  ATTENDANCE("Attendance", "الحضور والانصراف", Icons.Default.Schedule),
  DOCUMENTS("Documents", "الوثائق والأوراق", Icons.Default.Description),
  ACCOUNT("Account & Login", "حساب الدخول والأمان", Icons.Default.AdminPanelSettings),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersInfoModalDialog(
  workers: List<WorkerEntity>,
  records: List<AttendanceRecord>,
  leaveRequests: List<LeaveRequest>,
  leaveBalances: List<LeaveBalance>,
  isOnline: Boolean,
  users: List<UserAccount> = emptyList(),
  sites: List<WorkSite> = emptyList(),
  onDismiss: () -> Unit,
  onAddWorkerWithAccountClick: (() -> Unit)? = null,
  onEditWorkerWithAccountClick: ((WorkerEntity, UserAccount?) -> Unit)? = null,
  onDeleteWorkerClick: ((WorkerEntity) -> Unit)? = null,
  onResetDeviceBinding: ((String) -> Unit)? = null,
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
  onUpdateLeaveBalance: (
    workerId: String,
    annualTotal: Double,
    casualTotal: Double,
    sickTotal: Double,
    annualUsed: Double?,
    casualUsed: Double?,
    sickUsed: Double?,
  ) -> Unit = { _, _, _, _, _, _, _ -> },
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedRoleFilter by remember { mutableStateOf("All") }
  var selectedWorker by remember { mutableStateOf<WorkerEntity?>(null) }
  var isEditingDocuments by remember { mutableStateOf<WorkerEntity?>(null) }
  var feedbackSnackbarMessage by remember { mutableStateOf<String?>(null) }
  var workerToDeleteConfirm by remember { mutableStateOf<WorkerEntity?>(null) }
  var userToResetBindingConfirm by remember { mutableStateOf<UserAccount?>(null) }

  // Unique roles for filter chips
  val allRoles = remember(workers) {
    listOf("All") + workers.map { it.role.trim() }.filter { it.isNotEmpty() }.distinct()
  }

  // Filtered workers list
  val filteredWorkers = remember(workers, searchQuery, selectedRoleFilter, users) {
    val q = searchQuery.trim().lowercase()
    workers.filter { worker ->
      val linkedAccount = users.find { it.workerId == worker.id || it.workerName.equals(worker.fullName, ignoreCase = true) }
      val usernameMatch = linkedAccount?.username?.lowercase()?.contains(q) == true

      val matchesSearch = q.isEmpty() ||
        worker.fullName.lowercase().contains(q) ||
        worker.id.lowercase().contains(q) ||
        worker.role.lowercase().contains(q) ||
        worker.nationalId.lowercase().contains(q) ||
        worker.iqamaNumber.lowercase().contains(q) ||
        worker.phoneNumber.lowercase().contains(q) ||
        worker.siteName.lowercase().contains(q) ||
        usernameMatch

      val matchesRole = selectedRoleFilter == "All" || worker.role.equals(selectedRoleFilter, ignoreCase = true)
      matchesSearch && matchesRole
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = BentoBackground,
    ) {
      if (selectedWorker != null) {
        // Detailed Worker View
        val currentWorker = workers.find { it.id == selectedWorker!!.id } ?: selectedWorker!!
        val currentLinkedAccount = users.find { it.workerId == currentWorker.id || it.workerName.equals(currentWorker.fullName, ignoreCase = true) }
        WorkerFullDetailView(
          worker = currentWorker,
          userAccount = currentLinkedAccount,
          records = records.filter { it.workerName.equals(currentWorker.fullName, ignoreCase = true) },
          workerLeaveRequests = leaveRequests.filter { it.workerId == currentWorker.id },
          workerLeaveBalance = leaveBalances.find { it.workerId == currentWorker.id } ?: LeaveBalance(workerId = currentWorker.id),
          isOnline = isOnline,
          onBack = { selectedWorker = null },
          onEditDocuments = { isEditingDocuments = currentWorker },
          onEditWorkerAndAccount = {
            onEditWorkerWithAccountClick?.invoke(currentWorker, currentLinkedAccount)
          },
          onDeleteWorker = {
            workerToDeleteConfirm = currentWorker
          },
          onResetDeviceBinding = {
            if (currentLinkedAccount != null) {
              userToResetBindingConfirm = currentLinkedAccount
            }
          },
          onUpdateLeaveBalance = { annualTotal, casualTotal, sickTotal, annualUsed, casualUsed, sickUsed ->
            onUpdateLeaveBalance(currentWorker.id, annualTotal, casualTotal, sickTotal, annualUsed, casualUsed, sickUsed)
            feedbackSnackbarMessage = "Leave quota for ${currentWorker.fullName} updated successfully ✓"
          },
        )
      } else {
        // Main Search & User List View
        Scaffold(
          containerColor = BentoBackground,
          topBar = {
            Surface(
              color = Color.White,
              shadowElevation = 2.dp,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .statusBarsPadding()
                  .padding(horizontal = 16.dp, vertical = 12.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                      onClick = onDismiss,
                      modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(BentoBlueContainer)
                    ) {
                      Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                      Text(
                        text = "Workers & User Accounts",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                      )
                      Text(
                        text = "دليل الموظفين وإدارة حسابات المستخدمين",
                        fontSize = 11.5.sp,
                        color = BentoTextSecondary,
                      )
                    }
                  }

                  if (onAddWorkerWithAccountClick != null) {
                    Button(
                      onClick = onAddWorkerWithAccountClick,
                      shape = RoundedCornerShape(12.dp),
                      colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
                      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                      modifier = Modifier.height(38.dp),
                    ) {
                      Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("+ Add Staff", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                  } else {
                    Surface(
                      shape = RoundedCornerShape(12.dp),
                      color = BentoBluePrimary.copy(alpha = 0.1f),
                    ) {
                      Text(
                        text = "${filteredWorkers.size} Staff",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoBluePrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Box
                OutlinedTextField(
                  value = searchQuery,
                  onValueChange = { searchQuery = it },
                  placeholder = { Text("Search by name, ID, @username, phone, role, site...", color = Color.DarkGray, fontSize = 12.5.sp) },
                  leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Black) },
                  trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                      IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Black)
                      }
                    }
                  },
                  singleLine = true,
                  shape = RoundedCornerShape(14.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = BentoBluePrimary,
                    unfocusedBorderColor = BentoOutline,
                    focusedContainerColor = Color(0xFFF8F9FA),
                    unfocusedContainerColor = Color(0xFFF8F9FA),
                  ),
                  modifier = Modifier.fillMaxWidth(),
                )

                // Role Filter Chips
                if (allRoles.size > 2) {
                  Spacer(modifier = Modifier.height(8.dp))
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    allRoles.forEach { role ->
                      val isSelected = selectedRoleFilter == role
                      FilterChip(
                        selected = isSelected,
                        onClick = { selectedRoleFilter = role },
                        label = {
                          Text(
                            text = role,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) BentoBluePrimary else Color.Black,
                          )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                          selectedContainerColor = BentoBlueContainer,
                        ),
                      )
                    }
                  }
                }
              }
            }
          },
        ) { innerPadding ->
          if (filteredWorkers.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
              contentAlignment = Alignment.Center,
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(24.dp),
              ) {
                Icon(
                  Icons.Default.PersonSearch,
                  contentDescription = null,
                  tint = BentoTextSecondary,
                  modifier = Modifier.size(54.dp),
                )
                Text(
                  text = "No Workers Found / لم يتم العثور على موظفين",
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Black,
                )
                Text(
                  text = "Try adjusting your search keywords or tap '+ Add Staff' to register a new worker.",
                  fontSize = 13.sp,
                  color = BentoTextSecondary,
                  textAlign = TextAlign.Center,
                )
              }
            }
          } else {
            LazyColumn(
              modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              items(filteredWorkers, key = { "${it.id}_${it.fullName}" }) { worker ->
                val workerRecordsCount = records.count { it.workerName.equals(worker.fullName, ignoreCase = true) }
                val workerLeavesCount = leaveRequests.count { it.workerId == worker.id }
                val workerBalance = leaveBalances.find { it.workerId == worker.id } ?: LeaveBalance(workerId = worker.id)
                val linkedAccount = users.find { it.workerId == worker.id || it.workerName.equals(worker.fullName, ignoreCase = true) }

                UserInfoWorkerCard(
                  worker = worker,
                  userAccount = linkedAccount,
                  recordsCount = workerRecordsCount,
                  leavesCount = workerLeavesCount,
                  leaveBalance = workerBalance,
                  onClick = { selectedWorker = worker },
                  onEditClick = {
                    onEditWorkerWithAccountClick?.invoke(worker, linkedAccount)
                  },
                  onDeleteClick = {
                    workerToDeleteConfirm = worker
                  },
                )
              }

              item {
                Spacer(modifier = Modifier.height(24.dp))
              }
            }
          }
        }
      }
    }
  }

  // Delete Worker Confirmation Dialog
  if (workerToDeleteConfirm != null) {
    val targetWorker = workerToDeleteConfirm!!
    AlertDialog(
      onDismissRequest = { workerToDeleteConfirm = null },
      title = { Text("Delete Worker & User Account", fontWeight = FontWeight.Bold) },
      text = {
        Text("Are you sure you want to delete (${targetWorker.fullName}) and their linked user login account from the system?")
      },
      confirmButton = {
        Button(
          onClick = {
            onDeleteWorkerClick?.invoke(targetWorker)
            if (selectedWorker?.id == targetWorker.id) {
              selectedWorker = null
            }
            workerToDeleteConfirm = null
            feedbackSnackbarMessage = "Worker ${targetWorker.fullName} and linked account removed."
          },
          colors = ButtonDefaults.buttonColors(containerColor = BentoError),
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { workerToDeleteConfirm = null }) {
          Text("Cancel")
        }
      },
    )
  }

  // Reset Device Binding Confirmation Dialog
  if (userToResetBindingConfirm != null) {
    val targetUser = userToResetBindingConfirm!!
    AlertDialog(
      onDismissRequest = { userToResetBindingConfirm = null },
      title = { Text("Reset Device Binding", fontWeight = FontWeight.Bold) },
      text = {
        Text("Resetting hardware binding will unbind @${targetUser.username} from their current phone (${targetUser.boundDeviceModel.ifBlank { "Bound Device" }}), allowing them to login on a new device.")
      },
      confirmButton = {
        Button(
          onClick = {
            onResetDeviceBinding?.invoke(targetUser.username)
            userToResetBindingConfirm = null
            feedbackSnackbarMessage = "Device binding for @${targetUser.username} has been reset."
          },
          colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
        ) {
          Text("Reset Binding")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { userToResetBindingConfirm = null }) {
          Text("Cancel")
        }
      },
    )
  }

  // Document Editing Dialog
  if (isEditingDocuments != null) {
    val worker = isEditingDocuments!!
    EditWorkerDocumentsDialog(
      worker = worker,
      isOnline = isOnline,
      onDismiss = { isEditingDocuments = null },
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
        isEditingDocuments = null
        feedbackSnackbarMessage = "Document details for ${worker.fullName} updated successfully ✓"
      },
    )
  }

  // Feedback Notification Dialog / Toast
  if (feedbackSnackbarMessage != null) {
    AlertDialog(
      onDismissRequest = { feedbackSnackbarMessage = null },
      icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BentoSuccess) },
      title = { Text("Success / تم بنجاح", fontWeight = FontWeight.Bold, color = Color.Black) },
      text = { Text(feedbackSnackbarMessage!!, color = Color.Black, fontSize = 14.sp) },
      confirmButton = {
        Button(
          onClick = { feedbackSnackbarMessage = null },
          colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
        ) {
          Text("OK", color = Color.White)
        }
      },
      containerColor = Color.White,
      shape = RoundedCornerShape(18.dp),
    )
  }
}

// ==========================================
// --- WORKER SUMMARY CARD IN LIST ---
// ==========================================
@Composable
private fun UserInfoWorkerCard(
  worker: WorkerEntity,
  userAccount: UserAccount? = null,
  recordsCount: Int,
  leavesCount: Int,
  leaveBalance: LeaveBalance? = null,
  onClick: () -> Unit,
  onEditClick: () -> Unit = {},
  onDeleteClick: () -> Unit = {},
) {
  val balance = leaveBalance ?: LeaveBalance(workerId = worker.id)

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() },
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, BentoOutline),
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Avatar
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(if (userAccount?.role == UserRole.ADMIN) Color(0xFF673AB7) else BentoBluePrimary),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = worker.initials.ifBlank { worker.fullName.take(2).uppercase() },
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = worker.fullName,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color.Black,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            if (userAccount?.role == UserRole.ADMIN) {
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFEDE7F6),
              ) {
                Text(
                  text = "ADMIN",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF673AB7),
                  modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${worker.role} • ${worker.siteName.ifBlank { "Main Office" }}",
            fontSize = 12.sp,
            color = BentoTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )

          // User Account Credentials Badge
          Spacer(modifier = Modifier.height(3.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (userAccount != null) {
              Icon(Icons.Default.AccountCircle, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "@${userAccount.username}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = BentoBluePrimary,
              )
              if (userAccount.boundDeviceId.isNotBlank()) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = BentoSuccess, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                  text = "Bound",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = BentoSuccess,
                )
              }
            } else {
              Icon(Icons.Default.PersonOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "No Login Account",
                fontSize = 10.5.sp,
                color = Color.Gray,
              )
            }
          }
        }

        // Quick Actions (Edit & Delete)
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onEditClick,
            modifier = Modifier.size(34.dp),
          ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Worker & Account", tint = BentoBluePrimary, modifier = Modifier.size(18.dp))
          }
          IconButton(
            onClick = onDeleteClick,
            modifier = Modifier.size(34.dp),
          ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Worker", tint = BentoError, modifier = Modifier.size(18.dp))
          }
          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "View Details",
            tint = Color.Black,
            modifier = Modifier.size(20.dp),
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = BentoOutline.copy(alpha = 0.5f))
      Spacer(modifier = Modifier.height(8.dp))

      // Footer Quick Tags
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = BentoBlueContainer,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            ) {
              Icon(Icons.Default.Schedule, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "$recordsCount Logs",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = BentoBluePrimary,
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFFFF3E0),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            ) {
              Icon(Icons.Default.EventNote, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "$leavesCount Req",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100),
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF0FDF4),
            border = BorderStroke(0.5.dp, BentoSuccess.copy(alpha = 0.5f)),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            ) {
              Icon(Icons.Default.DateRange, contentDescription = null, tint = BentoSuccess, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${balance.annualAvailable.toInt()}d Ann",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = BentoSuccess,
              )
            }
          }
        }

        Text(
          text = "ID: ${worker.id}",
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          color = Color.Black,
        )
      }
    }
  }
}

// ==========================================
// --- COMPREHENSIVE WORKER DETAIL VIEW ---
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkerFullDetailView(
  worker: WorkerEntity,
  userAccount: UserAccount? = null,
  records: List<AttendanceRecord>,
  workerLeaveRequests: List<LeaveRequest>,
  workerLeaveBalance: LeaveBalance,
  isOnline: Boolean,
  onBack: () -> Unit,
  onEditDocuments: () -> Unit,
  onEditWorkerAndAccount: () -> Unit = {},
  onDeleteWorker: () -> Unit = {},
  onResetDeviceBinding: () -> Unit = {},
  onUpdateLeaveBalance: (
    annualTotal: Double,
    casualTotal: Double,
    sickTotal: Double,
    annualUsed: Double?,
    casualUsed: Double?,
    sickUsed: Double?,
  ) -> Unit = { _, _, _, _, _, _ -> },
) {
  var selectedTab by remember { mutableStateOf(WorkerDetailTab.LEAVES) }
  var showEditLeaveBalanceDialog by remember { mutableStateOf(false) }

  if (showEditLeaveBalanceDialog) {
    EditWorkerLeaveBalanceDialog(
      worker = worker,
      currentBalance = workerLeaveBalance,
      onDismiss = { showEditLeaveBalanceDialog = false },
      onSave = { annualTotal, casualTotal, sickTotal, annualUsed, casualUsed, sickUsed ->
        onUpdateLeaveBalance(annualTotal, casualTotal, sickTotal, annualUsed, casualUsed, sickUsed)
        showEditLeaveBalanceDialog = false
      },
    )
  }

  Scaffold(
    containerColor = BentoBackground,
    topBar = {
      Surface(
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp, bottom = 4.dp)
        ) {
          // Top Navigation Row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                onClick = onBack,
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(BentoBlueContainer)
              ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = worker.fullName,
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Black,
                )
                Text(
                  text = "ID: ${worker.id} • ${worker.role}" + if (userAccount != null) " • @${userAccount.username}" else "",
                  fontSize = 12.sp,
                  color = BentoTextSecondary,
                )
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                onClick = onEditWorkerAndAccount,
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(BentoBlueContainer)
              ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Profile & Login", tint = BentoBluePrimary, modifier = Modifier.size(18.dp))
              }
              Spacer(modifier = Modifier.width(6.dp))
              IconButton(
                onClick = onBack,
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFF0F0F0))
              ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Worker Summary Header Card (Condensed)
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
            border = BorderStroke(1.dp, BentoOutline),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(if (userAccount?.role == UserRole.ADMIN) Color(0xFF673AB7) else BentoBluePrimary),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = worker.initials.ifBlank { worker.fullName.take(2).uppercase() },
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp,
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Phone: ${worker.phoneNumber.ifBlank { "N/A" }}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = Color.Black,
                )
                Text(
                  text = "National / Iqama ID: ${worker.iqamaNumber.ifBlank { worker.nationalId.ifBlank { "N/A" } }}",
                  fontSize = 12.sp,
                  color = Color.Black,
                )
                Text(
                  text = "Site: ${worker.siteName.ifBlank { "All Sites" }}" + if (userAccount != null) " • @${userAccount.username} (${userAccount.role.name})" else "",
                  fontSize = 11.5.sp,
                  color = BentoTextSecondary,
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Four Sub-Tabs Selector
          TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color.White,
            contentColor = BentoBluePrimary,
            indicator = { tabPositions ->
              TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                color = BentoBluePrimary,
                height = 3.dp,
              )
            },
          ) {
            WorkerDetailTab.values().forEach { tab ->
              val isSelected = selectedTab == tab
              Tab(
                selected = isSelected,
                onClick = { selectedTab = tab },
                text = {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = tab.icon,
                      contentDescription = null,
                      tint = if (isSelected) BentoBluePrimary else Color.Black,
                      modifier = Modifier.size(15.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = tab.title,
                      fontSize = 11.5.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      color = if (isSelected) BentoBluePrimary else Color.Black,
                    )
                  }
                },
              )
            }
          }
        }
      }
    },
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (selectedTab) {
        WorkerDetailTab.LEAVES -> {
          WorkerLeavesSubView(
            worker = worker,
            leaveBalance = workerLeaveBalance,
            leaveRequests = workerLeaveRequests,
            onEditLeaveBalance = { showEditLeaveBalanceDialog = true },
          )
        }
        WorkerDetailTab.ATTENDANCE -> {
          WorkerAttendanceSubView(
            worker = worker,
            records = records,
          )
        }
        WorkerDetailTab.DOCUMENTS -> {
          WorkerDocumentsSubView(
            worker = worker,
            onEditDocuments = onEditDocuments,
          )
        }
        WorkerDetailTab.ACCOUNT -> {
          WorkerAccountSubView(
            worker = worker,
            userAccount = userAccount,
            onEditWorkerAndAccount = onEditWorkerAndAccount,
            onDeleteWorker = onDeleteWorker,
            onResetDeviceBinding = onResetDeviceBinding,
          )
        }
      }
    }
  }
}

// ==========================================
// --- WORKER USER ACCOUNT & ACCESS SUB-VIEW ---
// ==========================================
@Composable
private fun WorkerAccountSubView(
  worker: WorkerEntity,
  userAccount: UserAccount?,
  onEditWorkerAndAccount: () -> Unit,
  onDeleteWorker: () -> Unit,
  onResetDeviceBinding: () -> Unit,
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // 1. Account Credentials Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoOutline),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(if (userAccount?.role == UserRole.ADMIN) Color(0xFFEDE7F6) else BentoBlueContainer),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = if (userAccount?.role == UserRole.ADMIN) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                  contentDescription = null,
                  tint = if (userAccount?.role == UserRole.ADMIN) Color(0xFF673AB7) else BentoBluePrimary,
                  modifier = Modifier.size(20.dp),
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "User Login Account / حساب الدخول",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Black,
                )
                Text(
                  text = if (userAccount != null) "Credentials linked to this employee" else "No login account created yet",
                  fontSize = 11.sp,
                  color = BentoTextSecondary,
                )
              }
            }

            if (userAccount != null) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = BentoSuccessContainer,
              ) {
                Text(
                  text = "Active Account ✓",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = BentoSuccess,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
              }
            }
          }

          HorizontalDivider(color = BentoOutline.copy(alpha = 0.5f))

          if (userAccount != null) {
            DetailInfoRow(label = "Username / اسم المستخدم", value = "@${userAccount.username}", isBold = true)
            DetailInfoRow(label = "Account Role / الصلاحية", value = if (userAccount.role == UserRole.ADMIN) "Administrator (مدير نظام)" else "Worker (عامل / موظف)")
            DetailInfoRow(label = "Password / كلمة المرور", value = "•••••••• (Encrypted & Secure)")
            DetailInfoRow(label = "Worker ID Binding", value = userAccount.workerId.ifBlank { worker.id })
          } else {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color(0xFFFFF8E1),
              border = BorderStroke(1.dp, Color(0xFFFFD54F)),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "This worker does not have an active login account. Tap 'Edit Profile & Login' to create their username and password.",
                  fontSize = 12.sp,
                  color = Color(0xFF5D4037),
                )
              }
            }
          }
        }
      }
    }

    // 2. Hardware Device Binding & Biometrics Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoOutline),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center,
              ) {
                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = BentoSuccess, modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Hardware Device Binding / قفل الجهاز",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Black,
                )
                Text(
                  text = "Anti-proxy & device security protection",
                  fontSize = 11.sp,
                  color = BentoTextSecondary,
                )
              }
            }
          }

          HorizontalDivider(color = BentoOutline.copy(alpha = 0.5f))

          val boundDev = userAccount?.boundDeviceId ?: ""
          val boundModel = userAccount?.boundDeviceModel?.ifBlank { worker.deviceModel } ?: worker.deviceModel

          DetailInfoRow(label = "Approved Model / الجهاز المعتمد", value = boundModel.ifBlank { "Any Verified Android Phone" })
          DetailInfoRow(
            label = "Hardware UUID / معرف الجهاز",
            value = if (boundDev.isNotBlank()) boundDev else "Not Bound (Will lock on first login)",
            valueColor = if (boundDev.isNotBlank()) BentoSuccess else Color.DarkGray,
          )

          if (userAccount != null && boundDev.isNotBlank()) {
            OutlinedButton(
              onClick = onResetDeviceBinding,
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, BentoWarning),
              colors = ButtonDefaults.outlinedButtonColors(containerColor = BentoWarningContainer.copy(alpha = 0.3f)),
            ) {
              Icon(Icons.Default.LockReset, contentDescription = null, tint = BentoWarning, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Reset Device Binding / إلغاء قفل الجهاز", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoWarning)
            }
          }
        }
      }
    }

    // 3. Administrative Actions
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoOutline),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Staff Actions / إجراءات الموظف", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)

          Button(
            onClick = onEditWorkerAndAccount,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Edit Profile & User Account / تعديل البيانات والحساب", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }

          OutlinedButton(
            onClick = onDeleteWorker,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, BentoError),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = BentoErrorContainer.copy(alpha = 0.3f)),
          ) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = BentoError, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Delete Worker & Linked Account / حذف الموظف والحساب", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoError)
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

// ==========================================
// --- 1. WORKER LEAVES SUB-VIEW ---
// ==========================================
@Composable
private fun WorkerLeavesSubView(
  worker: WorkerEntity,
  leaveBalance: LeaveBalance,
  leaveRequests: List<LeaveRequest>,
  onEditLeaveBalance: () -> Unit = {},
) {
  var statusFilter by remember { mutableStateOf("All") }
  var visibleLeaveCount by remember { mutableIntStateOf(5) }
  var isLoadingMoreLeaves by remember { mutableStateOf(false) }
  val leaveCoroutineScope = rememberCoroutineScope()

  val filteredRequests = remember(leaveRequests, statusFilter) {
    when (statusFilter) {
      "Approved" -> leaveRequests.filter { it.status == LeaveStatus.APPROVED }
      "Rejected" -> leaveRequests.filter { it.status == LeaveStatus.REJECTED }
      "Pending" -> leaveRequests.filter { it.status == LeaveStatus.PENDING }
      else -> leaveRequests
    }
  }

  val visibleRequests = remember(filteredRequests, visibleLeaveCount) {
    filteredRequests.take(visibleLeaveCount)
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // 1. Leave Balances Summary Card
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = "Leave Balance Breakdown / رصيد الإجازات",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
          )
          Text(
            text = "Available, used, and allocated leave quota",
            fontSize = 10.5.sp,
            color = BentoTextSecondary,
          )
        }

        OutlinedButton(
          onClick = onEditLeaveBalance,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          border = BorderStroke(1.dp, BentoBluePrimary),
          shape = RoundedCornerShape(8.dp),
        ) {
          Icon(Icons.Default.DateRange, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("تعديل الرصيد / Edit", fontSize = 11.sp, color = BentoBluePrimary, fontWeight = FontWeight.Bold)
        }
      }
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        LeaveBalanceMiniCard(
          title = "Annual / سنوية",
          available = leaveBalance.annualAvailable,
          used = leaveBalance.annualUsed,
          total = leaveBalance.annualTotal,
          accentColor = BentoBluePrimary,
          modifier = Modifier.weight(1f),
        )
        LeaveBalanceMiniCard(
          title = "Casual / عارضة",
          available = leaveBalance.casualAvailable,
          used = leaveBalance.casualUsed,
          total = leaveBalance.casualTotal,
          accentColor = Color(0xFFE65100),
          modifier = Modifier.weight(1f),
        )
        LeaveBalanceMiniCard(
          title = "Sick / مرضية",
          available = leaveBalance.sickAvailable,
          used = leaveBalance.sickUsed,
          total = leaveBalance.sickTotal,
          accentColor = BentoSuccess,
          modifier = Modifier.weight(1f),
        )
      }
    }

    // 2. Request History Section Header
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Leave Requests History / سجل طلبات الإجازة",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = Color.Black,
        )

        Text(
          text = "${leaveRequests.size} Total",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = BentoBluePrimary,
        )
      }

      // Filter Chips
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        listOf("All", "Pending", "Approved", "Rejected").forEach { filter ->
          val isSelected = statusFilter == filter
          FilterChip(
            selected = isSelected,
            onClick = { statusFilter = filter },
            label = {
              Text(
                text = filter,
                fontSize = 11.5.sp,
                color = if (isSelected) BentoBluePrimary else Color.Black,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              )
            },
          )
        }
      }
    }

    // 3. Requests List
    if (filteredRequests.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, BentoOutline),
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            contentAlignment = Alignment.Center,
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Default.EventNote, contentDescription = null, tint = BentoTextSecondary, modifier = Modifier.size(36.dp))
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "No leave requests found for this filter",
                fontSize = 13.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
              )
            }
          }
        }
      }
    } else {
      items(visibleRequests, key = { it.id }) { request ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, BentoOutline),
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Leave Type & Status
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                      when (request.leaveType) {
                        LeaveType.ANNUAL -> BentoBlueContainer
                        LeaveType.CASUAL -> Color(0xFFFFF3E0)
                        LeaveType.SICK -> Color(0xFFE8F5E9)
                        else -> Color(0xFFF3E5F5)
                      }
                    ),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector = Icons.Default.EventNote,
                    contentDescription = null,
                    tint = when (request.leaveType) {
                      LeaveType.ANNUAL -> BentoBluePrimary
                      LeaveType.CASUAL -> Color(0xFFE65100)
                      LeaveType.SICK -> BentoSuccess
                      else -> Color(0xFF7E57C2)
                    },
                    modifier = Modifier.size(16.dp),
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = "${request.leaveType.name} Leave",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black,
                  )
                  Text(
                    text = "Request ID: #${request.id}",
                    fontSize = 11.sp,
                    color = BentoTextSecondary,
                  )
                }
              }

              // Status Badge
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (request.status) {
                  LeaveStatus.APPROVED -> Color(0xFFE8F5E9)
                  LeaveStatus.REJECTED -> Color(0xFFFFEBEE)
                  LeaveStatus.PENDING -> Color(0xFFFFF3E0)
                },
              ) {
                Text(
                  text = when (request.status) {
                    LeaveStatus.APPROVED -> "APPROVED ✓"
                    LeaveStatus.REJECTED -> "REJECTED ✕"
                    LeaveStatus.PENDING -> "PENDING ⏱"
                  },
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = when (request.status) {
                    LeaveStatus.APPROVED -> BentoSuccess
                    LeaveStatus.REJECTED -> BentoError
                    LeaveStatus.PENDING -> Color(0xFFE65100)
                  },
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dates & Duration
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF8F9FA))
                .padding(8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column {
                Text(
                  text = "Dates: ${request.startDate} ➔ ${request.endDate}",
                  fontSize = 12.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Black,
                )
                Text(
                  text = "Submitted: ${request.requestDate}",
                  fontSize = 11.sp,
                  color = Color.DarkGray,
                )
              }

              val daysStr = if (request.totalDays % 1.0 == 0.0) "${request.totalDays.toInt()} Days" else "${request.totalDays} Days"
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = BentoBluePrimary,
              ) {
                Text(
                  text = daysStr,
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                )
              }
            }

            // Worker's message / reason
            if (request.reason.isNotBlank()) {
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Reason / سبب الطلب:",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
              )
              Text(
                text = request.reason,
                fontSize = 12.sp,
                color = Color.Black,
                lineHeight = 16.sp,
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0xFFF1F3F4))
                  .padding(8.dp),
              )
            }

            // Admin response / Notes if rejected or approved
            if (!request.adminNotes.isNullOrBlank() || !request.approvedBy.isNullOrBlank()) {
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Admin Response / رد الإدارة:",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
              )
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (request.status == LeaveStatus.REJECTED) Color(0xFFFFEBEE) else Color(0xFFE8F5E9))
                  .padding(8.dp)
              ) {
                if (!request.adminNotes.isNullOrBlank()) {
                  Text(
                    text = request.adminNotes,
                    fontSize = 12.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium,
                  )
                }
                if (!request.approvedBy.isNullOrBlank()) {
                  Text(
                    text = "Reviewed by: ${request.approvedBy} on ${request.reviewDate ?: "N/A"}",
                    fontSize = 10.5.sp,
                    color = Color.DarkGray,
                  )
                }
              }
            }
          }
        }
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
                modifier = Modifier.padding(10.dp),
              ) {
                CircularProgressIndicator(
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
                  text = "Load More Requests (+5 of ${filteredRequests.size})",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                )
              }
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun LeaveBalanceMiniCard(
  title: String,
  available: Double,
  used: Double,
  total: Double,
  accentColor: Color,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, BentoOutline),
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = if (available % 1.0 == 0.0) "${available.toInt()}" else "$available",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = accentColor,
      )
      Text(
        text = "Days Left",
        fontSize = 9.5.sp,
        color = BentoTextSecondary,
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "Used: ${if (used % 1.0 == 0.0) used.toInt() else used}/${if (total % 1.0 == 0.0) total.toInt() else total}",
        fontSize = 9.sp,
        color = Color.DarkGray,
      )
    }
  }
}

// ==========================================
// --- 2. WORKER ATTENDANCE SUB-VIEW ---
// ==========================================
@Composable
private fun WorkerAttendanceSubView(
  worker: WorkerEntity,
  records: List<AttendanceRecord>,
) {
  var selectedRecordPhoto by remember { mutableStateOf<AttendanceRecord?>(null) }
  var selectedMonthFilter by remember { mutableStateOf("ALL") }
  var visibleAttendanceCount by remember { mutableIntStateOf(5) }
  var isLoadingMoreAttendance by remember { mutableStateOf(false) }
  val attendanceCoroutineScope = rememberCoroutineScope()
  val context = LocalContext.current

  // Sorted records by date descending
  val sortedRecords = remember(records) {
    records.sortedByDescending { it.workDate + (it.checkInTime ?: "") }
  }

  // Group by Month (YYYY-MM)
  val availableMonths = remember(sortedRecords) {
    sortedRecords.mapNotNull {
      if (it.workDate.length >= 7) it.workDate.take(7) else null
    }.distinct().sortedDescending()
  }

  val filteredRecords = remember(sortedRecords, selectedMonthFilter) {
    if (selectedMonthFilter == "ALL") {
      sortedRecords
    } else {
      sortedRecords.filter { it.workDate.startsWith(selectedMonthFilter) }
    }
  }

  val visibleRecords = remember(filteredRecords, visibleAttendanceCount) {
    filteredRecords.take(visibleAttendanceCount)
  }

  val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()) }
  val totalLogs = filteredRecords.size
  val onTimeLogs = filteredRecords.count {
    !it.isLate && !it.checkInTime.isNullOrBlank() && !it.checkOutTime.isNullOrBlank() &&
      it.notes?.contains("لم يتم تسجيل الخروج") != true &&
      it.notes?.contains("Auto-Closed", ignoreCase = true) != true &&
      it.notes?.contains("Missing Check-Out", ignoreCase = true) != true
  }
  val lateLogs = filteredRecords.count { it.isLate }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    // 1. Monthly Summary Header & Filter
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = "Monthly Attendance Summary / ملخص الحضور الشهري",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
          )
          Text(
            text = if (selectedMonthFilter == "ALL") "Summary across all months" else "Summary for month: $selectedMonthFilter",
            fontSize = 11.5.sp,
            color = BentoTextSecondary,
          )
        }
      }

      // Month Selection Filter Chips
      if (availableMonths.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          FilterChip(
            selected = selectedMonthFilter == "ALL",
            onClick = {
              selectedMonthFilter = "ALL"
              visibleAttendanceCount = 5
            },
            label = {
              Text(
                text = "All Months (${sortedRecords.size})",
                fontSize = 11.5.sp,
                fontWeight = if (selectedMonthFilter == "ALL") FontWeight.Bold else FontWeight.Medium,
              )
            },
          )

          availableMonths.forEach { monthKey ->
            val isSelected = selectedMonthFilter == monthKey
            val countForMonth = sortedRecords.count { it.workDate.startsWith(monthKey) }
            FilterChip(
              selected = isSelected,
              onClick = {
                selectedMonthFilter = monthKey
                visibleAttendanceCount = 5
              },
              label = {
                Text(
                  text = "$monthKey ($countForMonth)",
                  fontSize = 11.5.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                )
              },
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Monthly Metrics Cards
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        AttendanceStatPill(
          title = "Monthly Days",
          titleAr = "أيام الشهر",
          value = "$totalLogs",
          color = BentoBluePrimary,
          modifier = Modifier.weight(1f),
        )
        AttendanceStatPill(
          title = "On Time",
          titleAr = "حضور في الموعد",
          value = "$onTimeLogs",
          color = BentoSuccess,
          modifier = Modifier.weight(1f),
        )
        AttendanceStatPill(
          title = "Late Arrivals",
          titleAr = "تأخيرات",
          value = "$lateLogs",
          color = if (lateLogs > 0) BentoError else Color.Gray,
          modifier = Modifier.weight(1f),
        )
      }
    }

    // 2. Attendance Records List Header
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Attendance Logs / سجلات الحضور",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = Color.Black,
        )
        Text(
          text = "Showing ${visibleRecords.size} of ${filteredRecords.size}",
          fontSize = 11.5.sp,
          fontWeight = FontWeight.SemiBold,
          color = BentoBluePrimary,
        )
      }
    }

    if (filteredRecords.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, BentoOutline),
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            contentAlignment = Alignment.Center,
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Default.Schedule, contentDescription = null, tint = BentoTextSecondary, modifier = Modifier.size(36.dp))
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "No attendance records found for this period",
                fontSize = 13.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
              )
            }
          }
        }
      }
    } else {
      items(visibleRecords, key = { it.id }) { record ->
        val isRecordToday = record.workDate == todayDateStr
        val recordNote = record.notes ?: ""
        val isMissingCheckout = record.isMissingCheckOut || record.checkOutTime.isNullOrBlank()

        val (badgeText, badgeColor, badgeBg) = when {
          recordNote.contains("إجازة", ignoreCase = true) || recordNote.contains("Leave", ignoreCase = true) ->
            Triple("ON LEAVE", BentoBluePrimary, BentoBlueContainer)

          record.status == AttendanceStatus.NOT_CHECKED_IN && record.checkInTime.isNullOrBlank() ->
            Triple("ABSENT", BentoTextSecondary, BentoTileGray)

          isMissingCheckout -> {
            if (isRecordToday && record.status == AttendanceStatus.CHECKED_IN &&
              !recordNote.contains("لم يتم تسجيل الخروج") &&
              !recordNote.contains("Auto-Closed", ignoreCase = true)
            ) {
              if (record.isLate) Triple("LATE (ACTIVE)", BentoWarning, BentoWarningContainer)
              else Triple("ACTIVE NOW", BentoBluePrimary, BentoBlueContainer)
            } else {
              if (record.isLate) Triple("INCOMPLETE (LATE)", BentoError, BentoErrorContainer)
              else Triple("INCOMPLETE", BentoError, BentoErrorContainer)
            }
          }

          record.isLate && record.isEarlyDeparture ->
            Triple("LATE & EARLY EXIT", BentoError, BentoErrorContainer)

          record.isLate ->
            Triple("LATE ARRIVAL", BentoError, Color(0xFFFFEBEE))

          record.isEarlyDeparture ->
            Triple("EARLY EXIT", BentoWarning, Color(0xFFFFF3E0))

          else ->
            Triple("ON TIME ✓", BentoSuccess, Color(0xFFE8F5E9))
        }

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, BentoOutline),
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Date & Status Badge
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = record.workDate,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Black,
                )
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeBg,
              ) {
                Text(
                  text = badgeText,
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = badgeColor,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Check-in / Check-out Times
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF8F9FA))
                .padding(10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Login, contentDescription = null, tint = BentoSuccess, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Check-In: ", fontSize = 11.5.sp, color = Color.DarkGray)
                  Text(
                    text = record.checkInTime ?: "Not Registered",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                  )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Logout, contentDescription = null, tint = if (isMissingCheckout) BentoError else Color(0xFFE65100), modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Check-Out: ", fontSize = 11.5.sp, color = Color.DarkGray)
                  Text(
                    text = if (!record.checkOutTime.isNullOrBlank()) record.checkOutTime
                    else if (isRecordToday && record.status == AttendanceStatus.CHECKED_IN && !recordNote.contains("لم يتم تسجيل الخروج") && !recordNote.contains("Auto-Closed", ignoreCase = true)) "In Progress"
                    else "Missing (Incomplete)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMissingCheckout && (!isRecordToday || record.status != AttendanceStatus.CHECKED_IN)) BentoError else Color.Black,
                  )
                }
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = record.siteName.ifBlank { "Assigned Site" },
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color.Black,
                )
                if (record.checkInDistanceMeters != null) {
                  Text(
                    text = "Dist: ${record.checkInDistanceMeters.toInt()}m",
                    fontSize = 10.5.sp,
                    color = BentoTextSecondary,
                  )
                }
              }
            }

            // Row 3: Photo Verification & Timestamps (بالصور والوقت)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  Icons.Default.CameraAlt,
                  contentDescription = null,
                  tint = if (record.checkInPhotoBase64 != null || record.checkInPhotoUri != null) BentoBluePrimary else Color.Gray,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (record.checkInPhotoBase64 != null || record.checkInPhotoUri != null)
                    "Biometric Selfie Verified"
                  else
                    "GPS Verified Attendance",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  color = Color.Black,
                )
              }

              if (record.checkInPhotoBase64 != null || record.checkInPhotoUri != null) {
                OutlinedButton(
                  onClick = { selectedRecordPhoto = record },
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                  modifier = Modifier.height(28.dp),
                ) {
                  Icon(Icons.Default.Visibility, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("View Photo", fontSize = 10.5.sp, color = BentoBluePrimary, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }

      if (visibleAttendanceCount < filteredRecords.size) {
        item {
          Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
          ) {
            if (isLoadingMoreAttendance) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(10.dp),
              ) {
                CircularProgressIndicator(
                  modifier = Modifier.size(16.dp),
                  color = BentoBluePrimary,
                  strokeWidth = 2.dp,
                )
                Text("Loading next 5 attendance records...", fontSize = 12.sp, color = BentoBluePrimary, fontWeight = FontWeight.Bold)
              }
            } else {
              OutlinedButton(
                onClick = {
                  isLoadingMoreAttendance = true
                  attendanceCoroutineScope.launch {
                    kotlinx.coroutines.delay(250)
                    visibleAttendanceCount += 5
                    isLoadingMoreAttendance = false
                  }
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BentoBluePrimary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoBluePrimary),
              ) {
                Icon(
                  imageVector = Icons.Default.Schedule,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Load More Attendance Logs (+5 of ${filteredRecords.size})",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.5.sp,
                )
              }
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  // Photo Viewer Dialog
  if (selectedRecordPhoto != null) {
    val rec = selectedRecordPhoto!!
    AlertDialog(
      onDismissRequest = { selectedRecordPhoto = null },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.CameraAlt, contentDescription = null, tint = BentoBluePrimary)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Attendance Photo Record", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
        }
      },
      text = {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          val photoBitmap = remember(rec.checkInPhotoBase64, rec.checkInPhotoUri) {
            val base64Str = rec.checkInPhotoBase64
            if (!base64Str.isNullOrBlank()) {
              try {
                val clean = if (base64Str.contains(",")) base64Str.substringAfter(",") else base64Str
                val bytes = Base64.decode(clean, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
              } catch (e: Exception) {
                null
              }
            } else if (!rec.checkInPhotoUri.isNullOrBlank() && !rec.checkInPhotoUri.startsWith("http")) {
              try {
                val file = java.io.File(rec.checkInPhotoUri)
                if (file.exists()) {
                  BitmapFactory.decodeFile(file.absolutePath)
                } else null
              } catch (e: Exception) {
                null
              }
            } else {
              null
            }
          }

          if (photoBitmap != null) {
            Image(
              bitmap = photoBitmap.asImageBitmap(),
              contentDescription = "Check-in Photo",
              modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, BentoOutline, RoundedCornerShape(12.dp)),
            )
          } else {
            Box(
              modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE8F5E9)),
              contentAlignment = Alignment.Center,
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BentoSuccess, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Camera Biometric Verified", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
              }
            }
          }

          Text(
            text = "Worker: ${rec.workerName} • Date: ${rec.workDate}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
          )
          Text(
            text = "Time: ${rec.checkInTime ?: "N/A"} • Site: ${rec.siteName}",
            fontSize = 11.sp,
            color = Color.DarkGray,
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            CloudSyncService.openGoogleDriveFolder(context, rec.checkInDriveUrl)
            selectedRecordPhoto = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
          shape = RoundedCornerShape(10.dp),
        ) {
          Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Google Drive", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { selectedRecordPhoto = null }) {
          Text("Close", color = Color.Black)
        }
      },
      containerColor = Color.White,
      shape = RoundedCornerShape(18.dp),
    )
  }
}

@Composable
private fun AttendanceStatPill(
  title: String,
  titleAr: String,
  value: String,
  color: Color,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, BentoOutline),
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
      Spacer(modifier = Modifier.height(2.dp))
      Text(text = title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)
      Text(text = titleAr, fontSize = 8.5.sp, color = BentoTextSecondary, textAlign = TextAlign.Center)
    }
  }
}

// ==========================================
// --- 3. WORKER DOCUMENTS SUB-VIEW ---
// ==========================================
@Composable
private fun WorkerDocumentsSubView(
  worker: WorkerEntity,
  onEditDocuments: () -> Unit,
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Header & Edit Button
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = "Official Documents / الأوراق والوثائق",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
          )
          Text(
            text = "Iqama, medical insurance, passport & contract",
            fontSize = 11.5.sp,
            color = BentoTextSecondary,
          )
        }

        Button(
          onClick = onEditDocuments,
          colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        ) {
          Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Edit / تعديل", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
      }
    }

    // 1. Iqama / Residency Document Card
    item {
      DocumentDetailCard(
        title = "Iqama / Residency Card (الإقامة)",
        icon = Icons.Default.Badge,
        docNumber = worker.iqamaNumber.ifBlank { worker.nationalId.ifBlank { "Not Set" } },
        startDate = worker.iqamaStartDate.ifBlank { "Not Set" },
        endDate = worker.iqamaEndDate.ifBlank { "Not Set" },
        providerOrType = "National Residency Authority",
        accentColor = BentoBluePrimary,
      )
    }

    // 2. Medical Insurance Card
    item {
      DocumentDetailCard(
        title = "Medical Insurance (التأمين الطبي)",
        icon = Icons.Default.HealthAndSafety,
        docNumber = worker.insuranceNumber.ifBlank { "Not Set" },
        startDate = worker.insuranceStartDate.ifBlank { "Not Set" },
        endDate = worker.insuranceEndDate.ifBlank { "Not Set" },
        providerOrType = worker.insuranceProvider.ifBlank { "Health Insurance Partner" },
        accentColor = BentoSuccess,
      )
    }

    // 3. Passport & Nationality Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoOutline),
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFF3E0)),
              contentAlignment = Alignment.Center,
            ) {
              Icon(Icons.Default.Flight, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("Passport & Nationality / جواز السفر", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
              Text("International Travel & Identity", fontSize = 11.sp, color = BentoTextSecondary)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
              Text("Passport Number:", fontSize = 11.sp, color = Color.DarkGray)
              Text(
                text = worker.passportNumber.ifBlank { "Not Set" },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text("Nationality:", fontSize = 11.sp, color = Color.DarkGray)
              Text(
                text = worker.nationality.ifBlank { "Not Set" },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
              )
            }
          }
        }
      }
    }

    // 4. Employment Contract Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoOutline),
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3E5F5)),
              contentAlignment = Alignment.Center,
            ) {
              Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(0xFF7E57C2), modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("Employment Contract / عقد العمل", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
              Text("Company Employment Agreement", fontSize = 11.sp, color = BentoTextSecondary)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
              Text("Contract Expiry Date:", fontSize = 11.sp, color = Color.DarkGray)
              Text(
                text = worker.contractEndDate.ifBlank { "Open / Renewable" },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text("Employment Status:", fontSize = 11.sp, color = Color.DarkGray)
              Text(
                text = "Active & Regular",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BentoSuccess,
              )
            }
          }
        }
      }
    }

    // 5. Compensation & Service Tenure Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoOutline),
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFF8E1)),
              contentAlignment = Alignment.Center,
            ) {
              Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFFA000), modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("Compensation & Service Duration / الراتب ومدة الخدمة", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
              Text("Monthly Salary & Employment Dates", fontSize = 11.sp, color = BentoTextSecondary)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
              Text("Base Salary:", fontSize = 11.sp, color = Color.DarkGray)
              Text(
                text = if (worker.salary > 0) "${worker.salary.toInt()} SAR / Month" else "Not Specified",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (worker.salary > 0) BentoBluePrimary else Color.Black,
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text("Hire Date / Start:", fontSize = 11.sp, color = Color.DarkGray)
              Text(
                text = worker.hireDate.ifBlank { "2024-01-01" },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
              )
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun DocumentDetailCard(
  title: String,
  icon: ImageVector,
  docNumber: String,
  startDate: String,
  endDate: String,
  providerOrType: String,
  accentColor: Color,
) {
  val isExpired = remember(endDate) {
    try {
      if (endDate.isNotBlank() && endDate != "Not Set") {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val d = sdf.parse(endDate)
        d != null && d.before(Date())
      } else {
        false
      }
    } catch (_: Exception) {
      false
    }
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, if (isExpired) BentoError else BentoOutline),
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            Text(providerOrType, fontSize = 11.sp, color = BentoTextSecondary)
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isExpired) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
        ) {
          Text(
            text = if (isExpired) "EXPIRED ✕" else "VALID ✓",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isExpired) BentoError else BentoSuccess,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFFF8F9FA))
          .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column {
          Text("Document #:", fontSize = 11.sp, color = Color.DarkGray)
          Text(docNumber, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }

        Column {
          Text("Start Date:", fontSize = 11.sp, color = Color.DarkGray)
          Text(startDate, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        }

        Column(horizontalAlignment = Alignment.End) {
          Text("End Date:", fontSize = 11.sp, color = Color.DarkGray)
          Text(
            endDate,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isExpired) BentoError else Color.Black,
          )
        }
      }
    }
  }
}

// ==========================================
// --- 4. EDIT WORKER DOCUMENTS DIALOG ---
// ==========================================
@Composable
private fun EditWorkerDocumentsDialog(
  worker: WorkerEntity,
  isOnline: Boolean,
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

  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Edit, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text("Edit Worker Documents", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
          Text("تعديل أوراق ووثائق ${worker.fullName}", fontSize = 11.5.sp, color = BentoTextSecondary)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        if (errorMessage != null) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFFFEBEE),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = errorMessage!!,
              color = BentoError,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(8.dp),
            )
          }
        }

        Text("Iqama & Residency Details / الإقامة:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoBluePrimary)
        OutlinedTextField(
          value = iqamaNum,
          onValueChange = { iqamaNum = it },
          label = { Text("Iqama / National ID Number", fontSize = 11.sp, color = Color.Black) },
          colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          BentoDatePickerField(
            value = iqamaStart,
            onValueChange = { iqamaStart = it },
            label = "Start Date",
            modifier = Modifier.weight(1f),
          )
          BentoDatePickerField(
            value = iqamaEnd,
            onValueChange = { iqamaEnd = it },
            label = "End Date",
            modifier = Modifier.weight(1f),
          )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text("Medical Insurance Details / التأمين الطبي:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoSuccess)
        OutlinedTextField(
          value = insProvider,
          onValueChange = { insProvider = it },
          label = { Text("Insurance Provider (e.g. Bupa / Tawuniya)", fontSize = 11.sp, color = Color.Black) },
          colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = insNum,
          onValueChange = { insNum = it },
          label = { Text("Insurance Policy Number", fontSize = 11.sp, color = Color.Black) },
          colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          BentoDatePickerField(
            value = insStart,
            onValueChange = { insStart = it },
            label = "Start Date",
            modifier = Modifier.weight(1f),
          )
          BentoDatePickerField(
            value = insEnd,
            onValueChange = { insEnd = it },
            label = "End Date",
            modifier = Modifier.weight(1f),
          )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text("Passport & Contract / الجواز والعقد:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = passport,
            onValueChange = { passport = it },
            label = { Text("Passport Number", fontSize = 10.sp, color = Color.Black) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = nationality,
            onValueChange = { nationality = it },
            label = { Text("Nationality", fontSize = 10.sp, color = Color.Black) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black),
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
        }

        BentoDatePickerField(
          value = contractEnd,
          onValueChange = { contractEnd = it },
          label = "Contract End Date",
          modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text("Compensation & Service Tenure / الراتب والخدمة:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB78103))
        OutlinedTextField(
          value = salaryStr,
          onValueChange = { salaryStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
          label = { Text("Base Monthly Salary (SAR / QAR)", fontSize = 11.sp, color = Color.Black) },
          placeholder = { Text("e.g. 5000", fontSize = 10.sp) },
          colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          BentoDatePickerField(
            value = hireDate,
            onValueChange = { hireDate = it },
            label = "Hire Date",
            modifier = Modifier.weight(1f),
          )
          BentoDatePickerField(
            value = employmentEndDate,
            onValueChange = { employmentEndDate = it },
            label = "End Date",
            modifier = Modifier.weight(1f),
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (!isOnline) {
            errorMessage = "لا يمكن تعديل المستندات بدون اتصال بالإنترنت. يرجى التحقق من الشبكة."
            return@Button
          }
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
        shape = RoundedCornerShape(10.dp),
      ) {
        Text("Save Changes / حفظ التعديلات", color = Color.White, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
        Text("Cancel / إلغاء", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
    containerColor = Color.White,
    shape = RoundedCornerShape(18.dp),
  )
}

@Composable
private fun DetailInfoRow(
  label: String,
  value: String,
  isBold: Boolean = false,
  valueColor: Color = Color.Black,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      fontSize = 12.sp,
      color = BentoTextSecondary,
      modifier = Modifier.weight(1f),
    )
    Text(
      text = value,
      fontSize = 12.5.sp,
      fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
      color = valueColor,
      textAlign = TextAlign.End,
      modifier = Modifier.weight(1f),
    )
  }
}

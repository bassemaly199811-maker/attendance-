package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DeviceSecurityAlert
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.model.WorkerEntity
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
  users: List<UserAccount>,
  workers: List<WorkerEntity>,
  securityAlerts: List<DeviceSecurityAlert>,
  onAddUser: (UserAccount) -> Unit,
  onUpdateUser: (UserAccount) -> Unit,
  onDeleteUser: (username: String) -> Unit,
  onResetDeviceBinding: (username: String) -> Unit,
  onResolveSecurityAlert: (alertId: Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedTab by remember { mutableStateOf(0) }
  var searchQuery by remember { mutableStateOf("") }
  var showAddEditDialog by remember { mutableStateOf(false) }
  var userToEdit by remember { mutableStateOf<UserAccount?>(null) }
  var userToDelete by remember { mutableStateOf<UserAccount?>(null) }
  var userToResetBinding by remember { mutableStateOf<UserAccount?>(null) }

  val filteredUsers =
    users.filter {
      it.username.contains(searchQuery, ignoreCase = true) ||
        it.workerName.contains(searchQuery, ignoreCase = true) ||
        it.role.name.contains(searchQuery, ignoreCase = true)
    }

  Scaffold(
    modifier = modifier.fillMaxSize().testTag("user_management_screen"),
    containerColor = Color.Transparent,
    floatingActionButton = {
      if (selectedTab == 0) {
        FloatingActionButton(
          onClick = {
            userToEdit = null
            showAddEditDialog = true
          },
          containerColor = BentoBluePrimary,
          contentColor = Color.White,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.testTag("add_user_fab"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add User")
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Add User", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          }
        }
      }
    },
  ) { paddingValues ->
    Column(
      modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
    ) {
      // 1. Header
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "User & Access Management",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color =  Color.Black,
      )
      Text(
        text = "Manage system credentials, worker linkages, and device bindings",
        fontSize = 12.5.sp,
        color = BentoTextSecondary,
      )

      Spacer(modifier = Modifier.height(14.dp))

      // 2. Tabs: Users vs Security Alerts
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color.White,
        contentColor = BentoBluePrimary,
        modifier = Modifier.clip(RoundedCornerShape(14.dp)),
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("User Accounts (${users.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          },
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Security Alerts (${securityAlerts.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          },
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 3. Tab Content
      if (selectedTab == 0) {
        // Search Input
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search by username, worker name, role...", color = Color.DarkGray) },
          textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.5.sp, fontWeight = FontWeight.Medium),
          leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Black)
          },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors =
            OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.Black,
              unfocusedTextColor = Color.Black,
              focusedBorderColor = BentoBluePrimary,
              unfocusedBorderColor = Color(0xFFCCCCCC),
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
            ),
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(bottom = 80.dp),
        ) {
          if (filteredUsers.isEmpty()) {
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
                  Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = BentoTextSecondary, modifier = Modifier.size(40.dp))
                  Spacer(modifier = Modifier.height(8.dp))
                  Text("No user accounts found", fontWeight = FontWeight.Bold, color =  Color.Black)
                }
              }
            }
          } else {
            items(filteredUsers, key = { it.username }) { user ->
              UserAccountCard(
                user = user,
                onEdit = {
                  userToEdit = user
                  showAddEditDialog = true
                },
                onDelete = { userToDelete = user },
                onResetBinding = { userToResetBinding = user },
              )
            }
          }
        }
      } else {
        // Security Alerts Tab
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(bottom = 80.dp),
        ) {
          if (securityAlerts.isEmpty()) {
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
                  Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = BentoSuccess, modifier = Modifier.size(44.dp))
                  Spacer(modifier = Modifier.height(10.dp))
                  Text("No Security Alerts", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                  Spacer(modifier = Modifier.height(4.dp))
                  Text("All worker device bindings and login activities are secure.", fontSize = 12.sp, color = BentoTextSecondary)
                }
              }
            }
          } else {
            items(securityAlerts, key = { it.id }) { alert ->
              SecurityAlertCard(
                alert = alert,
                onResolve = { onResolveSecurityAlert(alert.id) },
              )
            }
          }
        }
      }
    }
  }

  // Add / Edit User Dialog
  if (showAddEditDialog) {
    AddEditUserDialog(
      userToEdit = userToEdit,
      availableWorkers = workers,
      onSave = { user ->
        if (userToEdit != null) {
          onUpdateUser(user)
        } else {
          onAddUser(user)
        }
        showAddEditDialog = false
        userToEdit = null
      },
      onDismiss = {
        showAddEditDialog = false
        userToEdit = null
      },
    )
  }

  // Delete Confirmation Dialog
  if (userToDelete != null) {
    val user = userToDelete!!
    AlertDialog(
      onDismissRequest = { userToDelete = null },
      icon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = BentoError) },
      title = { Text("Delete User Account?", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 17.sp) },
      text = {
        Text("Are you sure you want to delete user \"${user.username}\" (${user.workerName})? This action cannot be undone.", color = Color.Black, fontSize = 14.sp)
      },
      confirmButton = {
        Button(
          onClick = {
            onDeleteUser(user.username)
            userToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = BentoError, contentColor = Color.White),
          shape = RoundedCornerShape(10.dp),
        ) {
          Text("Delete Account", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = { userToDelete = null },
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
        ) {
          Text("Cancel", fontWeight = FontWeight.SemiBold, color = Color.Black)
        }
      },
      containerColor = Color.White,
      shape = RoundedCornerShape(18.dp),
    )
  }

  // Reset Binding Dialog
  if (userToResetBinding != null) {
    val user = userToResetBinding!!
    AlertDialog(
      onDismissRequest = { userToResetBinding = null },
      icon = { Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, tint = BentoWarning) },
      title = { Text("Reset Device Binding?", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 17.sp) },
      text = {
        Text("This will unbind \"${user.username}\" from ${user.boundDeviceModel.ifEmpty { "the current device" }}. The user will be able to log in and bind a new device on their next sign-in.", color = Color.Black, fontSize = 14.sp)
      },
      confirmButton = {
        Button(
          onClick = {
            onResetDeviceBinding(user.username)
            userToResetBinding = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary, contentColor = Color.White),
          shape = RoundedCornerShape(10.dp),
        ) {
          Text("Reset Binding", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = { userToResetBinding = null },
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
        ) {
          Text("Cancel", fontWeight = FontWeight.SemiBold, color = Color.Black)
        }
      },
      containerColor = Color.White,
      shape = RoundedCornerShape(18.dp),
    )
  }
}

@Composable
private fun UserAccountCard(
  user: UserAccount,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onResetBinding: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
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
                .size(40.dp)
                .clip(CircleShape)
                .background(if (user.role == UserRole.ADMIN) Color(0xFFEDE7F6) else BentoBlueContainer),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = if (user.role == UserRole.ADMIN) Icons.Default.AdminPanelSettings else Icons.Default.Person,
              contentDescription = null,
              tint = if (user.role == UserRole.ADMIN) Color(0xFF673AB7) else BentoBluePrimary,
              modifier = Modifier.size(22.dp),
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = user.username,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color =  Color.Black,
              )
              Spacer(modifier = Modifier.width(8.dp))
              // Role Badge
              Box(
                modifier =
                  Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (user.role == UserRole.ADMIN) Color(0xFFEDE7F6) else BentoBlueContainer)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
              ) {
                Text(
                  text = user.role.name,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (user.role == UserRole.ADMIN) Color(0xFF673AB7) else BentoBluePrimary,
                )
              }
            }
            Text(
              text = "${user.workerName} (${user.workerId})",
              fontSize = 12.sp,
              color = BentoTextSecondary,
            )
          }
        }

        // Action Buttons
        Row {
          IconButton(onClick = onEdit) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit User", tint = BentoBluePrimary, modifier = Modifier.size(19.dp))
          }
          IconButton(onClick = onDelete) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete User", tint = BentoError, modifier = Modifier.size(19.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = Color(0xFFF1F5F9))
      Spacer(modifier = Modifier.height(8.dp))

      // Device Binding Info & Reset Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Icon(
            imageVector = Icons.Default.PhoneAndroid,
            contentDescription = null,
            tint = if (user.boundDeviceId.isNotBlank()) BentoSuccess else BentoTextSecondary,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text =
              if (user.boundDeviceId.isNotBlank()) {
                "Bound: ${user.boundDeviceModel.ifEmpty { "1 Active Device" }}"
              } else {
                "Device: Not bound (open)"
              },
            fontSize = 11.5.sp,
            color = if (user.boundDeviceId.isNotBlank()) Color(0xFF1E293B) else BentoTextSecondary,
            fontWeight = if (user.boundDeviceId.isNotBlank()) FontWeight.Medium else FontWeight.Normal,
          )
        }

        if (user.boundDeviceId.isNotBlank()) {
          TextButton(
            onClick = onResetBinding,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
          ) {
            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp), tint = BentoWarning)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Reset Binding", fontSize = 11.sp, color = BentoWarning, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
private fun SecurityAlertCard(
  alert: DeviceSecurityAlert,
  onResolve: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = if (alert.isResolved) Color.White else BentoErrorContainer.copy(alpha = 0.5f)),
    border = BorderStroke(1.dp, if (alert.isResolved) BentoOutline else BentoError.copy(alpha = 0.5f)),
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = if (alert.isResolved) BentoTextSecondary else BentoError,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Unauthorized Device Attempt",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (alert.isResolved)  Color.Black else BentoError,
          )
        }

        if (!alert.isResolved) {
          Button(
            onClick = onResolve,
            colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
          ) {
            Text("Resolve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        } else {
          Text("Resolved ✓", fontSize = 11.sp, color = BentoSuccess, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Worker: ${alert.workerName} (${alert.username})",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        text = "Attempted Model: ${alert.attemptedDeviceModel}",
        fontSize = 11.5.sp,
        color = Color(0xFF334155),
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Timestamp: ${alert.timestamp}",
        fontSize = 10.5.sp,
        color = BentoTextSecondary,
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditUserDialog(
  userToEdit: UserAccount?,
  availableWorkers: List<WorkerEntity>,
  onSave: (UserAccount) -> Unit,
  onDismiss: () -> Unit,
) {
  var username by remember { mutableStateOf(userToEdit?.username ?: "") }
  var password by remember { mutableStateOf(userToEdit?.passwordHash ?: "") }
  var selectedRole by remember { mutableStateOf(userToEdit?.role ?: UserRole.WORKER) }
  var selectedWorker by remember {
    mutableStateOf(
      availableWorkers.find { it.id == userToEdit?.workerId } ?: availableWorkers.firstOrNull()
    )
  }
  var workerDropdownExpanded by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
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
            imageVector = if (userToEdit != null) Icons.Default.Edit else Icons.Default.PersonAdd,
            contentDescription = null,
            tint = BentoBluePrimary,
            modifier = Modifier.size(24.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (userToEdit != null) "Edit User Account" else "Add New User",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Username Field
        OutlinedTextField(
          value = username,
          onValueChange = {
            username = it
            errorMessage = null
          },
          label = { Text("Username *", color = Color.Black, fontWeight = FontWeight.SemiBold) },
          placeholder = { Text("e.g. jdoe", color = Color.DarkGray) },
          textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium),
          enabled = userToEdit == null, // Username is primary key
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors =
            OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.Black,
              unfocusedTextColor = Color.Black,
              disabledTextColor = Color.Black,
              focusedLabelColor = Color.Black,
              unfocusedLabelColor = Color.Black,
              disabledLabelColor = Color.Black,
              focusedBorderColor = BentoBluePrimary,
              unfocusedBorderColor = Color(0xFFCCCCCC),
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
            ),
        )

        // Password Field
        OutlinedTextField(
          value = password,
          onValueChange = {
            password = it
            errorMessage = null
          },
          label = { Text("Password *", color = Color.Black, fontWeight = FontWeight.SemiBold) },
          placeholder = { Text("Set account password", color = Color.DarkGray) },
          textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors =
            OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.Black,
              unfocusedTextColor = Color.Black,
              focusedLabelColor = Color.Black,
              unfocusedLabelColor = Color.Black,
              focusedBorderColor = BentoBluePrimary,
              unfocusedBorderColor = Color(0xFFCCCCCC),
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
            ),
        )

        // Role Selector
        Text(text = "User Role", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          UserRole.values().forEach { role ->
            FilterChip(
              selected = selectedRole == role,
              onClick = { selectedRole = role },
              label = {
                Text(
                  text = role.name,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                )
              },
              colors =
                FilterChipDefaults.filterChipColors(
                  selectedContainerColor = if (role == UserRole.ADMIN) Color(0xFF673AB7) else BentoBluePrimary,
                  selectedLabelColor = Color.White,
                  containerColor = Color(0xFFF1F5F9),
                  labelColor = Color.Black,
                ),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f),
            )
          }
        }

        // Link to Worker Profile (for Worker role)
        if (selectedRole == UserRole.WORKER) {
          Text(text = "Link to Worker Profile", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
          ExposedDropdownMenuBox(
            expanded = workerDropdownExpanded,
            onExpandedChange = { workerDropdownExpanded = !workerDropdownExpanded },
          ) {
            OutlinedTextField(
              value = selectedWorker?.let { "${it.fullName} (${it.id})" } ?: "Select Worker",
              onValueChange = {},
              readOnly = true,
              textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium),
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = workerDropdownExpanded) },
              modifier = Modifier.fillMaxWidth().menuAnchor(),
              shape = RoundedCornerShape(12.dp),
              colors =
                OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
            )
            ExposedDropdownMenu(
              expanded = workerDropdownExpanded,
              onDismissRequest = { workerDropdownExpanded = false },
              modifier = Modifier.background(Color.White),
            ) {
              availableWorkers.forEach { w ->
                DropdownMenuItem(
                  text = { Text("${w.fullName} (${w.id}) - ${w.role}", color = Color.Black, fontWeight = FontWeight.Medium) },
                  onClick = {
                    selectedWorker = w
                    workerDropdownExpanded = false
                  },
                )
              }
            }
          }
        }

        if (errorMessage != null) {
          Text(text = errorMessage!!, color = BentoError, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (username.isBlank() || password.isBlank()) {
            errorMessage = "Please enter both username and password."
            return@Button
          }
          val workerId = if (selectedRole == UserRole.ADMIN) "ADMIN-01" else (selectedWorker?.id ?: "EMP-9821")
          val workerName = if (selectedRole == UserRole.ADMIN) "System Administrator" else (selectedWorker?.fullName ?: "Worker")

          val account =
            (userToEdit ?: UserAccount(username = username.trim(), passwordHash = password.trim())).copy(
              username = username.trim(),
              passwordHash = password.trim(),
              role = selectedRole,
              workerId = workerId,
              workerName = workerName,
            )
          onSave(account)
        },
        colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
        shape = RoundedCornerShape(12.dp),
      ) {
        Text(if (userToEdit != null) "Update User" else "Create User", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
        Text("Cancel")
      }
    },
    shape = RoundedCornerShape(20.dp),
    containerColor = Color.White,
  )
}

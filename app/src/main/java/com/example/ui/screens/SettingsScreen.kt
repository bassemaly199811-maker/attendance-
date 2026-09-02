package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Layers
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityLog
import com.example.data.model.UserAccount
import com.example.data.model.WorkSite
import com.example.data.model.WorkerProfile
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.AlertDialog
import com.example.ui.theme.BentoBlueContainer
import com.example.ui.theme.BentoBluePrimary
import com.example.ui.theme.BentoError
import com.example.ui.theme.BentoErrorContainer
import com.example.ui.theme.BentoOutline
import com.example.ui.theme.BentoSuccess
import com.example.ui.theme.BentoSuccessContainer
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.BentoTileGray
import java.util.Locale

@Composable
fun SettingsScreen(
  profile: WorkerProfile,
  selectedSite: WorkSite,
  isOutsideSimulation: Boolean,
  fcmStatus: String,
  fcmToken: String?,
  activityLogs: List<ActivityLog>,
  currentUser: UserAccount? = null,
  mapTilerApiKey: String = "",
  onUpdateMapTilerKey: (String) -> Unit = {},
  onToggleSimulator: (Boolean) -> Unit,
  onResetToday: () -> Unit,
  onResetDeviceBinding: () -> Unit = {},
  onLogout: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val scrollState = rememberScrollState()
  var showLogoutConfirmDialog by remember { mutableStateOf(false) }
  val isAdmin = currentUser?.role == com.example.data.model.UserRole.ADMIN

  Column(
    modifier =
      modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 16.dp)
        .testTag("settings_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Spacer(modifier = Modifier.height(4.dp))

    // Header
    Text(
      text = "Settings & Security",
      fontSize = 22.sp,
      fontWeight = FontWeight.Bold,
      color =  Color.Black,
    )

    // 0. Account & Session Management Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      border = BorderStroke(1.dp, BentoOutline),
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier =
                Modifier
                  .size(46.dp)
                  .clip(CircleShape)
                  .background(BentoBlueContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                tint = BentoBluePrimary,
                modifier = Modifier.size(28.dp),
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = currentUser?.workerName?.ifBlank { profile.fullName } ?: profile.fullName,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
              )
              Text(
                text = "Username: @${currentUser?.username ?: "user"} • ${currentUser?.role?.name ?: "WORKER"}",
                fontSize = 12.sp,
                color = BentoTextSecondary,
              )
            }
          }

          Box(
            modifier =
              Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(BentoSuccessContainer)
                .padding(horizontal = 8.dp, vertical = 4.dp),
          ) {
            Text(
              text = "Active Session",
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Bold,
              color = BentoSuccess,
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(BentoTileGray.copy(alpha = 0.5f))
              .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column {
            Text(text = "Bound Device", fontSize = 11.sp, color = BentoTextSecondary)
            Text(
              text = currentUser?.boundDeviceModel ?: "This Device (Bound)",
              fontSize = 12.5.sp,
              fontWeight = FontWeight.SemiBold,
            )
          }
          Column {
            Text(text = "Device ID", fontSize = 11.sp, color = BentoTextSecondary)
            Text(
              text = currentUser?.boundDeviceId?.take(12)?.plus("...") ?: "SECURE_HW_ID",
              fontSize = 12.5.sp,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
          onClick = { showLogoutConfirmDialog = true },
          modifier = Modifier.fillMaxWidth().height(44.dp),
          colors = ButtonDefaults.buttonColors(containerColor = BentoError),
          shape = RoundedCornerShape(12.dp),
        ) {
          Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Log Out", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
      }
    }


    // 1. Worker Profile Bento Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      border = BorderStroke(1.dp, BentoOutline),
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier =
                Modifier
                  .size(46.dp)
                  .clip(CircleShape)
                  .background(BentoBlueContainer),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = profile.initials,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = BentoBluePrimary,
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = profile.fullName,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
              )
              Text(
                text = profile.role,
                fontSize = 12.sp,
                color = BentoTextSecondary,
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(BentoTileGray.copy(alpha = 0.5f))
              .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column {
            Text(text = "National ID", fontSize = 11.sp, color = BentoTextSecondary)
            Text(text = profile.nationalId, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
          }
          Column {
            Text(text = "Phone", fontSize = 11.sp, color = BentoTextSecondary)
            Text(text = profile.phoneNumber, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
          }
          Column {
            Text(text = "Worker ID", fontSize = 11.sp, color = BentoTextSecondary)
            Text(text = profile.id, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }

    // 2. Device Lock & Security Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      border = BorderStroke(1.dp, BentoOutline),
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Default.PhoneAndroid,
              contentDescription = null,
              tint = BentoBluePrimary,
              modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Device Security",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              maxLines = 1,
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Box(
            modifier =
              Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(BentoSuccessContainer)
                .padding(horizontal = 8.dp, vertical = 3.dp),
          ) {
            Text(
              text = "Authorized",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = BentoSuccess,
              maxLines = 1,
              softWrap = false,
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Device: ${profile.deviceModel}",
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
        )
        Text(
          text = "Device Fingerprint: ${profile.deviceId}",
          fontSize = 11.sp,
          color = BentoTextSecondary,
        )
      }
    }

    // 3. Geofencing Test Bench Simulator (Admin Only)
    if (isAdmin) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoOutline),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Default.Tune,
              contentDescription = null,
              tint = BentoBluePrimary,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Geofence Testing Simulator",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
            )
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Test validation when worker is simulated outside or inside site boundary:",
            fontSize = 12.sp,
            color = BentoTextSecondary,
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier =
              Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(BentoTileGray.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
              Text(
                text = if (isOutsideSimulation) "Outside (185m)" else "Inside (15m)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOutsideSimulation) BentoError else BentoSuccess,
              )
              Text(
                text = "Radius: ${selectedSite.radiusMeters}m",
                fontSize = 11.sp,
                color = BentoTextSecondary,
              )
            }

            Switch(
              checked = isOutsideSimulation,
              onCheckedChange = { onToggleSimulator(it) },
              colors =
                SwitchDefaults.colors(
                  checkedThumbColor = BentoError,
                  checkedTrackColor = Color(0xFFFFCDD2),
                  uncheckedThumbColor = BentoSuccess,
                  uncheckedTrackColor = BentoSuccessContainer,
                ),
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedButton(
            onClick = onResetToday,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Reset Today's Attendance for Testing", fontSize = 13.sp)
          }
        }
      }
    }

    // 4. GPS Geofence Telemetry & Precision Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      border = BorderStroke(1.dp, BentoOutline),
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Default.Radar,
              contentDescription = null,
              tint = BentoBluePrimary,
              modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "GPS Geofence",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
              )
              Text(
                text = "Site: ${selectedSite.name}",
                fontSize = 11.sp,
                color = BentoTextSecondary,
                maxLines = 1,
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          Box(
            modifier =
              Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(BentoSuccessContainer)
                .padding(horizontal = 8.dp, vertical = 3.dp),
          ) {
            Text(
              text = "High Accuracy",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = BentoSuccess,
              maxLines = 1,
              softWrap = false,
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Structured Properties Display: latitude, longitude, radius_meters
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = BentoTileGray),
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "├── latitude: ${String.format(Locale.ENGLISH, "%.6f", selectedSite.latitude)}",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              text = "├── longitude: ${String.format(Locale.ENGLISH, "%.6f", selectedSite.longitude)}",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              text = "└── radius_meters: ${selectedSite.radiusMeters} m",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = BentoBluePrimary,
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Proximity verified via real-time on-device Haversine calculations with anti-mock location protection enabled.",
          fontSize = 11.sp,
          color = BentoTextSecondary,
          lineHeight = 16.sp,
        )
      }
    }

    // Security Activity Logs (Admin Only)
    if (isAdmin) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoOutline),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              tint = BentoBluePrimary,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Security Audit & Activity Logs",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          activityLogs.take(5).forEach { log ->
            Row(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp),
              verticalAlignment = Alignment.Top,
            ) {
              Icon(
                imageVector = if (log.isSuccessful) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (log.isSuccessful) BentoSuccess else BentoError,
                modifier = Modifier.size(16.dp).padding(top = 2.dp),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = log.details,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                )
                Text(
                  text = log.timestamp,
                  fontSize = 10.sp,
                color = BentoTextSecondary,
              )
            }
          }
        }
      }
    }
  }

    Spacer(modifier = Modifier.height(16.dp))
  }

  // Logout Confirmation Dialog
  if (showLogoutConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showLogoutConfirmDialog = false },
      icon = { Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = BentoError) },
      title = { Text("Confirm Logout", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 17.sp) },
      text = {
        Text(
          text = "Are you sure you want to log out of your account? You will need to enter your password to sign in again.",
          color = Color.Black,
          fontSize = 14.sp,
          lineHeight = 20.sp,
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showLogoutConfirmDialog = false
            onLogout()
          },
          colors = ButtonDefaults.buttonColors(containerColor = BentoError, contentColor = Color.White),
          shape = RoundedCornerShape(10.dp),
        ) {
          Text("Log Out", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = { showLogoutConfirmDialog = false },
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


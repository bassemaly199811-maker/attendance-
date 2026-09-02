package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun AppSplashScreen(
  isOnline: Boolean,
  hasCameraPermission: Boolean,
  hasLocationPermission: Boolean,
  isLocationEnabled: Boolean,
  isLoading: Boolean,
  onRequestCameraPermission: () -> Unit,
  onRequestLocationPermission: () -> Unit,
  onEnableGpsSettings: () -> Unit,
  onRetryConnection: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val allPassed = isOnline && hasCameraPermission && hasLocationPermission && isLocationEnabled

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(
          brush =
            Brush.verticalGradient(
              colors =
                listOf(
                  Color(0xFFFFFFFF),
                  Color(0xFFF1F5F9),
                  Color(0xFFE2E8F0),
                )
            )
        )
        .testTag("app_splash_screen"),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      // 1. App Icon Badge
      Surface(
        modifier =
          Modifier
            .size(92.dp)
            .shadow(12.dp, CircleShape, spotColor = BentoBluePrimary.copy(alpha = 0.4f)),
        shape = CircleShape,
        color = Color.White,
        border = BorderStroke(2.dp, BentoBluePrimary.copy(alpha = 0.3f)),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = null,
            tint = BentoBluePrimary,
            modifier = Modifier.size(52.dp),
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 2. App Title
      Text(
        text = "Work Attendance & Verification",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1E293B),
        textAlign = TextAlign.Center,
      )

      Spacer(modifier = Modifier.height(4.dp))

      // 3. Subtitle / Features
      Text(
        text = "Identity Photo • GPS Geofencing • Cloud Sync",
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = BentoTextSecondary,
        textAlign = TextAlign.Center,
      )

      Spacer(modifier = Modifier.height(24.dp))

      // 4. Pre-flight Verification Status Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoOutline),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "System Readiness Checks",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color =  Color.Black,
            )
            if (allPassed) {
              Box(
                modifier =
                  Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(BentoSuccessContainer)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
              ) {
                Text(
                  text = "Ready ✓",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = BentoSuccess,
                )
              }
            } else {
              Box(
                modifier =
                  Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(BentoWarningContainer)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
              ) {
                Text(
                  text = "Action Required",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = BentoWarning,
                )
              }
            }
          }

          HorizontalDivider(color = Color(0xFFF1F5F9))

          // 1. Internet Connection
          CheckItemRow(
            title = "Internet Connection",
            description = if (isOnline) "Connected to Cloud Services" else "Internet connection is missing (Wi-Fi/4G)",
            icon = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
            isPassed = isOnline,
            actionButton = {
              if (!isOnline) {
                Button(
                  onClick = onRetryConnection,
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
                  contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                ) {
                  Text("Retry", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            },
          )

          // 2. Camera Access
          CheckItemRow(
            title = "Camera Permission",
            description = if (hasCameraPermission) "Live Camera verified" else "Camera access required for facial check-in",
            icon = Icons.Default.PhotoCamera,
            isPassed = hasCameraPermission,
            actionButton = {
              if (!hasCameraPermission) {
                Button(
                  onClick = onRequestCameraPermission,
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
                  contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                ) {
                  Text("Grant", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            },
          )

          // 3. Location (GPS & Permission)
          val isLocationOk = hasLocationPermission && isLocationEnabled
          val locationDesc =
            when {
              !hasLocationPermission -> "Location (GPS) permission required"
              !isLocationEnabled -> "Device GPS hardware service is disabled"
              else -> "Precise GPS active & ready"
            }

          CheckItemRow(
            title = "Location (GPS)",
            description = locationDesc,
            icon = if (isLocationOk) Icons.Default.LocationOn else Icons.Default.LocationOff,
            isPassed = isLocationOk,
            actionButton = {
              if (!hasLocationPermission) {
                Button(
                  onClick = onRequestLocationPermission,
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
                  contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                ) {
                  Text("Grant", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              } else if (!isLocationEnabled) {
                Button(
                  onClick = onEnableGpsSettings,
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
                  contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                ) {
                  Text("Enable", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            },
          )

          if (allPassed) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
            ) {
              CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = BentoBluePrimary,
                strokeWidth = 2.dp,
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Starting session...",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = BentoBluePrimary,
              )
            }
          } else {
            Spacer(modifier = Modifier.height(2.dp))
            OutlinedButton(
              onClick = onRetryConnection,
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
            ) {
              Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Verify All Services", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CheckItemRow(
  title: String,
  description: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isPassed: Boolean,
  actionButton: @Composable () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
      Box(
        modifier =
          Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (isPassed) BentoSuccessContainer else BentoErrorContainer),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (isPassed) BentoSuccess else BentoError,
          modifier = Modifier.size(18.dp),
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = title,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color =  Color.Black,
        )
        Text(
          text = description,
          fontSize = 11.sp,
          color = if (isPassed) BentoSuccess else BentoError,
          maxLines = 1,
        )
      }
    }

    actionButton()
  }
}

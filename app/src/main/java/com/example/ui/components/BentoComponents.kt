package com.example.ui.components

import com.example.BuildConfig
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Layers
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.AttendanceStatus
import com.example.data.model.WorkerProfile
import com.example.ui.theme.BentoBlueContainer
import com.example.ui.theme.BentoBlueOnContainer
import com.example.ui.theme.BentoBluePrimary
import com.example.ui.theme.BentoError
import com.example.ui.theme.BentoErrorContainer
import com.example.ui.theme.BentoLilac
import com.example.ui.theme.BentoLilacBorder
import com.example.ui.theme.BentoLilacText
import com.example.ui.theme.BentoNavBg
import com.example.ui.theme.BentoOutline
import com.example.ui.theme.BentoSuccess
import com.example.ui.theme.BentoSuccessContainer
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.BentoTileGray
import com.example.ui.theme.BentoTileLight
import com.example.ui.theme.BentoWarning
import java.util.Locale
import com.example.ui.viewmodel.BentoTab

/**
 * Top profile header matching Bento Grid design
 */
@Composable
fun BentoHeader(
  profile: WorkerProfile,
  isOnline: Boolean = true,
  onOpenDashboard: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
    val isNarrow = maxWidth < 360.dp

    if (isNarrow) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Hello,",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = BentoTextSecondary,
            )
            Spacer(modifier = Modifier.width(6.dp))
            // Online Badge
            Box(
              modifier =
                Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(if (isOnline) BentoSuccessContainer else BentoErrorContainer)
                  .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier =
                    Modifier
                      .size(6.dp)
                      .clip(CircleShape)
                      .background(if (isOnline) BentoSuccess else BentoError),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isOnline) "Cloud Connected" else "Offline",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isOnline) BentoSuccess else BentoError,
                )
              }
            }
          }

          Box(
            modifier =
              Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(BentoBlueContainer)
                .border(2.dp, Color.White, CircleShape)
                .shadow(2.dp, CircleShape),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = profile.initials,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = BentoBlueOnContainer,
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = profile.fullName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.weight(1f, fill = false),
          )

          if (onOpenDashboard != null) {
            Box(
              modifier =
                Modifier
                  .clip(RoundedCornerShape(50.dp))
                  .background(BentoBlueContainer)
                  .clickable { onOpenDashboard() }
                  .padding(horizontal = 8.dp, vertical = 5.dp),
              contentAlignment = Alignment.Center,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Dashboard,
                  contentDescription = "Dashboard",
                  tint = BentoBluePrimary,
                  modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Dashboard",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = BentoBluePrimary,
                )
              }
            }
          }
        }
      }
    } else {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Hello,",
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium,
              color = BentoTextSecondary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Online Badge
            Box(
              modifier =
                Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(if (isOnline) BentoSuccessContainer else BentoErrorContainer)
                  .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier =
                    Modifier
                      .size(6.dp)
                      .clip(CircleShape)
                      .background(if (isOnline) BentoSuccess else BentoError),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isOnline) "Cloud Connected" else "Offline",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isOnline) BentoSuccess else BentoError,
                )
              }
            }
          }
          Text(
            text = profile.fullName,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          if (onOpenDashboard != null) {
            Box(
              modifier =
                Modifier
                  .clip(RoundedCornerShape(50.dp))
                  .background(BentoBlueContainer)
                  .bounceClick(scaleDown = 0.93f) { onOpenDashboard() }
                  .padding(horizontal = 10.dp, vertical = 6.dp),
              contentAlignment = Alignment.Center,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Dashboard,
                  contentDescription = "Dashboard",
                  tint = BentoBluePrimary,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Dashboard",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = BentoBluePrimary,
                )
              }
            }
          }

          Box(
            modifier =
              Modifier
                .size(44.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(BentoBlueContainer)
                .border(2.dp, Color.White, CircleShape)
                .bounceClick(scaleDown = 0.93f) {},
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = profile.initials,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = BentoBlueOnContainer,
            )
          }
        }
      }
    }
  }
}

/**
 * Detailed Error & Actionable Guidance Dialog
 */
@Composable
fun BentoErrorGuidanceDialog(
  title: String?,
  message: String?,
  guidance: String?,
  onDismiss: () -> Unit,
) {
  if (title != null && message != null) {
    androidx.compose.material3.AlertDialog(
      onDismissRequest = onDismiss,
      icon = {
        Box(
          modifier =
            Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(BentoErrorContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = BentoError,
            modifier = Modifier.size(28.dp),
          )
        }
      },
      title = {
        Text(
          text = title,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = BentoError,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth(),
        )
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Text(
            text = message,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
          )

          if (!guidance.isNullOrBlank()) {
            Box(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(BentoTileLight)
                  .border(1.dp, BentoOutline, RoundedCornerShape(12.dp))
                  .padding(12.dp),
            ) {
              Column {
                Text(
                  text = "💡 How to resolve:",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = BentoBluePrimary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = guidance,
                  fontSize = 13.sp,
                  color = BentoTextSecondary,
                  lineHeight = 18.sp,
                )
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
          shape = RoundedCornerShape(12.dp),
        ) {
          Text(text = "Got It", fontWeight = FontWeight.Bold)
        }
      },
      shape = RoundedCornerShape(24.dp),
      containerColor = Color.White,
    )
  }
}

/**
 * Hero Bento Tile (Blue 32dp container)
 */
@Composable
fun BentoHeroCard(
  status: AttendanceStatus,
  lastOperation: String,
  modifier: Modifier = Modifier,
) {
  val title =
    when (status) {
      AttendanceStatus.NOT_CHECKED_IN -> "Not Checked In"
      AttendanceStatus.CHECKED_IN -> "Checked In (Active)"
      AttendanceStatus.CHECKED_OUT -> "Checked Out"
    }

  val gradient =
    Brush.verticalGradient(
      colors = listOf(BentoBluePrimary, Color(0xFF003F8A), Color(0xFF002B66))
    )

  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .shadow(
          elevation = 8.dp,
          shape = RoundedCornerShape(32.dp),
          spotColor = BentoBluePrimary.copy(alpha = 0.5f),
          ambientColor = BentoBluePrimary.copy(alpha = 0.15f),
        )
        .clip(RoundedCornerShape(32.dp))
        .background(gradient)
        .drawWithCache {
          val centerX = size.width
          val centerY = 0f
          onDrawBehind {
            // Decorative background glowing aura circles
            drawCircle(
              color = Color.White.copy(alpha = 0.07f),
              radius = size.width * 0.55f,
              center = Offset(centerX, centerY),
            )
            drawCircle(
              color = Color.White.copy(alpha = 0.04f),
              radius = size.width * 0.85f,
              center = Offset(centerX, centerY),
            )
            drawCircle(
              color = BentoBlueContainer.copy(alpha = 0.08f),
              radius = size.width * 0.40f,
              center = Offset(0f, size.height),
            )
          }
        }
        .bounceClick(scaleDown = 0.985f) {}
        .padding(horizontal = 24.dp, vertical = 22.dp)
        .testTag("bento_hero_card"),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        val beaconColor = when (status) {
          AttendanceStatus.CHECKED_IN -> Color(0xFF00E676)
          AttendanceStatus.NOT_CHECKED_IN -> Color(0xFF38BDF8)
          AttendanceStatus.CHECKED_OUT -> Color(0xFFCBD5E1)
        }
        Box(
          modifier = Modifier
            .size(7.dp)
            .pulseEffect(minScale = 0.85f, maxScale = 1.25f)
            .clip(CircleShape)
            .background(beaconColor)
        )
        Text(
          text = "CURRENT STATUS",
          fontSize = 12.5.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFFD8E2FF),
          letterSpacing = 1.2.sp,
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = title,
        fontSize = 32.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White,
        textAlign = TextAlign.Center,
      )

      Spacer(modifier = Modifier.height(10.dp))

      Box(
        modifier =
          Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(50.dp))
            .padding(horizontal = 14.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = lastOperation,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = Color.White,
        )
      }
    }
  }
}

/**
 * Clean, Full-Width Work Site Bento Card
 */
@Composable
fun BentoWorkSiteCard(
  siteName: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .bounceClick(scaleDown = 0.97f) { onClick() }
      .testTag("bento_site_tile"),
    shape = RoundedCornerShape(20.dp),
    color = Color.White,
    border = BorderStroke(1.dp, BentoOutline.copy(alpha = 0.6f)),
    shadowElevation = 3.dp,
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        modifier = Modifier.weight(1f, fill = false),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(BentoBlueContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Place,
            contentDescription = "Work Site",
            tint = BentoBluePrimary,
            modifier = Modifier.size(24.dp),
          )
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(
            text = "Assigned Work Site",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = BentoTextSecondary,
          )
          Text(
            text = siteName.ifBlank { "Main Work Site" },
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = BentoBlueContainer.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, BentoBluePrimary.copy(alpha = 0.25f)),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Place,
            contentDescription = null,
            tint = BentoBluePrimary,
            modifier = Modifier.size(16.dp),
          )
          Text(
            text = "Change Site",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = BentoBluePrimary,
          )
        }
      }
    }
  }
}

/**
 * Native Geofence Coordinates Bento Card (Zero WebView / Pure GPS Radar Telemetry)
 * Features:
 * ├── latitude
 * ├── longitude
 * ├── radius_meters
 * ├── Location search progress indicator
 * ├── Location search failure reason explanation
 * └── Location search success feedback
 */
@Composable
fun BentoGeofenceCoordinatesCard(
  siteName: String,
  latitude: Double,
  longitude: Double,
  radiusMeters: Int,
  isInside: Boolean,
  distanceMeters: Double,
  deviceLatitude: Double = 21.543333,
  deviceLongitude: Double = 39.172778,
  accuracyMeters: Double = 8.0,
  isSearchingLocation: Boolean = false,
  locationSearchStatus: String? = null,
  locationSearchError: String? = null,
  locationSearchSuccess: String? = null,
  isOnline: Boolean = true,
  onRefreshGps: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = Color.White,
    border = BorderStroke(1.dp, BentoOutline),
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      // 1. Header with Geofence Status Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          modifier = Modifier.weight(1f, fill = false),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier =
              Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(BentoBlueContainer),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Radar,
              contentDescription = null,
              tint = BentoBluePrimary,
              modifier = Modifier.size(16.dp),
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "Geofence Status",
              fontWeight = FontWeight.Bold,
              fontSize = 12.5.sp,
              color = Color.Black,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = siteName,
              fontSize = 10.5.sp,
              color = BentoTextSecondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Box(
          modifier =
            Modifier
              .clip(RoundedCornerShape(50.dp))
              .background(if (isInside) BentoSuccessContainer else Color(0xFFFFEBEE))
              .border(
                1.dp,
                if (isInside) BentoSuccess.copy(alpha = 0.3f) else Color(0xFFFFCDD2),
                RoundedCornerShape(50.dp),
              )
              .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
          Text(
            text = if (isInside) "Inside Geofence ✓" else "Outside Boundary ⚠️",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isInside) BentoSuccess else BentoError,
            maxLines = 1,
            softWrap = false,
          )
        }
      }

      // 2. ACTIVE SEARCHING INDICATOR BANNER
      if (isSearchingLocation) {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          color = BentoBlueContainer.copy(alpha = 0.8f),
          border = BorderStroke(1.dp, BentoBluePrimary.copy(alpha = 0.4f)),
        ) {
          Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
              strokeWidth = 2.dp,
              color = BentoBluePrimary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Searching for location via GPS...",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = BentoBluePrimary,
              )
            }
          }
        }
      }

      // 3. LOCATION SEARCH ERROR BANNER
      if (!isSearchingLocation && locationSearchError != null) {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFFFFEBEE),
          border = BorderStroke(1.dp, BentoError.copy(alpha = 0.4f)),
        ) {
          Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = BentoError,
                modifier = Modifier.size(14.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Location precision issue",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BentoError,
              )
            }
            Text(
              text = locationSearchError,
              fontSize = 10.5.sp,
              color = Color(0xFFC62828),
              lineHeight = 14.sp,
            )
          }
        }
      }

      // 4. In/Out Geofence Guidance Banner (Compact)
      if (!isInside) {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFFFFF3E0),
          border = BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.4f)),
        ) {
          Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = Color(0xFFE65100),
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Outside work site boundary",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFBF360C),
              )
              Text(
                text = "Distance: ${distanceMeters.toInt()}m (Allowed: ${radiusMeters}m)",
                fontSize = 10.sp,
                color = Color(0xFF5D4037),
              )
            }
          }
        }
      } else {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          color = BentoSuccessContainer.copy(alpha = 0.6f),
          border = BorderStroke(1.dp, BentoSuccess.copy(alpha = 0.2f)),
        ) {
          Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = BentoSuccess,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Verified inside geofence (${distanceMeters.toInt()}m / ${radiusMeters}m radius)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = BentoSuccess,
            )
          }
        }
      }

      // 5. Coordinates Telemetry Mini-Pills
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFFF8F9FA),
          border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
          modifier = Modifier.weight(1f),
        ) {
          Column(modifier = Modifier.padding(6.dp)) {
            Text("GPS Coordinates", fontSize = 9.sp, color = BentoTextSecondary)
            Text(
              text = String.format(java.util.Locale.ENGLISH, "%.4f, %.4f", deviceLatitude, deviceLongitude),
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Bold,
              color = Color.Black,
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFFF8F9FA),
          border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
          modifier = Modifier.weight(1f),
        ) {
          Column(modifier = Modifier.padding(6.dp)) {
            Text("Accuracy", fontSize = 9.sp, color = BentoTextSecondary)
            Text(
              text = "±${accuracyMeters.toInt()}m",
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Bold,
              color = if (accuracyMeters <= 20) BentoSuccess else BentoWarning,
            )
          }
        }
      }

      // 6. Worker GPS Refresh Button (Compact)
      if (onRefreshGps != null) {
        Button(
          onClick = onRefreshGps,
          enabled = !isSearchingLocation,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = BentoBluePrimary,
            contentColor = Color.White,
            disabledContainerColor = BentoBluePrimary.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.8f),
          ),
          modifier = Modifier.fillMaxWidth().height(36.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        ) {
          if (isSearchingLocation) {
            CircularProgressIndicator(
              modifier = Modifier.size(14.dp),
              strokeWidth = 2.dp,
              color = Color.White,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Updating GPS...",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
            )
          } else {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = null,
              modifier = Modifier.size(15.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Refresh Location",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }
    }
  }
}

/**
 * Backward compatibility wrapper
 */
@Composable
fun BentoLiveMapCard(
  siteName: String,
  latitude: Double,
  longitude: Double,
  radiusMeters: Int,
  isInside: Boolean,
  distanceMeters: Double,
  apiKey: String = "",
  onOpenFullMap: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  BentoGeofenceCoordinatesCard(
    siteName = siteName,
    latitude = latitude,
    longitude = longitude,
    radiusMeters = radiusMeters,
    isInside = isInside,
    distanceMeters = distanceMeters,
    modifier = modifier,
  )
}

/**
 * Identity Confirmation Bento Card (Direct Live Camera Capture & Google Drive Sync)
 */
@Composable
fun BentoIdentityCard(
  photoCaptured: Boolean,
  capturedBitmap: Bitmap? = null,
  photoUri: String? = null,
  photoBase64: String? = null,
  status: AttendanceStatus = AttendanceStatus.NOT_CHECKED_IN,
  isUploadingToDrive: Boolean = false,
  onCaptureClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val title =
    when (status) {
      AttendanceStatus.CHECKED_IN -> "Check-Out Photo"
      AttendanceStatus.CHECKED_OUT -> "Verification Complete"
      else -> "Check-In Photo"
    }

  val promptText =
    when (status) {
      AttendanceStatus.CHECKED_IN -> "Tap to open camera & capture live check-out photo"
      AttendanceStatus.CHECKED_OUT -> "Photos verified and synced to Google Drive"
      else -> "Tap to open camera & capture live check-in photo"
    }

  val subText =
    when (status) {
      AttendanceStatus.CHECKED_IN -> "Live face photo is required to complete check out & sync to cloud"
      AttendanceStatus.CHECKED_OUT -> "Shift record and Google Drive photo links are confirmed"
      else -> "Photo will be saved directly to the shared Google Drive folder"
    }

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    color = Color.White,
    border = BorderStroke(1.dp, BentoOutline),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
    ) {
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
            imageVector = Icons.Default.PhotoCamera,
            contentDescription = null,
            tint = BentoBluePrimary,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
          modifier =
            Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (photoCaptured) BentoSuccessContainer else Color(0xFFE1E2E8))
              .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
          Text(
            text = if (photoCaptured) "Drive Ready ✓" else "Camera Required",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (photoCaptured) BentoSuccess else BentoTextSecondary,
            maxLines = 1,
            softWrap = false,
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      val imageSource = if (capturedBitmap != null) null else (photoUri ?: if (!photoBase64.isNullOrBlank()) "data:image/jpeg;base64,$photoBase64" else null)

      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (photoCaptured) Color(0xFFF1F8F1) else BentoTileLight)
            .border(
              1.5.dp,
              if (photoCaptured) BentoSuccess.copy(alpha = 0.5f) else BentoOutline.copy(alpha = 0.7f),
              RoundedCornerShape(16.dp),
            )
            .bounceClick(scaleDown = 0.98f) { onCaptureClick() }
            .padding(10.dp)
            .testTag("bento_identity_box"),
        contentAlignment = Alignment.Center,
      ) {
        if (photoCaptured && (capturedBitmap != null || imageSource != null)) {
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            if (capturedBitmap != null) {
              Image(
                bitmap = capturedBitmap.asImageBitmap(),
                contentDescription = "Live Camera Photo",
                contentScale = ContentScale.Crop,
                modifier =
                  Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, BentoSuccess, RoundedCornerShape(12.dp)),
              )
            } else if (imageSource != null) {
              AsyncImage(
                model =
                  ImageRequest.Builder(context)
                    .data(imageSource)
                    .crossfade(true)
                    .build(),
                contentDescription = "Live Camera Photo",
                modifier =
                  Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, BentoSuccess, RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = BentoSuccess,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                val photoLabel =
                  when (status) {
                    AttendanceStatus.CHECKED_IN -> "Live Check-Out Photo Ready"
                    AttendanceStatus.CHECKED_OUT -> "Photos Verified & Synced"
                    else -> "Live Check-In Photo Ready"
                  }
                Text(
                  text = if (isUploadingToDrive) "Uploading to Drive..." else photoLabel,
                  fontSize = 12.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = BentoSuccess,
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Attached to attendance records and cloud Drive folder. Tap to retake if needed.",
                fontSize = 11.sp,
                color = BentoTextSecondary,
                lineHeight = 15.sp,
              )
            }
          }
        } else if (photoCaptured) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = "Photo captured",
              tint = BentoSuccess,
              modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Live photo verified ✓",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = BentoSuccess,
              textAlign = TextAlign.Center,
            )
            Text(
              text = "Tap to retake photo",
              fontSize = 11.sp,
              color = BentoTextSecondary,
              textAlign = TextAlign.Center,
            )
          }
        } else {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
          ) {
            Box(
              modifier =
                Modifier
                  .size(46.dp)
                  .pulseEffect(minScale = 0.92f, maxScale = 1.08f)
                  .clip(CircleShape)
                  .background(BentoBlueContainer)
                  .border(1.5.dp, BentoBluePrimary.copy(alpha = 0.4f), CircleShape),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Camera",
                tint = BentoBluePrimary,
                modifier = Modifier.size(24.dp),
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = promptText,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = BentoBluePrimary,
              textAlign = TextAlign.Center,
            )
            Text(
              text = subText,
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Medium,
              color = BentoTextSecondary,
              textAlign = TextAlign.Center,
            )
          }
        }
      }
    }
  }
}

/**
 * Main Action Button with Dynamic Photo & Cooldown State
 */
@Composable
fun BentoActionButton(
  status: AttendanceStatus,
  photoCaptured: Boolean = false,
  isProcessing: Boolean,
  cooldownRemainingSeconds: Long = 0L,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isCooldownActive = cooldownRemainingSeconds > 0L
  val min = cooldownRemainingSeconds / 60
  val sec = cooldownRemainingSeconds % 60
  val cooldownTimeFormatted = String.format(java.util.Locale.ENGLISH, "%02d:%02d", min, sec)

  val buttonText =
    when {
      isCooldownActive -> "Please wait ($cooldownTimeFormatted) - Cooldown"
      status == AttendanceStatus.NOT_CHECKED_IN -> {
        if (photoCaptured) "Confirm Check In ✓" else "Capture Photo to Check In 📷"
      }
      status == AttendanceStatus.CHECKED_IN -> {
        if (photoCaptured) "Confirm Check Out ➔" else "Capture Photo to Check Out 📷"
      }
      status == AttendanceStatus.CHECKED_OUT -> "Shift Completed for Today ✓"
      else -> "Submit"
    }

  val isCompleted = status == AttendanceStatus.CHECKED_OUT
  val buttonBg =
    when {
      isCooldownActive -> Color(0xFFB0BEC5)
      isCompleted -> Color(0xFF627282)
      status == AttendanceStatus.CHECKED_IN -> {
        if (photoCaptured) Color(0xFF1E3A8A) else Color(0xFF2563EB)
      }
      status == AttendanceStatus.NOT_CHECKED_IN -> {
        if (photoCaptured) BentoBluePrimary else Color(0xFF1D4ED8)
      }
      else -> BentoBluePrimary
    }

  val buttonInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

  Button(
    onClick = onClick,
    enabled = !isProcessing && !isCompleted && !isCooldownActive,
    interactionSource = buttonInteraction,
    modifier =
      modifier
        .fillMaxWidth()
        .height(56.dp)
        .shadow(
          elevation = if (isCompleted || isCooldownActive) 1.dp else 6.dp,
          shape = RoundedCornerShape(18.dp),
          spotColor = buttonBg.copy(alpha = 0.55f),
          ambientColor = buttonBg.copy(alpha = 0.2f),
        )
        .bounceOnPress(buttonInteraction, scaleDown = 0.96f)
        .testTag("attendance_action_button"),
    shape = RoundedCornerShape(18.dp),
    colors =
      ButtonDefaults.buttonColors(
        containerColor = buttonBg,
        contentColor = Color.White,
        disabledContainerColor = if (isCooldownActive) Color(0xFF90A4AE) else Color(0xFF8E99A8),
        disabledContentColor = Color.White.copy(alpha = 0.9f),
      ),
    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
  ) {
    if (isProcessing) {
      CircularProgressIndicator(
        modifier = Modifier.size(22.dp),
        color = Color.White,
        strokeWidth = 2.5.dp,
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = "Verifying location & server...",
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
      )
    } else if (isCooldownActive) {
      Icon(
        imageVector = Icons.Default.HourglassTop,
        contentDescription = null,
        modifier = Modifier.size(20.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = buttonText,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
      )
    } else {
      val actionIcon =
        when {
          isCompleted -> Icons.Default.Check
          !photoCaptured -> Icons.Default.PhotoCamera
          status == AttendanceStatus.CHECKED_IN -> Icons.Default.NearMe
          else -> Icons.Default.CheckCircle
        }

      Icon(
        imageVector = actionIcon,
        contentDescription = null,
        modifier = Modifier.size(20.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = buttonText,
        fontSize = 15.5.sp,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}

/**
 * Floating Rounded Bottom Navigation with role-based tabs
 */
@Composable
fun BentoNavigationBar(
  currentTab: BentoTab,
  onTabSelected: (BentoTab) -> Unit,
  isAdmin: Boolean = false,
  pendingLeaveCount: Int = 0,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .shadow(
          elevation = 10.dp,
          shape = RoundedCornerShape(50.dp),
          spotColor = BentoBluePrimary.copy(alpha = 0.22f),
          ambientColor = Color.Black.copy(alpha = 0.08f),
        )
        .clip(RoundedCornerShape(50.dp))
        .background(BentoNavBg)
        .border(1.dp, Color.White.copy(alpha = 0.65f), RoundedCornerShape(50.dp))
        .padding(horizontal = 4.dp, vertical = 6.dp)
        .testTag("bento_nav_bar"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (isAdmin) {
        BentoNavItem(
          label = "Dashboard",
          icon = Icons.Default.Dashboard,
          isSelected = currentTab == BentoTab.DASHBOARD,
          onClick = { onTabSelected(BentoTab.DASHBOARD) },
          modifier = Modifier.weight(1f),
        )
        BentoNavItem(
          label = "Users",
          icon = Icons.Default.People,
          isSelected = currentTab == BentoTab.USER_MANAGEMENT,
          onClick = { onTabSelected(BentoTab.USER_MANAGEMENT) },
          modifier = Modifier.weight(1f),
        )
        BentoNavItem(
          label = "Leaves",
          icon = Icons.Default.EventNote,
          isSelected = currentTab == BentoTab.LEAVE_APPROVALS,
          badgeCount = pendingLeaveCount,
          onClick = { onTabSelected(BentoTab.LEAVE_APPROVALS) },
          modifier = Modifier.weight(1f),
        )
      } else {
        BentoNavItem(
          label = "Home",
          icon = Icons.Default.Home,
          isSelected = currentTab == BentoTab.HOME,
          onClick = { onTabSelected(BentoTab.HOME) },
          modifier = Modifier.weight(1f),
        )
        BentoNavItem(
          label = "Time Off",
          icon = Icons.Default.DateRange,
          isSelected = currentTab == BentoTab.TIME_OFF,
          onClick = { onTabSelected(BentoTab.TIME_OFF) },
          modifier = Modifier.weight(1f),
        )
      }
      BentoNavItem(
        label = "History",
        icon = Icons.Default.History,
        isSelected = currentTab == BentoTab.HISTORY,
        onClick = { onTabSelected(BentoTab.HISTORY) },
        modifier = Modifier.weight(1f),
      )
      BentoNavItem(
        label = "Settings",
        icon = Icons.Default.Settings,
        isSelected = currentTab == BentoTab.SETTINGS,
        onClick = { onTabSelected(BentoTab.SETTINGS) },
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun BentoNavItem(
  label: String,
  icon: ImageVector,
  isSelected: Boolean,
  badgeCount: Int = 0,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val contentColor by androidx.compose.animation.animateColorAsState(
    targetValue = if (isSelected) BentoBluePrimary else BentoTextSecondary.copy(alpha = 0.7f),
    label = "navContentColor",
  )
  val bgColor by androidx.compose.animation.animateColorAsState(
    targetValue = if (isSelected) BentoBlueContainer else Color.Transparent,
    label = "navBgColor",
  )
  val iconScale by androidx.compose.animation.core.animateFloatAsState(
    targetValue = if (isSelected) 1.12f else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMediumLow,
    ),
    label = "navIconScale",
  )

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = bgColor,
    modifier = modifier
      .bounceClick(scaleDown = 0.88f) { onClick() }
      .padding(horizontal = 2.dp, vertical = 2.dp),
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.wrapContentSize(),
      ) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = contentColor,
          modifier = Modifier
            .size(22.dp)
            .graphicsLayer {
              scaleX = iconScale
              scaleY = iconScale
            },
        )
        if (badgeCount > 0) {
          Surface(
            shape = CircleShape,
            color = BentoError,
            modifier = Modifier
              .align(Alignment.TopEnd)
              .offset(x = 8.dp, y = (-5).dp),
            shadowElevation = 2.dp,
          ) {
            Box(
              modifier = Modifier
                .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                .padding(horizontal = 4.dp, vertical = 2.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = if (badgeCount > 99) "99+" else "$badgeCount",
                color = Color.White,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                lineHeight = 10.sp,
              )
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = contentColor,
        maxLines = 1,
      )
    }
  }
}

/**
 * Toast / Alert Banner for Geofence & Check-in results
 */
@Composable
fun BentoNotificationBanner(
  message: String?,
  isError: Boolean,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AnimatedVisibility(
    visible = message != null,
    enter = fadeIn(),
    exit = fadeOut(),
    modifier = modifier,
  ) {
    if (message != null) {
      val bg = if (isError) BentoErrorContainer else BentoSuccessContainer
      val color = if (isError) BentoError else BentoSuccess

      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable { onDismiss() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = if (isError) Icons.Default.Warning else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = message,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.weight(1f),
          )
        }
      }
    }
  }
}

/**
 * Early Departure Warning Confirmation Dialog
 */
@Composable
fun BentoEarlyCheckoutWarningDialog(
  shiftEndTime: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  androidx.compose.material3.AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Box(
        modifier =
          Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(Color(0xFFFFF3E0)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Warning,
          contentDescription = "Early Departure Warning",
          tint = com.example.ui.theme.BentoWarning,
          modifier = Modifier.size(30.dp),
        )
      }
    },
    title = {
      Text(
        text = "Warning: Early Departure",
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        textAlign = TextAlign.Center,
        color = Color.Black,
      )
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          text = "You are attempting to check out before the scheduled end time ($shiftEndTime).",
          fontSize = 13.sp,
          color = BentoTextSecondary,
          textAlign = TextAlign.Center,
          lineHeight = 19.sp,
        )

        Box(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0xFFFFF8E1))
              .border(1.dp, Color(0xFFFFE082), RoundedCornerShape(14.dp))
              .padding(12.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = com.example.ui.theme.BentoWarning,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "This will be recorded and marked as an \"Early Departure\" in attendance logs.",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = Color(0xFFE65100),
              lineHeight = 17.sp,
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.BentoWarning),
        shape = RoundedCornerShape(12.dp),
      ) {
        Text(text = "Confirm Early Checkout", fontWeight = FontWeight.Bold, color = Color.White)
      }
    },
    dismissButton = {
      androidx.compose.material3.OutlinedButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp),
      ) {
        Text(text = "Cancel & Stay")
      }
    },
    shape = RoundedCornerShape(24.dp),
    containerColor = Color.White,
  )
}

@Composable
fun CheckoutSuccessDialog(
  quote: String,
  workerName: String,
  checkOutTime: String,
  onDismiss: () -> Unit,
) {
  var millisRemaining by remember { mutableStateOf(4000L) }

  // Auto-dismiss after 4 seconds (4000ms) with smooth 100ms ticks
  LaunchedEffect(Unit) {
    val totalTime = 4000L
    val interval = 100L
    var elapsed = 0L
    while (elapsed < totalTime) {
      delay(interval)
      elapsed += interval
      millisRemaining = (totalTime - elapsed).coerceAtLeast(0L)
    }
    onDismiss()
  }

  val progress = (millisRemaining / 4000f).coerceIn(0f, 1f)
  val secondsLeft = ((millisRemaining + 999) / 1000).toInt().coerceAtLeast(1)

  androidx.compose.material3.AlertDialog(
    onDismissRequest = onDismiss,
    title = null,
    text = {
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Success Badge Icon
        Box(
          modifier =
            Modifier
              .size(72.dp)
              .clip(CircleShape)
              .background(Color(0xFFE8F5E9))
              .border(2.dp, Color(0xFF81C784), CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          Box(
            modifier =
              Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(com.example.ui.theme.BentoSuccess),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "Success",
              tint = Color.White,
              modifier = Modifier.size(32.dp),
            )
          }
        }

        // Title & Worker Subtitle
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = "Check-Out Successful!",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1B5E20),
            textAlign = TextAlign.Center,
          )
          Text(
            text = "Your departure has been recorded and synced.",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = BentoTextSecondary,
            textAlign = TextAlign.Center,
          )

          Spacer(modifier = Modifier.height(4.dp))

          Surface(
            shape = RoundedCornerShape(20.dp),
            color = BentoTileGray,
            border = BorderStroke(1.dp, BentoOutline),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = BentoBluePrimary,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = workerName.ifBlank { "Worker" },
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
              )
              if (checkOutTime.isNotBlank()) {
                Text(
                  text = "• $checkOutTime",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = BentoTextSecondary,
                )
              }
            }
          }
        }

        // Highlight Encouragement Quote Card
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          color = Color(0xFFFFFBEA),
          border = BorderStroke(1.5.dp, Color(0xFFFFD54F)),
          shadowElevation = 2.dp,
        ) {
          Column(
            modifier =
              Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = "Daily Inspiration ✨",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFB78103),
            )

            Text(
              text = quote.ifBlank { "Great job today! 🌟" },
              fontSize = 19.sp,
              fontWeight = FontWeight.Black,
              color = Color(0xFF422006),
              textAlign = TextAlign.Center,
              lineHeight = 26.sp,
            )
          }
        }

        // 4-Second Timer & Progress Bar
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          androidx.compose.material3.LinearProgressIndicator(
            progress = { progress },
            modifier =
              Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = com.example.ui.theme.BentoSuccess,
            trackColor = BentoOutline,
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Closing automatically in $secondsLeft seconds...",
              fontSize = 11.sp,
              color = BentoTextSecondary,
            )
            Text(
              text = "${secondsLeft}s",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = com.example.ui.theme.BentoSuccess,
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.BentoSuccess),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          text = "OK",
          fontWeight = FontWeight.Bold,
          color = Color.White,
          fontSize = 14.sp,
        )
      }
    },
    shape = RoundedCornerShape(24.dp),
    containerColor = Color.White,
  )
}

@Composable
fun CheckInSuccessDialog(
  quote: String,
  workerName: String,
  checkInTime: String,
  onDismiss: () -> Unit,
) {
  var millisRemaining by remember { mutableStateOf(4000L) }

  LaunchedEffect(Unit) {
    val totalTime = 4000L
    val interval = 100L
    var elapsed = 0L
    while (elapsed < totalTime) {
      delay(interval)
      elapsed += interval
      millisRemaining = (totalTime - elapsed).coerceAtLeast(0L)
    }
    onDismiss()
  }

  val progress = (millisRemaining / 4000f).coerceIn(0f, 1f)
  val secondsLeft = ((millisRemaining + 999) / 1000).toInt().coerceAtLeast(1)

  androidx.compose.material3.AlertDialog(
    onDismissRequest = onDismiss,
    title = null,
    text = {
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Box(
          modifier =
            Modifier
              .size(72.dp)
              .clip(CircleShape)
              .background(BentoBlueContainer)
              .border(2.dp, BentoBluePrimary.copy(alpha = 0.5f), CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            tint = BentoBluePrimary,
            modifier = Modifier.size(42.dp),
          )
        }

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = "Check-In Successful!",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BentoBluePrimary,
            textAlign = TextAlign.Center,
          )
          Text(
            text = "Have a productive and safe work day!",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = BentoTextSecondary,
            textAlign = TextAlign.Center,
          )

          Spacer(modifier = Modifier.height(4.dp))

          Surface(
            shape = RoundedCornerShape(20.dp),
            color = BentoTileGray,
            border = BorderStroke(1.dp, BentoOutline),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = BentoBluePrimary,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = workerName.ifBlank { "Worker" },
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
              )
              if (checkInTime.isNotBlank()) {
                Text(
                  text = "• $checkInTime",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = BentoTextSecondary,
                )
              }
            }
          }
        }

        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          color = Color(0xFFEFF6FF),
          border = BorderStroke(1.5.dp, Color(0xFF93C5FD)),
          shadowElevation = 2.dp,
        ) {
          Column(
            modifier =
              Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = "Good Morning ✨",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = BentoBluePrimary,
            )

            Text(
              text = quote.ifBlank { "Let's make today productive and safe! 🚀" },
              fontSize = 18.sp,
              fontWeight = FontWeight.Black,
              color = Color(0xFF1E3A8A),
              textAlign = TextAlign.Center,
              lineHeight = 24.sp,
            )
          }
        }

        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          androidx.compose.material3.LinearProgressIndicator(
            progress = { progress },
            modifier =
              Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = BentoBluePrimary,
            trackColor = BentoOutline,
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Closing automatically in $secondsLeft seconds...",
              fontSize = 11.sp,
              color = BentoTextSecondary,
            )
            Text(
              text = "${secondsLeft}s",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = BentoBluePrimary,
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          text = "OK",
          fontWeight = FontWeight.Bold,
          color = Color.White,
          fontSize = 14.sp,
        )
      }
    },
    shape = RoundedCornerShape(24.dp),
    containerColor = Color.White,
  )
}



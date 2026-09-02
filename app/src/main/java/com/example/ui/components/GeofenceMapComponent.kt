package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.*

/**
 * Format distance cleanly (e.g., "14 m" or "1.2 km")
 */
fun formatDistanceClean(meters: Double): String {
  return if (meters < 1000) {
    "${meters.toInt()} m"
  } else {
    String.format(Locale.ENGLISH, "%.1f km", meters / 1000.0)
  }
}

/**
 * Haversine formula calculation helper
 */
fun calculateHaversineDistanceMeters(
  lat1: Double,
  lon1: Double,
  lat2: Double,
  lon2: Double,
): Double {
  val r = 6371000.0
  val dLat = Math.toRadians(lat2 - lat1)
  val dLon = Math.toRadians(lon2 - lon1)
  val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2)
  val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return r * c
}

/**
 * Clean, lightweight 2D Map & GPS Geofence Telemetry Card (Zero WebViews, Light Map Theme)
 */
@Composable
fun BentoGeofenceMapCard(
  siteName: String,
  siteLatitude: Double,
  siteLongitude: Double,
  radiusMeters: Int,
  userLatitude: Double,
  userLongitude: Double,
  isInside: Boolean,
  distanceMeters: Double,
  accuracyMeters: Double = 8.0,
  isSearchingLocation: Boolean = false,
  locationSearchError: String? = null,
  isOutsideSimulation: Boolean = false,
  onToggleSimulator: ((Boolean) -> Unit)? = null,
  onRefreshGps: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  val formattedDistance = remember(distanceMeters) { formatDistanceClean(distanceMeters) }

  val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.3f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "PulseScale"
  )

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    color = Color.White,
    border = BorderStroke(1.dp, BentoOutline),
    shadowElevation = 2.dp,
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // 1. Header Bar: Title, Site Name, Status Badge
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
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(if (isInside) BentoSuccessContainer else Color(0xFFFFEBEE)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = if (isInside) Icons.Default.LocationOn else Icons.Default.WrongLocation,
              contentDescription = null,
              tint = if (isInside) BentoSuccess else BentoError,
              modifier = Modifier.size(20.dp),
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "Work Site Geofence",
              fontWeight = FontWeight.Bold,
              fontSize = 13.5.sp,
              color = Color.Black,
            )
            Text(
              text = siteName,
              fontSize = 11.sp,
              color = BentoTextSecondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        // Inside / Outside Status Badge
        Surface(
          shape = RoundedCornerShape(50.dp),
          color = if (isInside) BentoSuccessContainer else Color(0xFFFFEBEE),
          border = BorderStroke(
            1.dp,
            if (isInside) BentoSuccess.copy(alpha = 0.4f) else Color(0xFFFFCDD2)
          ),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
          ) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isInside) BentoSuccess else BentoError)
            )
            Text(
              text = if (isInside) "Inside ($formattedDistance)" else "Outside ($formattedDistance)",
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Bold,
              color = if (isInside) BentoSuccess else BentoError,
            )
          }
        }
      }

      // 2. Crisp Light Map Canvas
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(200.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(Color(0xFFF1F5F9))
          .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(14.dp)),
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val centerX = size.width / 2f
          val centerY = size.height / 2f
          val maxRadius = min(size.width, size.height) * 0.42f

          // A. Light map terrain base
          drawRect(color = Color(0xFFF8FAFC))

          // City blocks & landscape
          drawRoundRect(
            color = Color(0xFFDCFCE7),
            topLeft = Offset(15f, 15f),
            size = Size(size.width * 0.28f, size.height * 0.35f),
            cornerRadius = CornerRadius(8f, 8f)
          )
          drawRoundRect(
            color = Color(0xFFE2E8F0),
            topLeft = Offset(size.width * 0.65f, 20f),
            size = Size(size.width * 0.3f, size.height * 0.38f),
            cornerRadius = CornerRadius(8f, 8f)
          )
          drawRoundRect(
            color = Color(0xFFE0F2FE),
            topLeft = Offset(20f, size.height * 0.65f),
            size = Size(size.width * 0.35f, size.height * 0.28f),
            cornerRadius = CornerRadius(8f, 8f)
          )

          // Major Streets
          val streetWidth = 24f
          drawRect(
            color = Color(0xFFCBD5E1),
            topLeft = Offset(0f, centerY - streetWidth / 2f - 1.5f),
            size = Size(size.width, streetWidth + 3f)
          )
          drawRect(
            color = Color.White,
            topLeft = Offset(0f, centerY - streetWidth / 2f),
            size = Size(size.width, streetWidth)
          )
          drawRect(
            color = Color(0xFFCBD5E1),
            topLeft = Offset(centerX - streetWidth / 2f - 1.5f, 0f),
            size = Size(streetWidth + 3f, size.height)
          )
          drawRect(
            color = Color.White,
            topLeft = Offset(centerX - streetWidth / 2f, 0f),
            size = Size(streetWidth, size.height)
          )

          // B. Geofence Perimeter Ring
          val perimeterRadius = maxRadius * 0.60f
          val zoneColor = if (isInside) Color(0xFF10B981) else Color(0xFFEF4444)

          drawCircle(
            color = zoneColor.copy(alpha = 0.16f),
            radius = perimeterRadius,
            center = Offset(centerX, centerY),
          )
          drawCircle(
            color = zoneColor,
            radius = perimeterRadius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
          )

          // C. Center Work Site Pin
          val pinPath = Path().apply {
            moveTo(centerX, centerY + 2f)
            cubicTo(centerX - 12f, centerY - 12f, centerX - 12f, centerY - 24f, centerX, centerY - 24f)
            cubicTo(centerX + 12f, centerY - 24f, centerX + 12f, centerY - 12f, centerX, centerY + 2f)
            close()
          }
          drawPath(path = pinPath, color = Color(0xFF2563EB), style = Fill)
          drawPath(path = pinPath, color = Color.White, style = Stroke(width = 1.5f))
          drawCircle(color = Color.White, radius = 3.5f, center = Offset(centerX, centerY - 16f))

          // D. User Device Pin
          val dLat = userLatitude - siteLatitude
          val dLng = userLongitude - siteLongitude
          val rawAngle = atan2(dLat, dLng)

          val ratio = if (radiusMeters > 0) (distanceMeters / radiusMeters.toDouble()) else 0.0
          val userVisualDist = when {
            distanceMeters <= 5.0 -> 0f
            isInside -> (perimeterRadius * (ratio.toFloat()).coerceIn(0.2f, 0.88f))
            else -> (perimeterRadius + (maxRadius - perimeterRadius) * (ratio.toFloat() - 1f).coerceIn(0.2f, 1.0f)).coerceAtMost(maxRadius * 0.95f)
          }

          val userX = centerX + userVisualDist * cos(rawAngle).toFloat()
          val userY = centerY - userVisualDist * sin(rawAngle).toFloat()

          // Dotted Connection Line between Site and User
          drawLine(
            color = zoneColor,
            start = Offset(centerX, centerY),
            end = Offset(userX, userY),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
          )

          // User Pulse Wave
          drawCircle(
            color = zoneColor.copy(alpha = 0.25f),
            radius = 16f * pulseScale,
            center = Offset(userX, userY)
          )
          drawCircle(
            color = zoneColor,
            radius = 7.5f,
            center = Offset(userX, userY)
          )
          drawCircle(
            color = Color.White,
            radius = 3.5f,
            center = Offset(userX, userY)
          )
        }

        // Top Start Overlay: Distance & Perimeter Info
        Surface(
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(8.dp),
          shape = RoundedCornerShape(8.dp),
          color = Color.White.copy(alpha = 0.95f),
          border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
          shadowElevation = 2.dp,
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
          ) {
            Icon(
              imageVector = Icons.Default.NearMe,
              contentDescription = null,
              tint = if (isInside) BentoSuccess else BentoError,
              modifier = Modifier.size(13.dp),
            )
            Text(
              text = "Distance: $formattedDistance",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color.Black,
            )
            Text(
              text = "• Allowed: ${radiusMeters}m",
              fontSize = 10.sp,
              color = BentoTextSecondary,
            )
          }
        }

        // Top End Overlay: GPS Refresh Button
        Surface(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp),
          shape = CircleShape,
          color = Color.White.copy(alpha = 0.95f),
          border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
          shadowElevation = 2.dp,
        ) {
          IconButton(
            onClick = { onRefreshGps?.invoke() },
            modifier = Modifier.size(34.dp),
          ) {
            if (isSearchingLocation) {
              CircularProgressIndicator(
                modifier = Modifier.size(15.dp),
                strokeWidth = 2.dp,
                color = BentoBluePrimary,
              )
            } else {
              Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Refresh GPS",
                tint = BentoBluePrimary,
                modifier = Modifier.size(18.dp),
              )
            }
          }
        }

        // Bottom Coordinates Telemetry Bar
        Surface(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
          shape = RoundedCornerShape(6.dp),
          color = Color.White.copy(alpha = 0.92f),
          border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "📍 Device: ${String.format(Locale.ENGLISH, "%.4f, %.4f", userLatitude, userLongitude)} (±${accuracyMeters.toInt()}m)",
              color = Color(0xFF334155),
              fontSize = 10.sp,
              fontWeight = FontWeight.Medium,
            )
            Text(
              text = "🏢 Site: ${String.format(Locale.ENGLISH, "%.4f, %.4f", siteLatitude, siteLongitude)}",
              color = BentoTextSecondary,
              fontSize = 9.5.sp,
            )
          }
        }
      }

      // 3. Diagnostics Status Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (isInside) "✓ Device is within authorized work zone" else "⚠️ Device is outside authorized work zone",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isInside) BentoSuccess else BentoError,
          )
          Text(
            text = "Site: $siteName • Perimeter Radius: ${radiusMeters}m",
            fontSize = 10.sp,
            color = BentoTextSecondary,
          )
        }

        if (onRefreshGps != null) {
          FilledTonalButton(
            onClick = onRefreshGps,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.height(32.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = BentoBlueContainer,
              contentColor = BentoBluePrimary,
            ),
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Refresh GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      // 4. Offline Simulator Switch
      if (onToggleSimulator != null) {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFFF8FAFC),
          border = BorderStroke(1.dp, BentoOutline),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Science,
                contentDescription = null,
                tint = BentoTextSecondary,
                modifier = Modifier.size(14.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Geofence Testing Simulator",
                fontSize = 10.5.sp,
                color = BentoTextSecondary,
              )
            }

            FilterChip(
              selected = isOutsideSimulation,
              onClick = { onToggleSimulator(!isOutsideSimulation) },
              label = {
                Text(
                  if (isOutsideSimulation) "Simulating: Outside" else "Live GPS Mode",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFFFFEBEE),
                selectedLabelColor = BentoError,
              ),
              modifier = Modifier.height(28.dp),
            )
          }
        }
      }

      // 5. Error banner if any
      if (locationSearchError != null) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFFFFEBEE),
          border = BorderStroke(1.dp, BentoError.copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = BentoError, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = locationSearchError, fontSize = 10.sp, color = BentoError)
          }
        }
      }
    }
  }
}

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
 * Clean, lightweight GPS Coordinates & Geofence Telemetry Card (Pure Latitude & Longitude)
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
  showMap: Boolean = false,
  onRefreshGps: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  val formattedDistance = remember(distanceMeters) { formatDistanceClean(distanceMeters) }

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    color = Color.White,
    border = BorderStroke(1.dp, BentoOutline),
    shadowElevation = 2.dp,
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
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
              .size(38.dp)
              .clip(CircleShape)
              .background(if (isInside) BentoSuccessContainer else Color(0xFFFFEBEE)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = if (isInside) Icons.Default.LocationOn else Icons.Default.WrongLocation,
              contentDescription = null,
              tint = if (isInside) BentoSuccess else BentoError,
              modifier = Modifier.size(22.dp),
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "GPS Location & Verification",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color.Black,
            )
            Text(
              text = "Target: $siteName (${radiusMeters}m radius)",
              fontSize = 11.5.sp,
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
            if (isInside) BentoSuccess.copy(alpha = 0.5f) else Color(0xFFFFCDD2)
          ),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isInside) BentoSuccess else BentoError)
            )
            Text(
              text = if (isInside) "Inside Zone ($formattedDistance)" else "Outside Zone ($formattedDistance)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (isInside) BentoSuccess else BentoError,
            )
          }
        }
      }

      // 2. Telemetry Grid: Device Location vs Work Site Location
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // Device Current GPS Card
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(14.dp),
          color = Color(0xFFF8FAFC),
          border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.MyLocation, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Device Live GPS",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
              )
            }
            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text("Latitude:", fontSize = 10.5.sp, color = BentoTextSecondary)
              Text(
                String.format(Locale.ENGLISH, "%.6f", userLatitude),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text("Longitude:", fontSize = 10.5.sp, color = BentoTextSecondary)
              Text(
                String.format(Locale.ENGLISH, "%.6f", userLongitude),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text("Accuracy:", fontSize = 10.5.sp, color = BentoTextSecondary)
              Text(
                "±${accuracyMeters.toInt()}m",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (accuracyMeters <= 20) BentoSuccess else Color(0xFFD97706),
              )
            }
          }
        }

        // Target Site Coordinates Card
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(14.dp),
          color = Color(0xFFF8FAFC),
          border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Site Coordinates",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
              )
            }
            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text("Latitude:", fontSize = 10.5.sp, color = BentoTextSecondary)
              Text(
                String.format(Locale.ENGLISH, "%.6f", siteLatitude),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text("Longitude:", fontSize = 10.5.sp, color = BentoTextSecondary)
              Text(
                String.format(Locale.ENGLISH, "%.6f", siteLongitude),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text("Radius:", fontSize = 10.5.sp, color = BentoTextSecondary)
              Text(
                "${radiusMeters}m",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = BentoBluePrimary,
              )
            }
          }
        }
      }

      // 3. Dynamic Animated Radar Scope View (Shown only if showMap is true)
      if (showMap) {
        LiveRadarScopeView(
          isInside = isInside,
          distanceMeters = distanceMeters,
          radiusMeters = radiusMeters,
          accuracyMeters = accuracyMeters,
        )
      }

      // 4. Distance & Geofence Verification Banner
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isInside) BentoSuccessContainer.copy(alpha = 0.6f) else Color(0xFFFFEBEE),
        border = BorderStroke(1.dp, if (isInside) BentoSuccess.copy(alpha = 0.3f) else Color(0xFFFFCDD2)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
              imageVector = if (isInside) Icons.Default.CheckCircle else Icons.Default.Warning,
              contentDescription = null,
              tint = if (isInside) BentoSuccess else BentoError,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = if (isInside) "Inside Authorized Zone ($formattedDistance to center)" else "Outside Authorized Zone ($formattedDistance away)",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isInside) BentoSuccess else BentoError,
              )
              Text(
                text = "Allowed geofence threshold: ${radiusMeters} meters",
                fontSize = 10.sp,
                color = BentoTextSecondary,
              )
            }
          }

          if (onRefreshGps != null) {
            val refreshInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            Button(
              onClick = onRefreshGps,
              enabled = !isSearchingLocation,
              interactionSource = refreshInteraction,
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = BentoBluePrimary,
                contentColor = Color.White,
              ),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              modifier = Modifier
                .height(34.dp)
                .bounceOnPress(refreshInteraction, scaleDown = 0.92f),
            ) {
              if (isSearchingLocation) {
                CircularProgressIndicator(
                  modifier = Modifier.size(14.dp),
                  strokeWidth = 2.dp,
                  color = Color.White,
                )
              } else {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Refresh", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
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

/**
 * Dynamic Animated Satellite Radar Scope View
 * Renders an authentic live radar screen with rotating sweep line, concentric geofence rings,
 * pulsing target beacon, and compass orientation.
 */
@Composable
fun LiveRadarScopeView(
  isInside: Boolean,
  distanceMeters: Double,
  radiusMeters: Int,
  accuracyMeters: Double,
  modifier: Modifier = Modifier,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "radarScanner")

  val sweepAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 3600, easing = LinearEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "radarSweepAngle",
  )

  val pulseRadius by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "radarPulse",
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .height(148.dp)
      .clip(RoundedCornerShape(16.dp)),
    shape = RoundedCornerShape(16.dp),
    color = Color(0xFF070E1E),
    border = BorderStroke(1.dp, Color(0xFF1E293B)),
    shadowElevation = 2.dp,
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val maxRadius = minOf(centerX, centerY) - 14.dp.toPx()
        val center = Offset(centerX, centerY)

        // Crosshairs
        drawLine(
          color = Color(0xFF1E3A8A).copy(alpha = 0.35f),
          start = Offset(centerX - maxRadius, centerY),
          end = Offset(centerX + maxRadius, centerY),
          strokeWidth = 1.dp.toPx(),
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
        )
        drawLine(
          color = Color(0xFF1E3A8A).copy(alpha = 0.35f),
          start = Offset(centerX, centerY - maxRadius),
          end = Offset(centerX, centerY + maxRadius),
          strokeWidth = 1.dp.toPx(),
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
        )

        // Concentric distance rings
        listOf(0.33f, 0.66f, 1f).forEach { fraction ->
          val r = maxRadius * fraction
          drawCircle(
            color = Color(0xFF1E3A8A).copy(alpha = 0.3f),
            radius = r,
            center = center,
            style = Stroke(width = 1.dp.toPx()),
          )
        }

        // Geofence perimeter zone (boundary)
        val geofenceRadius = maxRadius * 0.70f
        val geofenceColor = if (isInside) BentoSuccess else Color(0xFFEF4444)
        drawCircle(
          color = geofenceColor.copy(alpha = 0.16f),
          radius = geofenceRadius,
          center = center,
          style = Fill,
        )
        drawCircle(
          color = geofenceColor.copy(alpha = 0.75f),
          radius = geofenceRadius,
          center = center,
          style = Stroke(
            width = 1.8.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
          ),
        )

        // Animated rotating radar sweep beam with sweep gradient
        drawArc(
          brush = Brush.sweepGradient(
            0.0f to Color.Transparent,
            0.82f to Color.Transparent,
            1.0f to if (isInside) Color(0x5000E676) else Color(0x5038BDF8),
            center = center,
          ),
          startAngle = sweepAngle - 90f,
          sweepAngle = 90f,
          useCenter = true,
          topLeft = Offset(centerX - maxRadius, centerY - maxRadius),
          size = Size(maxRadius * 2f, maxRadius * 2f),
        )

        // Radar sweep line
        val angleRad = Math.toRadians(sweepAngle.toDouble())
        val lineEndX = centerX + (maxRadius * cos(angleRad)).toFloat()
        val lineEndY = centerY + (maxRadius * sin(angleRad)).toFloat()
        drawLine(
          color = if (isInside) Color(0xFF00E676).copy(alpha = 0.85f) else Color(0xFF38BDF8).copy(alpha = 0.85f),
          start = center,
          end = Offset(lineEndX, lineEndY),
          strokeWidth = 1.5.dp.toPx(),
        )

        // Center Site Marker (Target Site)
        drawCircle(
          color = BentoBluePrimary,
          radius = 5.dp.toPx(),
          center = center,
        )
        drawCircle(
          color = Color.White,
          radius = 2.dp.toPx(),
          center = center,
        )

        // User device location calculation on the radar
        val distanceRatio = if (radiusMeters > 0) (distanceMeters / radiusMeters).toFloat() else 0f
        val userRadiusOffset = if (isInside) {
          (distanceRatio * geofenceRadius * 0.85f).coerceIn(0f, geofenceRadius - 6.dp.toPx())
        } else {
          (geofenceRadius + 12.dp.toPx()).coerceIn(geofenceRadius + 4.dp.toPx(), maxRadius - 6.dp.toPx())
        }
        val userAngle = if (isInside) 0.85 else 1.15
        val userX = centerX + (userRadiusOffset * cos(userAngle)).toFloat()
        val userY = centerY + (userRadiusOffset * sin(userAngle)).toFloat()
        val userPos = Offset(userX, userY)

        // Pulsing ripple around user beacon
        val userColor = if (isInside) Color(0xFF00E676) else Color(0xFFEF4444)
        drawCircle(
          color = userColor.copy(alpha = (1f - pulseRadius) * 0.7f),
          radius = 5.dp.toPx() + (pulseRadius * 15.dp.toPx()),
          center = userPos,
          style = Stroke(width = 1.5.dp.toPx()),
        )
        // Solid user beacon
        drawCircle(
          color = userColor,
          radius = 4.5.dp.toPx(),
          center = userPos,
        )
        drawCircle(
          color = Color.White,
          radius = 2.dp.toPx(),
          center = userPos,
        )
      }

      // HUD Header Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(7.dp)
              .clip(CircleShape)
              .background(if (isInside) Color(0xFF00E676) else Color(0xFFF59E0B)),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "LIVE SATELLITE RADAR",
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.sp,
          )
        }

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Color(0xFF1E293B).copy(alpha = 0.85f),
          border = BorderStroke(0.5.dp, Color(0xFF334155)),
        ) {
          Text(
            text = if (isInside) "IN GEOFENCE ✓" else "OUT OF GEOFENCE ⚠️",
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isInside) Color(0xFF00E676) else Color(0xFFEF4444),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
          )
        }
      }

      // Compass indicators
      Text(
        text = "N",
        fontSize = 8.5.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF64748B),
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(top = 22.dp),
      )
      Text(
        text = "S",
        fontSize = 8.5.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF64748B),
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 3.dp),
      )

      // Bottom Legend overlay
      Row(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(start = 12.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(BentoBluePrimary))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Site Center", fontSize = 8.5.sp, color = Color(0xFF94A3B8))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(if (isInside) Color(0xFF00E676) else Color(0xFFEF4444)))
          Spacer(modifier = Modifier.width(4.dp))
          Text("You (±${accuracyMeters.toInt()}m)", fontSize = 8.5.sp, color = Color(0xFF94A3B8))
        }
      }
    }
  }
}

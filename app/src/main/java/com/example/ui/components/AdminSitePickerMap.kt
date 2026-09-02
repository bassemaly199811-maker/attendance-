package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.min

/**
 * Clean, lightweight 2D Map & Geofence Picker (Zero WebViews, Light Map Theme, Fast & Clear)
 */
@Composable
fun AdminSitePickerMap(
  initialLat: Double,
  initialLng: Double,
  radiusMeters: Int,
  siteName: String,
  onCoordinatesChanged: (Double, Double) -> Unit,
  onMyLocationClick: (() -> Unit)? = null,
  isSearchingLocation: Boolean = false,
  modifier: Modifier = Modifier,
) {
  var currentLat by remember(initialLat) { mutableDoubleStateOf(initialLat) }
  var currentLng by remember(initialLng) { mutableDoubleStateOf(initialLng) }

  var dragOffsetX by remember { mutableFloatStateOf(0f) }
  var dragOffsetY by remember { mutableFloatStateOf(0f) }

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = Color(0xFFF8FAFC),
    border = BorderStroke(1.dp, BentoOutline),
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      // 1. Header Bar: Title, Site Name & Coordinates
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
              .size(32.dp)
              .clip(CircleShape)
              .background(BentoBlueContainer),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = BentoBluePrimary,
              modifier = Modifier.size(18.dp),
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = siteName.ifBlank { "Work Site Location" },
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = Color.Black,
            )
            Text(
              text = "Perimeter Radius: ${radiusMeters}m",
              fontSize = 11.sp,
              color = BentoTextSecondary,
            )
          }
        }

        // Live Coords Pill
        Surface(
          shape = RoundedCornerShape(50.dp),
          color = BentoBlueContainer,
          border = BorderStroke(1.dp, BentoBluePrimary.copy(alpha = 0.3f)),
        ) {
          Text(
            text = "${String.format(Locale.ENGLISH, "%.4f", currentLat)}, ${String.format(Locale.ENGLISH, "%.4f", currentLng)}",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = BentoBluePrimary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
      }

      // 2. Crisp Light Map & Geofence Canvas
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(200.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFFE2E8F0))
          .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
          .pointerInput(Unit) {
            detectTapGestures { tapOffset ->
              val centerX = size.width / 2f
              val centerY = size.height / 2f
              val deltaX = (tapOffset.x - centerX) / (size.width / 2f)
              val deltaY = (centerY - tapOffset.y) / (size.height / 2f)

              val newLat = (currentLat + deltaY * 0.0015).coerceIn(-90.0, 90.0)
              val newLng = (currentLng + deltaX * 0.0015).coerceIn(-180.0, 180.0)
              currentLat = newLat
              currentLng = newLng
              onCoordinatesChanged(newLat, newLng)
            }
          }
          .pointerInput(Unit) {
            detectDragGestures(
              onDragEnd = {
                val deltaX = dragOffsetX / 100000.0
                val deltaY = -dragOffsetY / 100000.0
                val newLat = (currentLat + deltaY).coerceIn(-90.0, 90.0)
                val newLng = (currentLng + deltaX).coerceIn(-180.0, 180.0)
                currentLat = newLat
                currentLng = newLng
                onCoordinatesChanged(newLat, newLng)
                dragOffsetX = 0f
                dragOffsetY = 0f
              },
              onDrag = { change, dragAmount ->
                change.consume()
                dragOffsetX += dragAmount.x
                dragOffsetY += dragAmount.y
              }
            )
          },
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val centerX = size.width / 2f
          val centerY = size.height / 2f
          val maxRadius = min(size.width, size.height) * 0.42f

          // A. Light map terrain base
          drawRect(color = Color(0xFFF1F5F9))

          // B. Styled City Road Grid & Blocks
          val blockColor = Color(0xFFE2E8F0)
          val roadColor = Color(0xFFFFFFFF)
          val roadBorderColor = Color(0xFFCBD5E1)

          // Background City Blocks
          drawRoundRect(
            color = Color(0xFFDCFCE7), // Park / green zone
            topLeft = Offset(15f, 15f),
            size = Size(size.width * 0.28f, size.height * 0.35f),
            cornerRadius = CornerRadius(8f, 8f)
          )
          drawRoundRect(
            color = blockColor,
            topLeft = Offset(size.width * 0.65f, 20f),
            size = Size(size.width * 0.3f, size.height * 0.38f),
            cornerRadius = CornerRadius(8f, 8f)
          )
          drawRoundRect(
            color = Color(0xFFE0F2FE), // Water / blue zone
            topLeft = Offset(20f, size.height * 0.65f),
            size = Size(size.width * 0.35f, size.height * 0.28f),
            cornerRadius = CornerRadius(8f, 8f)
          )
          drawRoundRect(
            color = blockColor,
            topLeft = Offset(size.width * 0.62f, size.height * 0.62f),
            size = Size(size.width * 0.32f, size.height * 0.32f),
            cornerRadius = CornerRadius(8f, 8f)
          )

          // Major Streets (Crossroads through the center)
          val streetWidth = 24f
          // Horizontal Main Avenue
          drawRect(
            color = roadBorderColor,
            topLeft = Offset(0f, centerY - streetWidth / 2f - 1.5f),
            size = Size(size.width, streetWidth + 3f)
          )
          drawRect(
            color = roadColor,
            topLeft = Offset(0f, centerY - streetWidth / 2f),
            size = Size(size.width, streetWidth)
          )
          // Center dashed yellow line
          drawLine(
            color = Color(0xFFF59E0B).copy(alpha = 0.8f),
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
          )

          // Vertical Main Boulevard
          drawRect(
            color = roadBorderColor,
            topLeft = Offset(centerX - streetWidth / 2f - 1.5f, 0f),
            size = Size(streetWidth + 3f, size.height)
          )
          drawRect(
            color = roadColor,
            topLeft = Offset(centerX - streetWidth / 2f, 0f),
            size = Size(streetWidth, size.height)
          )
          // Center dashed yellow line
          drawLine(
            color = Color(0xFFF59E0B).copy(alpha = 0.8f),
            start = Offset(centerX, 0f),
            end = Offset(centerX, size.height),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
          )

          // Secondary grid lanes
          drawLine(
            color = roadBorderColor,
            start = Offset(0f, size.height * 0.25f),
            end = Offset(size.width, size.height * 0.25f),
            strokeWidth = 6f
          )
          drawLine(
            color = roadBorderColor,
            start = Offset(0f, size.height * 0.75f),
            end = Offset(size.width, size.height * 0.75f),
            strokeWidth = 6f
          )

          // C. Geofence Perimeter (Blue Translucent Safe Area)
          val geofenceRadius = (maxRadius * 0.65f).coerceIn(28f, maxRadius)
          drawCircle(
            color = Color(0xFF2563EB).copy(alpha = 0.18f),
            radius = geofenceRadius,
            center = Offset(centerX, centerY),
          )
          drawCircle(
            color = Color(0xFF2563EB),
            radius = geofenceRadius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
          )

          // D. Center Site Pin (High-Contrast Red/Blue GPS Marker)
          // Shadow under pin
          drawOval(
            color = Color.Black.copy(alpha = 0.25f),
            topLeft = Offset(centerX - 10f, centerY + 2f),
            size = Size(20f, 8f)
          )

          // Map Marker Shape
          val pinPath = Path().apply {
            moveTo(centerX, centerY + 2f) // Pin tip touching ground
            cubicTo(
              centerX - 14f, centerY - 14f,
              centerX - 14f, centerY - 28f,
              centerX, centerY - 28f
            )
            cubicTo(
              centerX + 14f, centerY - 28f,
              centerX + 14f, centerY - 14f,
              centerX, centerY + 2f
            )
            close()
          }
          drawPath(path = pinPath, color = Color(0xFFDC2626), style = Fill)
          drawPath(path = pinPath, color = Color.White, style = Stroke(width = 2f))

          // Inner white dot in pin head
          drawCircle(
            color = Color.White,
            radius = 4.5f,
            center = Offset(centerX, centerY - 20f)
          )
        }

        // Top Start Guidance Badge
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
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(Icons.Default.TouchApp, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(13.dp))
            Text("Tap or drag to position site pin", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
          }
        }

        // Bottom End Geofence Radius Badge
        Surface(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(8.dp),
          shape = RoundedCornerShape(8.dp),
          color = Color.White.copy(alpha = 0.95f),
          border = BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.4f)),
          shadowElevation = 2.dp,
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF2563EB))
            )
            Text("Geofence: ${radiusMeters}m", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
          }
        }
      }

      // 3. Directional Nudge Adjusters (North, South, East, West)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("Fine Tune:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          FilledTonalButton(
            onClick = {
              val newLat = (currentLat + 0.0005).coerceIn(-90.0, 90.0)
              currentLat = newLat
              onCoordinatesChanged(newLat, currentLng)
            },
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp),
          ) {
            Text("▲ N", fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }

          FilledTonalButton(
            onClick = {
              val newLat = (currentLat - 0.0005).coerceIn(-90.0, 90.0)
              currentLat = newLat
              onCoordinatesChanged(newLat, currentLng)
            },
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp),
          ) {
            Text("▼ S", fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }

          FilledTonalButton(
            onClick = {
              val newLng = (currentLng - 0.0005).coerceIn(-180.0, 180.0)
              currentLng = newLng
              onCoordinatesChanged(currentLat, newLng)
            },
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp),
          ) {
            Text("◄ W", fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }

          FilledTonalButton(
            onClick = {
              val newLng = (currentLng + 0.0005).coerceIn(-180.0, 180.0)
              currentLng = newLng
              onCoordinatesChanged(currentLat, newLng)
            },
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp),
          ) {
            Text("► E", fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

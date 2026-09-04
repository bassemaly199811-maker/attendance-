package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Dynamic Bento Analytics & Charts Suite
 * 100% computed dynamically from real Room/Firestore AttendanceRecords, Workers, and WorkSites.
 */

enum class ChartTimeframe(val label: String, val days: Int) {
  TODAY("Today", 1),
  LAST_7_DAYS("7 Days", 7),
  LAST_30_DAYS("30 Days", 30),
}

@Composable
fun DynamicAttendanceAnalyticsSection(
  records: List<AttendanceRecord>,
  workers: List<WorkerEntity>,
  overviewWorkers: List<WorkerOverview> = emptyList(),
  sites: List<WorkSite> = emptyList(),
  shiftConfig: WorkShiftConfig = WorkShiftConfig(),
  modifier: Modifier = Modifier,
) {
  var selectedTimeframe by remember { mutableStateOf(ChartTimeframe.LAST_7_DAYS) }
  var selectedSiteId by remember { mutableStateOf("ALL") }

  // 1. Filter workers based on site selection
  val activeWorkers = remember(workers, selectedSiteId) {
    if (selectedSiteId == "ALL") workers
    else workers.filter { it.siteId == selectedSiteId || it.assignedSiteIds.contains(selectedSiteId) }
  }

  // 2. Generate date list for the selected timeframe (YYYY-MM-DD)
  val dateRange = remember(selectedTimeframe) {
    val cal = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val list = mutableListOf<String>()
    val daysCount = selectedTimeframe.days
    for (i in (daysCount - 1) downTo 0) {
      val c = Calendar.getInstance()
      c.add(Calendar.DAY_OF_YEAR, -i)
      list.add(sdf.format(c.time))
    }
    list
  }

  // 3. Compute dynamic metrics per day
  val dailyMetrics = remember(records, activeWorkers, dateRange) {
    val sdfDisplay = SimpleDateFormat("EEE d", Locale.ENGLISH)
    val sdfParse = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val totalWorkerCount = activeWorkers.size.coerceAtLeast(1)

    dateRange.map { dateStr ->
      val dayRecords = records.filter { it.date == dateStr }
      val matchingRecords = if (selectedSiteId == "ALL") {
        dayRecords
      } else {
        dayRecords.filter { r -> activeWorkers.any { it.fullName.equals(r.workerName, ignoreCase = true) } }
      }

      val presentSet = matchingRecords.map { it.workerName.trim().lowercase() }.toSet()
      val presentCount = presentSet.size
      val lateCount = matchingRecords.filter { it.isLate }.map { it.workerName.trim().lowercase() }.toSet().size
      val onTimeCount = (presentCount - lateCount).coerceAtLeast(0)
      val absentCount = (totalWorkerCount - presentCount).coerceAtLeast(0)

      val totalHours = matchingRecords.sumOf { it.workDurationHours }
      val overtimeHours = matchingRecords.sumOf { maxOf(0.0, it.workDurationHours - 8.0) }

      val cal = Calendar.getInstance()
      try {
        cal.time = sdfParse.parse(dateStr) ?: Date()
      } catch (e: Exception) {
        // fallback
      }
      val shortLabel = sdfDisplay.format(cal.time)

      DailyAttendanceMetric(
        dateStr = dateStr,
        displayLabel = shortLabel,
        presentCount = presentCount,
        onTimeCount = onTimeCount,
        lateCount = lateCount,
        absentCount = absentCount,
        totalWorkers = totalWorkerCount,
        totalHoursWorked = totalHours,
        overtimeHours = overtimeHours,
      )
    }
  }

  // 4. Overall Totals in Selected Timeframe
  val totalPresentInPeriod = dailyMetrics.sumOf { it.presentCount }
  val totalPossibleInPeriod = (activeWorkers.size.coerceAtLeast(1) * dailyMetrics.size).coerceAtLeast(1)
  val overallAttendanceRate = ((totalPresentInPeriod.toDouble() / totalPossibleInPeriod) * 100).toInt().coerceIn(0, 100)
  val totalLateInPeriod = dailyMetrics.sumOf { it.lateCount }
  val totalOnTimeInPeriod = dailyMetrics.sumOf { it.onTimeCount }
  val totalAbsentInPeriod = dailyMetrics.sumOf { it.absentCount }
  val totalHoursInPeriod = dailyMetrics.sumOf { it.totalHoursWorked }
  val totalOvertimeInPeriod = dailyMetrics.sumOf { it.overtimeHours }

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, BentoOutline),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // Header & Title
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
              .size(34.dp)
              .clip(CircleShape)
              .background(BentoBlueContainer),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.BarChart,
              contentDescription = null,
              tint = BentoBluePrimary,
              modifier = Modifier.size(18.dp),
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "Attendance Analytics",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color.Black,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = "Dynamic Charts & KPIs",
              fontSize = 11.sp,
              color = BentoTextSecondary,
              maxLines = 1,
            )
          }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Live Badge
        Surface(
          shape = RoundedCornerShape(50.dp),
          color = BentoSuccessContainer,
          border = BorderStroke(1.dp, BentoSuccess.copy(alpha = 0.3f)),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(BentoSuccess)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Live Sync",
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Bold,
              color = BentoSuccess,
              maxLines = 1,
            )
          }
        }
      }

      // Timeframe Selector Tabs
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFFF1F5F9))
          .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        ChartTimeframe.values().forEach { tf ->
          val isSelected = selectedTimeframe == tf
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (isSelected) BentoBluePrimary else Color.Transparent)
              .clickable { selectedTimeframe = tf }
              .padding(vertical = 7.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = tf.label,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) Color.White else Color(0xFF475569),
            )
          }
        }
      }

      // Work Site Filter Dropdown / Chips if sites exist
      if (sites.isNotEmpty()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Site Filter:",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = BentoTextSecondary,
            modifier = Modifier.padding(end = 2.dp),
          )

          FilterChip(
            selected = selectedSiteId == "ALL",
            onClick = { selectedSiteId = "ALL" },
            label = { Text("All Sites (${activeWorkers.size} staff)", fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = BentoBlueContainer,
              selectedLabelColor = BentoBluePrimary,
            ),
          )

          sites.forEach { site ->
            val siteWorkersCount = workers.count { it.siteId == site.id || it.assignedSiteIds.contains(site.id) }
            FilterChip(
              selected = selectedSiteId == site.id,
              onClick = { selectedSiteId = site.id },
              label = { Text("${site.name} ($siteWorkersCount)", fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = BentoBlueContainer,
                selectedLabelColor = BentoBluePrimary,
              ),
            )
          }
        }
      }

      // 4 Quick Top Metrics in Bento Grid
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        AnalyticsMiniMetric(
          title = "Attendance Rate",
          value = "$overallAttendanceRate%",
          subtitle = "$totalPresentInPeriod checks",
          containerColor = BentoBlueContainer,
          contentColor = BentoBluePrimary,
          modifier = Modifier.weight(1f),
        )
        AnalyticsMiniMetric(
          title = "On-Time Rate",
          value = if (totalPresentInPeriod > 0) "${((totalOnTimeInPeriod.toDouble() / totalPresentInPeriod) * 100).toInt()}%" else "100%",
          subtitle = "$totalOnTimeInPeriod on-time",
          containerColor = BentoSuccessContainer,
          contentColor = BentoSuccess,
          modifier = Modifier.weight(1f),
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        AnalyticsMiniMetric(
          title = "Late Arrivals",
          value = "$totalLateInPeriod",
          subtitle = "Delay incidents",
          containerColor = Color(0xFFFFF3E0),
          contentColor = Color(0xFFE65100),
          modifier = Modifier.weight(1f),
        )
        AnalyticsMiniMetric(
          title = "Total Hours Worked",
          value = String.format(Locale.ENGLISH, "%.1fh", totalHoursInPeriod),
          subtitle = if (totalOvertimeInPeriod > 0) "+${String.format(Locale.ENGLISH, "%.1fh", totalOvertimeInPeriod)} OT" else "Regular shifts",
          containerColor = Color(0xFFF3E5F5),
          contentColor = Color(0xFF7B1FA2),
          modifier = Modifier.weight(1f),
        )
      }

      HorizontalDivider(color = BentoOutline, thickness = 0.8.dp)

      // 1. Dynamic Bar Chart (Daily Trend)
      Text(
        text = "Daily Attendance Breakdown (${selectedTimeframe.label})",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
      )

      BentoDailyTrendBarChart(
        metrics = dailyMetrics,
        modifier = Modifier.fillMaxWidth(),
      )

      HorizontalDivider(color = BentoOutline, thickness = 0.8.dp)

      // 2. Donut Distribution Chart & Legend
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Status Distribution Breakdown",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = Color.Black,
        )
      }

      BentoStatusDonutChart(
        onTimeCount = totalOnTimeInPeriod,
        lateCount = totalLateInPeriod,
        absentCount = totalAbsentInPeriod,
        attendanceRate = overallAttendanceRate,
        modifier = Modifier.fillMaxWidth(),
      )

      // 3. Work Sites Distribution Bars if multiple sites exist
      if (sites.size > 1 && selectedSiteId == "ALL") {
        HorizontalDivider(color = BentoOutline, thickness = 0.8.dp)

        Text(
          text = "Work Sites Staff Distribution & Activity",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = Color.Black,
        )

        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          sites.forEach { site ->
            val siteStaff = workers.filter { it.siteId == site.id || it.assignedSiteIds.contains(site.id) }
            val siteActive = overviewWorkers.filter {
              it.siteName.equals(site.name, ignoreCase = true) && it.status == AttendanceStatus.CHECKED_IN
            }.size

            SiteOccupancyBar(
              siteName = site.name,
              radiusMeters = site.radiusMeters,
              totalStaff = siteStaff.size,
              activeOnSite = siteActive,
              maxStaffCount = workers.size.coerceAtLeast(1),
            )
          }
        }
      }
    }
  }
}

data class DailyAttendanceMetric(
  val dateStr: String,
  val displayLabel: String,
  val presentCount: Int,
  val onTimeCount: Int,
  val lateCount: Int,
  val absentCount: Int,
  val totalWorkers: Int,
  val totalHoursWorked: Double,
  val overtimeHours: Double,
)

@Composable
private fun AnalyticsMiniMetric(
  title: String,
  value: String,
  subtitle: String,
  containerColor: Color,
  contentColor: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = containerColor.copy(alpha = 0.7f),
    border = BorderStroke(1.dp, contentColor.copy(alpha = 0.2f)),
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = contentColor.copy(alpha = 0.9f),
      )
      Text(
        text = value,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = contentColor,
      )
      Text(
        text = subtitle,
        fontSize = 10.sp,
        color = BentoTextSecondary,
      )
    }
  }
}

/**
 * Interactive Daily Attendance Bar Chart
 */
@Composable
fun BentoDailyTrendBarChart(
  metrics: List<DailyAttendanceMetric>,
  modifier: Modifier = Modifier,
) {
  var selectedIndex by remember { mutableStateOf<Int?>(null) }
  val maxCount = remember(metrics) {
    metrics.maxOfOrNull { it.totalWorkers }?.coerceAtLeast(1) ?: 1
  }

  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
    // Selected Day Tooltip Callout
    AnimatedVisibility(
      visible = selectedIndex != null,
      enter = fadeIn() + expandVertically(),
      exit = fadeOut() + shrinkVertically(),
    ) {
      selectedIndex?.let { idx ->
        val m = metrics.getOrNull(idx)
        if (m != null) {
          val rate = if (m.totalWorkers > 0) ((m.presentCount.toDouble() / m.totalWorkers) * 100).toInt() else 0
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column {
                Text(
                  text = "${m.displayLabel} (${m.dateStr})",
                  color = Color.White,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = "On-Time: ${m.onTimeCount}  •  Late: ${m.lateCount}  •  Absent: ${m.absentCount}",
                  color = Color(0xFF94A3B8),
                  fontSize = 10.5.sp,
                )
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (rate >= 80) BentoSuccess else BentoWarning,
              ) {
                Text(
                  text = "$rate% Present",
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
              }
            }
          }
        }
      }
    }

    // Chart Canvas Area
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(140.dp)
        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
        .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
      ) {
        metrics.forEachIndexed { index, m ->
          val isSelected = selectedIndex == index
          val presentHeightRatio = (m.onTimeCount.toFloat() / maxCount).coerceIn(0f, 1f)
          val lateHeightRatio = (m.lateCount.toFloat() / maxCount).coerceIn(0f, 1f)
          val absentHeightRatio = (m.absentCount.toFloat() / maxCount).coerceIn(0f, 1f)

          Column(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .clickable {
                selectedIndex = if (selectedIndex == index) null else index
              },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
          ) {
            // Bars container
            Box(
              modifier = Modifier
                .width(if (metrics.size > 14) 8.dp else 18.dp)
                .weight(1f, fill = false)
                .height(95.dp),
              contentAlignment = Alignment.BottomCenter,
            ) {
              // Full background track
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .clip(RoundedCornerShape(4.dp))
                  .background(Color(0xFFE2E8F0).copy(alpha = 0.6f))
              )

              // Stacked Bars
              Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Bottom,
              ) {
                // Late bar (amber)
                if (m.lateCount > 0) {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .height((95 * lateHeightRatio).dp.coerceAtLeast(3.dp))
                      .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                      .background(if (isSelected) Color(0xFFF59E0B) else Color(0xFFFBBF24))
                  )
                }

                // On-Time bar (green)
                if (m.onTimeCount > 0) {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .height((95 * presentHeightRatio).dp.coerceAtLeast(3.dp))
                      .clip(if (m.lateCount == 0) RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp) else RoundedCornerShape(0.dp))
                      .background(if (isSelected) BentoSuccess else BentoSuccess.copy(alpha = 0.85f))
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Date Label below bar
            Text(
              text = m.displayLabel.take(3),
              fontSize = 9.5.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) BentoBluePrimary else Color(0xFF64748B),
              maxLines = 1,
              overflow = TextOverflow.Clip,
            )
          }
        }
      }
    }

    // Chart Legend
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      ChartLegendItem(color = BentoSuccess, label = "On-Time (حاضر)")
      Spacer(modifier = Modifier.width(16.dp))
      ChartLegendItem(color = Color(0xFFF59E0B), label = "Late (متأخر)")
      Spacer(modifier = Modifier.width(16.dp))
      ChartLegendItem(color = Color(0xFFCBD5E1), label = "Absent (غائب)")
    }
  }
}

@Composable
private fun ChartLegendItem(color: Color, label: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(color)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = label,
      fontSize = 10.5.sp,
      color = BentoTextSecondary,
      fontWeight = FontWeight.Medium,
    )
  }
}

/**
 * Dynamic Canvas Donut Chart for Attendance Status
 */
@Composable
fun BentoStatusDonutChart(
  onTimeCount: Int,
  lateCount: Int,
  absentCount: Int,
  attendanceRate: Int,
  modifier: Modifier = Modifier,
) {
  val total = (onTimeCount + lateCount + absentCount).coerceAtLeast(1)
  val onTimeAngle = (onTimeCount.toFloat() / total) * 360f
  val lateAngle = (lateCount.toFloat() / total) * 360f
  val absentAngle = (absentCount.toFloat() / total) * 360f

  val onTimeColor = BentoSuccess
  val lateColor = Color(0xFFF59E0B)
  val absentColor = Color(0xFFEF4444)

  Row(
    modifier = modifier
      .fillMaxWidth()
      .background(Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
      .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
      .padding(14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Canvas Donut Ring
    Box(
      modifier = Modifier.size(100.dp),
      contentAlignment = Alignment.Center,
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 14.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val centerOffset = Offset(size.width / 2, size.height / 2)
        val arcSize = Size(radius * 2, radius * 2)
        val topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius)

        // Background Ring
        drawCircle(
          color = Color(0xFFE2E8F0),
          radius = radius,
          center = centerOffset,
          style = Stroke(width = strokeWidth),
        )

        var startAngle = -90f

        // On-Time Arc
        if (onTimeAngle > 0f) {
          drawArc(
            color = onTimeColor,
            startAngle = startAngle,
            sweepAngle = onTimeAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
          )
          startAngle += onTimeAngle
        }

        // Late Arc
        if (lateAngle > 0f) {
          drawArc(
            color = lateColor,
            startAngle = startAngle,
            sweepAngle = lateAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
          )
          startAngle += lateAngle
        }

        // Absent Arc
        if (absentAngle > 0f) {
          drawArc(
            color = absentColor,
            startAngle = startAngle,
            sweepAngle = absentAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
          )
        }
      }

      // Center Percentage Label
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "$attendanceRate%",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A),
        )
        Text(
          text = "Attendance",
          fontSize = 8.5.sp,
          color = BentoTextSecondary,
          fontWeight = FontWeight.Medium,
        )
      }
    }

    // Legend & Detailed Percentage Items
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      DonutLegendRow(
        color = onTimeColor,
        title = "On-Time (في الموعد)",
        count = onTimeCount,
        percentage = if (total > 0) ((onTimeCount.toDouble() / total) * 100).toInt() else 0,
      )
      DonutLegendRow(
        color = lateColor,
        title = "Late (تأخير)",
        count = lateCount,
        percentage = if (total > 0) ((lateCount.toDouble() / total) * 100).toInt() else 0,
      )
      DonutLegendRow(
        color = absentColor,
        title = "Absent / Off (غياب)",
        count = absentCount,
        percentage = if (total > 0) ((absentCount.toDouble() / total) * 100).toInt() else 0,
      )
    }
  }
}

@Composable
private fun DonutLegendRow(
  color: Color,
  title: String,
  count: Int,
  percentage: Int,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
      Box(
        modifier = Modifier
          .size(10.dp)
          .clip(RoundedCornerShape(3.dp))
          .background(color)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = title,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF1E293B),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = "$count",
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0F172A),
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = "($percentage%)",
        fontSize = 10.sp,
        color = BentoTextSecondary,
      )
    }
  }
}

/**
 * Work Site Staff Occupancy Bar
 */
@Composable
private fun SiteOccupancyBar(
  siteName: String,
  radiusMeters: Int,
  totalStaff: Int,
  activeOnSite: Int,
  maxStaffCount: Int,
) {
  val ratio = (totalStaff.toFloat() / maxStaffCount).coerceIn(0.05f, 1f)

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Default.LocationOn,
          contentDescription = null,
          tint = BentoBluePrimary,
          modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = siteName,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF1E293B),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = Color(0xFFF1F5F9),
        ) {
          Text(
            text = "${radiusMeters}m radius",
            fontSize = 9.sp,
            color = BentoTextSecondary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
          )
        }
      }

      Text(
        text = "$activeOnSite active / $totalStaff assigned",
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (activeOnSite > 0) BentoSuccess else BentoTextSecondary,
      )
    }

    // Progress track
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(Color(0xFFE2E8F0)),
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(fraction = ratio)
          .fillMaxHeight()
          .clip(RoundedCornerShape(4.dp))
          .background(
            Brush.horizontalGradient(
              listOf(BentoBluePrimary, Color(0xFF00B0FF))
            )
          ),
      )
    }
  }
}

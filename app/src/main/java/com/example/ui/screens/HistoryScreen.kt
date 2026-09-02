package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.bounceClick
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.WorkSite
import com.example.data.model.WorkerEntity
import com.example.data.model.WorkerOverview
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import com.example.service.CloudSyncService
import com.example.ui.theme.BentoBlueContainer
import com.example.ui.theme.BentoBluePrimary
import com.example.ui.theme.BentoError
import com.example.ui.theme.BentoErrorContainer
import com.example.ui.theme.BentoLilac
import com.example.ui.theme.BentoLilacText
import com.example.ui.theme.BentoOutline
import com.example.ui.theme.BentoSuccess
import com.example.ui.theme.BentoSuccessContainer
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.BentoTileGray
import com.example.ui.theme.BentoWarning
import com.example.ui.theme.BentoWarningContainer
import com.example.util.exportAttendanceToCsv
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class PhotoDialogData(
  val title: String,
  val photoUri: String?,
  val photoBase64: String? = null,
  val driveUrl: String?,
  val date: String,
  val time: String?,
  val isCheckIn: Boolean,
)

enum class HistoryDateFilter(val label: String) {
  ALL("All Periods"),
  TODAY("Today"),
  YESTERDAY("Yesterday"),
  LAST_7_DAYS("Last 7 Days"),
  THIS_MONTH("This Month"),
  LAST_MONTH("Last Month"),
  CUSTOM_MONTH("Select Month..."),
}

fun formatEnglishDateWithDay(dateString: String): String {
  return try {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val parsed = sdf.parse(dateString) ?: return dateString
    val englishFormat = SimpleDateFormat("EEEE • dd MMMM yyyy", Locale.ENGLISH)
    englishFormat.format(parsed)
  } catch (e: Exception) {
    dateString
  }
}

fun isDateWithinDays(dateString: String, days: Int): Boolean {
  return try {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val date = sdf.parse(dateString) ?: return false
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    cal.add(Calendar.DAY_OF_YEAR, -days)
    date.time >= cal.timeInMillis
  } catch (e: Exception) {
    true
  }
}

fun isDateBetween(dateString: String, startDate: String, endDate: String): Boolean {
  return try {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val d = sdf.parse(dateString) ?: return false
    val start = if (startDate.isNotBlank()) sdf.parse(startDate) else null
    val end = if (endDate.isNotBlank()) sdf.parse(endDate) else null
    (start == null || !d.before(start)) && (end == null || !d.after(end))
  } catch (e: Exception) {
    true
  }
}

enum class ExportPeriodPreset(val labelEn: String, val labelAr: String) {
  ALL("All Records", "كل السجلات"),
  TODAY("Today", "اليوم"),
  YESTERDAY("Yesterday", "أمس"),
  LAST_7_DAYS("Last 7 Days", "آخر 7 أيام"),
  THIS_MONTH("This Month", "الشهر الحالي"),
  LAST_MONTH("Last Month", "الشهر الماضي"),
  LAST_30_DAYS("Last 30 Days", "آخر 30 يوماً"),
  CUSTOM("Custom Range", "فترة مخصصة"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
  records: List<AttendanceRecord>,
  workers: List<WorkerOverview> = emptyList(),
  rawWorkers: List<WorkerEntity> = emptyList(),
  sites: List<WorkSite> = emptyList(),
  isRefreshing: Boolean = false,
  lastSyncTime: String? = null,
  isAdmin: Boolean = true,
  onRefresh: () -> Unit = {},
  onAddRecord: (AttendanceRecord) -> Unit = {},
  onDeleteRecord: (AttendanceRecord) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  var selectedPhotoDialog by remember { mutableStateOf<PhotoDialogData?>(null) }
  var recordToDelete by remember { mutableStateOf<AttendanceRecord?>(null) }
  var showAddRecordDialog by remember { mutableStateOf(false) }
  var showExportPeriodDialog by remember { mutableStateOf(false) }
  var visibleCount by remember { mutableIntStateOf(5) }
  var isLoadingMore by remember { mutableStateOf(false) }
  var isExportingCsv by remember { mutableStateOf(false) }
  var exportErrorMessage by remember { mutableStateOf<String?>(null) }
  val coroutineScope = rememberCoroutineScope()
  val context = LocalContext.current

  // Filter States
  var selectedDateFilter by remember { mutableStateOf(HistoryDateFilter.ALL) }
  var selectedWorkerFilter by remember { mutableStateOf("ALL") }
  var searchQuery by remember { mutableStateOf("") }
  var isWorkerDropdownExpanded by remember { mutableStateOf(false) }
  var isMonthDropdownExpanded by remember { mutableStateOf(false) }

  // Today, Yesterday, Current Month, and Last Month Strings
  val todayStr = remember {
    SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
  }
  val yesterdayStr = remember {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -1)
    SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(cal.time)
  }
  val currentMonthStr = remember {
    SimpleDateFormat("yyyy-MM", Locale.ENGLISH).format(Date())
  }
  val lastMonthStr = remember {
    val cal = Calendar.getInstance()
    cal.add(Calendar.MONTH, -1)
    SimpleDateFormat("yyyy-MM", Locale.ENGLISH).format(cal.time)
  }

  // Extract distinct months from records for custom month picker
  val distinctMonths = remember(records) {
    val months = records.map { rec ->
      if (rec.workDate.length >= 7) rec.workDate.substring(0, 7) else ""
    }.filter { it.isNotEmpty() }.toMutableSet()
    months.add(currentMonthStr)
    months.add(lastMonthStr)
    months.toList().sortedDescending()
  }
  var selectedCustomMonth by remember { mutableStateOf(currentMonthStr) }

  // Extract distinct worker names from records, workersList, and rawWorkers
  val distinctWorkers = remember(records, workers, rawWorkers) {
    val fromRecords = records.map { it.workerName.ifEmpty { "Primary Staff" } }
    val fromWorkers = workers.map { it.fullName }
    val fromRaw = rawWorkers.map { it.fullName }
    (fromRecords + fromWorkers + fromRaw).filter { it.isNotBlank() }.distinct().sorted()
  }

  // Filtered Records
  val filteredRecords = remember(records, selectedDateFilter, selectedWorkerFilter, searchQuery, selectedCustomMonth) {
    records.filter { record ->
      val workerName = record.workerName.ifEmpty { "Primary Staff" }

      // 1. Worker filter
      val matchesWorker = selectedWorkerFilter == "ALL" || workerName.equals(selectedWorkerFilter, ignoreCase = true)

      // 2. Date / Month filter
      val matchesDate = when (selectedDateFilter) {
        HistoryDateFilter.ALL -> true
        HistoryDateFilter.TODAY -> record.workDate == todayStr
        HistoryDateFilter.YESTERDAY -> record.workDate == yesterdayStr
        HistoryDateFilter.LAST_7_DAYS -> isDateWithinDays(record.workDate, 7)
        HistoryDateFilter.THIS_MONTH -> record.workDate.startsWith(currentMonthStr)
        HistoryDateFilter.LAST_MONTH -> record.workDate.startsWith(lastMonthStr)
        HistoryDateFilter.CUSTOM_MONTH -> record.workDate.startsWith(selectedCustomMonth)
      }

      // 3. Search query
      val matchesSearch = if (searchQuery.isBlank()) true else {
        workerName.contains(searchQuery, ignoreCase = true) ||
          record.workDate.contains(searchQuery, ignoreCase = true) ||
          record.siteName.contains(searchQuery, ignoreCase = true)
      }

      matchesWorker && matchesDate && matchesSearch
    }
  }

  val visibleRecords = remember(filteredRecords, visibleCount) {
    filteredRecords.take(visibleCount)
  }

  fun handleExportCsv(
    recordsToExport: List<AttendanceRecord> = filteredRecords,
    periodLabel: String = "Selected Records",
  ) {
    if (recordsToExport.isEmpty()) {
      exportErrorMessage = "No attendance records found to export for selected period."
      return
    }
    exportErrorMessage = null
    isExportingCsv = true
    coroutineScope.launch {
      try {
        val uri = withContext(Dispatchers.IO) {
          exportAttendanceToCsv(context, recordsToExport)
        }
        isExportingCsv = false
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
          type = "text/csv"
          putExtra(Intent.EXTRA_SUBJECT, "Attendance Records Export ($periodLabel) - $todayStr")
          putExtra(Intent.EXTRA_TEXT, "Exported attendance records ($periodLabel - ${recordsToExport.size} records) from Work Attendance.")
          putExtra(Intent.EXTRA_STREAM, uri)
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Share Attendance CSV")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
      } catch (e: Exception) {
        isExportingCsv = false
        exportErrorMessage = "Export failed: ${e.localizedMessage ?: "Unknown error"}"
      }
    }
  }

  PullToRefreshBox(
    isRefreshing = isRefreshing,
    onRefresh = onRefresh,
    modifier = modifier.fillMaxSize().testTag("history_screen"),
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    ) {
      Spacer(modifier = Modifier.height(4.dp))

      // 1. Header: Title at top, Action Buttons below with improved spacing
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 10.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // Top Row: "Attendance Log" Title and Record Count
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
              text = "Attendance Log",
              fontSize = 22.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color.Black,
              letterSpacing = (-0.3).sp,
            )
            Text(
              text = if (isAdmin) "Filter daily logs, review records, and export CSV reports" else "Your verified daily logs and working hours",
              fontSize = 12.sp,
              color = BentoTextSecondary,
              modifier = Modifier.padding(top = 2.dp),
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = BentoSuccessContainer,
            border = BorderStroke(1.dp, BentoSuccess.copy(alpha = 0.35f)),
          ) {
            Text(
              text = "${filteredRecords.size} / ${records.size} Records",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = BentoSuccess,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              maxLines = 1,
            )
          }
        }

        // Action Buttons Row directly below the Attendance Log title
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          // Export CSV Button (Admin Only - Removed for workers)
          if (isAdmin) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = BentoLilac,
              border = BorderStroke(1.dp, BentoLilacText.copy(alpha = 0.35f)),
              modifier = Modifier
                .bounceClick(scaleDown = 0.94f, enabled = !isExportingCsv) {
                  showExportPeriodDialog = true
                }
                .testTag("export_csv_button"),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              ) {
                if (isExportingCsv) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = BentoLilacText,
                    strokeWidth = 2.dp,
                  )
                } else {
                  Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Export CSV",
                    tint = BentoLilacText,
                    modifier = Modifier.size(16.dp),
                  )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (isExportingCsv) "Exporting..." else "Export CSV",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = BentoLilacText,
                  maxLines = 1,
                )
              }
            }

            // Add Manual Attendance Record Button (Admin Only)
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = BentoBluePrimary,
              modifier = Modifier.bounceClick(scaleDown = 0.94f) { showAddRecordDialog = true },
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = "Add Record",
                  tint = Color.White,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Add Record",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  maxLines = 1,
                )
              }
            }
          }

          // Cloud Sync / Refresh Button (For both Admin & Worker)
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = BentoBlueContainer,
            border = BorderStroke(1.dp, BentoBluePrimary.copy(alpha = 0.25f)),
            modifier = Modifier.clickable { onRefresh() },
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
              if (isRefreshing) {
                CircularProgressIndicator(
                  modifier = Modifier.size(14.dp),
                  color = BentoBluePrimary,
                  strokeWidth = 2.dp,
                )
              } else {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = "Cloud Refresh",
                  tint = BentoBluePrimary,
                  modifier = Modifier.size(16.dp),
                )
              }
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isRefreshing) "Syncing..." else "Cloud Sync",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BentoBluePrimary,
                maxLines = 1,
              )
            }
          }
        }

        // Error message banner if export or any operation fails
        if (exportErrorMessage != null) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = BentoErrorContainer,
            border = BorderStroke(1.dp, BentoError.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
              ) {
                Icon(Icons.Default.WarningAmber, contentDescription = null, tint = BentoError, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = exportErrorMessage ?: "",
                  fontSize = 11.5.sp,
                  color = BentoError,
                  fontWeight = FontWeight.Medium,
                )
              }
              IconButton(
                onClick = { exportErrorMessage = null },
                modifier = Modifier.size(20.dp),
              ) {
                Icon(Icons.Default.Clear, contentDescription = "Dismiss", tint = BentoError, modifier = Modifier.size(14.dp))
              }
            }
          }
        }
      }

      // 2. Filter Bar Card: Employee Selector & Day/Month Filters
      Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoOutline),
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          // Employee Dropdown Filter (Admin Only)
          if (isAdmin) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              ExposedDropdownMenuBox(
                expanded = isWorkerDropdownExpanded,
                onExpandedChange = { isWorkerDropdownExpanded = !isWorkerDropdownExpanded },
                modifier = Modifier.weight(1f),
              ) {
                OutlinedTextField(
                  value = if (selectedWorkerFilter == "ALL") "All Workers" else selectedWorkerFilter,
                  onValueChange = {},
                  readOnly = true,
                  trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isWorkerDropdownExpanded) },
                  leadingIcon = {
                    Icon(
                      imageVector = if (selectedWorkerFilter == "ALL") Icons.Default.People else Icons.Default.Person,
                      contentDescription = null,
                      tint = BentoBluePrimary,
                      modifier = Modifier.size(18.dp),
                    )
                  },
                  modifier = Modifier.menuAnchor().fillMaxWidth(),
                  shape = RoundedCornerShape(12.dp),
                  colors =
                    OutlinedTextFieldDefaults.colors(
                      focusedTextColor = Color.Black,
                      unfocusedTextColor = Color.Black,
                      unfocusedContainerColor = BentoTileGray,
                      focusedContainerColor = BentoTileGray,
                      unfocusedBorderColor = BentoOutline,
                      focusedBorderColor = BentoBluePrimary,
                    ),
                  textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black),
                  singleLine = true,
                )

                ExposedDropdownMenu(
                  expanded = isWorkerDropdownExpanded,
                  onDismissRequest = { isWorkerDropdownExpanded = false },
                  modifier = Modifier.background(Color.White),
                ) {
                  DropdownMenuItem(
                    text = {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                      ) {
                        Text(
                          text = "All Workers",
                          fontSize = 13.5.sp,
                          fontWeight = FontWeight.Bold,
                          color = Color.Black,
                        )
                        if (distinctWorkers.isNotEmpty()) {
                          Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BentoBlueContainer,
                          ) {
                            Text(
                              text = "${distinctWorkers.size} Workers",
                              fontSize = 11.sp,
                              fontWeight = FontWeight.Bold,
                              color = BentoBluePrimary,
                              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                          }
                        }
                      }
                    },
                    onClick = {
                      selectedWorkerFilter = "ALL"
                      isWorkerDropdownExpanded = false
                    },
                    leadingIcon = {
                      Icon(Icons.Default.People, contentDescription = null, tint = BentoBluePrimary)
                    },
                  )

                  if (distinctWorkers.isNotEmpty()) {
                    HorizontalDivider(color = BentoOutline.copy(alpha = 0.5f))
                  }

                  distinctWorkers.forEach { name ->
                    val rawWorker = rawWorkers.find { it.fullName.equals(name, ignoreCase = true) }
                    val overviewWorker = workers.find { it.fullName.equals(name, ignoreCase = true) }
                    val roleText = rawWorker?.role?.ifBlank { null } ?: overviewWorker?.role?.ifBlank { null }
                    val siteText = rawWorker?.siteName?.ifBlank { null } ?: overviewWorker?.siteName?.ifBlank { null }

                    DropdownMenuItem(
                      text = {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                          Text(
                            text = name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                          )
                          if (roleText != null || siteText != null) {
                            Text(
                              text = listOfNotNull(roleText, siteText).joinToString(" • "),
                              fontSize = 11.sp,
                              color = BentoTextSecondary,
                            )
                          }
                        }
                      },
                      onClick = {
                        selectedWorkerFilter = name
                        isWorkerDropdownExpanded = false
                      },
                      leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = BentoBluePrimary)
                      },
                    )
                  }
                }
              }

              if (selectedWorkerFilter != "ALL" || selectedDateFilter != HistoryDateFilter.ALL || searchQuery.isNotEmpty()) {
                Surface(
                  onClick = {
                    selectedWorkerFilter = "ALL"
                    selectedDateFilter = HistoryDateFilter.ALL
                    searchQuery = ""
                  },
                  shape = RoundedCornerShape(10.dp),
                  color = BentoErrorContainer.copy(alpha = 0.5f),
                  border = BorderStroke(1.dp, BentoError.copy(alpha = 0.3f)),
                  modifier = Modifier.height(48.dp),
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear Filters", tint = BentoError, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear Filter", fontSize = 11.5.sp, color = BentoError, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }

          // Horizontal Scrollable Day & Month Filter Chips
          Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            HistoryDateFilter.values().forEach { filter ->
              val isSelected = selectedDateFilter == filter
              FilterChip(
                selected = isSelected,
                onClick = { selectedDateFilter = filter },
                label = {
                  Text(
                    text = if (filter == HistoryDateFilter.CUSTOM_MONTH && selectedDateFilter == HistoryDateFilter.CUSTOM_MONTH) {
                      "Month: $selectedCustomMonth"
                    } else {
                      filter.label
                    },
                    fontSize = 11.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Color.Black,
                  )
                },
                leadingIcon = {
                  if (isSelected) {
                    Icon(
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = null,
                      modifier = Modifier.size(14.dp),
                    )
                  }
                },
                colors =
                  FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BentoBluePrimary,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White,
                    containerColor = BentoTileGray,
                    labelColor = Color.Black,
                  ),
                border =
                  FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) BentoBluePrimary else BentoOutline,
                  ),
                shape = RoundedCornerShape(20.dp),
              )
            }
          }

          // Month Selection Dropdown if CUSTOM_MONTH is selected
          if (selectedDateFilter == HistoryDateFilter.CUSTOM_MONTH) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = "Select Month:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
              )
              ExposedDropdownMenuBox(
                expanded = isMonthDropdownExpanded,
                onExpandedChange = { isMonthDropdownExpanded = !isMonthDropdownExpanded },
                modifier = Modifier.weight(1f),
              ) {
                OutlinedTextField(
                  value = selectedCustomMonth,
                  onValueChange = {},
                  readOnly = true,
                  trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMonthDropdownExpanded) },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.CalendarMonth,
                      contentDescription = null,
                      tint = BentoBluePrimary,
                      modifier = Modifier.size(16.dp),
                    )
                  },
                  modifier = Modifier.menuAnchor().fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp),
                  colors =
                    OutlinedTextFieldDefaults.colors(
                      focusedTextColor = Color.Black,
                      unfocusedTextColor = Color.Black,
                      unfocusedContainerColor = BentoTileGray,
                      focusedContainerColor = BentoTileGray,
                      unfocusedBorderColor = BentoOutline,
                      focusedBorderColor = BentoBluePrimary,
                    ),
                  textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black),
                  singleLine = true,
                )

                ExposedDropdownMenu(
                  expanded = isMonthDropdownExpanded,
                  onDismissRequest = { isMonthDropdownExpanded = false },
                  modifier = Modifier.background(Color.White),
                ) {
                  distinctMonths.forEach { month ->
                    DropdownMenuItem(
                      text = { Text(month, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.Black) },
                      onClick = {
                        selectedCustomMonth = month
                        isMonthDropdownExpanded = false
                      },
                      leadingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = BentoBluePrimary)
                      },
                    )
                  }
                }
              }
            }
          }
        }
      }

      // 3. Google Drive Folder Banner (Admin Only & Compact)
      if (isAdmin) {
        Card(
          modifier =
            Modifier
              .fillMaxWidth()
              .clickable {
                CloudSyncService.openGoogleDriveFolder(context)
              }
              .padding(bottom = 8.dp),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF)),
          border = BorderStroke(1.dp, BentoBluePrimary.copy(alpha = 0.25f)),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier =
                  Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(BentoBluePrimary),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Default.Folder,
                  contentDescription = "Google Drive Folder",
                  tint = Color.White,
                  modifier = Modifier.size(15.dp),
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Google Drive Cloud Photos",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Black,
                )
                Text(
                  text = "Cloud synced records & photo backup",
                  fontSize = 9.5.sp,
                  color = BentoTextSecondary,
                )
              }
            }

            Icon(
              imageVector = Icons.Default.OpenInNew,
              contentDescription = "Open Folder",
              tint = BentoBluePrimary,
              modifier = Modifier.size(14.dp),
            )
          }
        }
      }

      // 4. Records List
      if (filteredRecords.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(24.dp),
          ) {
            Icon(
              imageVector = Icons.Default.EventNote,
              contentDescription = null,
              tint = BentoTextSecondary.copy(alpha = 0.5f),
              modifier = Modifier.size(54.dp),
            )
            Text(
              text = if (records.isEmpty()) "No attendance records found yet" else "No records match the selected filter",
              color = Color.Black,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
            )
            Text(
              text = "Try adjusting date filter or worker selection to view logs.",
              color = BentoTextSecondary,
              fontSize = 12.sp,
            )
          }
        }
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          contentPadding = PaddingValues(bottom = 24.dp),
        ) {
          items(visibleRecords, key = { it.id }) { record ->
            BentoHistoryCard(
              record = record,
              isAdmin = isAdmin,
              onCheckInPhotoClick = {
                selectedPhotoDialog =
                  PhotoDialogData(
                    title = "Check-In Photo (${record.workerName.ifEmpty { "Employee" }})",
                    photoUri = record.checkInPhotoUri,
                    photoBase64 = record.checkInPhotoBase64,
                    driveUrl = record.checkInDriveUrl ?: CloudSyncService.DRIVE_FOLDER_URL,
                    date = record.workDate,
                    time = record.checkInTime,
                    isCheckIn = true,
                  )
              },
              onCheckOutPhotoClick = {
                selectedPhotoDialog =
                  PhotoDialogData(
                    title = "Check-Out Photo (${record.workerName.ifEmpty { "Employee" }})",
                    photoUri = record.checkOutPhotoUri,
                    photoBase64 = record.checkOutPhotoBase64,
                    driveUrl = record.checkOutDriveUrl ?: CloudSyncService.DRIVE_FOLDER_URL,
                    date = record.workDate,
                    time = record.checkOutTime,
                    isCheckIn = false,
                  )
              },
              onDeleteClick = { recordToDelete = record },
            )
          }

          if (visibleCount < filteredRecords.size) {
            item {
              Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
              ) {
                if (isLoadingMore) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(12.dp),
                  ) {
                    CircularProgressIndicator(
                      modifier = Modifier.size(16.dp),
                      color = BentoBluePrimary,
                      strokeWidth = 2.dp,
                    )
                    Text("Loading next 5 records...", fontSize = 12.sp, color = BentoBluePrimary, fontWeight = FontWeight.Bold)
                  }
                } else {
                  OutlinedButton(
                    onClick = {
                      isLoadingMore = true
                      coroutineScope.launch {
                        kotlinx.coroutines.delay(250)
                        visibleCount += 5
                        isLoadingMore = false
                      }
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BentoBluePrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoBluePrimary),
                  ) {
                    Icon(
                      imageVector = Icons.Default.Refresh,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "Load More Records (+5 of ${filteredRecords.size})",
                      fontWeight = FontWeight.Bold,
                      fontSize = 12.5.sp,
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Delete Record Confirmation Dialog
  recordToDelete?.let { record ->
    val empName = record.workerName.ifEmpty { "Main Employee" }
    AlertDialog(
      onDismissRequest = { recordToDelete = null },
      containerColor = Color.White,
      titleContentColor = Color.Black,
      textContentColor = Color.Black,
      icon = {
        Icon(
          imageVector = Icons.Default.DeleteOutline,
          contentDescription = null,
          tint = BentoError,
          modifier = Modifier.size(40.dp),
        )
      },
      title = {
        Text(
          text = "Confirm Delete Record & Photos",
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = Color.Black,
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Are you sure you want to delete attendance record for ($empName) on (${record.workDate})?",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
          )
          Text(
            text = "• Will delete record from cloud database.\n• Will remove linked check-in/check-out photos from Google Drive and local cache.\n• If this record is for today, check-in action buttons will reset to start new entry.",
            fontSize = 12.5.sp,
            color = Color.Black,
            lineHeight = 19.sp,
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onDeleteRecord(record)
            recordToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = BentoError),
          shape = RoundedCornerShape(12.dp),
        ) {
          Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Delete & Remove Photos", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { recordToDelete = null }) {
          Text("Cancel", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      },
    )
  }

  // Photo Viewer Dialog
  selectedPhotoDialog?.let { dialogData ->
    val effectiveUrl = dialogData.driveUrl ?: CloudSyncService.DRIVE_FOLDER_URL
    AlertDialog(
      onDismissRequest = { selectedPhotoDialog = null },
      containerColor = Color.White,
      titleContentColor = Color.Black,
      textContentColor = Color.Black,
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.PhotoCamera,
            contentDescription = null,
            tint = if (dialogData.isCheckIn) BentoSuccess else BentoBluePrimary,
            modifier = Modifier.size(22.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = dialogData.title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.Black,
          )
        }
      },
      text = {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Box(
            modifier =
              Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(BentoTileGray),
            contentAlignment = Alignment.Center,
          ) {
            val dialogBitmap = remember(dialogData.photoUri, dialogData.photoBase64) {
              if (!dialogData.photoBase64.isNullOrBlank()) {
                try {
                  val clean = if (dialogData.photoBase64.contains(",")) dialogData.photoBase64.substringAfter(",") else dialogData.photoBase64
                  val bytes = android.util.Base64.decode(clean, android.util.Base64.DEFAULT)
                  BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } catch (_: Exception) {
                  null
                }
              } else if (!dialogData.photoUri.isNullOrBlank() && !dialogData.photoUri.startsWith("http")) {
                try {
                  val file = java.io.File(dialogData.photoUri)
                  if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                  } else {
                    null
                  }
                } catch (_: Exception) {
                  null
                }
              } else {
                null
              }
            }

            if (dialogBitmap != null) {
              Image(
                bitmap = dialogBitmap.asImageBitmap(),
                contentDescription = dialogData.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
              )
            } else if (!dialogData.photoUri.isNullOrBlank() && dialogData.photoUri.startsWith("http")) {
              AsyncImage(
                model =
                  ImageRequest.Builder(context)
                    .data(dialogData.photoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = dialogData.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
              )
            } else {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                  imageVector = Icons.Default.CameraAlt,
                  contentDescription = null,
                  tint = BentoTextSecondary,
                  modifier = Modifier.size(48.dp),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Photo stored in Google Drive",
                  fontSize = 12.sp,
                  color = BentoTextSecondary,
                )
              }
            }

            // Overlay banner
            Box(
              modifier =
                Modifier
                  .align(Alignment.BottomCenter)
                  .fillMaxWidth()
                  .background(Color.Black.copy(alpha = 0.70f))
                  .padding(vertical = 6.dp, horizontal = 10.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "Date: ${dialogData.date}",
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = "Time: ${dialogData.time ?: "--:--"}",
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }

          Text(
            text = if (isAdmin) "Live photo verified and synced to Google Drive cloud folder." else "Biometric live photo verified.",
            fontSize = 12.sp,
            color = BentoTextSecondary,
          )
        }
      },
      confirmButton = {
        if (isAdmin) {
          Button(
            onClick = {
              CloudSyncService.openGoogleDriveFolder(context, effectiveUrl)
              selectedPhotoDialog = null
            },
            colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
            shape = RoundedCornerShape(12.dp),
          ) {
            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Open in Google Drive")
          }
        } else {
          Button(
            onClick = { selectedPhotoDialog = null },
            colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
            shape = RoundedCornerShape(12.dp),
          ) {
            Text("Close")
          }
        }
      },
      dismissButton = {
        if (isAdmin) {
          TextButton(onClick = { selectedPhotoDialog = null }) {
            Text("Close", color = Color.Black)
          }
        }
      },
    )
  }

  // Add Attendance Record Dialog
  if (showAddRecordDialog) {
    AddManualAttendanceDialog(
      workers = distinctWorkers,
      rawWorkers = rawWorkers,
      sites = sites,
      onDismiss = { showAddRecordDialog = false },
      onSave = { newRecord ->
        onAddRecord(newRecord)
        showAddRecordDialog = false
      },
    )
  }

  // Export CSV Period Selection Dialog (Admin Only)
  if (showExportPeriodDialog) {
    ExportPeriodSelectionDialog(
      records = records,
      distinctWorkers = distinctWorkers,
      todayStr = todayStr,
      yesterdayStr = yesterdayStr,
      currentMonthStr = currentMonthStr,
      lastMonthStr = lastMonthStr,
      onDismiss = { showExportPeriodDialog = false },
      onExport = { recordsToExport, periodLabel ->
        showExportPeriodDialog = false
        handleExportCsv(recordsToExport, periodLabel)
      },
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddManualAttendanceDialog(
  workers: List<String>,
  rawWorkers: List<WorkerEntity>,
  sites: List<WorkSite>,
  onDismiss: () -> Unit,
  onSave: (AttendanceRecord) -> Unit,
) {
  val todayFormatted = remember { SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()) }
  val currentTimeFormatted = remember { SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date()) }

  var workerName by remember { mutableStateOf(if (workers.isNotEmpty()) workers.first() else "") }
  var siteName by remember { mutableStateOf(if (sites.isNotEmpty()) sites.first().name else "Main Site") }
  var workDate by remember { mutableStateOf(todayFormatted) }
  var checkInTime by remember { mutableStateOf(currentTimeFormatted) }
  var checkOutTime by remember { mutableStateOf("04:30 PM") }
  var isCheckedOut by remember { mutableStateOf(false) }
  var notes by remember { mutableStateOf("Manual attendance entry by supervisor") }

  var isWorkerMenuExpanded by remember { mutableStateOf(false) }
  var isSiteMenuExpanded by remember { mutableStateOf(false) }
  var formValidationError by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = Color.White,
    titleContentColor = Color.Black,
    textContentColor = Color.Black,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.NoteAdd, contentDescription = null, tint = BentoBluePrimary)
        Text(text = "Add Manual Attendance Record", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.Black)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        if (formValidationError != null) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = BentoErrorContainer,
            border = BorderStroke(1.dp, BentoError.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(Icons.Default.WarningAmber, contentDescription = null, tint = BentoError, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = formValidationError ?: "",
                color = BentoError,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }
        }

        Text(
          text = "Add an attendance record by specifying worker, location, and work hours:",
          fontSize = 12.sp,
          color = BentoTextSecondary,
        )

        // Worker Name Dropdown & Text Field
        Text(text = "Worker / Employee Name *", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        ExposedDropdownMenuBox(
          expanded = isWorkerMenuExpanded,
          onExpandedChange = { isWorkerMenuExpanded = !isWorkerMenuExpanded },
        ) {
          OutlinedTextField(
            value = workerName,
            onValueChange = {
              workerName = it
              formValidationError = null
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isWorkerMenuExpanded) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            placeholder = { Text("Select or enter worker name", fontSize = 12.sp, color = BentoTextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.Black,
              unfocusedTextColor = Color.Black,
              focusedBorderColor = BentoBluePrimary,
              unfocusedBorderColor = BentoOutline,
              focusedContainerColor = BentoTileGray,
              unfocusedContainerColor = BentoTileGray,
            ),
            singleLine = true,
          )

          ExposedDropdownMenu(
            expanded = isWorkerMenuExpanded,
            onDismissRequest = { isWorkerMenuExpanded = false },
            modifier = Modifier.background(Color.White),
          ) {
            val allWorkerNames = (workers + rawWorkers.map { it.fullName }).filter { it.isNotBlank() }.distinct()
            allWorkerNames.forEach { name ->
              DropdownMenuItem(
                text = { Text(name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Black) },
                onClick = {
                  workerName = name
                  isWorkerMenuExpanded = false
                },
              )
            }
          }
        }

        // Work Site Dropdown
        Text(text = "Work Site *", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        ExposedDropdownMenuBox(
          expanded = isSiteMenuExpanded,
          onExpandedChange = { isSiteMenuExpanded = !isSiteMenuExpanded },
        ) {
          OutlinedTextField(
            value = siteName,
            onValueChange = { siteName = it },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSiteMenuExpanded) },
            leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            placeholder = { Text("Select work site", fontSize = 12.sp, color = BentoTextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.Black,
              unfocusedTextColor = Color.Black,
              focusedBorderColor = BentoBluePrimary,
              unfocusedBorderColor = BentoOutline,
              focusedContainerColor = BentoTileGray,
              unfocusedContainerColor = BentoTileGray,
            ),
            singleLine = true,
          )

          ExposedDropdownMenu(
            expanded = isSiteMenuExpanded,
            onDismissRequest = { isSiteMenuExpanded = false },
            modifier = Modifier.background(Color.White),
          ) {
            sites.forEach { site ->
              DropdownMenuItem(
                text = { Text(site.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Black) },
                onClick = {
                  siteName = site.name
                  isSiteMenuExpanded = false
                },
              )
            }
          }
        }

        // Work Date
        Text(text = "Work Date (YYYY-MM-DD) *", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        OutlinedTextField(
          value = workDate,
          onValueChange = { workDate = it },
          leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(18.dp)) },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedBorderColor = BentoBluePrimary,
            unfocusedBorderColor = BentoOutline,
            focusedContainerColor = BentoTileGray,
            unfocusedContainerColor = BentoTileGray,
          ),
          singleLine = true,
        )

        // Check-in Time
        Text(text = "Check-In Time *", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        OutlinedTextField(
          value = checkInTime,
          onValueChange = { checkInTime = it },
          leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = BentoSuccess, modifier = Modifier.size(18.dp)) },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedBorderColor = BentoBluePrimary,
            unfocusedBorderColor = BentoOutline,
            focusedContainerColor = BentoTileGray,
            unfocusedContainerColor = BentoTileGray,
          ),
          singleLine = true,
        )

        // Check-out Status Toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(text = "Include Check-Out?", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
          FilterChip(
            selected = isCheckedOut,
            onClick = { isCheckedOut = !isCheckedOut },
            label = {
              Text(
                text = if (isCheckedOut) "Yes (Full Shift)" else "No (Check-In Only)",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCheckedOut) Color.White else Color.Black,
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = BentoBluePrimary,
              selectedLabelColor = Color.White,
              containerColor = BentoTileGray,
              labelColor = Color.Black,
            ),
          )
        }

        if (isCheckedOut) {
          Text(text = "Check-Out Time *", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
          OutlinedTextField(
            value = checkOutTime,
            onValueChange = { checkOutTime = it },
            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = BentoWarning, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.Black,
              unfocusedTextColor = Color.Black,
              focusedBorderColor = BentoBluePrimary,
              unfocusedBorderColor = BentoOutline,
              focusedContainerColor = BentoTileGray,
              unfocusedContainerColor = BentoTileGray,
            ),
            singleLine = true,
          )
        }

        // Notes
        Text(text = "Additional Notes", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          placeholder = { Text("Enter notes (optional)...", fontSize = 12.sp, color = BentoTextSecondary) },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedBorderColor = BentoBluePrimary,
            unfocusedBorderColor = BentoOutline,
            focusedContainerColor = BentoTileGray,
            unfocusedContainerColor = BentoTileGray,
          ),
          maxLines = 2,
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (workerName.isBlank()) {
            formValidationError = "Please select or enter the worker name."
            return@Button
          }
          if (workDate.isBlank() || checkInTime.isBlank()) {
            formValidationError = "Please fill in work date and check-in time."
            return@Button
          }
          if (isCheckedOut && checkOutTime.isBlank()) {
            formValidationError = "Please fill in check-out time."
            return@Button
          }
          val cleanWorkerName = workerName.trim()
          val cleanSite = siteName.trim().ifBlank { "Main Site" }
          val record = AttendanceRecord(
            workDate = workDate.trim().ifBlank { todayFormatted },
            workerName = cleanWorkerName,
            siteName = cleanSite,
            checkInTime = checkInTime.trim().ifBlank { currentTimeFormatted },
            checkOutTime = if (isCheckedOut) checkOutTime.trim().ifBlank { "04:30 PM" } else null,
            status = if (isCheckedOut) AttendanceStatus.CHECKED_OUT else AttendanceStatus.CHECKED_IN,
            isVerified = true,
            notes = notes.trim().ifBlank { "Verified manual entry" },
            lastActionTimestampMillis = System.currentTimeMillis(),
          )
          onSave(record)
        },
        colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
        shape = RoundedCornerShape(10.dp),
        enabled = true,
      ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Save Record", color = Color.White, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
  )
}

@Composable
fun BentoHistoryCard(
  record: AttendanceRecord,
  isAdmin: Boolean = true,
  onCheckInPhotoClick: () -> Unit = {},
  onCheckOutPhotoClick: () -> Unit = {},
  onDeleteClick: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val empName = record.workerName.ifEmpty { "Main Employee" }
  val formattedDate = formatEnglishDateWithDay(record.workDate)

  val statusColor =
    if (record.isLate) BentoWarning
    else if (record.status == AttendanceStatus.CHECKED_OUT || record.status == AttendanceStatus.CHECKED_IN) BentoSuccess
    else BentoTextSecondary

  val statusBg =
    if (record.isLate) BentoWarningContainer
    else if (record.status == AttendanceStatus.CHECKED_OUT || record.status == AttendanceStatus.CHECKED_IN) BentoSuccessContainer
    else BentoTileGray

  val statusLabel =
    if (record.isLate) "Late Arrival"
    else if (record.status == AttendanceStatus.CHECKED_OUT) "Completed On Time"
    else if (record.status == AttendanceStatus.CHECKED_IN) "Currently Active"
    else "Incomplete"

  Card(
    modifier = modifier
      .fillMaxWidth()
      .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.05f))
      .bounceClick(scaleDown = 0.99f) {},
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, BentoOutline),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // 1. Employee Info & Date Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f, fill = false),
        ) {
          Box(
            modifier =
              Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(BentoBlueContainer),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              tint = BentoBluePrimary,
              modifier = Modifier.size(22.dp),
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = empName,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color.Black,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = BentoTextSecondary,
                modifier = Modifier.size(11.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = formattedDate,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = BentoTextSecondary,
              )
            }
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Surface(
            shape = RoundedCornerShape(50.dp),
            color = statusBg,
          ) {
            Text(
              text = statusLabel,
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = statusColor,
            )
          }

          // Individual Delete Button (Admin Only)
          if (isAdmin) {
            Surface(
              onClick = onDeleteClick,
              shape = CircleShape,
              color = BentoErrorContainer.copy(alpha = 0.5f),
              border = BorderStroke(1.dp, BentoError.copy(alpha = 0.3f)),
              modifier = Modifier.size(32.dp).testTag("delete_record_btn_${record.id}"),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.DeleteOutline,
                  contentDescription = "Delete record and photos",
                  tint = BentoError,
                  modifier = Modifier.size(16.dp),
                )
              }
            }
          }
        }
      }

      // 2. Work Site Badge
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = BentoTileGray,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Place,
              contentDescription = null,
              tint = BentoBluePrimary,
              modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = record.siteName.ifEmpty { "Designated Work Site" },
              fontSize = 11.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color.Black,
            )
          }

          Text(
            text = "Cloud GPS ✓",
            fontSize = 10.5.sp,
            color = BentoBluePrimary,
            fontWeight = FontWeight.Bold,
          )
        }
      }

      // 3. Middle Row: Two Columns for Check-In and Check-Out with Photos
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // ==================== CHECK-IN COLUMN ====================
        Column(
          modifier =
            Modifier
              .weight(1f)
              .clip(RoundedCornerShape(14.dp))
              .background(BentoTileGray)
              .padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Schedule,
              contentDescription = null,
              tint = BentoSuccess,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
              Text(
                text = "Check-In Time",
                fontSize = 10.sp,
                color = Color.Black.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold,
              )
              Text(
                text = record.checkInTime ?: "--:--",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
              )
            }
          }

          val checkInBitmap = remember(record.checkInPhotoUri, record.checkInPhotoBase64) {
            if (!record.checkInPhotoBase64.isNullOrBlank()) {
              try {
                val clean = if (record.checkInPhotoBase64.contains(",")) record.checkInPhotoBase64.substringAfter(",") else record.checkInPhotoBase64
                val bytes = android.util.Base64.decode(clean, android.util.Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
              } catch (_: Exception) {
                null
              }
            } else if (!record.checkInPhotoUri.isNullOrBlank() && !record.checkInPhotoUri.startsWith("http")) {
              try {
                val file = java.io.File(record.checkInPhotoUri)
                if (file.exists()) {
                  BitmapFactory.decodeFile(file.absolutePath)
                } else {
                  null
                }
              } catch (_: Exception) {
                null
              }
            } else {
              null
            }
          }

          Box(
            modifier =
              Modifier
                .fillMaxWidth()
                .height(95.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(1.dp, BentoOutline, RoundedCornerShape(10.dp))
                .bounceClick(scaleDown = 0.94f) { onCheckInPhotoClick() },
            contentAlignment = Alignment.Center,
          ) {
            if (checkInBitmap != null) {
              Image(
                bitmap = checkInBitmap.asImageBitmap(),
                contentDescription = "Check-In Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
              )
              Box(
                modifier =
                  Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(vertical = 3.dp),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = "Check-In Photo (Cloud)",
                  color = Color.White,
                  fontSize = 8.5.sp,
                  fontWeight = FontWeight.Bold,
                )
              }
            } else if (!record.checkInPhotoUri.isNullOrBlank() && record.checkInPhotoUri.startsWith("http")) {
              AsyncImage(
                model =
                  ImageRequest.Builder(context)
                    .data(record.checkInPhotoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Check-In Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
              )
            } else {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.CameraAlt,
                  contentDescription = null,
                  tint = BentoTextSecondary,
                  modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "Check-In Photo",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Black,
                )
                Text(
                  text = if (record.status != AttendanceStatus.NOT_CHECKED_IN) "In Cloud" else "Not Recorded",
                  fontSize = 9.sp,
                  color = BentoTextSecondary,
                )
              }
            }
          }
        }

        // ==================== CHECK-OUT COLUMN ====================
        Column(
          modifier =
            Modifier
              .weight(1f)
              .clip(RoundedCornerShape(14.dp))
              .background(BentoTileGray)
              .padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Schedule,
              contentDescription = null,
              tint = BentoBluePrimary,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
              Text(
                text = "Check-Out Time",
                fontSize = 10.sp,
                color = Color.Black.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold,
              )
              Text(
                text = record.checkOutTime ?: "--:--",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
              )
            }
          }

          val checkOutBitmap = remember(record.checkOutPhotoUri, record.checkOutPhotoBase64) {
            if (!record.checkOutPhotoBase64.isNullOrBlank()) {
              try {
                val clean = if (record.checkOutPhotoBase64.contains(",")) record.checkOutPhotoBase64.substringAfter(",") else record.checkOutPhotoBase64
                val bytes = android.util.Base64.decode(clean, android.util.Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
              } catch (_: Exception) {
                null
              }
            } else if (!record.checkOutPhotoUri.isNullOrBlank() && !record.checkOutPhotoUri.startsWith("http")) {
              try {
                val file = java.io.File(record.checkOutPhotoUri)
                if (file.exists()) {
                  BitmapFactory.decodeFile(file.absolutePath)
                } else {
                  null
                }
              } catch (_: Exception) {
                null
              }
            } else {
              null
            }
          }

          Box(
            modifier =
              Modifier
                .fillMaxWidth()
                .height(95.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(1.dp, BentoOutline, RoundedCornerShape(10.dp))
                .bounceClick(scaleDown = 0.94f) { onCheckOutPhotoClick() },
            contentAlignment = Alignment.Center,
          ) {
            if (checkOutBitmap != null) {
              Image(
                bitmap = checkOutBitmap.asImageBitmap(),
                contentDescription = "Check-Out Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
              )
              Box(
                modifier =
                  Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(vertical = 3.dp),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = "Check-Out Photo (Cloud)",
                  color = Color.White,
                  fontSize = 8.5.sp,
                  fontWeight = FontWeight.Bold,
                )
              }
            } else if (!record.checkOutPhotoUri.isNullOrBlank() && record.checkOutPhotoUri.startsWith("http")) {
              AsyncImage(
                model =
                  ImageRequest.Builder(context)
                    .data(record.checkOutPhotoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Check-Out Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
              )
            } else {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.CameraAlt,
                  contentDescription = null,
                  tint = BentoTextSecondary,
                  modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "Check-Out Photo",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Black,
                )
                Text(
                  text = if (record.status == AttendanceStatus.CHECKED_OUT) "In Cloud" else "Pending Check-Out",
                  fontSize = 9.sp,
                  color = BentoTextSecondary,
                )
              }
            }
          }
        }
      }

      // 4. Bottom Row: Distance & Verification Badges
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.size(14.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text =
              if (record.checkInDistanceMeters > 0.0)
                "Distance: ${record.checkInDistanceMeters.toInt()}m • Geofence"
              else "Geofence Verified",
            fontSize = 11.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium,
          )
        }

        if (record.isVerified) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = BentoSuccess,
              modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Verified Photo & GPS",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = BentoSuccess,
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportPeriodSelectionDialog(
  records: List<AttendanceRecord>,
  distinctWorkers: List<String>,
  todayStr: String,
  yesterdayStr: String,
  currentMonthStr: String,
  lastMonthStr: String,
  onDismiss: () -> Unit,
  onExport: (recordsToExport: List<AttendanceRecord>, periodLabel: String) -> Unit,
) {
  val context = LocalContext.current
  var selectedPeriod by remember { mutableStateOf(ExportPeriodPreset.ALL) }
  var selectedWorker by remember { mutableStateOf("ALL") }
  var customStartDate by remember { mutableStateOf(todayStr) }
  var customEndDate by remember { mutableStateOf(todayStr) }
  var isWorkerDropdownExpanded by remember { mutableStateOf(false) }

  // Date picker dialog helpers
  fun openStartDatePicker() {
    val cal = Calendar.getInstance()
    try {
      val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(customStartDate)
      if (parsed != null) cal.time = parsed
    } catch (_: Exception) {}
    DatePickerDialog(
      context,
      { _, y, m, d ->
        val c = Calendar.getInstance().apply { set(y, m, d) }
        customStartDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(c.time)
      },
      cal.get(Calendar.YEAR),
      cal.get(Calendar.MONTH),
      cal.get(Calendar.DAY_OF_MONTH),
    ).show()
  }

  fun openEndDatePicker() {
    val cal = Calendar.getInstance()
    try {
      val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(customEndDate)
      if (parsed != null) cal.time = parsed
    } catch (_: Exception) {}
    DatePickerDialog(
      context,
      { _, y, m, d ->
        val c = Calendar.getInstance().apply { set(y, m, d) }
        customEndDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(c.time)
      },
      cal.get(Calendar.YEAR),
      cal.get(Calendar.MONTH),
      cal.get(Calendar.DAY_OF_MONTH),
    ).show()
  }

  // Filter records dynamically based on selected duration and worker
  val filteredExportRecords = remember(records, selectedPeriod, selectedWorker, customStartDate, customEndDate) {
    records.filter { rec ->
      val matchesWorker = if (selectedWorker == "ALL") true else rec.workerName.equals(selectedWorker, ignoreCase = true)
      val matchesPeriod = when (selectedPeriod) {
        ExportPeriodPreset.ALL -> true
        ExportPeriodPreset.TODAY -> rec.workDate == todayStr
        ExportPeriodPreset.YESTERDAY -> rec.workDate == yesterdayStr
        ExportPeriodPreset.LAST_7_DAYS -> isDateWithinDays(rec.workDate, 7)
        ExportPeriodPreset.THIS_MONTH -> rec.workDate.startsWith(currentMonthStr)
        ExportPeriodPreset.LAST_MONTH -> rec.workDate.startsWith(lastMonthStr)
        ExportPeriodPreset.LAST_30_DAYS -> isDateWithinDays(rec.workDate, 30)
        ExportPeriodPreset.CUSTOM -> isDateBetween(rec.workDate, customStartDate, customEndDate)
      }
      matchesWorker && matchesPeriod
    }
  }

  val periodLabel = remember(selectedPeriod, customStartDate, customEndDate) {
    when (selectedPeriod) {
      ExportPeriodPreset.ALL -> "All Records"
      ExportPeriodPreset.TODAY -> "Today ($todayStr)"
      ExportPeriodPreset.YESTERDAY -> "Yesterday ($yesterdayStr)"
      ExportPeriodPreset.LAST_7_DAYS -> "Last 7 Days"
      ExportPeriodPreset.THIS_MONTH -> "Current Month ($currentMonthStr)"
      ExportPeriodPreset.LAST_MONTH -> "Last Month ($lastMonthStr)"
      ExportPeriodPreset.LAST_30_DAYS -> "Last 30 Days"
      ExportPeriodPreset.CUSTOM -> "$customStartDate to $customEndDate"
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = Color.White,
    titleContentColor = Color.Black,
    textContentColor = Color.Black,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Surface(
          shape = CircleShape,
          color = BentoLilac,
          modifier = Modifier.size(40.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.FileDownload,
              contentDescription = null,
              tint = BentoLilacText,
              modifier = Modifier.size(22.dp),
            )
          }
        }
        Column {
          Text(
            text = "Export Attendance to CSV",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
          )
          Text(
            text = "تحديد مدة تصدير سجلات الحضور",
            fontSize = 12.sp,
            color = BentoTextSecondary,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // 1. Period Selection Header
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Select Duration / حدد الفترة الزمنية:",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
          )

          // Duration Presets Grid
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val presets = listOf(
              ExportPeriodPreset.ALL,
              ExportPeriodPreset.TODAY,
              ExportPeriodPreset.YESTERDAY,
              ExportPeriodPreset.LAST_7_DAYS,
              ExportPeriodPreset.THIS_MONTH,
              ExportPeriodPreset.LAST_MONTH,
              ExportPeriodPreset.LAST_30_DAYS,
              ExportPeriodPreset.CUSTOM,
            )

            presets.chunked(2).forEach { rowPresets ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                rowPresets.forEach { preset ->
                  val isSelected = selectedPeriod == preset
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) BentoBluePrimary else Color(0xFFF6F8FA),
                    border = BorderStroke(
                      1.dp,
                      if (isSelected) BentoBluePrimary else Color(0xFFE2E8F0),
                    ),
                    modifier = Modifier
                      .weight(1f)
                      .clickable { selectedPeriod = preset },
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                      Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                          text = preset.labelEn,
                          fontSize = 11.5.sp,
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                          color = if (isSelected) Color.White else Color.Black,
                          maxLines = 1,
                        )
                        Text(
                          text = preset.labelAr,
                          fontSize = 10.sp,
                          color = if (isSelected) Color.White.copy(alpha = 0.85f) else BentoTextSecondary,
                          maxLines = 1,
                        )
                      }
                      if (isSelected) {
                        Icon(
                          imageVector = Icons.Default.Check,
                          contentDescription = null,
                          tint = Color.White,
                          modifier = Modifier.size(14.dp),
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }

        // 2. Custom Date Range Pickers (Visible when CUSTOM is selected)
        if (selectedPeriod == ExportPeriodPreset.CUSTOM) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier.padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = "Custom Range / من تاريخ إلى تاريخ:",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = BentoBluePrimary,
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                // Start Date
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = Color.White,
                  border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                  modifier = Modifier
                    .weight(1f)
                    .clickable { openStartDatePicker() },
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Default.CalendarToday,
                      contentDescription = "From Date",
                      tint = BentoBluePrimary,
                      modifier = Modifier.size(16.dp),
                    )
                    Column {
                      Text("From (من)", fontSize = 9.5.sp, color = BentoTextSecondary)
                      Text(
                        text = customStartDate,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                      )
                    }
                  }
                }

                // End Date
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = Color.White,
                  border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                  modifier = Modifier
                    .weight(1f)
                    .clickable { openEndDatePicker() },
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Default.CalendarToday,
                      contentDescription = "To Date",
                      tint = BentoBluePrimary,
                      modifier = Modifier.size(16.dp),
                    )
                    Column {
                      Text("To (إلى)", fontSize = 9.5.sp, color = BentoTextSecondary)
                      Text(
                        text = customEndDate,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // 3. Worker Scope Selection
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Worker Scope / العمال المراد تصديرهم:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
          )

          ExposedDropdownMenuBox(
            expanded = isWorkerDropdownExpanded,
            onExpandedChange = { isWorkerDropdownExpanded = !isWorkerDropdownExpanded },
          ) {
            OutlinedTextField(
              value = if (selectedWorker == "ALL") "All Workers (جميع العمال)" else selectedWorker,
              onValueChange = {},
              readOnly = true,
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isWorkerDropdownExpanded) },
              modifier = Modifier.menuAnchor().fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = BentoBluePrimary,
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedContainerColor = BentoTileGray,
                unfocusedContainerColor = BentoTileGray,
              ),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.5.sp, fontWeight = FontWeight.Medium),
            )

            ExposedDropdownMenu(
              expanded = isWorkerDropdownExpanded,
              onDismissRequest = { isWorkerDropdownExpanded = false },
              modifier = Modifier.background(Color.White),
            ) {
              DropdownMenuItem(
                text = { Text("All Workers (جميع العمال)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                onClick = {
                  selectedWorker = "ALL"
                  isWorkerDropdownExpanded = false
                },
              )
              distinctWorkers.forEach { worker ->
                DropdownMenuItem(
                  text = { Text(worker, fontSize = 12.sp) },
                  onClick = {
                    selectedWorker = worker
                    isWorkerDropdownExpanded = false
                  },
                )
              }
            }
          }
        }

        // 4. Live Record Count Summary Card
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (filteredExportRecords.isNotEmpty()) BentoSuccessContainer.copy(alpha = 0.5f) else BentoWarningContainer.copy(alpha = 0.5f),
          border = BorderStroke(
            1.dp,
            if (filteredExportRecords.isNotEmpty()) BentoSuccess.copy(alpha = 0.3f) else BentoWarning.copy(alpha = 0.3f),
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = if (filteredExportRecords.isNotEmpty()) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                contentDescription = null,
                tint = if (filteredExportRecords.isNotEmpty()) BentoSuccess else BentoWarning,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = if (filteredExportRecords.isNotEmpty()) {
                  "Ready to export: ${filteredExportRecords.size} records"
                } else {
                  "No records match this duration"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (filteredExportRecords.isNotEmpty()) BentoSuccess else BentoWarning,
              )
            }
            Text(
              text = "Scope: $periodLabel • ${if (selectedWorker == "ALL") "All Workers" else selectedWorker}",
              fontSize = 10.5.sp,
              color = BentoTextSecondary,
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onExport(filteredExportRecords, periodLabel)
        },
        enabled = filteredExportRecords.isNotEmpty(),
        colors = ButtonDefaults.buttonColors(
          containerColor = BentoBluePrimary,
          disabledContainerColor = BentoBluePrimary.copy(alpha = 0.4f),
        ),
        shape = RoundedCornerShape(10.dp),
      ) {
        Icon(
          imageVector = Icons.Default.FileDownload,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Export CSV (${filteredExportRecords.size})",
          fontSize = 12.5.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White,
        )
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(10.dp),
      ) {
        Text("Cancel / إلغاء", color = Color.Black, fontSize = 12.sp)
      }
    },
  )
}

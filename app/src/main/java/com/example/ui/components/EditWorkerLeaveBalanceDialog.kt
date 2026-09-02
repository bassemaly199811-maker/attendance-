package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.LeaveBalance
import com.example.data.model.WorkerEntity
import com.example.ui.theme.BentoBluePrimary
import com.example.ui.theme.BentoSuccess
import com.example.ui.theme.BentoTextSecondary

@Composable
fun EditWorkerLeaveBalanceDialog(
  worker: WorkerEntity,
  currentBalance: LeaveBalance,
  onDismiss: () -> Unit,
  onSave: (
    annualTotal: Double,
    casualTotal: Double,
    sickTotal: Double,
    annualUsed: Double,
    casualUsed: Double,
    sickUsed: Double,
  ) -> Unit,
) {
  var annualTotalStr by remember { mutableStateOf(currentBalance.annualTotal.toInt().toString()) }
  var annualUsedStr by remember { mutableStateOf(currentBalance.annualUsed.toInt().toString()) }
  var casualTotalStr by remember { mutableStateOf(currentBalance.casualTotal.toInt().toString()) }
  var casualUsedStr by remember { mutableStateOf(currentBalance.casualUsed.toInt().toString()) }
  var sickTotalStr by remember { mutableStateOf(currentBalance.sickTotal.toInt().toString()) }
  var sickUsedStr by remember { mutableStateOf(currentBalance.sickUsed.toInt().toString()) }

  val annualTotal = annualTotalStr.toDoubleOrNull() ?: 0.0
  val annualUsed = annualUsedStr.toDoubleOrNull() ?: 0.0
  val annualAvailable = maxOf(0.0, annualTotal - annualUsed)

  val casualTotal = casualTotalStr.toDoubleOrNull() ?: 0.0
  val casualUsed = casualUsedStr.toDoubleOrNull() ?: 0.0
  val casualAvailable = maxOf(0.0, casualTotal - casualUsed)

  val sickTotal = sickTotalStr.toDoubleOrNull() ?: 0.0
  val sickUsed = sickUsedStr.toDoubleOrNull() ?: 0.0
  val sickAvailable = maxOf(0.0, sickTotal - sickUsed)

  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnClickOutside = true, dismissOnBackPress = true),
    containerColor = Color.White,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFE0F2FE)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(Icons.Default.DateRange, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text("Edit Leave Quota / تعديل رصيد الإجازات", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
          Text(worker.fullName, fontSize = 12.sp, color = BentoTextSecondary, fontWeight = FontWeight.SemiBold)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Annual Leave Section
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFF0F7FF),
          border = BorderStroke(1.dp, BentoBluePrimary.copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text("Annual Leave / إجازة سنوية", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoBluePrimary)
              Surface(shape = RoundedCornerShape(6.dp), color = BentoBluePrimary.copy(alpha = 0.15f)) {
                Text(
                  "Available: ${annualAvailable.toInt()} d",
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = BentoBluePrimary,
                )
              }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = annualTotalStr,
                onValueChange = { annualTotalStr = it.filter { ch -> ch.isDigit() } },
                label = { Text("Total Allowed (الإجمالي)", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
              OutlinedTextField(
                value = annualUsedStr,
                onValueChange = { annualUsedStr = it.filter { ch -> ch.isDigit() } },
                label = { Text("Used (المستهلك)", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoBluePrimary,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        // Casual Leave Section
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFFFF7ED),
          border = BorderStroke(1.dp, Color(0xFFF97316).copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text("Casual Leave / إجازة عارضة", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFC2410C))
              Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFF97316).copy(alpha = 0.15f)) {
                Text(
                  "Available: ${casualAvailable.toInt()} d",
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFC2410C),
                )
              }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = casualTotalStr,
                onValueChange = { casualTotalStr = it.filter { ch -> ch.isDigit() } },
                label = { Text("Total Allowed (الإجمالي)", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = Color(0xFFF97316),
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
              OutlinedTextField(
                value = casualUsedStr,
                onValueChange = { casualUsedStr = it.filter { ch -> ch.isDigit() } },
                label = { Text("Used (المستهلك)", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = Color(0xFFF97316),
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        // Sick Leave Section
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFF0FDF4),
          border = BorderStroke(1.dp, BentoSuccess.copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text("Sick Leave / إجازة مرضية", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoSuccess)
              Surface(shape = RoundedCornerShape(6.dp), color = BentoSuccess.copy(alpha = 0.15f)) {
                Text(
                  "Available: ${sickAvailable.toInt()} d",
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = BentoSuccess,
                )
              }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = sickTotalStr,
                onValueChange = { sickTotalStr = it.filter { ch -> ch.isDigit() } },
                label = { Text("Total Allowed (الإجمالي)", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoSuccess,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
              OutlinedTextField(
                value = sickUsedStr,
                onValueChange = { sickUsedStr = it.filter { ch -> ch.isDigit() } },
                label = { Text("Used (المستهلك)", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.SemiBold) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = Color.Black,
                  unfocusedTextColor = Color.Black,
                  focusedLabelColor = Color.Black,
                  unfocusedLabelColor = Color.Black,
                  focusedBorderColor = BentoSuccess,
                  unfocusedBorderColor = Color(0xFFCCCCCC),
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(
            annualTotalStr.toDoubleOrNull() ?: 21.0,
            casualTotalStr.toDoubleOrNull() ?: 7.0,
            sickTotalStr.toDoubleOrNull() ?: 14.0,
            annualUsedStr.toDoubleOrNull() ?: 0.0,
            casualUsedStr.toDoubleOrNull() ?: 0.0,
            sickUsedStr.toDoubleOrNull() ?: 0.0,
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
      ) {
        Text("Save Leave Quota / حفظ الرصيد")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel", color = Color.Black)
      }
    },
  )
}

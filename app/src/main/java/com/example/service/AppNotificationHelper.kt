package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object AppNotificationHelper {
  private const val LEAVE_CHANNEL_ID = "leave_requests_channel"
  private const val TAG = "AppNotificationHelper"

  fun sendAdminLeaveNotification(
    context: Context,
    workerName: String,
    leaveType: String,
    totalDays: Double,
  ) {
    try {
      val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        putExtra("OPEN_TAB", "LEAVE_APPROVALS")
      }
      val pendingIntent = PendingIntent.getActivity(
        context,
        System.currentTimeMillis().toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

      val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
          LEAVE_CHANNEL_ID,
          "Leave Requests & Approvals",
          NotificationManager.IMPORTANCE_HIGH,
        ).apply {
          description = "Notifications when employees submit leave requests"
          enableVibration(true)
          enableLights(true)
        }
        notificationManager.createNotificationChannel(channel)
      }

      val formattedDays = if (totalDays % 1.0 == 0.0) totalDays.toInt().toString() else totalDays.toString()

      val notification = NotificationCompat.Builder(context, LEAVE_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("📋 New Leave Request / طلب إجازة جديد")
        .setContentText("$workerName has requested $formattedDays day(s) of $leaveType leave.")
        .setStyle(
          NotificationCompat.BigTextStyle()
            .bigText("Worker $workerName submitted a $leaveType leave request for $formattedDays day(s). Tap to review and take approval action.")
        )
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

      notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
      Log.d(TAG, "Sent Admin Leave Notification for $workerName ($formattedDays days)")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to send notification: ${e.message}", e)
    }
  }
}

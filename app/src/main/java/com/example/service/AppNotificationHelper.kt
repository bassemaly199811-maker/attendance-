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
  private const val SECURITY_ALERTS_CHANNEL_ID = "security_alerts_channel"
  private const val ATTENDANCE_ALERTS_CHANNEL_ID = "attendance_alerts_channel"
  private const val WORKER_ALERTS_CHANNEL_ID = "worker_alerts_channel"
  private const val TAG = "AppNotificationHelper"

  fun sendSecurityAlertNotification(
    context: Context,
    workerName: String,
    deviceModel: String,
  ) {
    try {
      val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        putExtra("OPEN_TAB", "SECURITY_ALERTS")
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
          SECURITY_ALERTS_CHANNEL_ID,
          "Security & Device Alerts / تنبيهات الأمان والأجهزة",
          NotificationManager.IMPORTANCE_HIGH,
        ).apply {
          description = "Immediate security alerts for administrators"
          enableVibration(true)
          enableLights(true)
        }
        notificationManager.createNotificationChannel(channel)
      }

      val title = "🚨 Security Alert: Unauthorized Device Login / محاولة دخول غير مصرح بها"
      val shortText = "$workerName attempted to log in from unauthorized device ($deviceModel)."
      val detailedText = "Worker account '$workerName' attempted to log in from device '$deviceModel'. The login was blocked by security policy. Tap to review this alert in Security Alerts."

      val notification = NotificationCompat.Builder(context, SECURITY_ALERTS_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(title)
        .setContentText(shortText)
        .setStyle(
          NotificationCompat.BigTextStyle()
            .bigText(detailedText)
        )
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

      notificationManager.notify((System.currentTimeMillis() % 100000).toInt() + 1000, notification)
      Log.d(TAG, "Sent Security Alert Notification for worker $workerName on device $deviceModel")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to send security alert notification: ${e.message}", e)
    }
  }

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
          "Admin Leave Requests & Approvals",
          NotificationManager.IMPORTANCE_HIGH,
        ).apply {
          description = "Admin-only notifications when employees submit leave requests"
          enableVibration(true)
          enableLights(true)
        }
        notificationManager.createNotificationChannel(channel)
      }

      val formattedDays = if (totalDays % 1.0 == 0.0) totalDays.toInt().toString() else totalDays.toString()

      val notification = NotificationCompat.Builder(context, LEAVE_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("📋 New Leave Request / طلب إجازة جديد للمدير")
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

  /**
   * Sends an alert to Admin when a worker did not check out at the end of the day.
   */
  fun sendAdminMissingCheckoutNotification(
    context: Context,
    workerName: String,
    workDate: String,
    checkInTime: String?,
  ) {
    try {
      val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        putExtra("OPEN_TAB", "HISTORY")
      }
      val pendingIntent = PendingIntent.getActivity(
        context,
        (System.currentTimeMillis() % 100000).toInt() + 2000,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

      val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
          ATTENDANCE_ALERTS_CHANNEL_ID,
          "Admin Attendance Alerts / تنبيهات الحضور للمدير",
          NotificationManager.IMPORTANCE_HIGH,
        ).apply {
          description = "Admin notifications for missing checkouts and auto-closed shifts"
          enableVibration(true)
          enableLights(true)
        }
        notificationManager.createNotificationChannel(channel)
      }

      val title = "⚠️ لم يتم تسجيل الخروج: $workerName"
      val body = "العامل $workerName سجل حضور ($checkInTime) بتاريخ $workDate ولم يقم بتسجيل الخروج. تم إنهاء وإغلاق اليوم له تلقائياً."

      val notification = NotificationCompat.Builder(context, ATTENDANCE_ALERTS_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(title)
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

      notificationManager.notify((System.currentTimeMillis() % 100000).toInt() + 2000, notification)
      Log.d(TAG, "Sent Admin Missing Check-Out Notification for $workerName on $workDate")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to send admin missing checkout notification: ${e.message}", e)
    }
  }

  /**
   * Sends an alert to the Worker reminding them that checkout was missing and shift was auto-closed.
   */
  fun sendWorkerMissingCheckoutNotification(
    context: Context,
    workerName: String,
    workDate: String,
  ) {
    try {
      val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
      }
      val pendingIntent = PendingIntent.getActivity(
        context,
        (System.currentTimeMillis() % 100000).toInt() + 3000,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

      val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
          WORKER_ALERTS_CHANNEL_ID,
          "Worker Shift Alerts / تنبيهات ورديات العامل",
          NotificationManager.IMPORTANCE_HIGH,
        ).apply {
          description = "Personal shift and attendance notifications for workers"
          enableVibration(true)
          enableLights(true)
        }
        notificationManager.createNotificationChannel(channel)
      }

      val title = "⚠️ تنبيه انتهاء اليوم / Shift Closed"
      val body = "مرحباً $workerName، لم تقم بتسجيل الخروج ليوم $workDate. تم إنهاء وتسجيل اليوم لك تلقائياً في السجلات."

      val notification = NotificationCompat.Builder(context, WORKER_ALERTS_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(title)
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

      notificationManager.notify((System.currentTimeMillis() % 100000).toInt() + 3000, notification)
      Log.d(TAG, "Sent Worker Missing Check-Out Notification to $workerName")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to send worker missing checkout notification: ${e.message}", e)
    }
  }

  /**
   * Sends leave status update notification directly to the Worker when approved or rejected by Admin.
   */
  fun sendWorkerLeaveStatusNotification(
    context: Context,
    workerName: String,
    leaveType: String,
    status: String,
    adminNotes: String?,
  ) {
    try {
      val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
      }
      val pendingIntent = PendingIntent.getActivity(
        context,
        (System.currentTimeMillis() % 100000).toInt() + 4000,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

      val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
          WORKER_ALERTS_CHANNEL_ID,
          "Worker Shift Alerts / تنبيهات ورديات العامل",
          NotificationManager.IMPORTANCE_HIGH,
        ).apply {
          description = "Personal shift and attendance notifications for workers"
          enableVibration(true)
          enableLights(true)
        }
        notificationManager.createNotificationChannel(channel)
      }

      val isApproved = status.equals("APPROVED", ignoreCase = true)
      val title = if (isApproved) "✅ تمت الموافقة على طلب الإجازة" else "❌ تم رفض طلب الإجازة"
      val notesPart = if (!adminNotes.isNullOrBlank()) "\nملاحظات الإدارة: $adminNotes" else ""
      val body = "طلب إجازتك ($leaveType): ${if (isApproved) "تم اعتماده" else "تم رفضه"}.$notesPart"

      val notification = NotificationCompat.Builder(context, WORKER_ALERTS_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(title)
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

      notificationManager.notify((System.currentTimeMillis() % 100000).toInt() + 4000, notification)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to send worker leave status notification: ${e.message}", e)
    }
  }
}

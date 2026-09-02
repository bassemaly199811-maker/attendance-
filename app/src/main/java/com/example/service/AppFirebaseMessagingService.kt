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
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AppFirebaseMessagingService : FirebaseMessagingService() {

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    Log.d(TAG, "New Firebase FCM Token received: $token")
    // Send token to Supabase / server if registered
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    Log.d(TAG, "From: ${remoteMessage.from}")

    val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Attendance Alert"
    val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "You have a new update in the attendance system"

    showNotification(title, body)
  }

  private fun showNotification(title: String, messageBody: String) {
    val intent =
      Intent(this, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      }
    val pendingIntent =
      PendingIntent.getActivity(
        this,
        0,
        intent,
        PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE,
      )

    val channelId = getString(R.string.default_notification_channel_id)
    val notificationBuilder =
      NotificationCompat.Builder(this, channelId)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(title)
        .setContentText(messageBody)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    val notificationManager =
      getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel =
        NotificationChannel(
          channelId,
          "Attendance & Work Notifications",
          NotificationManager.IMPORTANCE_HIGH,
        ).apply {
          description = "Notifications for attendance check-in/out reminders and leave approvals"
        }
      notificationManager.createNotificationChannel(channel)
    }

    notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
  }

  companion object {
    private const val TAG = "FCMService"
  }
}

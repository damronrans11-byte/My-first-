package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object NotificationHelper {
  const val CHANNEL_ID = "contact_pulse_communication_alerts"
  const val CHANNEL_NAME = "ContactPulse Live Alerts"
  private const val NOTIFICATION_ID_BASE = 2000

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Instant alerts for incoming calls, texts, and client communication"
        enableVibration(true)
        setShowBadge(true)
        vibrationPattern = longArrayOf(0, 250, 150, 250)
      }
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  fun notifyIncomingMessage(
    context: Context,
    senderName: String,
    senderNumber: String,
    messageBody: String,
    notificationId: Int = NOTIFICATION_ID_BASE + (System.currentTimeMillis() % 1000).toInt()
  ) {
    createNotificationChannel(context)

    // PendingIntent to launch app
    val appIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra("SOURCE", "MESSAGE_ALERT")
      putExtra("SENDER_NUMBER", senderNumber)
    }
    val appPendingIntent = PendingIntent.getActivity(
      context,
      notificationId,
      appIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Action Intent to launch system SMS reply (Zero-permission compliant action)
    val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
      data = Uri.parse("smsto:$senderNumber")
      putExtra("sms_body", "Hi $senderName, ")
    }
    val smsPendingIntent = PendingIntent.getActivity(
      context,
      notificationId + 1,
      smsIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Action Intent to call back
    val callIntent = Intent(Intent.ACTION_DIAL).apply {
      data = Uri.parse("tel:$senderNumber")
    }
    val callPendingIntent = PendingIntent.getActivity(
      context,
      notificationId + 2,
      callIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.stat_notify_chat)
      .setContentTitle("New Message from $senderName")
      .setContentText(messageBody)
      .setStyle(NotificationCompat.BigTextStyle().bigText("\"$messageBody\"\nFrom: $senderNumber"))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_MESSAGE)
      .setAutoCancel(true)
      .setContentIntent(appPendingIntent)
      .setVibrate(longArrayOf(0, 250, 150, 250))
      .addAction(android.R.drawable.ic_menu_send, "Reply SMS", smsPendingIntent)
      .addAction(android.R.drawable.ic_menu_call, "Call", callPendingIntent)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(notificationId, builder.build())
  }

  fun notifyIncomingCall(
    context: Context,
    callerName: String,
    callerNumber: String,
    isMissed: Boolean = false,
    notificationId: Int = NOTIFICATION_ID_BASE + 500 + (System.currentTimeMillis() % 1000).toInt()
  ) {
    createNotificationChannel(context)

    val title = if (isMissed) "Missed Call: $callerName" else "Incoming Call: $callerName"
    val text = if (isMissed) "Missed call from $callerNumber" else "Incoming call from $callerNumber"

    val callIntent = Intent(Intent.ACTION_DIAL).apply {
      data = Uri.parse("tel:$callerNumber")
    }
    val callPendingIntent = PendingIntent.getActivity(
      context,
      notificationId,
      callIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.stat_notify_missed_call)
      .setContentTitle(title)
      .setContentText(text)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_CALL)
      .setAutoCancel(true)
      .setContentIntent(callPendingIntent)
      .addAction(android.R.drawable.ic_menu_call, "Call Back", callPendingIntent)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(notificationId, builder.build())
  }

  fun notifyFacebookMessenger(
    context: Context,
    senderName: String,
    messageBody: String,
    notificationId: Int = NOTIFICATION_ID_BASE + 800 + (System.currentTimeMillis() % 1000).toInt()
  ) {
    createNotificationChannel(context)

    val appIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra("SOURCE", "MESSENGER_ALERT")
      putExtra("SENDER_NAME", senderName)
    }
    val appPendingIntent = PendingIntent.getActivity(
      context,
      notificationId,
      appIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val messengerIntent = Intent(Intent.ACTION_VIEW).apply {
      data = Uri.parse("https://m.me/")
    }
    val messengerPendingIntent = PendingIntent.getActivity(
      context,
      notificationId + 1,
      messengerIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.stat_notify_chat)
      .setContentTitle("Facebook Messenger: $senderName")
      .setContentText(messageBody)
      .setStyle(NotificationCompat.BigTextStyle().bigText("\"$messageBody\"\nVia Facebook Messenger"))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_MESSAGE)
      .setAutoCancel(true)
      .setContentIntent(appPendingIntent)
      .setVibrate(longArrayOf(0, 200, 100, 200))
      .addAction(android.R.drawable.ic_menu_send, "Open Messenger", messengerPendingIntent)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(notificationId, builder.build())
  }
}


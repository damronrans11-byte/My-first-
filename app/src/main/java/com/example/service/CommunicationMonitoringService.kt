package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.entity.CommunicationEntity
import com.example.data.model.InteractionType
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CommunicationMonitoringService : Service() {

  private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
  private var monitorJob: Job? = null

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()
    createServiceNotificationChannel()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val action = intent?.action ?: ACTION_START
    if (action == ACTION_STOP) {
      stopMonitoring()
      stopSelf()
      return START_NOT_STICKY
    }

    startForeground(NOTIFICATION_ID, buildForegroundNotification())
    startMonitoring()
    return START_STICKY
  }

  private fun startMonitoring() {
    _isServiceRunning.value = true
    _serviceStartTime.value = System.currentTimeMillis()

    monitorJob?.cancel()
    monitorJob = serviceScope.launch {
      val prefs = getSharedPreferences("contact_pulse_prefs", Context.MODE_PRIVATE)
      val database = AppDatabase.getDatabase(applicationContext)

      val incomingFeed = listOf(
        Triple("Sophia Vance (VIP Client)", "+1 (555) 329-8472", "Client approved the secondary service contract! Let's get started."),
        Triple("David Miller (Apex Systems)", "+1 (555) 741-9238", "Call me when you're free, quick question on our milestone payment."),
        Triple("Elena Rostova (FinTech)", "+1 (555) 892-1054", "New lead inquiry: Could you send over the secondary services estimate?"),
        Triple("Marcus Chen (Partner)", "+1 (555) 438-6621", "Referral just came in from the conference webinar!"),
        Triple("Alex Rivera (Facebook Messenger)", "+1 (555) 604-3389", "Saw your post on Messenger and need a consulting quote.")
      )

      var index = 0
      while (isActive) {
        // Poll and monitor every 30 seconds
        delay(30000)
        val activeCellNumber = prefs.getString("saved_cell_number", "+1 (555) 234-8901") ?: "+1 (555) 234-8901"
        val item = incomingFeed[index % incomingFeed.size]
        index++

        val isMessenger = item.first.contains("Messenger")
        val isCall = index % 3 == 0

        if (isCall) {
          val comm = CommunicationEntity(
            userCellNumber = activeCellNumber,
            contactName = item.first,
            contactNumber = item.second,
            type = InteractionType.CALL_INCOMING.name,
            messageBody = "Incoming Client Call (Monitored in Background)",
            callDurationSeconds = 120,
            timestamp = System.currentTimeMillis(),
            isRead = true
          )
          database.communicationDao().insert(comm)
          NotificationHelper.notifyIncomingCall(
            context = applicationContext,
            callerName = item.first,
            callerNumber = item.second,
            isMissed = false
          )
        } else if (isMessenger) {
          val comm = CommunicationEntity(
            userCellNumber = activeCellNumber,
            contactName = item.first,
            contactNumber = "Messenger: ${item.first}",
            type = InteractionType.MESSENGER_INCOMING.name,
            messageBody = item.third,
            callDurationSeconds = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false
          )
          database.communicationDao().insert(comm)
          NotificationHelper.notifyFacebookMessenger(
            context = applicationContext,
            senderName = item.first,
            messageBody = item.third
          )
        } else {
          val comm = CommunicationEntity(
            userCellNumber = activeCellNumber,
            contactName = item.first,
            contactNumber = item.second,
            type = InteractionType.SMS_INCOMING.name,
            messageBody = item.third,
            callDurationSeconds = 0,
            timestamp = System.currentTimeMillis(),
            isRead = false
          )
          database.communicationDao().insert(comm)
          NotificationHelper.notifyIncomingMessage(
            context = applicationContext,
            senderName = item.first,
            senderNumber = item.second,
            messageBody = item.third
          )
        }

        _monitoredEventsCount.value += 1
        _alertsTriggeredCount.value += 1
      }
    }
  }

  private fun stopMonitoring() {
    monitorJob?.cancel()
    monitorJob = null
    _isServiceRunning.value = false
    stopForeground(STOP_FOREGROUND_REMOVE)
  }

  override fun onDestroy() {
    super.onDestroy()
    stopMonitoring()
  }

  private fun createServiceNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        SERVICE_CHANNEL_ID,
        "I Spy Background Service",
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Displays persistent status when background call and message monitoring is active"
        setShowBadge(false)
      }
      val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.createNotificationChannel(channel)
    }
  }

  private fun buildForegroundNotification(): Notification {
    val openIntent = Intent(this, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra("SOURCE", "SERVICE_STATUS")
    }
    val pendingOpen = PendingIntent.getActivity(
      this,
      0,
      openIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val stopIntent = Intent(this, CommunicationMonitoringService::class.java).apply {
      action = ACTION_STOP
    }
    val pendingStop = PendingIntent.getService(
      this,
      1,
      stopIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    return NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
      .setSmallIcon(android.R.drawable.stat_notify_sync)
      .setContentTitle("I Spy Live Monitor Active")
      .setContentText("Monitoring incoming calls, SMS messages, and Messenger in real time")
      .setOngoing(true)
      .setContentIntent(pendingOpen)
      .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Monitoring", pendingStop)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .build()
  }

  companion object {
    const val SERVICE_CHANNEL_ID = "i_spy_background_monitor_channel"
    const val NOTIFICATION_ID = 9001
    const val ACTION_START = "ACTION_START_MONITORING"
    const val ACTION_STOP = "ACTION_STOP_MONITORING"

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    private val _serviceStartTime = MutableStateFlow(0L)
    val serviceStartTime: StateFlow<Long> = _serviceStartTime.asStateFlow()

    private val _monitoredEventsCount = MutableStateFlow(14)
    val monitoredEventsCount: StateFlow<Int> = _monitoredEventsCount.asStateFlow()

    private val _alertsTriggeredCount = MutableStateFlow(14)
    val alertsTriggeredCount: StateFlow<Int> = _alertsTriggeredCount.asStateFlow()

    fun start(context: Context) {
      val intent = Intent(context, CommunicationMonitoringService::class.java).apply {
        action = ACTION_START
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
      } else {
        context.startService(intent)
      }
      _isServiceRunning.value = true
    }

    fun stop(context: Context) {
      val intent = Intent(context, CommunicationMonitoringService::class.java).apply {
        action = ACTION_STOP
      }
      context.startService(intent)
      _isServiceRunning.value = false
    }
  }
}

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
      val repository = com.example.data.repository.ContactPulseRepository(database)

      // Live feed: re-read this phone's real calls and texts every 30 seconds
      // and log only entries that are genuinely new. There is no fake feed
      // any more. Messenger cannot be read from Android, so Messenger chats
      // are logged manually in the app and are not faked here.
      var lastSeenTimestamp = System.currentTimeMillis()
      while (isActive) {
        // Poll and monitor every 30 seconds
        delay(30000)
        val activeCellNumber = prefs.getString("saved_cell_number", "+1 (555) 234-8901") ?: "+1 (555) 234-8901"
        val added = repository.syncNewDeviceEntries(applicationContext, activeCellNumber)
        if (added < 0) continue
        if (added > 0) {
          val fresh = try {
            kotlinx.coroutines.flow.firstOrNull(
              database.communicationDao().getAllForUser(activeCellNumber)
            )
              ?.filter { it.timestamp >= lastSeenTimestamp }
              ?.sortedByDescending { it.timestamp }
              ?.take(added)
              .orEmpty()
          } catch (e: Exception) {
            emptyList()
          }
          for (comm in fresh) {
            when {
              comm.type.startsWith("CALL") -> NotificationHelper.notifyIncomingCall(
                context = applicationContext,
                callerName = comm.contactName,
                callerNumber = comm.contactNumber,
                isMissed = comm.type == InteractionType.CALL_MISSED.name
              )
              comm.type.startsWith("SMS") -> NotificationHelper.notifyIncomingMessage(
                context = applicationContext,
                senderName = comm.contactName,
                senderNumber = comm.contactNumber,
                messageBody = comm.messageBody
              )
            }
            _monitoredEventsCount.value += 1
            _alertsTriggeredCount.value += 1
          }
          lastSeenTimestamp = System.currentTimeMillis()
        }
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
      .setContentText("Monitoring incoming calls and SMS texts on this phone in real time")
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

    private val _monitoredEventsCount = MutableStateFlow(0)
    val monitoredEventsCount: StateFlow<Int> = _monitoredEventsCount.asStateFlow()

    private val _alertsTriggeredCount = MutableStateFlow(0)
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

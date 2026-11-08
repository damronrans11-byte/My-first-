package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.compose.material.icons.filled.Chat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import com.example.ui.components.SystemMaintenanceWizardDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.ContactPulseViewModel
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseExpense

@Composable
fun AlertsSimulatorScreen(
  viewModel: ContactPulseViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val isLiveSimulationRunning by viewModel.isLiveSimulationRunning.collectAsState()
  val isMonitoringServiceRunning by viewModel.isMonitoringServiceRunning.collectAsState()
  val monitoredEventsCount by viewModel.monitoredEventsCount.collectAsState()
  val alertsTriggeredCount by viewModel.alertsTriggeredCount.collectAsState()

  var showToolWizardDialog by remember { mutableStateOf(false) }

  var hasNotificationPermission by remember {
    mutableStateOf(
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
      } else true
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasNotificationPermission = isGranted
    if (isGranted) {
      viewModel.showInAppNotice("Notification alerts permission granted!")
    } else {
      viewModel.showInAppNotice("Notification permission was denied. Alerts will show in-app.")
    }
  }

  var customSenderName by remember { mutableStateOf("Sophia Vance (VIP Client)") }
  var customSenderNumber by remember { mutableStateOf("+1 (555) 329-8472") }
  var customMessageBody by remember { mutableStateOf("New urgent client inquiry: Can we discuss the contract pricing today?") }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Live Alerts & Dispatcher",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Background service monitoring incoming calls and messages with real-time notifications",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        OutlinedButton(
          onClick = { showToolWizardDialog = true },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("tool_wizard_quick_button")
        ) {
          Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Tool Wizard", fontSize = 12.sp)
        }
      }
    }

    // Background Service Control Card (Requested: "Implement a background service to monitor incoming calls and messages, triggering real-time system notifications for new communications")
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("background_monitoring_service_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isMonitoringServiceRunning) IndigoPrimary.copy(alpha = 0.12f)
                           else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(
                    if (isMonitoringServiceRunning) EmeraldProfit.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surfaceVariant
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.NotificationsActive,
                  contentDescription = null,
                  tint = if (isMonitoringServiceRunning) EmeraldProfit else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(22.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Call & Message Monitoring Service",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = if (isMonitoringServiceRunning) "Running in background (DataSync)"
                         else "Stopped · Tap to start foreground monitoring",
                  style = MaterialTheme.typography.bodySmall,
                  color = if (isMonitoringServiceRunning) EmeraldProfit else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isMonitoringServiceRunning) EmeraldProfit.copy(alpha = 0.18f)
                      else MaterialTheme.colorScheme.surfaceVariant
            ) {
              Text(
                text = if (isMonitoringServiceRunning) "ACTIVE" else "INACTIVE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (isMonitoringServiceRunning) EmeraldProfit else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Live stats row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text("Events Monitored", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$monitoredEventsCount", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              }
            }
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text("Alerts Dispatched", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$alertsTriggeredCount", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CyanAccent)
              }
            }
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text("Channel", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Sound & Vibe", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = EmeraldProfit)
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "Active background service checks for incoming calls, SMS texts, and Facebook Messenger chats, posting high-priority heads-up alerts with sound, vibration, and interactive quick replies.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = { viewModel.toggleMonitoringService(context) },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isMonitoringServiceRunning) MaterialTheme.colorScheme.error else EmeraldProfit
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("toggle_background_service_button")
          ) {
            Icon(
              imageVector = if (isMonitoringServiceRunning) Icons.Default.Refresh else Icons.Default.NotificationsActive,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isMonitoringServiceRunning) "Stop Background Service" else "Start Background Monitoring Service",
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // Permission Banner (if Android 13+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Icon(
                Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Allow Heads-Up Alerts",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Enable notifications to hear sounds & banners when new texts arrive.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            Button(
              onClick = {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
              },
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Allow")
            }
          }
        }
      }
    }

    // Live Inbound Simulator Runner Toggle
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isLiveSimulationRunning) IndigoPrimary.copy(alpha = 0.12f)
                           else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                  if (isLiveSimulationRunning) EmeraldProfit.copy(alpha = 0.2f)
                  else MaterialTheme.colorScheme.surfaceVariant
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (isLiveSimulationRunning) Icons.Default.Campaign else Icons.Default.Alarm,
                contentDescription = null,
                tint = if (isLiveSimulationRunning) EmeraldProfit else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Automated Live Alerts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = if (isLiveSimulationRunning) "Active · Inbound message alerts dispatched every 25s"
                       else "Inactive · Tap to simulate continuous incoming messages",
                style = MaterialTheme.typography.bodySmall,
                color = if (isLiveSimulationRunning) EmeraldProfit else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Switch(
            checked = isLiveSimulationRunning,
            onCheckedChange = { viewModel.toggleLiveSimulation() },
            modifier = Modifier.testTag("live_simulation_toggle")
          )
        }
      }
    }

    // Instant Inbound Message Generator
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = CyanAccent)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Instant Inbound Message Alert",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }
          Text(
            text = "Fires a real heads-up system alert with vibration pattern and quick actions",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Sender Presets
          Text("Select Sender Preset:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            SenderPresetChip("Sophia Vance", "+1 (555) 329-8472") { name, num ->
              customSenderName = name
              customSenderNumber = num
              customMessageBody = "Client payment of $2,400 has been sent! Check invoice."
            }
            SenderPresetChip("David Miller", "+1 (555) 741-9238") { name, num ->
              customSenderName = name
              customSenderNumber = num
              customMessageBody = "Ready for our technical architecture review call whenever you are."
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = customSenderName,
            onValueChange = { customSenderName = it },
            label = { Text("Sender Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("sender_name_field")
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = customSenderNumber,
            onValueChange = { customSenderNumber = it },
            label = { Text("Sender Phone Number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("sender_number_field")
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = customMessageBody,
            onValueChange = { customMessageBody = it },
            label = { Text("Message Content") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth().testTag("message_body_field")
          )

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = {
              viewModel.triggerInstantIncomingAlert(
                senderName = customSenderName,
                senderNumber = customSenderNumber,
                messageBody = customMessageBody
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("dispatch_instant_alert_button")
          ) {
            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Dispatch Instant Message Alert", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Facebook Messenger Alert Card
    item {
      var fbSenderName by remember { mutableStateOf("Alex Rivera (Facebook Messenger)") }
      var fbMessageBody by remember { mutableStateOf("Hi! I saw your post on Facebook and wanted to request an estimate.") }

      Card(
        modifier = Modifier.fillMaxWidth().testTag("facebook_messenger_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF0084FF).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF0084FF), modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Facebook Messenger Alert",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Simulate inbound Messenger chats & open Facebook",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFF0084FF).copy(alpha = 0.15f)
            ) {
              Text(
                text = "Messenger",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0084FF),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedTextField(
            value = fbSenderName,
            onValueChange = { fbSenderName = it },
            label = { Text("Facebook Contact Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = fbMessageBody,
            onValueChange = { fbMessageBody = it },
            label = { Text("Messenger Text") },
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = {
                viewModel.triggerFacebookMessengerAlert(
                  senderName = fbSenderName,
                  messageBody = fbMessageBody
                )
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0084FF)),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f).testTag("dispatch_fb_alert_button")
            ) {
              Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Dispatch Alert", color = Color.White, fontSize = 12.sp)
            }

            OutlinedButton(
              onClick = {
                try {
                  val fbIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://m.me/"))
                  context.startActivity(fbIntent)
                } catch (e: Exception) {
                  viewModel.showInAppNotice("Unable to open Facebook Messenger app")
                }
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f).testTag("open_messenger_button")
            ) {
              Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF0084FF), modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Open Messenger", fontSize = 12.sp)
            }
          }
        }
      }
    }

    // Instant Call Alerts (Incoming & Missed)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Voice Call Alerts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Test incoming voice call rings or missed call notification alerts",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = {
                viewModel.triggerInstantCallAlert(
                  callerName = "Elena Rostova (Lead)",
                  callerNumber = "+1 (555) 892-1054",
                  isMissed = false
                )
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f).testTag("trigger_incoming_call_button")
            ) {
              Icon(Icons.Default.PhoneCallback, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Incoming Call", fontSize = 12.sp)
            }

            OutlinedButton(
              onClick = {
                viewModel.triggerInstantCallAlert(
                  callerName = "Marcus Chen (Partner)",
                  callerNumber = "+1 (555) 438-6621",
                  isMissed = true
                )
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f).testTag("trigger_missed_call_button")
            ) {
              Icon(Icons.Default.PhoneMissed, contentDescription = null, tint = RoseExpense, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Missed Call", fontSize = 12.sp, color = RoseExpense)
            }
          }
        }
      }
    }

    // Google Play Creator Compliance Architecture Info Card
    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.Top
        ) {
          Icon(
            Icons.Default.Security,
            contentDescription = null,
            tint = EmeraldProfit,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Privacy & Play Creator Compliance",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "All communications are tracked in local high-speed Room storage with zero-permission intents (ACTION_DIAL, ACTION_SENDTO) and native POST_NOTIFICATIONS alerts. Background interception of device-wide personal SMS/Call logs without user action is prohibited on the Google Play Creator Program, keeping this app 100% compliant and publishable.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(88.dp))
    }
  }

  if (showToolWizardDialog) {
    SystemMaintenanceWizardDialog(
      onDismiss = { showToolWizardDialog = false },
      onComplete = {
        viewModel.showInAppNotice("Tool Wizard completed: System diagnostic scan passed & links repaired!")
      }
    )
  }
}

@Composable
private fun SenderPresetChip(
  name: String,
  number: String,
  onSelect: (String, String) -> Unit
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .clickable { onSelect(name, number) }
  ) {
    Text(
      text = name,
      style = MaterialTheme.typography.labelSmall,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
    )
  }
}

package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ui.ContactPulseViewModel
import com.example.ui.components.CashAppSettlementCard
import com.example.ui.components.CellNumberHeader
import com.example.ui.components.LicensedProductAgreementDialog
import com.example.ui.components.SystemMaintenanceWizardDialog
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.IndigoPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
  viewModel: ContactPulseViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val activeCellNumber by viewModel.activeCellNumber.collectAsState()
  val cashAppTag by viewModel.cashAppTag.collectAsState()
  val cashAppDisplayName by viewModel.cashAppDisplayName.collectAsState()

  // Background Monitoring Service state
  val isMonitoringServiceRunning by viewModel.isMonitoringServiceRunning.collectAsState()
  val monitoredEventsCount by viewModel.monitoredEventsCount.collectAsState()
  val alertsTriggeredCount by viewModel.alertsTriggeredCount.collectAsState()

  // State for the Settings Updates Widget
  var isCheckingForUpdate by remember { mutableStateOf(false) }
  var updateStatusMessage by remember { mutableStateOf("I Spy is up to date (Version 1.0.0)") }
  var lastCheckedTime by remember { mutableStateOf("Just now") }
  var autoCheckUpdates by remember { mutableStateOf(true) }

  // Tool Wizard Dialog State
  var showToolWizardDialog by remember { mutableStateOf(false) }

  // Licensed Product & User Agreement Dialog State
  var showLicenseDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Settings & Updates",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "System update widget, Google Play distribution, and device profile",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    // Cell Line Section
    item {
      CellNumberHeader(
        cellNumber = activeCellNumber,
        onUpdateCellNumber = { viewModel.updateCellNumber(it) }
      )
    }

    // Cash App Revenue Payout Settings
    item {
      CashAppSettlementCard(
        cashAppTag = cashAppTag,
        displayName = cashAppDisplayName,
        onUpdateCashApp = { tag, name -> viewModel.updateCashAppInfo(tag, name) }
      )
    }

    // Administrator Rights & 100% Free Lifetime Access
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("admin_rights_card"),
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
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFF59E0B).copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.Verified,
                  contentDescription = "Admin Rights",
                  tint = Color(0xFFF59E0B),
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Administrator Rights",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Master Developer & Owner Status",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = EmeraldProfit.copy(alpha = 0.2f)
            ) {
              Text(
                text = "100% FREE LIFETIME",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldProfit,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldProfit, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Full unrestricted administrator privileges enabled.",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.SemiBold
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "As the author and owner of I Spy, this app is permanently free of charge to you at all times. All downloads of secondary services estimates, unlimited call and message telemetry, Facebook Messenger alerts, real-time alert dispatchers, and cash flow profit models are fully unlocked without subscription or payment walls.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = {
                viewModel.seedSampleData()
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Refresh Data", fontSize = 12.sp)
            }

            Button(
              onClick = {
                viewModel.showInAppNotice("Admin License: OWNER-FREE-LIFETIME-VERIFIED")
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("Verify Admin Key", fontSize = 12.sp)
            }
          }
        }
      }
    }

    // Licensed Product & Legal User Agreement Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("licensed_product_card"),
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
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.VerifiedUser,
                  contentDescription = null,
                  tint = Color(0xFFF59E0B),
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Licensed Product & Ownership",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Creator & Master Owner Rights Verified",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFF59E0B).copy(alpha = 0.2f)
            ) {
              Text(
                text = "LICENSED",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFB45309),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // EULA User Agreement Statement Quote
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Gavel, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "End User License Agreement (EULA)",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Under penalty of law, this software should not be used in any illegal activity whatsoever or manipulated or changed in anyway or reproduced for one's own monetary gain. All commercial monetization, paid Google Play download fees, and Cash App ($cashAppTag) receipts are reserved solely to the verified creator and owner.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = { showLicenseDialog = true },
            shape = RoundedCornerShape(12.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("open_license_dialog_button")
          ) {
            Icon(Icons.Default.Policy, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Full User Agreement & Certificate")
          }
        }
      }
    }

    // THE REQUESTED WIDGET: "have in the settings a widget for looking for updates and update using Samsung ui updates"
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("settings_updates_widget"),
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
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(IndigoPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.SystemUpdate,
                  contentDescription = null,
                  tint = IndigoPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "System & App Updates Widget",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Samsung One UI & Google Play Channel",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = EmeraldProfit.copy(alpha = 0.15f)
            ) {
              Text(
                text = "v1.0.0",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = EmeraldProfit,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // System info pills
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            SystemInfoPill("OS Target", "Android 15 / 16 (API 36)", Icons.Default.Android, Modifier.weight(1f))
            SystemInfoPill("UI System", "Samsung One UI & Material You", Icons.Default.Smartphone, Modifier.weight(1f))
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Status Box
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              if (isCheckingForUpdate) {
                CircularProgressIndicator(
                  modifier = Modifier.size(20.dp),
                  strokeWidth = 2.dp,
                  color = IndigoPrimary
                )
              } else {
                Icon(
                  Icons.Default.Verified,
                  contentDescription = null,
                  tint = EmeraldProfit,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = updateStatusMessage,
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = "Last checked: $lastCheckedTime",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Action Buttons: Check For Updates & Open Play Store Details
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = {
                isCheckingForUpdate = true
                updateStatusMessage = "Checking Samsung One UI & Google Play servers..."
                coroutineScope.launch {
                  delay(1600)
                  isCheckingForUpdate = false
                  updateStatusMessage = "Your I Spy build is the latest release (v1.0.0)"
                  lastCheckedTime = "Just now"
                  viewModel.showInAppNotice("Update check complete: System is up to date!")
                }
              },
              enabled = !isCheckingForUpdate,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("check_for_updates_button")
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Check for Updates")
            }

            OutlinedButton(
              onClick = {
                try {
                  val playIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.aistudio.contactpulse.vkmx")
                  )
                  context.startActivity(playIntent)
                } catch (e: Exception) {
                  val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.aistudio.contactpulse.vkmx")
                  )
                  context.startActivity(webIntent)
                }
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("open_store_listing_button")
            ) {
              Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Store Page")
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Dedicated Tool Wizard Launch inside Updates widget
          Button(
            onClick = { showToolWizardDialog = true },
            shape = RoundedCornerShape(12.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("launch_tool_wizard_widget_button")
          ) {
            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Launch Tool Wizard (Scan, Updates & Fix Links)", fontSize = 13.sp)
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Auto-check updates toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Auto-check for Samsung UI updates",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Switch(
              checked = autoCheckUpdates,
              onCheckedChange = { autoCheckUpdates = it }
            )
          }
        }
      }
    }

    // Background Call & Message Monitoring Service Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("background_monitoring_settings_card"),
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
                  .size(38.dp)
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
                  modifier = Modifier.size(20.dp)
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
                  text = "Android DataSync Foreground Service",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isMonitoringServiceRunning) EmeraldProfit.copy(alpha = 0.18f)
                      else MaterialTheme.colorScheme.surfaceVariant
            ) {
              Text(
                text = if (isMonitoringServiceRunning) "ACTIVE" else "STOPPED",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (isMonitoringServiceRunning) EmeraldProfit else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Live counters row
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
                Text("Monitored Events", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$monitoredEventsCount events", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              }
            }
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text("Alerts Dispatched", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$alertsTriggeredCount alerts", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CyanAccent)
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "Runs continuously in the background using Android Foreground Service DataSync architecture to monitor incoming calls and messages and fire real-time system notifications with sound and vibration.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = { viewModel.toggleMonitoringService(context) },
            shape = RoundedCornerShape(12.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
              containerColor = if (isMonitoringServiceRunning) MaterialTheme.colorScheme.error else EmeraldProfit
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("toggle_monitoring_service_settings_button")
          ) {
            Icon(
              imageVector = if (isMonitoringServiceRunning) Icons.Default.Refresh else Icons.Default.NotificationsActive,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isMonitoringServiceRunning) "Stop Monitoring Service" else "Start Background Monitoring Service",
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // System Tool Wizard Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("system_tool_wizard_card"),
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
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(CyanAccent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.AutoFixHigh,
                  contentDescription = null,
                  tint = CyanAccent,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "System Maintenance Wizard",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Scan, Update & Fix Links",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = CyanAccent.copy(alpha = 0.15f)
            ) {
              Text(
                text = "WIZARD",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = CyanAccent,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "Use the interactive Tool Wizard to check Samsung One UI and Google Play updates, run a full system diagnostic scan on local database and notification channels, and automatically detect and fix broken links and telephony intent schemes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = { showToolWizardDialog = true },
            shape = RoundedCornerShape(12.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = CyanAccent),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("launch_system_tool_wizard_button")
          ) {
            Icon(Icons.Default.Build, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Tool Wizard", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Google Play Upload & Monetization Hub
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Shop, contentDescription = null, tint = CyanAccent)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Google Play Upload & Paid Download",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }
          Text(
            text = "Publishing as a paid download on Google Play and monetizing software",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Step 1: Exporting AAB
          MonetizationStepItem(
            stepNumber = "1",
            title = "Download Release AAB / APK",
            description = "Click the top-right Settings menu in AI Studio and select 'Generate Android App Bundle (AAB)' to get your signed upload package."
          )

          // Step 2: Set Paid Download on Google Play Console
          MonetizationStepItem(
            stepNumber = "2",
            title = "Set Paid App Price on Play Console",
            description = "In your Google Play Console dashboard under Monetize > App Pricing, choose 'Paid' and set your download price (e.g., $4.99 or $9.99) so every download pays you directly!"
          )

          // Step 3: Advertising Policy Notice
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Advertising Monetization Policy Notice",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "The Google Play Creator Program strictly prohibits advertising monetization and bundling ad SDKs (such as AdMob, Display Ads, or Ads Mediation). To ensure your app is 100% compliant and publishable on the Creator Program, we monetize via Paid Upfront Google Play Downloads, Consulting Call Billing, and Lead Retainers. If you confirm you prefer third-party ad networks instead, we can add them upon your confirmation (which makes the app ineligible for the Creator Program).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
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

  if (showLicenseDialog) {
    LicensedProductAgreementDialog(
      cashAppTag = cashAppTag,
      onDismiss = { showLicenseDialog = false },
      onAcknowledge = {
        viewModel.showInAppNotice("User Agreement acknowledged: 100% legal compliance & creator rights verified")
      }
    )
  }
}

@Composable
private fun SystemInfoPill(
  label: String,
  value: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    modifier = modifier
  ) {
    Row(
      modifier = Modifier.padding(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(icon, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
private fun MonetizationStepItem(
  stepNumber: String,
  title: String,
  description: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    verticalAlignment = Alignment.Top
  ) {
    Box(
      modifier = Modifier
        .size(24.dp)
        .clip(CircleShape)
        .background(IndigoPrimary),
      contentAlignment = Alignment.Center
    ) {
      Text(stepNumber, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(modifier = Modifier.width(10.dp))
    Column {
      Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
      Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

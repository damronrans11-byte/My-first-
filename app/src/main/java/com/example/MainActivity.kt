package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppTab
import com.example.ui.ContactPulseViewModel
import com.example.ui.components.AddCashFlowDialog
import com.example.ui.components.ComposeMessageDialog
import com.example.ui.components.InAppAlertBanner
import com.example.ui.components.LogCallDialog
import com.example.ui.components.SystemMaintenanceWizardDialog
import com.example.ui.screens.AlertsSimulatorScreen
import com.example.ui.screens.CallLogsScreen
import com.example.ui.screens.CashFlowScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EstimateScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.util.NotificationHelper

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    NotificationHelper.createNotificationChannel(this)

    setContent {
      MyApplicationTheme {
        ContactPulseApp()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactPulseApp(
  viewModel: ContactPulseViewModel = viewModel()
) {
  val selectedTab by viewModel.selectedTab.collectAsState()
  val activeCellNumber by viewModel.activeCellNumber.collectAsState()
  val inAppAlert by viewModel.inAppAlert.collectAsState()
  val communications by viewModel.communications.collectAsState()

  val unreadMessagesCount = remember(communications) {
    communications.count { it.type.startsWith("SMS") && !it.isRead }
  }

  // Dialog States
  var showLogCallDialog by remember { mutableStateOf(false) }
  var showComposeDialog by remember { mutableStateOf(false) }
  var showAddCashFlowDialog by remember { mutableStateOf(false) }
  var showToolWizardDialog by remember { mutableStateOf(false) }

  // Custom BackHandler: If on secondary tab, back returns to Dashboard
  BackHandler(enabled = selectedTab != AppTab.DASHBOARD) {
    viewModel.setTab(AppTab.DASHBOARD)
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "I Spy",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = activeCellNumber,
              style = MaterialTheme.typography.labelSmall,
              color = EmeraldProfit,
              fontWeight = FontWeight.SemiBold
            )
          }
        },
        actions = {
          IconButton(
            onClick = { showToolWizardDialog = true },
            modifier = Modifier.testTag("top_bar_tool_wizard_button")
          ) {
            Icon(
              imageVector = Icons.Default.Build,
              contentDescription = "System Tool Wizard",
              tint = IndigoPrimary
            )
          }
          IconButton(
            onClick = { viewModel.setTab(AppTab.SETTINGS) },
            modifier = Modifier.testTag("top_bar_settings_button")
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Settings & Updates",
              tint = if (selectedTab == AppTab.SETTINGS) IndigoPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
      ) {
        NavigationBarItem(
          selected = selectedTab == AppTab.DASHBOARD,
          onClick = { viewModel.setTab(AppTab.DASHBOARD) },
          icon = { Icon(Icons.Default.Analytics, contentDescription = "Analytics") },
          label = { Text("Overview", fontSize = 10.sp) },
          modifier = Modifier.testTag("tab_dashboard")
        )
        NavigationBarItem(
          selected = selectedTab == AppTab.CALLS,
          onClick = { viewModel.setTab(AppTab.CALLS) },
          icon = { Icon(Icons.Default.Call, contentDescription = "Calls") },
          label = { Text("Calls", fontSize = 10.sp) },
          modifier = Modifier.testTag("tab_calls")
        )
        NavigationBarItem(
          selected = selectedTab == AppTab.MESSAGES,
          onClick = { viewModel.setTab(AppTab.MESSAGES) },
          icon = {
            if (unreadMessagesCount > 0) {
              BadgedBox(
                badge = {
                  Badge(containerColor = CyanAccent) {
                    Text("$unreadMessagesCount", color = Color.White)
                  }
                }
              ) {
                Icon(Icons.Default.Chat, contentDescription = "Messages")
              }
            } else {
              Icon(Icons.Default.Chat, contentDescription = "Messages")
            }
          },
          label = { Text("Messages", fontSize = 10.sp) },
          modifier = Modifier.testTag("tab_messages")
        )
        NavigationBarItem(
          selected = selectedTab == AppTab.CASH_FLOW,
          onClick = { viewModel.setTab(AppTab.CASH_FLOW) },
          icon = { Icon(Icons.Default.AttachMoney, contentDescription = "Cash Flow") },
          label = { Text("Profit", fontSize = 10.sp) },
          modifier = Modifier.testTag("tab_cash_flow")
        )
        NavigationBarItem(
          selected = selectedTab == AppTab.ESTIMATE,
          onClick = { viewModel.setTab(AppTab.ESTIMATE) },
          icon = { Icon(Icons.Default.RequestQuote, contentDescription = "Estimate") },
          label = { Text("Estimate", fontSize = 10.sp) },
          modifier = Modifier.testTag("tab_estimate")
        )
        NavigationBarItem(
          selected = selectedTab == AppTab.ALERTS,
          onClick = { viewModel.setTab(AppTab.ALERTS) },
          icon = { Icon(Icons.Default.NotificationsActive, contentDescription = "Alerts") },
          label = { Text("Alerts", fontSize = 10.sp) },
          modifier = Modifier.testTag("tab_alerts")
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      AnimatedContent(
        targetState = selectedTab,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "TabContent"
      ) { tab ->
        when (tab) {
          AppTab.DASHBOARD -> DashboardScreen(
            viewModel = viewModel,
            onNavigateToTab = { viewModel.setTab(it) },
            onQuickLogCall = { showLogCallDialog = true },
            onQuickLogMessage = { showComposeDialog = true },
            onQuickAddCashFlow = { showAddCashFlowDialog = true }
          )
          AppTab.CALLS -> CallLogsScreen(
            viewModel = viewModel,
            onOpenLogCallDialog = { showLogCallDialog = true }
          )
          AppTab.MESSAGES -> MessagesScreen(
            viewModel = viewModel,
            onOpenComposeDialog = { showComposeDialog = true }
          )
          AppTab.CASH_FLOW -> CashFlowScreen(
            viewModel = viewModel,
            onOpenAddTransaction = { showAddCashFlowDialog = true }
          )
          AppTab.ESTIMATE -> EstimateScreen(
            viewModel = viewModel
          )
          AppTab.ALERTS -> AlertsSimulatorScreen(
            viewModel = viewModel
          )
          AppTab.SETTINGS -> SettingsScreen(
            viewModel = viewModel
          )
        }
      }

      // In-App Alert banner
      InAppAlertBanner(
        message = inAppAlert,
        onDismiss = { viewModel.dismissInAppAlert() },
        modifier = Modifier.align(Alignment.TopCenter)
      )
    }
  }

  // Dialogs
  if (showLogCallDialog) {
    LogCallDialog(
      onDismiss = { showLogCallDialog = false },
      onConfirm = { name, number, type, durationSec, isBillable, rate ->
        viewModel.logManualCall(name, number, type, durationSec, isBillable, rate)
        showLogCallDialog = false
      }
    )
  }

  if (showComposeDialog) {
    ComposeMessageDialog(
      onDismiss = { showComposeDialog = false },
      onConfirm = { name, number, body, isIncoming, triggerAlert ->
        viewModel.logManualMessage(name, number, body, isIncoming, triggerAlert)
        showComposeDialog = false
      }
    )
  }

  if (showAddCashFlowDialog) {
    AddCashFlowDialog(
      onDismiss = { showAddCashFlowDialog = false },
      onConfirm = { title, amount, type, category, contact, notes ->
        viewModel.addCashFlow(title, amount, type, category, contact, notes)
        showAddCashFlowDialog = false
      }
    )
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

package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.PhoneForwarded
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CommunicationEntity
import com.example.ui.AppTab
import com.example.ui.ContactFrequencyMetric
import com.example.ui.ContactPulseViewModel
import com.example.ui.TimeRange
import com.example.ui.components.CashFlowProfitBarChart
import com.example.ui.components.CellNumberHeader
import com.example.ui.components.CommunicationRatioDonutChart
import com.example.ui.components.ContactFrequencyBarChart
import com.example.ui.components.ContactInteractionTrendsChart
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseExpense

@Composable
fun DashboardScreen(
  viewModel: ContactPulseViewModel,
  onNavigateToTab: (AppTab) -> Unit,
  onQuickLogCall: () -> Unit,
  onQuickLogMessage: () -> Unit,
  onQuickAddCashFlow: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val activeCellNumber by viewModel.activeCellNumber.collectAsState()
  val communications by viewModel.communications.collectAsState()
  val contactFrequencies by viewModel.contactFrequencies.collectAsState()
  val interactionTrends by viewModel.interactionTrends.collectAsState()
  val cashFlowSummary by viewModel.cashFlowSummary.collectAsState()
  val chartTimeRange by viewModel.chartTimeRange.collectAsState()

  val totalCalls = communications.count { it.type.startsWith("CALL") }
  val totalMessages = communications.count { it.type.startsWith("SMS") }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      CellNumberHeader(
        cellNumber = activeCellNumber,
        onUpdateCellNumber = { viewModel.updateCellNumber(it) }
      )
    }

    // High-Level Overview Metrics
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        SummaryKpiCard(
          title = "Total Interactions",
          value = "${communications.size}",
          subtitle = "$totalCalls calls · $totalMessages texts",
          icon = Icons.Default.TrendingUp,
          accentColor = IndigoPrimary,
          modifier = Modifier.weight(1f)
        )

        SummaryKpiCard(
          title = "Net Cash Profit",
          value = "$${"%.0f".format(cashFlowSummary.netProfit)}",
          subtitle = "${cashFlowSummary.profitMarginPercent}% margin",
          icon = Icons.Default.AttachMoney,
          accentColor = EmeraldProfit,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Quick Action Bar
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "Instant Actions & Alerts",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                viewModel.triggerInstantIncomingAlert(
                  senderName = "Sophia Vance (VIP Client)",
                  senderNumber = "+1 (555) 329-8472",
                  messageBody = "Hey! Urgently reviewing the proposal. Call me back when you see this!"
                )
              },
              colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("trigger_test_alert_button")
            ) {
              Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Test Alert", fontSize = 12.sp)
            }

            OutlinedButton(
              onClick = onQuickLogCall,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("quick_log_call_button")
            ) {
              Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Log Call", fontSize = 12.sp)
            }

            OutlinedButton(
              onClick = onQuickLogMessage,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("quick_log_msg_button")
            ) {
              Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Text", fontSize = 12.sp)
            }
          }
        }
      }
    }

    // Chart Time Range Selector
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Interactive Analytics",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          TimeRange.entries.forEach { range ->
            FilterChip(
              selected = chartTimeRange == range,
              onClick = { viewModel.setTimeRange(range) },
              label = { Text(range.label, fontSize = 12.sp) }
            )
          }
        }
      }
    }

    // Interaction Frequency Trends Component (7-Day Daily Trend Curve)
    item {
      ContactInteractionTrendsChart(
        trendPoints = interactionTrends
      )
    }

    // Contact Frequency Ranking Bar Chart
    item {
      ContactFrequencyBarChart(
        metrics = contactFrequencies,
        onContactClick = { selected ->
          // Quick option to call or text
        }
      )
    }

    // Channel Ratio Donut Chart
    item {
      CommunicationRatioDonutChart(
        totalCalls = totalCalls,
        totalMessages = totalMessages
      )
    }

    // Cash Flow Profit Bar Chart
    item {
      CashFlowProfitBarChart(
        inflow = cashFlowSummary.totalInflow,
        outflow = cashFlowSummary.totalOutflow,
        netProfit = cashFlowSummary.netProfit
      )
    }

    // Recent Activity Section
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Recent Interactions",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "View All",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.clickable { onNavigateToTab(AppTab.CALLS) }
        )
      }
    }

    items(communications.take(6)) { comm ->
      RecentInteractionCard(
        comm = comm,
        onCallClick = {
          val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${comm.contactNumber}")
          }
          context.startActivity(dialIntent)
        },
        onSmsClick = {
          val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${comm.contactNumber}")
          }
          context.startActivity(smsIntent)
        }
      )
    }

    item {
      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}

@Composable
fun SummaryKpiCard(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = value,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = accentColor,
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}

@Composable
fun RecentInteractionCard(
  comm: CommunicationEntity,
  onCallClick: () -> Unit,
  onSmsClick: () -> Unit
) {
  val isCall = comm.type.startsWith("CALL")
  val isIncoming = comm.type.contains("INCOMING") || comm.type.contains("MISSED")

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(
              when {
                comm.type == "CALL_MISSED" -> RoseExpense.copy(alpha = 0.15f)
                isCall -> IndigoPrimary.copy(alpha = 0.15f)
                else -> CyanAccent.copy(alpha = 0.15f)
              }
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = when {
              comm.type == "CALL_MISSED" -> Icons.Default.PhoneMissed
              comm.type == "CALL_INCOMING" -> Icons.Default.PhoneCallback
              comm.type == "CALL_OUTGOING" -> Icons.Default.PhoneForwarded
              else -> Icons.Default.Chat
            },
            contentDescription = null,
            tint = when {
              comm.type == "CALL_MISSED" -> RoseExpense
              isCall -> IndigoPrimary
              else -> CyanAccent
            },
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = comm.contactName,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            if (!comm.isRead) {
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(CyanAccent)
              )
            }
          }

          Text(
            text = if (comm.messageBody.isNotEmpty()) comm.messageBody else comm.type,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .clickable { onCallClick() }
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Call, contentDescription = "Call", tint = IndigoPrimary, modifier = Modifier.size(16.dp))
          }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .clickable { onSmsClick() }
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Chat, contentDescription = "SMS", tint = CyanAccent, modifier = Modifier.size(16.dp))
          }
        }
      }
    }
  }
}

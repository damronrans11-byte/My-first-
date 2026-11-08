package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CashFlowEntity
import com.example.ui.ContactPulseViewModel
import com.example.ui.components.CashAppSettlementCard
import com.example.ui.components.CashFlowProfitBarChart
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseExpense
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CashFlowScreen(
  viewModel: ContactPulseViewModel,
  onOpenAddTransaction: () -> Unit,
  modifier: Modifier = Modifier
) {
  val cashFlows by viewModel.cashFlows.collectAsState()
  val summary by viewModel.cashFlowSummary.collectAsState()
  val cashAppTag by viewModel.cashAppTag.collectAsState()
  val cashAppDisplayName by viewModel.cashAppDisplayName.collectAsState()

  var flowFilter by remember { mutableStateOf("ALL") } // ALL, INCOME, EXPENSE
  var showCalculator by remember { mutableStateOf(true) }

  // Interactive Profit Generator Sliders
  var hourlyRate by remember { mutableFloatStateOf(125f) }
  var weeklyCallHours by remember { mutableFloatStateOf(12f) }
  var playStoreDownloads by remember { mutableFloatStateOf(250f) }
  var playAppPrice by remember { mutableFloatStateOf(4.99f) }

  val monthlyCallRevenue = remember(hourlyRate, weeklyCallHours) {
    hourlyRate * weeklyCallHours * 4.33f
  }
  val monthlyPlayRevenue = remember(playStoreDownloads, playAppPrice) {
    playStoreDownloads * playAppPrice * 0.85f // net after platform fee
  }
  val estimatedMonthlyTotalInflow = remember(monthlyCallRevenue, monthlyPlayRevenue) {
    monthlyCallRevenue + monthlyPlayRevenue
  }
  val estimatedMonthlyNetProfit = remember(estimatedMonthlyTotalInflow) {
    (estimatedMonthlyTotalInflow - 120.0f).coerceAtLeast(0f) // deduct standard monthly phone & tools overhead
  }

  val filteredFlows = remember(cashFlows, flowFilter) {
    when (flowFilter) {
      "INCOME" -> cashFlows.filter { it.type == "INCOME" }
      "EXPENSE" -> cashFlows.filter { it.type == "EXPENSE" }
      else -> cashFlows
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Cash Flow & Profit Engine",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Track revenue from communication channels and generate your personal cash flow profit",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Main Net Profit Hero Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("net_profit_hero_card"),
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
          Column(
            modifier = Modifier
              .background(
                Brush.linearGradient(
                  listOf(
                    IndigoPrimary.copy(alpha = 0.12f),
                    EmeraldProfit.copy(alpha = 0.12f)
                  )
                )
              )
              .padding(18.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "NET CASH FLOW PROFIT",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = EmeraldProfit.copy(alpha = 0.2f)
              ) {
                Text(
                  text = "${summary.profitMarginPercent}% Margin",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldProfit,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "$${"%,.2f".format(summary.netProfit)}",
              style = MaterialTheme.typography.headlineLarge,
              fontWeight = FontWeight.ExtraBold,
              color = if (summary.netProfit >= 0) EmeraldProfit else RoseExpense
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Inflow vs Outflow Split Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              // Total Inflow
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.weight(1f)
              ) {
                Row(
                  modifier = Modifier.padding(10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(28.dp)
                      .clip(CircleShape)
                      .background(EmeraldProfit.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = EmeraldProfit, modifier = Modifier.size(16.dp))
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text("Total Inflow", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("+$${"%,.0f".format(summary.totalInflow)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = EmeraldProfit)
                  }
                }
              }

              // Total Outflow
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.weight(1f)
              ) {
                Row(
                  modifier = Modifier.padding(10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(28.dp)
                      .clip(CircleShape)
                      .background(RoseExpense.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = RoseExpense, modifier = Modifier.size(16.dp))
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text("Total Outflow", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("-$${"%,.0f".format(summary.totalOutflow)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = RoseExpense)
                  }
                }
              }
            }
          }
        }
      }

      // Cash App Revenue Settlement Destination
      item {
        CashAppSettlementCard(
          cashAppTag = cashAppTag,
          displayName = cashAppDisplayName,
          onUpdateCashApp = { tag, name -> viewModel.updateCashAppInfo(tag, name) }
        )
      }

      // Interactive Cash Flow Profit Generator & Forecast
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("cash_flow_generator_card"),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(IndigoPrimary.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Calculate, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "Cash Flow Profit Generator",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Interactive income & profit projection model",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Slider 1: Hourly Call Rate
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Billable Call Rate", style = MaterialTheme.typography.bodyMedium)
                Text("$${hourlyRate.toInt()}/hr", fontWeight = FontWeight.Bold, color = IndigoPrimary)
              }
              Slider(
                value = hourlyRate,
                onValueChange = { hourlyRate = it },
                valueRange = 50f..350f,
                steps = 11,
                modifier = Modifier.fillMaxWidth().testTag("rate_slider")
              )
            }

            // Slider 2: Weekly Call Hours
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Weekly Consulting Call Hours", style = MaterialTheme.typography.bodyMedium)
                Text("${weeklyCallHours.toInt()} hrs/week", fontWeight = FontWeight.Bold, color = IndigoPrimary)
              }
              Slider(
                value = weeklyCallHours,
                onValueChange = { weeklyCallHours = it },
                valueRange = 2f..40f,
                steps = 18,
                modifier = Modifier.fillMaxWidth().testTag("hours_slider")
              )
            }

            // Slider 3: Paid App Downloads (Google Play)
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Google Play Paid Downloads ($4.99/ea)", style = MaterialTheme.typography.bodyMedium)
                Text("${playStoreDownloads.toInt()} sales", fontWeight = FontWeight.Bold, color = CyanAccent)
              }
              Slider(
                value = playStoreDownloads,
                onValueChange = { playStoreDownloads = it },
                valueRange = 10f..1000f,
                steps = 19,
                modifier = Modifier.fillMaxWidth().testTag("downloads_slider")
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Projected Monthly Result Box
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = EmeraldProfit.copy(alpha = 0.12f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text(
                  text = "PROJECTED MONTHLY CASH PROFIT",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp,
                  color = EmeraldProfit
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "$${"%,.0f".format(estimatedMonthlyNetProfit)} / month",
                  style = MaterialTheme.typography.headlineMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = EmeraldProfit
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Breakdown: $${"%,.0f".format(monthlyCallRevenue)} from communication calls + $${"%,.0f".format(monthlyPlayRevenue)} from Google Play app purchases (less $120 mobile/telecom overhead).",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }

      // Cash Flow Chart
      item {
        CashFlowProfitBarChart(
          inflow = summary.totalInflow,
          outflow = summary.totalOutflow,
          netProfit = summary.netProfit
        )
      }

      // Transaction History Header & Filter
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Cash Flow Records",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("ALL" to "All", "INCOME" to "Inflow", "EXPENSE" to "Outflow").forEach { (key, label) ->
              FilterChip(
                selected = flowFilter == key,
                onClick = { flowFilter = key },
                label = { Text(label, fontSize = 12.sp) }
              )
            }
          }
        }
      }

      if (filteredFlows.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp),
            contentAlignment = Alignment.Center
          ) {
            Text("No cash flow entries logged yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      } else {
        items(filteredFlows, key = { it.id }) { item ->
          CashFlowItemCard(
            item = item,
            onDelete = { viewModel.deleteCashFlow(item) }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(88.dp))
      }
    }

    FloatingActionButton(
      onClick = onOpenAddTransaction,
      containerColor = EmeraldProfit,
      contentColor = Color.White,
      shape = CircleShape,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(20.dp)
        .testTag("add_cash_flow_fab")
    ) {
      Icon(Icons.Default.Add, contentDescription = "Add Cash Flow")
    }
  }
}

@Composable
fun CashFlowItemCard(
  item: CashFlowEntity,
  onDelete: () -> Unit
) {
  val isIncome = item.type == "INCOME"
  val dateFormatted = remember(item.timestamp) {
    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(item.timestamp))
  }

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
        .padding(14.dp),
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
            .background(if (isIncome) EmeraldProfit.copy(alpha = 0.15f) else RoseExpense.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
            contentDescription = null,
            tint = if (isIncome) EmeraldProfit else RoseExpense,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
            text = item.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "${item.category} · $dateFormatted",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          if (!item.relatedContactName.isNullOrBlank()) {
            Text(
              text = "Contact: ${item.relatedContactName}",
              style = MaterialTheme.typography.labelSmall,
              color = IndigoPrimary
            )
          }
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = if (isIncome) "+$${"%,.2f".format(item.amount)}" else "-$${"%,.2f".format(item.amount)}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = if (isIncome) EmeraldProfit else RoseExpense
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
        }
      }
    }
  }
}

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.PhoneForwarded
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CommunicationEntity
import com.example.ui.ContactPulseViewModel
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseExpense
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallLogsScreen(
  viewModel: ContactPulseViewModel,
  onOpenLogCallDialog: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val communications by viewModel.communications.collectAsState()
  val callFilter by viewModel.callFilter.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()

  val callLogs = remember(communications, callFilter, searchQuery) {
    communications.filter { it.type.startsWith("CALL") }
      .filter {
        when (callFilter) {
          "INCOMING" -> it.type == "CALL_INCOMING"
          "OUTGOING" -> it.type == "CALL_OUTGOING"
          "MISSED" -> it.type == "CALL_MISSED"
          else -> true
        }
      }
      .filter {
        if (searchQuery.isBlank()) true
        else it.contactName.contains(searchQuery, ignoreCase = true) ||
             it.contactNumber.contains(searchQuery)
      }
  }

  var callToBill by remember { mutableStateOf<CommunicationEntity?>(null) }

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Call History & Telemetry",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Interactive call records with direct dial, SMS, and profit conversion",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Search Bar
      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { viewModel.setSearchQuery(it) },
          placeholder = { Text("Search by name or phone...") },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("call_logs_search_field")
        )
      }

      // Filter Chips
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(
            "ALL" to "All Calls",
            "INCOMING" to "Incoming",
            "OUTGOING" to "Outgoing",
            "MISSED" to "Missed"
          ).forEach { (key, label) ->
            FilterChip(
              selected = callFilter == key,
              onClick = { viewModel.setCallFilter(key) },
              label = { Text(label, fontSize = 12.sp) },
              modifier = Modifier.testTag("filter_call_$key")
            )
          }
        }
      }

      if (callLogs.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(200.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                Icons.Default.Call,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "No calls found for this filter.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      } else {
        items(callLogs, key = { it.id }) { call ->
          CallLogItemCard(
            call = call,
            onDial = {
              val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${call.contactNumber}")
              }
              context.startActivity(dialIntent)
            },
            onSms = {
              val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${call.contactNumber}")
              }
              context.startActivity(smsIntent)
            },
            onConvertToBillable = { callToBill = call },
            onDelete = { viewModel.deleteCommunication(call) }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(88.dp))
      }
    }

    FloatingActionButton(
      onClick = onOpenLogCallDialog,
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = Color.White,
      shape = CircleShape,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(20.dp)
        .testTag("add_call_log_fab")
    ) {
      Icon(Icons.Default.Add, contentDescription = "Log Call")
    }
  }

  // Convert to Billable Cash Flow Dialog
  callToBill?.let { call ->
    ConvertToProfitDialog(
      call = call,
      onDismiss = { callToBill = null },
      onConfirm = { rate ->
        viewModel.convertCallToBillable(call, rate)
        callToBill = null
      }
    )
  }
}

@Composable
fun CallLogItemCard(
  call: CommunicationEntity,
  onDial: () -> Unit,
  onSms: () -> Unit,
  onConvertToBillable: () -> Unit,
  onDelete: () -> Unit
) {
  val dateFormatted = remember(call.timestamp) {
    SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(call.timestamp))
  }

  val durationFormatted = remember(call.callDurationSeconds) {
    val m = call.callDurationSeconds / 60
    val s = call.callDurationSeconds % 60
    if (m > 0) "${m}m ${s}s" else "${s}s"
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("call_item_${call.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(
                when (call.type) {
                  "CALL_MISSED" -> RoseExpense.copy(alpha = 0.15f)
                  "CALL_INCOMING" -> IndigoPrimary.copy(alpha = 0.15f)
                  else -> CyanAccent.copy(alpha = 0.15f)
                }
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = when (call.type) {
                "CALL_MISSED" -> Icons.Default.PhoneMissed
                "CALL_INCOMING" -> Icons.Default.PhoneCallback
                else -> Icons.Default.PhoneForwarded
              },
              contentDescription = null,
              tint = when (call.type) {
                "CALL_MISSED" -> RoseExpense
                "CALL_INCOMING" -> IndigoPrimary
                else -> CyanAccent
              },
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = call.contactName,
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "${call.contactNumber} · $dateFormatted",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Call duration badge
        if (call.type != "CALL_MISSED") {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Text(
              text = durationFormatted,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        } else {
          Text(
            text = "Missed",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = RoseExpense
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Billable Status & Actions Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (call.isBillable) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = EmeraldProfit.copy(alpha = 0.15f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                Icons.Default.AttachMoney,
                contentDescription = null,
                tint = EmeraldProfit,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = "Billed $${"%.2f".format(call.billedAmount)}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = EmeraldProfit
              )
            }
          }
        } else if (call.type != "CALL_MISSED" && call.callDurationSeconds > 0) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.clickable { onConvertToBillable() }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                Icons.Default.AttachMoney,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = "Monetize Call",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        } else {
          Spacer(modifier = Modifier.width(1.dp))
        }

        // Action Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          IconButton(onClick = onDial, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Call, contentDescription = "Dial", tint = IndigoPrimary, modifier = Modifier.size(18.dp))
          }
          IconButton(onClick = onSms, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Chat, contentDescription = "SMS", tint = CyanAccent, modifier = Modifier.size(18.dp))
          }
          IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
          }
        }
      }
    }
  }
}

@Composable
fun ConvertToProfitDialog(
  call: CommunicationEntity,
  onDismiss: () -> Unit,
  onConfirm: (Double) -> Unit
) {
  var rateText by remember { mutableStateOf("125") }

  val hours = (call.callDurationSeconds.toDouble() / 3600.0).coerceAtLeast(0.15)
  val parsedRate = rateText.toDoubleOrNull() ?: 0.0
  val estimatedProfit = Math.round(hours * parsedRate * 100.0) / 100.0

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(Icons.Default.AttachMoney, contentDescription = null, tint = EmeraldProfit)
    },
    title = {
      Text("Convert Call to Cash Flow Profit", fontWeight = FontWeight.Bold)
    },
    text = {
      Column {
        Text(
          text = "Monetize this ${(call.callDurationSeconds / 60)}m call with ${call.contactName} as billable client revenue.",
          style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
          value = rateText,
          onValueChange = { rateText = it },
          label = { Text("Hourly Rate ($/hr)") },
          prefix = { Text("$") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = EmeraldProfit.copy(alpha = 0.12f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Calculated Cash Revenue:", style = MaterialTheme.typography.bodySmall)
            Text(
              text = "+$$estimatedProfit",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = EmeraldProfit
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(parsedRate) },
        enabled = parsedRate > 0
      ) {
        Text("Record Inflow")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InteractionType
import com.example.ui.theme.EmeraldProfit

@Composable
fun LogCallDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, String, InteractionType, Int, Boolean, Double) -> Unit
) {
  var contactName by remember { mutableStateOf("") }
  var phoneNumber by remember { mutableStateOf("") }
  var callType by remember { mutableStateOf(InteractionType.CALL_INCOMING) }
  var durationMinutesText by remember { mutableStateOf("15") }
  var isBillable by remember { mutableStateOf(true) }
  var hourlyRateText by remember { mutableStateOf("125") }

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(Icons.Default.Call, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    },
    title = {
      Text("Log Communication Call", fontWeight = FontWeight.Bold)
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = contactName,
          onValueChange = { contactName = it },
          label = { Text("Contact Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("dialog_call_name")
        )

        OutlinedTextField(
          value = phoneNumber,
          onValueChange = { phoneNumber = it },
          label = { Text("Phone Number") },
          placeholder = { Text("+1 (555) 000-0000") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          modifier = Modifier.fillMaxWidth().testTag("dialog_call_number")
        )

        // Type chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(
            InteractionType.CALL_INCOMING to "Incoming",
            InteractionType.CALL_OUTGOING to "Outgoing",
            InteractionType.CALL_MISSED to "Missed"
          ).forEach { (type, label) ->
            FilterChip(
              selected = callType == type,
              onClick = { callType = type },
              label = { Text(label, fontSize = 11.sp) }
            )
          }
        }

        if (callType != InteractionType.CALL_MISSED) {
          OutlinedTextField(
            value = durationMinutesText,
            onValueChange = { durationMinutesText = it },
            label = { Text("Call Duration (minutes)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
          )

          Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
              checked = isBillable,
              onCheckedChange = { isBillable = it }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Monetize call as billable cash profit", style = MaterialTheme.typography.bodySmall)
          }

          if (isBillable) {
            OutlinedTextField(
              value = hourlyRateText,
              onValueChange = { hourlyRateText = it },
              label = { Text("Billable Rate ($/hr)") },
              prefix = { Text("$") },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val minutes = durationMinutesText.toIntOrNull() ?: 1
          val durationSec = if (callType == InteractionType.CALL_MISSED) 0 else minutes * 60
          val rate = hourlyRateText.toDoubleOrNull() ?: 0.0
          onConfirm(
            contactName.ifBlank { "Client Contact" },
            phoneNumber.ifBlank { "+1 (555) 123-4567" },
            callType,
            durationSec,
            isBillable && callType != InteractionType.CALL_MISSED,
            rate
          )
        },
        modifier = Modifier.testTag("dialog_call_confirm_button")
      ) {
        Text("Save Call Log")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

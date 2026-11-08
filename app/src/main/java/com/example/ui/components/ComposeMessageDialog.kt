package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ComposeMessageDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, String, String, Boolean, Boolean) -> Unit
) {
  val context = LocalContext.current
  var contactName by remember { mutableStateOf("") }
  var phoneNumber by remember { mutableStateOf("") }
  var messageText by remember { mutableStateOf("") }
  var isIncoming by remember { mutableStateOf(true) }
  var triggerAlert by remember { mutableStateOf(true) }

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(Icons.Default.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
    },
    title = {
      Text(if (isIncoming) "Simulate Incoming Text Alert" else "Log Outbound Message", fontWeight = FontWeight.Bold)
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = isIncoming,
            onClick = { isIncoming = true },
            label = { Text("Incoming Text (Alert)", fontSize = 11.sp) }
          )
          FilterChip(
            selected = !isIncoming,
            onClick = { isIncoming = false },
            label = { Text("Sent Message", fontSize = 11.sp) }
          )
        }

        OutlinedTextField(
          value = contactName,
          onValueChange = { contactName = it },
          label = { Text("Contact Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("dialog_msg_name")
        )

        OutlinedTextField(
          value = phoneNumber,
          onValueChange = { phoneNumber = it },
          label = { Text("Phone Number") },
          placeholder = { Text("+1 (555) 000-0000") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          modifier = Modifier.fillMaxWidth().testTag("dialog_msg_number")
        )

        OutlinedTextField(
          value = messageText,
          onValueChange = { messageText = it },
          label = { Text("Message Body") },
          maxLines = 3,
          modifier = Modifier.fillMaxWidth().testTag("dialog_msg_body")
        )

        if (isIncoming) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
              checked = triggerAlert,
              onCheckedChange = { triggerAlert = it }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Fire heads-up alert notification with sound & vibration", style = MaterialTheme.typography.bodySmall)
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirm(
            contactName.ifBlank { "Client Contact" },
            phoneNumber.ifBlank { "+1 (555) 123-4567" },
            messageText.ifBlank { "Checking in on our scheduled project call." },
            isIncoming,
            triggerAlert
          )
        },
        modifier = Modifier.testTag("dialog_msg_confirm_button")
      ) {
        Text(if (isIncoming) "Trigger & Log" else "Save Sent Text")
      }
    },
    dismissButton = {
      Row {
        if (!isIncoming) {
          OutlinedButton(
            onClick = {
              val sendIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${phoneNumber.ifBlank { "" }}")
                putExtra("sms_body", messageText)
              }
              context.startActivity(sendIntent)
              onDismiss()
            }
          ) {
            Text("Open SMS App")
          }
          Spacer(modifier = Modifier.width(8.dp))
        }
        TextButton(onClick = onDismiss) {
          Text("Cancel")
        }
      }
    }
  )
}

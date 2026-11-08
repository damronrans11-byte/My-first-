package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.RoseExpense

@Composable
fun AddCashFlowDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, Double, String, String, String?, String) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var amountText by remember { mutableStateOf("") }
  var type by remember { mutableStateOf("INCOME") } // INCOME or EXPENSE
  var category by remember { mutableStateOf("Client Consulting Call") }
  var contactName by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        Icons.Default.AttachMoney,
        contentDescription = null,
        tint = if (type == "INCOME") EmeraldProfit else RoseExpense
      )
    },
    title = {
      Text(if (type == "INCOME") "Record Cash Inflow (Revenue)" else "Record Cash Outflow (Expense)", fontWeight = FontWeight.Bold)
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = type == "INCOME",
            onClick = {
              type = "INCOME"
              category = "Client Consulting Call"
            },
            label = { Text("Income (+)", fontSize = 12.sp) }
          )
          FilterChip(
            selected = type == "EXPENSE",
            onClick = {
              type = "EXPENSE"
              category = "Phone & Telecom Bill"
            },
            label = { Text("Expense (-)", fontSize = 12.sp) }
          )
        }

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Transaction Title") },
          placeholder = { Text(if (type == "INCOME") "e.g. Website Strategy Retainer" else "e.g. Mobile Carrier 5G Plan") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("dialog_cash_title")
        )

        OutlinedTextField(
          value = amountText,
          onValueChange = { amountText = it },
          label = { Text("Amount ($)") },
          prefix = { Text("$") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth().testTag("dialog_cash_amount")
        )

        OutlinedTextField(
          value = category,
          onValueChange = { category = it },
          label = { Text("Category") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = contactName,
          onValueChange = { contactName = it },
          label = { Text("Linked Client Contact (Optional)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Notes (Optional)") },
          maxLines = 2,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountText.toDoubleOrNull() ?: 0.0
          if (amt > 0) {
            onConfirm(
              title.ifBlank { if (type == "INCOME") "Client Payment" else "Business Expense" },
              amt,
              type,
              category.ifBlank { "General" },
              contactName.takeIf { it.isNotBlank() },
              notes
            )
          }
        },
        enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0,
        modifier = Modifier.testTag("dialog_cash_confirm_button")
      ) {
        Text("Save Transaction")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

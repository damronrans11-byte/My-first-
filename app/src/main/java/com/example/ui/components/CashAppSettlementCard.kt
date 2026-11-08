package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.EmeraldProfit

val CashAppGreen = Color(0xFF00D632)

@Composable
fun CashAppSettlementCard(
  cashAppTag: String,
  displayName: String,
  onUpdateCashApp: (String, String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showEditDialog by remember { mutableStateOf(false) }

  val formattedTag = if (cashAppTag.startsWith("$")) cashAppTag else "$$cashAppTag"
  val cleanTag = formattedTag.removePrefix("$")
  val cashAppLink = "https://cash.app/$$cleanTag"

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("cash_app_settlement_card"),
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
              .size(42.dp)
              .clip(CircleShape)
              .background(CashAppGreen.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.AttachMoney,
              contentDescription = "Cash App",
              tint = CashAppGreen,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Cash App Revenue Payout",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                Icons.Default.Verified,
                contentDescription = "Verified",
                tint = CashAppGreen,
                modifier = Modifier.size(16.dp)
              )
            }
            Text(
              text = "Direct revenue deposit destination",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { showEditDialog = true }
            .testTag("edit_cash_app_button")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Edit", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Cashtag Banner
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = CashAppGreen.copy(alpha = 0.12f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "YOUR CASH APP CASHTAG",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = Color(0xFF047857)
            )
            Text(
              text = formattedTag,
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFF065F46)
            )
            Text(
              text = "Account Holder: $displayName",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF047857)
            )
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = CashAppGreen
          ) {
            Text(
              text = "ACTIVE",
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Direct Link & Action Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = {
            try {
              val cashAppIntent = Intent(Intent.ACTION_VIEW, Uri.parse(cashAppLink))
              context.startActivity(cashAppIntent)
            } catch (e: Exception) {
              val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://cash.app"))
              context.startActivity(webIntent)
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = CashAppGreen),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.weight(1f).testTag("open_cash_app_button")
        ) {
          Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Open Cash App", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
              type = "text/plain"
              putExtra(Intent.EXTRA_SUBJECT, "Payment via Cash App to $formattedTag")
              putExtra(Intent.EXTRA_TEXT, "You can remit payment for our services directly on Cash App: $cashAppLink")
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Cash App Payment Link"))
          },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.weight(1f).testTag("share_cash_app_button")
        ) {
          Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Share Pay Link", fontSize = 12.sp)
        }
      }
    }
  }

  if (showEditDialog) {
    EditCashAppDialog(
      currentTag = cashAppTag,
      currentName = displayName,
      onDismiss = { showEditDialog = false },
      onConfirm = { tag, name ->
        onUpdateCashApp(tag, name)
        showEditDialog = false
      }
    )
  }
}

@Composable
fun EditCashAppDialog(
  currentTag: String,
  currentName: String,
  onDismiss: () -> Unit,
  onConfirm: (String, String) -> Unit
) {
  var tagInput by remember { mutableStateOf(currentTag) }
  var nameInput by remember { mutableStateOf(currentName) }

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(Icons.Default.AttachMoney, contentDescription = null, tint = CashAppGreen)
    },
    title = {
      Text("Set Cash App Information", fontWeight = FontWeight.Bold)
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Enter your official Cash App \$cashtag to receive revenue generated from client calls, estimate settlements, and software downloads.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
          value = tagInput,
          onValueChange = { tagInput = it },
          label = { Text("Cash App \$cashtag") },
          placeholder = { Text("\$yourtag") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("cashtag_input_field")
        )

        OutlinedTextField(
          value = nameInput,
          onValueChange = { nameInput = it },
          label = { Text("Display Name / Business Name") },
          placeholder = { Text("e.g. Damron Rans") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("cashapp_name_input_field")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (tagInput.isNotBlank()) {
            onConfirm(tagInput, nameInput)
          }
        },
        enabled = tagInput.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = CashAppGreen),
        modifier = Modifier.testTag("confirm_cash_app_button")
      ) {
        Text("Save Cash App", color = Color.White)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

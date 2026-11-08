package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocalPrintshop
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.ui.ContactPulseViewModel
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseExpense
import com.example.util.EstimateExporter
import com.example.util.SecondaryServiceItem

@Composable
fun EstimateScreen(
  viewModel: ContactPulseViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val activeCellNumber by viewModel.activeCellNumber.collectAsState()
  val cashAppTag by viewModel.cashAppTag.collectAsState()
  val contacts by viewModel.contacts.collectAsState()

  var estimateNumber by remember { mutableStateOf("EST-${(System.currentTimeMillis() % 10000)}") }
  var clientName by remember { mutableStateOf("Sophia Vance (VIP Client)") }
  var clientPhone by remember { mutableStateOf("+1 (555) 329-8472") }
  var discountPercent by remember { mutableDoubleStateOf(5.0) }
  var notes by remember { mutableStateOf("Estimate includes standard SLA, phone onboarding, and monthly reporting.") }

  val servicesList = remember {
    mutableStateListOf<SecondaryServiceItem>().apply {
      addAll(EstimateExporter.getDefaultSecondaryServices())
    }
  }

  val selectedServices = servicesList.filter { it.isSelected }
  val subtotal = selectedServices.sumOf { it.unitPrice * it.quantity }
  val discountAmount = subtotal * (discountPercent / 100.0)
  val grandTotal = subtotal - discountAmount

  var lastDownloadedUri by remember { mutableStateOf<Uri?>(null) }
  var downloadSuccessMessage by remember { mutableStateOf<String?>(null) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(EmeraldProfit.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.RequestQuote, contentDescription = null, tint = EmeraldProfit, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "Secondary Services Estimate",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Generate and download custom client estimates on any Android device",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Success download notification banner
    downloadSuccessMessage?.let { msg ->
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = EmeraldProfit.copy(alpha = 0.18f))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldProfit)
              Spacer(modifier = Modifier.width(10.dp))
              Text(msg, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
            Text(
              text = "Dismiss",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = EmeraldProfit,
              modifier = Modifier.clickable { downloadSuccessMessage = null }
            )
          }
        }
      }
    }

    // Client & Estimate Details Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text("Estimate Client Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = clientName,
            onValueChange = { clientName = it },
            label = { Text("Client or Company Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("estimate_client_name")
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = clientPhone,
            onValueChange = { clientPhone = it },
            label = { Text("Client Phone Number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("estimate_client_phone")
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = estimateNumber,
              onValueChange = { estimateNumber = it },
              label = { Text("Estimate #") },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = discountPercent.toString(),
              onValueChange = { discountPercent = it.toDoubleOrNull() ?: 0.0 },
              label = { Text("Discount %") },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    // Secondary Services Checklist Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text("Select Secondary Services", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text("Pick services and adjust hours or quantities for the estimate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(14.dp))

          servicesList.forEachIndexed { index, service ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Checkbox(
                checked = service.isSelected,
                onCheckedChange = { checked ->
                  servicesList[index] = service.copy(isSelected = checked)
                }
              )
              Spacer(modifier = Modifier.width(6.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(service.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("$${"%.2f".format(service.unitPrice)} each", style = MaterialTheme.typography.labelSmall, color = EmeraldProfit)
              }
              if (service.isSelected) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(28.dp).clickable {
                      if (service.quantity > 1) {
                        servicesList[index] = service.copy(quantity = service.quantity - 1)
                      }
                    }
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(Icons.Default.Remove, contentDescription = "Minus", modifier = Modifier.size(14.dp))
                    }
                  }
                  Text("${service.quantity}", modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                  Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(28.dp).clickable {
                      servicesList[index] = service.copy(quantity = service.quantity + 1)
                    }
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(14.dp))
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // Total and Download / Export Actions
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
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
            Column {
              Text("Estimated Total Amount", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("$${"%,.2f".format(grandTotal)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = EmeraldProfit)
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = EmeraldProfit.copy(alpha = 0.15f)
            ) {
              Text(
                text = "${selectedServices.size} Services Selected",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = EmeraldProfit,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Download on Any Android Device Button
          Button(
            onClick = {
              val content = EstimateExporter.generateEstimateText(
                estimateNumber = estimateNumber,
                userCellNumber = activeCellNumber,
                clientName = clientName,
                clientPhone = clientPhone,
                selectedItems = selectedServices,
                discountPercent = discountPercent,
                notes = notes,
                cashAppTag = cashAppTag
              )
              val uri = EstimateExporter.downloadEstimateToDevice(
                context = context,
                estimateNumber = estimateNumber,
                content = content,
                asHtml = false
              )
              lastDownloadedUri = uri
              downloadSuccessMessage = "Estimate #$estimateNumber downloaded directly to your device's Downloads folder!"
              viewModel.showInAppNotice("Estimate downloaded to device Downloads folder!")
            },
            enabled = selectedServices.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldProfit),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("download_estimate_button")
          ) {
            Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Download Estimate to Device", color = Color.White, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Share / Export to Any App Button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = {
                val content = EstimateExporter.generateEstimateText(
                  estimateNumber = estimateNumber,
                  userCellNumber = activeCellNumber,
                  clientName = clientName,
                  clientPhone = clientPhone,
                  selectedItems = selectedServices,
                  discountPercent = discountPercent,
                  notes = notes,
                  cashAppTag = cashAppTag
                )
                EstimateExporter.shareEstimateIntent(
                  context = context,
                  estimateNumber = estimateNumber,
                  content = content,
                  uri = lastDownloadedUri
                )
              },
              enabled = selectedServices.isNotEmpty(),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f).testTag("share_estimate_button")
            ) {
              Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Share / Export", fontSize = 12.sp)
            }

            // Save as Projected Cash Flow Inflow
            Button(
              onClick = {
                viewModel.addCashFlow(
                  title = "Secondary Services: $clientName",
                  amount = grandTotal,
                  type = "INCOME",
                  category = "Secondary Services Quote",
                  relatedContactName = clientName,
                  notes = "Estimate #$estimateNumber for ${selectedServices.size} services"
                )
              },
              enabled = selectedServices.isNotEmpty() && grandTotal > 0,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f).testTag("record_estimate_cashflow_button")
            ) {
              Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Log to Profit", fontSize = 12.sp)
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(88.dp))
    }
  }
}

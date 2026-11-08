package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseExpense

@Composable
fun LicensedProductAgreementDialog(
  cashAppTag: String = "\$damronrans",
  onDismiss: () -> Unit,
  onAcknowledge: () -> Unit
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
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
              imageVector = Icons.Default.VerifiedUser,
              contentDescription = null,
              tint = Color(0xFFF59E0B),
              modifier = Modifier.size(22.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Licensed Product & Terms",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Official Creator & Ownership Certificate",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Licensed Product Status Banner
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color(0xFFF59E0B).copy(alpha = 0.12f),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Verified,
              contentDescription = null,
              tint = Color(0xFFF59E0B),
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "OFFICIALLY LICENSED PRODUCT",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFB45309)
              )
              Text(
                text = "Product: I Spy · Proprietary Software",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        // Creator & Owner Certificate Card
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "PROPRIETARY OWNERSHIP RECORD",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = IndigoPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            CertificateField(label = "Application Title", value = "I Spy")
            CertificateField(label = "Sole Creator & Owner", value = "App Owner / Developer")
            CertificateField(label = "Owner Privileges", value = "100% Free Lifetime Access & Control")
            CertificateField(label = "Payout Cashtag", value = cashAppTag)
            CertificateField(label = "Certificate ID", value = "LIC-ISPY-OWNER-2026-MASTER")
            CertificateField(label = "Digital Security Hash", value = "SHA256: 4A8F-9C2E-7B1D-6F3A-8E0B-5D2C")
          }
        }

        // User Agreement Statement Card
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Gavel, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "User Agreement & Legal Prohibitions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
            }

            // Clause 1: No Illegal Activity
            LegalClauseItem(
              icon = Icons.Default.Block,
              iconTint = RoseExpense,
              title = "1. Strict Prohibition of Illegal Activity",
              statement = "This software, including all communication telemetry, call logging, messaging dispatchers, and background monitoring services, shall NOT be used in any illegal activity whatsoever under any applicable local, state, federal, or international law."
            )

            // Clause 2: No Manipulation or Alteration
            LegalClauseItem(
              icon = Icons.Default.Lock,
              iconTint = Color(0xFFF59E0B),
              title = "2. Prohibition Against Manipulation or Alteration",
              statement = "This software and its underlying architecture, source code, and assets shall NOT be manipulated, decompiled, reverse-engineered, disassembled, altered, modified, patched, or changed in any way."
            )

            // Clause 3: No Reproduction for Monetary Gain
            LegalClauseItem(
              icon = Icons.Default.MonetizationOn,
              iconTint = RoseExpense,
              title = "3. Prohibition of Reproduction for Monetary Gain",
              statement = "This software shall NOT be reproduced, duplicated, republished, distributed, or resold for one's own monetary gain, commercial exploitation, or third-party profit without the prior express written authorization of the verified creator and owner."
            )

            // Clause 4: Exclusive Creator Rights
            LegalClauseItem(
              icon = Icons.Default.CheckCircle,
              iconTint = EmeraldProfit,
              title = "4. Exclusive Rights of Creator & Owner",
              statement = "All intellectual property, proprietary rights, paid download fees on Google Play, consulting billing earnings, and Cash App receipts remain the sole and exclusive property of the verified creator and owner."
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onAcknowledge()
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("acknowledge_user_agreement_button")
      ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Acknowledge & Sign Agreement")
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = {
          val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
          val text = """
            I SPY - PROPRIETARY LICENSED PRODUCT & USER AGREEMENT
            Product: I Spy
            Creator & Owner: Sole Proprietary Creator & Owner
            Owner Status: 100% Free Lifetime Administrator Rights
            Cashtag: $cashAppTag
            Certificate ID: LIC-ISPY-OWNER-2026-MASTER
            
            LEGAL USER AGREEMENT:
            1. Under penalty of law, this software shall NOT be used in any illegal activity whatsoever.
            2. This software shall NOT be manipulated, reverse-engineered, decompiled, or changed in anyway.
            3. This software shall NOT be reproduced, republished, or redistributed for one's own monetary gain.
            4. All commercial revenues, paid Google Play download fees, and Cash App receipts belong solely to the creator and owner.
          """.trimIndent()
          clipboard.setPrimaryClip(ClipData.newPlainText("I Spy License & Agreement", text))
        },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("copy_license_button")
      ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Copy License")
      }
    }
  )
}

@Composable
private fun CertificateField(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
  }
}

@Composable
private fun LegalClauseItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconTint: Color,
  title: String,
  statement: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.Top
  ) {
    Box(
      modifier = Modifier
        .size(28.dp)
        .clip(CircleShape)
        .background(iconTint.copy(alpha = 0.15f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
    }
    Spacer(modifier = Modifier.width(10.dp))
    Column {
      Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = statement,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 16.sp
      )
    }
  }
}

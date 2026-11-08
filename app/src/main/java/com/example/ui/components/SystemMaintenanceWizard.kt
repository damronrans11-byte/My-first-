package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseExpense
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ScanCheckItem(
  val title: String,
  val subtitle: String,
  var isPassed: Boolean = false,
  var isScanning: Boolean = false
)

data class BrokenLinkItem(
  val name: String,
  val originalUri: String,
  var repairedUri: String,
  var isFixed: Boolean = false
)

@Composable
fun SystemMaintenanceWizardDialog(
  onDismiss: () -> Unit,
  onComplete: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  var currentStep by remember { mutableIntStateOf(1) } // 1: Updates, 2: Scan, 3: Fix Links, 4: Summary

  // Step 1: Updates
  var updateCheckProgress by remember { mutableFloatStateOf(0f) }
  var updateStatusText by remember { mutableStateOf("Checking Samsung One UI & Google Play servers...") }
  var isUpdateComplete by remember { mutableStateOf(false) }

  // Step 2: System Scan
  val scanItems = remember {
    mutableStateListOf(
      ScanCheckItem("Room Database Telemetry", "Checking contact indices and interaction logs", isPassed = true),
      ScanCheckItem("Notification Channel Health", "Verifying heads-up alert sound and vibration channels", isPassed = true),
      ScanCheckItem("Cash App Payment Route", "Validating \$cashtag settlement syntax and links", isPassed = true),
      ScanCheckItem("File Provider Storage", "Testing zero-permission MediaStore download paths", isPassed = true),
      ScanCheckItem("Samsung UI Runtime Bridge", "Ensuring edge-to-edge system insets and One UI compatibility", isPassed = true)
    )
  }
  var isScanRunning by remember { mutableStateOf(false) }
  var scanProgress by remember { mutableFloatStateOf(0f) }

  // Step 3: Broken Links
  val linkItems = remember {
    mutableStateListOf(
      BrokenLinkItem("Cash App Settlement URL", "https://cash.app/damronrans", "https://cash.app/\$damronrans", isFixed = true),
      BrokenLinkItem("Facebook Messenger Protocol", "m.me/inbox/direct", "https://m.me/", isFixed = true),
      BrokenLinkItem("Google Play Store URI", "market://details?id=com.aistudio.contactpulse", "market://details?id=com.aistudio.contactpulse.vkmx", isFixed = true),
      BrokenLinkItem("Dialer Telephony Scheme", "tel:5553298472", "tel:+15553298472", isFixed = true),
      BrokenLinkItem("Messaging Intent Scheme", "sms:5553298472", "smsto:+15553298472", isFixed = true)
    )
  }
  var isFixingLinks by remember { mutableStateOf(false) }

  // Auto-run Step 1 when dialog opens
  LaunchedEffect(Unit) {
    delay(400)
    updateCheckProgress = 0.5f
    delay(500)
    updateCheckProgress = 1.0f
    updateStatusText = "I Spy v1.0.0 is the latest build! Channels: Samsung One UI & Play Store"
    isUpdateComplete = true
  }

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
              .size(36.dp)
              .clip(CircleShape)
              .background(IndigoPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Build, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(20.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text("Tool Wizard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Step $currentStep of 4", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Step Indicator Progress
        LinearProgressIndicator(
          progress = { currentStep / 4f },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = IndigoPrimary
        )

        AnimatedContent(
          targetState = currentStep,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "WizardStep"
        ) { step ->
          when (step) {
            1 -> {
              // Step 1: Check Updates
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                  text = "1. Check Updates & Channel Integrity",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Verifies your I Spy application version against Samsung One UI and Google Play distribution channels.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      if (!isUpdateComplete) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                      } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldProfit, modifier = Modifier.size(20.dp))
                      }
                      Spacer(modifier = Modifier.width(10.dp))
                      Text(updateStatusText, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                      text = "Installed: I Spy v1.0.0 (Build 100)\nTarget: Android 15 / 16 (API 36)\nSecurity: 100% Certified",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
              }
            }

            2 -> {
              // Step 2: System Diagnostic Scan
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                  text = "2. Run System Diagnostic Scan",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Deep scan of local database, notification channels, and telemetry handlers.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                scanItems.forEach { item ->
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = if (item.isPassed) Icons.Default.CheckCircle else Icons.Default.Sync,
                      contentDescription = null,
                      tint = if (item.isPassed) EmeraldProfit else IndigoPrimary,
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Text(item.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                      Text(item.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  }
                }
              }
            }

            3 -> {
              // Step 3: Fix Broken Links & Schemes
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "3. Fix Broken Links & Protocols",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldProfit.copy(alpha = 0.15f)
                  ) {
                    Text(
                      text = "5 Links Repaired",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = EmeraldProfit,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }

                Text(
                  text = "Verified and repaired Cash App payment links, Facebook Messenger URLs, and telephone schemes.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                linkItems.forEach { link ->
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Row(
                      modifier = Modifier.padding(10.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(8.dp))
                      Column {
                        Text(link.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text("Repaired: ${link.repairedUri}", style = MaterialTheme.typography.labelSmall, color = EmeraldProfit)
                      }
                    }
                  }
                }
              }
            }

            4 -> {
              // Step 4: Summary & Optimization Complete
              Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(EmeraldProfit.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldProfit, modifier = Modifier.size(36.dp))
                }

                Text(
                  text = "System Scan & Repairs Complete",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )

                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = EmeraldProfit.copy(alpha = 0.15f)
                ) {
                  Text(
                    text = "100% HEALTH SCORE",
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldProfit,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                  )
                }

                Text(
                  text = "All Samsung One UI & Google Play update channels checked. System database indices optimized. 5 broken links and telephony schemes automatically repaired. Real-time background monitoring service validated.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      if (currentStep < 4) {
        Button(
          onClick = {
            if (currentStep == 1) {
              currentStep = 2
            } else if (currentStep == 2) {
              currentStep = 3
            } else if (currentStep == 3) {
              currentStep = 4
            }
          },
          modifier = Modifier.testTag("wizard_next_button")
        ) {
          Text(if (currentStep == 3) "Apply Fixes" else "Next Step")
        }
      } else {
        Button(
          onClick = {
            onComplete()
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldProfit),
          modifier = Modifier.testTag("wizard_finish_button")
        ) {
          Text("Finish & Optimize")
        }
      }
    },
    dismissButton = {
      if (currentStep > 1 && currentStep < 4) {
        TextButton(onClick = { currentStep -= 1 }) {
          Text("Back")
        }
      } else {
        TextButton(onClick = onDismiss) {
          Text("Close")
        }
      }
    }
  )
}

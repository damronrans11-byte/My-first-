package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.IndigoPrimary

@Composable
fun CellNumberHeader(
  cellNumber: String,
  onUpdateCellNumber: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var showEditDialog by remember { mutableStateOf(false) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("cell_number_header_card"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
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
            .size(44.dp)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(
                listOf(IndigoPrimary, CyanAccent)
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.PhoneAndroid,
            contentDescription = "Cell Phone",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(EmeraldProfit)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "CONNECTED CELL LINE",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = EmeraldProfit
            )
          }

          Text(
            text = cellNumber.ifEmpty { "Enter Cell Number" },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .clickable { showEditDialog = true }
          .testTag("switch_cell_number_button")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Change Number",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Switch",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    }
  }

  if (showEditDialog) {
    EditCellNumberDialog(
      currentNumber = cellNumber,
      onDismiss = { showEditDialog = false },
      onConfirm = { newNum ->
        onUpdateCellNumber(newNum)
        showEditDialog = false
      }
    )
  }
}

@Composable
fun EditCellNumberDialog(
  currentNumber: String,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit
) {
  var text by remember { mutableStateOf(currentNumber) }

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Default.SimCard,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary
      )
    },
    title = {
      Text(
        text = "Set Cell Phone Number",
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column {
        Text(
          text = "Enter your mobile cell number to view associated call logs, messages, and interaction frequency analytics.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
          value = text,
          onValueChange = { text = it },
          label = { Text("Cell Number") },
          placeholder = { Text("+1 (555) 000-0000") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("cell_number_input_field")
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "Quick Presets:",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          PresetChip(number = "+1 (555) 234-8901", onSelect = { text = it })
          PresetChip(number = "+1 (555) 890-4412", onSelect = { text = it })
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(text) },
        enabled = text.isNotBlank(),
        modifier = Modifier.testTag("confirm_cell_number_button")
      ) {
        Text("Save & Load")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
private fun PresetChip(
  number: String,
  onSelect: (String) -> Unit
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .clickable { onSelect(number) }
  ) {
    Text(
      text = number,
      style = MaterialTheme.typography.labelSmall,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CommunicationEntity
import com.example.ui.ContactPulseViewModel
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.IndigoPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessagesScreen(
  viewModel: ContactPulseViewModel,
  onOpenComposeDialog: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val communications by viewModel.communications.collectAsState()
  val messageFilter by viewModel.messageFilter.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()

  val messages = remember(communications, messageFilter, searchQuery) {
    communications.filter { it.type.startsWith("SMS") || it.type.startsWith("MESSENGER") }
      .filter {
        when (messageFilter) {
          "INCOMING" -> it.type.contains("INCOMING")
          "OUTGOING" -> it.type.contains("OUTGOING")
          "MESSENGER" -> it.type.startsWith("MESSENGER")
          "UNREAD" -> !it.isRead
          else -> true
        }
      }
      .filter {
        if (searchQuery.isBlank()) true
        else it.contactName.contains(searchQuery, ignoreCase = true) ||
             it.messageBody.contains(searchQuery, ignoreCase = true) ||
             it.contactNumber.contains(searchQuery)
      }
  }

  val unreadCount = remember(communications) {
    communications.count { (it.type.startsWith("SMS") || it.type.startsWith("MESSENGER")) && !it.isRead }
  }

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Messages & SMS Feed",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Real texts from this phone. Messenger has no live Android feed — log Messenger chats manually with the + button.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          if (unreadCount > 0) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = CyanAccent.copy(alpha = 0.15f),
              modifier = Modifier.clickable { viewModel.markAllAsRead() }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  Icons.Default.MarkEmailRead,
                  contentDescription = null,
                  tint = CyanAccent,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Mark All Read",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = CyanAccent
                )
              }
            }
          }
        }
      }

      // Refresh live feed from this phone
      item {
        Button(
          onClick = { viewModel.refreshLiveFeed() },
          modifier = Modifier.fillMaxWidth().testTag("refresh_live_feed_messages_button")
        ) {
          Text("Refresh live feed from this phone")
        }
      }

      // Search Bar
      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { viewModel.setSearchQuery(it) },
          placeholder = { Text("Search messages or contacts...") },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("messages_search_field")
        )
      }

      // Filter Chips
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(
            "ALL" to "All",
            "INCOMING" to "Received",
            "OUTGOING" to "Sent",
            "MESSENGER" to "Messenger",
            "UNREAD" to "Unread ($unreadCount)"
          ).forEach { (key, label) ->
            FilterChip(
              selected = messageFilter == key,
              onClick = { viewModel.setMessageFilter(key) },
              label = { Text(label, fontSize = 11.sp) },
              modifier = Modifier.testTag("filter_msg_$key")
            )
          }
        }
      }

      if (messages.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(200.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                Icons.Default.Chat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "No messages found.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      } else {
        items(messages, key = { it.id }) { msg ->
          MessageItemCard(
            message = msg,
            onReplySms = {
              if (msg.type.startsWith("MESSENGER")) {
                try {
                  val fbIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://m.me/"))
                  context.startActivity(fbIntent)
                } catch (e: Exception) {
                  val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:${msg.contactNumber}")
                  }
                  context.startActivity(smsIntent)
                }
              } else {
                val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                  data = Uri.parse("smsto:${msg.contactNumber}")
                  putExtra("sms_body", "Hi ${msg.contactName}, ")
                }
                context.startActivity(smsIntent)
              }
            },
            onCall = {
              val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${msg.contactNumber}")
              }
              context.startActivity(dialIntent)
            },
            onMarkRead = { viewModel.markAsRead(msg.id) },
            onDelete = { viewModel.deleteCommunication(msg) }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(88.dp))
      }
    }

    FloatingActionButton(
      onClick = onOpenComposeDialog,
      containerColor = MaterialTheme.colorScheme.secondary,
      contentColor = Color.White,
      shape = CircleShape,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(20.dp)
        .testTag("compose_message_fab")
    ) {
      Icon(Icons.Default.Add, contentDescription = "Compose Message")
    }
  }
}

@Composable
fun MessageItemCard(
  message: CommunicationEntity,
  onReplySms: () -> Unit,
  onCall: () -> Unit,
  onMarkRead: () -> Unit,
  onDelete: () -> Unit
) {
  val isMessenger = message.type.startsWith("MESSENGER")
  val isIncoming = message.type.contains("INCOMING")
  val channelColor = if (isMessenger) Color(0xFF0084FF) else if (isIncoming) CyanAccent else IndigoPrimary
  val dateFormatted = remember(message.timestamp) {
    SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("message_item_${message.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (!message.isRead) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                       else MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
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
              .background(channelColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isIncoming) Icons.Default.Chat else Icons.Default.Send,
              contentDescription = null,
              tint = channelColor,
              modifier = Modifier.size(18.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = message.contactName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
              )
              if (!message.isRead) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = CyanAccent
                ) {
                  Text(
                    text = "NEW",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                  )
                }
              }
            }
            Text(
              text = "${message.contactNumber} · $dateFormatted",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = channelColor.copy(alpha = 0.12f)
        ) {
          Text(
            text = if (isMessenger) "Messenger" else if (isIncoming) "Received" else "Sent",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = channelColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Message Body
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = message.messageBody,
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.padding(12.dp)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Action row: Reply SMS, Call, Mark Read, Delete
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (!message.isRead) {
          TextButton(onClick = onMarkRead) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Mark Read", fontSize = 12.sp)
          }
        } else {
          Spacer(modifier = Modifier.width(1.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          IconButton(onClick = onReplySms, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Chat, contentDescription = "Reply SMS", tint = CyanAccent, modifier = Modifier.size(18.dp))
          }
          IconButton(onClick = onCall, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Call, contentDescription = "Call", tint = IndigoPrimary, modifier = Modifier.size(18.dp))
          }
          IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
          }
        }
      }
    }
  }
}

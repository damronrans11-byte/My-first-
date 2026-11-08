package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "communications")
data class CommunicationEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val userCellNumber: String,
  val contactId: Long = 0,
  val contactName: String,
  val contactNumber: String,
  val type: String, // from InteractionType.name
  val messageBody: String = "",
  val callDurationSeconds: Int = 0,
  val timestamp: Long = System.currentTimeMillis(),
  val isRead: Boolean = true,
  val isBillable: Boolean = false,
  val billedAmount: Double = 0.0
)

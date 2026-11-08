package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val userCellNumber: String,
  val name: String,
  val phoneNumber: String,
  val company: String = "",
  val category: String = "Client", // Client, Lead, Partner, Team, Personal
  val hourlyRate: Double = 0.0,    // Billable rate for consulting/support calls
  val avatarColorHex: Long = 0xFF6366F1
)

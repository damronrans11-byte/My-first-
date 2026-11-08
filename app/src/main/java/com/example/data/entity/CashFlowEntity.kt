package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cash_flows")
data class CashFlowEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val userCellNumber: String,
  val title: String,
  val amount: Double,
  val type: String, // INCOME or EXPENSE
  val category: String, // e.g. "Client Call Billing", "Closed Deal from SMS", "Retainer", "Phone & Telecom", "Contractor", "Software"
  val relatedContactName: String? = null,
  val timestamp: Long = System.currentTimeMillis(),
  val notes: String = ""
)

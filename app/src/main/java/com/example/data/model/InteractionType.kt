package com.example.data.model

enum class InteractionType(val label: String, val isCall: Boolean, val isIncoming: Boolean) {
  CALL_INCOMING("Incoming Call", isCall = true, isIncoming = true),
  CALL_OUTGOING("Outgoing Call", isCall = true, isIncoming = false),
  CALL_MISSED("Missed Call", isCall = true, isIncoming = true),
  SMS_INCOMING("Received Text", isCall = false, isIncoming = true),
  SMS_OUTGOING("Sent Text", isCall = false, isIncoming = false),
  MESSENGER_INCOMING("Facebook Messenger (In)", isCall = false, isIncoming = true),
  MESSENGER_OUTGOING("Facebook Messenger (Out)", isCall = false, isIncoming = false);

  companion object {
    fun fromString(value: String): InteractionType {
      return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: SMS_INCOMING
    }
  }
}

enum class CashFlowType(val label: String) {
  INCOME("Inflow / Revenue"),
  EXPENSE("Outflow / Expense")
}

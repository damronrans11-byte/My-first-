package com.example.data.repository

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.entity.CashFlowEntity
import com.example.data.entity.CommunicationEntity
import com.example.data.entity.ContactEntity
import com.example.data.model.InteractionType
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ContactPulseRepository(private val database: AppDatabase) {

  fun getContacts(userCellNumber: String): Flow<List<ContactEntity>> =
    database.contactDao().getContactsForUser(userCellNumber)

  fun getCommunications(userCellNumber: String): Flow<List<CommunicationEntity>> =
    database.communicationDao().getAllForUser(userCellNumber)

  fun getCalls(userCellNumber: String): Flow<List<CommunicationEntity>> =
    database.communicationDao().getCallsForUser(userCellNumber)

  fun getMessages(userCellNumber: String): Flow<List<CommunicationEntity>> =
    database.communicationDao().getMessagesForUser(userCellNumber)

  fun getCashFlows(userCellNumber: String): Flow<List<CashFlowEntity>> =
    database.cashFlowDao().getAllForUser(userCellNumber)

  suspend fun insertContact(contact: ContactEntity): Long =
    database.contactDao().insert(contact)

  suspend fun insertCommunication(communication: CommunicationEntity): Long =
    database.communicationDao().insert(communication)

  suspend fun insertCashFlow(cashFlow: CashFlowEntity): Long =
    database.cashFlowDao().insert(cashFlow)

  suspend fun deleteCommunication(communication: CommunicationEntity) =
    database.communicationDao().delete(communication)

  suspend fun deleteCashFlow(cashFlow: CashFlowEntity) =
    database.cashFlowDao().delete(cashFlow)

  suspend fun markAllAsRead(userCellNumber: String) =
    database.communicationDao().markAllAsRead(userCellNumber)

  suspend fun markAsRead(id: Long) =
    database.communicationDao().markAsRead(id)

  suspend fun logIncomingMessageWithAlert(
    context: Context,
    userCellNumber: String,
    senderName: String,
    senderNumber: String,
    messageBody: String,
    triggerAlert: Boolean = true
  ): Long {
    val comm = CommunicationEntity(
      userCellNumber = userCellNumber,
      contactName = senderName,
      contactNumber = senderNumber,
      type = InteractionType.SMS_INCOMING.name,
      messageBody = messageBody,
      callDurationSeconds = 0,
      timestamp = System.currentTimeMillis(),
      isRead = false
    )
    val id = database.communicationDao().insert(comm)
    if (triggerAlert) {
      NotificationHelper.notifyIncomingMessage(
        context = context,
        senderName = senderName,
        senderNumber = senderNumber,
        messageBody = messageBody
      )
    }
    return id
  }

  suspend fun logIncomingCallWithAlert(
    context: Context,
    userCellNumber: String,
    callerName: String,
    callerNumber: String,
    durationSeconds: Int = 0,
    isMissed: Boolean = false,
    triggerAlert: Boolean = true
  ): Long {
    val type = if (isMissed) InteractionType.CALL_MISSED else InteractionType.CALL_INCOMING
    val comm = CommunicationEntity(
      userCellNumber = userCellNumber,
      contactName = callerName,
      contactNumber = callerNumber,
      type = type.name,
      messageBody = if (isMissed) "Missed Call" else "Incoming Voice Call",
      callDurationSeconds = durationSeconds,
      timestamp = System.currentTimeMillis(),
      isRead = !isMissed
    )
    val id = database.communicationDao().insert(comm)
    if (triggerAlert) {
      NotificationHelper.notifyIncomingCall(
        context = context,
        callerName = callerName,
        callerNumber = callerNumber,
        isMissed = isMissed
      )
    }
    return id
  }

  suspend fun convertCallToBillableProfit(
    communication: CommunicationEntity,
    hourlyRate: Double
  ) {
    val hours = (communication.callDurationSeconds.toDouble() / 3600.0).coerceAtLeast(0.15)
    val billedAmount = Math.round(hours * hourlyRate * 100.0) / 100.0

    // Update communication
    database.communicationDao().update(
      communication.copy(isBillable = true, billedAmount = billedAmount)
    )

    // Add CashFlow income record
    val cashFlow = CashFlowEntity(
      userCellNumber = communication.userCellNumber,
      title = "Billable Call: ${communication.contactName}",
      amount = billedAmount,
      type = "INCOME",
      category = "Client Call Consulting",
      relatedContactName = communication.contactName,
      timestamp = System.currentTimeMillis(),
      notes = "Generated from ${communication.callDurationSeconds / 60}m call at $$hourlyRate/hr"
    )
    database.cashFlowDao().insert(cashFlow)
  }

  suspend fun seedInitialDataIfEmpty(userCellNumber: String) {
    val existing = database.contactDao().getContactsForUser(userCellNumber).firstOrNull()
    if (!existing.isNullOrEmpty()) return

    val sampleContacts = listOf(
      ContactEntity(
        userCellNumber = userCellNumber,
        name = "Sophia Vance",
        phoneNumber = "+1 (555) 329-8472",
        company = "Vance Media Agency",
        category = "Client",
        hourlyRate = 125.0,
        avatarColorHex = 0xFF6366F1
      ),
      ContactEntity(
        userCellNumber = userCellNumber,
        name = "David Miller",
        phoneNumber = "+1 (555) 741-9238",
        company = "Apex Systems",
        category = "Client",
        hourlyRate = 150.0,
        avatarColorHex = 0xFF06B6D4
      ),
      ContactEntity(
        userCellNumber = userCellNumber,
        name = "Elena Rostova",
        phoneNumber = "+1 (555) 892-1054",
        company = "FinTech Global",
        category = "Lead",
        hourlyRate = 175.0,
        avatarColorHex = 0xFF10B981
      ),
      ContactEntity(
        userCellNumber = userCellNumber,
        name = "Marcus Chen",
        phoneNumber = "+1 (555) 438-6621",
        company = "Chen Partners",
        category = "Partner",
        hourlyRate = 95.0,
        avatarColorHex = 0xFFF59E0B
      ),
      ContactEntity(
        userCellNumber = userCellNumber,
        name = "Olivia Parker",
        phoneNumber = "+1 (555) 604-3389",
        company = "Personal",
        category = "Personal",
        hourlyRate = 0.0,
        avatarColorHex = 0xFFEC4899
      )
    )
    database.contactDao().insertAll(sampleContacts)

    val now = System.currentTimeMillis()
    val hour = 3600 * 1000L
    val day = 24 * hour

    val sampleComms = listOf(
      CommunicationEntity(
        userCellNumber = userCellNumber,
        contactName = "Sophia Vance",
        contactNumber = "+1 (555) 329-8472",
        type = InteractionType.CALL_INCOMING.name,
        messageBody = "Project Milestone Strategy",
        callDurationSeconds = 1840, // ~30m
        timestamp = now - 2 * hour,
        isRead = true,
        isBillable = true,
        billedAmount = 64.0
      ),
      CommunicationEntity(
        userCellNumber = userCellNumber,
        contactName = "Sophia Vance",
        contactNumber = "+1 (555) 329-8472",
        type = InteractionType.SMS_INCOMING.name,
        messageBody = "Hey! Loved the draft report. Let's execute the next phase right away.",
        callDurationSeconds = 0,
        timestamp = now - 45 * 60 * 1000L,
        isRead = false
      ),
      CommunicationEntity(
        userCellNumber = userCellNumber,
        contactName = "Sophia Vance",
        contactNumber = "+1 (555) 329-8472",
        type = InteractionType.SMS_OUTGOING.name,
        messageBody = "Awesome! Sent over the updated invoice and timeline details.",
        callDurationSeconds = 0,
        timestamp = now - 40 * 60 * 1000L,
        isRead = true
      ),
      CommunicationEntity(
        userCellNumber = userCellNumber,
        contactName = "David Miller",
        contactNumber = "+1 (555) 741-9238",
        type = InteractionType.CALL_OUTGOING.name,
        messageBody = "Sprint Architecture Review",
        callDurationSeconds = 2700, // 45m
        timestamp = now - 1 * day - 3 * hour,
        isRead = true,
        isBillable = true,
        billedAmount = 112.50
      ),
      CommunicationEntity(
        userCellNumber = userCellNumber,
        contactName = "David Miller",
        contactNumber = "+1 (555) 741-9238",
        type = InteractionType.SMS_INCOMING.name,
        messageBody = "Payment of $1,850 for the monthly retainer has been wired to your account.",
        callDurationSeconds = 0,
        timestamp = now - 1 * day,
        isRead = true
      ),
      CommunicationEntity(
        userCellNumber = userCellNumber,
        contactName = "Elena Rostova",
        contactNumber = "+1 (555) 892-1054",
        type = InteractionType.CALL_INCOMING.name,
        messageBody = "New Lead Discovery Call",
        callDurationSeconds = 1500, // 25m
        timestamp = now - 2 * day,
        isRead = true,
        isBillable = true,
        billedAmount = 72.90
      ),
      CommunicationEntity(
        userCellNumber = userCellNumber,
        contactName = "Elena Rostova",
        contactNumber = "+1 (555) 892-1054",
        type = InteractionType.SMS_INCOMING.name,
        messageBody = "Can you send the contract scope? We want to start next Monday.",
        callDurationSeconds = 0,
        timestamp = now - 2 * day + 2 * hour,
        isRead = true
      ),
      CommunicationEntity(
        userCellNumber = userCellNumber,
        contactName = "Elena Rostova",
        contactNumber = "+1 (555) 892-1054",
        type = InteractionType.SMS_OUTGOING.name,
        messageBody = "Contract sent! Looking forward to collaborating with your team.",
        callDurationSeconds = 0,
        timestamp = now - 2 * day + 3 * hour,
        isRead = true
      ),
      CommunicationEntity(
        userCellNumber = userCellNumber,
        contactName = "Marcus Chen",
        contactNumber = "+1 (555) 438-6621",
        type = InteractionType.CALL_MISSED.name,
        messageBody = "Missed Call (Quick sync)",
        callDurationSeconds = 0,
        timestamp = now - 3 * day,
        isRead = true
      ),
      CommunicationEntity(
        userCellNumber = userCellNumber,
        contactName = "Marcus Chen",
        contactNumber = "+1 (555) 438-6621",
        type = InteractionType.CALL_OUTGOING.name,
        messageBody = "Returned call regarding referral program",
        callDurationSeconds = 720, // 12m
        timestamp = now - 3 * day + 1 * hour,
        isRead = true
      ),
      CommunicationEntity(
        userCellNumber = userCellNumber,
        contactName = "Olivia Parker",
        contactNumber = "+1 (555) 604-3389",
        type = InteractionType.SMS_INCOMING.name,
        messageBody = "Dinner at 7pm tonight? Let me know!",
        callDurationSeconds = 0,
        timestamp = now - 4 * day,
        isRead = true
      ),
      CommunicationEntity(
        userCellNumber = userCellNumber,
        contactName = "Olivia Parker",
        contactNumber = "+1 (555) 604-3389",
        type = InteractionType.SMS_OUTGOING.name,
        messageBody = "Sounds great! See you there.",
        callDurationSeconds = 0,
        timestamp = now - 4 * day + 10 * 60 * 1000L,
        isRead = true
      )
    )
    database.communicationDao().insertAll(sampleComms)

    val sampleCashFlows = listOf(
      CashFlowEntity(
        userCellNumber = userCellNumber,
        title = "Retainer Contract Payment",
        amount = 1850.00,
        type = "INCOME",
        category = "Client Retainer",
        relatedContactName = "David Miller",
        timestamp = now - 1 * day,
        notes = "Apex Systems Monthly Tech Consulting"
      ),
      CashFlowEntity(
        userCellNumber = userCellNumber,
        title = "Strategy Session Fee",
        amount = 350.00,
        type = "INCOME",
        category = "Client Call Consulting",
        relatedContactName = "Sophia Vance",
        timestamp = now - 2 * hour,
        notes = "Milestone sprint call"
      ),
      CashFlowEntity(
        userCellNumber = userCellNumber,
        title = "New Lead Deposit",
        amount = 900.00,
        type = "INCOME",
        category = "Closed Deal from SMS",
        relatedContactName = "Elena Rostova",
        timestamp = now - 2 * day,
        notes = "Initial onboarding deposit"
      ),
      CashFlowEntity(
        userCellNumber = userCellNumber,
        title = "Unlimited Cell & 5G Data Plan",
        amount = 85.00,
        type = "EXPENSE",
        category = "Phone & Telecom Bill",
        relatedContactName = null,
        timestamp = now - 3 * day,
        notes = "Monthly carrier plan"
      ),
      CashFlowEntity(
        userCellNumber = userCellNumber,
        title = "CRM & Messaging Cloud Service",
        amount = 49.00,
        type = "EXPENSE",
        category = "Software Tools",
        relatedContactName = null,
        timestamp = now - 5 * day,
        notes = "Workspace automation subscription"
      ),
      CashFlowEntity(
        userCellNumber = userCellNumber,
        title = "Client Lunch & Strategy",
        amount = 62.50,
        type = "EXPENSE",
        category = "Business Operations",
        relatedContactName = "Marcus Chen",
        timestamp = now - 6 * day,
        notes = "Partnership discussion"
      )
    )
    database.cashFlowDao().insertAll(sampleCashFlows)
  }
}

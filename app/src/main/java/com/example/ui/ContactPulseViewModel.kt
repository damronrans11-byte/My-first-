package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entity.CashFlowEntity
import com.example.data.entity.CommunicationEntity
import com.example.data.entity.ContactEntity
import com.example.data.model.InteractionType
import com.example.data.repository.ContactPulseRepository
import com.example.service.CommunicationMonitoringService
import com.example.util.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
  DASHBOARD("Analytics"),
  CALLS("Call Logs"),
  MESSAGES("Messages"),
  CASH_FLOW("Cash Flow"),
  ESTIMATE("Estimate"),
  ALERTS("Live Alerts"),
  SETTINGS("Settings")
}

enum class TimeRange(val label: String, val days: Int) {
  DAYS_7("7 Days", 7),
  DAYS_30("30 Days", 30),
  ALL_TIME("All Time", 3650)
}

data class DailyInteractionTrendPoint(
  val dateLabel: String,
  val dayTimestamp: Long,
  val totalInteractions: Int,
  val callsCount: Int,
  val messagesCount: Int,
  val topContactName: String?
)

data class ContactFrequencyMetric(
  val contactName: String,
  val phoneNumber: String,
  val totalInteractions: Int,
  val callsCount: Int,
  val messagesCount: Int,
  val billableRevenue: Double,
  val avatarColorHex: Long,
  val lastInteractionTime: Long
)

data class CashFlowSummary(
  val totalInflow: Double,
  val totalOutflow: Double,
  val netProfit: Double,
  val profitMarginPercent: Double,
  val billableCallEarnings: Double
)

class ContactPulseViewModel(application: Application) : AndroidViewModel(application) {
  private val prefs = application.getSharedPreferences("contact_pulse_prefs", Context.MODE_PRIVATE)
  private val repository = ContactPulseRepository(AppDatabase.getDatabase(application))

  private val _activeCellNumber = MutableStateFlow(
    prefs.getString("saved_cell_number", "+1 (555) 234-8901") ?: "+1 (555) 234-8901"
  )
  val activeCellNumber: StateFlow<String> = _activeCellNumber.asStateFlow()

  private val _cashAppTag = MutableStateFlow(
    prefs.getString("saved_cash_app_tag", "\$damronrans") ?: "\$damronrans"
  )
  val cashAppTag: StateFlow<String> = _cashAppTag.asStateFlow()

  private val _cashAppDisplayName = MutableStateFlow(
    prefs.getString("saved_cash_app_name", "App Owner") ?: "App Owner"
  )
  val cashAppDisplayName: StateFlow<String> = _cashAppDisplayName.asStateFlow()

  private val _selectedTab = MutableStateFlow(AppTab.DASHBOARD)
  val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

  private val _chartTimeRange = MutableStateFlow(TimeRange.DAYS_30)
  val chartTimeRange: StateFlow<TimeRange> = _chartTimeRange.asStateFlow()

  private val _callFilter = MutableStateFlow("ALL") // ALL, INCOMING, OUTGOING, MISSED
  val callFilter: StateFlow<String> = _callFilter.asStateFlow()

  private val _messageFilter = MutableStateFlow("ALL") // ALL, INCOMING, OUTGOING, UNREAD
  val messageFilter: StateFlow<String> = _messageFilter.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  // Real-time background monitoring service state
  val isMonitoringServiceRunning: StateFlow<Boolean> = CommunicationMonitoringService.isServiceRunning
  val monitoredEventsCount: StateFlow<Int> = CommunicationMonitoringService.monitoredEventsCount
  val alertsTriggeredCount: StateFlow<Int> = CommunicationMonitoringService.alertsTriggeredCount

  fun toggleMonitoringService(context: Context) {
    if (CommunicationMonitoringService.isServiceRunning.value) {
      CommunicationMonitoringService.stop(context)
      showInAppNotice("Background Call & Message Service Stopped")
    } else {
      CommunicationMonitoringService.start(context)
      showInAppNotice("Background Call & Message Service Started (Active Foreground Service)")
    }
  }

  // Real-time live inbound simulator toggle
  private val _isLiveSimulationRunning = MutableStateFlow(false)
  val isLiveSimulationRunning: StateFlow<Boolean> = _isLiveSimulationRunning.asStateFlow()
  private var liveSimulationJob: Job? = null

  // In-app alert notification banner state
  private val _inAppAlert = MutableStateFlow<String?>(null)
  val inAppAlert: StateFlow<String?> = _inAppAlert.asStateFlow()

  // Profit projection calculator params
  val simulatedHourlyRate = MutableStateFlow(120.0)
  val simulatedWeeklyHours = MutableStateFlow(15.0)
  val simulatedConversionRate = MutableStateFlow(25.0) // %
  val simulatedAvgDealValue = MutableStateFlow(600.0)

  // Reactive Contacts Flow
  val contacts: StateFlow<List<ContactEntity>> = _activeCellNumber.flatMapLatest { number ->
    repository.getContacts(number)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Reactive Communications Flow
  val communications: StateFlow<List<CommunicationEntity>> = _activeCellNumber.flatMapLatest { number ->
    repository.getCommunications(number)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Reactive Cash Flows
  val cashFlows: StateFlow<List<CashFlowEntity>> = _activeCellNumber.flatMapLatest { number ->
    repository.getCashFlows(number)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Derived Contact Frequency Metrics
  val contactFrequencies: StateFlow<List<ContactFrequencyMetric>> = combine(
    contacts,
    communications,
    _chartTimeRange
  ) { contactList, commsList, range ->
    val cutoff = if (range == TimeRange.ALL_TIME) 0L else System.currentTimeMillis() - (range.days * 24L * 3600 * 1000)
    val filteredComms = commsList.filter { it.timestamp >= cutoff }

    val contactMap = contactList.associateBy { it.phoneNumber }

    val groupedByNumber = filteredComms.groupBy { it.contactNumber }

    val results = mutableListOf<ContactFrequencyMetric>()

    groupedByNumber.forEach { (number, items) ->
      val contact = contactMap[number]
      val name = contact?.name ?: items.firstOrNull()?.contactName ?: number
      val color = contact?.avatarColorHex ?: 0xFF6366F1
      val calls = items.count { it.type.startsWith("CALL") }
      val msgs = items.count { it.type.startsWith("SMS") }
      val billableRev = items.filter { it.isBillable }.sumOf { it.billedAmount }
      val lastTime = items.maxOfOrNull { it.timestamp } ?: 0L

      results.add(
        ContactFrequencyMetric(
          contactName = name,
          phoneNumber = number,
          totalInteractions = items.size,
          callsCount = calls,
          messagesCount = msgs,
          billableRevenue = billableRev,
          avatarColorHex = color,
          lastInteractionTime = lastTime
        )
      )
    }

    // Add any contacts with 0 interactions in period
    contactList.forEach { c ->
      if (results.none { it.phoneNumber == c.phoneNumber }) {
        results.add(
          ContactFrequencyMetric(
            contactName = c.name,
            phoneNumber = c.phoneNumber,
            totalInteractions = 0,
            callsCount = 0,
            messagesCount = 0,
            billableRevenue = 0.0,
            avatarColorHex = c.avatarColorHex,
            lastInteractionTime = 0L
          )
        )
      }
    }

    results.sortedByDescending { it.totalInteractions }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Derived Cash Flow Summary
  val cashFlowSummary: StateFlow<CashFlowSummary> = combine(
    cashFlows,
    communications
  ) { flows, comms ->
    val inflow = flows.filter { it.type == "INCOME" }.sumOf { it.amount }
    val outflow = flows.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val net = inflow - outflow
    val margin = if (inflow > 0) (net / inflow) * 100.0 else 0.0
    val billableCommEarnings = comms.filter { it.isBillable }.sumOf { it.billedAmount }

    CashFlowSummary(
      totalInflow = Math.round(inflow * 100.0) / 100.0,
      totalOutflow = Math.round(outflow * 100.0) / 100.0,
      netProfit = Math.round(net * 100.0) / 100.0,
      profitMarginPercent = Math.round(margin * 10.0) / 10.0,
      billableCallEarnings = Math.round(billableCommEarnings * 100.0) / 100.0
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CashFlowSummary(0.0, 0.0, 0.0, 0.0, 0.0))

  // Derived Interaction Frequency Trends (Daily Velocity from Room Database)
  val interactionTrends: StateFlow<List<DailyInteractionTrendPoint>> = communications.map { commsList ->
    val calendar = java.util.Calendar.getInstance()
    val dayFormat = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
    val now = System.currentTimeMillis()
    val points = mutableListOf<DailyInteractionTrendPoint>()

    for (i in 6 downTo 0) {
      calendar.timeInMillis = now - (i * 24L * 3600 * 1000)
      calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
      calendar.set(java.util.Calendar.MINUTE, 0)
      calendar.set(java.util.Calendar.SECOND, 0)
      calendar.set(java.util.Calendar.MILLISECOND, 0)
      val startOfDay = calendar.timeInMillis
      val endOfDay = startOfDay + (24L * 3600 * 1000) - 1

      val dayLabel = dayFormat.format(calendar.time)
      val dayComms = commsList.filter { it.timestamp in startOfDay..endOfDay }

      val calls = dayComms.count { it.type.startsWith("CALL") }
      val msgs = dayComms.count { it.type.startsWith("SMS") || it.type.startsWith("MESSENGER") }
      val topContact = dayComms.groupBy { it.contactName }.maxByOrNull { it.value.size }?.key

      points.add(
        DailyInteractionTrendPoint(
          dateLabel = dayLabel,
          dayTimestamp = startOfDay,
          totalInteractions = dayComms.size,
          callsCount = calls,
          messagesCount = msgs,
          topContactName = topContact
        )
      )
    }
    points
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  init {
    // Seed initial data if newly registered cell number
    viewModelScope.launch {
      repository.seedInitialDataIfEmpty(_activeCellNumber.value)
    }
  }

  fun setTab(tab: AppTab) {
    _selectedTab.value = tab
  }

  fun setTimeRange(range: TimeRange) {
    _chartTimeRange.value = range
  }

  fun setCallFilter(filter: String) {
    _callFilter.value = filter
  }

  fun setMessageFilter(filter: String) {
    _messageFilter.value = filter
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun dismissInAppAlert() {
    _inAppAlert.value = null
  }

  fun updateCellNumber(newNumber: String) {
    val clean = newNumber.trim()
    if (clean.isNotEmpty()) {
      _activeCellNumber.value = clean
      prefs.edit().putString("saved_cell_number", clean).apply()
      viewModelScope.launch {
        repository.seedInitialDataIfEmpty(clean)
      }
      showInAppNotice("Connected cell number updated to $clean")
    }
  }

  fun updateCashAppInfo(tag: String, displayName: String) {
    val cleanTag = if (tag.trim().startsWith("$")) tag.trim() else "$${tag.trim()}"
    val cleanName = displayName.trim().ifBlank { "App Owner" }
    _cashAppTag.value = cleanTag
    _cashAppDisplayName.value = cleanName
    prefs.edit()
      .putString("saved_cash_app_tag", cleanTag)
      .putString("saved_cash_app_name", cleanName)
      .apply()
    showInAppNotice("Cash App information saved: $cleanTag")
  }

  fun showInAppNotice(msg: String) {
    _inAppAlert.value = msg
  }

  fun logManualCall(
    contactName: String,
    phoneNumber: String,
    type: InteractionType,
    durationSeconds: Int,
    isBillable: Boolean,
    hourlyRate: Double
  ) {
    viewModelScope.launch {
      val billedAmount = if (isBillable && hourlyRate > 0) {
        val hours = (durationSeconds.toDouble() / 3600.0).coerceAtLeast(0.1)
        Math.round(hours * hourlyRate * 100.0) / 100.0
      } else 0.0

      val comm = CommunicationEntity(
        userCellNumber = _activeCellNumber.value,
        contactName = contactName,
        contactNumber = phoneNumber,
        type = type.name,
        messageBody = if (type == InteractionType.CALL_MISSED) "Missed Call" else "Voice Call (${durationSeconds / 60}m ${durationSeconds % 60}s)",
        callDurationSeconds = durationSeconds,
        timestamp = System.currentTimeMillis(),
        isRead = true,
        isBillable = isBillable,
        billedAmount = billedAmount
      )
      repository.insertCommunication(comm)

      if (isBillable && billedAmount > 0) {
        repository.insertCashFlow(
          CashFlowEntity(
            userCellNumber = _activeCellNumber.value,
            title = "Consulting Call: $contactName",
            amount = billedAmount,
            type = "INCOME",
            category = "Client Call Consulting",
            relatedContactName = contactName,
            timestamp = System.currentTimeMillis(),
            notes = "Logged $durationSeconds sec at $$hourlyRate/hr"
          )
        )
      }
      showInAppNotice("Logged call with $contactName")
    }
  }

  fun seedSampleData() {
    viewModelScope.launch {
      repository.seedInitialDataIfEmpty(_activeCellNumber.value)
      showInAppNotice("Administrator refresh: Telemetry loaded")
    }
  }

  fun logManualMessage(
    contactName: String,
    phoneNumber: String,
    messageBody: String,
    isIncoming: Boolean,
    triggerAlert: Boolean
  ) {
    viewModelScope.launch {
      val type = if (isIncoming) InteractionType.SMS_INCOMING else InteractionType.SMS_OUTGOING
      val comm = CommunicationEntity(
        userCellNumber = _activeCellNumber.value,
        contactName = contactName,
        contactNumber = phoneNumber,
        type = type.name,
        messageBody = messageBody,
        callDurationSeconds = 0,
        timestamp = System.currentTimeMillis(),
        isRead = !isIncoming
      )
      repository.insertCommunication(comm)

      if (isIncoming && triggerAlert) {
        NotificationHelper.notifyIncomingMessage(
          context = getApplication<Application>(),
          senderName = contactName,
          senderNumber = phoneNumber,
          messageBody = messageBody
        )
        showInAppNotice("Alert triggered for message from $contactName")
      } else {
        showInAppNotice("Message logged for $contactName")
      }
    }
  }

  fun triggerFacebookMessengerAlert(
    senderName: String,
    messageBody: String
  ) {
    viewModelScope.launch {
      val comm = CommunicationEntity(
        userCellNumber = _activeCellNumber.value,
        contactName = senderName,
        contactNumber = "Messenger: $senderName",
        type = InteractionType.MESSENGER_INCOMING.name,
        messageBody = messageBody,
        callDurationSeconds = 0,
        timestamp = System.currentTimeMillis(),
        isRead = false
      )
      repository.insertCommunication(comm)
      NotificationHelper.notifyFacebookMessenger(
        context = getApplication<Application>(),
        senderName = senderName,
        messageBody = messageBody
      )
      showInAppNotice("Facebook Messenger Alert: $senderName")
    }
  }

  fun triggerInstantIncomingAlert(
    senderName: String,
    senderNumber: String,
    messageBody: String
  ) {
    viewModelScope.launch {
      repository.logIncomingMessageWithAlert(
        context = getApplication(),
        userCellNumber = _activeCellNumber.value,
        senderName = senderName,
        senderNumber = senderNumber,
        messageBody = messageBody,
        triggerAlert = true
      )
      showInAppNotice("Incoming Alert Dispatched: $senderName")
    }
  }

  fun triggerInstantCallAlert(
    callerName: String,
    callerNumber: String,
    isMissed: Boolean
  ) {
    viewModelScope.launch {
      repository.logIncomingCallWithAlert(
        context = getApplication(),
        userCellNumber = _activeCellNumber.value,
        callerName = callerName,
        callerNumber = callerNumber,
        durationSeconds = if (isMissed) 0 else 180,
        isMissed = isMissed,
        triggerAlert = true
      )
      showInAppNotice(if (isMissed) "Missed Call Alert: $callerName" else "Incoming Call Alert: $callerName")
    }
  }

  fun addCashFlow(
    title: String,
    amount: Double,
    type: String,
    category: String,
    relatedContactName: String?,
    notes: String
  ) {
    viewModelScope.launch {
      val flow = CashFlowEntity(
        userCellNumber = _activeCellNumber.value,
        title = title,
        amount = amount,
        type = type,
        category = category,
        relatedContactName = relatedContactName?.takeIf { it.isNotBlank() },
        timestamp = System.currentTimeMillis(),
        notes = notes
      )
      repository.insertCashFlow(flow)
      showInAppNotice("Cash flow $type of $$amount recorded!")
    }
  }

  fun convertCallToBillable(comm: CommunicationEntity, rate: Double) {
    viewModelScope.launch {
      repository.convertCallToBillableProfit(comm, rate)
      showInAppNotice("Converted call to billable profit revenue!")
    }
  }

  fun markAsRead(id: Long) {
    viewModelScope.launch {
      repository.markAsRead(id)
    }
  }

  fun markAllAsRead() {
    viewModelScope.launch {
      repository.markAllAsRead(_activeCellNumber.value)
      showInAppNotice("All messages marked as read")
    }
  }

  fun deleteCommunication(comm: CommunicationEntity) {
    viewModelScope.launch {
      repository.deleteCommunication(comm)
      showInAppNotice("Record deleted")
    }
  }

  fun deleteCashFlow(flow: CashFlowEntity) {
    viewModelScope.launch {
      repository.deleteCashFlow(flow)
      showInAppNotice("Cash flow entry removed")
    }
  }

  fun toggleLiveSimulation() {
    if (_isLiveSimulationRunning.value) {
      liveSimulationJob?.cancel()
      liveSimulationJob = null
      _isLiveSimulationRunning.value = false
      showInAppNotice("Live message simulation stopped")
    } else {
      _isLiveSimulationRunning.value = true
      showInAppNotice("Live message simulator started (Alerts every 25s)")
      liveSimulationJob = viewModelScope.launch {
        val sampleInbounds = listOf(
          Triple("Elena Rostova", "+1 (555) 892-1054", "Just reviewed the scope! Can we do a 10 min call?"),
          Triple("Sophia Vance", "+1 (555) 329-8472", "Client approved the $2,500 budget increase!"),
          Triple("David Miller", "+1 (555) 741-9238", "New lead inbound from our webinar. Contact details attached."),
          Triple("Marcus Chen", "+1 (555) 438-6621", "Hey! Quick question on tomorrow's client presentation."),
          Triple("Emma Watson", "+1 (555) 198-4420", "Interested in your consulting services. What's your rate?")
        )
        var index = 0
        while (isActive) {
          delay(25000)
          val (name, number, text) = sampleInbounds[index % sampleInbounds.size]
          index++
          repository.logIncomingMessageWithAlert(
            context = getApplication(),
            userCellNumber = _activeCellNumber.value,
            senderName = name,
            senderNumber = number,
            messageBody = text,
            triggerAlert = true
          )
        }
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    liveSimulationJob?.cancel()
  }
}

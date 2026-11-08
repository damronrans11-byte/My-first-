package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import com.example.ui.ContactFrequencyMetric
import com.example.ui.DailyInteractionTrendPoint
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseExpense
import kotlin.math.cos
import kotlin.math.sin

/**
 * Interactive Horizontal Bar Chart visualizing contact interaction frequency.
 * Allows tapping each contact bar to inspect call vs message ratios and billable profit.
 */
@Composable
fun ContactFrequencyBarChart(
  metrics: List<ContactFrequencyMetric>,
  modifier: Modifier = Modifier,
  onContactClick: ((ContactFrequencyMetric) -> Unit)? = null
) {
  var selectedMetric by remember { mutableStateOf<ContactFrequencyMetric?>(null) }
  val topMetrics = remember(metrics) { metrics.take(5) }
  val maxInteractions = remember(topMetrics) {
    (topMetrics.maxOfOrNull { it.totalInteractions } ?: 1).coerceAtLeast(1)
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("contact_frequency_chart_card"),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    shape = RoundedCornerShape(20.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Interaction Frequency Ranking",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Top contacts ranked by calls and messages",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (topMetrics.isEmpty() || maxInteractions == 0) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No communication activity logged yet for this period.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        topMetrics.forEach { item ->
          val isSelected = selectedMetric?.phoneNumber == item.phoneNumber
          val animatedFraction = remember { Animatable(0f) }

          LaunchedEffect(item.totalInteractions) {
            animatedFraction.animateTo(
              targetValue = item.totalInteractions.toFloat() / maxInteractions.toFloat(),
              animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
            )
          }

          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                else Color.Transparent
              )
              .clickable {
                selectedMetric = if (isSelected) null else item
                onContactClick?.invoke(item)
              }
              .padding(vertical = 8.dp, horizontal = 6.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(item.avatarColorHex)),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = item.contactName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = item.contactName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = "${item.callsCount} calls · ${item.messagesCount} texts",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Text(
                text = "${item.totalInteractions} total",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Dual progress bar: Calls (Indigo) vs Messages (Cyan)
            val callRatio = if (item.totalInteractions > 0) item.callsCount.toFloat() / item.totalInteractions else 0f
            val msgRatio = if (item.totalInteractions > 0) item.messagesCount.toFloat() / item.totalInteractions else 0f

            Canvas(
              modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
            ) {
              val fullBarWidth = size.width * animatedFraction.value
              // Background track
              drawRoundRect(
                color = Color.LightGray.copy(alpha = 0.25f),
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
              )
              // Calls segment
              val callsWidth = fullBarWidth * callRatio
              drawRoundRect(
                color = Color(0xFF6366F1),
                size = Size(callsWidth, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
              )
              // Messages segment
              val msgsWidth = fullBarWidth * msgRatio
              drawRoundRect(
                color = Color(0xFF06B6D4),
                topLeft = Offset(callsWidth, 0f),
                size = Size(msgsWidth, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
              )
            }
          }
        }

        // Legend row
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF6366F1)))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Calls", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.width(12.dp))
          Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF06B6D4)))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Messages", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }

      // Selected Contact Detail Card Pop-up
      selectedMetric?.let { sel ->
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = "${sel.contactName} Breakdown",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Phone: ${sel.phoneNumber}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            if (sel.billableRevenue > 0) {
              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "$${"%.2f".format(sel.billableRevenue)}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldProfit
                )
                Text(
                  text = "Profit Earned",
                  style = MaterialTheme.typography.labelSmall,
                  color = EmeraldProfit
                )
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Interactive Donut Chart showing Communication Channel Breakdown:
 * Calls vs Messages with interactive tap selection.
 */
@Composable
fun CommunicationRatioDonutChart(
  totalCalls: Int,
  totalMessages: Int,
  modifier: Modifier = Modifier
) {
  val total = (totalCalls + totalMessages).coerceAtLeast(1)
  val callSweep = (totalCalls.toFloat() / total.toFloat()) * 360f
  val msgSweep = (totalMessages.toFloat() / total.toFloat()) * 360f

  val animatedAngle = remember { Animatable(0f) }
  LaunchedEffect(totalCalls, totalMessages) {
    animatedAngle.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )
  }

  var selectedSegment by remember { mutableStateOf<String?>(null) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("communication_ratio_donut_chart"),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    shape = RoundedCornerShape(20.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Text(
        text = "Communication Channel Ratio",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Distribution of voice calls vs SMS messages",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(16.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        // Donut Chart Canvas
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.size(150.dp)
        ) {
          Canvas(
            modifier = Modifier
              .size(130.dp)
              .pointerInput(Unit) {
                detectTapGestures { offset ->
                  val center = Offset(size.width / 2f, size.height / 2f)
                  val angle = (Math.toDegrees(
                    Math.atan2((offset.y - center.y).toDouble(), (offset.x - center.x).toDouble())
                  ) + 360) % 360
                  selectedSegment = if (angle <= callSweep) "Calls" else "Messages"
                }
              }
          ) {
            val strokeWidth = 24.dp.toPx()
            val canvasSize = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // Calls Arc
            drawArc(
              color = Color(0xFF6366F1),
              startAngle = -90f,
              sweepAngle = callSweep * animatedAngle.value,
              useCenter = false,
              topLeft = topLeft,
              size = Size(canvasSize, canvasSize),
              style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Messages Arc
            drawArc(
              color = Color(0xFF06B6D4),
              startAngle = -90f + (callSweep * animatedAngle.value),
              sweepAngle = msgSweep * animatedAngle.value,
              useCenter = false,
              topLeft = topLeft,
              size = Size(canvasSize, canvasSize),
              style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
          }

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "${totalCalls + totalMessages}",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "Total",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Legends and Metrics
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          // Calls Stat
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (selectedSegment == "Calls") Color(0xFF6366F1).copy(alpha = 0.15f) else Color.Transparent,
            modifier = Modifier.clickable { selectedSegment = "Calls" }
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(14.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF6366F1))
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Voice Calls",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = "$totalCalls (${(callSweep / 3.6f).toInt()}%)",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          // Messages Stat
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (selectedSegment == "Messages") Color(0xFF06B6D4).copy(alpha = 0.15f) else Color.Transparent,
            modifier = Modifier.clickable { selectedSegment = "Messages" }
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(14.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF06B6D4))
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "SMS Messages",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = "$totalMessages (${(msgSweep / 3.6f).toInt()}%)",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Interactive Cash Flow Profit Chart visualizing Inflow vs Outflow vs Net Profit.
 */
@Composable
fun CashFlowProfitBarChart(
  inflow: Double,
  outflow: Double,
  netProfit: Double,
  modifier: Modifier = Modifier
) {
  val maxValue = maxOf(inflow, outflow, kotlin.math.abs(netProfit), 100.0)

  val animatedScale = remember { Animatable(0f) }
  LaunchedEffect(inflow, outflow, netProfit) {
    animatedScale.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("cash_flow_profit_chart"),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    shape = RoundedCornerShape(20.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Cash Flow & Profit Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Total revenue earned vs expenses logged",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Surface(
          color = if (netProfit >= 0) EmeraldProfit.copy(alpha = 0.15f) else RoseExpense.copy(alpha = 0.15f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = if (netProfit >= 0) "+$${"%.0f".format(netProfit)} Net" else "-$${"%.0f".format(kotlin.math.abs(netProfit))} Loss",
            color = if (netProfit >= 0) EmeraldProfit else RoseExpense,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
      ) {
        // Inflow Bar
        BarColumn(
          label = "Inflow",
          amount = inflow,
          heightFraction = (inflow / maxValue).toFloat() * animatedScale.value,
          color = Color(0xFF10B981)
        )

        // Outflow Bar
        BarColumn(
          label = "Outflow",
          amount = outflow,
          heightFraction = (outflow / maxValue).toFloat() * animatedScale.value,
          color = Color(0xFFF43F5E)
        )

        // Net Profit Bar
        BarColumn(
          label = "Net Profit",
          amount = netProfit,
          heightFraction = (kotlin.math.abs(netProfit) / maxValue).toFloat() * animatedScale.value,
          color = if (netProfit >= 0) Color(0xFF059669) else Color(0xFFE11D48)
        )
      }
    }
  }
}

@Composable
private fun BarColumn(
  label: String,
  amount: Double,
  heightFraction: Float,
  color: Color
) {
  val clampedHeightFraction = heightFraction.coerceIn(0.08f, 1f)

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Bottom,
    modifier = Modifier.width(80.dp)
  ) {
    Text(
      text = "$${"%.0f".format(amount)}",
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = color
    )
    Spacer(modifier = Modifier.height(4.dp))
    Box(
      modifier = Modifier
        .width(36.dp)
        .height((100 * clampedHeightFraction).dp)
        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
        .background(
          Brush.verticalGradient(
            listOf(color.copy(alpha = 0.85f), color)
          )
        )
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

/**
 * Interactive Contact Interaction Frequency Trends Chart (Data Visualization Component).
 * Plots 7-day daily communication volume curves from Room database telemetry with cubic curves,
 * gradient fill, node selection, and breakdown by Calls vs Messages.
 */
@Composable
fun ContactInteractionTrendsChart(
  trendPoints: List<DailyInteractionTrendPoint>,
  modifier: Modifier = Modifier
) {
  var selectedMetricMode by remember { mutableStateOf("ALL") } // "ALL", "CALLS", "MESSAGES"
  var selectedIndex by remember { mutableStateOf(if (trendPoints.isNotEmpty()) trendPoints.size - 1 else -1) }

  val points = remember(trendPoints) {
    if (trendPoints.isEmpty()) {
      listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").mapIndexed { i, label ->
        DailyInteractionTrendPoint(label, System.currentTimeMillis() - ((6 - i) * 86400000L), 0, 0, 0, null)
      }
    } else trendPoints
  }

  val activeValues = remember(points, selectedMetricMode) {
    points.map { pt ->
      when (selectedMetricMode) {
        "CALLS" -> pt.callsCount
        "MESSAGES" -> pt.messagesCount
        else -> pt.totalInteractions
      }
    }
  }

  val maxVal = remember(activeValues) {
    (activeValues.maxOrNull() ?: 1).coerceAtLeast(4)
  }

  val animatedProgress = remember { Animatable(0f) }
  LaunchedEffect(points, selectedMetricMode) {
    animatedProgress.snapTo(0f)
    animatedProgress.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
  }

  val primaryColor = when (selectedMetricMode) {
    "CALLS" -> Color(0xFF6366F1)
    "MESSAGES" -> Color(0xFF06B6D4)
    else -> Color(0xFF4F46E5)
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("contact_interaction_trends_chart"),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(20.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Interaction Frequency Trends",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Daily communication velocity across the last 7 days",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = EmeraldProfit.copy(alpha = 0.15f)
        ) {
          Text(
            text = "+28% VELOCITY",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = EmeraldProfit,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Metric Filter Chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        TrendFilterChip(label = "All Trends", isSelected = selectedMetricMode == "ALL", color = IndigoPrimary) {
          selectedMetricMode = "ALL"
        }
        TrendFilterChip(label = "Calls", isSelected = selectedMetricMode == "CALLS", color = Color(0xFF6366F1)) {
          selectedMetricMode = "CALLS"
        }
        TrendFilterChip(label = "Texts", isSelected = selectedMetricMode == "MESSAGES", color = Color(0xFF06B6D4)) {
          selectedMetricMode = "MESSAGES"
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Smooth Curve Canvas
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(170.dp)
      ) {
        Canvas(
          modifier = Modifier
            .fillMaxSize()
            .pointerInput(points) {
              detectTapGestures { offset ->
                val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                val touchedIndex = (offset.x / stepX).toInt().coerceIn(0, points.size - 1)
                selectedIndex = touchedIndex
              }
            }
        ) {
          val width = size.width
          val height = size.height - 24.dp.toPx()
          val stepX = width / (points.size - 1).coerceAtLeast(1)

          // Draw horizontal guidelines
          val gridLines = 3
          for (g in 0..gridLines) {
            val y = height * (g.toFloat() / gridLines.toFloat())
            drawLine(
              color = Color.LightGray.copy(alpha = 0.2f),
              start = Offset(0f, y),
              end = Offset(width, y),
              strokeWidth = 1.dp.toPx()
            )
          }

          // Build path points
          val coords = activeValues.mapIndexed { i, value ->
            val x = i * stepX
            val ratio = (value.toFloat() / maxVal.toFloat()) * animatedProgress.value
            val y = height - (ratio * (height - 20.dp.toPx()))
            Offset(x, y)
          }

          if (coords.size >= 2) {
            val strokePath = Path().apply {
              moveTo(coords[0].x, coords[0].y)
              for (i in 0 until coords.size - 1) {
                val p0 = coords[i]
                val p1 = coords[i + 1]
                val cx = (p0.x + p1.x) / 2f
                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
              }
            }

            // Fill area with gradient
            val fillPath = Path().apply {
              addPath(strokePath)
              lineTo(coords.last().x, height)
              lineTo(coords.first().x, height)
              close()
            }

            drawPath(
              path = fillPath,
              brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent),
                startY = 0f,
                endY = height
              )
            )

            // Draw line stroke
            drawPath(
              path = strokePath,
              color = primaryColor,
              style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Draw data node circles
            coords.forEachIndexed { idx, point ->
              val isSelected = idx == selectedIndex
              val nodeRadius = if (isSelected) 7.dp.toPx() else 4.5.dp.toPx()

              if (isSelected) {
                drawCircle(
                  color = primaryColor.copy(alpha = 0.25f),
                  radius = 14.dp.toPx(),
                  center = point
                )
              }

              drawCircle(
                color = Color.White,
                radius = nodeRadius + 2.dp.toPx(),
                center = point
              )
              drawCircle(
                color = primaryColor,
                radius = nodeRadius,
                center = point
              )
            }
          }
        }
      }

      // X-Axis Day Labels
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        points.forEachIndexed { i, pt ->
          val isSelected = i == selectedIndex
          Text(
            text = pt.dateLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable { selectedIndex = i }
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Selected Day Detail Box
      if (selectedIndex in points.indices) {
        val sel = points[selectedIndex]
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "${sel.dateLabel} Interaction Velocity",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "${sel.callsCount} calls · ${sel.messagesCount} texts" + (sel.topContactName?.let { " · Top: $it" } ?: ""),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = primaryColor.copy(alpha = 0.15f)
            ) {
              Text(
                text = "${sel.totalInteractions} total",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = primaryColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TrendFilterChip(
  label: String,
  isSelected: Boolean,
  color: Color,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, color) else null,
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .clickable(onClick = onClick)
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
    )
  }
}

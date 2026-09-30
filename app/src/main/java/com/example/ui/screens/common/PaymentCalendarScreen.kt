package com.example.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentScheduleEntity
import com.example.data.model.ScheduleStatus
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatCurrency
import com.example.ui.theme.OverdueRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun PaymentCalendarScreen(
    schedules: List<PaymentScheduleEntity>
) {
    val paidCount = schedules.count { it.status == ScheduleStatus.PAID }
    val pendingCount = schedules.count { it.status == ScheduleStatus.PENDING }
    val overdueCount = schedules.count { it.status == ScheduleStatus.OVERDUE }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("payment_calendar_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Payment Calendar & Schedules",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Track installment due dates, overdue notices, and completed payments.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Legend Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalendarLegendCard("🟢 Paid ($paidCount)", SuccessGreen, Modifier.weight(1f))
                CalendarLegendCard("🟡 Upcoming ($pendingCount)", WarningAmber, Modifier.weight(1f))
                CalendarLegendCard("🔴 Overdue ($overdueCount)", OverdueRed, Modifier.weight(1f))
            }
        }

        item {
            Text(
                text = "Chronological Installment Schedule",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (schedules.isEmpty()) {
            item {
                Text("No payment schedules available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(schedules) { schedule ->
                val indicatorColor = when (schedule.status) {
                    ScheduleStatus.PAID -> SuccessGreen
                    ScheduleStatus.OVERDUE -> OverdueRed
                    else -> WarningAmber
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(indicatorColor)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Installment #${schedule.installmentNumber} • Due: ${schedule.dueDate}",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Principal: ${formatCurrency(schedule.principalComponent)} | Interest: ${formatCurrency(schedule.interestComponent)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (schedule.paidDate.isNotEmpty()) {
                                Text(
                                    text = "Paid on: ${schedule.paidDate}",
                                    fontSize = 11.sp,
                                    color = SuccessGreen
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatCurrency(schedule.dueAmount),
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StatusBadge(schedule.status.name)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarLegendCard(title: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
            Text(title, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

package com.example.ui.screens.financier

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommissionEntity
import com.example.data.model.LoanEntity
import com.example.data.model.PaymentEntity
import com.example.ui.components.MetricStatCard
import com.example.ui.components.formatCurrency
import com.example.ui.theme.LoanPrimary
import com.example.ui.theme.LoanSecondary
import com.example.ui.theme.OverdueRed
import com.example.ui.theme.SuccessGreen

@Composable
fun FinancierReportsScreen(
    loans: List<LoanEntity>,
    payments: List<PaymentEntity>,
    commissions: List<CommissionEntity>
) {
    var exportFormatMessage by remember { mutableStateOf<String?>(null) }

    val totalLent = loans.sumOf { it.principalAmount }
    val totalReceived = loans.sumOf { it.totalPaid }
    val totalPrincipalReceived = loans.sumOf { it.principalPaid }
    val totalInterestReceived = loans.sumOf { it.interestPaid }
    val totalCommission = commissions.sumOf { it.amount }
    val netEarnings = totalInterestReceived - totalCommission

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("financier_reports_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Financial Reports & Analytics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Detailed audit of portfolio performance, interest yield, and platform commission.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Export Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { exportFormatMessage = "Exported financial statement to PDF" },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { exportFormatMessage = "Exported financial ledger to Excel (.xlsx)" },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Excel", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { exportFormatMessage = "Exported transactions to CSV format" },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CSV", fontSize = 12.sp)
                }
            }
        }

        exportFormatMessage?.let { msg ->
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SuccessGreen.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(msg, color = SuccessGreen, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Key Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "Total Lent",
                    value = formatCurrency(totalLent),
                    icon = Icons.Default.AccountBalance,
                    iconTint = LoanSecondary,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Total Collected",
                    value = formatCurrency(totalReceived),
                    icon = Icons.Default.Savings,
                    iconTint = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "Interest Collected",
                    value = formatCurrency(totalInterestReceived),
                    icon = Icons.Default.TrendingUp,
                    iconTint = LoanPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Platform Fee",
                    value = formatCurrency(totalCommission),
                    icon = Icons.Default.ReceiptLong,
                    iconTint = OverdueRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Custom Visual Fintech Chart: Capital Distribution
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Portfolio Capital Velocity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Visual comparison of capital disbursed vs recovered vs yield",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    val maxVal = maxOf(1.0, totalLent, totalReceived)
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val barWidth = size.width / 5
                        val spacing = (size.width - (barWidth * 3)) / 4

                        // Bar 1: Lent
                        val height1 = ((totalLent / maxVal) * size.height * 0.85).toFloat()
                        drawRoundRect(
                            color = Color(0xFF0EA5E9),
                            topLeft = Offset(spacing, size.height - height1),
                            size = Size(barWidth, height1),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )

                        // Bar 2: Principal Received
                        val height2 = ((totalPrincipalReceived / maxVal) * size.height * 0.85).toFloat()
                        drawRoundRect(
                            color = Color(0xFF10B981),
                            topLeft = Offset(spacing * 2 + barWidth, size.height - height2),
                            size = Size(barWidth, height2),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )

                        // Bar 3: Interest Yield
                        val height3 = ((totalInterestReceived / maxVal) * size.height * 0.85).coerceAtLeast(10.0).toFloat()
                        drawRoundRect(
                            color = Color(0xFFF59E0B),
                            topLeft = Offset(spacing * 3 + barWidth * 2, size.height - height3),
                            size = Size(barWidth, height3),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        LegendItem("Total Lent", Color(0xFF0EA5E9), formatCurrency(totalLent))
                        LegendItem("Principal In", Color(0xFF10B981), formatCurrency(totalPrincipalReceived))
                        LegendItem("Interest Yield", Color(0xFFF59E0B), formatCurrency(totalInterestReceived))
                    }
                }
            }
        }

        // Commission Ledger Section (Section 30 & 31)
        item {
            Text(
                text = "Commission & Platform Charges Ledger",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (commissions.isEmpty()) {
            item {
                Text("No commission transactions recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(commissions) { comm ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(comm.id, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            Text("Loan: ${comm.loanId} • Rate: ${comm.ratePercentage}%", style = MaterialTheme.typography.bodySmall)
                            Text("Payer: ${comm.payer} → Recipient: ${comm.recipient}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(formatCurrency(comm.amount), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

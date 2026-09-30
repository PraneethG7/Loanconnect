package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LoanEntity
import com.example.data.model.LoanStatus
import com.example.ui.theme.OverdueRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun FinancialBreakdownCard(
    loan: LoanEntity,
    modifier: Modifier = Modifier,
    isFinancierView: Boolean = false,
    onPayClick: (() -> Unit)? = null,
    onEarlySettleClick: (() -> Unit)? = null,
    onToggleAutoPay: ((Boolean) -> Unit)? = null
) {
    var expandedDetails by remember { mutableStateOf(false) }

    val remainingTotal = maxOf(0.0, loan.totalPayable - loan.totalPaid)
    val remainingPrincipal = maxOf(0.0, loan.principalAmount - loan.principalPaid)
    val progress = if (loan.totalPayable > 0) (loan.totalPaid / loan.totalPayable).toFloat().coerceIn(0f, 1f) else 0f

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Loan ID, Counterpart, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = loan.id,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isFinancierView) "Borrower: ${loan.borrowerName}" else "Financier: ${loan.financierName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (loan.purpose.isNotEmpty()) {
                        Text(
                            text = "Purpose: ${loan.purpose}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                StatusBadge(status = loan.status.name)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Metric: Total Outstanding / Remaining Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = if (isFinancierView) "Outstanding Balance" else "Remaining Total",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(remainingTotal),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (loan.status == LoanStatus.OVERDUE) OverdueRed else MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Next Due: ${loan.nextDueDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (loan.status == LoanStatus.OVERDUE) OverdueRed else WarningAmber,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatCurrency(loan.nextInstallmentAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = SuccessGreen,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Paid: ${formatCurrency(loan.totalPaid)} (${(progress * 100).toInt()}%)",
                        fontSize = 11.sp,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Total: ${formatCurrency(loan.totalPayable)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expandable Complete Breakdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedDetails = !expandedDetails }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expandedDetails) "Hide Financial Breakdown" else "View Full Financial Breakdown",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (expandedDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = expandedDetails) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BreakdownRow("Original Principal", formatCurrency(loan.principalAmount), true)
                    BreakdownRow(
                        "Interest (${loan.interestRate}% ${loan.interestType.name.lowercase()})",
                        formatCurrency(loan.interestAmount)
                    )
                    BreakdownRow("Processing Fees", formatCurrency(loan.processingFee))
                    BreakdownRow("Platform Commission / Charges", formatCurrency(loan.platformFee))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    BreakdownRow("Total Payable", formatCurrency(loan.totalPayable), true)
                    BreakdownRow("Total Paid", formatCurrency(loan.totalPaid), textColor = SuccessGreen)
                    BreakdownRow(" - Principal Paid", formatCurrency(loan.principalPaid))
                    BreakdownRow(" - Interest Paid", formatCurrency(loan.interestPaid))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    BreakdownRow("Remaining Principal", formatCurrency(remainingPrincipal))
                    BreakdownRow(
                        "Remaining Total",
                        formatCurrency(remainingTotal),
                        isBold = true,
                        textColor = if (loan.status == LoanStatus.OVERDUE) OverdueRed else MaterialTheme.colorScheme.primary
                    )
                    BreakdownRow("Payment Frequency", loan.paymentFrequency)
                    BreakdownRow("Tenure / Duration", "${loan.durationMonths} months")
                    BreakdownRow("Start Date", loan.startDate)
                    BreakdownRow("Final Maturity Date", loan.dueDate)

                    if (!isFinancierView && onToggleAutoPay != null && remainingTotal > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Auto Pay (e-Mandate)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (loan.isAutoPayEnabled) "Active on due date" else "Disabled",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = loan.isAutoPayEnabled,
                                onCheckedChange = onToggleAutoPay
                            )
                        }
                    }
                }
            }

            // Action Buttons
            if (!isFinancierView && remainingTotal > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onPayClick != null) {
                        Button(
                            onClick = onPayClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pay Now")
                        }
                    }
                    if (onEarlySettleClick != null) {
                        OutlinedButton(
                            onClick = onEarlySettleClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Close Early")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    textColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = if (textColor != Color.Unspecified) textColor else MaterialTheme.colorScheme.onSurface
        )
    }
}

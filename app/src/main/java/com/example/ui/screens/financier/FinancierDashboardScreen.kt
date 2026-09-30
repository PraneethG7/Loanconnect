package com.example.ui.screens.financier

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun FinancierDashboardScreen(
    financier: UserEntity,
    loans: List<LoanEntity>,
    payments: List<PaymentEntity>,
    requests: List<LoanRequestEntity>,
    commissions: List<CommissionEntity>,
    onNavigateToRequests: () -> Unit,
    onNavigateToSmartCollections: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToAi: () -> Unit,
    onRecordOfflinePayment: (LoanEntity) -> Unit
) {
    // Financial Summary metrics
    val totalLent = loans.sumOf { it.principalAmount }
    val totalReceived = loans.sumOf { it.totalPaid }
    val totalOutstanding = loans.sumOf { maxOf(0.0, it.totalPayable - it.totalPaid) }
    val totalInterest = loans.sumOf { it.interestPaid }
    val totalCommission = commissions.sumOf { it.amount }
    val netEarnings = totalInterest - totalCommission

    // Loan counts
    val activeLoans = loans.filter { it.status == LoanStatus.ACTIVE }
    val overdueLoans = loans.filter { it.status == LoanStatus.OVERDUE }
    val completedLoans = loans.filter { it.status == LoanStatus.COMPLETED }

    // Today's collection
    val now = System.currentTimeMillis()
    val todayPayments = payments.filter { it.paymentDate >= now - 86400000L }
    val todayCollected = todayPayments.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("financier_dashboard"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card: Financial Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF1E3A8A))
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = financier.businessName.ifEmpty { financier.name },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Financier Portfolio Dashboard",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            VerificationBadge(status = financier.verificationStatus)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Total Capital Outstanding",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = formatCurrency(totalOutstanding),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Grid in Hero
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.Black.copy(alpha = 0.25f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Lent", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(formatCurrency(totalLent), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column {
                                Text("Total Received", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(formatCurrency(totalReceived), color = Color(0xFF67FDCD), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Net Earnings", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(formatCurrency(netEarnings), color = Color(0xFFFDE047), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Collection Highlights (Section 6 & 29)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "Today's Collection",
                    value = formatCurrency(todayCollected),
                    subtitle = "${todayPayments.size} receipts",
                    icon = Icons.Default.CurrencyRupee,
                    iconTint = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Overdue Capital",
                    value = formatCurrency(overdueLoans.sumOf { it.nextInstallmentAmount }),
                    subtitle = "${overdueLoans.size} overdue borrower(s)",
                    icon = Icons.Default.Warning,
                    iconTint = OverdueRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Navigation Grid
        item {
            Text(
                text = "Operations & Management",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FinancierQuickButton(
                    icon = Icons.Default.Inbox,
                    label = "Requests (${requests.size})",
                    color = WarningAmber,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToRequests
                )
                FinancierQuickButton(
                    icon = Icons.Default.TrendingUp,
                    label = "Collections",
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSmartCollections
                )
                FinancierQuickButton(
                    icon = Icons.Default.BarChart,
                    label = "Analytics",
                    color = LoanSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToReports
                )
                FinancierQuickButton(
                    icon = Icons.Default.SmartToy,
                    label = "AI Query",
                    color = InfoCyan,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAi
                )
            }
        }

        // Loan Accounts List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Loan Accounts (${loans.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (overdueLoans.isNotEmpty()) {
                    StatusBadge("OVERDUE: ${overdueLoans.size}")
                }
            }
        }

        if (loans.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No loans disbursed yet.")
                        Text("Check incoming loan requests to send loan offers.", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onNavigateToRequests) {
                            Text("View Loan Requests")
                        }
                    }
                }
            }
        } else {
            items(loans) { loan ->
                Column {
                    FinancialBreakdownCard(
                        loan = loan,
                        isFinancierView = true
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        FilledTonalButton(
                            onClick = { onRecordOfflinePayment(loan) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Money, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Record Cash Payment", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancierQuickButton(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(78.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

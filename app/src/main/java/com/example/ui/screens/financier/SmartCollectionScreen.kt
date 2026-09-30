package com.example.ui.screens.financier

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LoanEntity
import com.example.data.model.LoanStatus
import com.example.data.model.PaymentEntity
import com.example.ui.components.*
import com.example.ui.theme.OverdueRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun SmartCollectionScreen(
    loans: List<LoanEntity>,
    payments: List<PaymentEntity>,
    onRecordOfflinePayment: (LoanEntity) -> Unit
) {
    var paymentLinkLoan by remember { mutableStateOf<LoanEntity?>(null) }
    var qrPaymentLoan by remember { mutableStateOf<LoanEntity?>(null) }

    val now = System.currentTimeMillis()
    val todayPayments = payments.filter { it.paymentDate >= now - 86400000L }
    val totalCollectedToday = todayPayments.sumOf { it.amount }

    val overdueLoans = loans.filter { it.status == LoanStatus.OVERDUE }
    val activeLoans = loans.filter { it.status == LoanStatus.ACTIVE }

    val totalExpectedToday = activeLoans.sumOf { it.nextInstallmentAmount }
    val remainingToCollect = maxOf(0.0, totalExpectedToday - totalCollectedToday)
    val totalOverdue = overdueLoans.sumOf { it.nextInstallmentAmount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("smart_collection_screen")
    ) {
        Text(
            text = "Smart Collection Dashboard",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Track today's collection targets, overdue payments, and record receipts.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Summary Metric Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricStatCard(
                title = "Collected",
                value = formatCurrency(totalCollectedToday),
                icon = Icons.Default.CheckCircle,
                iconTint = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
            MetricStatCard(
                title = "Remaining",
                value = formatCurrency(remainingToCollect),
                icon = Icons.Default.Pending,
                iconTint = WarningAmber,
                modifier = Modifier.weight(1f)
            )
            MetricStatCard(
                title = "Overdue",
                value = formatCurrency(totalOverdue),
                icon = Icons.Default.Warning,
                iconTint = OverdueRed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Borrower Collection Roster",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Overdue section
            if (overdueLoans.isNotEmpty()) {
                item {
                    Text(
                        text = "🔴 Overdue Accounts",
                        fontWeight = FontWeight.Bold,
                        color = OverdueRed,
                        fontSize = 13.sp
                    )
                }
                items(overdueLoans) { loan ->
                    CollectionItemCard(
                        borrowerName = loan.borrowerName,
                        loanId = loan.id,
                        amount = loan.nextInstallmentAmount,
                        statusText = "OVERDUE (Due: ${loan.nextDueDate})",
                        isOverdue = true,
                        onRecordCash = { onRecordOfflinePayment(loan) },
                        onShareLink = { paymentLinkLoan = loan },
                        onShowQr = { qrPaymentLoan = loan }
                    )
                }
            }

            // Active / Upcoming section
            item {
                Text(
                    text = "🟡 Due / Upcoming Installments",
                    fontWeight = FontWeight.Bold,
                    color = WarningAmber,
                    fontSize = 13.sp
                )
            }
            items(activeLoans) { loan ->
                CollectionItemCard(
                    borrowerName = loan.borrowerName,
                    loanId = loan.id,
                    amount = loan.nextInstallmentAmount,
                    statusText = "Due: ${loan.nextDueDate}",
                    isOverdue = false,
                    onRecordCash = { onRecordOfflinePayment(loan) },
                    onShareLink = { paymentLinkLoan = loan },
                    onShowQr = { qrPaymentLoan = loan }
                )
            }

            // Collected today
            if (todayPayments.isNotEmpty()) {
                item {
                    Text(
                        text = "🟢 Collected Today",
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen,
                        fontSize = 13.sp
                    )
                }
                items(todayPayments) { pay ->
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
                                Text(pay.borrowerName, fontWeight = FontWeight.Bold)
                                Text("${pay.loanId} • ${pay.paymentMethod.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(formatCurrency(pay.amount), fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                    }
                }
            }
        }
    }

    // Payment Link Dialog (Section 21)
    paymentLinkLoan?.let { loan ->
        Dialog(onDismissRequest = { paymentLinkLoan = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Secure Payment Link Generated", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "https://pay.loanconnect.io/m/${loan.id}?amt=${loan.nextInstallmentAmount.toInt()}",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Share this link with ${loan.borrowerName} via WhatsApp, SMS, or Email. It allows one-click UPI and Card repayments.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { paymentLinkLoan = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Copy & Share Link")
                    }
                }
            }
        }
    }

    // QR Payment Dialog (Section 20)
    qrPaymentLoan?.let { loan ->
        Dialog(onDismissRequest = { qrPaymentLoan = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Financier Collection QR", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    SimulatedQrCodeView(
                        payeeName = loan.financierName,
                        amount = loan.nextInstallmentAmount,
                        loanId = loan.id
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Ask ${loan.borrowerName} to scan this QR with GPay, PhonePe, Paytm, or BHIM.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { qrPaymentLoan = null }, modifier = Modifier.fillMaxWidth()) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionItemCard(
    borrowerName: String,
    loanId: String,
    amount: Double,
    statusText: String,
    isOverdue: Boolean,
    onRecordCash: () -> Unit,
    onShareLink: () -> Unit,
    onShowQr: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(borrowerName, fontWeight = FontWeight.Bold)
                    Text(loanId, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatCurrency(amount), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverdue) OverdueRed else WarningAmber
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRecordCash,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Money, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cash", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = onShareLink,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Link", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = onShowQr,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("QR", fontSize = 11.sp)
                }
            }
        }
    }
}

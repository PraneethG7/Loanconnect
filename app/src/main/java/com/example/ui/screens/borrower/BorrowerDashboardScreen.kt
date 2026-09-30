package com.example.ui.screens.borrower

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun BorrowerDashboardScreen(
    user: UserEntity,
    loans: List<LoanEntity>,
    payments: List<PaymentEntity>,
    offers: List<LoanOfferEntity>,
    onNavigateToFindFinancier: () -> Unit,
    onNavigateToMyLoans: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    onPayLoan: (LoanEntity) -> Unit,
    onEarlySettle: (LoanEntity) -> Unit,
    onToggleAutoPay: (LoanEntity, Boolean) -> Unit,
    onAcceptOffer: (LoanOfferEntity) -> Unit,
    onRejectOffer: (LoanOfferEntity) -> Unit,
    onViewReceipt: (PaymentEntity) -> Unit
) {
    var showQrModal by remember { mutableStateOf(false) }

    // Computed Financial Metrics
    val totalOutstanding = loans.filter { it.status == LoanStatus.ACTIVE || it.status == LoanStatus.OVERDUE }
        .sumOf { maxOf(0.0, it.totalPayable - it.totalPaid) }
    val totalPaid = loans.sumOf { it.totalPaid }
    val activeLoans = loans.filter { it.status == LoanStatus.ACTIVE || it.status == LoanStatus.OVERDUE }
    val nextLoan = activeLoans.minByOrNull { it.nextDueDate }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("borrower_dashboard"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Financial Header Card
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
                                colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0D9488))
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
                                    text = "Hello, ${user.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = "My Total Outstanding",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = Color(0xFF67FDCD),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "${activeLoans.size} Active Loans",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = formatCurrency(totalOutstanding),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sub stats: Next Payment, Due Date, Total Paid
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.Black.copy(alpha = 0.25f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Next Payment", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(
                                    text = formatCurrency(nextLoan?.nextInstallmentAmount ?: 0.0),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Column {
                                Text("Due Date", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(
                                    text = nextLoan?.nextDueDate ?: "None",
                                    color = Color(0xFFFDE047),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Paid", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(
                                    text = formatCurrency(totalPaid),
                                    color = Color(0xFF67FDCD),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Action Grid / Buttons per Section 7 & 58
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Payment,
                    label = "Pay Now",
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f)
                ) {
                    if (nextLoan != null) onPayLoan(nextLoan) else onNavigateToMyLoans()
                }
                QuickActionButton(
                    icon = Icons.Default.QrCodeScanner,
                    label = "Scan QR",
                    color = InfoCyan,
                    modifier = Modifier.weight(1f)
                ) {
                    showQrModal = true
                }
                QuickActionButton(
                    icon = Icons.Default.Search,
                    label = "Find Financier",
                    color = LoanSecondary,
                    modifier = Modifier.weight(1f)
                ) {
                    onNavigateToFindFinancier()
                }
                QuickActionButton(
                    icon = Icons.Default.SmartToy,
                    label = "Ask AI",
                    color = WarningAmber,
                    modifier = Modifier.weight(1f)
                ) {
                    onNavigateToAi()
                }
            }
        }

        // Secondary Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SecondaryActionChip(Icons.Default.FormatListBulleted, "My Loans", onNavigateToMyLoans, Modifier.weight(1f))
                SecondaryActionChip(Icons.Default.CalendarMonth, "Calendar", onNavigateToCalendar, Modifier.weight(1f))
                SecondaryActionChip(Icons.Default.Calculate, "Calculator", onNavigateToCalculator, Modifier.weight(1f))
                SecondaryActionChip(Icons.Default.HelpCenter, "Support", onNavigateToSupport, Modifier.weight(1f))
            }
        }

        // Pending Offers Section
        val pendingOffers = offers.filter { it.status == OfferStatus.OFFER_SENT }
        if (pendingOffers.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Loan Offers (${pendingOffers.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            items(pendingOffers) { offer ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = offer.financierName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            StatusBadge("OFFER_SENT")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Approved Amount: ${formatCurrency(offer.approvedAmount)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Interest: ${offer.interestRate}% ${offer.interestType.name.lowercase()} | EMI: ${formatCurrency(offer.installmentAmount)}/mo | Tenure: ${offer.durationMonths} months",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Fees: ${formatCurrency(offer.processingFee + offer.platformFee)} | Total Payable: ${formatCurrency(offer.totalPayable)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onRejectOffer(offer) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Reject")
                            }
                            Button(
                                onClick = { onAcceptOffer(offer) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Accept Terms")
                            }
                        }
                    }
                }
            }
        }

        // Active Loans List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Active Loans",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToMyLoans) {
                    Text("View All (${loans.size})")
                }
            }
        }

        if (activeLoans.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Outstanding Debt!", fontWeight = FontWeight.Bold)
                        Text("Explore disclosed financiers to request a new loan.", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onNavigateToFindFinancier) {
                            Text("Find Financiers")
                        }
                    }
                }
            }
        } else {
            items(activeLoans) { loan ->
                FinancialBreakdownCard(
                    loan = loan,
                    isFinancierView = false,
                    onPayClick = { onPayLoan(loan) },
                    onEarlySettleClick = { onEarlySettle(loan) },
                    onToggleAutoPay = { enable -> onToggleAutoPay(loan, enable) }
                )
            }
        }

        // Recent Payment History Preview
        if (payments.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Payments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(payments.take(3)) { payment ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(payment.financierName, fontWeight = FontWeight.SemiBold)
                            Text(payment.loanId, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(payment.paymentMethod.name.replace("_", " "), fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatCurrency(payment.amount), fontWeight = FontWeight.Bold, color = SuccessGreen)
                            StatusBadge(payment.status.name)
                            TextButton(onClick = { onViewReceipt(payment) }) {
                                Text("Receipt", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // QR Payment Scanner Modal simulation
    if (showQrModal) {
        Dialog(onDismissRequest = { showQrModal = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("QR Scanner & UPI Pay", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    SimulatedQrCodeView(
                        payeeName = nextLoan?.financierName ?: "LoanConnect Financier",
                        amount = nextLoan?.nextInstallmentAmount ?: 5000.0,
                        loanId = nextLoan?.id ?: "LOAN-SAMPLE"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Scan this QR code with any UPI app or click to pay directly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { showQrModal = false }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                showQrModal = false
                                if (nextLoan != null) onPayLoan(nextLoan)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Pay via UPI")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
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

@Composable
private fun SecondaryActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(label, fontSize = 10.sp)
        }
    }
}

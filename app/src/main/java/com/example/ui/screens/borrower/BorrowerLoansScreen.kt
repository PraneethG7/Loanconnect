package com.example.ui.screens.borrower

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LoanEntity
import com.example.data.model.LoanStatus
import com.example.ui.components.FinancialBreakdownCard
import com.example.ui.components.formatCurrency

@Composable
fun BorrowerLoansScreen(
    loans: List<LoanEntity>,
    onPayLoan: (LoanEntity) -> Unit,
    onEarlySettle: (LoanEntity) -> Unit,
    onToggleAutoPay: (LoanEntity, Boolean) -> Unit,
    onNavigateToApplyForLoan: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var statementLoan by remember { mutableStateOf<LoanEntity?>(null) }

    val activeLoans = loans.filter { it.status == LoanStatus.ACTIVE || it.status == LoanStatus.OVERDUE }
    val completedLoans = loans.filter { it.status == LoanStatus.COMPLETED || it.status == LoanStatus.SETTLED_EARLY }

    val displayedLoans = if (selectedTab == 0) activeLoans else completedLoans

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("borrower_loans_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "My Loans",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Track your repayment schedules, balances, and statements.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onNavigateToApplyForLoan,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("borrower_loans_apply_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Apply", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Active (${activeLoans.size})", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Completed (${completedLoans.size})", fontWeight = FontWeight.SemiBold) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (displayedLoans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (selectedTab == 0) "No active loans found." else "No completed loans yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (selectedTab == 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToApplyForLoan,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Apply for Loan Now")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(displayedLoans) { loan ->
                    Column {
                        FinancialBreakdownCard(
                            loan = loan,
                            isFinancierView = false,
                            onPayClick = { onPayLoan(loan) },
                            onEarlySettleClick = { onEarlySettle(loan) },
                            onToggleAutoPay = { enable -> onToggleAutoPay(loan, enable) }
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { statementLoan = loan }) {
                                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download Statement", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Statement Dialog (Section 36)
    statementLoan?.let { loan ->
        BorrowerStatementDialog(
            loan = loan,
            onDismiss = { statementLoan = null }
        )
    }
}

@Composable
fun BorrowerStatementDialog(
    loan: LoanEntity,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Official Statement", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(loan.id, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatementItem("Borrower", loan.borrowerName)
                        StatementItem("Financier", loan.financierName)
                        StatementItem("Original Principal", formatCurrency(loan.principalAmount))
                        StatementItem("Interest (${loan.interestRate}%)", formatCurrency(loan.interestAmount))
                        StatementItem("Fees & Platform Charges", formatCurrency(loan.processingFee + loan.platformFee))
                        HorizontalDivider()
                        StatementItem("Total Payable", formatCurrency(loan.totalPayable), isBold = true)
                        StatementItem("Total Paid", formatCurrency(loan.totalPaid), isBold = true)
                        StatementItem(" - Principal Paid", formatCurrency(loan.principalPaid))
                        StatementItem(" - Interest Paid", formatCurrency(loan.interestPaid))
                        HorizontalDivider()
                        StatementItem("Remaining Principal", formatCurrency(maxOf(0.0, loan.principalAmount - loan.principalPaid)))
                        StatementItem("Remaining Total", formatCurrency(maxOf(0.0, loan.totalPayable - loan.totalPaid)), isBold = true)
                        StatementItem("Status", loan.status.name)
                        StatementItem("Start Date", loan.startDate)
                        StatementItem("Maturity Date", loan.dueDate)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export Statement PDF")
                }
            }
        }
    }
}

@Composable
private fun StatementItem(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium)
    }
}

package com.example.ui.screens.admin

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
import com.example.data.model.*
import com.example.ui.components.MetricStatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatCurrency
import com.example.ui.theme.LoanPrimary
import com.example.ui.theme.OverdueRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun AdminDashboardScreen(
    users: List<UserEntity>,
    loans: List<LoanEntity>,
    payments: List<PaymentEntity>,
    commissions: List<CommissionEntity>,
    reports: List<UserReportEntity>,
    tickets: List<SupportTicketEntity>,
    auditLogs: List<AuditLogEntity>,
    onVerifyFinancier: (financierId: String, status: VerificationStatus) -> Unit
) {
    var selectedSection by remember { mutableIntStateOf(0) }

    val totalVolume = loans.sumOf { it.principalAmount }
    val totalPlatformCommission = commissions.sumOf { it.amount }
    val financiers = users.filter { it.role == UserRole.FINANCIER }
    val borrowers = users.filter { it.role == UserRole.BORROWER }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_dashboard_screen")
    ) {
        Text(
            text = "Platform Admin HQ",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Regulatory oversight, KYC verification, dispute resolution, and audit logs.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Platform Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricStatCard(
                title = "Total Volume",
                value = formatCurrency(totalVolume),
                icon = Icons.Default.CurrencyRupee,
                iconTint = LoanPrimary,
                modifier = Modifier.weight(1f)
            )
            MetricStatCard(
                title = "Platform Fees",
                value = formatCurrency(totalPlatformCommission),
                icon = Icons.Default.Receipt,
                iconTint = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
            MetricStatCard(
                title = "Financiers",
                value = financiers.size.toString(),
                subtitle = "${borrowers.size} Borrowers",
                icon = Icons.Default.People,
                iconTint = WarningAmber,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedSection,
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(selected = selectedSection == 0, onClick = { selectedSection = 0 }, text = { Text("Verifications (${financiers.size})") })
            Tab(selected = selectedSection == 1, onClick = { selectedSection = 1 }, text = { Text("Reports (${reports.size})") })
            Tab(selected = selectedSection == 2, onClick = { selectedSection = 2 }, text = { Text("Tickets (${tickets.size})") })
            Tab(selected = selectedSection == 3, onClick = { selectedSection = 3 }, text = { Text("Audit Trail (${auditLogs.size})") })
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (selectedSection) {
                0 -> {
                    // Financier Verification Queue
                    items(financiers) { f ->
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
                                        Text(f.businessName.ifEmpty { f.name }, fontWeight = FontWeight.Bold)
                                        Text("${f.name} • ${f.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("KYC Doc: ${f.kycDocumentType}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                    StatusBadge(f.verificationStatus.name)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onVerifyFinancier(f.id, VerificationStatus.VERIFIED) },
                                        enabled = f.verificationStatus != VerificationStatus.VERIFIED,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Verify")
                                    }
                                    OutlinedButton(
                                        onClick = { onVerifyFinancier(f.id, VerificationStatus.SUSPENDED) },
                                        enabled = f.verificationStatus != VerificationStatus.SUSPENDED,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Suspend")
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Reports
                    if (reports.isEmpty()) {
                        item {
                            Text("No pending dispute reports.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        items(reports) { rep ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Report: ${rep.category}", fontWeight = FontWeight.Bold, color = OverdueRed)
                                        StatusBadge(rep.status)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Reporter: ${rep.reporterName} → Reported: ${rep.reportedUserName}", style = MaterialTheme.typography.bodySmall)
                                    Text("Description: \"${rep.description}\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Support Tickets
                    if (tickets.isEmpty()) {
                        item {
                            Text("No support tickets submitted.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        items(tickets) { tck ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(tck.category, fontWeight = FontWeight.Bold)
                                        StatusBadge(tck.status.name)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("User: ${tck.userName}", style = MaterialTheme.typography.bodySmall)
                                    Text("Issue: ${tck.description}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (tck.resolutionNote.isNotEmpty()) {
                                        Text("Resolution: ${tck.resolutionNote}", fontSize = 11.sp, color = SuccessGreen)
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Audit Logs
                    items(auditLogs) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(log.action, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    Text(log.entityType, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                Text("${log.userName}: ${log.details}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

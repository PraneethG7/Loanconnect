package com.example.ui.screens.common

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SupportTicketEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.LoanPrimary
import com.example.ui.theme.OverdueRed
import com.example.ui.theme.WarningAmber

@Composable
fun HelpAndSupportScreen(
    tickets: List<SupportTicketEntity>,
    onSubmitTicket: (category: String, desc: String, loanId: String) -> Unit,
    onSubmitReport: (reportedName: String, category: String, desc: String) -> Unit
) {
    var showCreateTicketDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showDisclosuresDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("help_support_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Help & Support Center",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "24/7 dedicated fintech grievance redressal and fraud prevention.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Safety Warnings Card (Section 50)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = WarningAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LoanConnect Safety Protocol",
                            fontWeight = FontWeight.Bold,
                            color = WarningAmber
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Never share your Password, OTP, or UPI PIN with anyone, including support staff.", fontSize = 12.sp)
                    Text("• Never transfer money outside supported LoanConnect payment flows.", fontSize = 12.sp)
                    Text("• Carefully inspect interest rate and tenure terms before accepting any loan agreement.", fontSize = 12.sp)
                }
            }
        }

        // Support Channels (Section 45)
        item {
            Text(
                text = "Official Support Channels",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Helpline", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("1800-LOAN-CONNECT", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Email Support", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("support@loanconnect.io", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Action Buttons: Raise Ticket or Report User
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showCreateTicketDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Raise Ticket")
                }
                OutlinedButton(
                    onClick = { showReportDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Report, contentDescription = null, modifier = Modifier.size(16.dp), tint = OverdueRed)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Report User", color = OverdueRed)
                }
            }
        }

        // Regulatory & Loan Terms Disclosure Button
        item {
            OutlinedButton(
                onClick = { showDisclosuresDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Policy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Regulatory & Personal Loan Disclosures (APR & Terms)")
            }
        }

        // My Tickets Section (Section 46)
        item {
            Text(
                text = "My Support Tickets (${tickets.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (tickets.isEmpty()) {
            item {
                Text("No support tickets raised.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(tickets) { tck ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(tck.id, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = LoanPrimary)
                            StatusBadge(tck.status.name)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(tck.category, fontWeight = FontWeight.Bold)
                        Text(tck.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (tck.resolutionNote.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Agent Resolution: ${tck.resolutionNote}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    // Create Ticket Dialog
    if (showCreateTicketDialog) {
        var category by remember { mutableStateOf("Payment Deducted but Not Reflected") }
        var description by remember { mutableStateOf("") }
        var loanId by remember { mutableStateOf("") }

        val categories = listOf(
            "Payment Failed",
            "Payment Deducted but Not Reflected",
            "Duplicate Payment",
            "Incorrect Balance",
            "Wrong Loan Information",
            "Suspicious Activity"
        )

        Dialog(onDismissRequest = { showCreateTicketDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Raise Grievance Ticket", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                    Text("Select Category", style = MaterialTheme.typography.labelMedium)
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }

                    OutlinedTextField(
                        value = loanId,
                        onValueChange = { loanId = it },
                        label = { Text("Related Loan ID (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Describe the issue in detail") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Button(
                        onClick = {
                            if (description.isNotBlank()) {
                                onSubmitTicket(category, description, loanId)
                                showCreateTicketDialog = false
                            }
                        },
                        enabled = description.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit Ticket")
                    }
                }
            }
        }
    }

    // Report Dialog (Section 47 & 49)
    if (showReportDialog) {
        var reportedName by remember { mutableStateOf("") }
        var reportCat by remember { mutableStateOf("Suspicious Activity") }
        var reportDesc by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showReportDialog = false }) {
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Report Account to Admin", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = OverdueRed)
                    OutlinedTextField(
                        value = reportedName,
                        onValueChange = { reportedName = it },
                        label = { Text("Person or Business Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reportDesc,
                        onValueChange = { reportDesc = it },
                        label = { Text("Violation Reason / Complaint") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    Button(
                        onClick = {
                            if (reportedName.isNotBlank() && reportDesc.isNotBlank()) {
                                onSubmitReport(reportedName, reportCat, reportDesc)
                                showReportDialog = false
                            }
                        },
                        enabled = reportedName.isNotBlank() && reportDesc.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = OverdueRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit Formal Report")
                    }
                }
            }
        }
    }

    // Regulatory & Google Play Personal Loans Disclosure Dialog
    if (showDisclosuresDialog) {
        Dialog(onDismissRequest = { showDisclosuresDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Regulatory Disclosures", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showDisclosuresDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    }

                    item {
                        Text("Google Play Financial Services & Personal Loans Compliance", fontWeight = FontWeight.Bold, color = LoanPrimary, fontSize = 13.sp)
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("• Minimum Repayment Period: 61 days (3 months)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("• Maximum Repayment Period: 36 months (3 years)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("• Annual Percentage Rate (APR): 9.0% to 24.0% per annum", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("• Processing Fees: 1.0% to 2.0% (one-time)", fontSize = 12.sp)
                                Text("• Platform Technology Fee: 0.5%", fontSize = 12.sp)
                                Text("• Zero short-term payday loans: LoanConnect does not support loans requiring full repayment in 60 days or less.", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    item {
                        Text("Representative Example of Total Loan Cost:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Principal Amount Borrowed: ₹50,000", fontSize = 12.sp)
                                Text("Tenure: 12 Months", fontSize = 12.sp)
                                Text("Interest Rate (APR): 12% per annum", fontSize = 12.sp)
                                Text("Processing Fee (1%): ₹500", fontSize = 12.sp)
                                Text("Platform Fee: ₹250", fontSize = 12.sp)
                                Text("Total Interest Payable: ₹6,000", fontSize = 12.sp)
                                Text("Total Amount Payable: ₹56,750", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LoanPrimary)
                                Text("Monthly Installment (EMI): ₹4,729", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item {
                        Text("Data Privacy & Security:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "All customer financial information is strictly isolated. We do not store raw payment card data, UPI PINs, or banking passwords. End-to-end encryption is used for all transactions.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    item {
                        Button(
                            onClick = { showDisclosuresDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("I Understand")
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens.financier

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatCurrency
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LoanRequestsScreen(
    requests: List<LoanRequestEntity>,
    offers: List<LoanOfferEntity>,
    onCreateOffer: (
        request: LoanRequestEntity,
        approvedAmount: Double,
        interestRate: Double,
        interestType: InterestType,
        durationMonths: Int,
        installmentAmount: Double,
        processingFee: Double,
        platformFee: Double,
        totalPayable: Double,
        termsText: String
    ) -> Unit
) {
    var selectedRequestToOffer by remember { mutableStateOf<LoanRequestEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("loan_requests_screen")
    ) {
        Text(
            text = "Loan Requests & Offers",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Review borrower applications, assess requirements, and structure loan offers.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (requests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No incoming loan requests at the moment.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(requests) { req ->
                    val hasOffer = offers.any { it.requestId == req.id }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(req.borrowerName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("Request: ${req.id}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                                StatusBadge(req.status.name)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Requested: ${formatCurrency(req.requestedAmount)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Purpose: ${req.purpose}", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                            Text("Preferred Tenure: ${req.durationMonths} months • Frequency: ${req.paymentFrequency}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            if (req.message.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Borrower Note: \"${req.message}\"", style = MaterialTheme.typography.bodySmall, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (!hasOffer) {
                                Button(
                                    onClick = { selectedRequestToOffer = req },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Structure & Send Offer")
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "✓ Loan offer sent to borrower. Awaiting borrower agreement.",
                                        modifier = Modifier.padding(8.dp),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Offer Dialog
    selectedRequestToOffer?.let { req ->
        CreateOfferDialog(
            request = req,
            onDismiss = { selectedRequestToOffer = null },
            onSubmit = { amount, rate, type, duration, emi, procFee, platFee, total, terms ->
                onCreateOffer(req, amount, rate, type, duration, emi, procFee, platFee, total, terms)
                selectedRequestToOffer = null
            }
        )
    }
}

@Composable
fun CreateOfferDialog(
    request: LoanRequestEntity,
    onDismiss: () -> Unit,
    onSubmit: (
        amount: Double,
        rate: Double,
        type: InterestType,
        duration: Int,
        installment: Double,
        procFee: Double,
        platFee: Double,
        total: Double,
        terms: String
    ) -> Unit
) {
    var approvedAmountText by remember { mutableStateOf(request.requestedAmount.toInt().toString()) }
    var interestRateText by remember { mutableStateOf("10.0") }
    var interestType by remember { mutableStateOf(InterestType.MONTHLY) }
    var durationText by remember { mutableStateOf(request.durationMonths.toString()) }
    var procFeeText by remember { mutableStateOf("500") }
    var termsText by remember { mutableStateOf("Disbursement within 2 hours of acceptance. Prepayment permitted with zero penalty.") }

    val amount = approvedAmountText.toDoubleOrNull() ?: 0.0
    val rate = interestRateText.toDoubleOrNull() ?: 10.0
    val duration = durationText.toIntOrNull() ?: 12
    val procFee = procFeeText.toDoubleOrNull() ?: 0.0
    val platFee = 250.0

    // Calculation
    val totalInterest = if (interestType == InterestType.MONTHLY) {
        amount * (rate / 100.0)
    } else {
        amount * (rate / 100.0) * (duration / 12.0)
    }
    val totalPayable = amount + totalInterest + procFee + platFee
    val installmentAmount = if (duration > 0) totalPayable / duration else 0.0

    Dialog(onDismissRequest = onDismiss) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Create Loan Offer", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("For ${request.borrowerName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }

                OutlinedTextField(
                    value = approvedAmountText,
                    onValueChange = { approvedAmountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Approved Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = interestRateText,
                        onValueChange = { interestRateText = it },
                        label = { Text("Rate (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Tenure (Mos)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Summary
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Estimated Monthly EMI", style = MaterialTheme.typography.bodySmall)
                            Text(formatCurrency(installmentAmount), fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Payable", style = MaterialTheme.typography.bodySmall)
                            Text(formatCurrency(totalPayable), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Button(
                    onClick = {
                        onSubmit(amount, rate, interestType, duration, installmentAmount, procFee, platFee, totalPayable, termsText)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Dispatch Loan Offer")
                }
            }
        }
    }
}

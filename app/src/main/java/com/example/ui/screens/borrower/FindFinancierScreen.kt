package com.example.ui.screens.borrower

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.InterestType
import com.example.data.model.UserEntity
import com.example.data.model.VerificationStatus
import com.example.ui.components.VerificationBadge
import com.example.ui.components.formatCurrency
import com.example.ui.theme.LoanPrimary

@Composable
fun FindFinancierScreen(
    financiers: List<UserEntity>,
    onRequestLoan: (financier: UserEntity, amount: Double, purpose: String, duration: Int, message: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedAmountFilter by remember { mutableStateOf<Double?>(null) }
    var selectedRateFilter by remember { mutableStateOf<Double?>(null) }
    var showOnlyVerified by remember { mutableStateOf(false) }

    // Selected for comparison
    val selectedForComparison = remember { mutableStateListOf<UserEntity>() }
    var showComparisonDialog by remember { mutableStateOf(false) }

    // Request Money Dialog
    var financierToRequest by remember { mutableStateOf<UserEntity?>(null) }

    // Filter logic
    val filteredFinanciers = financiers.filter { f ->
        val matchesQuery = f.businessName.contains(searchQuery, ignoreCase = true) ||
                f.name.contains(searchQuery, ignoreCase = true) ||
                f.serviceArea.contains(searchQuery, ignoreCase = true)
        val matchesAmount = selectedAmountFilter == null || (selectedAmountFilter!! >= f.minLoanAmount && selectedAmountFilter!! <= f.maxLoanAmount)
        val matchesRate = selectedRateFilter == null || f.standardInterestRate <= selectedRateFilter!!
        val matchesVerified = !showOnlyVerified || f.verificationStatus == VerificationStatus.VERIFIED
        matchesQuery && matchesAmount && matchesRate && matchesVerified
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("find_financier_screen")
    ) {
        Text(
            text = "Financier Marketplace",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Browse verified financiers and compare publicly disclosed loan terms.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, region, or city...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedAmountFilter == 50000.0,
                onClick = { selectedAmountFilter = if (selectedAmountFilter == 50000.0) null else 50000.0 },
                label = { Text("₹50,000") }
            )
            FilterChip(
                selected = selectedRateFilter == 10.0,
                onClick = { selectedRateFilter = if (selectedRateFilter == 10.0) null else 10.0 },
                label = { Text("≤ 10% Rate") }
            )
            FilterChip(
                selected = showOnlyVerified,
                onClick = { showOnlyVerified = !showOnlyVerified },
                label = { Text("Verified Only") }
            )
        }

        // Compare Bar
        AnimatedVisibility(visible = selectedForComparison.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedForComparison.size} selected to compare",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { selectedForComparison.clear() }) {
                            Text("Clear")
                        }
                        Button(
                            onClick = { showComparisonDialog = true },
                            enabled = selectedForComparison.size >= 2
                        ) {
                            Text("Compare Terms")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Financier List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredFinanciers) { financier ->
                val isSelectedForCompare = selectedForComparison.any { it.id == financier.id }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = financier.businessName.ifEmpty { financier.name },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Contact: ${financier.name} • ${financier.phone}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Service Area: ${financier.serviceArea}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            VerificationBadge(status = financier.verificationStatus)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Financial Specs Grid
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Amount Range", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${formatCurrency(financier.minLoanAmount)} - ${formatCurrency(financier.maxLoanAmount)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Standard Rate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${financier.standardInterestRate}% (${financier.standardInterestType.name.lowercase()})", fontWeight = FontWeight.Bold, color = LoanPrimary, style = MaterialTheme.typography.bodySmall)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Payment Frequency", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Monthly / Flexible", style = MaterialTheme.typography.bodySmall)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Supported Methods", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(financier.supportedMethods, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (isSelectedForCompare) {
                                        selectedForComparison.removeAll { it.id == financier.id }
                                    } else {
                                        if (selectedForComparison.size < 3) selectedForComparison.add(financier)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (isSelectedForCompare) "Remove" else "Compare")
                            }
                            Button(
                                onClick = { financierToRequest = financier },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Request Money")
                            }
                        }
                    }
                }
            }
        }
    }

    // Comparison Dialog
    if (showComparisonDialog) {
        ComparisonDialog(
            financiers = selectedForComparison,
            onDismiss = { showComparisonDialog = false },
            onRequestMoney = { financier ->
                showComparisonDialog = false
                financierToRequest = financier
            }
        )
    }

    // Request Money Dialog
    financierToRequest?.let { f ->
        RequestMoneyDialog(
            financier = f,
            onDismiss = { financierToRequest = null },
            onSubmit = { amount, purpose, duration, message ->
                onRequestLoan(f, amount, purpose, duration, message)
                financierToRequest = null
            }
        )
    }
}

@Composable
fun ComparisonDialog(
    financiers: List<UserEntity>,
    onDismiss: () -> Unit,
    onRequestMoney: (UserEntity) -> Unit
) {
    val sampleAmount = 50000.0
    val sampleDuration = 12

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
                    Text(
                        text = "Compare Loan Terms",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }

                Text(
                    text = "Assumption: Sample ₹50,000 for 12 months duration",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Comparison Columns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    financiers.forEach { f ->
                        val interestVal = if (f.standardInterestType == InterestType.MONTHLY) {
                            sampleAmount * (f.standardInterestRate / 100.0)
                        } else {
                            sampleAmount * (f.standardInterestRate / 100.0) * (sampleDuration / 12.0)
                        }
                        val feeVal = 500.0
                        val totalPayable = sampleAmount + interestVal + feeVal

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = f.businessName.ifEmpty { f.name },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                                HorizontalDivider()
                                Text("Rate: ${f.standardInterestRate}%", fontSize = 11.sp)
                                Text("Type: ${f.standardInterestType.name}", fontSize = 10.sp)
                                Text("Fees: ₹500", fontSize = 11.sp)
                                Text(
                                    "Total: ${formatCurrency(totalPayable)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = { onRequestMoney(f) },
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 6.dp)
                                ) {
                                    Text("Select", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestMoneyDialog(
    financier: UserEntity,
    onDismiss: () -> Unit,
    onSubmit: (amount: Double, purpose: String, duration: Int, message: String) -> Unit
) {
    var amountText by remember { mutableStateOf("50000") }
    var purpose by remember { mutableStateOf("Personal Needs & Education") }
    var durationText by remember { mutableStateOf("12") }
    var message by remember { mutableStateOf("") }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val duration = durationText.toIntOrNull() ?: 12
    val isValid = amount >= financier.minLoanAmount && amount <= financier.maxLoanAmount && duration > 0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Request Money",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "To: ${financier.businessName.ifEmpty { financier.name }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Requested Amount (₹)") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    supportingText = {
                        Text("Allowed: ${formatCurrency(financier.minLoanAmount)} - ${formatCurrency(financier.maxLoanAmount)}")
                    }
                )

                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Loan Purpose") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Preferred Tenure (Months)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message / Note for Financier (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Button(
                    onClick = {
                        if (isValid) {
                            onSubmit(amount, purpose, duration, message)
                        }
                    },
                    enabled = isValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Loan Request", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

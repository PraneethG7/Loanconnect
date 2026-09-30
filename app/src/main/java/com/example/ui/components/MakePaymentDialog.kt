package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LoanEntity
import com.example.data.model.PaymentMethod
import com.example.ui.theme.SuccessGreen

@Composable
fun MakePaymentDialog(
    loan: LoanEntity,
    onDismiss: () -> Unit,
    onConfirmPayment: (amount: Double, method: PaymentMethod, notes: String) -> Unit,
    isOfflineRecord: Boolean = false
) {
    val remainingTotal = maxOf(0.0, loan.totalPayable - loan.totalPaid)
    var amountText by remember { mutableStateOf(loan.nextInstallmentAmount.coerceAtMost(remainingTotal).toInt().toString()) }
    var selectedMethod by remember { mutableStateOf(if (isOfflineRecord) PaymentMethod.CASH else PaymentMethod.UPI) }
    var notes by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val isValid = amount > 0 && amount <= remainingTotal

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isOfflineRecord) "Record Offline Payment" else "Make Payment",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Loan: ${loan.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Balance summary
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Outstanding", style = MaterialTheme.typography.labelSmall)
                            Text(formatCurrency(remainingTotal), fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Regular Installment", style = MaterialTheme.typography.labelSmall)
                            Text(formatCurrency(loan.nextInstallmentAmount), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Amount input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Payment Amount (₹)") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = !isValid && amountText.isNotEmpty(),
                    supportingText = {
                        if (!isValid && amountText.isNotEmpty()) {
                            Text("Amount must be between ₹1 and ${formatCurrency(remainingTotal)}")
                        } else {
                            Text("Remaining after payment: ${formatCurrency(maxOf(0.0, remainingTotal - amount))}")
                        }
                    }
                )

                // Quick Amount Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { amountText = loan.nextInstallmentAmount.coerceAtMost(remainingTotal).toInt().toString() },
                        label = { Text("EMI (${formatCurrency(loan.nextInstallmentAmount)})") }
                    )
                    AssistChip(
                        onClick = { amountText = (loan.nextInstallmentAmount / 2).coerceAtMost(remainingTotal).toInt().toString() },
                        label = { Text("Partial") }
                    )
                    AssistChip(
                        onClick = { amountText = remainingTotal.toInt().toString() },
                        label = { Text("Full Payoff") }
                    )
                }

                // Payment Method Selector
                Text(
                    text = "Select Payment Method",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val methods = if (isOfflineRecord) {
                        listOf(PaymentMethod.CASH, PaymentMethod.BANK_TRANSFER)
                    } else {
                        listOf(PaymentMethod.UPI, PaymentMethod.BANK_TRANSFER, PaymentMethod.CARD, PaymentMethod.NET_BANKING)
                    }

                    methods.forEach { method ->
                        val isSelected = selectedMethod == method
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMethod = method }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = getMethodIcon(method),
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = method.name.replace("_", " "),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Notes / Reference
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Transaction Reference / Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Confirm Button
                Button(
                    onClick = {
                        if (isValid) {
                            isProcessing = true
                            onConfirmPayment(amount, selectedMethod, notes)
                        }
                    },
                    enabled = isValid && !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text(
                            text = if (isOfflineRecord) "Record Offline Payment" else "Authorize & Pay ${formatCurrency(amount)}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun getMethodIcon(method: PaymentMethod): ImageVector {
    return when (method) {
        PaymentMethod.UPI -> Icons.Default.QrCode
        PaymentMethod.BANK_TRANSFER -> Icons.Default.AccountBalance
        PaymentMethod.CARD -> Icons.Default.CreditCard
        PaymentMethod.NET_BANKING -> Icons.Default.Language
        PaymentMethod.CASH -> Icons.Default.Money
        PaymentMethod.AUTO_PAY -> Icons.Default.Autorenew
    }
}

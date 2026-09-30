package com.example.ui.screens.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InterestType
import com.example.ui.components.formatCurrency
import com.example.ui.theme.LoanPrimary

@Composable
fun LoanCalculatorScreen() {
    var principalText by remember { mutableStateOf("50000") }
    var interestRateText by remember { mutableStateOf("10.0") }
    var durationMonthsText by remember { mutableStateOf("12") }
    var selectedInterestType by remember { mutableStateOf(InterestType.MONTHLY) }

    val principal = principalText.toDoubleOrNull() ?: 50000.0
    val rate = interestRateText.toDoubleOrNull() ?: 10.0
    val duration = (durationMonthsText.toIntOrNull() ?: 12).coerceAtLeast(1)

    // Calculation logic (Section 18)
    val interestAmount = when (selectedInterestType) {
        InterestType.MONTHLY -> principal * (rate / 100.0)
        InterestType.YEARLY -> principal * (rate / 100.0) * (duration / 12.0)
        InterestType.FIXED -> principal * (rate / 100.0)
        InterestType.NONE -> 0.0
    }
    val fees = 500.0
    val totalPayable = principal + interestAmount + fees
    val installmentAmount = totalPayable / duration

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("loan_calculator_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Calculate, contentDescription = null, tint = LoanPrimary, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Interactive Loan Calculator",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Simulate interest rates, repayment tenures, and monthly installment obligations.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Calculated Monthly Installment", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = formatCurrency(installmentAmount),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Original Principal", style = MaterialTheme.typography.bodySmall)
                        Text(formatCurrency(principal), fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Calculated Interest", style = MaterialTheme.typography.bodySmall)
                        Text(formatCurrency(interestAmount), fontWeight = FontWeight.Bold, color = LoanPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Standard Processing Fees", style = MaterialTheme.typography.bodySmall)
                        Text(formatCurrency(fees), fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Amount Payable", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(formatCurrency(totalPayable), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Inputs
        item {
            OutlinedTextField(
                value = principalText,
                onValueChange = { principalText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Loan Principal (₹)") },
                leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = interestRateText,
                    onValueChange = { interestRateText = it },
                    label = { Text("Interest Rate (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = durationMonthsText,
                    onValueChange = { durationMonthsText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Duration (Months)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }

        // Interest Type Selector
        item {
            Text("Interest Calculation Method", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                InterestType.values().forEach { type ->
                    FilterChip(
                        selected = selectedInterestType == type,
                        onClick = { selectedInterestType = type },
                        label = { Text(type.name, fontSize = 10.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Amortization Schedule Preview
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Repayment Schedule Preview ($duration Months)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        val schedulePreview = (1..duration.coerceAtMost(12)).map { month ->
            Triple("Month $month", installmentAmount, totalPayable - (installmentAmount * month))
        }

        items(schedulePreview) { item ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.first, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Text("EMI: ${formatCurrency(item.second)}", fontSize = 12.sp)
                    Text("Balance: ${formatCurrency(maxOf(0.0, item.third))}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

package com.example.ui.screens.bank

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BankAccountEntity
import com.example.ui.theme.LoanPrimary
import com.example.ui.theme.OverdueRed
import com.example.ui.theme.SuccessGreen

@Composable
fun ConnectBankDialog(
    currentBankAccounts: List<BankAccountEntity>,
    onDismiss: () -> Unit,
    onConnectBank: (
        bankName: String,
        accountNumber: String,
        ifscCode: String,
        accountHolderName: String,
        accountType: String,
        upiId: String,
        isPrimary: Boolean
    ) -> Unit,
    onDeleteBankAccount: (BankAccountEntity) -> Unit,
    onSetPrimary: (String) -> Unit
) {
    var isAddingNew by remember { mutableStateOf(currentBankAccounts.isEmpty()) }

    var selectedBankName by remember { mutableStateOf("HDFC Bank") }
    var accountNumber by remember { mutableStateOf("") }
    var confirmAccountNumber by remember { mutableStateOf("") }
    var ifscCode by remember { mutableStateOf("HDFC0000240") }
    var accountHolderName by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf("Savings") }
    var upiId by remember { mutableStateOf("") }
    var isPrimary by remember { mutableStateOf(true) }

    var isVerifyingPennyDrop by remember { mutableStateOf(false) }
    var verificationSuccess by remember { mutableStateOf(false) }

    val popularBanks = listOf(
        "HDFC Bank",
        "State Bank of India",
        "ICICI Bank",
        "Axis Bank",
        "Kotak Mahindra Bank",
        "Punjab National Bank",
        "Bank of Baroda"
    )

    val isFormValid = accountNumber.length >= 8 &&
            accountNumber == confirmAccountNumber &&
            ifscCode.length >= 6 &&
            accountHolderName.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(LoanPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = LoanPrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Linked Bank Accounts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("For Direct Pay & Disbursals", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // Existing Linked Accounts List
                if (currentBankAccounts.isNotEmpty() && !isAddingNew) {
                    item {
                        Text("Active Connected Accounts", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    items(currentBankAccounts) { account ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(account.bankName, fontWeight = FontWeight.Bold)
                                        if (account.isPrimary) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = LoanPrimary.copy(alpha = 0.15f)
                                            ) {
                                                Text("Primary", color = LoanPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                    Text("A/C: •••• ${account.accountNumberLast4} • ${account.accountType}", style = MaterialTheme.typography.bodySmall)
                                    Text("IFSC: ${account.ifscCode} • UPI: ${account.upiId}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!account.isPrimary) {
                                        TextButton(onClick = { onSetPrimary(account.id) }) {
                                            Text("Make Primary", fontSize = 11.sp)
                                        }
                                    }
                                    IconButton(onClick = { onDeleteBankAccount(account) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = OverdueRed, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { isAddingNew = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connect Another Bank Account")
                        }
                    }
                } else {
                    // ADD NEW BANK ACCOUNT FORM
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Add New Bank Account", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (currentBankAccounts.isNotEmpty()) {
                                TextButton(onClick = { isAddingNew = false }) {
                                    Text("View Existing")
                                }
                            }
                        }
                    }

                    item {
                        Text("Select Bank", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(popularBanks) { bName ->
                                FilterChip(
                                    selected = selectedBankName == bName,
                                    onClick = { selectedBankName = bName },
                                    label = { Text(bName, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = accountHolderName,
                            onValueChange = { accountHolderName = it },
                            label = { Text("Account Holder Name (as per bank)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = accountNumber,
                            onValueChange = { accountNumber = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Bank Account Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = confirmAccountNumber,
                            onValueChange = { confirmAccountNumber = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Re-enter Account Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            isError = confirmAccountNumber.isNotEmpty() && confirmAccountNumber != accountNumber,
                            supportingText = {
                                if (confirmAccountNumber.isNotEmpty() && confirmAccountNumber != accountNumber) {
                                    Text("Account numbers do not match", color = OverdueRed)
                                }
                            }
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = ifscCode,
                            onValueChange = { ifscCode = it.uppercase() },
                            label = { Text("Bank IFSC Code (11 characters)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = upiId,
                            onValueChange = { upiId = it },
                            label = { Text("Linked UPI ID (e.g., name@okhdfcbank)") },
                            leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null) },
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
                            FilterChip(
                                selected = accountType == "Savings",
                                onClick = { accountType = "Savings" },
                                label = { Text("Savings Account") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = accountType == "Current",
                                onClick = { accountType = "Current" },
                                label = { Text("Current Account") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Set as Primary Account", style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = isPrimary, onCheckedChange = { isPrimary = it })
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                isVerifyingPennyDrop = true
                                // Simulated micro-deposit penny drop verification
                                onConnectBank(
                                    selectedBankName,
                                    accountNumber,
                                    ifscCode,
                                    accountHolderName,
                                    accountType,
                                    upiId,
                                    isPrimary
                                )
                                isVerifyingPennyDrop = false
                                verificationSuccess = true
                                onDismiss()
                            },
                            enabled = isFormValid && !isVerifyingPennyDrop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isVerifyingPennyDrop) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verify & Connect Bank Account")
                            }
                        }
                    }
                }
            }
        }
    }
}

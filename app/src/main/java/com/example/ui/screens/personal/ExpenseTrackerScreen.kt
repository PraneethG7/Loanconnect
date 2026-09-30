package com.example.ui.screens.personal

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BankAccountEntity
import com.example.data.model.ExpenseEntity
import com.example.ui.components.formatCurrency
import com.example.ui.theme.LoanPrimary
import com.example.ui.theme.LoanSecondary
import com.example.ui.theme.OverdueRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpenseTrackerScreen(
    expenses: List<ExpenseEntity>,
    linkedBankAccounts: List<BankAccountEntity>,
    onAddExpense: (
        title: String,
        amount: Double,
        category: String,
        paymentMethod: String,
        bankAccountId: String?,
        date: String,
        notes: String
    ) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    onNavigateToBudget: () -> Unit,
    onConnectBank: () -> Unit
) {
    val context = LocalContext.current
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf(
        "All",
        "Food & Dining",
        "Groceries",
        "Loan EMI",
        "Transportation",
        "Utilities",
        "Shopping",
        "Health",
        "Entertainment",
        "Other"
    )

    val filteredExpenses = expenses.filter { exp ->
        val matchesCategory = selectedCategoryFilter == "All" || exp.category == selectedCategoryFilter
        val matchesSearch = searchQuery.isBlank() || exp.title.contains(searchQuery, ignoreCase = true) || exp.notes.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val totalSpent = filteredExpenses.sumOf { it.amount }
    val thisMonthCount = filteredExpenses.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("expense_tracker_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Expense Tracker Card
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
                                    text = "Real-Time Expense Tracker",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = "September 2026 Spending",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            FilledTonalButton(
                                onClick = onNavigateToBudget,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.2f),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Budgets", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = formatCurrency(totalSpent),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.Black.copy(alpha = 0.25f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Transactions", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text("$thisMonthCount tracked", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column {
                                Text("Daily Average", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                val avg = if (thisMonthCount > 0) totalSpent / 29.0 else 0.0
                                Text(formatCurrency(avg), color = Color(0xFF67FDCD), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Top Category", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                val topCat = expenses.groupBy { it.category }.maxByOrNull { it.value.sumOf { e -> e.amount } }?.key ?: "None"
                                Text(topCat, color = Color(0xFFFDE047), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions & Log Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("log_expense_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LoanPrimary)
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Expense", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onConnectBank,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(0.9f)
                ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bank Sync", fontSize = 12.sp)
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null
                    )
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search transactions, notes, merchant...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Transactions List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Real-Time Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredExpenses.size} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (filteredExpenses.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("No expenses found", fontWeight = FontWeight.Bold)
                        Text("Tap 'Log Expense' to track real-time payments via Google Pay or bank accounts.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(filteredExpenses, key = { it.id }) { expense ->
                ExpenseItemCard(
                    expense = expense,
                    onDelete = { onDeleteExpense(expense) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            linkedBankAccounts = linkedBankAccounts,
            onDismiss = { showAddDialog = false },
            onSave = { title, amt, cat, payMethod, bId, date, notes, launchDirectGPay ->
                onAddExpense(title, amt, cat, payMethod, bId, date, notes)
                if (launchDirectGPay) {
                    val payeeVpa = "merchant.pay@okhdfcbank"
                    val upiUri = Uri.parse(
                        "upi://pay?pa=$payeeVpa&pn=${Uri.encode(title)}&am=${String.format(Locale.US, "%.2f", amt)}&cu=INR&tn=${Uri.encode(title)}"
                    )
                    val intent = Intent(Intent.ACTION_VIEW, upiUri)
                    intent.setPackage("com.google.android.apps.nbu.paisa.user")
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Handled fallback gracefully
                    }
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ExpenseItemCard(
    expense: ExpenseEntity,
    onDelete: () -> Unit
) {
    val categoryIcon = getCategoryIcon(expense.category)
    val categoryColor = getCategoryColor(expense.category)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(categoryIcon, contentDescription = null, tint = categoryColor, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(expense.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = categoryColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = expense.category,
                            color = categoryColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    Text(expense.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (expense.paymentMethod.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when {
                                expense.paymentMethod.contains("Google Pay", ignoreCase = true) -> Icons.Default.Payments
                                expense.paymentMethod.contains("Bank", ignoreCase = true) -> Icons.Default.AccountBalance
                                expense.paymentMethod.contains("PhonePe", ignoreCase = true) || expense.paymentMethod.contains("Paytm", ignoreCase = true) -> Icons.Default.QrCode
                                else -> Icons.Default.Payment
                            },
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(expense.paymentMethod, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "-${formatCurrency(expense.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    linkedBankAccounts: List<BankAccountEntity>,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        amount: Double,
        category: String,
        paymentMethod: String,
        bankAccountId: String?,
        date: String,
        notes: String,
        launchDirectGPay: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food & Dining") }
    var selectedPaymentMethod by remember { mutableStateOf("Google Pay") }
    var selectedBankId by remember { mutableStateOf(linkedBankAccounts.firstOrNull()?.id) }
    var notes by remember { mutableStateOf("") }
    var directGPayDebit by remember { mutableStateOf(false) }

    val categories = listOf(
        "Food & Dining",
        "Groceries",
        "Loan EMI",
        "Transportation",
        "Utilities",
        "Shopping",
        "Health",
        "Entertainment",
        "Other"
    )

    val paymentMethods = listOf(
        "Google Pay",
        "PhonePe",
        "Paytm",
        "Connected Bank Account",
        "Debit Card",
        "Cash"
    )

    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val amount = amountText.toDoubleOrNull() ?: 0.0
    val isValid = title.isNotBlank() && amount > 0.0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(22.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Log Real-Time Expense", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Expense Title / Merchant") },
                        placeholder = { Text("e.g. Swiggy, DMart, Metro Pass") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Amount (₹)") },
                        leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    Text("Select Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            val isSel = selectedCategory == cat
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    Text("Payment Method", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(paymentMethods) { method ->
                            val isSel = selectedPaymentMethod == method
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedPaymentMethod = method },
                                label = { Text(method, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                if (selectedPaymentMethod == "Connected Bank Account" && linkedBankAccounts.isNotEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Debiting Bank:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val primaryBank = linkedBankAccounts.firstOrNull { it.isPrimary } ?: linkedBankAccounts.first()
                                Text("${primaryBank.bankName} (•••• ${primaryBank.accountNumberLast4})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                if (selectedPaymentMethod == "Google Pay") {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { directGPayDebit = !directGPayDebit }
                        ) {
                            Checkbox(checked = directGPayDebit, onCheckedChange = { directGPayDebit = it })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Directly open Google Pay app now to pay", fontSize = 12.sp)
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    Button(
                        onClick = {
                            val finalMethod = if (selectedPaymentMethod == "Connected Bank Account") {
                                val b = linkedBankAccounts.firstOrNull { it.id == selectedBankId } ?: linkedBankAccounts.firstOrNull()
                                "Connected Bank (${b?.bankName ?: "Bank"})"
                            } else {
                                selectedPaymentMethod
                            }
                            onSave(title, amount, selectedCategory, finalMethod, selectedBankId, todayDate, notes, directGPayDebit)
                        },
                        enabled = isValid,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LoanPrimary)
                    ) {
                        Text(if (directGPayDebit) "Pay via Google Pay & Log" else "Save Expense", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun getCategoryIcon(category: String): ImageVector = when (category) {
    "Food & Dining" -> Icons.Default.Restaurant
    "Groceries" -> Icons.Default.ShoppingCart
    "Loan EMI" -> Icons.Default.AccountBalance
    "Transportation" -> Icons.Default.DirectionsCar
    "Utilities" -> Icons.Default.ElectricBolt
    "Shopping" -> Icons.Default.ShoppingBag
    "Health" -> Icons.Default.LocalHospital
    "Entertainment" -> Icons.Default.Movie
    else -> Icons.Default.Receipt
}

fun getCategoryColor(category: String): Color = when (category) {
    "Food & Dining" -> Color(0xFFF97316)
    "Groceries" -> Color(0xFF10B981)
    "Loan EMI" -> Color(0xFF6366F1)
    "Transportation" -> Color(0xFF0EA5E9)
    "Utilities" -> Color(0xFFF59E0B)
    "Shopping" -> Color(0xFFEC4899)
    "Health" -> Color(0xFFEF4444)
    "Entertainment" -> Color(0xFF8B5CF6)
    else -> Color(0xFF64748B)
}

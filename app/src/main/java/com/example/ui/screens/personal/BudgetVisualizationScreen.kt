package com.example.ui.screens.personal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BudgetEntity
import com.example.data.model.ExpenseEntity
import com.example.ui.components.formatCurrency
import com.example.ui.theme.LoanPrimary
import com.example.ui.theme.OverdueRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun BudgetVisualizationScreen(
    budgets: List<BudgetEntity>,
    expenses: List<ExpenseEntity>,
    onSetBudget: (category: String, limit: Double, month: String) -> Unit,
    onNavigateToExpenses: () -> Unit,
    onAskAi: () -> Unit
) {
    var showSetBudgetDialog by remember { mutableStateOf(false) }
    var selectedBudgetToEdit by remember { mutableStateOf<BudgetEntity?>(null) }

    val currentMonth = "2026-09"

    // Group expenses by category
    val expensesByCategory = expenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    val totalSpent = expenses.sumOf { it.amount }

    // Find overall budget or sum of category budgets
    val overallBudget = budgets.find { it.category == "Overall" }?.monthlyLimit
        ?: budgets.filter { it.category != "Overall" }.sumOf { it.monthlyLimit }.takeIf { it > 0 } ?: 40000.0

    val overallRemaining = maxOf(0.0, overallBudget - totalSpent)
    val overallPercentage = if (overallBudget > 0) (totalSpent / overallBudget * 100.0).coerceAtMost(100.0) else 0.0
    val isOverBudget = totalSpent > overallBudget
    val isNearBudget = !isOverBudget && overallPercentage >= 80.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("budget_visualization_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Overall Budget Card
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
                                colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0284C7))
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
                                    text = "Monthly Budget Visualization",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = "September 2026 Target",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            IconButton(
                                onClick = {
                                    selectedBudgetToEdit = budgets.find { it.category == "Overall" }
                                    showSetBudgetDialog = true
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Budget", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text("Remaining to Spend", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(
                                    text = formatCurrency(overallRemaining),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isOverBudget) OverdueRed else Color.White
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Limit", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text(
                                    text = formatCurrency(overallBudget),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF67FDCD),
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Progress Bar
                        val animatedProgress by animateFloatAsState(
                            targetValue = (overallPercentage / 100f).toFloat(),
                            label = "overall_progress"
                        )
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = when {
                                isOverBudget -> OverdueRed
                                isNearBudget -> WarningAmber
                                else -> LoanPrimary
                            },
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${String.format("%.1f", overallPercentage)}% consumed",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Spent ${formatCurrency(totalSpent)}",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Budget Alerts
        if (isOverBudget) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = OverdueRed.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = OverdueRed)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Budget Exceeded", fontWeight = FontWeight.Bold, color = OverdueRed, fontSize = 13.sp)
                            Text("You have exceeded your monthly limit by ${formatCurrency(totalSpent - overallBudget)}. Consider curbing non-essential spending.", fontSize = 11.sp)
                        }
                    }
                }
            }
        } else if (isNearBudget) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = WarningAmber.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = WarningAmber)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("80% Budget Consumed", fontWeight = FontWeight.Bold, color = WarningAmber, fontSize = 13.sp)
                            Text("You have ${formatCurrency(overallRemaining)} left for the rest of the month.", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Spending Distribution Donut Chart
        if (expenses.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Spending Distribution Chart",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            // Donut Chart Canvas
                            Box(
                                modifier = Modifier.size(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(130.dp)) {
                                    val strokeWidth = 24.dp.toPx()
                                    val sizeOffset = strokeWidth / 2
                                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                                    var startAngle = -90f

                                    if (totalSpent <= 0) {
                                        drawCircle(
                                            color = Color.LightGray.copy(alpha = 0.4f),
                                            style = Stroke(width = strokeWidth)
                                        )
                                    } else {
                                        expensesByCategory.forEach { (category, amount) ->
                                            val sweepAngle = (amount / totalSpent * 360f).toFloat()
                                            val color = getCategoryColor(category)
                                            drawArc(
                                                color = color,
                                                startAngle = startAngle,
                                                sweepAngle = sweepAngle,
                                                useCenter = false,
                                                topLeft = Offset(sizeOffset, sizeOffset),
                                                size = arcSize,
                                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                            )
                                            startAngle += sweepAngle
                                        }
                                    }
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Total", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        formatCurrency(totalSpent),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Donut Legend
                            Column(
                                modifier = Modifier.padding(start = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                expensesByCategory.entries.take(5).forEach { (cat, amt) ->
                                    val pct = if (totalSpent > 0) (amt / totalSpent * 100).toInt() else 0
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(getCategoryColor(cat))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "$cat ($pct%)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Category Budgets Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category Budgets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = {
                    selectedBudgetToEdit = null
                    showSetBudgetDialog = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Budget Target", fontSize = 12.sp)
                }
            }
        }

        // Category Budget Items
        val categoryBudgetsList = budgets.filter { it.category != "Overall" }
        if (categoryBudgetsList.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No category budgets set yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(onClick = { showSetBudgetDialog = true }) {
                            Text("Set Target Budgets")
                        }
                    }
                }
            }
        } else {
            items(categoryBudgetsList, key = { it.id }) { budget ->
                val spent = expensesByCategory[budget.category] ?: 0.0
                CategoryBudgetCard(
                    budget = budget,
                    spent = spent,
                    onEdit = {
                        selectedBudgetToEdit = budget
                        showSetBudgetDialog = true
                    }
                )
            }
        }

        // AI Financial Advisory & Budget Insights
        item {
            Card(
                onClick = onAskAi,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(LoanPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LoanPrimary)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("AI Budget Forecast & Optimization", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            "You are spending 14% less than your monthly ceiling. Ask AI for customized savings advice in your language.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }

        // View Transactions Shortcut
        item {
            OutlinedButton(
                onClick = onNavigateToExpenses,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Real-Time Transaction Logs")
            }
        }
    }

    if (showSetBudgetDialog) {
        SetBudgetDialog(
            initialBudget = selectedBudgetToEdit,
            onDismiss = {
                showSetBudgetDialog = false
                selectedBudgetToEdit = null
            },
            onSave = { category, limit ->
                onSetBudget(category, limit, currentMonth)
                showSetBudgetDialog = false
                selectedBudgetToEdit = null
            }
        )
    }
}

@Composable
fun CategoryBudgetCard(
    budget: BudgetEntity,
    spent: Double,
    onEdit: () -> Unit
) {
    val categoryIcon = getCategoryIcon(budget.category)
    val categoryColor = getCategoryColor(budget.category)
    val limit = budget.monthlyLimit
    val pct = if (limit > 0) (spent / limit * 100.0).coerceAtMost(100.0) else 0.0
    val isOver = spent > limit
    val isNear = !isOver && pct >= budget.alertThresholdPercent

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            .background(categoryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(categoryIcon, contentDescription = null, tint = categoryColor, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(budget.category, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "${formatCurrency(spent)} of ${formatCurrency(limit)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            isOver -> OverdueRed.copy(alpha = 0.15f)
                            isNear -> WarningAmber.copy(alpha = 0.15f)
                            else -> SuccessGreen.copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            text = if (isOver) "Exceeded" else "${String.format("%.0f", pct)}%",
                            color = when {
                                isOver -> OverdueRed
                                isNear -> WarningAmber
                                else -> SuccessGreen
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val progressValue = (pct / 100.0).toFloat()
            LinearProgressIndicator(
                progress = { progressValue },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    isOver -> OverdueRed
                    isNear -> WarningAmber
                    else -> categoryColor
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isOver) "Over budget by ${formatCurrency(spent - limit)}" else "${formatCurrency(limit - spent)} remaining",
                    fontSize = 11.sp,
                    color = if (isOver) OverdueRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Ceiling ${formatCurrency(limit)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SetBudgetDialog(
    initialBudget: BudgetEntity?,
    onDismiss: () -> Unit,
    onSave: (category: String, limit: Double) -> Unit
) {
    val predefinedCategories = listOf(
        "Overall",
        "Food & Dining",
        "Groceries",
        "Loan EMI",
        "Transportation",
        "Utilities",
        "Shopping",
        "Health",
        "Entertainment"
    )

    var selectedCategory by remember { mutableStateOf(initialBudget?.category ?: "Food & Dining") }
    var limitText by remember { mutableStateOf(initialBudget?.monthlyLimit?.toInt()?.toString() ?: "5000") }

    val limit = limitText.toDoubleOrNull() ?: 0.0
    val isValid = limit > 0.0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialBudget != null) "Edit Budget Target" else "Set Category Budget",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }

                if (initialBudget == null) {
                    Text("Select Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        predefinedCategories.chunked(3).forEach { rowCats ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowCats.forEach { cat ->
                                    val isSel = selectedCategory == cat
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { selectedCategory = cat },
                                        label = { Text(cat, fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(getCategoryIcon(selectedCategory), contentDescription = null, tint = getCategoryColor(selectedCategory))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Category: $selectedCategory", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Monthly Budget Limit (₹)") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = { onSave(selectedCategory, limit) },
                    enabled = isValid,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LoanPrimary)
                ) {
                    Text("Save Budget Target", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

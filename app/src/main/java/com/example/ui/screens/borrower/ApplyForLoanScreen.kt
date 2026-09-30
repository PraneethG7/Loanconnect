package com.example.ui.screens.borrower

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LoanEntity
import com.example.data.model.UserEntity
import com.example.ui.components.formatCurrency
import com.example.ui.theme.InfoCyan
import com.example.ui.theme.LoanPrimary
import com.example.ui.theme.LoanSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

private data class LoanPurposeCategory(
    val title: String,
    val icon: ImageVector,
    val description: String
)

private val standardPurposes = listOf(
    LoanPurposeCategory("Business Expansion", Icons.Default.BusinessCenter, "Working capital, inventory & store growth"),
    LoanPurposeCategory("Medical Emergency", Icons.Default.LocalHospital, "Hospital bills, diagnostics & urgent care"),
    LoanPurposeCategory("Higher Education", Icons.Default.School, "Tuition fees, textbooks & semester courses"),
    LoanPurposeCategory("Home Renovation", Icons.Default.Home, "Repairs, solar installation & construction"),
    LoanPurposeCategory("Agriculture & Farm", Icons.Default.Agriculture, "Equipment, seeds, fertilizers & harvesting"),
    LoanPurposeCategory("Vehicle Purchase", Icons.Default.DirectionsCar, "Two-wheeler, commercial auto or repair"),
    LoanPurposeCategory("Debt Consolidation", Icons.Default.AccountBalanceWallet, "Clear high-interest informal loans"),
    LoanPurposeCategory("Personal & Family", Icons.Default.FamilyRestroom, "Family event, relocation or electronics")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyForLoanScreen(
    borrower: UserEntity,
    financiers: List<UserEntity>,
    onBack: () -> Unit,
    onSubmitApplication: (amount: Double, duration: Int, purpose: String, financier: UserEntity?, rate: Double) -> Unit
) {
    // Form Inputs
    var amountInput by remember { mutableStateOf("50000") }
    var durationMonths by remember { mutableIntStateOf(12) }
    var purposeText by remember { mutableStateOf("Business Expansion") }
    var selectedFinancier by remember { mutableStateOf<UserEntity?>(financiers.firstOrNull()) }
    var paymentFrequency by remember { mutableStateOf("Monthly") }
    var termsAccepted by remember { mutableStateOf(true) }

    // Validation & Dialog State
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submittedLoan by remember { mutableStateOf<LoanEntity?>(null) }

    // Financial Calculation helpers
    val parsedAmount = amountInput.toDoubleOrNull() ?: 0.0
    val interestRate = selectedFinancier?.standardInterestRate ?: 10.0
    val totalInterest = parsedAmount * (interestRate / 100.0) * (durationMonths / 12.0)
    val processingFee = (parsedAmount * 0.01).coerceAtLeast(300.0).coerceAtMost(2500.0)
    val platformFee = 150.0
    val totalPayable = parsedAmount + totalInterest + processingFee + platformFee
    val monthlyEmi = if (durationMonths > 0) totalPayable / durationMonths else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Apply for Loan",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Instant loan origination & direct disbursal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("apply_loan_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .testTag("apply_for_loan_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Intro Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
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
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF0284C7).copy(alpha = 0.35f)
                                ) {
                                    Text(
                                        text = "FAST DISBURSAL • ZERO PAPERWORK",
                                        color = Color(0xFF7DD3FC),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Transparent Digital Lending",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Input your requested amount, tenure, and purpose. Funds are disbursed upon submission.",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CurrencyRupee,
                                    contentDescription = null,
                                    tint = Color(0xFF67FDCD),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 1: REQUESTED AMOUNT
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = LoanPrimary)
                            Text(
                                text = "1. Requested Loan Amount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Choose between ₹5,000 and ₹5,00,000",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = {
                                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                    amountInput = it
                                }
                            },
                            label = { Text("Amount (INR)") },
                            prefix = { Text("₹ ", fontWeight = FontWeight.Bold, color = LoanPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apply_loan_amount_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Amount Chips
                        Text(
                            text = "Quick Select:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val quickAmounts = listOf(10000, 25000, 50000, 100000, 200000, 300000, 500000)
                            items(quickAmounts) { amt ->
                                val isSelected = parsedAmount.toInt() == amt
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { amountInput = amt.toString() },
                                    label = { Text("₹${amt / 1000}k", fontWeight = FontWeight.Medium) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Amount Slider
                        Slider(
                            value = (parsedAmount.toFloat().coerceIn(5000f, 500000f)),
                            onValueChange = { newValue ->
                                val rounded = (Math.round(newValue / 5000f) * 5000).toInt()
                                amountInput = rounded.toString()
                            },
                            valueRange = 5000f..500000f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Min: ₹5,000", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Max: ₹5,00,000", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // SECTION 2: DURATION / TENURE
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = InfoCyan)
                            Text(
                                text = "2. Loan Duration",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Select repayment period ($durationMonths months)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Duration Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val durations = listOf(3, 6, 9, 12, 18, 24, 36)
                            items(durations) { months ->
                                val isSelected = durationMonths == months
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { durationMonths = months },
                                    label = { Text("$months Months", fontWeight = FontWeight.Medium) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Duration Slider
                        Slider(
                            value = durationMonths.toFloat(),
                            onValueChange = { durationMonths = it.toInt() },
                            valueRange = 3f..36f,
                            steps = 32,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apply_loan_duration_slider")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("3 Months (Short Term)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("36 Months (Long Term)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // SECTION 3: LOAN PURPOSE
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Flag, contentDescription = null, tint = WarningAmber)
                            Text(
                                text = "3. Loan Purpose",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Specify what this loan will be used for",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = purposeText,
                            onValueChange = { purposeText = it },
                            label = { Text("Purpose / Intended Use") },
                            placeholder = { Text("e.g. Working Capital, Equipment, Education") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apply_loan_purpose_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Common Categories:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Category Chips with Icons
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(standardPurposes) { item ->
                                val isSelected = purposeText.equals(item.title, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { purposeText = item.title },
                                    label = { Text(item.title) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 4: FINANCIER & PARTNER SELECTION
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = LoanSecondary)
                            Text(
                                text = "4. Lending Partner",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Select a disclosed financier for instant underwriting",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (financiers.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                financiers.forEach { f ->
                                    val isSelected = selectedFinancier?.id == f.id
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { selectedFinancier = f }
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) LoanPrimary else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        color = if (isSelected) LoanPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = { selectedFinancier = f }
                                                )
                                                Column {
                                                    Text(
                                                        text = f.businessName.ifEmpty { f.name },
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                    Text(
                                                        text = "${f.serviceArea} • ${f.standardInterestRate}% ${f.standardInterestType.name.lowercase()}",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = SuccessGreen.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "VERIFIED",
                                                    color = SuccessGreen,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Applying directly through LoanConnect Partner Lending (Standard 10% rate).",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // SECTION 5: LIVE FINANCIAL SUMMARY / EMI CALCULATOR
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Estimated Repayment",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$interestRate% p.a.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Highlight EMI
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Monthly Installment (EMI)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = formatCurrency(monthlyEmi),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = LoanPrimary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Total Payable", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = formatCurrency(totalPayable),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Details grid
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SummaryLine(label = "Principal Disbursed", value = formatCurrency(parsedAmount))
                            SummaryLine(label = "Total Interest ($durationMonths mo)", value = formatCurrency(totalInterest))
                            SummaryLine(label = "Processing Fee (1%)", value = formatCurrency(processingFee))
                            SummaryLine(label = "Platform Convenience Fee", value = formatCurrency(platformFee))
                        }
                    }
                }
            }

            // SECTION 6: TERMS & SUBMIT
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { termsAccepted = !termsAccepted }
                    ) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = { termsAccepted = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "I accept the loan agreement terms, interest schedule, and consent to direct disbursement to my linked account.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Text(
                                    text = errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            errorMessage = null
                            if (parsedAmount <= 0) {
                                errorMessage = "Please enter a valid loan amount greater than ₹0."
                                return@Button
                            }
                            if (durationMonths <= 0) {
                                errorMessage = "Please select a valid duration in months."
                                return@Button
                            }
                            if (purposeText.isBlank()) {
                                errorMessage = "Please specify a purpose for your loan application."
                                return@Button
                            }
                            if (!termsAccepted) {
                                errorMessage = "Please accept the loan terms and agreement to proceed."
                                return@Button
                            }

                            isSubmitting = true
                            onSubmitApplication(
                                parsedAmount,
                                durationMonths,
                                purposeText.trim(),
                                selectedFinancier,
                                interestRate
                            )
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("apply_loan_submit_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LoanPrimary)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing Application...")
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Submit Loan Application",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

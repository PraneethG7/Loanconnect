package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InterestType
import com.example.data.model.UserRole
import com.example.ui.theme.LoanPrimary
import com.example.ui.theme.LoanSecondary
import com.example.ui.theme.OverdueRed

@Composable
fun AuthScreen(
    authError: String?,
    onLogin: (emailOrPhone: String, pass: String) -> Unit,
    onRegister: (
        name: String,
        email: String,
        phone: String,
        pass: String,
        role: UserRole,
        businessName: String,
        address: String,
        serviceArea: String,
        minAmount: Double,
        maxAmount: Double,
        rate: Double,
        interestType: InterestType
    ) -> Unit,
    onQuickDemoLogin: (userId: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Sign In, 1 = Create Account

    // Sign In form fields
    var loginIdentifier by remember { mutableStateOf("priya.sharma@gmail.com") }
    var loginPassword by remember { mutableStateOf("password123") }
    var showPassword by remember { mutableStateOf(false) }

    // Register form fields
    var regRole by remember { mutableStateOf(UserRole.BORROWER) }
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regAddress by remember { mutableStateOf("") }
    var regBusinessName by remember { mutableStateOf("") }
    var regServiceArea by remember { mutableStateOf("Bangalore & South Region") }
    var regMinAmountText by remember { mutableStateOf("10000") }
    var regMaxAmountText by remember { mutableStateOf("500000") }
    var regRateText by remember { mutableStateOf("10.0") }
    var regInterestType by remember { mutableStateOf(InterestType.MONTHLY) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("auth_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Hero Branding
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = LoanPrimary,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("LC", fontWeight = FontWeight.Black, color = Color.White, fontSize = 22.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "LoanConnect",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Secure Financial Lending & Payment Platform",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Quick Demo Login Chips
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "1-Tap Quick Demo Sign In",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onQuickDemoLogin("user_b1") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("Borrower", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { onQuickDemoLogin("user_f1") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("Financier", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { onQuickDemoLogin("user_admin") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Auth Tabs: Sign In / Create Account
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.clip(RoundedCornerShape(14.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Sign In", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Create Account", fontWeight = FontWeight.Bold) }
                )
            }
        }

        // Error message banner
        authError?.let { err ->
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = OverdueRed.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = OverdueRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(err, color = OverdueRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        if (selectedTab == 0) {
            // SIGN IN TAB
            item {
                OutlinedTextField(
                    value = loginIdentifier,
                    onValueChange = { loginIdentifier = it },
                    label = { Text("Email or Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = loginPassword,
                    onValueChange = { loginPassword = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            item {
                Button(
                    onClick = { onLogin(loginIdentifier, loginPassword) },
                    enabled = loginIdentifier.isNotBlank() && loginPassword.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Login, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Secure Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        } else {
            // CREATE ACCOUNT TAB
            item {
                Text(
                    text = "Select Account Type",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = regRole == UserRole.BORROWER,
                        onClick = { regRole = UserRole.BORROWER },
                        label = { Text("Borrower / Common User") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = regRole == UserRole.FINANCIER,
                        onClick = { regRole = UserRole.FINANCIER },
                        label = { Text("Financier / Lender") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = regName,
                    onValueChange = { regName = it },
                    label = { Text("Full Legal Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = regEmail,
                    onValueChange = { regEmail = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = regPhone,
                    onValueChange = { regPhone = it },
                    label = { Text("Mobile Phone (+91)") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = regPassword,
                    onValueChange = { regPassword = it },
                    label = { Text("Create Secure Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = regAddress,
                    onValueChange = { regAddress = it },
                    label = { Text("Residential / Business Address") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Financier specific fields
            if (regRole == UserRole.FINANCIER) {
                item {
                    Text(
                        text = "Financier Business & Terms Details",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = LoanPrimary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = regBusinessName,
                        onValueChange = { regBusinessName = it },
                        label = { Text("Registered Business / Agency Name") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = regServiceArea,
                        onValueChange = { regServiceArea = it },
                        label = { Text("Service Area / Cities") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
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
                            value = regMinAmountText,
                            onValueChange = { regMinAmountText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Min Loan (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = regMaxAmountText,
                            onValueChange = { regMaxAmountText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Max Loan (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = regRateText,
                        onValueChange = { regRateText = it },
                        label = { Text("Standard Interest Rate (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        val minAmt = regMinAmountText.toDoubleOrNull() ?: 5000.0
                        val maxAmt = regMaxAmountText.toDoubleOrNull() ?: 500000.0
                        val rate = regRateText.toDoubleOrNull() ?: 10.0
                        onRegister(
                            regName,
                            regEmail,
                            regPhone,
                            regPassword,
                            regRole,
                            regBusinessName,
                            regAddress,
                            regServiceArea,
                            minAmt,
                            maxAmt,
                            rate,
                            regInterestType
                        )
                    },
                    enabled = regName.isNotBlank() && regEmail.isNotBlank() && regPhone.isNotBlank() && regPassword.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Register & Access Dashboard", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

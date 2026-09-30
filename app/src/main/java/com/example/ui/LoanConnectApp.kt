package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.components.MakePaymentDialog
import com.example.ui.components.PaymentReceiptDialog
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.ai.LoanConnectAiScreen
import com.example.ui.screens.borrower.BorrowerDashboardScreen
import com.example.ui.screens.borrower.BorrowerLoansScreen
import com.example.ui.screens.borrower.FindFinancierScreen
import com.example.ui.screens.common.HelpAndSupportScreen
import com.example.ui.screens.common.LoanCalculatorScreen
import com.example.ui.screens.common.PaymentCalendarScreen
import com.example.ui.screens.financier.FinancierDashboardScreen
import com.example.ui.screens.financier.FinancierReportsScreen
import com.example.ui.screens.financier.LoanRequestsScreen
import com.example.ui.screens.financier.SmartCollectionScreen
import com.example.ui.theme.LoanPrimary
import com.example.ui.theme.OverdueRed
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.LoanConnectViewModel

enum class NavigationTab {
    DASHBOARD,
    FIND_FINANCIER,
    MY_LOANS,
    COLLECTIONS,
    REQUESTS,
    REPORTS,
    AI_ASSISTANT,
    CALCULATOR,
    CALENDAR,
    SUPPORT,
    ADMIN_MAIN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanConnectApp(viewModel: LoanConnectViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val userLoans by viewModel.userLoans.collectAsStateWithLifecycle()
    val userPayments by viewModel.userPayments.collectAsStateWithLifecycle()
    val userOffers by viewModel.userOffers.collectAsStateWithLifecycle()
    val userRequests by viewModel.userRequests.collectAsStateWithLifecycle()
    val userSchedules by viewModel.userSchedules.collectAsStateWithLifecycle()
    val userCommissions by viewModel.userCommissions.collectAsStateWithLifecycle()
    val userNotifications by viewModel.userNotifications.collectAsStateWithLifecycle()
    val disclosedFinanciers by viewModel.disclosedFinanciers.collectAsStateWithLifecycle()

    val aiMessages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()

    val activeReceipt by viewModel.activeReceipt.collectAsStateWithLifecycle()

    // Navigation State
    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }

    // Dialogs
    var paymentLoanTarget by remember { mutableStateOf<LoanEntity?>(null) }
    var isPaymentOffline by remember { mutableStateOf(false) }
    var showUserSwitcher by remember { mutableStateOf(false) }
    var showNotificationsModal by remember { mutableStateOf(false) }

    val unreadNotifs = userNotifications.count { !it.isRead }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LoanPrimary,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("LC", fontWeight = FontWeight.Black, color = Color.White, fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("LoanConnect", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            currentUser?.let { user ->
                                Text(
                                    text = "${user.name} • ${user.role.name}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Profile Switcher (Financier / Borrower / Admin)
                    IconButton(
                        onClick = { showUserSwitcher = true },
                        modifier = Modifier.testTag("switch_user_button")
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Switch Account")
                    }

                    // Notifications
                    IconButton(onClick = { showNotificationsModal = true }) {
                        BadgedBox(badge = {
                            if (unreadNotifs > 0) {
                                Badge { Text(unreadNotifs.toString()) }
                            }
                        }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            currentUser?.let { user ->
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    when (user.role) {
                        UserRole.BORROWER -> {
                            NavigationBarItem(
                                selected = currentTab == NavigationTab.DASHBOARD,
                                onClick = { currentTab = NavigationTab.DASHBOARD },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Home") }
                            )
                            NavigationBarItem(
                                selected = currentTab == NavigationTab.FIND_FINANCIER,
                                onClick = { currentTab = NavigationTab.FIND_FINANCIER },
                                icon = { Icon(Icons.Default.Search, contentDescription = "Market") },
                                label = { Text("Market") }
                            )
                            NavigationBarItem(
                                selected = currentTab == NavigationTab.MY_LOANS,
                                onClick = { currentTab = NavigationTab.MY_LOANS },
                                icon = { Icon(Icons.Default.FormatListBulleted, contentDescription = "Loans") },
                                label = { Text("Loans") }
                            )
                            NavigationBarItem(
                                selected = currentTab == NavigationTab.AI_ASSISTANT,
                                onClick = { currentTab = NavigationTab.AI_ASSISTANT },
                                icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI") },
                                label = { Text("Ask AI") }
                            )
                            NavigationBarItem(
                                selected = currentTab == NavigationTab.SUPPORT || currentTab == NavigationTab.CALCULATOR || currentTab == NavigationTab.CALENDAR,
                                onClick = { currentTab = NavigationTab.SUPPORT },
                                icon = { Icon(Icons.Default.HelpCenter, contentDescription = "More") },
                                label = { Text("More") }
                            )
                        }
                        UserRole.FINANCIER -> {
                            NavigationBarItem(
                                selected = currentTab == NavigationTab.DASHBOARD,
                                onClick = { currentTab = NavigationTab.DASHBOARD },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("Dashboard") }
                            )
                            NavigationBarItem(
                                selected = currentTab == NavigationTab.COLLECTIONS,
                                onClick = { currentTab = NavigationTab.COLLECTIONS },
                                icon = { Icon(Icons.Default.Payments, contentDescription = "Collect") },
                                label = { Text("Collect") }
                            )
                            NavigationBarItem(
                                selected = currentTab == NavigationTab.REQUESTS,
                                onClick = { currentTab = NavigationTab.REQUESTS },
                                icon = { Icon(Icons.Default.Inbox, contentDescription = "Requests") },
                                label = { Text("Requests") }
                            )
                            NavigationBarItem(
                                selected = currentTab == NavigationTab.REPORTS,
                                onClick = { currentTab = NavigationTab.REPORTS },
                                icon = { Icon(Icons.Default.BarChart, contentDescription = "Reports") },
                                label = { Text("Reports") }
                            )
                            NavigationBarItem(
                                selected = currentTab == NavigationTab.AI_ASSISTANT,
                                onClick = { currentTab = NavigationTab.AI_ASSISTANT },
                                icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI") },
                                label = { Text("AI Query") }
                            )
                        }
                        UserRole.ADMIN -> {
                            NavigationBarItem(
                                selected = currentTab == NavigationTab.ADMIN_MAIN || currentTab == NavigationTab.DASHBOARD,
                                onClick = { currentTab = NavigationTab.ADMIN_MAIN },
                                icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                                label = { Text("Admin HQ") }
                            )
                            NavigationBarItem(
                                selected = currentTab == NavigationTab.AI_ASSISTANT,
                                onClick = { currentTab = NavigationTab.AI_ASSISTANT },
                                icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI") },
                                label = { Text("Platform AI") }
                            )
                            NavigationBarItem(
                                selected = currentTab == NavigationTab.SUPPORT,
                                onClick = { currentTab = NavigationTab.SUPPORT },
                                icon = { Icon(Icons.Default.SupportAgent, contentDescription = "Support") },
                                label = { Text("Support") }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val user = currentUser
            if (user != null) {
                when {
                    user.role == UserRole.BORROWER -> {
                        when (currentTab) {
                            NavigationTab.DASHBOARD -> BorrowerDashboardScreen(
                                user = user,
                                loans = userLoans,
                                payments = userPayments,
                                offers = userOffers,
                                onNavigateToFindFinancier = { currentTab = NavigationTab.FIND_FINANCIER },
                                onNavigateToMyLoans = { currentTab = NavigationTab.MY_LOANS },
                                onNavigateToAi = { currentTab = NavigationTab.AI_ASSISTANT },
                                onNavigateToSupport = { currentTab = NavigationTab.SUPPORT },
                                onNavigateToCalendar = { currentTab = NavigationTab.CALENDAR },
                                onNavigateToCalculator = { currentTab = NavigationTab.CALCULATOR },
                                onPayLoan = { loan ->
                                    isPaymentOffline = false
                                    paymentLoanTarget = loan
                                },
                                onEarlySettle = { loan -> viewModel.settleLoanEarly(loan) },
                                onToggleAutoPay = { loan, enable -> viewModel.toggleAutoPay(loan, enable) },
                                onAcceptOffer = { offer -> viewModel.acceptOffer(offer) },
                                onRejectOffer = { offer -> viewModel.rejectOffer(offer) },
                                onViewReceipt = { payment -> viewModel.showReceipt(payment) }
                            )
                            NavigationTab.FIND_FINANCIER -> FindFinancierScreen(
                                financiers = disclosedFinanciers,
                                onRequestLoan = { f, amt, purpose, dur, msg ->
                                    viewModel.submitLoanRequest(f, amt, purpose, dur, msg)
                                }
                            )
                            NavigationTab.MY_LOANS -> BorrowerLoansScreen(
                                loans = userLoans,
                                onPayLoan = { loan ->
                                    isPaymentOffline = false
                                    paymentLoanTarget = loan
                                },
                                onEarlySettle = { loan -> viewModel.settleLoanEarly(loan) },
                                onToggleAutoPay = { loan, enable -> viewModel.toggleAutoPay(loan, enable) }
                            )
                            NavigationTab.AI_ASSISTANT -> LoanConnectAiScreen(
                                user = user,
                                messages = aiMessages,
                                selectedLanguage = selectedLanguage,
                                isLoading = isAiLoading,
                                onSendMessage = { text -> viewModel.sendAiMessage(text) },
                                onSelectLanguage = { lang -> viewModel.setLanguage(lang) }
                            )
                            NavigationTab.CALCULATOR -> LoanCalculatorScreen()
                            NavigationTab.CALENDAR -> PaymentCalendarScreen(schedules = userSchedules)
                            NavigationTab.SUPPORT -> HelpAndSupportScreen(
                                tickets = emptyList(),
                                onSubmitTicket = { cat, desc, loanId -> viewModel.submitSupportTicket(cat, desc, loanId) },
                                onSubmitReport = { rName, cat, desc -> viewModel.submitReport(rName, rName, cat, desc) }
                            )
                            else -> BorrowerDashboardScreen(
                                user = user,
                                loans = userLoans,
                                payments = userPayments,
                                offers = userOffers,
                                onNavigateToFindFinancier = { currentTab = NavigationTab.FIND_FINANCIER },
                                onNavigateToMyLoans = { currentTab = NavigationTab.MY_LOANS },
                                onNavigateToAi = { currentTab = NavigationTab.AI_ASSISTANT },
                                onNavigateToSupport = { currentTab = NavigationTab.SUPPORT },
                                onNavigateToCalendar = { currentTab = NavigationTab.CALENDAR },
                                onNavigateToCalculator = { currentTab = NavigationTab.CALCULATOR },
                                onPayLoan = { loan ->
                                    isPaymentOffline = false
                                    paymentLoanTarget = loan
                                },
                                onEarlySettle = { loan -> viewModel.settleLoanEarly(loan) },
                                onToggleAutoPay = { loan, enable -> viewModel.toggleAutoPay(loan, enable) },
                                onAcceptOffer = { offer -> viewModel.acceptOffer(offer) },
                                onRejectOffer = { offer -> viewModel.rejectOffer(offer) },
                                onViewReceipt = { payment -> viewModel.showReceipt(payment) }
                            )
                        }
                    }
                    user.role == UserRole.FINANCIER -> {
                        when (currentTab) {
                            NavigationTab.DASHBOARD -> FinancierDashboardScreen(
                                financier = user,
                                loans = userLoans,
                                payments = userPayments,
                                requests = userRequests,
                                commissions = userCommissions,
                                onNavigateToRequests = { currentTab = NavigationTab.REQUESTS },
                                onNavigateToSmartCollections = { currentTab = NavigationTab.COLLECTIONS },
                                onNavigateToReports = { currentTab = NavigationTab.REPORTS },
                                onNavigateToAi = { currentTab = NavigationTab.AI_ASSISTANT },
                                onRecordOfflinePayment = { loan ->
                                    isPaymentOffline = true
                                    paymentLoanTarget = loan
                                }
                            )
                            NavigationTab.COLLECTIONS -> SmartCollectionScreen(
                                loans = userLoans,
                                payments = userPayments,
                                onRecordOfflinePayment = { loan ->
                                    isPaymentOffline = true
                                    paymentLoanTarget = loan
                                }
                            )
                            NavigationTab.REQUESTS -> LoanRequestsScreen(
                                requests = userRequests,
                                offers = userOffers,
                                onCreateOffer = { req, amt, rate, type, dur, emi, procFee, platFee, total, terms ->
                                    viewModel.createLoanOffer(req, amt, rate, type, dur, emi, procFee, platFee, total, terms)
                                }
                            )
                            NavigationTab.REPORTS -> FinancierReportsScreen(
                                loans = userLoans,
                                payments = userPayments,
                                commissions = userCommissions
                            )
                            NavigationTab.AI_ASSISTANT -> LoanConnectAiScreen(
                                user = user,
                                messages = aiMessages,
                                selectedLanguage = selectedLanguage,
                                isLoading = isAiLoading,
                                onSendMessage = { text -> viewModel.sendAiMessage(text) },
                                onSelectLanguage = { lang -> viewModel.setLanguage(lang) }
                            )
                            else -> FinancierDashboardScreen(
                                financier = user,
                                loans = userLoans,
                                payments = userPayments,
                                requests = userRequests,
                                commissions = userCommissions,
                                onNavigateToRequests = { currentTab = NavigationTab.REQUESTS },
                                onNavigateToSmartCollections = { currentTab = NavigationTab.COLLECTIONS },
                                onNavigateToReports = { currentTab = NavigationTab.REPORTS },
                                onNavigateToAi = { currentTab = NavigationTab.AI_ASSISTANT },
                                onRecordOfflinePayment = { loan ->
                                    isPaymentOffline = true
                                    paymentLoanTarget = loan
                                }
                            )
                        }
                    }
                    user.role == UserRole.ADMIN -> {
                        when (currentTab) {
                            NavigationTab.AI_ASSISTANT -> LoanConnectAiScreen(
                                user = user,
                                messages = aiMessages,
                                selectedLanguage = selectedLanguage,
                                isLoading = isAiLoading,
                                onSendMessage = { text -> viewModel.sendAiMessage(text) },
                                onSelectLanguage = { lang -> viewModel.setLanguage(lang) }
                            )
                            NavigationTab.SUPPORT -> HelpAndSupportScreen(
                                tickets = emptyList(),
                                onSubmitTicket = { cat, desc, loanId -> viewModel.submitSupportTicket(cat, desc, loanId) },
                                onSubmitReport = { rName, cat, desc -> viewModel.submitReport(rName, rName, cat, desc) }
                            )
                            else -> AdminDashboardScreen(
                                users = allUsers,
                                loans = userLoans,
                                payments = userPayments,
                                commissions = userCommissions,
                                reports = emptyList(),
                                tickets = emptyList(),
                                auditLogs = emptyList(),
                                onVerifyFinancier = { fId, status -> viewModel.updateFinancierVerification(fId, status) }
                            )
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    // Payment Dialog
    paymentLoanTarget?.let { loan ->
        MakePaymentDialog(
            loan = loan,
            isOfflineRecord = isPaymentOffline,
            onDismiss = { paymentLoanTarget = null },
            onConfirmPayment = { amount, method, notes ->
                viewModel.makePayment(loan, amount, method, notes, isOffline = isPaymentOffline)
                paymentLoanTarget = null
            }
        )
    }

    // Payment Receipt Modal
    activeReceipt?.let { receipt ->
        PaymentReceiptDialog(
            payment = receipt,
            onDismiss = { viewModel.clearActiveReceipt() }
        )
    }

    // User Role Switcher Dialog
    if (showUserSwitcher) {
        Dialog(onDismissRequest = { showUserSwitcher = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Switch Active Account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showUserSwitcher = false }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                    Text(
                        "Test separate data isolation across Financiers, Borrowers, and Admin:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    allUsers.forEach { user ->
                        val isSelected = currentUser?.id == user.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.switchUser(user.id)
                                    currentTab = NavigationTab.DASHBOARD
                                    showUserSwitcher = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (user.role) {
                                                UserRole.FINANCIER -> LoanPrimary.copy(alpha = 0.2f)
                                                UserRole.BORROWER -> Color(0xFF0EA5E9).copy(alpha = 0.2f)
                                                UserRole.ADMIN -> WarningAmber.copy(alpha = 0.2f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (user.role) {
                                            UserRole.FINANCIER -> Icons.Default.AccountBalance
                                            UserRole.BORROWER -> Icons.Default.Person
                                            UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(user.name, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${user.role.name} ${if (user.businessName.isNotEmpty()) "• ${user.businessName}" else ""}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Notifications Dialog
    if (showNotificationsModal) {
        Dialog(onDismissRequest = {
            showNotificationsModal = false
            viewModel.markNotificationsRead()
        }) {
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
                        Text("Notifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = {
                            showNotificationsModal = false
                            viewModel.markNotificationsRead()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (userNotifications.isEmpty()) {
                        Text("No new notifications.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        userNotifications.forEach { notif ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(notif.message, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

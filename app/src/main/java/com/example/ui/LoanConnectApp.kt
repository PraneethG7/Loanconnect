package com.example.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.data.model.*
import com.example.ui.components.MakePaymentDialog
import com.example.ui.components.PaymentReceiptDialog
import com.example.ui.components.formatCurrency
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.ai.LoanConnectAiScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.bank.ConnectBankDialog
import com.example.ui.screens.borrower.ApplyForLoanScreen
import com.example.ui.screens.borrower.BorrowerDashboardScreen
import com.example.ui.screens.borrower.BorrowerLoansScreen
import com.example.ui.screens.borrower.FindFinancierScreen
import com.example.ui.screens.personal.BudgetVisualizationScreen
import com.example.ui.screens.personal.ExpenseTrackerScreen
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
    EXPENSES,
    BUDGET,
    FIND_FINANCIER,
    APPLY_FOR_LOAN,
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

    val userExpenses by viewModel.userExpenses.collectAsStateWithLifecycle()
    val userBudgets by viewModel.userBudgets.collectAsStateWithLifecycle()

    val allReports by viewModel.allReports.collectAsStateWithLifecycle()
    val allAuditLogs by viewModel.allAuditLogs.collectAsStateWithLifecycle()
    val adminTickets by viewModel.adminTickets.collectAsStateWithLifecycle()

    val aiMessages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()

    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val linkedBankAccounts by viewModel.linkedBankAccounts.collectAsStateWithLifecycle()

    val activeReceipt by viewModel.activeReceipt.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Navigation State
    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }

    // Dialogs
    var paymentLoanTarget by remember { mutableStateOf<LoanEntity?>(null) }
    var isPaymentOffline by remember { mutableStateOf(false) }
    var showUserSwitcher by remember { mutableStateOf(false) }
    var showNotificationsModal by remember { mutableStateOf(false) }
    var showConnectBankModal by remember { mutableStateOf(false) }
    var newlyAppliedLoan by remember { mutableStateOf<LoanEntity?>(null) }

    val unreadNotifs = userNotifications.count { !it.isRead }

    BackHandler(enabled = isLoggedIn && currentTab != NavigationTab.DASHBOARD) {
        currentTab = NavigationTab.DASHBOARD
    }

    if (!isLoggedIn) {
        AuthScreen(
            authError = authError,
            onLogin = { emailOrPhone, pass ->
                viewModel.login(emailOrPhone, pass) {}
            },
            onRegister = { name, email, phone, pass, role, bizName, addr, area, minAmt, maxAmt, rate, iType ->
                viewModel.register(name, email, phone, pass, role, bizName, addr, area, minAmt, maxAmt, rate, iType) {}
            },
            onGoogleSignIn = {
                viewModel.signInWithGoogle(context, UserRole.BORROWER) { success, err ->
                    if (!success && err != null) {
                        scope.launch { snackbarHostState.showSnackbar(err) }
                    }
                }
            }
        )
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        // Connect Bank Account
                        IconButton(onClick = { showConnectBankModal = true }) {
                            Icon(Icons.Default.AccountBalance, contentDescription = "Connect Bank")
                        }

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
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Home") },
                                label = { Text("Home") }
                            )
                            NavigationBarItem(
                                selected = currentTab == NavigationTab.EXPENSES,
                                onClick = { currentTab = NavigationTab.EXPENSES },
                                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Expenses") },
                                label = { Text("Expenses") }
                            )
                            NavigationBarItem(
                                selected = currentTab == NavigationTab.BUDGET,
                                onClick = { currentTab = NavigationTab.BUDGET },
                                icon = { Icon(Icons.Default.PieChart, contentDescription = "Budget") },
                                label = { Text("Budget") }
                            )
                            NavigationBarItem(
                                selected = currentTab == NavigationTab.MY_LOANS || currentTab == NavigationTab.FIND_FINANCIER,
                                onClick = { currentTab = NavigationTab.MY_LOANS },
                                icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Loans") },
                                label = { Text("Loans & Pay") }
                            )
                            NavigationBarItem(
                                selected = currentTab == NavigationTab.AI_ASSISTANT,
                                onClick = { currentTab = NavigationTab.AI_ASSISTANT },
                                icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI") },
                                label = { Text("Ask AI") }
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
                                expenses = userExpenses,
                                budgets = userBudgets,
                                onNavigateToFindFinancier = { currentTab = NavigationTab.FIND_FINANCIER },
                                onNavigateToMyLoans = { currentTab = NavigationTab.MY_LOANS },
                                onNavigateToApplyForLoan = { currentTab = NavigationTab.APPLY_FOR_LOAN },
                                onNavigateToAi = { currentTab = NavigationTab.AI_ASSISTANT },
                                onNavigateToSupport = { currentTab = NavigationTab.SUPPORT },
                                onNavigateToCalendar = { currentTab = NavigationTab.CALENDAR },
                                onNavigateToCalculator = { currentTab = NavigationTab.CALCULATOR },
                                onNavigateToExpenses = { currentTab = NavigationTab.EXPENSES },
                                onNavigateToBudget = { currentTab = NavigationTab.BUDGET },
                                onPayLoan = { loan ->
                                    isPaymentOffline = false
                                    paymentLoanTarget = loan
                                },
                                onEarlySettle = { loan -> viewModel.settleLoanEarly(loan) },
                                onToggleAutoPay = { loan, enable -> viewModel.toggleAutoPay(loan, enable) },
                                onAcceptOffer = { offer -> viewModel.acceptOffer(offer) },
                                onRejectOffer = { offer -> viewModel.rejectOffer(offer) },
                                onViewReceipt = { payment -> viewModel.showReceipt(payment) },
                                onConnectBank = { showConnectBankModal = true }
                            )
                            NavigationTab.APPLY_FOR_LOAN -> ApplyForLoanScreen(
                                borrower = user,
                                financiers = disclosedFinanciers,
                                onBack = { currentTab = NavigationTab.DASHBOARD },
                                onSubmitApplication = { amount, duration, purpose, financier, rate ->
                                    viewModel.applyForLoan(
                                        requestedAmount = amount,
                                        durationMonths = duration,
                                        purpose = purpose,
                                        financier = financier,
                                        interestRate = rate
                                    ) { loan ->
                                        newlyAppliedLoan = loan
                                        currentTab = NavigationTab.MY_LOANS
                                    }
                                }
                            )
                            NavigationTab.EXPENSES -> ExpenseTrackerScreen(
                                expenses = userExpenses,
                                linkedBankAccounts = linkedBankAccounts,
                                onAddExpense = { title, amt, cat, method, bId, date, notes ->
                                    viewModel.addExpense(title, amt, cat, method, bId, date, notes)
                                },
                                onDeleteExpense = { exp -> viewModel.deleteExpense(exp) },
                                onNavigateToBudget = { currentTab = NavigationTab.BUDGET },
                                onConnectBank = { showConnectBankModal = true }
                            )
                            NavigationTab.BUDGET -> BudgetVisualizationScreen(
                                budgets = userBudgets,
                                expenses = userExpenses,
                                onSetBudget = { cat, limit, month -> viewModel.setBudget(cat, limit, month) },
                                onNavigateToExpenses = { currentTab = NavigationTab.EXPENSES },
                                onAskAi = { currentTab = NavigationTab.AI_ASSISTANT }
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
                                onToggleAutoPay = { loan, enable -> viewModel.toggleAutoPay(loan, enable) },
                                onNavigateToApplyForLoan = { currentTab = NavigationTab.APPLY_FOR_LOAN }
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
                                expenses = userExpenses,
                                budgets = userBudgets,
                                onNavigateToFindFinancier = { currentTab = NavigationTab.FIND_FINANCIER },
                                onNavigateToMyLoans = { currentTab = NavigationTab.MY_LOANS },
                                onNavigateToAi = { currentTab = NavigationTab.AI_ASSISTANT },
                                onNavigateToSupport = { currentTab = NavigationTab.SUPPORT },
                                onNavigateToCalendar = { currentTab = NavigationTab.CALENDAR },
                                onNavigateToCalculator = { currentTab = NavigationTab.CALCULATOR },
                                onNavigateToExpenses = { currentTab = NavigationTab.EXPENSES },
                                onNavigateToBudget = { currentTab = NavigationTab.BUDGET },
                                onPayLoan = { loan ->
                                    isPaymentOffline = false
                                    paymentLoanTarget = loan
                                },
                                onEarlySettle = { loan -> viewModel.settleLoanEarly(loan) },
                                onToggleAutoPay = { loan, enable -> viewModel.toggleAutoPay(loan, enable) },
                                onAcceptOffer = { offer -> viewModel.acceptOffer(offer) },
                                onRejectOffer = { offer -> viewModel.rejectOffer(offer) },
                                onViewReceipt = { payment -> viewModel.showReceipt(payment) },
                                onConnectBank = { showConnectBankModal = true }
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
                                reports = allReports,
                                tickets = adminTickets,
                                auditLogs = allAuditLogs,
                                onVerifyFinancier = { fId, status -> viewModel.updateFinancierVerification(fId, status) },
                                onReseedData = {
                                    viewModel.reseedAllData { msg ->
                                        scope.launch { snackbarHostState.showSnackbar(msg) }
                                    }
                                },
                                onSyncFirebase = {
                                    viewModel.syncAllDataToFirebase { ok, msg ->
                                        scope.launch { snackbarHostState.showSnackbar(msg) }
                                    }
                                }
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
            primaryBankAccount = linkedBankAccounts.firstOrNull { it.isPrimary } ?: linkedBankAccounts.firstOrNull(),
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

    // Connect Bank Dialog
    if (showConnectBankModal) {
        ConnectBankDialog(
            currentBankAccounts = linkedBankAccounts,
            onDismiss = { showConnectBankModal = false },
            onConnectBank = { bName, accNum, ifsc, holder, type, upi, primary ->
                viewModel.connectBankAccount(bName, accNum, ifsc, holder, type, upi, primary)
            },
            onDeleteBankAccount = { account ->
                viewModel.removeBankAccount(account)
            },
            onSetPrimary = { accountId ->
                viewModel.setPrimaryBankAccount(accountId)
            }
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

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.logout()
                            showUserSwitcher = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Out of Account")
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

    // Loan Application Success Celebration Dialog
    newlyAppliedLoan?.let { loan ->
        Dialog(onDismissRequest = { newlyAppliedLoan = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(22.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF67FDCD).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "Loan Approved & Disbursed!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Your application was approved instantly. Disbursed funds are credited to your account.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Loan ID:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(loan.id, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Amount:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatCurrency(loan.principalAmount), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = LoanPrimary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Duration:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${loan.durationMonths} Months", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                            }
                            if (loan.purpose.isNotEmpty()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Purpose:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(loan.purpose, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Monthly EMI:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatCurrency(loan.nextInstallmentAmount), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("First Due Date:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(loan.nextDueDate, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Disbursal Tx:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(loan.disbursementTxId, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Button(
                        onClick = { newlyAppliedLoan = null },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View in My Loans")
                    }
                }
            }
        }
    }
    }
}

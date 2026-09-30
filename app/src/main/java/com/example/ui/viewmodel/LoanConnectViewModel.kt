package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.LoanConnectAiService
import com.example.data.database.AppDatabase
import com.example.data.firebase.FirebaseManager
import com.example.data.model.*
import com.example.data.repository.LoanConnectRepository
import com.example.data.sample.DemoDataSeeder
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String,
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionSuggested: String? = null
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LoanConnectViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repo = LoanConnectRepository(db.loanConnectDao())
    private val aiService = LoanConnectAiService(application)
    val firebaseManager = FirebaseManager(application)

    // Current User
    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    val allUsers: StateFlow<List<UserEntity>> = repo.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<UserEntity?> = _currentUserId
        .flatMapLatest { id -> repo.getUserById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Linked Bank Accounts
    val linkedBankAccounts: StateFlow<List<BankAccountEntity>> = _currentUserId
        .flatMapLatest { id -> repo.getBankAccountsForUser(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Real-time Expense Tracking & Budget Visualization
    val userExpenses: StateFlow<List<ExpenseEntity>> = _currentUserId
        .flatMapLatest { id -> repo.getExpensesForUser(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userBudgets: StateFlow<List<BudgetEntity>> = _currentUserId
        .flatMapLatest { id -> repo.getBudgetsForUser(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Disclosed Financiers for marketplace
    val disclosedFinanciers: StateFlow<List<UserEntity>> = repo.getDisclosedFinanciers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Isolated Loans
    val userLoans: StateFlow<List<LoanEntity>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else when (user.role) {
                UserRole.FINANCIER -> repo.getLoansForFinancier(user.id)
                UserRole.BORROWER -> repo.getLoansForBorrower(user.id)
                UserRole.ADMIN -> repo.getAllLoansForAdmin()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Loan Requests
    val userRequests: StateFlow<List<LoanRequestEntity>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else when (user.role) {
                UserRole.FINANCIER -> repo.getRequestsForFinancier(user.id)
                UserRole.BORROWER -> repo.getRequestsForBorrower(user.id)
                UserRole.ADMIN -> flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Loan Offers
    val userOffers: StateFlow<List<LoanOfferEntity>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else when (user.role) {
                UserRole.FINANCIER -> repo.getOffersForFinancier(user.id)
                UserRole.BORROWER -> repo.getOffersForBorrower(user.id)
                UserRole.ADMIN -> flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Payments
    val userPayments: StateFlow<List<PaymentEntity>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else when (user.role) {
                UserRole.FINANCIER -> repo.getPaymentsForFinancier(user.id)
                UserRole.BORROWER -> repo.getPaymentsForBorrower(user.id)
                UserRole.ADMIN -> repo.getAllPaymentsForAdmin()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Schedules
    val userSchedules: StateFlow<List<PaymentScheduleEntity>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else when (user.role) {
                UserRole.FINANCIER -> repo.getSchedulesForFinancier(user.id)
                UserRole.BORROWER -> repo.getSchedulesForBorrower(user.id)
                UserRole.ADMIN -> flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AutoPay Mandates
    val userMandates: StateFlow<List<AutoPayMandateEntity>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else when (user.role) {
                UserRole.BORROWER -> repo.getMandatesForBorrower(user.id)
                UserRole.FINANCIER -> repo.getMandatesForFinancier(user.id)
                UserRole.ADMIN -> flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Commissions
    val userCommissions: StateFlow<List<CommissionEntity>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else when (user.role) {
                UserRole.FINANCIER -> repo.getCommissionsForFinancier(user.id)
                UserRole.ADMIN -> repo.getAllCommissionsForAdmin()
                UserRole.BORROWER -> flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications
    val userNotifications: StateFlow<List<NotificationEntity>> = _currentUserId
        .flatMapLatest { id -> repo.getNotificationsForUser(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Lists
    val allReports: StateFlow<List<UserReportEntity>> = repo.getAllReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<AuditLogEntity>> = repo.getAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminTickets: StateFlow<List<SupportTicketEntity>> = repo.getAllTicketsForAdmin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Assistant State
    private val _aiMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "msg-welcome",
                sender = "ai",
                text = "Hello! I am LoanConnect AI. You can ask me anything about your loans, upcoming payments, overdue borrowers, or calculate repayments. How can I help you today?"
            )
        )
    )
    val aiMessages: StateFlow<List<ChatMessage>> = _aiMessages.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _selectedModel = MutableStateFlow("gemini-3.5-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _isSearchGroundingEnabled = MutableStateFlow(true)
    val isSearchGroundingEnabled: StateFlow<Boolean> = _isSearchGroundingEnabled.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    fun setSelectedModel(model: String) {
        _selectedModel.value = model
    }

    fun toggleSearchGrounding(enabled: Boolean) {
        _isSearchGroundingEnabled.value = enabled
    }

    fun clearChat() {
        _aiMessages.value = listOf(
            ChatMessage(
                id = "msg-welcome",
                sender = "ai",
                text = "Hello! I am LoanConnect AI. You can ask me anything about your loans, upcoming payments, overdue borrowers, or calculate repayments. How can I help you today?"
            )
        )
    }

    fun signInWithGoogle(context: Context, role: UserRole = UserRole.BORROWER, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            val result = firebaseManager.signInWithGoogle(context)
            if (result.isSuccess) {
                val fUser = result.getOrNull()!!
                val existing = repo.getUserByEmail(fUser.email ?: "")
                val user = if (existing != null) {
                    existing
                } else {
                    val newUser = UserEntity(
                        id = fUser.uid,
                        name = fUser.displayName ?: fUser.email?.substringBefore("@") ?: "Google User",
                        email = fUser.email ?: "${fUser.uid}@loanconnect.io",
                        phone = fUser.phoneNumber ?: "+91 98000 12345",
                        role = role,
                        verificationStatus = VerificationStatus.VERIFIED
                    )
                    repo.registerUser(newUser)
                    newUser
                }
                firebaseManager.syncUserProfile(user)
                _currentUserId.value = user.id
                _isLoggedIn.value = true
                onResult(true, null)
            } else {
                val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Google sign in failed"
                _authError.value = errorMsg
                onResult(false, errorMsg)
            }
        }
    }

    // Last generated receipt for viewing
    private val _activeReceipt = MutableStateFlow<PaymentEntity?>(null)
    val activeReceipt: StateFlow<PaymentEntity?> = _activeReceipt.asStateFlow()

    init {
        viewModelScope.launch {
            DemoDataSeeder.seedIfNeeded(db.loanConnectDao())
        }
    }

    fun reseedAllData(onComplete: (String) -> Unit = {}) {
        viewModelScope.launch {
            DemoDataSeeder.seedIfNeeded(db.loanConnectDao(), force = true)
            onComplete("All platform demo datasets (users, loans, schedules, payments, budgets) refreshed successfully!")
        }
    }

    fun syncAllDataToFirebase(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val users = repo.getAllUsers().first()
                for (u in users) {
                    firebaseManager.syncUserProfile(u)
                }
                val loans = repo.getAllLoansForAdmin().first()
                for (l in loans) {
                    firebaseManager.syncLoan(l)
                }
                val payments = repo.getAllPaymentsForAdmin().first()
                for (p in payments) {
                    firebaseManager.syncPayment(p)
                }
                onResult(true, "Cloud sync complete: ${users.size} users, ${loans.size} loans, and ${payments.size} payments synced to Firestore.")
            } catch (e: Exception) {
                onResult(false, "Firestore sync failed: ${e.message}")
            }
        }
    }

    fun switchUser(userId: String) {
        _currentUserId.value = userId
        _isLoggedIn.value = true
        _authError.value = null
    }

    fun login(emailOrPhone: String, pass: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            val user = repo.authenticateUser(emailOrPhone.trim(), pass.trim())
            if (user != null) {
                _currentUserId.value = user.id
                _isLoggedIn.value = true
                onResult(true)
            } else {
                _authError.value = "Invalid email/phone or password. Please verify your credentials."
                onResult(false)
            }
        }
    }

    fun register(
        name: String,
        email: String,
        phone: String,
        pass: String,
        role: UserRole,
        businessName: String = "",
        address: String = "",
        serviceArea: String = "All Regions",
        minAmount: Double = 5000.0,
        maxAmount: Double = 500000.0,
        rate: Double = 10.0,
        interestType: InterestType = InterestType.MONTHLY,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val prefix = when (role) {
                UserRole.FINANCIER -> "user_f_"
                UserRole.BORROWER -> "user_b_"
                UserRole.ADMIN -> "user_a_"
            }
            val newUserId = "$prefix${System.currentTimeMillis() % 100000}"
            val newUser = UserEntity(
                id = newUserId,
                name = name.trim(),
                email = email.trim(),
                phone = phone.trim(),
                password = pass.trim(),
                role = role,
                businessName = businessName.trim(),
                address = address.trim(),
                serviceArea = serviceArea.trim(),
                minLoanAmount = minAmount,
                maxLoanAmount = maxAmount,
                standardInterestRate = rate,
                standardInterestType = interestType,
                verificationStatus = if (role == UserRole.FINANCIER) VerificationStatus.PENDING else VerificationStatus.VERIFIED
            )
            repo.registerUser(newUser)
            _currentUserId.value = newUserId
            _isLoggedIn.value = true
            _authError.value = null
            onComplete()
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentUserId.value = ""
        _authError.value = null
    }

    // --- Bank Account Management ---
    fun connectBankAccount(
        bankName: String,
        accountNumber: String,
        ifscCode: String,
        accountHolderName: String,
        accountType: String = "Savings",
        upiId: String = "",
        isPrimary: Boolean = true
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val last4 = if (accountNumber.length >= 4) accountNumber.takeLast(4) else accountNumber
            val bankAccount = BankAccountEntity(
                id = "BANK-${System.currentTimeMillis() % 100000}",
                userId = user.id,
                bankName = bankName,
                accountNumber = accountNumber,
                accountNumberLast4 = last4,
                ifscCode = ifscCode.uppercase().trim(),
                accountHolderName = accountHolderName.ifEmpty { user.name },
                accountType = accountType,
                isVerified = true,
                isPrimary = isPrimary,
                upiId = upiId.ifEmpty { "${user.phone.filter { it.isDigit() }}@upi" }
            )
            repo.addBankAccount(bankAccount)
        }
    }

    fun removeBankAccount(bankAccount: BankAccountEntity) {
        viewModelScope.launch {
            repo.deleteBankAccount(bankAccount)
        }
    }

    fun setPrimaryBankAccount(bankAccountId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repo.setPrimaryBankAccount(user.id, bankAccountId)
        }
    }

    // --- Expense Management ---
    fun addExpense(
        title: String,
        amount: Double,
        category: String,
        paymentMethod: String,
        bankAccountId: String? = null,
        date: String,
        notes: String = "",
        isRecurring: Boolean = false
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repo.addExpense(
                userId = user.id,
                title = title.trim(),
                amount = amount,
                category = category,
                paymentMethod = paymentMethod,
                bankAccountId = bankAccountId,
                date = date,
                notes = notes.trim(),
                isRecurring = isRecurring
            )
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repo.deleteExpense(expense)
        }
    }

    // --- Budget Management ---
    fun setBudget(
        category: String,
        monthlyLimit: Double,
        month: String,
        alertThresholdPercent: Double = 80.0
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repo.setBudget(
                userId = user.id,
                category = category,
                monthlyLimit = monthlyLimit,
                month = month,
                alertThresholdPercent = alertThresholdPercent
            )
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repo.deleteBudget(budget)
        }
    }

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    fun clearActiveReceipt() {
        _activeReceipt.value = null
    }

    fun showReceipt(payment: PaymentEntity) {
        _activeReceipt.value = payment
    }

    // --- Action Handlers ---
    fun makePayment(
        loan: LoanEntity,
        amount: Double,
        method: PaymentMethod,
        notes: String = "",
        isOffline: Boolean = false
    ) {
        viewModelScope.launch {
            val payment = repo.recordPayment(loan, amount, method, notes, isOffline)
            _activeReceipt.value = payment
            firebaseManager.syncPayment(payment)
            val updated = repo.getLoanByIdSync(loan.id)
            if (updated != null) {
                firebaseManager.syncLoan(updated)
            }
        }
    }

    fun settleLoanEarly(loan: LoanEntity) {
        viewModelScope.launch {
            val payment = repo.settleLoanEarly(loan)
            _activeReceipt.value = payment
            firebaseManager.syncPayment(payment)
            val updated = repo.getLoanByIdSync(loan.id)
            if (updated != null) {
                firebaseManager.syncLoan(updated)
            }
        }
    }

    fun submitLoanRequest(
        financier: UserEntity,
        amount: Double,
        purpose: String,
        duration: Int,
        message: String
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repo.submitLoanRequest(
                borrowerId = user.id,
                borrowerName = user.name,
                financierId = financier.id,
                financierName = financier.businessName.ifEmpty { financier.name },
                amount = amount,
                purpose = purpose,
                durationMonths = duration,
                message = message
            )
        }
    }

    fun createLoanOffer(
        request: LoanRequestEntity,
        approvedAmount: Double,
        interestRate: Double,
        interestType: InterestType,
        durationMonths: Int,
        installmentAmount: Double,
        processingFee: Double,
        platformFee: Double,
        totalPayable: Double,
        termsText: String
    ) {
        val financier = currentUser.value ?: return
        viewModelScope.launch {
            repo.createOffer(
                requestId = request.id,
                financierId = financier.id,
                borrowerId = request.borrowerId,
                financierName = financier.businessName.ifEmpty { financier.name },
                borrowerName = request.borrowerName,
                approvedAmount = approvedAmount,
                interestRate = interestRate,
                interestType = interestType,
                durationMonths = durationMonths,
                installmentAmount = installmentAmount,
                processingFee = processingFee,
                platformFee = platformFee,
                totalPayable = totalPayable,
                termsText = termsText
            )
        }
    }

    fun acceptOffer(offer: LoanOfferEntity) {
        viewModelScope.launch {
            repo.acceptOffer(offer)
        }
    }

    fun applyForLoan(
        requestedAmount: Double,
        durationMonths: Int,
        purpose: String,
        financier: UserEntity? = null,
        interestRate: Double = 10.0,
        paymentFrequency: String = "Monthly",
        onSuccess: (LoanEntity) -> Unit = {}
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val loan = repo.applyForLoan(
                borrowerId = user.id,
                borrowerName = user.name,
                requestedAmount = requestedAmount,
                durationMonths = durationMonths,
                purpose = purpose,
                financierId = financier?.id,
                financierName = financier?.businessName?.ifEmpty { financier.name },
                interestRate = interestRate,
                paymentFrequency = paymentFrequency
            )
            firebaseManager.syncLoan(loan)
            onSuccess(loan)
        }
    }

    fun rejectOffer(offer: LoanOfferEntity) {
        viewModelScope.launch {
            repo.rejectOffer(offer)
        }
    }

    fun toggleAutoPay(loan: LoanEntity, enable: Boolean) {
        viewModelScope.launch {
            repo.toggleAutoPay(loan, enable, loan.nextInstallmentAmount)
        }
    }

    fun updateFinancierVerification(financierId: String, status: VerificationStatus) {
        viewModelScope.launch {
            repo.updateFinancierVerification(financierId, status)
        }
    }

    fun submitSupportTicket(category: String, desc: String, loanId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repo.createTicket(user.id, user.name, category, desc, loanId)
        }
    }

    fun submitReport(reportedUserId: String, reportedUserName: String, category: String, desc: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repo.submitReport(user.id, user.name, reportedUserId, reportedUserName, category, desc)
        }
    }

    fun blockUser(blockedUserId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repo.blockUser(user.id, blockedUserId)
        }
    }

    fun sendAiMessage(
        prompt: String,
        modelOverride: String? = null,
        searchOverride: Boolean? = null,
        onComplete: ((String) -> Unit)? = null
    ) {
        val user = currentUser.value ?: return
        val currentLoans = userLoans.value
        val currentPayments = userPayments.value
        val lang = _selectedLanguage.value
        val model = modelOverride ?: _selectedModel.value
        val useSearch = searchOverride ?: _isSearchGroundingEnabled.value

        val userMsg = ChatMessage(
            id = "msg-${System.currentTimeMillis()}-u",
            sender = "user",
            text = prompt
        )
        val updatedList = _aiMessages.value + userMsg
        _aiMessages.value = updatedList
        _isAiLoading.value = true

        viewModelScope.launch {
            val history = updatedList.map { it.sender to it.text }
            val response = aiService.queryMultiTurnAi(
                conversationHistory = history,
                currentUser = user,
                loans = currentLoans,
                payments = currentPayments,
                modelName = model,
                enableSearchGrounding = useSearch,
                language = lang
            )
            val aiMsg = ChatMessage(
                id = "msg-${System.currentTimeMillis()}-ai",
                sender = "ai",
                text = response
            )
            _aiMessages.value = _aiMessages.value + aiMsg
            _isAiLoading.value = false
            onComplete?.invoke(response)
        }
    }

    fun markNotificationsRead() {
        val uid = _currentUserId.value
        viewModelScope.launch {
            repo.markAllNotificationsRead(uid)
        }
    }
}

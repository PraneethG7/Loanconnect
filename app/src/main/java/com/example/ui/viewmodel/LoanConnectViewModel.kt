package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.LoanConnectAiService
import com.example.data.database.AppDatabase
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

class LoanConnectViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repo = LoanConnectRepository(db.loanConnectDao())
    private val aiService = LoanConnectAiService(application)

    // Current User
    private val _currentUserId = MutableStateFlow("user_b1") // Starts as Priya Sharma (Borrower)
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    val allUsers: StateFlow<List<UserEntity>> = repo.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<UserEntity?> = _currentUserId
        .flatMapLatest { id -> repo.getUserById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Last generated receipt for viewing
    private val _activeReceipt = MutableStateFlow<PaymentEntity?>(null)
    val activeReceipt: StateFlow<PaymentEntity?> = _activeReceipt.asStateFlow()

    init {
        viewModelScope.launch {
            DemoDataSeeder.seedIfNeeded(db.loanConnectDao())
        }
    }

    fun switchUser(userId: String) {
        _currentUserId.value = userId
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
        }
    }

    fun settleLoanEarly(loan: LoanEntity) {
        viewModelScope.launch {
            val payment = repo.settleLoanEarly(loan)
            _activeReceipt.value = payment
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

    fun sendAiMessage(prompt: String) {
        val user = currentUser.value ?: return
        val currentLoans = userLoans.value
        val currentPayments = userPayments.value
        val lang = _selectedLanguage.value

        val userMsg = ChatMessage(
            id = "msg-${System.currentTimeMillis()}-u",
            sender = "user",
            text = prompt
        )
        _aiMessages.value = _aiMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            val response = aiService.queryAi(
                prompt = prompt,
                currentUser = user,
                loans = currentLoans,
                payments = currentPayments,
                language = lang
            )
            val aiMsg = ChatMessage(
                id = "msg-${System.currentTimeMillis()}-ai",
                sender = "ai",
                text = response
            )
            _aiMessages.value = _aiMessages.value + aiMsg
            _isAiLoading.value = false
        }
    }

    fun markNotificationsRead() {
        val uid = _currentUserId.value
        viewModelScope.launch {
            repo.markAllNotificationsRead(uid)
        }
    }
}

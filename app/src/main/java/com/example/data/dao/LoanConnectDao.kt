package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanConnectDao {

    // --- Users ---
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserByIdSync(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'FINANCIER' AND verificationStatus = 'VERIFIED' AND isAcceptingRequests = 1")
    fun getDisclosedFinanciers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersByRole(role: UserRole): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE email = :query OR phone = :query LIMIT 1")
    suspend fun getUserByEmailOrPhone(query: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    // --- Loans (Strict Data Isolation) ---
    @Query("SELECT * FROM loans WHERE financierId = :financierId ORDER BY createdAt DESC")
    fun getLoansForFinancier(financierId: String): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE borrowerId = :borrowerId ORDER BY createdAt DESC")
    fun getLoansForBorrower(borrowerId: String): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans ORDER BY createdAt DESC")
    fun getAllLoansForAdmin(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE id = :loanId LIMIT 1")
    fun getLoanById(loanId: String): Flow<LoanEntity?>

    @Query("SELECT * FROM loans WHERE id = :loanId LIMIT 1")
    suspend fun getLoanByIdSync(loanId: String): LoanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoans(loans: List<LoanEntity>)

    @Update
    suspend fun updateLoan(loan: LoanEntity)

    // --- Loan Requests ---
    @Query("SELECT * FROM loan_requests WHERE financierId = :financierId ORDER BY createdAt DESC")
    fun getRequestsForFinancier(financierId: String): Flow<List<LoanRequestEntity>>

    @Query("SELECT * FROM loan_requests WHERE borrowerId = :borrowerId ORDER BY createdAt DESC")
    fun getRequestsForBorrower(borrowerId: String): Flow<List<LoanRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: LoanRequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequests(requests: List<LoanRequestEntity>)

    @Update
    suspend fun updateRequest(request: LoanRequestEntity)

    // --- Loan Offers ---
    @Query("SELECT * FROM loan_offers WHERE financierId = :financierId ORDER BY createdAt DESC")
    fun getOffersForFinancier(financierId: String): Flow<List<LoanOfferEntity>>

    @Query("SELECT * FROM loan_offers WHERE borrowerId = :borrowerId ORDER BY createdAt DESC")
    fun getOffersForBorrower(borrowerId: String): Flow<List<LoanOfferEntity>>

    @Query("SELECT * FROM loan_offers WHERE id = :offerId LIMIT 1")
    suspend fun getOfferById(offerId: String): LoanOfferEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: LoanOfferEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffers(offers: List<LoanOfferEntity>)

    @Update
    suspend fun updateOffer(offer: LoanOfferEntity)

    // --- Payments ---
    @Query("SELECT * FROM payments WHERE financierId = :financierId ORDER BY paymentDate DESC")
    fun getPaymentsForFinancier(financierId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE borrowerId = :borrowerId ORDER BY paymentDate DESC")
    fun getPaymentsForBorrower(borrowerId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE loanId = :loanId ORDER BY paymentDate DESC")
    fun getPaymentsForLoan(loanId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPaymentsForAdmin(): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    // --- Schedules ---
    @Query("SELECT * FROM payment_schedules WHERE loanId = :loanId ORDER BY installmentNumber ASC")
    fun getSchedulesForLoan(loanId: String): Flow<List<PaymentScheduleEntity>>

    @Query("SELECT * FROM payment_schedules WHERE loanId IN (SELECT id FROM loans WHERE borrowerId = :borrowerId) ORDER BY dueDate ASC")
    fun getSchedulesForBorrower(borrowerId: String): Flow<List<PaymentScheduleEntity>>

    @Query("SELECT * FROM payment_schedules WHERE loanId IN (SELECT id FROM loans WHERE financierId = :financierId) ORDER BY dueDate ASC")
    fun getSchedulesForFinancier(financierId: String): Flow<List<PaymentScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<PaymentScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: PaymentScheduleEntity)

    // --- AutoPay Mandates ---
    @Query("SELECT * FROM autopay_mandates WHERE borrowerId = :borrowerId")
    fun getMandatesForBorrower(borrowerId: String): Flow<List<AutoPayMandateEntity>>

    @Query("SELECT * FROM autopay_mandates WHERE financierId = :financierId")
    fun getMandatesForFinancier(financierId: String): Flow<List<AutoPayMandateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMandate(mandate: AutoPayMandateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMandates(mandates: List<AutoPayMandateEntity>)

    @Update
    suspend fun updateMandate(mandate: AutoPayMandateEntity)

    // --- Commissions ---
    @Query("SELECT * FROM commissions WHERE financierId = :financierId ORDER BY createdAt DESC")
    fun getCommissionsForFinancier(financierId: String): Flow<List<CommissionEntity>>

    @Query("SELECT * FROM commissions ORDER BY createdAt DESC")
    fun getAllCommissionsForAdmin(): Flow<List<CommissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommission(commission: CommissionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommissions(commissions: List<CommissionEntity>)

    // --- Notifications ---
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllNotificationsRead(userId: String)

    // --- Support Tickets ---
    @Query("SELECT * FROM support_tickets WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTicketsForUser(userId: String): Flow<List<SupportTicketEntity>>

    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    fun getAllTicketsForAdmin(): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<SupportTicketEntity>)

    @Update
    suspend fun updateTicket(ticket: SupportTicketEntity)

    // --- Reports ---
    @Query("SELECT * FROM user_reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<UserReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: UserReportEntity)

    @Update
    suspend fun updateReport(report: UserReportEntity)

    // --- Blocked Users ---
    @Query("SELECT * FROM blocked_users WHERE blockerId = :blockerId")
    fun getBlockedUsers(blockerId: String): Flow<List<BlockedUserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedUser(blocked: BlockedUserEntity)

    @Delete
    suspend fun deleteBlockedUser(blocked: BlockedUserEntity)

    // --- Audit Logs ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLogs(logs: List<AuditLogEntity>)

    // --- Bank Accounts ---
    @Query("SELECT * FROM bank_accounts WHERE userId = :userId ORDER BY isPrimary DESC, createdAt DESC")
    fun getBankAccountsForUser(userId: String): Flow<List<BankAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBankAccount(bankAccount: BankAccountEntity)

    @Delete
    suspend fun deleteBankAccount(bankAccount: BankAccountEntity)

    @Query("UPDATE bank_accounts SET isPrimary = 0 WHERE userId = :userId")
    suspend fun clearPrimaryBankAccounts(userId: String)

    @Query("UPDATE bank_accounts SET isPrimary = 1 WHERE id = :bankAccountId AND userId = :userId")
    suspend fun setPrimaryBankAccount(userId: String, bankAccountId: String)

    // --- Real-time Expense Tracking ---
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC, createdAt DESC")
    fun getExpensesForUser(userId: String): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    // --- Budget Visualization ---
    @Query("SELECT * FROM budgets WHERE userId = :userId ORDER BY category ASC")
    fun getBudgetsForUser(userId: String): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>)

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)
}

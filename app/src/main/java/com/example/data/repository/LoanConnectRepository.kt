package com.example.data.repository

import com.example.data.dao.LoanConnectDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class LoanConnectRepository(private val dao: LoanConnectDao) {

    // --- Users ---
    fun getAllUsers(): Flow<List<UserEntity>> = dao.getAllUsers()
    fun getUserById(userId: String): Flow<UserEntity?> = dao.getUserById(userId)
    suspend fun getUserByIdSync(userId: String): UserEntity? = dao.getUserByIdSync(userId)
    fun getDisclosedFinanciers(): Flow<List<UserEntity>> = dao.getDisclosedFinanciers()

    suspend fun insertUser(user: UserEntity) = dao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = dao.updateUser(user)

    // --- Loans (Strict Data Isolation) ---
    fun getLoansForFinancier(financierId: String): Flow<List<LoanEntity>> =
        dao.getLoansForFinancier(financierId)

    fun getLoansForBorrower(borrowerId: String): Flow<List<LoanEntity>> =
        dao.getLoansForBorrower(borrowerId)

    fun getAllLoansForAdmin(): Flow<List<LoanEntity>> = dao.getAllLoansForAdmin()

    fun getLoanById(loanId: String): Flow<LoanEntity?> = dao.getLoanById(loanId)

    // --- Requests ---
    fun getRequestsForFinancier(financierId: String): Flow<List<LoanRequestEntity>> =
        dao.getRequestsForFinancier(financierId)

    fun getRequestsForBorrower(borrowerId: String): Flow<List<LoanRequestEntity>> =
        dao.getRequestsForBorrower(borrowerId)

    suspend fun submitLoanRequest(
        borrowerId: String,
        borrowerName: String,
        financierId: String,
        financierName: String,
        amount: Double,
        purpose: String,
        durationMonths: Int,
        message: String
    ): String {
        val reqId = "REQ-${System.currentTimeMillis() % 100000}"
        val req = LoanRequestEntity(
            id = reqId,
            borrowerId = borrowerId,
            financierId = financierId,
            borrowerName = borrowerName,
            financierName = financierName,
            requestedAmount = amount,
            purpose = purpose,
            durationMonths = durationMonths,
            message = message,
            status = RequestStatus.PENDING
        )
        dao.insertRequest(req)
        dao.insertNotification(
            NotificationEntity(
                id = "NOTIF-${System.currentTimeMillis()}",
                userId = financierId,
                title = "New Loan Request",
                message = "$borrowerName submitted a loan request for ₹${String.format("%,.0f", amount)} ($purpose).",
                type = "LOAN_REQUEST"
            )
        )
        return reqId
    }

    // --- Offers ---
    fun getOffersForFinancier(financierId: String): Flow<List<LoanOfferEntity>> =
        dao.getOffersForFinancier(financierId)

    fun getOffersForBorrower(borrowerId: String): Flow<List<LoanOfferEntity>> =
        dao.getOffersForBorrower(borrowerId)

    suspend fun createOffer(
        requestId: String,
        financierId: String,
        borrowerId: String,
        financierName: String,
        borrowerName: String,
        approvedAmount: Double,
        interestRate: Double,
        interestType: InterestType,
        durationMonths: Int,
        installmentAmount: Double,
        processingFee: Double,
        platformFee: Double,
        totalPayable: Double,
        termsText: String
    ): String {
        val offerId = "OFF-${System.currentTimeMillis() % 100000}"
        val offer = LoanOfferEntity(
            id = offerId,
            requestId = requestId,
            financierId = financierId,
            borrowerId = borrowerId,
            financierName = financierName,
            borrowerName = borrowerName,
            approvedAmount = approvedAmount,
            interestRate = interestRate,
            interestType = interestType,
            durationMonths = durationMonths,
            installmentAmount = installmentAmount,
            processingFee = processingFee,
            platformFee = platformFee,
            totalPayable = totalPayable,
            expiryDate = "2026-11-15",
            termsText = termsText,
            status = OfferStatus.OFFER_SENT
        )
        dao.insertOffer(offer)
        dao.insertNotification(
            NotificationEntity(
                id = "NOTIF-${System.currentTimeMillis()}",
                userId = borrowerId,
                title = "Loan Offer Received!",
                message = "$financierName offered ₹${String.format("%,.0f", approvedAmount)} at $interestRate% rate.",
                type = "LOAN_OFFER"
            )
        )
        return offerId
    }

    suspend fun acceptOffer(offer: LoanOfferEntity): LoanEntity {
        val loanId = "LOAN-${(100000..999999).random()}"
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val startDate = sdf.format(cal.time)
        cal.add(Calendar.MONTH, offer.durationMonths)
        val dueDate = sdf.format(cal.time)
        cal.time = Date()
        cal.add(Calendar.MONTH, 1)
        val nextDueDate = sdf.format(cal.time)

        val interestAmount = offer.totalPayable - offer.approvedAmount - offer.processingFee - offer.platformFee

        val loan = LoanEntity(
            id = loanId,
            financierId = offer.financierId,
            borrowerId = offer.borrowerId,
            financierName = offer.financierName,
            borrowerName = offer.borrowerName,
            principalAmount = offer.approvedAmount,
            interestRate = offer.interestRate,
            interestType = offer.interestType,
            interestAmount = if (interestAmount > 0) interestAmount else 0.0,
            processingFee = offer.processingFee,
            platformFee = offer.platformFee,
            totalPayable = offer.totalPayable,
            totalPaid = 0.0,
            principalPaid = 0.0,
            interestPaid = 0.0,
            startDate = startDate,
            dueDate = dueDate,
            nextDueDate = nextDueDate,
            nextInstallmentAmount = offer.installmentAmount,
            durationMonths = offer.durationMonths,
            status = LoanStatus.ACTIVE,
            disbursementTxId = "DISB-${System.currentTimeMillis() % 1000000}",
            agreementAcceptedAt = System.currentTimeMillis()
        )
        dao.insertLoan(loan)
        dao.updateOffer(offer.copy(status = OfferStatus.ACCEPTED))

        // Create initial installment schedules
        val scheduleList = mutableListOf<PaymentScheduleEntity>()
        val schedCal = Calendar.getInstance()
        val monthlyPrincipal = offer.approvedAmount / offer.durationMonths
        val monthlyInterest = (if (interestAmount > 0) interestAmount else 0.0) / offer.durationMonths
        for (i in 1..offer.durationMonths) {
            schedCal.add(Calendar.MONTH, 1)
            scheduleList.add(
                PaymentScheduleEntity(
                    id = "SCH-${loanId}-$i",
                    loanId = loanId,
                    installmentNumber = i,
                    dueAmount = offer.installmentAmount,
                    principalComponent = monthlyPrincipal,
                    interestComponent = monthlyInterest,
                    dueDate = sdf.format(schedCal.time),
                    status = ScheduleStatus.PENDING
                )
            )
        }
        dao.insertSchedules(scheduleList)

        dao.insertNotification(
            NotificationEntity(
                id = "NOTIF-${System.currentTimeMillis()}",
                userId = offer.financierId,
                title = "Loan Offer Accepted",
                message = "${offer.borrowerName} accepted offer for ₹${String.format("%,.0f", offer.approvedAmount)}. Loan $loanId is now ACTIVE.",
                type = "DISBURSEMENT"
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                id = "LOG-${System.currentTimeMillis()}",
                userId = offer.borrowerId,
                userName = offer.borrowerName,
                action = "OFFER_ACCEPTED",
                entityType = "LOAN",
                entityId = loanId,
                details = "Accepted loan agreement for ₹${offer.approvedAmount} with ${offer.financierName}."
            )
        )
        return loan
    }

    suspend fun rejectOffer(offer: LoanOfferEntity) {
        dao.updateOffer(offer.copy(status = OfferStatus.REJECTED))
    }

    // --- Payments ---
    fun getPaymentsForFinancier(financierId: String): Flow<List<PaymentEntity>> =
        dao.getPaymentsForFinancier(financierId)

    fun getPaymentsForBorrower(borrowerId: String): Flow<List<PaymentEntity>> =
        dao.getPaymentsForBorrower(borrowerId)

    fun getPaymentsForLoan(loanId: String): Flow<List<PaymentEntity>> =
        dao.getPaymentsForLoan(loanId)

    fun getAllPaymentsForAdmin(): Flow<List<PaymentEntity>> = dao.getAllPaymentsForAdmin()

    suspend fun recordPayment(
        loan: LoanEntity,
        amount: Double,
        method: PaymentMethod,
        notes: String = "",
        isOffline: Boolean = false
    ): PaymentEntity {
        val paymentId = "PAY-${System.currentTimeMillis() % 100000}"
        val receiptNumber = "REC-${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}-${(1000..9999).random()}"
        val txId = if (isOffline) "OFFLINE-${System.currentTimeMillis() % 100000}" else "TXN-${System.currentTimeMillis()}"

        // Split between interest and principal
        val remainingInterest = loan.interestAmount - loan.interestPaid
        val interestPortion = if (remainingInterest > 0) minOf(amount * 0.15, remainingInterest) else 0.0
        val principalPortion = amount - interestPortion

        val newTotalPaid = loan.totalPaid + amount
        val newPrincipalPaid = loan.principalPaid + principalPortion
        val newInterestPaid = loan.interestPaid + interestPortion

        val isFullyPaid = newTotalPaid >= loan.totalPayable
        val updatedStatus = if (isFullyPaid) LoanStatus.COMPLETED else LoanStatus.ACTIVE

        val payment = PaymentEntity(
            id = paymentId,
            loanId = loan.id,
            borrowerId = loan.borrowerId,
            financierId = loan.financierId,
            borrowerName = loan.borrowerName,
            financierName = loan.financierName,
            amount = amount,
            principalPaid = principalPortion,
            interestPaid = interestPortion,
            paymentMethod = method,
            transactionId = txId,
            status = PaymentStatus.SUCCESSFUL,
            paymentDate = System.currentTimeMillis(),
            notes = notes,
            isOfflineRecord = isOffline,
            receiptNumber = receiptNumber
        )
        dao.insertPayment(payment)

        dao.updateLoan(
            loan.copy(
                totalPaid = newTotalPaid,
                principalPaid = newPrincipalPaid,
                interestPaid = newInterestPaid,
                status = updatedStatus
            )
        )

        // Record Platform Commission (2.5% fee)
        val commissionAmount = amount * 0.025
        dao.insertCommission(
            CommissionEntity(
                id = "COMM-${System.currentTimeMillis() % 100000}",
                loanId = loan.id,
                paymentId = paymentId,
                financierId = loan.financierId,
                amount = commissionAmount,
                ratePercentage = 2.5,
                payer = "Borrower",
                recipient = "Platform"
            )
        )

        // Automatically log real-time expense for personal finance tracking
        dao.insertExpense(
            ExpenseEntity(
                id = "EXP-${System.currentTimeMillis() % 100000}",
                userId = loan.borrowerId,
                title = "Loan Repayment (${loan.financierName})",
                amount = amount,
                category = "Loan EMI",
                paymentMethod = when (method) {
                    PaymentMethod.UPI -> "Google Pay / UPI"
                    PaymentMethod.BANK_TRANSFER -> "Bank Transfer"
                    PaymentMethod.CARD -> "Debit Card"
                    PaymentMethod.NET_BANKING -> "Net Banking"
                    PaymentMethod.CASH -> "Cash"
                    PaymentMethod.AUTO_PAY -> "Auto-Debit"
                },
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                notes = "Loan ${loan.id} installment paid via $method. $notes"
            )
        )

        // Send notifications
        dao.insertNotification(
            NotificationEntity(
                id = "NOTIF-${System.currentTimeMillis()}-1",
                userId = loan.borrowerId,
                title = "Payment Successful",
                message = "Paid ₹${String.format("%,.0f", amount)} for ${loan.id}. Remaining: ₹${String.format("%,.0f", maxOf(0.0, loan.totalPayable - newTotalPaid))}.",
                type = "PAYMENT_SUCCESS"
            )
        )
        dao.insertNotification(
            NotificationEntity(
                id = "NOTIF-${System.currentTimeMillis()}-2",
                userId = loan.financierId,
                title = "Payment Received",
                message = "Received ₹${String.format("%,.0f", amount)} from ${loan.borrowerName} via $method.",
                type = "PAYMENT_SUCCESS"
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                id = "LOG-${System.currentTimeMillis()}",
                userId = loan.borrowerId,
                userName = loan.borrowerName,
                action = if (isOffline) "OFFLINE_PAYMENT_RECORDED" else "ONLINE_PAYMENT_SUCCESS",
                entityType = "PAYMENT",
                entityId = paymentId,
                details = "Recorded payment of ₹$amount on ${loan.id} via $method."
            )
        )

        return payment
    }

    suspend fun settleLoanEarly(loan: LoanEntity): PaymentEntity {
        val remainingTotal = maxOf(0.0, loan.totalPayable - loan.totalPaid)
        return recordPayment(
            loan = loan,
            amount = remainingTotal,
            method = PaymentMethod.UPI,
            notes = "Early Settlement Full Payoff",
            isOffline = false
        )
    }

    // --- Schedules ---
    fun getSchedulesForLoan(loanId: String): Flow<List<PaymentScheduleEntity>> =
        dao.getSchedulesForLoan(loanId)

    fun getSchedulesForBorrower(borrowerId: String): Flow<List<PaymentScheduleEntity>> =
        dao.getSchedulesForBorrower(borrowerId)

    fun getSchedulesForFinancier(financierId: String): Flow<List<PaymentScheduleEntity>> =
        dao.getSchedulesForFinancier(financierId)

    // --- AutoPay Mandates ---
    fun getMandatesForBorrower(borrowerId: String): Flow<List<AutoPayMandateEntity>> =
        dao.getMandatesForBorrower(borrowerId)

    fun getMandatesForFinancier(financierId: String): Flow<List<AutoPayMandateEntity>> =
        dao.getMandatesForFinancier(financierId)

    suspend fun toggleAutoPay(loan: LoanEntity, enable: Boolean, amount: Double) {
        dao.updateLoan(loan.copy(isAutoPayEnabled = enable))
        if (enable) {
            val mandate = AutoPayMandateEntity(
                id = "MAND-${System.currentTimeMillis() % 100000}",
                borrowerId = loan.borrowerId,
                loanId = loan.id,
                financierId = loan.financierId,
                amount = amount,
                frequency = loan.paymentFrequency,
                status = MandateStatus.ACTIVE,
                nextPaymentDate = loan.nextDueDate,
                providerReference = "E-MANDATE-NPCI-${(100000..999999).random()}"
            )
            dao.insertMandate(mandate)
        }
    }

    // --- Commissions ---
    fun getCommissionsForFinancier(financierId: String): Flow<List<CommissionEntity>> =
        dao.getCommissionsForFinancier(financierId)

    fun getAllCommissionsForAdmin(): Flow<List<CommissionEntity>> =
        dao.getAllCommissionsForAdmin()

    // --- Notifications ---
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>> =
        dao.getNotificationsForUser(userId)

    suspend fun markAllNotificationsRead(userId: String) =
        dao.markAllNotificationsRead(userId)

    // --- Support Tickets ---
    fun getTicketsForUser(userId: String): Flow<List<SupportTicketEntity>> =
        dao.getTicketsForUser(userId)

    fun getAllTicketsForAdmin(): Flow<List<SupportTicketEntity>> =
        dao.getAllTicketsForAdmin()

    suspend fun createTicket(userId: String, userName: String, category: String, desc: String, loanId: String): String {
        val tId = "TCK-${System.currentTimeMillis() % 100000}"
        dao.insertTicket(
            SupportTicketEntity(
                id = tId,
                userId = userId,
                userName = userName,
                category = category,
                description = desc,
                loanId = loanId,
                status = TicketStatus.OPEN
            )
        )
        return tId
    }

    suspend fun updateTicketStatus(ticket: SupportTicketEntity, status: TicketStatus, note: String) {
        dao.updateTicket(ticket.copy(status = status, resolutionNote = note))
    }

    // --- Reports ---
    fun getAllReports(): Flow<List<UserReportEntity>> = dao.getAllReports()

    suspend fun submitReport(reporterId: String, reporterName: String, reportedUserId: String, reportedUserName: String, category: String, desc: String) {
        dao.insertReport(
            UserReportEntity(
                id = "REP-${System.currentTimeMillis() % 100000}",
                reporterId = reporterId,
                reporterName = reporterName,
                reportedUserId = reportedUserId,
                reportedUserName = reportedUserName,
                category = category,
                description = desc
            )
        )
    }

    // --- Block ---
    fun getBlockedUsers(blockerId: String): Flow<List<BlockedUserEntity>> = dao.getBlockedUsers(blockerId)
    suspend fun blockUser(blockerId: String, blockedUserId: String) {
        dao.insertBlockedUser(
            BlockedUserEntity(
                id = "${blockerId}_$blockedUserId",
                blockerId = blockerId,
                blockedUserId = blockedUserId
            )
        )
    }

    // --- Audit Logs ---
    fun getAuditLogs(): Flow<List<AuditLogEntity>> = dao.getAuditLogs()

    // --- Admin Verification ---
    suspend fun updateFinancierVerification(financierId: String, status: VerificationStatus) {
        val user = dao.getUserByIdSync(financierId) ?: return
        dao.updateUser(user.copy(verificationStatus = status))
        dao.insertAuditLog(
            AuditLogEntity(
                id = "LOG-${System.currentTimeMillis()}",
                userId = "user_admin",
                userName = "Admin",
                action = "VERIFICATION_${status.name}",
                entityType = "USER",
                entityId = financierId,
                details = "Updated verification status of ${user.name} (${user.businessName}) to $status."
            )
        )
    }

    // --- Authentication & Account Management ---
    suspend fun authenticateUser(emailOrPhone: String, password: String): UserEntity? {
        val user = dao.getUserByEmailOrPhone(emailOrPhone) ?: return null
        return if (user.password == password) user else null
    }

    suspend fun registerUser(user: UserEntity): UserEntity {
        dao.insertUser(user)
        dao.insertAuditLog(
            AuditLogEntity(
                id = "LOG-${System.currentTimeMillis()}",
                userId = user.id,
                userName = user.name,
                action = "ACCOUNT_REGISTERED",
                entityType = "USER",
                entityId = user.id,
                details = "Registered as ${user.role.name} with email ${user.email}."
            )
        )
        return user
    }

    // --- Bank Accounts ---
    fun getBankAccountsForUser(userId: String): Flow<List<BankAccountEntity>> =
        dao.getBankAccountsForUser(userId)

    suspend fun addBankAccount(bankAccount: BankAccountEntity) {
        if (bankAccount.isPrimary) {
            dao.clearPrimaryBankAccounts(bankAccount.userId)
        }
        dao.insertBankAccount(bankAccount)
        dao.insertAuditLog(
            AuditLogEntity(
                id = "LOG-${System.currentTimeMillis()}",
                userId = bankAccount.userId,
                userName = bankAccount.accountHolderName,
                action = "BANK_ACCOUNT_CONNECTED",
                entityType = "BANK_ACCOUNT",
                entityId = bankAccount.id,
                details = "Connected ${bankAccount.bankName} (Ending in ${bankAccount.accountNumberLast4})."
            )
        )
    }

    suspend fun deleteBankAccount(bankAccount: BankAccountEntity) {
        dao.deleteBankAccount(bankAccount)
    }

    suspend fun setPrimaryBankAccount(userId: String, bankAccountId: String) {
        dao.clearPrimaryBankAccounts(userId)
        dao.setPrimaryBankAccount(userId, bankAccountId)
    }

    // --- Real-time Expense Tracking ---
    fun getExpensesForUser(userId: String): Flow<List<ExpenseEntity>> =
        dao.getExpensesForUser(userId)

    suspend fun addExpense(
        userId: String,
        title: String,
        amount: Double,
        category: String,
        paymentMethod: String,
        bankAccountId: String? = null,
        date: String,
        notes: String = "",
        isRecurring: Boolean = false
    ): ExpenseEntity {
        val expense = ExpenseEntity(
            id = "EXP-${System.currentTimeMillis() % 100000}",
            userId = userId,
            title = title,
            amount = amount,
            category = category,
            paymentMethod = paymentMethod,
            bankAccountId = bankAccountId,
            date = date,
            notes = notes,
            isRecurring = isRecurring
        )
        dao.insertExpense(expense)
        return expense
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        dao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        dao.deleteExpense(expense)
    }

    // --- Budget Visualization ---
    fun getBudgetsForUser(userId: String): Flow<List<BudgetEntity>> =
        dao.getBudgetsForUser(userId)

    suspend fun setBudget(
        userId: String,
        category: String,
        monthlyLimit: Double,
        month: String,
        alertThresholdPercent: Double = 80.0
    ): BudgetEntity {
        val budget = BudgetEntity(
            id = "BUD-${userId}_${category.replace(" ", "_")}_$month",
            userId = userId,
            category = category,
            monthlyLimit = monthlyLimit,
            month = month,
            alertThresholdPercent = alertThresholdPercent
        )
        dao.insertBudget(budget)
        return budget
    }

    suspend fun deleteBudget(budget: BudgetEntity) {
        dao.deleteBudget(budget)
    }
}

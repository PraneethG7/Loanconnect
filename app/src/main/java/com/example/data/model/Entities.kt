package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    FINANCIER,
    BORROWER,
    ADMIN
}

enum class VerificationStatus {
    NOT_SUBMITTED,
    PENDING,
    VERIFIED,
    REJECTED,
    SUSPENDED
}

enum class InterestType {
    MONTHLY,
    YEARLY,
    FIXED,
    NONE
}

enum class LoanStatus {
    PENDING_DISBURSEMENT,
    ACTIVE,
    OVERDUE,
    COMPLETED,
    SETTLED_EARLY
}

enum class RequestStatus {
    PENDING,
    UNDER_REVIEW,
    OFFER_SENT,
    ACCEPTED,
    REJECTED,
    CANCELLED
}

enum class OfferStatus {
    OFFER_SENT,
    ACCEPTED,
    REJECTED,
    EXPIRED
}

enum class PaymentStatus {
    SUCCESSFUL,
    PENDING,
    FAILED,
    REFUNDED
}

enum class PaymentMethod {
    UPI,
    BANK_TRANSFER,
    CARD,
    NET_BANKING,
    CASH,
    AUTO_PAY
}

enum class ScheduleStatus {
    PENDING,
    PAID,
    OVERDUE,
    PARTIALLY_PAID
}

enum class MandateStatus {
    ACTIVE,
    PAUSED,
    CANCELLED
}

enum class TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String, // e.g. "user_f1", "user_b1", "user_admin"
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val businessName: String = "",
    val serviceArea: String = "All Regions",
    val verificationStatus: VerificationStatus = VerificationStatus.VERIFIED,
    val address: String = "",
    val kycDocumentType: String = "Aadhaar / National ID",
    val isAcceptingRequests: Boolean = true,
    val minLoanAmount: Double = 5000.0,
    val maxLoanAmount: Double = 500000.0,
    val standardInterestRate: Double = 10.0,
    val standardInterestType: InterestType = InterestType.MONTHLY,
    val supportedMethods: String = "UPI, Bank Transfer, Net Banking, Card, Cash",
    val languages: String = "English, Hindi, Tamil",
    val password: String = "password123",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "loan_requests")
data class LoanRequestEntity(
    @PrimaryKey val id: String, // "REQ-1001"
    val borrowerId: String,
    val financierId: String,
    val borrowerName: String,
    val financierName: String,
    val requestedAmount: Double,
    val purpose: String,
    val durationMonths: Int,
    val paymentFrequency: String = "Monthly",
    val message: String = "",
    val status: RequestStatus = RequestStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "loan_offers")
data class LoanOfferEntity(
    @PrimaryKey val id: String, // "OFF-2001"
    val requestId: String,
    val financierId: String,
    val borrowerId: String,
    val financierName: String,
    val borrowerName: String,
    val approvedAmount: Double,
    val interestRate: Double,
    val interestType: InterestType,
    val durationMonths: Int,
    val paymentFrequency: String = "Monthly",
    val installmentAmount: Double,
    val processingFee: Double,
    val platformFee: Double,
    val totalPayable: Double,
    val expiryDate: String,
    val termsText: String,
    val status: OfferStatus = OfferStatus.OFFER_SENT,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey val id: String, // "LOAN-000101"
    val financierId: String,
    val borrowerId: String,
    val financierName: String,
    val borrowerName: String,
    val principalAmount: Double,
    val interestRate: Double,
    val interestType: InterestType,
    val interestAmount: Double,
    val processingFee: Double,
    val platformFee: Double,
    val totalPayable: Double,
    val totalPaid: Double = 0.0,
    val principalPaid: Double = 0.0,
    val interestPaid: Double = 0.0,
    val startDate: String,
    val dueDate: String,
    val nextDueDate: String,
    val nextInstallmentAmount: Double,
    val paymentFrequency: String = "Monthly",
    val durationMonths: Int,
    val status: LoanStatus = LoanStatus.ACTIVE,
    val disbursementTxId: String = "",
    val agreementAcceptedAt: Long = System.currentTimeMillis(),
    val isAutoPayEnabled: Boolean = false,
    val purpose: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String, // "PAY-9001"
    val loanId: String,
    val borrowerId: String,
    val financierId: String,
    val borrowerName: String,
    val financierName: String,
    val amount: Double,
    val principalPaid: Double,
    val interestPaid: Double,
    val paymentMethod: PaymentMethod,
    val transactionId: String,
    val status: PaymentStatus = PaymentStatus.SUCCESSFUL,
    val paymentDate: Long = System.currentTimeMillis(),
    val notes: String = "",
    val isOfflineRecord: Boolean = false,
    val receiptNumber: String = ""
)

@Entity(tableName = "payment_schedules")
data class PaymentScheduleEntity(
    @PrimaryKey val id: String, // "SCH-101-1"
    val loanId: String,
    val installmentNumber: Int,
    val dueAmount: Double,
    val principalComponent: Double,
    val interestComponent: Double,
    val dueDate: String,
    val status: ScheduleStatus = ScheduleStatus.PENDING,
    val paidAmount: Double = 0.0,
    val paidDate: String = ""
)

@Entity(tableName = "autopay_mandates")
data class AutoPayMandateEntity(
    @PrimaryKey val id: String, // "MAND-501"
    val borrowerId: String,
    val loanId: String,
    val financierId: String,
    val amount: Double,
    val frequency: String = "Monthly",
    val status: MandateStatus = MandateStatus.ACTIVE,
    val nextPaymentDate: String,
    val providerReference: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "commissions")
data class CommissionEntity(
    @PrimaryKey val id: String,
    val loanId: String,
    val paymentId: String,
    val financierId: String,
    val amount: Double,
    val ratePercentage: Double,
    val payer: String, // "Borrower" or "Financier"
    val recipient: String, // "Platform" or "Financier"
    val status: String = "SETTLED",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val id: String, // "TCK-801"
    val userId: String,
    val userName: String,
    val category: String,
    val description: String,
    val loanId: String = "",
    val status: TicketStatus = TicketStatus.OPEN,
    val createdAt: Long = System.currentTimeMillis(),
    val resolutionNote: String = ""
)

@Entity(tableName = "user_reports")
data class UserReportEntity(
    @PrimaryKey val id: String,
    val reporterId: String,
    val reporterName: String,
    val reportedUserId: String,
    val reportedUserName: String,
    val category: String,
    val description: String,
    val status: String = "UNDER_REVIEW",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "blocked_users")
data class BlockedUserEntity(
    @PrimaryKey val id: String,
    val blockerId: String,
    val blockedUserId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val action: String,
    val entityType: String,
    val entityId: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bank_accounts")
data class BankAccountEntity(
    @PrimaryKey val id: String, // "BANK-101"
    val userId: String,
    val bankName: String, // "HDFC Bank", "State Bank of India", etc.
    val accountNumber: String, // "5010049281726"
    val accountNumberLast4: String, // "1726"
    val ifscCode: String, // "HDFC0000240"
    val accountHolderName: String,
    val accountType: String = "Savings", // "Savings", "Current"
    val isVerified: Boolean = true,
    val isPrimary: Boolean = true,
    val upiId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String, // "EXP-1001"
    val userId: String,
    val title: String,
    val amount: Double,
    val category: String, // "Food & Dining", "Groceries", "Loan EMI", "Transportation", "Utilities", "Shopping", "Health", "Entertainment", "Education", "Other"
    val paymentMethod: String, // "Google Pay", "PhonePe", "Paytm", "Connected Bank (HDFC)", "UPI", "Debit Card", "Cash"
    val bankAccountId: String? = null,
    val date: String, // "YYYY-MM-DD"
    val notes: String = "",
    val isRecurring: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: String, // "BUD-101"
    val userId: String,
    val category: String, // "Overall", "Food & Dining", "Groceries", "Loan EMI", "Transportation", "Utilities", "Shopping", "Entertainment"
    val monthlyLimit: Double,
    val month: String, // "2026-09"
    val alertThresholdPercent: Double = 80.0,
    val createdAt: Long = System.currentTimeMillis()
)


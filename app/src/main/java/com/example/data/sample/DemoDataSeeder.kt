package com.example.data.sample

import com.example.data.dao.LoanConnectDao
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DemoDataSeeder {

    suspend fun seedIfNeeded(dao: LoanConnectDao, force: Boolean = false) = withContext(Dispatchers.IO) {
        val existing = dao.getUserByIdSync("user_f1")
        val existingLoan = dao.getLoanByIdSync("LOAN-000101")
        if (!force && existing != null && existingLoan != null) return@withContext

        // 1. Users
        val users = listOf(
            UserEntity(
                id = "user_f1",
                name = "Ramesh Gupta",
                email = "ramesh@rameshfinance.com",
                phone = "+91 98401 23456",
                role = UserRole.FINANCIER,
                businessName = "Ramesh Financial Services",
                serviceArea = "Bangalore, Chennai & South Region",
                verificationStatus = VerificationStatus.VERIFIED,
                address = "142 MG Road, Bangalore 560001",
                minLoanAmount = 10000.0,
                maxLoanAmount = 1000000.0,
                standardInterestRate = 10.0,
                standardInterestType = InterestType.MONTHLY,
                supportedMethods = "UPI, Bank Transfer, Card, Cash",
                languages = "English, Tamil, Kannada, Hindi"
            ),
            UserEntity(
                id = "user_f2",
                name = "Vikram Malhotra",
                email = "vikram@apexcapital.in",
                phone = "+91 98200 65432",
                role = UserRole.FINANCIER,
                businessName = "Apex Capital Lending",
                serviceArea = "Mumbai, Delhi NCR & West Region",
                verificationStatus = VerificationStatus.VERIFIED,
                address = "Level 8, BKC Financial Tower, Mumbai 400051",
                minLoanAmount = 25000.0,
                maxLoanAmount = 2500000.0,
                standardInterestRate = 12.0,
                standardInterestType = InterestType.YEARLY,
                supportedMethods = "UPI, Bank Transfer, Net Banking",
                languages = "English, Hindi, Marathi"
            ),
            UserEntity(
                id = "user_f3",
                name = "Lakshmi Narayanan",
                email = "contact@pragathimicro.org",
                phone = "+91 97000 11223",
                role = UserRole.FINANCIER,
                businessName = "Pragathi Micro Credit",
                serviceArea = "Hyderabad, Vijayawada & AP/Telangana",
                verificationStatus = VerificationStatus.VERIFIED,
                address = "56 Jubilee Hills, Hyderabad 500033",
                minLoanAmount = 5000.0,
                maxLoanAmount = 200000.0,
                standardInterestRate = 9.0,
                standardInterestType = InterestType.MONTHLY,
                supportedMethods = "UPI, Cash, Bank Transfer",
                languages = "Telugu, English, Hindi"
            ),
            UserEntity(
                id = "user_b1",
                name = "Priya Sharma",
                email = "priya.sharma@gmail.com",
                phone = "+91 98765 43210",
                role = UserRole.BORROWER,
                address = "Flat 402, Green View Apartments, Indiranagar, Bangalore",
                serviceArea = "Bangalore"
            ),
            UserEntity(
                id = "user_b2",
                name = "Rahul Verma",
                email = "rahul.verma@outlook.com",
                phone = "+91 98111 88776",
                role = UserRole.BORROWER,
                address = "B-12 Lajpat Nagar, New Delhi 110024",
                serviceArea = "Delhi NCR"
            ),
            UserEntity(
                id = "user_b3",
                name = "Ananya Iyer",
                email = "ananya.iyer@proton.me",
                phone = "+91 94444 33221",
                role = UserRole.BORROWER,
                address = "24 Besant Nagar, Chennai 600090",
                serviceArea = "Chennai"
            ),
            UserEntity(
                id = "user_admin",
                name = "System Administrator",
                email = "admin@loanconnect.io",
                phone = "+91 80000 00001",
                role = UserRole.ADMIN,
                businessName = "LoanConnect Platform HQ",
                address = "Prestige Tech Park, Bangalore"
            )
        )
        dao.insertUsers(users)

        // 2. Loans
        val loans = listOf(
            LoanEntity(
                id = "LOAN-000101",
                financierId = "user_f1",
                borrowerId = "user_b1",
                financierName = "Ramesh Financial Services",
                borrowerName = "Priya Sharma",
                principalAmount = 100000.0,
                interestRate = 10.0,
                interestType = InterestType.MONTHLY,
                interestAmount = 10000.0,
                processingFee = 500.0,
                platformFee = 250.0,
                totalPayable = 110750.0,
                totalPaid = 60000.0,
                principalPaid = 54000.0,
                interestPaid = 6000.0,
                startDate = "2026-04-10",
                dueDate = "2027-04-10",
                nextDueDate = "2026-10-10",
                nextInstallmentAmount = 10000.0,
                durationMonths = 12,
                status = LoanStatus.ACTIVE,
                disbursementTxId = "DISB-TXN-887192",
                isAutoPayEnabled = true,
                purpose = "Working Capital & Inventory"
            ),
            LoanEntity(
                id = "LOAN-000102",
                financierId = "user_f2",
                borrowerId = "user_b1",
                financierName = "Apex Capital Lending",
                borrowerName = "Priya Sharma",
                principalAmount = 50000.0,
                interestRate = 12.0,
                interestType = InterestType.YEARLY,
                interestAmount = 6000.0,
                processingFee = 500.0,
                platformFee = 150.0,
                totalPayable = 56650.0,
                totalPaid = 15000.0,
                principalPaid = 13500.0,
                interestPaid = 1500.0,
                startDate = "2026-07-15",
                dueDate = "2027-07-15",
                nextDueDate = "2026-10-15",
                nextInstallmentAmount = 5000.0,
                durationMonths = 12,
                status = LoanStatus.ACTIVE,
                disbursementTxId = "DISB-TXN-552140",
                isAutoPayEnabled = false,
                purpose = "Home Renovation & Solar Setup"
            ),
            LoanEntity(
                id = "LOAN-000103",
                financierId = "user_f1",
                borrowerId = "user_b2",
                financierName = "Ramesh Financial Services",
                borrowerName = "Rahul Verma",
                principalAmount = 30000.0,
                interestRate = 10.0,
                interestType = InterestType.MONTHLY,
                interestAmount = 3000.0,
                processingFee = 300.0,
                platformFee = 100.0,
                totalPayable = 33400.0,
                totalPaid = 11000.0,
                principalPaid = 10000.0,
                interestPaid = 1000.0,
                startDate = "2026-06-25",
                dueDate = "2026-12-25",
                nextDueDate = "2026-09-25",
                nextInstallmentAmount = 5500.0,
                durationMonths = 6,
                status = LoanStatus.OVERDUE,
                disbursementTxId = "DISB-TXN-339901",
                isAutoPayEnabled = false,
                purpose = "Medical Emergency & Treatment"
            ),
            LoanEntity(
                id = "LOAN-000104",
                financierId = "user_f3",
                borrowerId = "user_b2",
                financierName = "Pragathi Micro Credit",
                borrowerName = "Rahul Verma",
                principalAmount = 20000.0,
                interestRate = 9.0,
                interestType = InterestType.MONTHLY,
                interestAmount = 1800.0,
                processingFee = 200.0,
                platformFee = 50.0,
                totalPayable = 22050.0,
                totalPaid = 22050.0,
                principalPaid = 20000.0,
                interestPaid = 1800.0,
                startDate = "2026-01-01",
                dueDate = "2026-07-01",
                nextDueDate = "2026-07-01",
                nextInstallmentAmount = 0.0,
                durationMonths = 6,
                status = LoanStatus.COMPLETED,
                disbursementTxId = "DISB-TXN-110099",
                isAutoPayEnabled = false,
                purpose = "Agricultural Equipment"
            )
        )
        dao.insertLoans(loans)

        // 3. Payment Schedules for LOAN-000101
        val schedules = listOf(
            PaymentScheduleEntity("SCH-101-1", "LOAN-000101", 1, 10000.0, 9000.0, 1000.0, "2026-05-10", ScheduleStatus.PAID, 10000.0, "2026-05-09"),
            PaymentScheduleEntity("SCH-101-2", "LOAN-000101", 2, 10000.0, 9000.0, 1000.0, "2026-06-10", ScheduleStatus.PAID, 10000.0, "2026-06-10"),
            PaymentScheduleEntity("SCH-101-3", "LOAN-000101", 3, 10000.0, 9000.0, 1000.0, "2026-07-10", ScheduleStatus.PAID, 10000.0, "2026-07-08"),
            PaymentScheduleEntity("SCH-101-4", "LOAN-000101", 4, 10000.0, 9000.0, 1000.0, "2026-08-10", ScheduleStatus.PAID, 10000.0, "2026-08-10"),
            PaymentScheduleEntity("SCH-101-5", "LOAN-000101", 5, 10000.0, 9000.0, 1000.0, "2026-09-10", ScheduleStatus.PAID, 10000.0, "2026-09-10"),
            PaymentScheduleEntity("SCH-101-6", "LOAN-000101", 6, 10000.0, 9000.0, 1000.0, "2026-09-28", ScheduleStatus.PAID, 10000.0, "2026-09-28"),
            PaymentScheduleEntity("SCH-101-7", "LOAN-000101", 7, 10000.0, 9000.0, 1000.0, "2026-10-10", ScheduleStatus.PENDING, 0.0, ""),
            PaymentScheduleEntity("SCH-101-8", "LOAN-000101", 8, 10000.0, 9000.0, 1000.0, "2026-11-10", ScheduleStatus.PENDING, 0.0, ""),
            PaymentScheduleEntity("SCH-101-9", "LOAN-000101", 9, 10000.0, 9000.0, 1000.0, "2026-12-10", ScheduleStatus.PENDING, 0.0, "")
        )
        dao.insertSchedules(schedules)

        // 4. Payments
        val payments = listOf(
            PaymentEntity(
                id = "PAY-9001",
                loanId = "LOAN-000101",
                borrowerId = "user_b1",
                financierId = "user_f1",
                borrowerName = "Priya Sharma",
                financierName = "Ramesh Financial Services",
                amount = 10000.0,
                principalPaid = 9000.0,
                interestPaid = 1000.0,
                paymentMethod = PaymentMethod.UPI,
                transactionId = "UPI-REF-992837190",
                status = PaymentStatus.SUCCESSFUL,
                paymentDate = System.currentTimeMillis() - 86400000L,
                receiptNumber = "REC-2026-0901"
            ),
            PaymentEntity(
                id = "PAY-9002",
                loanId = "LOAN-000101",
                borrowerId = "user_b1",
                financierId = "user_f1",
                borrowerName = "Priya Sharma",
                financierName = "Ramesh Financial Services",
                amount = 10000.0,
                principalPaid = 9000.0,
                interestPaid = 1000.0,
                paymentMethod = PaymentMethod.AUTO_PAY,
                transactionId = "AP-REF-887162541",
                status = PaymentStatus.SUCCESSFUL,
                paymentDate = System.currentTimeMillis() - (86400000L * 20),
                receiptNumber = "REC-2026-0810"
            ),
            PaymentEntity(
                id = "PAY-9003",
                loanId = "LOAN-000102",
                borrowerId = "user_b1",
                financierId = "user_f2",
                borrowerName = "Priya Sharma",
                financierName = "Apex Capital Lending",
                amount = 15000.0,
                principalPaid = 13500.0,
                interestPaid = 1500.0,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                transactionId = "NEFT-AXIS-776152",
                status = PaymentStatus.SUCCESSFUL,
                paymentDate = System.currentTimeMillis() - (86400000L * 15),
                receiptNumber = "REC-2026-0715"
            )
        )
        dao.insertPayments(payments)

        // 5. Loan Requests
        val requests = listOf(
            LoanRequestEntity(
                id = "REQ-1001",
                borrowerId = "user_b3",
                financierId = "user_f1",
                borrowerName = "Ananya Iyer",
                financierName = "Ramesh Financial Services",
                requestedAmount = 50000.0,
                purpose = "Emergency Medical Expenses & Surgery",
                durationMonths = 12,
                paymentFrequency = "Monthly",
                message = "Need urgent funds for dental & hospital deposit. Can provide payslips.",
                status = RequestStatus.PENDING
            ),
            LoanRequestEntity(
                id = "REQ-1002",
                borrowerId = "user_b2",
                financierId = "user_f2",
                borrowerName = "Rahul Verma",
                financierName = "Apex Capital Lending",
                requestedAmount = 150000.0,
                purpose = "Retail Store Inventory Expansion for Festive Season",
                durationMonths = 18,
                paymentFrequency = "Monthly",
                message = "Expanding electronics shop inventory for Diwali rush.",
                status = RequestStatus.OFFER_SENT
            )
        )
        dao.insertRequests(requests)

        // 6. Loan Offers
        val offers = listOf(
            LoanOfferEntity(
                id = "OFF-2001",
                requestId = "REQ-1002",
                financierId = "user_f2",
                borrowerId = "user_b2",
                financierName = "Apex Capital Lending",
                borrowerName = "Rahul Verma",
                approvedAmount = 150000.0,
                interestRate = 11.5,
                interestType = InterestType.YEARLY,
                durationMonths = 18,
                paymentFrequency = "Monthly",
                installmentAmount = 9750.0,
                processingFee = 1500.0,
                platformFee = 350.0,
                totalPayable = 177350.0,
                expiryDate = "2026-10-15",
                termsText = "Disbursement within 2 hours of digital sign. Prepayment allowed with zero penalty after 6 months."
            )
        )
        dao.insertOffers(offers)

        // 7. AutoPay Mandates
        val mandates = listOf(
            AutoPayMandateEntity(
                id = "MAND-501",
                borrowerId = "user_b1",
                loanId = "LOAN-000101",
                financierId = "user_f1",
                amount = 10000.0,
                frequency = "Monthly",
                status = MandateStatus.ACTIVE,
                nextPaymentDate = "2026-10-10",
                providerReference = "NPCI-E-MANDATE-991209"
            )
        )
        dao.insertMandates(mandates)

        // 8. Commissions
        val commissions = listOf(
            CommissionEntity(
                id = "COMM-101",
                loanId = "LOAN-000101",
                paymentId = "PAY-9001",
                financierId = "user_f1",
                amount = 250.0,
                ratePercentage = 2.5,
                payer = "Borrower",
                recipient = "Platform"
            ),
            CommissionEntity(
                id = "COMM-102",
                loanId = "LOAN-000102",
                paymentId = "PAY-9003",
                financierId = "user_f2",
                amount = 375.0,
                ratePercentage = 2.5,
                payer = "Borrower",
                recipient = "Platform"
            )
        )
        dao.insertCommissions(commissions)

        // 9. Notifications
        val notifications = listOf(
            NotificationEntity(
                id = "NOTIF-1",
                userId = "user_b1",
                title = "Payment Received",
                message = "₹10,000 received for LOAN-000101. Remaining balance is ₹50,750.",
                type = "PAYMENT_SUCCESS"
            ),
            NotificationEntity(
                id = "NOTIF-2",
                userId = "user_f1",
                title = "New Loan Request",
                message = "Ananya Iyer requested ₹50,000 for Medical Expenses.",
                type = "LOAN_REQUEST"
            ),
            NotificationEntity(
                id = "NOTIF-3",
                userId = "user_f1",
                title = "Overdue Alert",
                message = "Rahul Verma has an overdue installment of ₹5,500 on LOAN-000103.",
                type = "OVERDUE"
            )
        )
        dao.insertNotifications(notifications)

        // 10. Support Tickets
        val tickets = listOf(
            SupportTicketEntity(
                id = "TCK-801",
                userId = "user_b1",
                userName = "Priya Sharma",
                category = "Auto Pay Setup Query",
                description = "Wanted to confirm if debit happens at 9 AM or 5 PM on due date.",
                loanId = "LOAN-000101",
                status = TicketStatus.RESOLVED,
                resolutionNote = "Informed user that Auto Pay triggers at 10:00 AM IST on the 10th of every month."
            )
        )
        dao.insertTickets(tickets)

        // 11. Audit Logs
        val logs = listOf(
            AuditLogEntity(
                id = "LOG-001",
                userId = "user_admin",
                userName = "System Administrator",
                action = "FINANCIER_VERIFIED",
                entityType = "USER",
                entityId = "user_f1",
                details = "Completed KYC background verification for Ramesh Financial Services."
            ),
            AuditLogEntity(
                id = "LOG-002",
                userId = "user_f1",
                userName = "Ramesh Gupta",
                action = "LOAN_DISBURSED",
                entityType = "LOAN",
                entityId = "LOAN-000101",
                details = "Disbursed ₹1,00,000 via IMPS to Priya Sharma."
            )
        )
        dao.insertAuditLogs(logs)

        // 12. Linked Bank Accounts
        val bankAccounts = listOf(
            BankAccountEntity(
                id = "BANK-001",
                userId = "user_b1",
                bankName = "HDFC Bank",
                accountNumber = "5010049281726",
                accountNumberLast4 = "1726",
                ifscCode = "HDFC0000240",
                accountHolderName = "Priya Sharma",
                accountType = "Savings",
                isVerified = true,
                isPrimary = true,
                upiId = "priyasharma@okhdfcbank"
            ),
            BankAccountEntity(
                id = "BANK-002",
                userId = "user_f1",
                bankName = "State Bank of India",
                accountNumber = "203948571928",
                accountNumberLast4 = "1928",
                ifscCode = "SBIN0004123",
                accountHolderName = "Ramesh Gupta",
                accountType = "Current",
                isVerified = true,
                isPrimary = true,
                upiId = "rameshfinance@oksbi"
            )
        )
        bankAccounts.forEach { dao.insertBankAccount(it) }

        // 13. Personal Finance - Expenses
        val expenses = listOf(
            ExpenseEntity(
                id = "EXP-101",
                userId = "user_b1",
                title = "Loan Repayment - Ramesh Financial",
                amount = 10500.0,
                category = "Loan EMI",
                paymentMethod = "Google Pay",
                bankAccountId = "BANK-001",
                date = "2026-09-10",
                notes = "September EMI for LOAN-000101 via Google Pay UPI"
            ),
            ExpenseEntity(
                id = "EXP-102",
                userId = "user_b1",
                title = "Monthly Grocery Stock (DMart)",
                amount = 4650.0,
                category = "Groceries",
                paymentMethod = "Connected Bank (HDFC)",
                bankAccountId = "BANK-001",
                date = "2026-09-12",
                notes = "Monthly essentials and pantry groceries"
            ),
            ExpenseEntity(
                id = "EXP-103",
                userId = "user_b1",
                title = "Electricity & BESCOM Utility Bill",
                amount = 2150.0,
                category = "Utilities",
                paymentMethod = "Google Pay",
                date = "2026-09-15",
                notes = "Auto-billed via Google Pay"
            ),
            ExpenseEntity(
                id = "EXP-104",
                userId = "user_b1",
                title = "Family Dinner at Bistro",
                amount = 1420.0,
                category = "Food & Dining",
                paymentMethod = "Google Pay",
                date = "2026-09-18",
                notes = "Weekend family dining"
            ),
            ExpenseEntity(
                id = "EXP-105",
                userId = "user_b1",
                title = "Fuel & Metro Card Recharge",
                amount = 1800.0,
                category = "Transportation",
                paymentMethod = "PhonePe",
                date = "2026-09-21",
                notes = "Commute expenses"
            ),
            ExpenseEntity(
                id = "EXP-106",
                userId = "user_b1",
                title = "Clothing & Festive Shopping",
                amount = 3200.0,
                category = "Shopping",
                paymentMethod = "Connected Bank (HDFC)",
                bankAccountId = "BANK-001",
                date = "2026-09-24",
                notes = "Festive seasonal apparel"
            ),
            ExpenseEntity(
                id = "EXP-107",
                userId = "user_b1",
                title = "Apollo Pharmacy Medicines",
                amount = 890.0,
                category = "Health",
                paymentMethod = "Paytm",
                date = "2026-09-27",
                notes = "Routine vitamins and prescription"
            )
        )
        dao.insertExpenses(expenses)

        // 14. Personal Finance - Category Budgets
        val currentMonth = "2026-09"
        val budgets = listOf(
            BudgetEntity(
                id = "BUD-user_b1_Overall_$currentMonth",
                userId = "user_b1",
                category = "Overall",
                monthlyLimit = 40000.0,
                month = currentMonth,
                alertThresholdPercent = 80.0
            ),
            BudgetEntity(
                id = "BUD-user_b1_Loan_EMI_$currentMonth",
                userId = "user_b1",
                category = "Loan EMI",
                monthlyLimit = 12000.0,
                month = currentMonth,
                alertThresholdPercent = 90.0
            ),
            BudgetEntity(
                id = "BUD-user_b1_Groceries_$currentMonth",
                userId = "user_b1",
                category = "Groceries",
                monthlyLimit = 6000.0,
                month = currentMonth,
                alertThresholdPercent = 80.0
            ),
            BudgetEntity(
                id = "BUD-user_b1_Food_Dining_$currentMonth",
                userId = "user_b1",
                category = "Food & Dining",
                monthlyLimit = 3500.0,
                month = currentMonth,
                alertThresholdPercent = 75.0
            ),
            BudgetEntity(
                id = "BUD-user_b1_Utilities_$currentMonth",
                userId = "user_b1",
                category = "Utilities",
                monthlyLimit = 3000.0,
                month = currentMonth,
                alertThresholdPercent = 80.0
            ),
            BudgetEntity(
                id = "BUD-user_b1_Transportation_$currentMonth",
                userId = "user_b1",
                category = "Transportation",
                monthlyLimit = 2500.0,
                month = currentMonth,
                alertThresholdPercent = 80.0
            ),
            BudgetEntity(
                id = "BUD-user_b1_Shopping_$currentMonth",
                userId = "user_b1",
                category = "Shopping",
                monthlyLimit = 4000.0,
                month = currentMonth,
                alertThresholdPercent = 80.0
            )
        )
        dao.insertBudgets(budgets)
    }
}

package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun userRoleValidation_isAccurate() {
    val roles = listOf(
        com.example.data.model.UserRole.BORROWER,
        com.example.data.model.UserRole.FINANCIER,
        com.example.data.model.UserRole.ADMIN
    )
    assertEquals(3, roles.size)
    assertTrue(roles.contains(com.example.data.model.UserRole.BORROWER))
    assertTrue(roles.contains(com.example.data.model.UserRole.FINANCIER))
    assertTrue(roles.contains(com.example.data.model.UserRole.ADMIN))
  }

  @Test
  fun expenseAndBudgetCalculation_isAccurate() {
    val expenses = listOf(10500.0, 4650.0, 2150.0, 1420.0, 1800.0, 3200.0, 890.0)
    val totalSpent = expenses.sum()
    val monthlyBudget = 40000.0
    val remaining = monthlyBudget - totalSpent
    val percentage = (totalSpent / monthlyBudget) * 100.0

    assertEquals(24610.0, totalSpent, 0.01)
    assertEquals(15390.0, remaining, 0.01)
    assertEquals(61.525, percentage, 0.01)
    assertTrue("Budget not exceeded", totalSpent <= monthlyBudget)
  }

  @Test
  fun loanEntityCreation_withRequestedAmountDurationAndPurpose_isAccurate() {
    val loan = com.example.data.model.LoanEntity(
        id = "LOAN-TEST01",
        financierId = "user_f1",
        borrowerId = "user_b1",
        financierName = "Ramesh Financial Services",
        borrowerName = "Priya Sharma",
        principalAmount = 75000.0,
        interestRate = 10.0,
        interestType = com.example.data.model.InterestType.MONTHLY,
        interestAmount = 7500.0,
        processingFee = 750.0,
        platformFee = 150.0,
        totalPayable = 83400.0,
        startDate = "2026-10-01",
        dueDate = "2027-10-01",
        nextDueDate = "2026-11-01",
        nextInstallmentAmount = 6950.0,
        durationMonths = 12,
        purpose = "Business Expansion & Working Capital"
    )

    assertEquals("LOAN-TEST01", loan.id)
    assertEquals(75000.0, loan.principalAmount, 0.01)
    assertEquals(12, loan.durationMonths)
    assertEquals("Business Expansion & Working Capital", loan.purpose)
    assertEquals(com.example.data.model.LoanStatus.ACTIVE, loan.status)
    assertEquals(83400.0, loan.totalPayable, 0.01)
  }
}

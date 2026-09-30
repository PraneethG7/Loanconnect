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
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
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
}

package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.InterestType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("LoanConnect", appName)
  }

  @Test
  fun `verify loan interest calculation formula`() {
    val principal = 100000.0
    val monthlyRate = 10.0 // 10%
    val interest = principal * (monthlyRate / 100.0)
    val fees = 500.0
    val platformCharges = 250.0
    val totalPayable = principal + interest + fees + platformCharges

    assertEquals(10000.0, interest, 0.01)
    assertEquals(110750.0, totalPayable, 0.01)
  }
}

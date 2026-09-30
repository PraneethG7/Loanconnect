package com.example.data.ai

import android.content.Context
import com.example.BuildConfig
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class LoanConnectAiService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun queryAi(
        prompt: String,
        currentUser: UserEntity,
        loans: List<LoanEntity>,
        payments: List<PaymentEntity>,
        language: String = "English"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        // Prepare context data isolated for this user
        val summaryContext = buildUserFinancialContext(currentUser, loans, payments)

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                return@withContext callGeminiRest(prompt, summaryContext, language, apiKey)
            } catch (e: Exception) {
                // Graceful fallback to smart financial rules engine
            }
        }

        return@withContext generateSmartFinancialResponse(prompt, currentUser, loans, payments, language)
    }

    private fun buildUserFinancialContext(
        user: UserEntity,
        loans: List<LoanEntity>,
        payments: List<PaymentEntity>
    ): String {
        return buildString {
            append("User: ${user.name} (${user.role.name})\n")
            if (user.role == UserRole.FINANCIER) {
                append("Financier Business: ${user.businessName}\n")
                append("Total Loans Managed: ${loans.size}\n")
                val totalLent = loans.sumOf { it.principalAmount }
                val totalCollected = loans.sumOf { it.totalPaid }
                val totalOutstanding = loans.sumOf { maxOf(0.0, it.totalPayable - it.totalPaid) }
                val overdueLoans = loans.filter { it.status == LoanStatus.OVERDUE }
                append("Total Lent: ₹$totalLent, Collected: ₹$totalCollected, Outstanding: ₹$totalOutstanding\n")
                append("Overdue Count: ${overdueLoans.size}\n")
                overdueLoans.forEach {
                    append(" - Borrower ${it.borrowerName}, Loan ${it.id}, Due: ₹${it.nextInstallmentAmount}, DueDate: ${it.nextDueDate}\n")
                }
            } else {
                append("Active Loans: ${loans.size}\n")
                val totalPayable = loans.sumOf { it.totalPayable }
                val totalPaid = loans.sumOf { it.totalPaid }
                val remainingOwed = loans.sumOf { maxOf(0.0, it.totalPayable - it.totalPaid) }
                append("Total Payable: ₹$totalPayable, Paid: ₹$totalPaid, Remaining Owed: ₹$remainingOwed\n")
                loans.forEach {
                    append(" - Loan ${it.id} from ${it.financierName}: Remaining ₹${it.totalPayable - it.totalPaid}, Next Due: ${it.nextDueDate} (₹${it.nextInstallmentAmount})\n")
                }
            }
        }
    }

    private fun callGeminiRest(
        prompt: String,
        contextData: String,
        language: String,
        apiKey: String
    ): String {
        val systemPrompt = """
            You are LoanConnect AI, an intelligent, empathetic, and compliant fintech financial assistant.
            The user is speaking or typing in: $language.
            Respond accurately in $language (or translate if requested).
            Use only the authorized context data provided below. Never make up loans or disclose other users' data.
            Keep responses concise, clear, and formatted for mobile screens.
            If the user asks to execute a payment or disburse funds, remind them that transactions require manual authorization on the payment screen.
            
            [AUTHORIZED CONTEXT]
            $contextData
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", "$systemPrompt\n\nUser Question: $prompt"))
                    })
                })
            })
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("API error: ${response.code}")
            }
            val resStr = response.body?.string() ?: ""
            val resJson = JSONObject(resStr)
            val candidates = resJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")
            return text ?: "I am ready to help you with your loans."
        }
    }

    private fun generateSmartFinancialResponse(
        prompt: String,
        user: UserEntity,
        loans: List<LoanEntity>,
        payments: List<PaymentEntity>,
        language: String
    ): String {
        val p = prompt.lowercase()

        if (user.role == UserRole.FINANCIER) {
            return when {
                p.contains("overdue") || p.contains("not paid") || p.contains("who has not paid") -> {
                    val overdue = loans.filter { it.status == LoanStatus.OVERDUE }
                    if (overdue.isEmpty()) {
                        "All your borrowers are currently up to date! There are zero overdue loans."
                    } else {
                        val names = overdue.joinToString(", ") { "${it.borrowerName} (${it.id}, ₹${String.format("%,.0f", it.nextInstallmentAmount)} due ${it.nextDueDate})" }
                        "You have ${overdue.size} overdue loan(s):\n$names."
                    }
                }
                p.contains("collect") || p.contains("today") -> {
                    val todayPayments = payments.filter {
                        val now = System.currentTimeMillis()
                        it.paymentDate >= now - 86400000L
                    }
                    val totalToday = todayPayments.sumOf { it.amount }
                    "Today's total collection is ₹${String.format("%,.0f", totalToday)} across ${todayPayments.size} transaction(s)."
                }
                p.contains("commission") || p.contains("earn") -> {
                    val totalCommission = loans.sumOf { it.totalPaid * 0.025 }
                    val interestEarned = loans.sumOf { it.interestPaid }
                    "Your financial summary: Total interest received is ₹${String.format("%,.0f", interestEarned)}, and platform commission recorded is ₹${String.format("%,.0f", totalCommission)}."
                }
                p.contains("outstanding") || p.contains("lent") -> {
                    val lent = loans.sumOf { it.principalAmount }
                    val outstanding = loans.sumOf { maxOf(0.0, it.totalPayable - it.totalPaid) }
                    "Total capital lent across all active loans is ₹${String.format("%,.0f", lent)}. Current outstanding balance is ₹${String.format("%,.0f", outstanding)}."
                }
                else -> {
                    "Hello ${user.name}! I am your LoanConnect Financier Assistant. You have ${loans.size} active loan accounts. You can ask me: 'Who has not paid?', 'Show overdue loans', 'How much did I collect today?', or 'Show earnings'."
                }
            }
        } else {
            // Borrower
            return when {
                p.contains("owe") || p.contains("remaining") || p.contains("balance") -> {
                    val totalRemaining = loans.sumOf { maxOf(0.0, it.totalPayable - it.totalPaid) }
                    "Your total outstanding balance across ${loans.size} loan(s) is ₹${String.format("%,.0f", totalRemaining)}."
                }
                p.contains("next payment") || p.contains("due date") || p.contains("when") -> {
                    val active = loans.filter { it.status == LoanStatus.ACTIVE || it.status == LoanStatus.OVERDUE }
                        .sortedBy { it.nextDueDate }
                        .firstOrNull()
                    if (active != null) {
                        "Your next installment is ₹${String.format("%,.0f", active.nextInstallmentAmount)} for ${active.id} (${active.financierName}) due on ${active.nextDueDate}."
                    } else {
                        "You have no upcoming installments due at this moment."
                    }
                }
                p.contains("paid") || p.contains("history") -> {
                    val totalPaid = loans.sumOf { it.totalPaid }
                    "You have successfully paid a total of ₹${String.format("%,.0f", totalPaid)} across your loans."
                }
                p.contains("autopay") || p.contains("auto pay") -> {
                    "You can enable Auto Pay directly from your active loan card using NPCI e-Mandate. Installments are debited automatically on the due date."
                }
                p.contains("explain") || p.contains("terms") || p.contains("agreement") -> {
                    val loan = loans.firstOrNull()
                    if (loan != null) {
                        "Your loan ${loan.id} with ${loan.financierName} has a principal of ₹${String.format("%,.0f", loan.principalAmount)} at ${loan.interestRate}% ${loan.interestType.name.lowercase()} interest. Total payable is ₹${String.format("%,.0f", loan.totalPayable)}. You have paid ₹${String.format("%,.0f", loan.totalPaid)} so far."
                    } else {
                        "LoanConnect offers transparent loans with disclosed interest rates, zero hidden charges, and flexible repayment terms."
                    }
                }
                else -> {
                    "Hello ${user.name}! I am your LoanConnect Assistant. You can ask: 'How much do I still owe?', 'When is my next payment?', 'How much have I paid?', or 'Explain my loan'."
                }
            }
        }
    }
}

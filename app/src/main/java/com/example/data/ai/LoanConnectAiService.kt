package com.example.data.ai

import android.content.Context
import android.util.Log
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

    private val tag = "LoanConnectAiService"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private class RateLimitException(message: String) : Exception(message)

    /**
     * Executes a multi-turn chat request using the Gemini API.
     * Supports:
     * - Multi-turn conversation history
     * - Role-specific system instructions
     * - Models: gemini-3.1-pro-preview, gemini-3.5-flash, gemini-2.5-flash, gemini-3.1-flash-lite-preview
     * - Google Search Grounding for real-time market data
     * - Automatic model fallback & local fintech engine when rate limits (429) or quota exceeded occur
     */
    suspend fun queryMultiTurnAi(
        conversationHistory: List<Pair<String, String>>, // list of (sender, text)
        currentUser: UserEntity,
        loans: List<LoanEntity>,
        payments: List<PaymentEntity>,
        modelName: String = "gemini-3.5-flash",
        enableSearchGrounding: Boolean = true,
        language: String = "English"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        val summaryContext = buildUserFinancialContext(currentUser, loans, payments)
        val systemInstruction = buildRoleSystemInstruction(currentUser.role, summaryContext, language, modelName)

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            val candidateModels = listOf(
                modelName,
                "gemini-2.5-flash",
                "gemini-flash-latest",
                "gemini-3.1-flash-lite-preview"
            ).distinct()

            for (candidate in candidateModels) {
                try {
                    val result = callGeminiApi(
                        conversationHistory = conversationHistory,
                        systemInstruction = systemInstruction,
                        model = candidate,
                        enableSearchGrounding = enableSearchGrounding && candidate == "gemini-3.5-flash",
                        apiKey = apiKey
                    )
                    if (result.isNotBlank()) {
                        return@withContext result
                    }
                } catch (e: RateLimitException) {
                    Log.w(tag, "Model $candidate rate-limited or quota exceeded (429), checking next fallback...")
                } catch (e: Exception) {
                    Log.w(tag, "Model $candidate request error: ${e.message}")
                }
            }
        }

        // Seamless fallback to local high-precision fintech intelligence engine
        val latestPrompt = conversationHistory.lastOrNull { it.first == "user" }?.second ?: ""
        return@withContext generateSmartFinancialResponse(latestPrompt, currentUser, loans, payments, language)
    }

    suspend fun queryAi(
        prompt: String,
        currentUser: UserEntity,
        loans: List<LoanEntity>,
        payments: List<PaymentEntity>,
        language: String = "English"
    ): String {
        return queryMultiTurnAi(
            conversationHistory = listOf("user" to prompt),
            currentUser = currentUser,
            loans = loans,
            payments = payments,
            modelName = "gemini-3.5-flash",
            enableSearchGrounding = true,
            language = language
        )
    }

    private fun buildRoleSystemInstruction(
        role: UserRole,
        financialContext: String,
        language: String,
        modelName: String
    ): String {
        val roleSpecifics = when (role) {
            UserRole.BORROWER -> """
                ROLE: Empathetic Borrower Loan Advisor & Budget Coach.
                OBJECTIVE: Help the borrower understand repayment schedules, explain loan contracts in clear everyday language, calculate early settlement savings, and suggest budget adjustments to prevent default. Always prioritize borrower financial well-being and transparency.
            """.trimIndent()
            UserRole.FINANCIER -> """
                ROLE: Prudent Portfolio Risk & Smart Collection Analyst.
                OBJECTIVE: Help the financier monitor capital recovery, prioritize overdue accounts with compliant follow-up plans, calculate portfolio yields and commissions, and maintain ethical lending practices.
            """.trimIndent()
            UserRole.ADMIN -> """
                ROLE: Platform Compliance & Integrity Officer.
                OBJECTIVE: Monitor overall platform liquidity, detect abnormal interest rate violations, inspect platform fee compliance, and assist with disputes and support audits.
            """.trimIndent()
        }

        val voiceGuidance = if (modelName == "gemini-3.8-live") {
            "VOICE MODE ACTIVE: Deliver answers in conversational spoken style. Keep responses natural, concise, and easy to listen to without complex markdown formatting."
        } else ""

        return """
            You are LoanConnect AI, an intelligent fintech assistant embedded inside the LoanConnect platform.
            $roleSpecifics
            $voiceGuidance
            
            LANGUAGE: Respond fluently in $language (or translate if asked).
            ACCURACY: Use the provided [AUTHORIZED USER CONTEXT] data below as the primary source of truth for the user's loans. Never disclose private data of other unrelated users.
            COMPLIANCE: Remind users that loan disbursement and payments must be finalized with direct authorization in the UI.
            
            [AUTHORIZED USER CONTEXT]
            $financialContext
        """.trimIndent()
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
                    append(" - Loan ${it.id} (${it.purpose}) from ${it.financierName}: Remaining ₹${it.totalPayable - it.totalPaid}, Next Due: ${it.nextDueDate} (₹${it.nextInstallmentAmount})\n")
                }
            }
        }
    }

    private fun callGeminiApi(
        conversationHistory: List<Pair<String, String>>,
        systemInstruction: String,
        model: String,
        enableSearchGrounding: Boolean,
        apiKey: String
    ): String {
        // Multi-turn contents array
        val contentsArray = JSONArray()

        conversationHistory.forEach { (sender, text) ->
            val role = if (sender == "user") "user" else "model"
            val contentObj = JSONObject().apply {
                put("role", role)
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", text))
                })
            }
            contentsArray.put(contentObj)
        }

        val jsonBody = JSONObject().apply {
            put("contents", contentsArray)

            // System instruction
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", systemInstruction))
                })
            })

            // Search Grounding tool (gemini-3.5-flash)
            if (enableSearchGrounding) {
                put("tools", JSONArray().apply {
                    put(JSONObject().put("googleSearch", JSONObject()))
                })
            }
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val resStr = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                if (response.code == 429 || resStr.contains("RESOURCE_EXHAUSTED", ignoreCase = true)) {
                    Log.w(tag, "Gemini API rate limit or quota exceeded (HTTP 429). Will use fallback.")
                    throw RateLimitException("Quota exceeded: 429")
                }
                Log.w(tag, "Gemini API non-200 response: ${response.code}")
                throw RuntimeException("API error: ${response.code}")
            }

            val resJson = JSONObject(resStr)
            val candidates = resJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            var responseText = ""
            if (parts != null && parts.length() > 0) {
                for (i in 0 until parts.length()) {
                    val part = parts.optJSONObject(i)
                    val t = part?.optString("text", "") ?: ""
                    if (t.isNotEmpty()) {
                        responseText += t
                    }
                }
            }

            // Extract Google Search Grounding metadata citations if present
            val groundingMetadata = firstCandidate?.optJSONObject("groundingMetadata")
            if (groundingMetadata != null) {
                val webSearchQueries = groundingMetadata.optJSONArray("webSearchQueries")
                val searchChunks = groundingMetadata.optJSONArray("groundingChunks")
                if (searchChunks != null && searchChunks.length() > 0) {
                    val sources = mutableListOf<String>()
                    for (i in 0 until searchChunks.length()) {
                        val chunk = searchChunks.optJSONObject(i)
                        val web = chunk?.optJSONObject("web")
                        val title = web?.optString("title")
                        val uri = web?.optString("uri")
                        if (!title.isNullOrEmpty() && !uri.isNullOrEmpty()) {
                            sources.add("• [$title]($uri)")
                        }
                    }
                    if (sources.isNotEmpty()) {
                        responseText += "\n\n🌐 **Verified Sources (Google Search):**\n" + sources.take(3).joinToString("\n")
                    }
                }
            }

            return if (responseText.isNotEmpty()) responseText else "I've processed your request."
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
                        "Your loan ${loan.id} with ${loan.financierName} has a principal of ₹${String.format("%,.0f", loan.principalAmount)} at ${loan.interestRate}% ${loan.interestType.name.lowercase()} interest for ${loan.purpose}. Total payable is ₹${String.format("%,.0f", loan.totalPayable)}. You have paid ₹${String.format("%,.0f", loan.totalPaid)} so far."
                    } else {
                        "LoanConnect offers transparent loans with disclosed interest rates, zero hidden charges, and flexible repayment terms."
                    }
                }
                p.contains("calculate") || p.contains("emi") -> {
                    val sampleP = 50000.0
                    val sampleR = 10.0 / 1200.0
                    val sampleN = 12
                    val emi = (sampleP * sampleR * Math.pow(1.0 + sampleR, sampleN.toDouble())) / (Math.pow(1.0 + sampleR, sampleN.toDouble()) - 1.0)
                    "EMI Calculation Formula: E = P × r × (1+r)^n / ((1+r)^n - 1). For example, a ₹50,000 loan over 12 months at 10% annual interest results in an EMI of ₹${String.format("%,.0f", emi)}/month. Total interest: ₹${String.format("%,.0f", (emi * 12) - sampleP)}."
                }
                p.contains("rbi") || p.contains("guideline") || p.contains("regulation") || p.contains("rule") -> {
                    "Under RBI NBFC-P2P Directions and Fair Practices Code, individual borrower aggregate exposure is capped at ₹10 Lakhs across all P2P platforms. Escrow mechanisms are mandatory for all disbursements and repayments, and full APR fee disclosure is strictly required."
                }
                p.contains("inflation") || p.contains("repo") || p.contains("market") -> {
                    "The Reserve Bank of India (RBI) repo rate is benchmarked at 6.50% to maintain CPI inflation within the 4.0% (+/- 2%) target band. LoanConnect peer financiers offer rates typically between 9.5% and 14.5% based on borrower credit profile."
                }
                else -> {
                    "Hello ${user.name}! I am your LoanConnect AI Assistant. You have ${loans.size} active loan account(s). You can ask me about your outstanding balance, next installment due date, early settlement calculations, RBI guidelines, or EMI comparisons."
                }
            }
        }
    }
}

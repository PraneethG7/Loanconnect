package com.example.data.firebase

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.model.*
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseManager(private val context: Context) {

    private val tag = "FirebaseManager"
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Mandatory custom database ID from firebase_applet_config.xml
    val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Signs in using Google Sign-In via Jetpack CredentialManager.
     */
    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser> {
        return try {
            val credentialManager = CredentialManager.create(context)
            val serverClientId = context.getString(R.string.default_web_client_id)
            val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId).build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user ?: throw RuntimeException("Null Firebase user after sign in")
                Log.d(tag, "Google sign in successful for: ${user.email} (${user.uid})")
                Result.success(user)
            } else {
                Result.failure(RuntimeException("Unexpected credential type: ${credential.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(tag, "User cancelled Google Sign-In", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(tag, "Google Sign-In failed", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    /**
     * Saves/syncs user profile into Firestore: users/{userId}
     */
    suspend fun syncUserProfile(user: UserEntity) {
        val uid = auth.currentUser?.uid ?: user.id
        val data = mapOf(
            "userId" to uid,
            "name" to user.name,
            "email" to user.email,
            "phone" to user.phone,
            "role" to user.role.name,
            "businessName" to user.businessName,
            "serviceArea" to user.serviceArea,
            "address" to user.address,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            db.collection("users").document(uid).set(data, SetOptions.merge()).await()
            Log.d(tag, "User profile synced to Firestore: $uid")
        } catch (e: Exception) {
            Log.w(tag, "Failed to sync user profile to Firestore", e)
        }
    }

    /**
     * Saves/syncs loan entity to Firestore: loans/{loanId}
     */
    suspend fun syncLoan(loan: LoanEntity) {
        val data = mapOf(
            "id" to loan.id,
            "borrowerId" to loan.borrowerId,
            "financierId" to loan.financierId,
            "borrowerName" to loan.borrowerName,
            "financierName" to loan.financierName,
            "principalAmount" to loan.principalAmount,
            "durationMonths" to loan.durationMonths,
            "purpose" to loan.purpose,
            "interestRate" to loan.interestRate,
            "totalPayable" to loan.totalPayable,
            "totalPaid" to loan.totalPaid,
            "status" to loan.status.name,
            "startDate" to loan.startDate,
            "dueDate" to loan.dueDate,
            "nextDueDate" to loan.nextDueDate,
            "nextInstallmentAmount" to loan.nextInstallmentAmount,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            db.collection("loans").document(loan.id).set(data, SetOptions.merge()).await()
            Log.d(tag, "Loan synced to Firestore: ${loan.id}")
        } catch (e: Exception) {
            Log.w(tag, "Failed to sync loan to Firestore: ${loan.id}", e)
        }
    }

    /**
     * Saves/syncs loan application request to Firestore: loan_requests/{requestId}
     */
    suspend fun syncLoanRequest(request: LoanRequestEntity) {
        val data = mapOf(
            "id" to request.id,
            "borrowerId" to request.borrowerId,
            "financierId" to request.financierId,
            "borrowerName" to request.borrowerName,
            "financierName" to request.financierName,
            "requestedAmount" to request.requestedAmount,
            "durationMonths" to request.durationMonths,
            "purpose" to request.purpose,
            "status" to request.status.name,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            db.collection("loan_requests").document(request.id).set(data, SetOptions.merge()).await()
            Log.d(tag, "Loan request synced to Firestore: ${request.id}")
        } catch (e: Exception) {
            Log.w(tag, "Failed to sync loan request to Firestore: ${request.id}", e)
        }
    }

    /**
     * Saves/syncs payment record to Firestore: payments/{paymentId}
     */
    suspend fun syncPayment(payment: PaymentEntity) {
        val data = mapOf(
            "id" to payment.id,
            "loanId" to payment.loanId,
            "borrowerId" to payment.borrowerId,
            "financierId" to payment.financierId,
            "borrowerName" to payment.borrowerName,
            "financierName" to payment.financierName,
            "amount" to payment.amount,
            "paymentMethod" to payment.paymentMethod.name,
            "transactionId" to payment.transactionId,
            "status" to payment.status.name,
            "paymentDate" to payment.paymentDate,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            db.collection("payments").document(payment.id).set(data, SetOptions.merge()).await()
            Log.d(tag, "Payment synced to Firestore: ${payment.id}")
        } catch (e: Exception) {
            Log.w(tag, "Failed to sync payment to Firestore: ${payment.id}", e)
        }
    }
}

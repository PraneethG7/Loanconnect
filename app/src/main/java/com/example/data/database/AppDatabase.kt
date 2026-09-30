package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.LoanConnectDao
import com.example.data.model.*

@Database(
    entities = [
        UserEntity::class,
        LoanEntity::class,
        LoanRequestEntity::class,
        LoanOfferEntity::class,
        PaymentEntity::class,
        PaymentScheduleEntity::class,
        AutoPayMandateEntity::class,
        CommissionEntity::class,
        NotificationEntity::class,
        SupportTicketEntity::class,
        UserReportEntity::class,
        BlockedUserEntity::class,
        AuditLogEntity::class,
        BankAccountEntity::class,
        ExpenseEntity::class,
        BudgetEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun loanConnectDao(): LoanConnectDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "loan_connect_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.example.calculatorpro.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.DatabaseConfiguration
import androidx.room.RoomDatabase
import com.example.calculatorpro.data.model.BudgetLedgerEntity
import com.example.calculatorpro.data.model.CurrencyRateEntity
import com.example.calculatorpro.data.model.HistoryEntity

@Database(
    entities = [
        HistoryEntity::class,
        CurrencyRateEntity::class,
        BudgetLedgerEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CalculatorDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: CalculatorDatabase? = null

        fun getDatabase(context: Context): CalculatorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CalculatorDatabase::class.java,
                    "calculator_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

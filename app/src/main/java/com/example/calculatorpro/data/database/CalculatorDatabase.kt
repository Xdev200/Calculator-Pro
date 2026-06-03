package com.example.calculatorpro.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.calculatorpro.data.model.BudgetLedgerEntity
import com.example.calculatorpro.data.model.CurrencyRateEntity
import com.example.calculatorpro.data.model.HistoryEntity
import com.example.calculatorpro.data.model.WidgetSettingsEntity
import com.example.calculatorpro.data.model.BudgetHistoryEntity

@Database(
    entities = [
        HistoryEntity::class,
        CurrencyRateEntity::class,
        BudgetLedgerEntity::class,
        WidgetSettingsEntity::class,
        BudgetHistoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class CalculatorDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun budgetDao(): BudgetDao
    abstract fun widgetSettingsDao(): WidgetSettingsDao
    abstract fun budgetHistoryDao(): BudgetHistoryDao


    companion object {
        @Volatile
        private var INSTANCE: CalculatorDatabase? = null

        /**
         * Migration from v1 to v2:
         * - Removes price calculator columns (priceUnit, pricePerUnit, amountPurchased)
         * - Adds currencyPairs column for configurable currency snapshot display
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // SQLite doesn't support DROP COLUMN directly, so we recreate the table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS widget_settings_table_new (
                        id INTEGER NOT NULL PRIMARY KEY,
                        conversionCategory TEXT NOT NULL DEFAULT 'Mass',
                        conversionFromUnit TEXT NOT NULL DEFAULT 'Pounds',
                        conversionToUnit TEXT NOT NULL DEFAULT 'Kilograms',
                        currencyPairs TEXT NOT NULL DEFAULT 'INR,EUR,GBP'
                    )
                """.trimIndent())

                database.execSQL("""
                    INSERT INTO widget_settings_table_new (id, conversionCategory, conversionFromUnit, conversionToUnit)
                    SELECT id, conversionCategory, conversionFromUnit, conversionToUnit
                    FROM widget_settings_table
                """.trimIndent())

                database.execSQL("DROP TABLE widget_settings_table")
                database.execSQL("ALTER TABLE widget_settings_table_new RENAME TO widget_settings_table")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS budget_history_table (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        timestamp INTEGER NOT NULL,
                        amountDeducted REAL NOT NULL,
                        note TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): CalculatorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CalculatorDatabase::class.java,
                    "calculator_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

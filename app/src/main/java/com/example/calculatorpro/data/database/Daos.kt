package com.example.calculatorpro.data.database

import androidx.room.*
import com.example.calculatorpro.data.model.BudgetLedgerEntity
import com.example.calculatorpro.data.model.CurrencyRateEntity
import com.example.calculatorpro.data.model.HistoryEntity
import com.example.calculatorpro.data.model.WidgetSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_table ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: HistoryEntity)

    @Query("DELETE FROM history_table")
    suspend fun clearHistory()
}

@Dao
interface CurrencyDao {
    @Query("SELECT * FROM currency_rates_table")
    fun getAllRatesFlow(): Flow<List<CurrencyRateEntity>>

    @Query("SELECT * FROM currency_rates_table WHERE currencyCode = :code LIMIT 1")
    suspend fun getRate(code: String): CurrencyRateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRates(rates: List<CurrencyRateEntity>)

    @Query("DELETE FROM currency_rates_table")
    suspend fun clearRates()
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget_ledger_table WHERE id = 1 LIMIT 1")
    fun getBudgetFlow(): Flow<BudgetLedgerEntity?>

    @Query("SELECT * FROM budget_ledger_table WHERE id = 1 LIMIT 1")
    suspend fun getBudget(): BudgetLedgerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: BudgetLedgerEntity)
}

@Dao
interface WidgetSettingsDao {
    @Query("SELECT * FROM widget_settings_table WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<WidgetSettingsEntity?>

    @Query("SELECT * FROM widget_settings_table WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): WidgetSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: WidgetSettingsEntity)
}

@Dao
interface BudgetHistoryDao {
    @Query("SELECT * FROM budget_history_table ORDER BY timestamp DESC")
    fun getAllBudgetHistory(): Flow<List<com.example.calculatorpro.data.model.BudgetHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetHistory(item: com.example.calculatorpro.data.model.BudgetHistoryEntity)

    @Update
    suspend fun updateBudgetHistory(item: com.example.calculatorpro.data.model.BudgetHistoryEntity)

    @Delete
    suspend fun deleteBudgetHistory(item: com.example.calculatorpro.data.model.BudgetHistoryEntity)
}

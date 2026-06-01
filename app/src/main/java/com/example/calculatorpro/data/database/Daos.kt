package com.example.calculatorpro.data.database

import androidx.room.*
import com.example.calculatorpro.data.model.BudgetLedgerEntity
import com.example.calculatorpro.data.model.CurrencyRateEntity
import com.example.calculatorpro.data.model.HistoryEntity
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

package com.example.calculatorpro.domain.repository

import com.example.calculatorpro.data.model.BudgetLedgerEntity
import com.example.calculatorpro.data.model.CurrencyRateEntity
import com.example.calculatorpro.data.model.HistoryEntity
import kotlinx.coroutines.flow.Flow

interface CalculatorRepository {
    fun getAllHistory(): Flow<List<HistoryEntity>>
    suspend fun insertHistory(item: HistoryEntity)
    suspend fun clearHistory()

    fun getAllRatesFlow(): Flow<List<CurrencyRateEntity>>
    suspend fun getRate(code: String): CurrencyRateEntity?
    suspend fun insertRates(rates: List<CurrencyRateEntity>)
    suspend fun fetchAndSaveRates(): Boolean

    fun getBudgetFlow(): Flow<BudgetLedgerEntity?>
    suspend fun getBudget(): BudgetLedgerEntity?
    suspend fun updateBudget(budget: BudgetLedgerEntity)
}

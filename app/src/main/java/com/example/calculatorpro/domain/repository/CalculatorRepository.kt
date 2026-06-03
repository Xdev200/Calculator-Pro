package com.example.calculatorpro.domain.repository

import com.example.calculatorpro.data.model.BudgetLedgerEntity
import com.example.calculatorpro.data.model.CurrencyRateEntity
import com.example.calculatorpro.data.model.HistoryEntity
import com.example.calculatorpro.data.model.WidgetSettingsEntity
import com.example.calculatorpro.data.model.BudgetHistoryEntity
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

    fun getWidgetSettingsFlow(): Flow<WidgetSettingsEntity?>
    suspend fun getWidgetSettings(): WidgetSettingsEntity?
    suspend fun updateWidgetSettings(settings: WidgetSettingsEntity)

    fun getAllBudgetHistory(): Flow<List<BudgetHistoryEntity>>
    suspend fun insertBudgetHistory(item: BudgetHistoryEntity)
    suspend fun updateBudgetHistory(item: BudgetHistoryEntity)
    suspend fun deleteBudgetHistory(item: BudgetHistoryEntity)
}


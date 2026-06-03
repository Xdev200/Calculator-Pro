package com.example.calculatorpro.data.repository

import com.example.calculatorpro.data.database.BudgetDao
import com.example.calculatorpro.data.database.CurrencyDao
import com.example.calculatorpro.data.database.HistoryDao
import com.example.calculatorpro.data.database.WidgetSettingsDao
import com.example.calculatorpro.data.database.BudgetHistoryDao
import com.example.calculatorpro.data.model.BudgetLedgerEntity
import com.example.calculatorpro.data.model.CurrencyRateEntity
import com.example.calculatorpro.data.model.HistoryEntity
import com.example.calculatorpro.data.model.WidgetSettingsEntity
import com.example.calculatorpro.data.model.BudgetHistoryEntity
import com.example.calculatorpro.data.network.CurrencyApi
import com.example.calculatorpro.domain.repository.CalculatorRepository
import kotlinx.coroutines.flow.Flow

class CalculatorRepositoryImpl(
    private val historyDao: HistoryDao,
    private val currencyDao: CurrencyDao,
    private val budgetDao: BudgetDao,
    private val widgetSettingsDao: WidgetSettingsDao,
    private val budgetHistoryDao: BudgetHistoryDao,
    private val api: CurrencyApi
) : CalculatorRepository {

    override fun getAllHistory(): Flow<List<HistoryEntity>> = historyDao.getAllHistory()

    override suspend fun insertHistory(item: HistoryEntity) {
        historyDao.insertHistory(item)
    }

    override suspend fun clearHistory() {
        historyDao.clearHistory()
    }

    override fun getAllRatesFlow(): Flow<List<CurrencyRateEntity>> = currencyDao.getAllRatesFlow()

    override suspend fun getRate(code: String): CurrencyRateEntity? = currencyDao.getRate(code)

    override suspend fun insertRates(rates: List<CurrencyRateEntity>) {
        currencyDao.insertRates(rates)
    }

    override suspend fun fetchAndSaveRates(): Boolean {
        return try {
            val response = api.fetchUsdRates()
            val timestamp = System.currentTimeMillis()
            // Map the API map (usd -> currency rates)
            val entities = response.usd.map { (code, rate) ->
                CurrencyRateEntity(
                    currencyCode = code.lowercase(),
                    rateAgainstUsd = rate,
                    lastUpdated = timestamp
                )
            }
            if (entities.isNotEmpty()) {
                currencyDao.insertRates(entities)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun getBudgetFlow(): Flow<BudgetLedgerEntity?> = budgetDao.getBudgetFlow()

    override suspend fun getBudget(): BudgetLedgerEntity? = budgetDao.getBudget()

    override suspend fun updateBudget(budget: BudgetLedgerEntity) {
        budgetDao.insertOrUpdateBudget(budget)
    }

    override fun getWidgetSettingsFlow(): Flow<WidgetSettingsEntity?> = widgetSettingsDao.getSettingsFlow()

    override suspend fun getWidgetSettings(): WidgetSettingsEntity? = widgetSettingsDao.getSettings()

    override suspend fun updateWidgetSettings(settings: WidgetSettingsEntity) {
        widgetSettingsDao.insertOrUpdateSettings(settings)
    }

    override fun getAllBudgetHistory(): Flow<List<BudgetHistoryEntity>> = budgetHistoryDao.getAllBudgetHistory()

    override suspend fun insertBudgetHistory(item: BudgetHistoryEntity) {
        budgetHistoryDao.insertBudgetHistory(item)
    }

    override suspend fun updateBudgetHistory(item: BudgetHistoryEntity) {
        budgetHistoryDao.updateBudgetHistory(item)
    }

    override suspend fun deleteBudgetHistory(item: BudgetHistoryEntity) {
        budgetHistoryDao.deleteBudgetHistory(item)
    }
}


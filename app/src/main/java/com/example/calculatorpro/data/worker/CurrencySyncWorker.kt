package com.example.calculatorpro.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.calculatorpro.data.database.CalculatorDatabase
import com.example.calculatorpro.data.network.CurrencyApi
import com.example.calculatorpro.data.repository.CalculatorRepositoryImpl

class CurrencySyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = CalculatorDatabase.getDatabase(applicationContext)
            val api = CurrencyApi()
            val repository = CalculatorRepositoryImpl(
                database.historyDao(),
                database.currencyDao(),
                database.budgetDao(),
                database.widgetSettingsDao(),
                database.budgetHistoryDao(),
                api
            )
            val success = repository.fetchAndSaveRates()
            if (success) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

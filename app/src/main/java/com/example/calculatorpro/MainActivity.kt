package com.example.calculatorpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.calculatorpro.data.database.CalculatorDatabase
import com.example.calculatorpro.data.network.CurrencyApi
import com.example.calculatorpro.data.repository.CalculatorRepositoryImpl
import com.example.calculatorpro.data.worker.CurrencySyncWorker
import com.example.calculatorpro.domain.usecase.EvaluateExpressionUseCase
import com.example.calculatorpro.domain.usecase.FinancialEmiUseCase
import com.example.calculatorpro.domain.usecase.UnitConversionUseCase
import com.example.calculatorpro.theme.CalculatorProTheme
import com.example.calculatorpro.ui.mvi.AppTheme
import com.example.calculatorpro.ui.screens.MainScreen
import com.example.calculatorpro.ui.viewmodel.CalculatorViewModel
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize dependencies manually (Simple DI locator pattern)
        val database = CalculatorDatabase.getDatabase(applicationContext)
        val api = CurrencyApi()
        val repository = CalculatorRepositoryImpl(
            database.historyDao(),
            database.currencyDao(),
            database.budgetDao(),
            api
        )
        val evaluateUseCase = EvaluateExpressionUseCase()
        val conversionUseCase = UnitConversionUseCase()
        val emiUseCase = FinancialEmiUseCase()

        val viewModel = CalculatorViewModel(
            repository,
            evaluateUseCase,
            conversionUseCase,
            emiUseCase
        )

        // Schedule Daily WorkManager Task
        scheduleDailyCurrencySync()

        enableEdgeToEdge()
        setContent {
            val state by viewModel.state.collectAsState()
            val darkTheme = when (state.theme) {
                AppTheme.DARK -> true
                AppTheme.LIGHT -> false
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }
            CalculatorProTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }

    private fun scheduleDailyCurrencySync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<CurrencySyncWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "CurrencySyncWork",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}

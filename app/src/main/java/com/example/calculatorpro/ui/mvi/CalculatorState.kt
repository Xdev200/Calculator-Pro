package com.example.calculatorpro.ui.mvi

import com.example.calculatorpro.data.model.BudgetLedgerEntity
import com.example.calculatorpro.data.model.CurrencyRateEntity
import com.example.calculatorpro.data.model.HistoryEntity

enum class CalculatorMode {
    STANDARD, SCIENTIFIC, CURRENCY, METRIC, EMI
}

enum class AppTheme {
    DARK, LIGHT, SYSTEM
}

data class CalculatorState(
    val mode: CalculatorMode = CalculatorMode.STANDARD,
    val theme: AppTheme = AppTheme.SYSTEM,

    // Standard / Scientific Calculator fields
    val expression: String = "",
    val result: String = "0",
    val isDegreeMode: Boolean = true,
    val historyList: List<HistoryEntity> = emptyList(),

    // Conversion fields (Metric & Currency)
    val conversionCategory: String = "Mass", // e.g. Mass, Length, Area, Volume, Temp
    val fromUnit: String = "Pounds",
    val toUnit: String = "Kilograms",
    val fromValue: String = "",
    val toValue: String = "",
    val currencyRates: List<CurrencyRateEntity> = emptyList(),
    val offlineDisclaimerVisible: Boolean = false,
    val offlineTimestamp: String = "",

    // Financial EMI fields
    val emiPrincipal: String = "",
    val emiInterestRate: String = "",
    val emiTermMonths: String = "",
    val emiResult: String = "0.00",
    val emiActiveField: Int = 0, // 0 = Principal, 1 = Interest, 2 = Term

    // Budget Tracker (ledger status tracking)
    val budgetLimit: Double = 200.0,
    val budgetSpent: Double = 0.0
)

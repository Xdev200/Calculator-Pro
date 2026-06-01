package com.example.calculatorpro.ui.mvi

sealed class CalculatorIntent {
    // Mode transitions
    data class ChangeMode(val mode: CalculatorMode) : CalculatorIntent()
    data class ChangeTheme(val theme: AppTheme) : CalculatorIntent()

    // Math calculation intents
    data class EnterDigit(val digit: String) : CalculatorIntent()
    data class EnterOperator(val operator: String) : CalculatorIntent()
    object Backspace : CalculatorIntent()
    object Clear : CalculatorIntent()
    object TogglePlusMinus : CalculatorIntent()
    object CalculateResult : CalculatorIntent()
    object ToggleAngleUnit : CalculatorIntent()
    object ClearHistory : CalculatorIntent()

    // Conversion intents
    data class ChangeConversionCategory(val category: String) : CalculatorIntent()
    data class ChangeFromUnit(val unit: String) : CalculatorIntent()
    data class ChangeToUnit(val unit: String) : CalculatorIntent()
    data class EnterConversionDigit(val digit: String) : CalculatorIntent()
    object BackspaceConversion : CalculatorIntent()
    object ClearConversion : CalculatorIntent()
    object SwapConversionUnits : CalculatorIntent()
    object SyncRates : CalculatorIntent()

    // Financial EMI intents
    data class EnterEmiDigit(val digit: String) : CalculatorIntent()
    object BackspaceEmi : CalculatorIntent()
    object ClearEmi : CalculatorIntent()
    data class ChangeEmiActiveField(val fieldIndex: Int) : CalculatorIntent()
    object CalculateEmiResult : CalculatorIntent()

    // Budget Tracker intents
    data class UpdateBudgetLimit(val limit: Double) : CalculatorIntent()
    data class DeductBudgetAmount(val amount: Double) : CalculatorIntent()
}

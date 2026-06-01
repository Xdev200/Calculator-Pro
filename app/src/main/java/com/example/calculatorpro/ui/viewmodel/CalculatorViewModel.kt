package com.example.calculatorpro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculatorpro.data.model.BudgetLedgerEntity
import com.example.calculatorpro.data.model.CurrencyRateEntity
import com.example.calculatorpro.data.model.HistoryEntity
import com.example.calculatorpro.domain.repository.CalculatorRepository
import com.example.calculatorpro.domain.usecase.EvaluateExpressionUseCase
import com.example.calculatorpro.domain.usecase.FinancialEmiUseCase
import com.example.calculatorpro.domain.usecase.UnitConversionUseCase
import com.example.calculatorpro.ui.mvi.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalculatorViewModel(
    private val repository: CalculatorRepository,
    private val evaluateUseCase: EvaluateExpressionUseCase,
    private val conversionUseCase: UnitConversionUseCase,
    private val emiUseCase: FinancialEmiUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    init {
        // Collect calculation history
        viewModelScope.launch {
            repository.getAllHistory().collect { list ->
                _state.update { it.copy(historyList = list) }
            }
        }

        // Collect currency rates
        viewModelScope.launch {
            repository.getAllRatesFlow().collect { rates ->
                _state.update { it.copy(currencyRates = rates) }
                if (rates.isEmpty()) {
                    prepopulateDefaultRates()
                } else {
                    updateConversionResult()
                }
            }
        }

        // Collect budget status
        viewModelScope.launch {
            repository.getBudgetFlow().collect { budget ->
                if (budget != null) {
                    _state.update {
                        it.copy(
                            budgetLimit = budget.capAmount,
                            budgetSpent = budget.spentAmount
                        )
                    }
                } else {
                    // Save default budget ledger
                    repository.updateBudget(BudgetLedgerEntity(1, 200.0, 0.0, System.currentTimeMillis()))
                }
            }
        }
    }

    private suspend fun prepopulateDefaultRates() {
        val timestamp = System.currentTimeMillis()
        val defaultRates = listOf(
            CurrencyRateEntity("usd", 1.0, timestamp),
            CurrencyRateEntity("inr", 83.42, timestamp),
            CurrencyRateEntity("eur", 0.92, timestamp),
            CurrencyRateEntity("gbp", 0.79, timestamp),
            CurrencyRateEntity("jpy", 156.40, timestamp),
            CurrencyRateEntity("cad", 1.37, timestamp),
            CurrencyRateEntity("aud", 1.50, timestamp),
            CurrencyRateEntity("cny", 7.25, timestamp)
        )
        repository.insertRates(defaultRates)
        repository.updateBudget(BudgetLedgerEntity(1, 200.0, 0.0, timestamp)) // Ensure budget exists
        viewModelScope.launch(Dispatchers.IO) {
            repository.fetchAndSaveRates()
        }
    }

    fun processIntent(intent: CalculatorIntent) {
        viewModelScope.launch {
            when (intent) {
                is CalculatorIntent.ChangeMode -> {
                    _state.update { it.copy(mode = intent.mode) }
                    // Update conversion default settings on mode change
                    if (intent.mode == CalculatorMode.CURRENCY) {
                        _state.update {
                            it.copy(
                                conversionCategory = "Currency",
                                fromUnit = "USD",
                                toUnit = "INR",
                                fromValue = "",
                                toValue = ""
                            )
                        }
                    } else if (intent.mode == CalculatorMode.METRIC) {
                        _state.update {
                            it.copy(
                                conversionCategory = "Mass",
                                fromUnit = "Pounds",
                                toUnit = "Kilograms",
                                fromValue = "",
                                toValue = ""
                            )
                        }
                    }
                }
                is CalculatorIntent.ChangeTheme -> {
                    _state.update { it.copy(theme = intent.theme) }
                }

                // Math calculator logic
                is CalculatorIntent.EnterDigit -> {
                    val currentExpr = _state.value.expression
                    // Prevent multiple decimal points in a single term
                    if (intent.digit == "." && currentExpr.split(Regex("[+\\-*÷×/]")).lastOrNull()?.contains(".") == true) {
                        return@launch
                    }
                    _state.update { it.copy(expression = currentExpr + intent.digit) }
                    autoEvaluateMath()
                }
                is CalculatorIntent.EnterOperator -> {
                    val currentExpr = _state.value.expression
                    if (currentExpr.isEmpty()) {
                        if (intent.operator == "-") {
                            _state.update { it.copy(expression = "-") }
                        }
                        return@launch
                    }
                    val lastChar = currentExpr.last().toString()
                    val operators = listOf("+", "-", "×", "÷", "^")
                    if (operators.contains(lastChar)) {
                        // Replace last operator
                        _state.update { it.copy(expression = currentExpr.dropLast(1) + intent.operator) }
                    } else {
                        _state.update { it.copy(expression = currentExpr + intent.operator) }
                    }
                }
                is CalculatorIntent.Backspace -> {
                    val currentExpr = _state.value.expression
                    if (currentExpr.isNotEmpty()) {
                        _state.update { it.copy(expression = currentExpr.dropLast(1)) }
                        autoEvaluateMath()
                    }
                }
                is CalculatorIntent.Clear -> {
                    _state.update { it.copy(expression = "", result = "0") }
                }
                is CalculatorIntent.TogglePlusMinus -> {
                    val currentExpr = _state.value.expression
                    if (currentExpr.startsWith("-")) {
                        _state.update { it.copy(expression = currentExpr.drop(1)) }
                    } else {
                        _state.update { it.copy(expression = "-$currentExpr") }
                    }
                    autoEvaluateMath()
                }
                is CalculatorIntent.CalculateResult -> {
                    val finalResult = evaluateMathDirectly(_state.value.expression)
                    if (finalResult != "Error" && _state.value.expression.isNotEmpty()) {
                        repository.insertHistory(
                            HistoryEntity(
                                expression = _state.value.expression,
                                result = finalResult,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                        _state.update { it.copy(expression = finalResult, result = "") }
                    } else {
                        _state.update { it.copy(result = "Error") }
                    }
                }
                is CalculatorIntent.ToggleAngleUnit -> {
                    _state.update { it.copy(isDegreeMode = !it.isDegreeMode) }
                    autoEvaluateMath()
                }
                is CalculatorIntent.ClearHistory -> {
                    repository.clearHistory()
                }

                // Conversions (Metric and Currency)
                is CalculatorIntent.ChangeConversionCategory -> {
                    val defaultUnits = when (intent.category.lowercase()) {
                        "mass", "weight" -> Pair("Pounds", "Kilograms")
                        "length" -> Pair("Feet", "Meters")
                        "area" -> Pair("Sq Feet", "Sq Meters")
                        "volume" -> Pair("Gallons", "Liters")
                        "temperature" -> Pair("Fahrenheit", "Celsius")
                        else -> Pair("", "")
                    }
                    _state.update {
                        it.copy(
                            conversionCategory = intent.category,
                            fromUnit = defaultUnits.first,
                            toUnit = defaultUnits.second,
                            fromValue = "",
                            toValue = ""
                        )
                    }
                }
                is CalculatorIntent.ChangeFromUnit -> {
                    _state.update { it.copy(fromUnit = intent.unit) }
                    updateConversionResult()
                }
                is CalculatorIntent.ChangeToUnit -> {
                    _state.update { it.copy(toUnit = intent.unit) }
                    updateConversionResult()
                }
                is CalculatorIntent.EnterConversionDigit -> {
                    val currentVal = _state.value.fromValue
                    if (intent.digit == "." && currentVal.contains(".")) return@launch
                    _state.update { it.copy(fromValue = currentVal + intent.digit) }
                    updateConversionResult()
                }
                is CalculatorIntent.BackspaceConversion -> {
                    val currentVal = _state.value.fromValue
                    if (currentVal.isNotEmpty()) {
                        _state.update { it.copy(fromValue = currentVal.dropLast(1)) }
                        updateConversionResult()
                    }
                }
                is CalculatorIntent.ClearConversion -> {
                    _state.update { it.copy(fromValue = "", toValue = "") }
                }
                is CalculatorIntent.SwapConversionUnits -> {
                    val from = _state.value.fromUnit
                    val to = _state.value.toUnit
                    val fromVal = _state.value.fromValue
                    val toVal = _state.value.toValue
                    _state.update {
                        it.copy(
                            fromUnit = to,
                            toUnit = from,
                            fromValue = toVal,
                            toValue = fromVal
                        )
                    }
                }
                is CalculatorIntent.SyncRates -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        val success = repository.fetchAndSaveRates()
                        _state.update {
                            it.copy(
                                offlineDisclaimerVisible = !success,
                                offlineTimestamp = if (!success) {
                                    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                                    sdf.format(Date())
                                } else ""
                            )
                        }
                    }
                }

                // Financial EMI
                is CalculatorIntent.EnterEmiDigit -> {
                    val currentFieldVal = when (_state.value.emiActiveField) {
                        0 -> _state.value.emiPrincipal
                        1 -> _state.value.emiInterestRate
                        2 -> _state.value.emiTermMonths
                        else -> ""
                    }
                    if (intent.digit == "." && currentFieldVal.contains(".")) return@launch

                    val newVal = currentFieldVal + intent.digit
                    _state.update { state ->
                        when (state.emiActiveField) {
                            0 -> state.copy(emiPrincipal = newVal)
                            1 -> state.copy(emiInterestRate = newVal)
                            2 -> state.copy(emiTermMonths = newVal)
                            else -> state
                        }
                    }
                    autoCalculateEmi()
                }
                is CalculatorIntent.BackspaceEmi -> {
                    val currentFieldVal = when (_state.value.emiActiveField) {
                        0 -> _state.value.emiPrincipal
                        1 -> _state.value.emiInterestRate
                        2 -> _state.value.emiTermMonths
                        else -> ""
                    }
                    if (currentFieldVal.isNotEmpty()) {
                        val newVal = currentFieldVal.dropLast(1)
                        _state.update { state ->
                            when (state.emiActiveField) {
                                0 -> state.copy(emiPrincipal = newVal)
                                1 -> state.copy(emiInterestRate = newVal)
                                2 -> state.copy(emiTermMonths = newVal)
                                else -> state
                            }
                        }
                        autoCalculateEmi()
                    }
                }
                is CalculatorIntent.ClearEmi -> {
                    _state.update {
                        it.copy(
                            emiPrincipal = "",
                            emiInterestRate = "",
                            emiTermMonths = "",
                            emiResult = "0.00"
                        )
                    }
                }
                is CalculatorIntent.ChangeEmiActiveField -> {
                    _state.update { it.copy(emiActiveField = intent.fieldIndex) }
                }
                is CalculatorIntent.CalculateEmiResult -> {
                    autoCalculateEmi()
                }

                // Budget tracker updates
                is CalculatorIntent.UpdateBudgetLimit -> {
                    val currentLimit = intent.limit
                    val currentSpent = _state.value.budgetSpent
                    repository.updateBudget(
                        BudgetLedgerEntity(
                            id = 1,
                            capAmount = currentLimit,
                            spentAmount = currentSpent,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                }
                is CalculatorIntent.DeductBudgetAmount -> {
                    val currentLimit = _state.value.budgetLimit
                    val newSpent = _state.value.budgetSpent + intent.amount
                    repository.updateBudget(
                        BudgetLedgerEntity(
                            id = 1,
                            capAmount = currentLimit,
                            spentAmount = newSpent,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    private fun autoEvaluateMath() {
        val expr = _state.value.expression
        if (expr.isEmpty()) {
            _state.update { it.copy(result = "0") }
            return
        }
        val lastChar = expr.last().toString()
        val operators = listOf("+", "-", "×", "÷", "^", "(")
        if (operators.contains(lastChar)) return

        val res = evaluateMathDirectly(expr)
        if (res != "Error") {
            _state.update { it.copy(result = res) }
        }
    }

    private fun evaluateMathDirectly(expr: String): String {
        return try {
            val resultValue = evaluateUseCase.execute(expr, _state.value.isDegreeMode)
            if (resultValue.isNaN() || resultValue.isInfinite()) "Error" else formatResult(resultValue)
        } catch (e: Exception) {
            "Error"
        }
    }

    private fun formatResult(value: Double): String {
        val longVal = value.toLong()
        if (longVal.toDouble() == value) {
            return longVal.toString()
        }
        val formatted = String.format(Locale.US, "%.8f", value)
        return formatted.trimEnd('0').trimEnd('.')
    }

    private fun updateConversionResult() {
        val fromValStr = _state.value.fromValue
        if (fromValStr.isEmpty() || fromValStr == "-") {
            _state.update { it.copy(toValue = "") }
            return
        }
        val value = fromValStr.toDoubleOrNull() ?: return

        if (_state.value.mode == CalculatorMode.CURRENCY) {
            // Currency conversions
            val fromCode = _state.value.fromUnit.lowercase()
            val toCode = _state.value.toUnit.lowercase()
            val rates = _state.value.currencyRates

            val fromRateEntity = rates.find { it.currencyCode == fromCode }
            val toRateEntity = rates.find { it.currencyCode == toCode }

            if (fromRateEntity != null && toRateEntity != null) {
                // Rate represents currency per 1 USD
                val inUsd = value / fromRateEntity.rateAgainstUsd
                val targetVal = inUsd * toRateEntity.rateAgainstUsd
                _state.update { it.copy(toValue = String.format(Locale.US, "%.4f", targetVal).trimEnd('0').trimEnd('.')) }
            } else {
                _state.update { it.copy(toValue = "Error") }
            }
        } else {
            // Metric conversions
            val resultVal = conversionUseCase.convert(
                value,
                _state.value.conversionCategory,
                _state.value.fromUnit,
                _state.value.toUnit
            )
            _state.update { it.copy(toValue = String.format(Locale.US, "%.6f", resultVal).trimEnd('0').trimEnd('.')) }
        }
    }

    private fun autoCalculateEmi() {
        val p = _state.value.emiPrincipal.toDoubleOrNull() ?: 0.0
        val r = _state.value.emiInterestRate.toDoubleOrNull() ?: 0.0
        val n = _state.value.emiTermMonths.toIntOrNull() ?: 0

        val monthlyPayment = emiUseCase.calculateEmi(p, r, n)
        _state.update {
            it.copy(emiResult = String.format(Locale.US, "%.2f", monthlyPayment))
        }
    }
}

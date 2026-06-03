package com.example.calculatorpro.viewmodel

import com.example.calculatorpro.data.model.BudgetLedgerEntity
import com.example.calculatorpro.data.model.CurrencyRateEntity
import com.example.calculatorpro.data.model.HistoryEntity
import com.example.calculatorpro.data.model.WidgetSettingsEntity
import com.example.calculatorpro.domain.repository.CalculatorRepository
import com.example.calculatorpro.domain.usecase.EvaluateExpressionUseCase
import com.example.calculatorpro.domain.usecase.FinancialEmiUseCase
import com.example.calculatorpro.domain.usecase.UnitConversionUseCase
import com.example.calculatorpro.ui.mvi.CalculatorIntent
import com.example.calculatorpro.ui.mvi.CalculatorMode
import com.example.calculatorpro.ui.viewmodel.CalculatorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CalculatorViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepository: FakeCalculatorRepository
    private lateinit var viewModel: CalculatorViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCalculatorRepository()
        viewModel = CalculatorViewModel(
            fakeRepository,
            EvaluateExpressionUseCase(),
            UnitConversionUseCase(),
            FinancialEmiUseCase()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testModeTransition() = runTest {
        viewModel.processIntent(CalculatorIntent.ChangeMode(CalculatorMode.SCIENTIFIC))
        val state = viewModel.state.first()
        assertEquals(CalculatorMode.SCIENTIFIC, state.mode)
    }

    @Test
    fun testMathEvaluationFlow() = runTest {
        viewModel.processIntent(CalculatorIntent.EnterDigit("5"))
        viewModel.processIntent(CalculatorIntent.EnterOperator("+"))
        var state = viewModel.state.value
        assertEquals("5+", state.expression)

        viewModel.processIntent(CalculatorIntent.EnterDigit("3"))
        state = viewModel.state.value
        assertEquals("5+3", state.expression)
        
        viewModel.processIntent(CalculatorIntent.CalculateResult)
        state = viewModel.state.value
        assertEquals("8", state.expression)
    }

    @Test
    fun testEmiCalculationsFlow() = runTest {
        viewModel.processIntent(CalculatorIntent.ChangeMode(CalculatorMode.EMI))
        viewModel.processIntent(CalculatorIntent.ChangeEmiActiveField(0))
        viewModel.processIntent(CalculatorIntent.EnterEmiDigit("1"))
        viewModel.processIntent(CalculatorIntent.EnterEmiDigit("0"))
        viewModel.processIntent(CalculatorIntent.EnterEmiDigit("0"))
        viewModel.processIntent(CalculatorIntent.EnterEmiDigit("0"))
        viewModel.processIntent(CalculatorIntent.EnterEmiDigit("0")) // 10000

        viewModel.processIntent(CalculatorIntent.ChangeEmiActiveField(1))
        viewModel.processIntent(CalculatorIntent.EnterEmiDigit("6")) // 6%

        viewModel.processIntent(CalculatorIntent.ChangeEmiActiveField(2))
        viewModel.processIntent(CalculatorIntent.EnterEmiDigit("1"))
        viewModel.processIntent(CalculatorIntent.EnterEmiDigit("2")) // 12 months

        viewModel.processIntent(CalculatorIntent.CalculateEmiResult)

        val state = viewModel.state.value
        assertEquals("860.66", state.emiResult)
    }

    @Test
    fun testPrepopulateDefaultRatesOnStart() = runTest {
        // Yield to allow coroutines launched during setup to run
        testScheduler.advanceUntilIdle()
        val rates = fakeRepository.insertedRates
        org.junit.Assert.assertNotNull(rates)
        assertEquals(8, rates!!.size)
        assertEquals("usd", rates[0].currencyCode)
        assertEquals("inr", rates[1].currencyCode)
    }

    // Stub repository class for fast execution
    class FakeCalculatorRepository : CalculatorRepository {
        private val history = MutableStateFlow<List<HistoryEntity>>(emptyList())
        private val rates = MutableStateFlow<List<CurrencyRateEntity>>(emptyList())
        private val budget = MutableStateFlow<BudgetLedgerEntity?>(null)
        
        var insertedRates: List<CurrencyRateEntity>? = null

        override fun getAllHistory(): Flow<List<HistoryEntity>> = history
        override suspend fun insertHistory(item: HistoryEntity) {
            history.update { listOf(item) + it }
        }
        override suspend fun clearHistory() {
            history.update { emptyList() }
        }
        override fun getAllRatesFlow(): Flow<List<CurrencyRateEntity>> = rates
        override suspend fun getRate(code: String): CurrencyRateEntity? = rates.value.find { it.currencyCode == code }
        override suspend fun insertRates(rates: List<CurrencyRateEntity>) {
            this.insertedRates = rates
            this.rates.update { rates }
        }
        override suspend fun fetchAndSaveRates(): Boolean {
            rates.update {
                listOf(
                    CurrencyRateEntity("usd", 1.0, 0),
                    CurrencyRateEntity("inr", 83.42, 0)
                )
            }
            return true
        }
        override fun getBudgetFlow(): Flow<BudgetLedgerEntity?> = budget
        override suspend fun getBudget(): BudgetLedgerEntity? = budget.value
        override suspend fun updateBudget(budget: BudgetLedgerEntity) {
            this.budget.update { budget }
        }

        private val settings = MutableStateFlow<WidgetSettingsEntity?>(null)
        override fun getWidgetSettingsFlow(): Flow<WidgetSettingsEntity?> = settings
        override suspend fun getWidgetSettings(): WidgetSettingsEntity? = settings.value
        override suspend fun updateWidgetSettings(settings: WidgetSettingsEntity) {
            this.settings.update { settings }
        }
    }
}

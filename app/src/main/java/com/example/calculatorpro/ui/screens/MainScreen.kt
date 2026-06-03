package com.example.calculatorpro.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.calculatorpro.ui.mvi.CalculatorMode
import com.example.calculatorpro.ui.mvi.CalculatorIntent
import com.example.calculatorpro.ui.viewmodel.CalculatorViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: CalculatorViewModel) {
    val state by viewModel.state.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Widget reconfigure dialogs triggered from home screen widget ⚙️ buttons
    var showBudgetConfigDialog by remember { mutableStateOf(false) }
    var showUnitConfigDialog by remember { mutableStateOf(false) }
    var showCurrencyConfigDialog by remember { mutableStateOf(false) }

    // React to widgetConfigureTarget changes from intent
    LaunchedEffect(state.widgetConfigureTarget) {
        when (state.widgetConfigureTarget) {
            "budget" -> {
                showBudgetConfigDialog = true
                viewModel.processIntent(CalculatorIntent.SetWidgetConfigureTarget(null))
            }
            "unit_converter" -> {
                showUnitConfigDialog = true
                viewModel.processIntent(CalculatorIntent.SetWidgetConfigureTarget(null))
            }
            "currency" -> {
                showCurrencyConfigDialog = true
                viewModel.processIntent(CalculatorIntent.SetWidgetConfigureTarget(null))
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.width(300.dp)
            ) {
                DrawerContent(
                    viewModel = viewModel,
                    currentMode = state.mode,
                    currentTheme = state.theme,
                    onModeSelected = { mode ->
                        viewModel.processIntent(CalculatorIntent.ChangeMode(mode))
                        scope.launch { drawerState.close() }
                    },
                    onThemeSelected = { theme ->
                        viewModel.processIntent(CalculatorIntent.ChangeTheme(theme))
                    }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                AnimatedContent(
                    targetState = state.mode,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "ScreenTransition"
                ) { mode ->
                    when (mode) {
                        CalculatorMode.STANDARD -> CalculatorScreen(viewModel, isScientific = false, onOpenDrawer = { scope.launch { drawerState.open() } })
                        CalculatorMode.SCIENTIFIC -> CalculatorScreen(viewModel, isScientific = true, onOpenDrawer = { scope.launch { drawerState.open() } })
                        CalculatorMode.METRIC -> ConversionScreen(viewModel, isCurrency = false, onOpenDrawer = { scope.launch { drawerState.open() } })
                        CalculatorMode.CURRENCY -> ConversionScreen(viewModel, isCurrency = true, onOpenDrawer = { scope.launch { drawerState.open() } })
                        CalculatorMode.EMI -> EmiScreen(viewModel, onOpenDrawer = { scope.launch { drawerState.open() } })
                    }
                }

                // Budget History Overlay Screen
                AnimatedVisibility(
                    visible = state.isBudgetHistoryVisible,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    BudgetHistoryScreen(
                        state = state,
                        onIntent = { intent -> viewModel.processIntent(intent) }
                    )
                }
            }
        }
    }

    // Widget reconfigure dialogs (shown when app is launched from widget ⚙️ button)
    if (showBudgetConfigDialog) {
        BudgetConfigDialog(
            currentLimit = state.budgetLimit,
            currentCurrency = state.budgetCurrency,
            onSave = { limit, currency ->
                viewModel.processIntent(CalculatorIntent.UpdateBudgetSettings(limit, currency))
                showBudgetConfigDialog = false
                Toast.makeText(context, "Budget Widget Updated!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showBudgetConfigDialog = false }
        )
    }

    if (showUnitConfigDialog) {
        UnitConverterConfigDialog(
            currentCategory = state.widgetConversionCategory,
            currentFromUnit = state.widgetConversionFromUnit,
            currentToUnit = state.widgetConversionToUnit,
            onSave = { cat, from, to ->
                viewModel.processIntent(CalculatorIntent.UpdateWidgetConversionSettings(cat, from, to))
                showUnitConfigDialog = false
                Toast.makeText(context, "Unit Widget Updated!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showUnitConfigDialog = false }
        )
    }

    if (showCurrencyConfigDialog) {
        CurrencyPairConfigDialog(
            currentPairs = state.widgetCurrencyPairs,
            onSave = { pairs ->
                viewModel.processIntent(CalculatorIntent.UpdateCurrencyPairSettings(pairs))
                showCurrencyConfigDialog = false
                Toast.makeText(context, "Currency Snapshot Updated!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showCurrencyConfigDialog = false }
        )
    }
}

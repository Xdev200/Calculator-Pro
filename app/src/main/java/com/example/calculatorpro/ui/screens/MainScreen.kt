package com.example.calculatorpro.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.width(300.dp)
            ) {
                DrawerContent(
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
            }
        }
    }
}

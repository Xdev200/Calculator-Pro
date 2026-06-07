package com.example.calculatorpro.ui.screens

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculatorpro.ui.mvi.AppTheme
import com.example.calculatorpro.ui.mvi.CalculatorMode
import com.example.calculatorpro.ui.mvi.CalculatorIntent
import com.example.calculatorpro.ui.viewmodel.CalculatorViewModel
import androidx.compose.runtime.collectAsState
import com.example.calculatorpro.widget.*

@Composable
fun DrawerContent(
    viewModel: CalculatorViewModel,
    currentMode: CalculatorMode,
    currentTheme: AppTheme,
    onModeSelected: (CalculatorMode) -> Unit,
    onThemeSelected: (AppTheme) -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    var showBudgetDialog by remember { mutableStateOf(false) }
    var showUnitConverterDialog by remember { mutableStateOf(false) }
    var showCurrencyPairDialog by remember { mutableStateOf(false) }

    // Track whether to pin after saving settings
    var pinAfterSaveBudget by remember { mutableStateOf(false) }
    var pinAfterSaveUnit by remember { mutableStateOf(false) }
    var pinAfterSaveCurrency by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Calculator Pro",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val menuItems = listOf(
                Triple("Standard Mode", CalculatorMode.STANDARD, Icons.Default.Home),
                Triple("Scientific Mode", CalculatorMode.SCIENTIFIC, Icons.Default.Build),
                Triple("Currency Converter", CalculatorMode.CURRENCY, Icons.Default.Refresh),
                Triple("Metric Converter", CalculatorMode.METRIC, Icons.Default.List),
                Triple("Financial EMI", CalculatorMode.EMI, Icons.Default.PlayArrow)
            )

            menuItems.forEach { (title, mode, icon) ->
                val isSelected = currentMode == mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .height(48.dp)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable { onModeSelected(mode) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Budget History special item
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .height(48.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.medium
                    )
                    .clickable { 
                        viewModel.processIntent(CalculatorIntent.SetBudgetHistoryVisibility(true))
                    }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Budget History",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Budget History",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Widgets Section
        Column {
            Text(
                text = "Home Screen Widgets",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Widget items: name, receiver class, icon, has settings dialog
            data class WidgetItem(
                val name: String,
                val receiverClass: Class<*>,
                val icon: androidx.compose.ui.graphics.vector.ImageVector,
                val hasSettings: Boolean
            )

            val widgets = listOf(
                WidgetItem("Currency Snapshot", CurrencySnapshotReceiver::class.java, Icons.Default.Refresh, true),
                WidgetItem("Daily Budget Ledger", BudgetBurnReceiver::class.java, Icons.Default.ShoppingCart, true),
                WidgetItem("Custom Unit Converter", UnitConversionReceiver::class.java, Icons.Default.List, true)
            )

            widgets.forEach { widgetItem ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .height(48.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable {
                            // Row tap opens the settings dialog (not pin)
                            when (widgetItem.name) {
                                "Daily Budget Ledger" -> {
                                    pinAfterSaveBudget = false
                                    showBudgetDialog = true
                                }
                                "Custom Unit Converter" -> {
                                    pinAfterSaveUnit = false
                                    showUnitConverterDialog = true
                                }
                                "Currency Snapshot" -> {
                                    pinAfterSaveCurrency = false
                                    showCurrencyPairDialog = true
                                }
                            }
                        }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = widgetItem.icon,
                        contentDescription = widgetItem.name,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = widgetItem.name,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    // + button: shows settings first, then pins after save
                    IconButton(
                        onClick = {
                            when (widgetItem.name) {
                                "Daily Budget Ledger" -> {
                                    pinAfterSaveBudget = true
                                    showBudgetDialog = true
                                }
                                "Custom Unit Converter" -> {
                                    pinAfterSaveUnit = true
                                    showUnitConverterDialog = true
                                }
                                "Currency Snapshot" -> {
                                    pinAfterSaveCurrency = true
                                    showCurrencyPairDialog = true
                                }
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Pin Widget",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // ========================
        // Budget Configuration Dialog
        // ========================
        if (showBudgetDialog) {
            BudgetConfigDialog(
                currentLimit = state.budgetLimit,
                currentCurrency = state.budgetCurrency,
                onSave = { limit, currency ->
                    viewModel.processIntent(CalculatorIntent.UpdateBudgetSettings(limit, currency))
                    showBudgetDialog = false
                    Toast.makeText(context, "Budget Configured!", Toast.LENGTH_SHORT).show()
                    if (pinAfterSaveBudget) {
                        pinWidget(context, BudgetBurnReceiver::class.java)
                        pinAfterSaveBudget = false
                    }
                },
                onDismiss = {
                    showBudgetDialog = false
                    pinAfterSaveBudget = false
                }
            )
        }

        // ========================
        // Unit Converter Configuration Dialog
        // ========================
        if (showUnitConverterDialog) {
            UnitConverterConfigDialog(
                currentCategory = state.widgetConversionCategory,
                currentFromUnit = state.widgetConversionFromUnit,
                currentToUnit = state.widgetConversionToUnit,
                onSave = { cat, from, to ->
                    viewModel.processIntent(CalculatorIntent.UpdateWidgetConversionSettings(cat, from, to))
                    showUnitConverterDialog = false
                    Toast.makeText(context, "Conversion Widget Configured!", Toast.LENGTH_SHORT).show()
                    if (pinAfterSaveUnit) {
                        pinWidget(context, UnitConversionReceiver::class.java)
                        pinAfterSaveUnit = false
                    }
                },
                onDismiss = {
                    showUnitConverterDialog = false
                    pinAfterSaveUnit = false
                }
            )
        }

        // ========================
        // Currency Pair Configuration Dialog
        // ========================
        if (showCurrencyPairDialog) {
            CurrencyPairConfigDialog(
                currentPairs = state.widgetCurrencyPairs,
                onSave = { pairs ->
                    viewModel.processIntent(CalculatorIntent.UpdateCurrencyPairSettings(pairs))
                    showCurrencyPairDialog = false
                    Toast.makeText(context, "Currency Snapshot Configured!", Toast.LENGTH_SHORT).show()
                    if (pinAfterSaveCurrency) {
                        pinWidget(context, CurrencySnapshotReceiver::class.java)
                        pinAfterSaveCurrency = false
                    }
                },
                onDismiss = {
                    showCurrencyPairDialog = false
                    pinAfterSaveCurrency = false
                }
            )
        }

        // Theme Selection Section
        Column {
            Text(
                text = "Theme Selection",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val themes = listOf(
                    Pair("Dark", AppTheme.DARK),
                    Pair("Light", AppTheme.LIGHT),
                    Pair("System", AppTheme.SYSTEM)
                )

                themes.forEach { (name, theme) ->
                    val isSelected = currentTheme == theme
                    Button(
                        onClick = { onThemeSelected(theme) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text(text = name, fontSize = 12.sp)
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = "v1.0.0 (NeuralMesh)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
        }
    }
}

// ==========================================
// Reusable Widget Configuration Dialogs
// ==========================================

@Composable
fun BudgetConfigDialog(
    currentLimit: Double,
    currentCurrency: String,
    onSave: (Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    var limitInput by remember { mutableStateOf(currentLimit.toString()) }
    var selectedCurrency by remember { mutableStateOf(currentCurrency) }
    var showCurrencyDropdown by remember { mutableStateOf(false) }
    val currencySymbols = listOf("$", "₹", "€", "£", "¥", "元")

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Configure Budget Widget", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = limitInput,
                    onValueChange = { limitInput = it },
                    label = { Text("Budget Cap Limit") },
                    modifier = Modifier.fillMaxWidth()
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showCurrencyDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Currency Symbol: $selectedCurrency ▼")
                    }
                    DropdownMenu(
                        expanded = showCurrencyDropdown,
                        onDismissRequest = { showCurrencyDropdown = false }
                    ) {
                        currencySymbols.forEach { sym ->
                            DropdownMenuItem(
                                text = { Text(sym) },
                                onClick = {
                                    selectedCurrency = sym
                                    showCurrencyDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitInput.toDoubleOrNull() ?: 200.0
                    onSave(limit, selectedCurrency)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun UnitConverterConfigDialog(
    currentCategory: String,
    currentFromUnit: String,
    currentToUnit: String,
    onSave: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCat by remember { mutableStateOf(currentCategory) }
    var selectedFrom by remember { mutableStateOf(currentFromUnit) }
    var selectedTo by remember { mutableStateOf(currentToUnit) }

    var showCatDropdown by remember { mutableStateOf(false) }
    var showFromDropdown by remember { mutableStateOf(false) }
    var showToDropdown by remember { mutableStateOf(false) }

    val cats = listOf("Mass", "Length", "Area", "Volume", "Temperature")
    val catUnits = when (selectedCat.lowercase()) {
        "mass" -> listOf("Pounds", "Kilograms", "Grams", "Ounces")
        "length" -> listOf("Meters", "Kilometers", "Centimeters", "Inches", "Feet", "Miles")
        "area" -> listOf("Sq Meters", "Sq Kilometers", "Sq Feet", "Acres")
        "volume" -> listOf("Liters", "Milliliters", "Gallons", "Cups")
        "temperature" -> listOf("Celsius", "Fahrenheit", "Kelvin")
        else -> emptyList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Configure Unit Widget", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showCatDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Category: $selectedCat ▼")
                    }
                    DropdownMenu(
                        expanded = showCatDropdown,
                        onDismissRequest = { showCatDropdown = false }
                    ) {
                        cats.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = {
                                    selectedCat = c
                                    showCatDropdown = false
                                    val defaultU = when (c.lowercase()) {
                                        "mass" -> Pair("Pounds", "Kilograms")
                                        "length" -> Pair("Feet", "Meters")
                                        "area" -> Pair("Sq Feet", "Sq Meters")
                                        "volume" -> Pair("Gallons", "Liters")
                                        "temperature" -> Pair("Fahrenheit", "Celsius")
                                        else -> Pair("", "")
                                    }
                                    selectedFrom = defaultU.first
                                    selectedTo = defaultU.second
                                }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showFromDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("From Unit: $selectedFrom ▼")
                    }
                    DropdownMenu(
                        expanded = showFromDropdown,
                        onDismissRequest = { showFromDropdown = false }
                    ) {
                        catUnits.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(u) },
                                onClick = {
                                    selectedFrom = u
                                    showFromDropdown = false
                                }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showToDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("To Unit: $selectedTo ▼")
                    }
                    DropdownMenu(
                        expanded = showToDropdown,
                        onDismissRequest = { showToDropdown = false }
                    ) {
                        catUnits.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(u) },
                                onClick = {
                                    selectedTo = u
                                    showToDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selectedCat, selectedFrom, selectedTo) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CurrencyPairConfigDialog(
    currentPairs: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val allCurrencies = listOf("INR", "EUR", "GBP", "JPY", "CAD", "AUD", "CNY")
    val currentSelected = currentPairs.split(",").map { it.trim().uppercase() }.filter { it.isNotEmpty() }
    var selectedCodes by remember { mutableStateOf(currentSelected.toSet()) }

    val currencyLabels = mapOf(
        "INR" to "🇮🇳 Indian Rupee (INR)",
        "EUR" to "🇪🇺 Euro (EUR)",
        "GBP" to "🇬🇧 British Pound (GBP)",
        "JPY" to "🇯🇵 Japanese Yen (JPY)",
        "CAD" to "🇨🇦 Canadian Dollar (CAD)",
        "AUD" to "🇦🇺 Australian Dollar (AUD)",
        "CNY" to "🇨🇳 Chinese Yuan (CNY)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Configure Currency Pairs", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Select currencies to display against USD:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                allCurrencies.forEach { code ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedCodes = if (selectedCodes.contains(code)) {
                                    selectedCodes - code
                                } else {
                                    selectedCodes + code
                                }
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedCodes.contains(code),
                            onCheckedChange = { checked ->
                                selectedCodes = if (checked) {
                                    selectedCodes + code
                                } else {
                                    selectedCodes - code
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currencyLabels[code] ?: code,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pairs = selectedCodes.joinToString(",")
                    if (pairs.isNotEmpty()) {
                        onSave(pairs)
                    }
                },
                enabled = selectedCodes.isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun pinWidget(context: Context, receiverClass: Class<*>) {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val myProvider = ComponentName(context, receiverClass)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            val successCallback = PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, receiverClass).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
        } else {
            Toast.makeText(context, "Pinning widgets is not supported by your launcher.", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "Direct widget pinning requires Android 8.0 or above.", Toast.LENGTH_SHORT).show()
    }
}

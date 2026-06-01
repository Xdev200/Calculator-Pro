package com.example.calculatorpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculatorpro.ui.mvi.CalculatorIntent
import com.example.calculatorpro.ui.viewmodel.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionScreen(
    viewModel: CalculatorViewModel,
    isCurrency: Boolean,
    onOpenDrawer: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showFromUnitMenu by remember { mutableStateOf(false) }
    var showToUnitMenu by remember { mutableStateOf(false) }

    val categories = listOf("Mass", "Length", "Area", "Volume", "Temperature")
    val units = when (state.conversionCategory.lowercase()) {
        "mass" -> listOf("Pounds", "Kilograms", "Grams", "Ounces")
        "length" -> listOf("Meters", "Kilometers", "Centimeters", "Inches", "Feet", "Miles")
        "area" -> listOf("Sq Meters", "Sq Kilometers", "Sq Feet", "Acres")
        "volume" -> listOf("Liters", "Milliliters", "Gallons", "Cups")
        "temperature" -> listOf("Celsius", "Fahrenheit", "Kelvin")
        else -> emptyList()
    }

    val currencyCodes = listOf("USD", "INR", "EUR", "GBP", "JPY", "CAD", "AUD", "CNY")

    val currentUnitList = if (isCurrency) currencyCodes else units

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Menu",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            if (isCurrency) {
                Text(
                    text = "Currency Converter",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = { viewModel.processIntent(CalculatorIntent.SyncRates) }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync Rates",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                // Category Selector Spinner
                Box {
                    Button(
                        onClick = { showCategoryMenu = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(text = "${state.conversionCategory} ▼", color = MaterialTheme.colorScheme.onSurface)
                    }
                    DropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    viewModel.processIntent(CalculatorIntent.ChangeConversionCategory(cat))
                                    showCategoryMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Two Field Displays (From & To)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // From Block
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "From",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        // Dropdown Selector
                        Box {
                            Text(
                                text = "${state.fromUnit} ▼",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable { showFromUnitMenu = true }
                                    .padding(8.dp)
                            )
                            DropdownMenu(
                                expanded = showFromUnitMenu,
                                onDismissRequest = { showFromUnitMenu = false }
                            ) {
                                currentUnitList.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit) },
                                        onClick = {
                                            viewModel.processIntent(CalculatorIntent.ChangeFromUnit(unit))
                                            showFromUnitMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = state.fromValue.ifEmpty { "0" },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // To Block
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "To",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        // Dropdown Selector
                        Box {
                            Text(
                                text = "${state.toUnit} ▼",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable { showToUnitMenu = true }
                                    .padding(8.dp)
                            )
                            DropdownMenu(
                                expanded = showToUnitMenu,
                                onDismissRequest = { showToUnitMenu = false }
                            ) {
                                currentUnitList.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit) },
                                        onClick = {
                                            viewModel.processIntent(CalculatorIntent.ChangeToUnit(unit))
                                            showToUnitMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = state.toValue.ifEmpty { "0" },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (isCurrency && state.offlineDisclaimerVisible) {
                Text(
                    text = "⚠️ Offline rate cache time: ${state.offlineTimestamp}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Numeric entry keypad (preserving exact keypad physical layout targets)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val keys = listOf(
                listOf("7", "8", "9", "⌫"),
                listOf("4", "5", "6", "AC"),
                listOf("1", "2", "3", "⇄"),
                listOf("+/-", "0", ".", "Done")
            )

            keys.forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowKeys.forEach { key ->
                        val isUtility = listOf("⌫", "AC", "⇄", "Done").contains(key)
                        CalculatorKey(
                            label = key,
                            modifier = Modifier.weight(1f),
                            containerColor = when {
                                key == "Done" -> MaterialTheme.colorScheme.primary
                                isUtility -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            contentColor = when {
                                key == "Done" -> MaterialTheme.colorScheme.onPrimary
                                isUtility -> MaterialTheme.colorScheme.onSecondaryContainer
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        ) {
                            when (key) {
                                "AC" -> viewModel.processIntent(CalculatorIntent.ClearConversion)
                                "⌫" -> viewModel.processIntent(CalculatorIntent.BackspaceConversion)
                                "⇄" -> viewModel.processIntent(CalculatorIntent.SwapConversionUnits)
                                "+/-" -> {
                                    // Temp can be negative, others positive
                                    if (state.conversionCategory.lowercase() == "temperature") {
                                        val curVal = state.fromValue
                                        if (curVal.startsWith("-")) {
                                            viewModel.processIntent(CalculatorIntent.ClearConversion)
                                            viewModel.processIntent(CalculatorIntent.EnterConversionDigit(curVal.drop(1)))
                                        } else if (curVal.isNotEmpty()) {
                                            viewModel.processIntent(CalculatorIntent.ClearConversion)
                                            viewModel.processIntent(CalculatorIntent.EnterConversionDigit("-$curVal"))
                                        }
                                    }
                                }
                                "Done" -> {
                                    // Simply dismiss keyboard
                                }
                                else -> viewModel.processIntent(CalculatorIntent.EnterConversionDigit(key))
                            }
                        }
                    }
                }
            }
        }
    }
}

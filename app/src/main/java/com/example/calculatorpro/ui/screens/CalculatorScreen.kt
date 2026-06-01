package com.example.calculatorpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.window.Dialog
import com.example.calculatorpro.ui.mvi.CalculatorIntent
import com.example.calculatorpro.ui.viewmodel.CalculatorViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    isScientific: Boolean,
    onOpenDrawer: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showHistoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Action Header
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isScientific) {
                    TextButton(onClick = { viewModel.processIntent(CalculatorIntent.ToggleAngleUnit) }) {
                        Text(
                            text = if (state.isDegreeMode) "DEG" else "RAD",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                IconButton(onClick = { showHistoryDialog = true }) {
                    Text(
                        text = "🕒",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Display Area (Expression and Result)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = state.expression,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.result.ifEmpty { "0" },
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Keypad area
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isScientific) {
                // Row 1 Scientific
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScientificKey("sin", Modifier.weight(1f)) { viewModel.processIntent(CalculatorIntent.EnterDigit("sin(")) }
                    ScientificKey("cos", Modifier.weight(1f)) { viewModel.processIntent(CalculatorIntent.EnterDigit("cos(")) }
                    ScientificKey("tan", Modifier.weight(1f)) { viewModel.processIntent(CalculatorIntent.EnterDigit("tan(")) }
                    ScientificKey("√", Modifier.weight(1f)) { viewModel.processIntent(CalculatorIntent.EnterDigit("sqrt(")) }
                }
                // Row 2 Scientific
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScientificKey("ln", Modifier.weight(1f)) { viewModel.processIntent(CalculatorIntent.EnterDigit("ln(")) }
                    ScientificKey("log", Modifier.weight(1f)) { viewModel.processIntent(CalculatorIntent.EnterDigit("log(")) }
                    ScientificKey("xʸ", Modifier.weight(1f)) { viewModel.processIntent(CalculatorIntent.EnterOperator("^")) }
                    ScientificKey("π", Modifier.weight(1f)) { viewModel.processIntent(CalculatorIntent.EnterDigit("π")) }
                    ScientificKey("e", Modifier.weight(1f)) { viewModel.processIntent(CalculatorIntent.EnterDigit("e")) }
                }
            }

            // Standard rows (Immutable layout structure preserving muscle memory)
            val rows = listOf(
                listOf("C", "(", ")", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("+/-", "0", ".", "=")
            )

            rows.forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowKeys.forEach { key ->
                        val isOperator = listOf("÷", "×", "-", "+", "=").contains(key)
                        val isUtility = listOf("C", "(", ")", "+/-").contains(key)
                        CalculatorKey(
                            label = key,
                            modifier = Modifier.weight(1f),
                            containerColor = when {
                                key == "=" -> MaterialTheme.colorScheme.primary
                                isOperator -> MaterialTheme.colorScheme.primaryContainer
                                isUtility -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            contentColor = when {
                                key == "=" -> MaterialTheme.colorScheme.onPrimary
                                isOperator -> MaterialTheme.colorScheme.onPrimaryContainer
                                isUtility -> MaterialTheme.colorScheme.onSecondaryContainer
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        ) {
                            when (key) {
                                "C" -> viewModel.processIntent(CalculatorIntent.Clear)
                                "=" -> viewModel.processIntent(CalculatorIntent.CalculateResult)
                                "+/-" -> viewModel.processIntent(CalculatorIntent.TogglePlusMinus)
                                "(" -> viewModel.processIntent(CalculatorIntent.EnterDigit("("))
                                ")" -> viewModel.processIntent(CalculatorIntent.EnterDigit(")"))
                                "÷", "×", "-", "+" -> viewModel.processIntent(CalculatorIntent.EnterOperator(key))
                                else -> viewModel.processIntent(CalculatorIntent.EnterDigit(key))
                            }
                        }
                    }
                }
            }
        }
    }

    // History Dialog popup
    if (showHistoryDialog) {
        Dialog(onDismissRequest = { showHistoryDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(16.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Calculation History",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        TextButton(onClick = { viewModel.processIntent(CalculatorIntent.ClearHistory) }) {
                            Text("Clear", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    if (state.historyList.isEmpty()) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No calculations recorded yet.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.historyList) { item ->
                                val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                val time = sdf.format(Date(item.timestamp))

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.processIntent(CalculatorIntent.Clear)
                                            viewModel.processIntent(CalculatorIntent.EnterDigit(item.expression))
                                            showHistoryDialog = false
                                        }
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = item.expression,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = "= ${item.result}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = time,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        modifier = Modifier.align(Alignment.Start)
                                    )
                                    Divider(modifier = Modifier.padding(top = 4.dp))
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showHistoryDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorKey(
    label: String,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(containerColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@Composable
fun ScientificKey(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

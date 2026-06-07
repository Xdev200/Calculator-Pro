package com.example.calculatorpro.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Delete
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
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp), // Screen margin / safe area padding
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
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.extraLarge)
                            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.extraLarge)
                            .clickable { viewModel.processIntent(CalculatorIntent.ToggleAngleUnit) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (state.isDegreeMode) "DEG" else "RAD",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                IconButton(onClick = { showHistoryDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "History",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Display Area (Expression and Result) with Glass Effect feel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp)
                .clip(MaterialTheme.shapes.large) // 24px radius
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), MaterialTheme.shapes.large)
                .padding(24.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = state.expression,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.result.ifEmpty { "0" },
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )
        }

        // Keypad area
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isScientific) {
                // Row 1 Scientific
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ScientificKey("sin", Modifier.weight(1f)) { viewModel.processIntent(CalculatorIntent.EnterDigit("sin(")) }
                    ScientificKey("cos", Modifier.weight(1f)) { viewModel.processIntent(CalculatorIntent.EnterDigit("cos(")) }
                    ScientificKey("tan", Modifier.weight(1f)) { viewModel.processIntent(CalculatorIntent.EnterDigit("tan(")) }
                    ScientificKey("√", Modifier.weight(1f)) { viewModel.processIntent(CalculatorIntent.EnterDigit("sqrt(")) }
                }
                // Row 2 Scientific
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ScientificKey("ln", Modifier.weight(1f)) { viewModel.processIntent(CalculatorIntent.EnterDigit("ln(")) }
                    ScientificKey("log", Modifier.weight(1f)) { viewModel.processIntent(CalculatorIntent.EnterDigit("log(")) }
                    ScientificKey("xʸ", Modifier.weight(1f)) { viewModel.processIntent(CalculatorIntent.EnterOperator("^")) }
                    ScientificKey("π", Modifier.weight(1f)) { viewModel.processIntent(CalculatorIntent.EnterDigit("π")) }
                    ScientificKey("e", Modifier.weight(1f)) { viewModel.processIntent(CalculatorIntent.EnterDigit("e")) }
                }
            }

            // Standard rows
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowKeys.forEach { key ->
                        val isOperator = listOf("÷", "×", "-", "+", "=").contains(key)
                        val isUtility = listOf("C", "(", ")", "+/-").contains(key)
                        CalculatorKey(
                            label = key,
                            modifier = Modifier.weight(1f),
                            containerColor = when {
                                key == "=" -> MaterialTheme.colorScheme.primary
                                isOperator -> MaterialTheme.colorScheme.surface
                                isUtility -> MaterialTheme.colorScheme.surface
                                else -> MaterialTheme.colorScheme.surface
                            },
                            contentColor = when {
                                key == "=" -> MaterialTheme.colorScheme.onPrimary
                                isOperator -> MaterialTheme.colorScheme.primary
                                isUtility -> MaterialTheme.colorScheme.onSurface
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            borderColor = when {
                                key == "=" -> MaterialTheme.colorScheme.primaryContainer
                                isOperator -> MaterialTheme.colorScheme.primary
                                isUtility -> MaterialTheme.colorScheme.onSurface
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            },
                            isPrimary = key == "="
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
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { viewModel.processIntent(CalculatorIntent.ClearHistory) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear History",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline)

                    if (state.historyList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
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
                                    Divider(modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showHistoryDialog = false },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
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
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val yOffset by animateDpAsState(if (isPressed) 2.dp else 0.dp)
    val shadowHeight = if (isPrimary) 4.dp else 2.dp

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .offset(y = yOffset)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        // Shadow / Bottom Border layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = shadowHeight)
                .clip(MaterialTheme.shapes.small)
                .background(borderColor)
        )
        // Main Button Surface
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isPressed) 0.dp else shadowHeight)
                .clip(MaterialTheme.shapes.small)
                .background(containerColor)
                .border(1.dp, borderColor, MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = if (label.length > 2) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.labelLarge,
                color = contentColor
            )
        }
    }
}

@Composable
fun ScientificKey(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val yOffset by animateDpAsState(if (isPressed) 2.dp else 0.dp)
    val shadowHeight = 2.dp
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .height(44.dp)
            .offset(y = yOffset)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = shadowHeight)
                .clip(MaterialTheme.shapes.small)
                .background(borderColor)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isPressed) 0.dp else shadowHeight)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, borderColor, MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

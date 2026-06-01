package com.example.calculatorpro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculatorpro.ui.mvi.CalculatorIntent
import com.example.calculatorpro.ui.viewmodel.CalculatorViewModel

@Composable
fun EmiScreen(
    viewModel: CalculatorViewModel,
    onOpenDrawer: () -> Unit
) {
    val state by viewModel.state.collectAsState()

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
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Menu",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Financial EMI Terminal",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Input Form Fields
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Principal Field
            val isPrincipalActive = state.emiActiveField == 0
            OutlinedCard(
                border = BorderStroke(
                    width = if (isPrincipalActive) 2.dp else 1.dp,
                    color = if (isPrincipalActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPrincipalActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.processIntent(CalculatorIntent.ChangeEmiActiveField(0)) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Principal Loan ($)", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Text(
                        text = state.emiPrincipal.ifEmpty { "0" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isPrincipalActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Interest Rate Field
            val isInterestActive = state.emiActiveField == 1
            OutlinedCard(
                border = BorderStroke(
                    width = if (isInterestActive) 2.dp else 1.dp,
                    color = if (isInterestActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isInterestActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.processIntent(CalculatorIntent.ChangeEmiActiveField(1)) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Target Interest (%)", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Text(
                        text = state.emiInterestRate.ifEmpty { "0" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isInterestActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Repayment Term Field
            val isTermActive = state.emiActiveField == 2
            OutlinedCard(
                border = BorderStroke(
                    width = if (isTermActive) 2.dp else 1.dp,
                    color = if (isTermActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isTermActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.processIntent(CalculatorIntent.ChangeEmiActiveField(2)) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Repayment Term (Months)", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Text(
                        text = state.emiTermMonths.ifEmpty { "0" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isTermActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Result Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Estimated Monthly Installment",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$${state.emiResult}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Adapted Keypad
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val keys = listOf(
                listOf("7", "8", "9", "Next"),
                listOf("4", "5", "6", "⌫"),
                listOf("1", "2", "3", "C"),
                listOf("", "0", ".", "Calc")
            )

            keys.forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowKeys.forEach { key ->
                        if (key.isEmpty()) {
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            val isAction = listOf("Next", "⌫", "C", "Calc").contains(key)
                            CalculatorKey(
                                label = key,
                                modifier = Modifier.weight(1f),
                                containerColor = when {
                                    key == "Calc" -> MaterialTheme.colorScheme.primary
                                    isAction -> MaterialTheme.colorScheme.secondaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                contentColor = when {
                                    key == "Calc" -> MaterialTheme.colorScheme.onPrimary
                                    isAction -> MaterialTheme.colorScheme.onSecondaryContainer
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            ) {
                                when (key) {
                                    "C" -> viewModel.processIntent(CalculatorIntent.ClearEmi)
                                    "⌫" -> viewModel.processIntent(CalculatorIntent.BackspaceEmi)
                                    "Next" -> {
                                        val nextField = (state.emiActiveField + 1) % 3
                                        viewModel.processIntent(CalculatorIntent.ChangeEmiActiveField(nextField))
                                    }
                                    "Calc" -> viewModel.processIntent(CalculatorIntent.CalculateEmiResult)
                                    else -> viewModel.processIntent(CalculatorIntent.EnterEmiDigit(key))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

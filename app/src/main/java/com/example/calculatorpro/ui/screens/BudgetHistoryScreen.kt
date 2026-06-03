package com.example.calculatorpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.calculatorpro.data.model.BudgetHistoryEntity
import com.example.calculatorpro.ui.mvi.CalculatorIntent
import com.example.calculatorpro.ui.mvi.CalculatorState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetHistoryScreen(
    state: CalculatorState,
    onIntent: (CalculatorIntent) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<BudgetHistoryEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget History") },
                navigationIcon = {
                    IconButton(onClick = { onIntent(CalculatorIntent.SetBudgetHistoryVisibility(false)) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Deduction")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Total spent header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${state.budgetCurrency}${String.format(Locale.US, "%.2f", state.budgetSpent)} / ${state.budgetCurrency}${String.format(Locale.US, "%.2f", state.budgetLimit)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // History List
            if (state.budgetHistoryList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No deductions yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.budgetHistoryList, key = { it.id }) { item ->
                        BudgetHistoryCard(
                            item = item,
                            currencySymbol = state.budgetCurrency,
                            onEdit = { editingItem = item },
                            onDelete = { onIntent(CalculatorIntent.DeleteBudgetHistoryEntry(item)) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        BudgetHistoryEditDialog(
            item = null,
            onDismiss = { showAddDialog = false },
            onSave = { amount, note ->
                onIntent(CalculatorIntent.AddBudgetHistoryEntry(amount, note))
                showAddDialog = false
            }
        )
    }

    editingItem?.let { item ->
        BudgetHistoryEditDialog(
            item = item,
            onDismiss = { editingItem = null },
            onSave = { amount, note ->
                onIntent(CalculatorIntent.UpdateBudgetHistoryEntry(item.copy(amountDeducted = amount, note = note)))
                editingItem = null
            }
        )
    }
}

@Composable
fun BudgetHistoryCard(
    item: BudgetHistoryEntity,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val dateStr = sdf.format(Date(item.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.note.ifEmpty { "Deduction" }, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = dateStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = "$currencySymbol${String.format(Locale.US, "%.2f", item.amountDeducted)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun BudgetHistoryEditDialog(
    item: BudgetHistoryEntity?,
    onDismiss: () -> Unit,
    onSave: (Double, String) -> Unit
) {
    var amountText by remember { mutableStateOf(item?.amountDeducted?.toString() ?: "") }
    var noteText by remember { mutableStateOf(item?.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Add Deduction" else "Edit Deduction") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        onSave(amount, noteText)
                    }
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

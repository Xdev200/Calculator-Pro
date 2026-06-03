package com.example.calculatorpro.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.glance.appwidget.updateAll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.calculatorpro.data.database.CalculatorDatabase
import com.example.calculatorpro.data.model.BudgetHistoryEntity
import com.example.calculatorpro.data.model.BudgetLedgerEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WidgetInputDialogActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val action = intent.action
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        
        setContent {
            MaterialTheme {
                when (action) {
                    "ACTION_EDIT_BUDGET" -> BudgetInputDialog(onDismiss = { finish() }, context = this, appWidgetId = appWidgetId)
                    "ACTION_EDIT_UNIT" -> UnitInputDialog(onDismiss = { finish() }, context = this, appWidgetId = appWidgetId)
                    else -> finish()
                }
            }
        }
    }
}

@Composable
fun BudgetInputDialog(onDismiss: () -> Unit, context: Context, appWidgetId: Int) {
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    val db = CalculatorDatabase.getDatabase(context)

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
        title = { Text("Add Budget Deduction") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note (Optional)") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amount = amountText.toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val current = db.budgetDao().getBudget()
                        val limit = current?.capAmount ?: 200.0
                        val spent = current?.spentAmount ?: 0.0
                        val curSym = current?.currencySymbol ?: "$"
                        
                        // Add to history
                        val historyEntry = BudgetHistoryEntity(
                            timestamp = System.currentTimeMillis(),
                            amountDeducted = amount,
                            note = noteText.ifEmpty { "Widget Deduction" }
                        )
                        db.budgetHistoryDao().insertBudgetHistory(historyEntry)
                        
                        // Wait, if spent is updated via a trigger or ViewModel, we should update it here too,
                        // or better yet, since the widget just reads spent, we update it immediately:
                        val newSpent = spent + amount
                        db.budgetDao().insertOrUpdateBudget(
                            BudgetLedgerEntity(1, limit, newSpent, System.currentTimeMillis(), curSym)
                        )

                        withContext(Dispatchers.Main) {
                            BudgetBurnWidget().updateAll(context)
                            onDismiss()
                        }
                    }
                } else {
                    onDismiss()
                }
            }) {
                Text("Deduct")
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
fun UnitInputDialog(onDismiss: () -> Unit, context: Context, appWidgetId: Int) {
    var amountText by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
        title = { Text("Set Input Value") },
        text = {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Value") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(onClick = {
                val amount = amountText.toDoubleOrNull()
                if (amount != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        // Update DataStore value
                        androidx.glance.appwidget.state.updateAppWidgetState(
                            context,
                            androidx.glance.state.PreferencesGlanceStateDefinition,
                            androidx.glance.appwidget.GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
                        ) { prefs ->
                            prefs.toMutablePreferences().apply { set(convertValKey, amount) }
                        }
                        UnitConversionWidget().updateAll(context)
                        withContext(Dispatchers.Main) {
                            onDismiss()
                        }
                    }
                } else {
                    onDismiss()
                }
            }) {
                Text("Set")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

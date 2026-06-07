package com.example.calculatorpro.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.*
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.layout.*
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.calculatorpro.MainActivity
import com.example.calculatorpro.data.database.CalculatorDatabase
import com.example.calculatorpro.data.model.BudgetLedgerEntity
import com.example.calculatorpro.data.model.WidgetSettingsEntity
import com.example.calculatorpro.data.repository.CalculatorRepositoryImpl
import com.example.calculatorpro.data.network.CurrencyApi
import com.example.calculatorpro.domain.usecase.UnitConversionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Preference Keys for Widget Interactions
val convertValKey = doublePreferencesKey("convert_value")

// Colors for dark minimalist theme
val WidgetBg = ColorProvider(Color(0xFF131313))
val AccentColor = ColorProvider(Color(0xFFFFB1C0))
val OnAccentColor = ColorProvider(Color(0xFF660029))
val WhiteText = ColorProvider(Color(0xFFE5E2E1))
val MutedText = ColorProvider(Color(0xFFAB888E))
val AccentGlow = ColorProvider(Color(0xFFFFB1C0))


// ==========================================
// 1. CURRENCY SNAPSHOT (Dynamic)
// ==========================================
class CurrencySnapshotWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val db = CalculatorDatabase.getDatabase(context)

            val settings = androidx.compose.runtime.produceState<WidgetSettingsEntity?>(initialValue = null) {
                value = db.widgetSettingsDao().getSettings()
            }.value

            val pairCodes = (settings?.currencyPairs ?: "INR,EUR,GBP")
                .split(",")
                .map { it.trim().uppercase() }
                .filter { it.isNotEmpty() }

            val rates = androidx.compose.runtime.produceState<Map<String, Double>>(initialValue = emptyMap()) {
                val result = mutableMapOf<String, Double>()
                for (code in pairCodes) {
                    val rate = db.currencyDao().getRate(code.lowercase())
                    if (rate != null) {
                        result[code] = rate.rateAgainstUsd
                    }
                }
                value = result
            }.value

            // Currency symbol lookup
            val currencySymbols = mapOf(
                "INR" to "₹", "EUR" to "€", "GBP" to "£", "JPY" to "¥",
                "CAD" to "C$", "AUD" to "A$", "CNY" to "¥"
            )
            val currencyEmojis = mapOf(
                "INR" to "💵", "EUR" to "💶", "GBP" to "💷", "JPY" to "💴",
                "CAD" to "💵", "AUD" to "💵", "CNY" to "💴"
            )

            // Build the reconfigure intent
            val configureIntent = Intent(context, MainActivity::class.java).apply {
                putExtra("WIDGET_CONFIGURE", "currency")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(WidgetBg)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💱 Currency Snapshot",
                        style = TextStyle(color = WhiteText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Button(
                        text = "⚙️",
                        onClick = actionStartActivity(configureIntent),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AccentColor, contentColor = OnAccentColor)
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    Button(
                        text = "Sync",
                        onClick = actionRunCallback<SyncRatesCallback>(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AccentColor, contentColor = OnAccentColor)
                    )
                }

                Spacer(modifier = GlanceModifier.height(4.dp))

                Column {
                    pairCodes.forEachIndexed { index, code ->
                        val rate = rates[code]
                        val sym = currencySymbols[code] ?: ""
                        val emoji = currencyEmojis[code] ?: "💵"
                        Text(
                            text = "$emoji 1 USD = $sym${String.format("%.2f", rate ?: 0.0)} $code",
                            style = TextStyle(color = AccentGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        )
                        if (index < pairCodes.size - 1) {
                            Spacer(modifier = GlanceModifier.height(2.dp))
                        }
                    }
                }
            }
        }
    }
}

class SyncRatesCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val database = CalculatorDatabase.getDatabase(context)
        val api = CurrencyApi()
        val repository = CalculatorRepositoryImpl(
            database.historyDao(),
            database.currencyDao(),
            database.budgetDao(),
            database.widgetSettingsDao(),
            database.budgetHistoryDao(),
            api
        )
        // Background thread call
        CoroutineScope(Dispatchers.IO).launch {
            repository.fetchAndSaveRates()
            CurrencySnapshotWidget().update(context, glanceId)
        }
    }
}

class CurrencySnapshotReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = CurrencySnapshotWidget()
}


// ==========================================
// 2. THE DAILY BUDGET BURN (4x1)
// ==========================================
class BudgetBurnWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val db = CalculatorDatabase.getDatabase(context)
            val budget = androidx.compose.runtime.produceState<BudgetLedgerEntity?>(initialValue = null) {
                value = db.budgetDao().getBudget()
            }.value

            val limit = budget?.capAmount ?: 200.0
            val spent = budget?.spentAmount ?: 0.0
            val left = (limit - spent).coerceAtLeast(0.0)
            val curSym = budget?.currencySymbol ?: "$"

            // Progress bar characters
            val totalBars = 10
            val filledBars = if (limit > 0) ((spent / limit) * totalBars).toInt().coerceIn(0, totalBars) else 0
            val emptyBars = totalBars - filledBars
            val progressStr = "█".repeat(filledBars) + "░".repeat(emptyBars)

            // Build the reconfigure intent
            val configureIntent = Intent(context, MainActivity::class.java).apply {
                putExtra("WIDGET_CONFIGURE", "budget")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(WidgetBg)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "💳 Budget: [$progressStr]",
                        style = TextStyle(color = MutedText, fontSize = 11.sp)
                    )
                    Text(
                        text = "$curSym${String.format("%.2f", left)} / $curSym${String.format("%.2f", limit)} Left",
                        style = TextStyle(color = WhiteText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        text = "⚙️",
                        onClick = actionStartActivity(configureIntent),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AccentColor, contentColor = OnAccentColor)
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    val inputIntent = Intent(context, WidgetInputDialogActivity::class.java).apply {
                        action = "ACTION_EDIT_BUDGET"
                        putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID, id.hashCode())
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    Button(
                        text = "✏️",
                        onClick = actionStartActivity(inputIntent),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AccentColor, contentColor = OnAccentColor)
                    )
                }
            }
        }
    }
}

// Removed DeductBudgetCallback as we use the transparent activity now

class BudgetBurnReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = BudgetBurnWidget()
}


// ==========================================
// 3. CUSTOM UNIT CONVERSION (4x1)
// ==========================================
class UnitConversionWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val db = CalculatorDatabase.getDatabase(context)

            val settings = androidx.compose.runtime.produceState<WidgetSettingsEntity?>(initialValue = null) {
                value = db.widgetSettingsDao().getSettings()
            }.value

            val prefs = currentState<Preferences>()
            val fromVal = prefs[convertValKey] ?: 1.0

            val category = settings?.conversionCategory ?: "Mass"
            val fromUnitLabel = settings?.conversionFromUnit ?: "Pounds"
            val toUnitLabel = settings?.conversionToUnit ?: "Kilograms"

            val toVal = UnitConversionUseCase().convert(fromVal, category, fromUnitLabel, toUnitLabel)

            // Build the reconfigure intent
            val configureIntent = Intent(context, MainActivity::class.java).apply {
                putExtra("WIDGET_CONFIGURE", "unit_converter")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(WidgetBg)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "⚖️ $category",
                        style = TextStyle(color = MutedText, fontSize = 11.sp)
                    )
                    Text(
                        text = "${String.format("%.2f", fromVal)} $fromUnitLabel = ${String.format("%.2f", toVal)} $toUnitLabel",
                        style = TextStyle(color = AccentGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        text = "⚙️",
                        onClick = actionStartActivity(configureIntent),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AccentColor, contentColor = OnAccentColor)
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    Button(
                        text = "⇄",
                        onClick = actionRunCallback<SwapWidgetConversionCallback>(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AccentColor, contentColor = OnAccentColor)
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    val inputIntent = Intent(context, WidgetInputDialogActivity::class.java).apply {
                        action = "ACTION_EDIT_UNIT"
                        putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID, id.hashCode())
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    Button(
                        text = "✏️",
                        onClick = actionStartActivity(inputIntent),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AccentColor, contentColor = OnAccentColor)
                    )
                }
            }
        }
    }
}

class SwapWidgetConversionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val db = CalculatorDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            val current = db.widgetSettingsDao().getSettings() ?: WidgetSettingsEntity()
            db.widgetSettingsDao().insertOrUpdateSettings(
                current.copy(
                    conversionFromUnit = current.conversionToUnit,
                    conversionToUnit = current.conversionFromUnit
                )
            )
            UnitConversionWidget().update(context, glanceId)
        }
    }
}

// Removed IncrementWidgetValCallback and DecrementWidgetValCallback as we use the transparent activity now

class UnitConversionReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = UnitConversionWidget()
}

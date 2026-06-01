package com.example.calculatorpro.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.*
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.layout.*
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.calculatorpro.data.database.CalculatorDatabase
import com.example.calculatorpro.data.model.BudgetLedgerEntity
import com.example.calculatorpro.data.repository.CalculatorRepositoryImpl
import com.example.calculatorpro.data.network.CurrencyApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.math.sin

// Preference Keys for Widget Interactions
val offsetKey = intPreferencesKey("visualizer_offset")
val basePriceKey = doublePreferencesKey("base_price")
val weightKey = doublePreferencesKey("weight_val")
val isLbsToKgKey = booleanPreferencesKey("lbs_to_kg")
val convertValKey = doublePreferencesKey("convert_value")

// Colors for dark minimalist theme
val WidgetBg = ColorProvider(Color(0xFF121212))
val AccentColor = ColorProvider(Color(0xFF2A5C91))
val WhiteText = ColorProvider(Color(0xFFE0E0E0))
val MutedText = ColorProvider(Color(0xFF8A8A8A))
val AccentGlow = ColorProvider(Color(0xFF5A9CF0))

// ==========================================
// 1. FUNCTION VISUALIZER (2x2)
// ==========================================
class FunctionVisualizerWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val offset = prefs[offsetKey] ?: 0

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(WidgetBg)
                    .padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "f(x) Wave Canvas",
                        style = TextStyle(color = WhiteText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    )
                }

                // Procedural textual graphical visualizer
                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Render sin wave lines using custom characters
                    for (i in 0 until 4) {
                        val angle = (i + offset) * 0.8
                        val sinValue = sin(angle)
                        val spaceCount = ((sinValue + 1.0) * 8).toInt().coerceIn(0, 16)
                        val spaces = " ".repeat(spaceCount)
                        Text(
                            text = "${spaces}●",
                            style = TextStyle(color = AccentGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                // Shift buttons (interaction)
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        text = "◀ Left",
                        onClick = actionRunCallback<ShiftWaveLeftCallback>(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AccentColor, contentColor = WhiteText)
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Button(
                        text = "Right ▶",
                        onClick = actionRunCallback<ShiftWaveRightCallback>(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AccentColor, contentColor = WhiteText)
                    )
                }
            }
        }
    }
}

class ShiftWaveLeftCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val current = prefs[offsetKey] ?: 0
            prefs.toMutablePreferences().apply { set(offsetKey, current - 1) }
        }
        FunctionVisualizerWidget().update(context, glanceId)
    }
}

class ShiftWaveRightCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val current = prefs[offsetKey] ?: 0
            prefs.toMutablePreferences().apply { set(offsetKey, current + 1) }
        }
        FunctionVisualizerWidget().update(context, glanceId)
    }
}

class FunctionVisualizerReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = FunctionVisualizerWidget()
}


// ==========================================
// 2. PRICE CALCULATOR (4x1)
// ==========================================
class PriceCalculatorWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val base = prefs[basePriceKey] ?: 120.0
            val weight = prefs[weightKey] ?: 1.0

            // Formula: base * weight + 18% tax - 10% discount
            val total = base * weight * 1.18 * 0.90

            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(WidgetBg)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rate: $${String.format("%.2f", base)} | Wt: ${String.format("%.1f", weight)}kg",
                        style = TextStyle(color = MutedText, fontSize = 11.sp)
                    )
                    Text(
                        text = "Total (Tax-Disc): $${String.format("%.2f", total)}",
                        style = TextStyle(color = AccentGlow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        text = "+$10",
                        onClick = actionRunCallback<AddPriceCallback>(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AccentColor, contentColor = WhiteText)
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    Button(
                        text = "+1kg",
                        onClick = actionRunCallback<AddWeightCallback>(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AccentColor, contentColor = WhiteText)
                    )
                }
            }
        }
    }
}

class AddPriceCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val current = prefs[basePriceKey] ?: 120.0
            val newVal = if (current >= 500.0) 10.0 else current + 10.0
            prefs.toMutablePreferences().apply { set(basePriceKey, newVal) }
        }
        PriceCalculatorWidget().update(context, glanceId)
    }
}

class AddWeightCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val current = prefs[weightKey] ?: 1.0
            val newVal = if (current >= 10.0) 1.0 else current + 1.0
            prefs.toMutablePreferences().apply { set(weightKey, newVal) }
        }
        PriceCalculatorWidget().update(context, glanceId)
    }
}

class PriceCalculatorReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = PriceCalculatorWidget()
}


// ==========================================
// 3. CURRENCY SNAPSHOT (4x2)
// ==========================================
class CurrencySnapshotWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val db = CalculatorDatabase.getDatabase(context)
            // Synchronously retrieve currency rates in Glance Compose scope
            val rateEntity = androidx.compose.runtime.produceState<Double?>(initialValue = 83.42) {
                db.currencyDao().getRate("inr").let { value = it?.rateAgainstUsd }
            }.value

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(WidgetBg)
                    .padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💱 USD ➔ INR Trend",
                        style = TextStyle(color = WhiteText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Button(
                        text = "Sync",
                        onClick = actionRunCallback<SyncRatesCallback>(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AccentColor, contentColor = WhiteText)
                    )
                }

                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = "₹${String.format("%.2f", rateEntity ?: 83.42)} (+0.14% Day)",
                    style = TextStyle(color = AccentGlow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )

                // Simple static ASCII mini trend graph representation
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = "Trend:   __/\\  /\\____",
                    style = TextStyle(color = MutedText, fontSize = 12.sp)
                )
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
// 4. THE DAILY BUDGET BURN (4x1)
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

            // Progress bar characters
            val totalBars = 10
            val filledBars = if (limit > 0) ((spent / limit) * totalBars).toInt().coerceIn(0, totalBars) else 0
            val emptyBars = totalBars - filledBars
            val progressStr = "█".repeat(filledBars) + "░".repeat(emptyBars)

            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(WidgetBg)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "💳 Budget: [$progressStr]",
                        style = TextStyle(color = MutedText, fontSize = 11.sp)
                    )
                    Text(
                        text = "$${String.format("%.2f", left)} / $${String.format("%.2f", limit)} Left",
                        style = TextStyle(color = WhiteText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                Button(
                    text = "Deduct $10",
                    onClick = actionRunCallback<DeductBudgetCallback>(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = AccentColor, contentColor = WhiteText)
                )
            }
        }
    }
}

class DeductBudgetCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val db = CalculatorDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            val current = db.budgetDao().getBudget()
            val limit = current?.capAmount ?: 200.0
            val spent = current?.spentAmount ?: 0.0
            val newSpent = if (spent + 10.0 > limit) 0.0 else spent + 10.0 // reset if over limit
            db.budgetDao().insertOrUpdateBudget(
                BudgetLedgerEntity(1, limit, newSpent, System.currentTimeMillis())
            )
            BudgetBurnWidget().update(context, glanceId)
        }
    }
}

class BudgetBurnReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = BudgetBurnWidget()
}


// ==========================================
// 5. CUSTOM UNIT CONVERSION (2x2)
// ==========================================
class UnitConversionWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val isLbsToKg = prefs[isLbsToKgKey] ?: true
            val fromVal = prefs[convertValKey] ?: 180.0

            val toVal = if (isLbsToKg) {
                fromVal * 0.45359237
            } else {
                fromVal / 0.45359237
            }

            val fromUnitLabel = if (isLbsToKg) "lbs" else "kg"
            val toUnitLabel = if (isLbsToKg) "kg" else "lbs"

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(WidgetBg)
                    .padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚖️ Conversion",
                        style = TextStyle(color = WhiteText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Button(
                        text = "⇄",
                        onClick = actionRunCallback<SwapWidgetConversionCallback>(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AccentColor, contentColor = WhiteText)
                    )
                }

                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(
                    text = "Input: ${String.format("%.1f", fromVal)} $fromUnitLabel",
                    style = TextStyle(color = WhiteText, fontSize = 13.sp)
                )
                Text(
                    text = "Result: ${String.format("%.2f", toVal)} $toUnitLabel",
                    style = TextStyle(color = AccentGlow, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = GlanceModifier.defaultWeight())
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        text = "-10",
                        onClick = actionRunCallback<DecrementWidgetValCallback>(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AccentColor, contentColor = WhiteText)
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Button(
                        text = "+10",
                        onClick = actionRunCallback<IncrementWidgetValCallback>(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AccentColor, contentColor = WhiteText)
                    )
                }
            }
        }
    }
}

class SwapWidgetConversionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val current = prefs[isLbsToKgKey] ?: true
            prefs.toMutablePreferences().apply { set(isLbsToKgKey, !current) }
        }
        UnitConversionWidget().update(context, glanceId)
    }
}

class IncrementWidgetValCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val current = prefs[convertValKey] ?: 180.0
            val newVal = if (current >= 500.0) 10.0 else current + 10.0
            prefs.toMutablePreferences().apply { set(convertValKey, newVal) }
        }
        UnitConversionWidget().update(context, glanceId)
    }
}

class DecrementWidgetValCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val current = prefs[convertValKey] ?: 180.0
            val newVal = if (current <= 10.0) 180.0 else current - 10.0
            prefs.toMutablePreferences().apply { set(convertValKey, newVal) }
        }
        UnitConversionWidget().update(context, glanceId)
    }
}

class UnitConversionReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = UnitConversionWidget()
}

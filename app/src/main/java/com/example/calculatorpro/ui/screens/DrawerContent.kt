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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculatorpro.ui.mvi.AppTheme
import com.example.calculatorpro.ui.mvi.CalculatorMode
import com.example.calculatorpro.widget.*

@Composable
fun DrawerContent(
    currentMode: CalculatorMode,
    currentTheme: AppTheme,
    onModeSelected: (CalculatorMode) -> Unit,
    onThemeSelected: (AppTheme) -> Unit
) {
    val context = LocalContext.current

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

            val widgets = listOf(
                Triple("Function Visualizer", FunctionVisualizerReceiver::class.java, Icons.Default.Star),
                Triple("Price Calculator Bar", PriceCalculatorReceiver::class.java, Icons.Default.Info),
                Triple("Currency Snapshot", CurrencySnapshotReceiver::class.java, Icons.Default.Refresh),
                Triple("Daily Budget Ledger", BudgetBurnReceiver::class.java, Icons.Default.ShoppingCart),
                Triple("Custom Unit Converter", UnitConversionReceiver::class.java, Icons.Default.List)
            )

            widgets.forEach { (name, receiverClass, icon) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .height(48.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable { pinWidget(context, receiverClass) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = name,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = name,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
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

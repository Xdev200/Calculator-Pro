package com.example.calculatorpro.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccent,
    background = DarkBackground,
    surface = DarkDisplayArea,
    onPrimary = Color.White,
    onBackground = DarkText,
    onSurface = DarkText,
    surfaceVariant = DarkKeyStandard,
    primaryContainer = DarkKeyOperator,
    secondaryContainer = DarkKeyScientific,
    onSurfaceVariant = DarkText,
    onPrimaryContainer = Color.White,
    onSecondaryContainer = DarkText
)

private val LightColorScheme = lightColorScheme(
    primary = LightAccent,
    background = LightBackground,
    surface = LightDisplayArea,
    onPrimary = Color.White,
    onBackground = LightText,
    onSurface = LightText,
    surfaceVariant = LightKeyStandard,
    primaryContainer = LightKeyOperator,
    secondaryContainer = LightKeyScientific,
    onSurfaceVariant = LightText,
    onPrimaryContainer = Color.White,
    onSecondaryContainer = LightText
)

@Composable
fun CalculatorProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

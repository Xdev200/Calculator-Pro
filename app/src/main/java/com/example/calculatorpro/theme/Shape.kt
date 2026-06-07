package com.example.calculatorpro.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(8.dp),      // 0.5rem for Buttons
    medium = RoundedCornerShape(12.dp),    // 0.75rem
    large = RoundedCornerShape(24.dp),     // 1.5rem for Display Container
    extraLarge = RoundedCornerShape(50.dp) // Large pill shape
)

package com.example.calculatorpro.domain.usecase

class UnitConversionUseCase {
    fun convert(
        value: Double,
        category: String,
        fromUnit: String,
        toUnit: String
    ): Double {
        if (fromUnit == toUnit) return value

        return when (category.lowercase()) {
            "weight", "mass" -> convertMass(value, fromUnit, toUnit)
            "length" -> convertLength(value, fromUnit, toUnit)
            "area" -> convertArea(value, fromUnit, toUnit)
            "volume" -> convertVolume(value, fromUnit, toUnit)
            "temperature" -> convertTemperature(value, fromUnit, toUnit)
            else -> value
        }
    }

    private fun convertMass(value: Double, from: String, to: String): Double {
        val inKg = when (from.lowercase()) {
            "kg", "kilograms" -> value
            "lbs", "pounds" -> value * 0.45359237
            "g", "grams" -> value / 1000.0
            "oz", "ounces" -> value * 0.0283495231
            else -> value
        }
        return when (to.lowercase()) {
            "kg", "kilograms" -> inKg
            "lbs", "pounds" -> inKg / 0.45359237
            "g", "grams" -> inKg * 1000.0
            "oz", "ounces" -> inKg / 0.0283495231
            else -> inKg
        }
    }

    private fun convertLength(value: Double, from: String, to: String): Double {
        val inMeters = when (from.lowercase()) {
            "m", "meters" -> value
            "km", "kilometers" -> value * 1000.0
            "cm", "centimeters" -> value / 100.0
            "inch", "inches" -> value * 0.0254
            "ft", "feet" -> value * 0.3048
            "mi", "miles" -> value * 1609.344
            else -> value
        }
        return when (to.lowercase()) {
            "m", "meters" -> inMeters
            "km", "kilometers" -> inMeters / 1000.0
            "cm", "centimeters" -> inMeters * 100.0
            "inch", "inches" -> inMeters / 0.0254
            "ft", "feet" -> inMeters / 0.3048
            "mi", "miles" -> inMeters / 1609.344
            else -> inMeters
        }
    }

    private fun convertArea(value: Double, from: String, to: String): Double {
        val inSqMeters = when (from.lowercase()) {
            "m²", "sq meters" -> value
            "km²", "sq kilometers" -> value * 1_000_000.0
            "ft²", "sq feet" -> value * 0.09290304
            "ac", "acres" -> value * 4046.8564224
            else -> value
        }
        return when (to.lowercase()) {
            "m²", "sq meters" -> inSqMeters
            "km²", "sq kilometers" -> inSqMeters / 1_000_000.0
            "ft²", "sq feet" -> inSqMeters / 0.09290304
            "ac", "acres" -> inSqMeters / 4046.8564224
            else -> inSqMeters
        }
    }

    private fun convertVolume(value: Double, from: String, to: String): Double {
        val inLiters = when (from.lowercase()) {
            "l", "liters" -> value
            "ml", "milliliters" -> value / 1000.0
            "gal", "gallons" -> value * 3.785411784
            "cup", "cups" -> value * 0.2365882365
            else -> value
        }
        return when (to.lowercase()) {
            "l", "liters" -> inLiters
            "ml", "milliliters" -> inLiters * 1000.0
            "gal", "gallons" -> inLiters / 3.785411784
            "cup", "cups" -> inLiters / 0.2365882365
            else -> inLiters
        }
    }

    private fun convertTemperature(value: Double, from: String, to: String): Double {
        val inCelsius = when (from.lowercase()) {
            "c", "celsius" -> value
            "f", "fahrenheit" -> (value - 32.0) * 5.0 / 9.0
            "k", "kelvin" -> value - 273.15
            else -> value
        }
        return when (to.lowercase()) {
            "c", "celsius" -> inCelsius
            "f", "fahrenheit" -> inCelsius * 9.0 / 5.0 + 32.0
            "k", "kelvin" -> inCelsius + 273.15
            else -> inCelsius
        }
    }
}

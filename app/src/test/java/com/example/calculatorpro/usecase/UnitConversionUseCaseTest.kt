package com.example.calculatorpro.usecase

import com.example.calculatorpro.domain.usecase.UnitConversionUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConversionUseCaseTest {

    private val conversionUseCase = UnitConversionUseCase()

    @Test
    fun testMassConversions() {
        // 100 lbs to kg
        val kg = conversionUseCase.convert(100.0, "mass", "Pounds", "Kilograms")
        assertEquals(45.359237, kg, 0.001)

        // 100 kg to lbs
        val lbs = conversionUseCase.convert(45.359237, "mass", "Kilograms", "Pounds")
        assertEquals(100.0, lbs, 0.001)
    }

    @Test
    fun testLengthConversions() {
        // 1 meter to feet
        val feet = conversionUseCase.convert(1.0, "length", "Meters", "Feet")
        assertEquals(3.28084, feet, 0.001)
    }

    @Test
    fun testTemperatureConversions() {
        // 100 Celsius to Fahrenheit
        val fahr = conversionUseCase.convert(100.0, "temperature", "Celsius", "Fahrenheit")
        assertEquals(212.0, fahr, 0.001)

        // 32 Fahrenheit to Celsius
        val cel = conversionUseCase.convert(32.0, "temperature", "Fahrenheit", "Celsius")
        assertEquals(0.0, cel, 0.001)
    }
}

package com.example.calculatorpro.usecase

import com.example.calculatorpro.domain.usecase.EvaluateExpressionUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluateExpressionUseCaseTest {

    private val evaluateUseCase = EvaluateExpressionUseCase()

    @Test
    fun testBasicArithmetic() {
        val result1 = evaluateUseCase.execute("2+2", isDegreeMode = false)
        assertEquals(4.0, result1, 0.0001)

        val result2 = evaluateUseCase.execute("10-3*2", isDegreeMode = false)
        assertEquals(4.0, result2, 0.0001)

        val result3 = evaluateUseCase.execute("(5+5)×2", isDegreeMode = false)
        assertEquals(20.0, result3, 0.0001)
    }

    @Test
    fun testTrigonometryAndLogarithms() {
        // Radians Mode: sin(pi / 2) = 1.0
        val sinRad = evaluateUseCase.execute("sin(π/2)", isDegreeMode = false)
        assertEquals(1.0, sinRad, 0.0001)

        // Degrees Mode: sin(90) = 1.0
        val sinDeg = evaluateUseCase.execute("sin(90)", isDegreeMode = true)
        assertEquals(1.0, sinDeg, 0.0001)

        val logVal = evaluateUseCase.execute("log(10, 100)", isDegreeMode = false)
        assertEquals(2.0, logVal, 0.0001)
    }

    @Test
    fun testDivisionByZeroAndErrors() {
        val divZero = evaluateUseCase.execute("5/0", isDegreeMode = false)
        assertTrue(divZero.isInfinite() || divZero.isNaN())

        val malformed = evaluateUseCase.execute("5+*2", isDegreeMode = false)
        assertTrue(malformed.isNaN())
    }
}

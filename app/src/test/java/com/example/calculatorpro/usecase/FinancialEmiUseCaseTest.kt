package com.example.calculatorpro.usecase

import com.example.calculatorpro.domain.usecase.FinancialEmiUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class FinancialEmiUseCaseTest {

    private val emiUseCase = FinancialEmiUseCase()

    @Test
    fun testCalculateEmiWithInterest() {
        // Principal: $10,000, Interest: 6% annual, Term: 12 months
        // EMI = 10000 * 0.005 * (1.005)^12 / ((1.005)^12 - 1) = 860.66
        val emi = emiUseCase.calculateEmi(10000.0, 6.0, 12)
        assertEquals(860.66, emi, 0.01)
    }

    @Test
    fun testCalculateEmiZeroInterest() {
        val emi = emiUseCase.calculateEmi(12000.0, 0.0, 12)
        assertEquals(1000.0, emi, 0.001)
    }

    @Test
    fun testCalculateEmiZeroTerm() {
        val emi = emiUseCase.calculateEmi(10000.0, 6.0, 0)
        assertEquals(0.0, emi, 0.001)
    }
}

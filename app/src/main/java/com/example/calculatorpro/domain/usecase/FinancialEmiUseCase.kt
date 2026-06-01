package com.example.calculatorpro.domain.usecase

import kotlin.math.pow

class FinancialEmiUseCase {
    fun calculateEmi(principal: Double, annualInterestRate: Double, termMonths: Int): Double {
        if (termMonths <= 0) return 0.0
        val monthlyRate = annualInterestRate / 12.0 / 100.0
        if (monthlyRate == 0.0) return principal / termMonths

        return principal * monthlyRate * (1.0 + monthlyRate).pow(termMonths) /
                ((1.0 + monthlyRate).pow(termMonths) - 1.0)
    }
}

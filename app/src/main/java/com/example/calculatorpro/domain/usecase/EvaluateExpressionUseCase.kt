package com.example.calculatorpro.domain.usecase

import org.mariuszgromada.math.mxparser.Expression
import org.mariuszgromada.math.mxparser.mXparser

class EvaluateExpressionUseCase {
    fun execute(expressionStr: String, isDegreeMode: Boolean): Double {
        if (isDegreeMode) {
            mXparser.setDegreesMode()
        } else {
            mXparser.setRadiansMode()
        }

        // Map math typography tokens to parser standard format symbols
        val formatted = expressionStr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", "pi")

        val expression = Expression(formatted)
        return expression.calculate()
    }
}

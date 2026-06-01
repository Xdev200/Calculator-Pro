package com.example.calculatorpro.data.network

import kotlinx.serialization.Serializable

@Serializable
data class CurrencyResponse(
    val date: String = "",
    val usd: Map<String, Double> = emptyMap()
)

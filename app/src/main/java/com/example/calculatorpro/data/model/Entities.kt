package com.example.calculatorpro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_table")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val expression: String,
    val result: String,
    val timestamp: Long
)

@Entity(tableName = "currency_rates_table")
data class CurrencyRateEntity(
    @PrimaryKey val currencyCode: String,
    val rateAgainstUsd: Double,
    val lastUpdated: Long
)

@Entity(tableName = "budget_ledger_table")
data class BudgetLedgerEntity(
    @PrimaryKey val id: Int = 1,
    val capAmount: Double,
    val spentAmount: Double,
    val lastUpdated: Long
)

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
    val lastUpdated: Long,
    val currencySymbol: String = "$"
)

@Entity(tableName = "widget_settings_table")
data class WidgetSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val conversionCategory: String = "Mass",
    val conversionFromUnit: String = "Pounds",
    val conversionToUnit: String = "Kilograms",
    val currencyPairs: String = "INR,EUR,GBP" // Comma-separated target currency codes (base is always USD)
)

@Entity(tableName = "budget_history_table")
data class BudgetHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val amountDeducted: Double,
    val note: String
)

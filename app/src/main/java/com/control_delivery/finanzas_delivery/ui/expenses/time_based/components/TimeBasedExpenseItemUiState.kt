package com.control_delivery.finanzas_delivery.ui.expenses.time_based.components

import androidx.compose.ui.graphics.Color

/**
 * State that defines the visual information of a single time-based expense card.
 * All formatting and logic calculations are done before reaching the UI.
 */
data class TimeBasedExpenseItemUiState(
    val id: String = "",
    val description: String = "",
    val daysLeftText: String = "",
    val daysLeftColor: Color = Color.Black,
    val generalProgress: Float = 0.0F,
    val generalProgressText: String = "",
    val dailyProgress: Float = 0.0F,
    val dailyProgressText: String = "",
    val savedAmountText: String = "",
    val generalGoalAmountText: String = "",
    val dailyGoalAmountText: String = "",
    val savedToday : String = ""
)

package com.control_delivery.finanzas_delivery.ui.expenses.distance_based

import androidx.compose.ui.graphics.Color

/**
 * State that defines the visual information of a single distance-based expense card.
 */
data class DistanceExpenseItemUiState(
    val id: String,
    val description: String,
    val costPerKmText: String,
    val isSavingsGoal: Boolean,
    val isGoalReached: Boolean = false,
    
    // For savings goals (Oil, etc.)
    val moneyProgress: Float = 0f,
    val moneyProgressText: String = "",
    val moneySavedText: String = "",
    val moneyGoalText: String = "",
    
    val kmProgress: Float = 0f,
    val kmProgressText: String = "",
    val kmCurrentText: String = "",
    val kmGoalText: String = "",
    
    // For pure deductions (Gasoline)
    val pricePerUnitText: String = "",
    val kmPerUnitText: String = "",
    
    val appliedToText: String = ""
)

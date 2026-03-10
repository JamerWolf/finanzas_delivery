package com.control_delivery.finanzas_delivery.data.repository

import com.control_delivery.finanzas_delivery.domain.model.DistanceBasedExpense
import com.control_delivery.finanzas_delivery.domain.model.DistanceExpenseType

object DistanceBasedExpensesFake {
    val expenses = mutableListOf(
        DistanceBasedExpense(
            description = "Gasoline",
            type = DistanceExpenseType.PureDeduction(
                pricePerUnit = 14000,
                kmPerUnit = 100.0
            )
        ),
        DistanceBasedExpense(
            description = "Engine Oil",
            type = DistanceExpenseType.SavingsGoal(
                targetAmount = 55000L,
                targetKm = 1500.0,
                accumulatedAmount = 0L,
                accumulatedKm = 0.0
            )
        )
    )
}

package com.control_delivery.finanzas_delivery.domain.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

data class DistanceBasedExpense(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val type: DistanceExpenseType,
    val appliedTo: List<java.lang.Class<out DistanceType>>,
    val isDeleted: Boolean = false
) {
    val costPerKm: Long get() = when(type) {
        is DistanceExpenseType.PureDeduction -> {
            if (type.kmPerUnit > 0) {
                BigDecimal.valueOf(type.pricePerUnit)
                    .divide(BigDecimal.valueOf(type.kmPerUnit), 0, RoundingMode.CEILING)
                    .toLong()
            } else 0L
        }
        is DistanceExpenseType.SavingsGoal -> {
            if (type.targetKm > 0) {
                BigDecimal.valueOf(type.targetAmount)
                    .divide(BigDecimal.valueOf(type.targetKm), 0, RoundingMode.CEILING)
                    .toLong()
            } else 0L
        }
    }

    /**
     * Checks if the savings goal or mileage limit has been reached.
     */
    fun isGoalReached(): Boolean {
        return when (val t = type) {
            is DistanceExpenseType.PureDeduction -> false
            is DistanceExpenseType.SavingsGoal -> {
                t.accumulatedAmount >= t.targetAmount || t.accumulatedKm >= t.targetKm
            }
        }
    }

    /**
     * Resets the accumulated amount and mileage for savings goals, preserving any surplus.
     */
    fun reset(): DistanceBasedExpense {
        return when (val t = type) {
            is DistanceExpenseType.PureDeduction -> this
            is DistanceExpenseType.SavingsGoal -> {
                this.copy(
                    type = t.copy(
                        accumulatedAmount = (t.accumulatedAmount - t.targetAmount).coerceAtLeast(0),
                        accumulatedKm = (t.accumulatedKm - t.targetKm).coerceAtLeast(0.0)
                    )
                )
            }
        }
    }
}



sealed class DistanceExpenseType {
    data class PureDeduction(
        val pricePerUnit: Long,
        val kmPerUnit: Double
    ) : DistanceExpenseType()

    data class SavingsGoal(
        val targetAmount: Long,
        val targetKm: Double,
        val accumulatedAmount: Long = 0,
        val accumulatedKm: Double = 0.0
    ) : DistanceExpenseType()
}

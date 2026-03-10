package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.DistanceExpenseType
import com.control_delivery.finanzas_delivery.domain.repository.DistanceBasedExpenseRepository
import kotlinx.coroutines.flow.first

class ApplyKmDeductionUseCase(
    private val repository: DistanceBasedExpenseRepository
) {
    /**
     * Applies KM-based deductions to the given amount using the total distance driven.
     * Since we now track a single continuous distance per Trip, all active expenses
     * apply to the total km — no more filtering by DistanceType.
     */
    suspend operator fun invoke(amount: Long, totalKm: Double): KmDeductionResult {
        val allExpenses = repository.getDistanceBasedExpenses().first()
        val activeExpenses = allExpenses.filter { !it.isDeleted }
        val breakdown = mutableMapOf<String, Long>()
        var totalDeduction = 0L

        val updatedExpenses = activeExpenses.map { expense ->
            if (totalKm > 0) {
                val deductionForThisItem = (totalKm * expense.costPerKm).toLong()

                totalDeduction += deductionForThisItem
                breakdown[expense.description] = deductionForThisItem

                if (expense.type is DistanceExpenseType.SavingsGoal) {
                    expense.copy(
                        type = expense.type.copy(
                            accumulatedAmount = expense.type.accumulatedAmount + deductionForThisItem,
                            accumulatedKm = expense.type.accumulatedKm + totalKm
                        )
                    )
                } else {
                    expense
                }
            } else {
                expense
            }
        }

        repository.updateExpenses(updatedExpenses)

        return KmDeductionResult(
            amountAfterDeduction = amount - totalDeduction,
            deductionAmount = totalDeduction,
            breakdown = breakdown
        )
    }
}

/**
 * Represents the result of applying the mileage filter.
 */
data class KmDeductionResult(
    val amountAfterDeduction: Long,
    val deductionAmount: Long,
    val breakdown: Map<String, Long>
)

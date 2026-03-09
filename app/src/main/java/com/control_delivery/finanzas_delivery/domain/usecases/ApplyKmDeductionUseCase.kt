package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.DistanceExpenseType
import com.control_delivery.finanzas_delivery.domain.model.DistanceType
import com.control_delivery.finanzas_delivery.domain.repository.DistanceBasedExpenseRepository
import kotlinx.coroutines.flow.first

class ApplyKmDeductionUseCase(
    private val repository: DistanceBasedExpenseRepository
) {
    suspend operator fun invoke(amount: Long, distances: List<DistanceType>): KmDeductionResult {
        val allExpenses = repository.getDistanceBasedExpenses().first()
        val breakdown = mutableMapOf<String, Long>()
        var totalDeduction = 0L

        val updatedExpenses = allExpenses.map { expense ->
            // Filter trayectos that apply to this expense
            val relevantKm = distances.filter { distance ->
                expense.appliedTo.any { it.isInstance(distance) }
            }.sumOf { it.value }
            
            if (relevantKm > 0) {
                val deductionForThisItem = (relevantKm * expense.costPerKm).toLong()
                
                totalDeduction += deductionForThisItem
                breakdown[expense.description] = deductionForThisItem

                // If it's a savings goal, we update accumulated values
                if (expense.type is DistanceExpenseType.SavingsGoal) {
                    expense.copy(
                        type = expense.type.copy(
                            accumulatedAmount = expense.type.accumulatedAmount + deductionForThisItem,
                            accumulatedKm = expense.type.accumulatedKm + relevantKm
                        )
                    )
                } else {
                    expense
                }
            } else {
                expense
            }
        }

        // Save changes in batch
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


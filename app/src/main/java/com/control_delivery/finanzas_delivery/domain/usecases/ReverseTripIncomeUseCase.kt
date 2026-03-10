package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.DistanceExpenseType
import com.control_delivery.finanzas_delivery.domain.model.Trip
import com.control_delivery.finanzas_delivery.domain.repository.DistanceBasedExpenseRepository
import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * Reverses the financial impact of a completed Trip on the expense repositories.
 * Used when a Trip or its Orders are updated or deleted, to subtract the previous
 * deductions before recalculating new ones.
 */
class ReverseTripIncomeUseCase(
    private val timeBasedExpenseRepository: TimeBasedExpenseRepository,
    private val distanceBasedExpenseRepository: DistanceBasedExpenseRepository
) {
    suspend operator fun invoke(trip: Trip) {
        Timber.d("Reversing financial impact for trip \${trip.id}")

        // 1. Reverse Time-based Expenses
        val tripDate = trip.toLocalDate()
        val allTimeExpenses = timeBasedExpenseRepository.getAllExpenses().first()
        trip.timeExpensesDeductionsBreakdown.forEach { (description, amount) ->
            val expense = allTimeExpenses.find { it.description == description }
            if (expense != null) {
                timeBasedExpenseRepository.subtractContribution(expense.id, amount, tripDate)
            } else {
                Timber.w("Could not find time-based expense with description '$description' to reverse.")
            }
        }

        // 2. Reverse Distance-based Expenses
        val allDistanceExpenses = distanceBasedExpenseRepository.getDistanceBasedExpenses().first()
        val updatedDistanceExpenses = allDistanceExpenses.map { expense ->
            val amountToReverse = trip.kmDeductionsBreakdown[expense.description]
            if (amountToReverse != null && expense.type is DistanceExpenseType.SavingsGoal) {
                expense.copy(
                    type = expense.type.copy(
                        accumulatedAmount = (expense.type.accumulatedAmount - amountToReverse).coerceAtLeast(0L),
                        accumulatedKm = (expense.type.accumulatedKm - trip.totalDistanceKm).coerceAtLeast(0.0)
                    )
                )
            } else {
                expense
            }
        }
        distanceBasedExpenseRepository.updateExpenses(updatedDistanceExpenses)
    }
}

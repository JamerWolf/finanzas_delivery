package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import kotlin.math.min

/**
 * Domain Filter: Takes an amount of money and distributes it equally
 * among the pending daily savings goals.
 */
class ApplyTimeBasedDeductionUseCase(
    private val repository: TimeBasedExpenseRepository
) {
    suspend operator fun invoke(amount: Double, today: LocalDate = LocalDate.now()): TimeBasedExpenseResult {
        var pool = amount // pool of available money to be distributed among daily goals.
        val allExpenses = repository.getAllExpenses().first()
        // TODO: Refactor, What the hell? I don't understand a damn thing.
        // TODO: create a single method to reset the daily contribution and all expenses
        var expensesToUpdate = allExpenses.map {
            it.syncDailyContribution(today).renew(today)
        }

        while (pool > 0.1) {
            val pendingExpenses = expensesToUpdate.filter {
                !it.isDeleted && it.getRemainingDailyQuota(today) > 0
            }
            if (pendingExpenses.isEmpty()) break

            val fairShare = pool / pendingExpenses.size
            var amountDistributedThisRound = 0.0

            expensesToUpdate = expensesToUpdate.map { expense ->
                val remainingQuota = expense.getRemainingDailyQuota(today)

                if (!expense.isDeleted && remainingQuota > 0) {
                    val amountToTake = min(fairShare, remainingQuota)

                    amountDistributedThisRound += amountToTake

                    expense.copy(
                        accumulatedAmount = expense.accumulatedAmount + amountToTake,
                        contributionToday = expense.contributionToday + amountToTake,
                        lastContributionTimestamp = System.currentTimeMillis()
                    )
                } else {
                    expense
                }
            }

            pool -= amountDistributedThisRound

            if (amountDistributedThisRound == 0.0) break
        }

        repository.updateExpenses(expensesToUpdate)

        return TimeBasedExpenseResult(
            amountAfterDeduction = pool,
            deductionAmount = amount - pool
        )
    }
}
data class TimeBasedExpenseResult(
    val amountAfterDeduction: Double,
    val deductionAmount: Double
)
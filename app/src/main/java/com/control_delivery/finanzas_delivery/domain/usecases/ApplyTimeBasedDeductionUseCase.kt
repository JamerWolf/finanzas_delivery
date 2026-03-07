package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

/**
 * Domain Filter: Takes an amount of money and distributes it equally
 * among the pending daily savings goals.
 */
class ApplyTimeBasedDeductionUseCase(
    private val repository: TimeBasedExpenseRepository
) {
    suspend operator fun invoke(amount: Long, today: LocalDate = LocalDate.now()): TimeBasedExpenseResult {
        var pool = amount // pool of available money to be distributed among daily goals.
        val allExpenses = repository.getAllExpenses().first()
        
        var expensesToUpdate = allExpenses.map {

            it.syncDailyContribution(today).renew(today)
        }

        while (pool > 0) {
            val pendingExpenses = expensesToUpdate.filter {
                !it.isDeleted && it.getRemainingDailyQuota(today) > 0
            }
            if (pendingExpenses.isEmpty()) break

            // Calculate fair share with ceiling rounding to ensure we cover the goal
            val fairShare = BigDecimal.valueOf(pool)
                .divide(BigDecimal.valueOf(pendingExpenses.size.toLong()), 0, RoundingMode.CEILING)
                .toLong()
            
            var amountDistributedThisRound = 0L

            expensesToUpdate = expensesToUpdate.map { expense ->
                val remainingQuota = expense.getRemainingDailyQuota(today)

                if (!expense.isDeleted && remainingQuota > 0) {
                    val amountToTake = minOf(fairShare, remainingQuota, pool)

                    amountDistributedThisRound += amountToTake
                    pool -= amountToTake

                    expense.copy(
                        accumulatedAmount = expense.accumulatedAmount + amountToTake,
                        contributionToday = expense.contributionToday + amountToTake,
                        lastContributionTimestamp = System.currentTimeMillis()
                    )
                } else {
                    expense
                }
            }

            if (amountDistributedThisRound == 0L) break
        }

        repository.updateExpenses(expensesToUpdate)

        return TimeBasedExpenseResult(
            amountAfterDeduction = pool,
            deductionAmount = amount - pool
        )
    }
}

data class TimeBasedExpenseResult(
    val amountAfterDeduction: Long,
    val deductionAmount: Long
)

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
        var pool = amount
        // The repository now handles synchronization automatically when getting expenses
        val allExpenses = repository.getAllExpenses().first()
        val breakdown = mutableMapOf<String, Long>()
        
        var expensesToUpdate = allExpenses

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
                    
                    val currentBreakdown = breakdown.getOrDefault(expense.description, 0L)
                    breakdown[expense.description] = currentBreakdown + amountToTake

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
            deductionAmount = amount - pool,
            breakdown = breakdown
        )
    }
}

data class TimeBasedExpenseResult(
    val amountAfterDeduction: Long,
    val deductionAmount: Long,
    val breakdown: Map<String, Long>
)


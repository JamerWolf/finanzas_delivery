package com.control_delivery.finanzas_delivery.domain.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.UUID

/**
 * Define the time at which an expense is repeated.
 */
sealed class ExpenseFrequency {
    object Daily : ExpenseFrequency()
    data class Weekly(val dayOfWeek: DayOfWeek) : ExpenseFrequency()
    data class Monthly(val dayOfMonth: Int) : ExpenseFrequency()
    data class Yearly(val dayOfMonth: Int, val month: java.time.Month) : ExpenseFrequency()
    data class Once(val timestamp: Long) : ExpenseFrequency()
}

/**
 * It represents a savings goal for a time-based expense.
 */
data class TimeBasedExpense(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val amount: Long,
    val accumulatedAmount: Long = 0,
    val frequency: ExpenseFrequency,
    val startTimestamp: Long,
    val isDeleted: Boolean = false,
    val nextDeadline: Long = calculateDeadline(startTimestamp, frequency),
    val currentCycleStart: Long = startTimestamp,
    val contributionToday: Long = 0,
    val lastContributionTimestamp: Long? = null
) {
    /**
     * Calculates the total savings quota assigned for this day.
     * Ignores any contributions made during the current day to maintain
     * consistency in net balance reports.
     */
    fun getFullDailyQuota(today: LocalDate): Long {
        val balancePendingAtStartOfDay = amount - (accumulatedAmount - contributionToday)
        val daysUntilDeadline = getDaysUntilDeadline(today)
        return BigDecimal.valueOf(balancePendingAtStartOfDay)
            .divide(BigDecimal.valueOf(daysUntilDeadline.toLong()), 0, RoundingMode.CEILING)
            .toLong()
    }

    /**
     * Calculate how much money you still need to save TODAY to reach the goal daily.
     * Uses the stable daily quota to ensure consistency with reports.
     */
    fun getRemainingDailyQuota(today: LocalDate): Long {
        val totalNeededToday = getFullDailyQuota(today)
        return (totalNeededToday - contributionToday).coerceAtLeast(0)
    }


    /**
     * Check if the day has changed since the last contribution.
     * If it is a new day, return a copy with the daily counter set to 0.
     */
    fun syncDailyContribution(today: LocalDate): TimeBasedExpense {
        val zoneId = ZoneId.systemDefault()
        val lastDate = lastContributionTimestamp?.let {
            Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate()
        }
        return if (lastDate == null || today.isAfter(lastDate)) {
            this.copy(contributionToday = 0)
        } else {
            this
        }
    }

    /**
     * Calculate the daily amount of the expense.
     */
    fun getDailyAmount(today: LocalDate): Long {
        val pendingAmount = amount - accumulatedAmount
        val daysUntilDeadline = getDaysUntilDeadline(today)
        return BigDecimal.valueOf(pendingAmount)
            .divide(BigDecimal.valueOf(daysUntilDeadline.toLong()), 0, RoundingMode.CEILING)
            .toLong()
    }

    /**
     * Calculate the number of days until the expense expires.
     */
    fun getDaysUntilDeadline(today: LocalDate): Int {
        val nextDeadlineDate = Instant.ofEpochMilli(nextDeadline)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return if (ChronoUnit.DAYS.between(today, nextDeadlineDate) <= 0) {
            1
        } else {
            ChronoUnit.DAYS.between(today, nextDeadlineDate).toInt()
        }
    }

    /**
     * Check if the expense is expired.
     */
    fun isExpired(today: LocalDate) = nextDeadline <= today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    /**
     * Renew the deadline expense and reset the accumulated amount.
     * Preserves any surplus from the previous cycle.
     * @return A copy of the expense with the new deadline or the same expense if it is not necessary.
     */
    fun renew(today: LocalDate): TimeBasedExpense {
        if (frequency !is ExpenseFrequency.Once && isExpired(today)) {
            return this.copy(
                contributionToday = 0,
                // We keep the surplus for the next cycle
                accumulatedAmount = (accumulatedAmount - amount).coerceAtLeast(0),
                nextDeadline = calculateDeadline(nextDeadline, frequency),
                currentCycleStart = nextDeadline
            )
        }
        return this
    }


    companion object{
        /**
         *Calculate the first deadline timestamp based solely on the time when
         * the expense was created (startTimestamp).
         */
        fun calculateDeadline(startTimestamp: Long, frequency: ExpenseFrequency): Long {
            val zoneId = ZoneId.systemDefault()
            // We convert the start timestamp to a local date to calculate
            val referenceDate = Instant.ofEpochMilli(startTimestamp)
                .atZone(zoneId)
                .toLocalDate()
            val deadlineDate = when (val f = frequency) {
                // Expires at the end of the same day it begins
                is ExpenseFrequency.Daily -> referenceDate.plusDays(1)

                // Find the next day of the week X (if today is X, return today)
                is ExpenseFrequency.Weekly -> {
                    referenceDate.with(TemporalAdjusters.next(f.dayOfWeek))
                }

                // Find day X of the month. If it has already passed, skip to the next month.
                is ExpenseFrequency.Monthly -> {
                    if (referenceDate.dayOfMonth < f.dayOfMonth) {
                        // Payday has not yet arrived this month.
                        safeWithDayOfMonth(referenceDate, f.dayOfMonth)
                    } else {
                        // Payday has already passed or is today, so we skip to the next month.
                        safeWithDayOfMonth(referenceDate.plusMonths(1), f.dayOfMonth)
                    }
                }
                // Find the exact month and day. If it has already passed this year, skip to the next one.
                is ExpenseFrequency.Yearly -> {
                    val targetThisYear = safeWithMonthDay(referenceDate, f.month, f.dayOfMonth)
                    if (referenceDate.isBefore(targetThisYear)) {
                        targetThisYear
                    } else {
                        safeWithMonthDay(referenceDate.plusYears(1), f.month, f.dayOfMonth)
                    }
                }

                // The expiration is the exact timestamp configured in the frequency.
                is ExpenseFrequency.Once -> {
                    Instant.ofEpochMilli(f.timestamp).atZone(zoneId).toLocalDate()
                }
            }
            // We return the start of that day (00:00:00) in milliseconds.
            return deadlineDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        }

        // Auxiliary functions for handling shorter months (e.g., April 31 -> April 30)
        private fun safeWithDayOfMonth(date: LocalDate, day: Int): LocalDate {
            return try { date.withDayOfMonth(day) }
            catch (e: Exception) { date.with(TemporalAdjusters.lastDayOfMonth()) }
        }
        private fun safeWithMonthDay(date: LocalDate, month: java.time.Month, day: Int): LocalDate {
            return try { date.withMonth(month.value).withDayOfMonth(day) }
            catch (e: Exception) { date.withMonth(month.value).with(TemporalAdjusters.lastDayOfMonth()) }
        }
    }
}
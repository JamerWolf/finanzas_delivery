package com.control_delivery.finanzas_delivery.domain.model

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
    val amount: Double,
    val accumulatedAmount: Double = 0.0,
    val frequency: ExpenseFrequency,
    val startTimestamp: Long,
    val isDeleted: Boolean = false,
    val nextDeadline: Long = calculateDeadline(startTimestamp, frequency)
) {
    /**
     * Calculate the daily amount of the expense.
     */
    fun getDailyAmount(today: LocalDate): Double{
        return (amount - accumulatedAmount) / getDaysUntilDeadline(today)
    }

    /**
     * Calculate the number of days until the expense expires.
     */
    fun getDaysUntilDeadline(today: LocalDate): Int {
        val nextDeadline = Instant.ofEpochMilli(nextDeadline)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return if (ChronoUnit.DAYS.between(today, nextDeadline) <= 0) {
            1
        } else {
            ChronoUnit.DAYS.between(today, nextDeadline).toInt()
        }
    }

    /**
     * Check if the expense is expired.
     */
    fun isExpired(today: LocalDate) = nextDeadline <= today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    /**
     * Renew the deadline expense and reset the accumulated amount.
     * @return A copy of the expense with the new deadline or the same expense if it is not necessary.
     */
    fun renew(today: LocalDate): TimeBasedExpense {
        if (frequency !is ExpenseFrequency.Once && isExpired(today)) {
            return this.copy(
                accumulatedAmount = 0.0,
                nextDeadline=calculateDeadline(nextDeadline, frequency))
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
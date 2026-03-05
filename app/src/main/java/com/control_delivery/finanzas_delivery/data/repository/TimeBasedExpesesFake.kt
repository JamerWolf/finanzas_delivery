package com.control_delivery.finanzas_delivery.data.repository

import com.control_delivery.finanzas_delivery.domain.model.ExpenseFrequency
import com.control_delivery.finanzas_delivery.domain.model.TimeBasedExpense
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val startOfToday = LocalDate.parse("2026-02-26")
    .atStartOfDay(ZoneId.systemDefault())
    .toInstant()
    .toEpochMilli()

object TimeBasedExpensesFake {
    val expenses = mutableListOf(
        TimeBasedExpense(
            description = "Cell Phone Bill",
            amount = 30000,
            accumulatedAmount = 0,
            frequency = ExpenseFrequency.Monthly(dayOfMonth = 1),
            startTimestamp = startOfToday
        ),
        TimeBasedExpense(
            description = "SOAT",
            amount = 343000,
            accumulatedAmount = 0,
            frequency = ExpenseFrequency.Yearly(dayOfMonth = 26, month = Month.NOVEMBER),
            startTimestamp = startOfToday
        ),
        TimeBasedExpense(
            description = "RTM",
            amount = 230000,
            accumulatedAmount = 0,
            frequency = ExpenseFrequency.Yearly(dayOfMonth = 26, month = Month.NOVEMBER),
            startTimestamp = startOfToday
        )
    )
}

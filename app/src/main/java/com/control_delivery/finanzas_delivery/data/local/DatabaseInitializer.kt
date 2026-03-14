package com.control_delivery.finanzas_delivery.data.local

import com.control_delivery.finanzas_delivery.domain.model.*
import com.control_delivery.finanzas_delivery.domain.repository.DistanceBasedExpenseRepository
import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository
import kotlinx.coroutines.flow.first
import java.time.Month
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseInitializer @Inject constructor(
    private val timeRepo: TimeBasedExpenseRepository,
    private val distanceRepo: DistanceBasedExpenseRepository
) {
    suspend fun initializeDefaults() {
        val currentTime = System.currentTimeMillis()

        // Initialize Time-Based Expenses
        val existingTimeExpenses = timeRepo.getAllExpenses().first()
        if (existingTimeExpenses.isEmpty()) {
            val defaults = listOf(
                TimeBasedExpense(
                    description = "SOAT",
                    amount = 343000,
                    frequency = ExpenseFrequency.Yearly(dayOfMonth = 26, month = Month.NOVEMBER),
                    startTimestamp = currentTime
                ),
                TimeBasedExpense(
                    description = "RTM",
                    amount = 250000,
                    frequency = ExpenseFrequency.Yearly(dayOfMonth = 20, month = Month.NOVEMBER),
                    startTimestamp = currentTime
                ),
                TimeBasedExpense(
                    description = "Plan celular",
                    amount = 30000,
                    frequency = ExpenseFrequency.Monthly(dayOfMonth = 6),
                    startTimestamp = currentTime
                )
            )
            defaults.forEach { timeRepo.addExpense(it) }
        }

        // Initialize Distance-Based Expenses
        val existingDistanceExpenses = distanceRepo.getDistanceBasedExpenses().first()
        if (existingDistanceExpenses.isEmpty()) {
            val defaults = listOf(
                DistanceBasedExpense(
                    description = "Gasolina",
                    type = DistanceExpenseType.PureDeduction(pricePerUnit = 14500, kmPerUnit = 120.0)
                ),
                DistanceBasedExpense(
                    description = "Aceite, Filtro, Mant",
                    type = DistanceExpenseType.SavingsGoal(targetAmount = 80000, targetKm = 2000.0)
                )
            )
            defaults.forEach { distanceRepo.saveExpense(it) }
        }
    }
}

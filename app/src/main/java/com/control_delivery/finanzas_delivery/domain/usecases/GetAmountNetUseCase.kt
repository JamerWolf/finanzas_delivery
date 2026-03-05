package com.control_delivery.finanzas_delivery.domain.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import timber.log.Timber

class GetAmountNetUseCase(
    private val getOrdersNetTotalUseCase: GetOrdersNetTotalUseCase,
    private val getTotalTimeBasedExpensesImpactUseCase: GetTotalTimeBasedExpensesImpactUseCase
) {
    /**
     * Calculate the net amount based on income and expenses impact.
     * @param startDate The start date of the date range.
     * @param endDate The end date of the date range.
     * @return A Flow emitting the net amount.
     */
    operator fun invoke(startDate: Long, endDate: Long): Flow<Double> {
        return combine(
            getOrdersNetTotalUseCase(startDate, endDate),
            getTotalTimeBasedExpensesImpactUseCase(startDate, endDate)
        ) { netIncomeFromOrders, expensesImpact ->
            netIncomeFromOrders - expensesImpact
        }
    }
}
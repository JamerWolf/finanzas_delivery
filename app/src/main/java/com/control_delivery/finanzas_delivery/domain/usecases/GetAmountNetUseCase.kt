package com.control_delivery.finanzas_delivery.domain.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetAmountNetUseCase(
    private val getOrdersAmountAfterKmUseCase: GetOrdersAmountAfterKmUseCase,
    private val getTotalTimeBasedExpensesImpactUseCase: GetTotalTimeBasedExpensesImpactUseCase
) {
    /**
     * Calculate the net amount based on income and expenses impact.
     * @param startDate The start date of the date range.
     * @param endDate The end date of the date range.
     * @return A Flow emitting the net amount.
     */
    operator fun invoke(startDate: Long, endDate: Long): Flow<Long> {
        return combine(
            getOrdersAmountAfterKmUseCase(startDate, endDate),
            getTotalTimeBasedExpensesImpactUseCase(startDate, endDate)
        ) { amountAfterKm, totalExpensesImpactDaily ->
           amountAfterKm - totalExpensesImpactDaily
        }
    }
}
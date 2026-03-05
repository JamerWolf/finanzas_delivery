package com.control_delivery.finanzas_delivery.domain.usecases

import timber.log.Timber

data class OrderProcessingResult(
    val kmDeduction: Double,
    val timeExpensesDeduction: Double,
    val finalNetProfit: Double
)

/**
 * Master Orchestrator: Coordinates the flow of money through expense filters.
 */
class ProcessOrderIncomeUseCase (
    private val applyKmDeduction: ApplyKmDeductionUseCase,
    private val applyTimeBasedDeduction: ApplyTimeBasedDeductionUseCase
) {
    /**
     * Processes the amount of an order and returns the result of each filter.
     * @param orderAmount The amount of the order.
     * @return The result of each filter.
     */
    suspend operator fun invoke(orderAmount: Double): OrderProcessingResult {
        val kmResult = applyKmDeduction(orderAmount)
        val finalNet = applyTimeBasedDeduction(kmResult.amountAfterDeduction)

        return OrderProcessingResult(
            kmDeduction = kmResult.deductionAmount,
            timeExpensesDeduction = finalNet.deductionAmount,
            finalNetProfit = finalNet.amountAfterDeduction
        )
    }
}
package com.control_delivery.finanzas_delivery.domain.usecases

import timber.log.Timber

data class OrderProcessingResult(
    val kmDeduction: Double,
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
     * Processes the amount of an order and returns the final net profit.
     */
    suspend operator fun invoke(orderAmount: Double): OrderProcessingResult {
        val kmResult = applyKmDeduction(orderAmount)
        val finalNet = applyTimeBasedDeduction(kmResult.amountAfterDeduction)
        Timber.d("Final net: $finalNet")

        return OrderProcessingResult(
            kmDeduction = kmResult.deductionAmount,
            finalNetProfit = finalNet
        )
    }
}
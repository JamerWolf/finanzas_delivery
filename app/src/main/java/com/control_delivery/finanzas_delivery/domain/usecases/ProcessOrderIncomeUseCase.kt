package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.DistanceType

data class OrderProcessingResult(
    val kmDeduction: Long,
    val timeExpensesDeduction: Long,
    val finalNetProfit: Long,
    val kmDeductionsBreakdown: Map<String, Long>,
    val timeExpensesDeductionsBreakdown: Map<String, Long>
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
     * @param distances The distances traveled in the order.
     * @return The result of each filter.
     */
    suspend operator fun invoke(orderAmount: Long, distances: List<DistanceType>): OrderProcessingResult {
        val kmResult = applyKmDeduction(orderAmount, distances)
        val timeBasedResult = applyTimeBasedDeduction(kmResult.amountAfterDeduction)

        return OrderProcessingResult(
            kmDeduction = kmResult.deductionAmount,
            timeExpensesDeduction = timeBasedResult.deductionAmount,
            finalNetProfit = timeBasedResult.amountAfterDeduction,
            kmDeductionsBreakdown = kmResult.breakdown,
            timeExpensesDeductionsBreakdown = timeBasedResult.breakdown
        )
    }
}

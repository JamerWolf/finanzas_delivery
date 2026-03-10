package com.control_delivery.finanzas_delivery.domain.usecases

/**
 * Result of processing a completed Trip's income through all expense filters.
 */
data class TripProcessingResult(
    val kmDeduction: Long,
    val timeExpensesDeduction: Long,
    val finalNetProfit: Long,
    val kmDeductionsBreakdown: Map<String, Long>,
    val timeExpensesDeductionsBreakdown: Map<String, Long>
)

/**
 * Master Orchestrator: Coordinates the flow of money through expense filters at the Trip level.
 * When a Trip completes, sum all order amounts → apply KM deduction on total trip distance
 * → apply time-based deduction on the remainder.
 */
class ProcessTripIncomeUseCase(
    private val applyKmDeduction: ApplyKmDeductionUseCase,
    private val applyTimeBasedDeduction: ApplyTimeBasedDeductionUseCase
) {
    /**
     * Processes the total income of a completed trip.
     * @param totalAmount Sum of all order amounts in the trip.
     * @param totalDistanceKm Total GPS-tracked distance for the trip.
     * @return The result of each deduction filter.
     */
    suspend operator fun invoke(totalAmount: Long, totalDistanceKm: Double): TripProcessingResult {
        val kmResult = applyKmDeduction(totalAmount, totalDistanceKm)
        val timeBasedResult = applyTimeBasedDeduction(kmResult.amountAfterDeduction)

        return TripProcessingResult(
            kmDeduction = kmResult.deductionAmount,
            timeExpensesDeduction = timeBasedResult.deductionAmount,
            finalNetProfit = timeBasedResult.amountAfterDeduction,
            kmDeductionsBreakdown = kmResult.breakdown,
            timeExpensesDeductionsBreakdown = timeBasedResult.breakdown
        )
    }
}

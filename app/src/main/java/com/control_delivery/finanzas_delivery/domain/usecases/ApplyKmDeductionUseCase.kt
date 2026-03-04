package com.control_delivery.finanzas_delivery.domain.usecases

class ApplyKmDeductionUseCase {
    operator fun invoke(amount: Double): KmDeductionResult {
        //TODO: Not implemented yet, value symbolic
        val deductionAmount = (amount * 0.1)
        return KmDeductionResult(
            amountAfterDeduction = amount - deductionAmount,
            deductionAmount = deductionAmount
        )
    }
}

/**
 * Represents the result of applying the mileage filter.
 */
data class KmDeductionResult(
    val amountAfterDeduction: Double,
    val deductionAmount: Double
)
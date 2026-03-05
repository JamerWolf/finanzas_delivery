package com.control_delivery.finanzas_delivery.domain.usecases

import java.math.BigDecimal
import java.math.RoundingMode

class ApplyKmDeductionUseCase {
    operator fun invoke(amount: Long): KmDeductionResult {
        //TODO: Not implemented yet, value symbolic
        val deductionAmount = BigDecimal.valueOf(amount)
            .multiply(BigDecimal.valueOf(0.1))
            .setScale(0, RoundingMode.CEILING)
            .toLong()
        
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
    val amountAfterDeduction: Long,
    val deductionAmount: Long
)

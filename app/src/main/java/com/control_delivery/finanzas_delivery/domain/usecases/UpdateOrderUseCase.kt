package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.DistanceType
import com.control_delivery.finanzas_delivery.domain.model.Order
import com.control_delivery.finanzas_delivery.domain.repository.OrderRepository
import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class UpdateOrderUseCase(
    private val orderRepository: OrderRepository,
    private val timeBasedExpenseRepository: TimeBasedExpenseRepository,
    private val processOrderIncomeUseCase: ProcessOrderIncomeUseCase
) {
    suspend operator fun invoke(
        oldOrderId: String,
        updatedPlatform: String,
        updatedAddress: String,
        updatedAmount: Long,
        updatedDistances: List<DistanceType>
    ) {
        val oldOrder = orderRepository.getOrderById(oldOrderId).first() ?: return
        val allExpenses = timeBasedExpenseRepository.getAllExpenses().first()

        // 1. Revert old order impact on time-based expenses
        oldOrder.timeExpensesDeductionsBreakdown.forEach { (expenseName, amount) ->
            val expense = allExpenses.find { it.description == expenseName }
            if (expense != null && oldOrder.timestamp >= expense.currentCycleStart) {
                timeBasedExpenseRepository.subtractContribution(expense.id, amount)
            }
        }

        // 2. Process new income with new values
        val result = processOrderIncomeUseCase(updatedAmount, updatedDistances)

        // 3. Create updated order
        val updatedOrder = oldOrder.copy(
            platform = updatedPlatform,
            customerAddress = updatedAddress,
            totalAmount = updatedAmount,
            kmDeduction = result.kmDeduction,
            timeExpensesDeduction = result.timeExpensesDeduction,
            kmDeductionsBreakdown = result.kmDeductionsBreakdown,
            timeExpensesDeductionsBreakdown = result.timeExpensesDeductionsBreakdown,
            distances = updatedDistances,
            amountAfterKmDeduction = updatedAmount - result.kmDeduction,
            netAmount = updatedAmount - (result.kmDeduction + result.timeExpensesDeduction)
        )

        // 4. Persist update
        orderRepository.updateOrder(updatedOrder)
    }
}

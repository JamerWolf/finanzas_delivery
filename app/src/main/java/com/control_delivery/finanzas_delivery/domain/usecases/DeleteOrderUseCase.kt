package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.repository.OrderRepository
import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class DeleteOrderUseCase(
    private val orderRepository: OrderRepository,
    private val timeBasedExpenseRepository: TimeBasedExpenseRepository
) {
    suspend operator fun invoke(orderId: String) {
        val order = orderRepository.getOrderById(orderId).first() ?: return
        val today = LocalDate.now()

        // Reverse Time-based savings
        val allExpenses = timeBasedExpenseRepository.getAllExpenses().first()
        
        order.timeExpensesDeductionsBreakdown.forEach { (expenseName, amount) ->
            val expense = allExpenses.find { it.description == expenseName }
            
            // Only subtract if the order belongs to the current cycle of the expense
            if (expense != null && order.timestamp >= expense.currentCycleStart) {
                timeBasedExpenseRepository.subtractContribution(expense.id, amount)
            }
        }

        // Delete the order
        orderRepository.deleteOrder(orderId)
    }
}

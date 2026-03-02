package com.control_delivery.finanzas_delivery.domain.usecases

import com.control_delivery.finanzas_delivery.domain.model.Order
import com.control_delivery.finanzas_delivery.domain.repository.OrderRepository
import com.control_delivery.finanzas_delivery.domain.repository.TimeBasedExpenseRepository

class AddOrderUseCase (
    private val orderRepository: OrderRepository,
    private val timeBasedExpenseRepository: TimeBasedExpenseRepository
) {
    suspend operator fun invoke(order: Order): String {
        SyncTimeBasedExpensesUseCase(timeBasedExpenseRepository)
        timeBasedExpenseRepository.syncExpenses()
        return orderRepository.addOrder(order)
    }
}
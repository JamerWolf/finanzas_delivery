package com.control_delivery.finanzas_delivery.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

data class Order(
    val id: String = UUID.randomUUID().toString(),
    val platform: String,
    val customerAddress: String,
    val totalAmount: Long,
    val status: OrderStatus = OrderStatus.ON_THE_WAY_TO_RECEIVE,
    val timestamp: Long = System.currentTimeMillis(),
    val kmDeduction: Long = 0,
    val timeExpensesDeduction: Long = 0,
    val amountAfterKmDeduction: Long = totalAmount - kmDeduction,
    val netAmount: Long = totalAmount - (kmDeduction + timeExpensesDeduction),
    val isDeleted: Boolean = false
) {
    init {
        require(totalAmount >= 0) { "Total amount cannot be negative" }
    }

    fun toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    }
}

enum class OrderStatus {
    ON_THE_WAY_TO_RECEIVE,
    RECEIVED,
    ON_THE_WAY_TO_DELIVERY,
    DELIVERED,
    CANCELLED
}

package com.control_delivery.finanzas_delivery.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

data class Order(
    val id: String = UUID.randomUUID().toString(),
    val platform: String,
    val customerAddress: String,
    val totalAmount: Double,
    val status: OrderStatus = OrderStatus.ON_THE_WAY_TO_RECEIVE,
    val timestamp: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
) {
    init {
        require(totalAmount >= 0.0) { "Total amount must be non-negative" }
    }

    override fun toString(): String {
        return """
            Order(
                id='$id',
                platform='$platform',
                customerAddress='$customerAddress',
                totalAmount=$totalAmount,
                status=$status,
                isDeleted=$isDeleted
            )
        """.trimIndent()
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

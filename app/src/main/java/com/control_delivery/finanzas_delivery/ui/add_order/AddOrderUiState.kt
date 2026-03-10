package com.control_delivery.finanzas_delivery.ui.add_order

data class AddOrderUiState(
    val orderId: String? = null,
    val platform: String = "",
    val address: String = "",
    val amount: String = "",
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val errorMessage: String? = null,
) {

    val isFormValid: Boolean
        get() = (platform.isNotBlank() && address.isNotBlank() && amount.isNotBlank()
                && (amount.toDoubleOrNull() ?: 0.0) > 0.0)
}

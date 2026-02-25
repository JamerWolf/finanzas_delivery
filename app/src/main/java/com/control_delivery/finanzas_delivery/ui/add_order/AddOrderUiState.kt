package com.control_delivery.finanzas_delivery.ui.add_order

data class AddOrderUiState(
    val platform: String = "",
    val address: String = "",
    val amount: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    val isFormValid: Boolean
        get() = (platform.isNotBlank() && address.isNotBlank() && amount.isNotBlank()
                && (amount.toDoubleOrNull() ?: 0.0) > 0.0)
}
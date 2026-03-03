package com.control_delivery.finanzas_delivery.ui.expenses.distance_based

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DistanceExpensesViewModel @Inject constructor() : ViewModel() {
    var uiState by mutableStateOf(DistanceExpensesUiState())
        private set
    // TODO: The logic for calculating per KM will be implemented here in the future.
}
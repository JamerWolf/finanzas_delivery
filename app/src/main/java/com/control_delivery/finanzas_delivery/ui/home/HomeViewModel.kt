package com.control_delivery.finanzas_delivery.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.control_delivery.finanzas_delivery.domain.model.OrderStatus
import com.control_delivery.finanzas_delivery.domain.usecases.GetOrdersFlowUseCase
import com.control_delivery.finanzas_delivery.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getOrdersFlowUseCase: GetOrdersFlowUseCase
): ViewModel() {
    var uiState by mutableStateOf(HomeScreenUiState())
    private set

    init {
        viewModelScope.launch {
            val (start, end) = DateUtils.getTimestampRange("TODAY")
            getOrdersFlowUseCase(
                List(1) {OrderStatus.DELIVERED},
                start, end
            ).collect {
                uiState = uiState.copy(ordersDeliveredToday = it)
            }
        }
    }

    fun onAddOrderClick() {
        uiState = uiState.copy(isAddOrderDialogVisible = true)
    }
    fun onDismissDialog() {
        uiState = uiState.copy(isAddOrderDialogVisible = false)
    }
}
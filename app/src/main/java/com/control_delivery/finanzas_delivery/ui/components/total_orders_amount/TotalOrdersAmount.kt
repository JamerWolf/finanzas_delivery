package com.control_delivery.finanzas_delivery.ui.components.total_orders_amount

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.control_delivery.finanzas_delivery.ui.theme.Finanzas_deliveryTheme

@Composable
fun TotalOrdersAmount(
    modifier: Modifier = Modifier,
    totalOrdersAmountViewModel: OrdersTotalAmountViewModel = hiltViewModel(),
    totalOrdersAmountType : TotalOrdersAmountType
    ) {
    val state = totalOrdersAmountViewModel.uiState

    Text(
        text = when (totalOrdersAmountType) {
            TotalOrdersAmountType.DAILY -> state.totalAmountDay
            TotalOrdersAmountType.WEEKLY -> state.totalAmountWeek },
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
fun TotalOrdersAmountPreview() {
    Finanzas_deliveryTheme {
        TotalOrdersAmount(totalOrdersAmountType = TotalOrdersAmountType.DAILY)
    }
}

enum class TotalOrdersAmountType {
    DAILY,
    WEEKLY
}

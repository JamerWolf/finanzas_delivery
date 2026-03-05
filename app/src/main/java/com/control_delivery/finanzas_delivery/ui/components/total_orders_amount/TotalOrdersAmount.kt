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
    totalOrdersAmountType: TotalOrdersAmountType
) {
    val state = totalOrdersAmountViewModel.uiState
    val amount = when (totalOrdersAmountType) {
        TotalOrdersAmountType.DAILY -> state.totalAmountDay
        TotalOrdersAmountType.WEEKLY -> state.totalAmountWeek
    }

    TotalOrdersAmountContent(
        amount = amount,
        modifier = modifier
    )
}

@Composable
fun TotalOrdersAmountContent(
    amount: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = amount,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
fun TotalOrdersAmountPreview() {
    Finanzas_deliveryTheme {
        TotalOrdersAmountContent(amount = "$ 10.000,00")
    }
}

enum class TotalOrdersAmountType {
    DAILY,
    WEEKLY
}

package com.control_delivery.finanzas_delivery.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.control_delivery.finanzas_delivery.domain.model.Order
import com.control_delivery.finanzas_delivery.ui.add_order.AddOrderDialog
import com.control_delivery.finanzas_delivery.ui.components.total_orders_amount.OrdersTotalAmountViewModel
import com.control_delivery.finanzas_delivery.ui.components.total_orders_amount.TotalOrdersAmountContent
import com.control_delivery.finanzas_delivery.ui.theme.Finanzas_deliveryTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel(),
    totalViewModel: OrdersTotalAmountViewModel = hiltViewModel(),
    onOrderClick: (String) -> Unit
) {
    HomeScreenContent(
        state = homeViewModel.uiState,
        dailyTotal = totalViewModel.uiState.totalAmountDay,
        weeklyTotal = totalViewModel.uiState.totalAmountWeek,
        onAddOrderClick = { homeViewModel.onAddOrderClick() },
        onDismissDialog = { homeViewModel.onDismissDialog() },
        onOrderClick = onOrderClick,
        modifier = modifier
    )
}

@Composable
fun HomeScreenContent(
    state: HomeScreenUiState,
    dailyTotal: String,
    weeklyTotal: String,
    onAddOrderClick: () -> Unit,
    onDismissDialog: () -> Unit,
    onOrderClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = onAddOrderClick) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Order")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Header (Totals)
            Row(
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Today", style = MaterialTheme.typography.labelMedium)
                        TotalOrdersAmountContent(amount = dailyTotal)
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(text = "Weekly", style = MaterialTheme.typography.labelMedium)
                        TotalOrdersAmountContent(amount = weeklyTotal)
                    }
                }
            }

            Text(
                text = "Orders Delivered Today",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(state.ordersDeliveredToday, key = { it.id }) { order ->
                    OrderItem(
                        order = order,
                        onClick = { onOrderClick(order.id) }
                    )
                }
            }

            if (state.isAddOrderDialogVisible) {
                AddOrderDialog(
                    onDismiss = onDismissDialog
                )
            }
        }
    }
}

@Composable
fun OrderItem(
    order: Order,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val formattedAmount = NumberFormat.getNumberInstance(Locale.GERMANY).format(order.netAmount)

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = order.platform.uppercase(),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            supportingContent = {
                Text(
                    text = order.customerAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            },
            trailingContent = {
                Text(
                    text = formattedAmount,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            },
            leadingContent = {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = order.platform.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    Finanzas_deliveryTheme {
        HomeScreenContent(
            state = HomeScreenUiState(
                ordersDeliveredToday = listOf(
                    Order(platform = "DIDI", customerAddress = "Calle 123", totalAmount = 5000),
                    Order(platform = "RAPPI", customerAddress = "Av. Siempre Viva", totalAmount = 3500)
                )
            ),
            dailyTotal = "$ 8.500,00",
            weeklyTotal = "$ 45.000,00",
            onAddOrderClick = {},
            onDismissDialog = {},
            onOrderClick = {}
        )
    }
}

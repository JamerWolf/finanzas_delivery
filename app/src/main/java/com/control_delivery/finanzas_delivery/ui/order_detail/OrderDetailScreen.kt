package com.control_delivery.finanzas_delivery.ui.order_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.control_delivery.finanzas_delivery.ui.theme.Finanzas_deliveryTheme
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBackClick: () -> Unit,
    viewModel: OrderDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }
    val state = viewModel.uiState
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error, color = MaterialTheme.colorScheme.error)
            }
        } else {
            OrderDetailContent(state = state, modifier = Modifier.padding(padding))
        }
    }
}
@Composable
fun OrderDetailContent(
    state: OrderDetailUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // General Information Card
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text(state.platform, fontWeight = FontWeight.Bold) },
                supportingContent = { Text(state.dateText) },
                leadingContent = {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(state.platform.take(1), style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            )
        }
        // Address Card
        AddressCard(address = state.address, km = state.km)
        // Financial Breakdown Card
        FinancialBreakdownCard(
            gross = state.grossAmount,
            deduction = state.kmDeduction,
            net = state.netAmount,
            timeExpensesAmount = state.timeExpensesAmount
        )
    }
}
@Composable
fun FinancialBreakdownCard(
    gross: String,
    deduction: String,
    net: String,
    timeExpensesAmount: String
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Profit Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Gross Income")
                Text(gross)
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Vehicle Expenses (KM)")
                Text("- $deduction", color = MaterialTheme.colorScheme.error)
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Time Expenses")
                Text("- $timeExpensesAmount", color = MaterialTheme.colorScheme.error)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Net Profit", fontWeight = FontWeight.Bold)
                Text(net, color = Color(0xFF2E7D32), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
@Composable
fun AddressCard(address: String, km: String) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text("Delivery Address", style = MaterialTheme.typography.labelMedium) },
            supportingContent = { Text(address, style = MaterialTheme.typography.bodyLarge) },
            leadingContent = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            trailingContent = { Text("$km km") }
        )
    }
}
@Preview(showBackground = true)
@Composable
fun OrderDetailPreview() {
    Finanzas_deliveryTheme {
        OrderDetailContent(
            state = OrderDetailUiState(
                platform = "DIDI",
                address = "Street 123 #45-67",
                dateText = "26 Feb 2026, 14:30",
                grossAmount = "$ 5.000,00",
                kmDeduction = "$ 500,00",
                netAmount = "$ 3.000,00",
                km = "3.2",
                timeExpensesAmount = "$ 1.500,00",
                isLoading = false
            )
        )
    }
}
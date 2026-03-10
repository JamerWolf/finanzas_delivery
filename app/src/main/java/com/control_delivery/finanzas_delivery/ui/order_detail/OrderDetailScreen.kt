package com.control_delivery.finanzas_delivery.ui.order_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.control_delivery.finanzas_delivery.ui.theme.Finanzas_deliveryTheme
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.control_delivery.finanzas_delivery.ui.add_order.AddOrderDialog

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
                },
                actions = {
                    IconButton(onClick = { viewModel.showEditDialog() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Order")
                    }
                    IconButton(onClick = { viewModel.showDeleteDialog() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Order")
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

        if (state.isEditDialogVisible && state.order != null) {
            AddOrderDialog(
                orderToEdit = state.order,
                onDismiss = { viewModel.dismissEditDialog() }
            )
        }

        if (state.isDeleteDialogVisible) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDeleteDialog() },
                title = { Text("Delete Order") },
                text = { Text("Are you sure you want to delete this order? Associated savings will be reversed if they belong to the current cycle.") },
                confirmButton = {
                    Button(
                        onClick = { 
                            viewModel.deleteOrder(orderId, onSuccess = onBackClick)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
@Composable
fun OrderDetailContent(
    state: OrderDetailUiState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
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
        AddressCard(
            address = state.address, 
            totalKm = state.km,
            pickupKm = state.pickupKm,
            deliveryKm = state.deliveryKm
        )
        // Financial Breakdown Card
        FinancialBreakdownCard(
            gross = state.grossAmount,
            deduction = state.kmDeduction,
            net = state.netAmount,
            timeExpensesAmount = state.timeExpensesAmount,
            kmBreakdown = state.kmDeductionsBreakdown,
            timeBreakdown = state.timeExpensesBreakdown,
            deletedTimeExpenseNames = state.deletedTimeExpenseNames,
            deletedKmExpenseNames = state.deletedKmExpenseNames
        )
    }
}
@Composable
fun FinancialBreakdownCard(
    gross: String,
    deduction: String,
    net: String,
    timeExpensesAmount: String,
    kmBreakdown: Map<String, String>,
    timeBreakdown: Map<String, String>,
    deletedTimeExpenseNames: Set<String> = emptySet(),
    deletedKmExpenseNames: Set<String> = emptySet()
) {
    var showDeletedInfoDialog by remember { mutableStateOf(false) }

    if (showDeletedInfoDialog) {
        AlertDialog(
            onDismissRequest = { showDeletedInfoDialog = false },
            title = { Text("Deleted Expense/Goal") },
            text = { Text("This expense or savings goal was deleted. It no longer applies to new orders, but historical deductions remain for accurate records.") },
            confirmButton = {
                TextButton(onClick = { showDeletedInfoDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

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
            
            // KM Breakdown
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
            Text("Vehicle Expenses (KM)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            kmBreakdown.forEach { (description, amount) ->
                val isDeleted = description in deletedKmExpenseNames
                Row(Modifier.fillMaxWidth().padding(start = 8.dp), Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(description, style = MaterialTheme.typography.bodySmall)
                        if (isDeleted) {
                            Text(
                                text = " *",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { showDeletedInfoDialog = true }
                            )
                        }
                    }
                    Text("- $amount", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Total KM Deduction", fontWeight = FontWeight.SemiBold)
                Text("- $deduction", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
            }

            // Time Breakdown
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
            Text("Savings Goals (Time)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            timeBreakdown.forEach { (description, amount) ->
                val isDeleted = description in deletedTimeExpenseNames
                Row(Modifier.fillMaxWidth().padding(start = 8.dp), Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(description, style = MaterialTheme.typography.bodySmall)
                        if (isDeleted) {
                            Text(
                                text = " *",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { showDeletedInfoDialog = true }
                            )
                        }
                    }
                    Text("- $amount", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Total Savings", fontWeight = FontWeight.SemiBold)
                Text("- $timeExpensesAmount", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant, thickness = 1.dp)
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Net Profit", fontWeight = FontWeight.Bold)
                Text(net, color = Color(0xFF2E7D32), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}


@Composable
fun AddressCard(address: String, totalKm: String, pickupKm: String, deliveryKm: String) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text("Delivery Address", style = MaterialTheme.typography.labelMedium) },
            overlineContent = { 
                Text(
                    text = "Pickup: $pickupKm km | Delivery: $deliveryKm km",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                ) 
            },
            supportingContent = { Text(address, style = MaterialTheme.typography.bodyLarge) },
            leadingContent = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            trailingContent = { Text("$totalKm km", fontWeight = FontWeight.Bold) }
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
                pickupKm = "1.2",
                deliveryKm = "2.0",
                timeExpensesAmount = "$ 1.500,00",
                kmDeductionsBreakdown = mapOf("Gasoline" to "$ 400,00", "Engine Oil" to "$ 100,00"),
                timeExpensesBreakdown = mapOf("Cell Phone" to "$ 1.000,00", "SOAT" to "$ 500,00"),
                isLoading = false
            )


        )
    }
}
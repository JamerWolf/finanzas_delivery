package com.control_delivery.finanzas_delivery.ui.trip_detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.control_delivery.finanzas_delivery.ui.theme.Finanzas_deliveryTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: String,
    onBackClick: () -> Unit,
    viewModel: TripDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(tripId) {
        viewModel.loadTrip(tripId)
    }
    val state = viewModel.uiState
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showDeleteDialog() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Trip")
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
            TripDetailContent(
                state = state,
                onToggleInfo = { viewModel.toggleInfoPopup() },
                modifier = Modifier.padding(padding)
            )
        }

        if (state.isDeleteDialogVisible) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDeleteDialog() },
                title = { Text("Delete Trip") },
                text = { Text("Are you sure you want to delete this entire trip? Financial metrics will be updated.") },
                confirmButton = {
                    Button(
                        onClick = { 
                            viewModel.deleteTrip(tripId, onSuccess = onBackClick)
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

        if (state.isInfoPopupVisible) {
            AlertDialog(
                onDismissRequest = { viewModel.toggleInfoPopup() },
                title = { Text("Deleted Expenses") },
                text = { Text("Asterisks (*) indicate deductions related to expenses that have since been deleted from the app. They still apply to this trip.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.toggleInfoPopup() }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun TripDetailContent(
    state: TripDetailUiState,
    onToggleInfo: () -> Unit,
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
        // General Info
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Trip Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Date")
                    Text(state.dateText)
                }
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Status")
                    Text(state.statusText, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Distance Traveled")
                    Text(state.totalDistanceText, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Financial Breakdown
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Financial Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onToggleInfo) {
                        Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Gross Earnings (Orders)")
                    Text(state.grossAmountText, fontWeight = FontWeight.SemiBold)
                }
                
                // KM Deductions
                Divider()
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Vehicle Expenses (KM)")
                    Text("- ${state.kmDeductionText}", color = MaterialTheme.colorScheme.error)
                }
                state.kmBreakdown.forEach { (name, amount) ->
                    Row(Modifier.fillMaxWidth().padding(start = 16.dp), Arrangement.SpaceBetween) {
                        Text(name, style = MaterialTheme.typography.bodySmall)
                        Text("$ $amount", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }

                // Time Deductions
                Divider()
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Savings / Time Expenses")
                    Text("- ${state.timeDeductionText}", color = MaterialTheme.colorScheme.error)
                }
                state.timeBreakdown.forEach { (name, amount) ->
                    Row(Modifier.fillMaxWidth().padding(start = 16.dp), Arrangement.SpaceBetween) {
                        Text(name, style = MaterialTheme.typography.bodySmall)
                        Text("$ $amount", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }

                // Net Profit
                Divider(thickness = 2.dp)
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Net Profit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(state.netAmountText, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        // Orders List
        Text("Orders in this Trip", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        state.orders.forEach { order ->
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text(order.platform.uppercase(), fontWeight = FontWeight.Bold) },
                    supportingContent = { Text(order.customerAddress, maxLines = 1) },
                    trailingContent = { Text("$ ${order.totalAmount}", fontWeight = FontWeight.SemiBold) }
                )
            }
        }
    }
}

package com.control_delivery.finanzas_delivery.ui.home

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.control_delivery.finanzas_delivery.domain.model.Order
import com.control_delivery.finanzas_delivery.domain.model.OrderStatus
import com.control_delivery.finanzas_delivery.domain.model.Trip
import com.control_delivery.finanzas_delivery.service.TrackingService
import com.control_delivery.finanzas_delivery.ui.add_order.AddOrderDialog
import com.control_delivery.finanzas_delivery.ui.components.total_orders_amount.OrdersTotalAmountViewModel
import com.control_delivery.finanzas_delivery.ui.components.total_orders_amount.TotalOrdersAmountContent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel(),
    totalViewModel: OrdersTotalAmountViewModel = hiltViewModel(),
    onTripClick: (String) -> Unit // Route to TripDetailScreen instead of OrderDetailScreen
) {
    val context = LocalContext.current

    val permissionsToRequest = remember {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissions
    }

    val requiredPermissionsState = rememberMultiplePermissionsState(
        permissions = permissionsToRequest
    )

    var showPermissionRationale by remember { mutableStateOf(false) }

    HomeScreenContent(
        state = homeViewModel.uiState,
        dailyTotal = totalViewModel.uiState.totalAmountDay,
        weeklyTotal = totalViewModel.uiState.totalAmountWeek,
        onAddOrderClick = {
            if (requiredPermissionsState.allPermissionsGranted) {
                homeViewModel.onAddOrderClick()
            } else if (requiredPermissionsState.shouldShowRationale) {
                showPermissionRationale = true
            } else {
                requiredPermissionsState.launchMultiplePermissionRequest()
            }
        },
        onDismissDialog = { isNewTrip -> 
            homeViewModel.onDismissDialog()
            if (isNewTrip) {
                if (requiredPermissionsState.allPermissionsGranted) {
                    TrackingService.start(context)
                } else if (requiredPermissionsState.shouldShowRationale) {
                    showPermissionRationale = true
                } else {
                    requiredPermissionsState.launchMultiplePermissionRequest()
                }
            }
        },
        onTripClick = onTripClick,
        onAdvanceStatus = { order ->
            homeViewModel.advanceOrderStatus(order, onTripAutoCompleted = {
                TrackingService.stop(context)
            })
        },
        onCompleteTripManually = {
            homeViewModel.completeActiveTrip {
                TrackingService.stop(context)
            }
        },
        modifier = modifier
    )

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Location Permission Required") },
            text = { Text("This app requires location permissions to track your active trips and calculate distances. Please grant the permissions to proceed.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionRationale = false
                    requiredPermissionsState.launchMultiplePermissionRequest()
                }) {
                    Text("Grant")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HomeScreenContent(
    state: HomeScreenUiState,
    dailyTotal: String,
    weeklyTotal: String,
    onAddOrderClick: () -> Unit,
    onDismissDialog: (Boolean) -> Unit,
    onTripClick: (String) -> Unit,
    onAdvanceStatus: (Order) -> Unit,
    onCompleteTripManually: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = onAddOrderClick) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Order / Start Trip")
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

            // Active Trip Panel
            if (state.activeTrip != null) {
                ActiveTripPanel(
                    activeTrip = state.activeTrip,
                    onAdvanceStatus = onAdvanceStatus,
                    onCompleteTripManually = onCompleteTripManually
                )
            }

            // Completed Trips History
            Text(
                text = "Completed Trips Today",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
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
                if (state.tripsCompletedToday.isEmpty()) {
                    item {
                        Text(
                            text = "No trips completed today.",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(state.tripsCompletedToday, key = { it.id }) { trip ->
                        TripItem(
                            trip = trip,
                            onClick = { onTripClick(trip.id) }
                        )
                    }
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
fun ActiveTripPanel(
    activeTrip: Trip,
    onAdvanceStatus: (Order) -> Unit,
    onCompleteTripManually: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Active Trip (GPS Tracking)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Distance so far: ${String.format(Locale.US, "%.2f", activeTrip.totalDistanceKm)} km",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            // List of orders in the active trip
            activeTrip.orders.forEach { order ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${order.platform.uppercase()} - ${order.customerAddress}",
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            text = "Status: ${order.status.name}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (order.status != OrderStatus.DELIVERED && order.status != OrderStatus.CANCELLED) {
                        Button(
                            onClick = { onAdvanceStatus(order) },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = when (order.status) {
                                    OrderStatus.ON_THE_WAY_TO_RECEIVE -> "Received"
                                    OrderStatus.RECEIVED -> "Picked Up"
                                    OrderStatus.ON_THE_WAY_TO_DELIVERY -> "Delivered"
                                    else -> "Advance"
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 4.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCompleteTripManually) {
                    Text("Force Complete")
                }
            }
        }
    }
}

@Composable
fun TripItem(
    trip: Trip,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val formattedNet = NumberFormat.getNumberInstance(Locale.GERMANY).format(trip.netAmount)

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = "Trip with ${trip.orders.size} order(s)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            supportingContent = {
                Text(
                    text = "${String.format(Locale.US, "%.1f", trip.totalDistanceKm)} km | Gross: $${trip.totalOrdersAmount}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            },
            trailingContent = {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Net: $$formattedNet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            leadingContent = {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "T",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        )
    }
}

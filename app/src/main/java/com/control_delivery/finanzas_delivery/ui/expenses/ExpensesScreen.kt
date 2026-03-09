package com.control_delivery.finanzas_delivery.ui.expenses

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.control_delivery.finanzas_delivery.ui.expenses.distance_based.DistanceExpensesScreen
import com.control_delivery.finanzas_delivery.ui.expenses.time_based.TimeBasedExpensesScreen
import com.control_delivery.finanzas_delivery.ui.expenses.time_based.TimeBasedExpensesViewModel

@Composable
fun ExpensesScreen(
    timeViewModel: TimeBasedExpensesViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Time", "Distance")
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                if (selectedTab == 0) timeViewModel.onAddClick()
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(text = title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> TimeBasedExpensesScreen(viewModel = timeViewModel)
                1 -> DistanceExpensesScreen()
            }
        }
    }
}

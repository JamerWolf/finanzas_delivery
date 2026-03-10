package com.control_delivery.finanzas_delivery.ui.expenses.time_based

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.control_delivery.finanzas_delivery.ui.expenses.time_based.components.AddTimeBasedExpenseDialog
import com.control_delivery.finanzas_delivery.ui.expenses.time_based.components.TimeBasedExpenseItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeBasedExpensesScreen(
    viewModel: TimeBasedExpensesViewModel = hiltViewModel()
) {
    val state = viewModel.uiState
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.expenses, key = { it.id }) { expense ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        when (dismissValue) {
                            SwipeToDismissBoxValue.EndToStart -> {
                                viewModel.onDeleteClick(expense)
                                false
                            }
                            SwipeToDismissBoxValue.StartToEnd -> {
                                viewModel.onEditClick(expense)
                                false
                            }
                            SwipeToDismissBoxValue.Settled -> false
                        }
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val direction = dismissState.dismissDirection
                        val color by animateColorAsState(
                            when (direction) {
                                SwipeToDismissBoxValue.EndToStart -> Color(0xFFD32F2F)
                                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary
                                else -> Color.Transparent
                            },
                            label = "swipe_bg_color"
                        )
                        val alignment = when (direction) {
                            SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                            SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                            else -> Alignment.Center
                        }
                        val icon = when (direction) {
                            SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                            SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                            else -> Icons.Default.Edit
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(color)
                                .padding(horizontal = 24.dp),
                            contentAlignment = alignment
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    },
                    enableDismissFromStartToEnd = true,
                    enableDismissFromEndToStart = true
                ) {
                    TimeBasedExpenseItem(expenseId = expense.id)
                }
            }
        }

        // Add Dialog
        if (state.isAddDialogVisible) {
            AddTimeBasedExpenseDialog(
                onDismiss = { viewModel.onDismissDialog() },
                onConfirm = { expense ->
                    viewModel.onSaveExpense(expense)
                }
            )
        }

        // Edit Dialog
        if (state.expenseToEdit != null) {
            AddTimeBasedExpenseDialog(
                expenseToEdit = state.expenseToEdit,
                onDismiss = { viewModel.onDismissDialog() },
                onConfirm = { expense ->
                    viewModel.onUpdateExpense(expense)
                }
            )
        }

        // Delete Confirmation Dialog
        if (state.expenseToDelete != null) {
            AlertDialog(
                onDismissRequest = { viewModel.onDismissDelete() },
                title = { Text("Delete Savings Goal") },
                text = {
                    Text("Are you sure you want to delete \"${state.expenseToDelete.description}\"? The accumulated amount will be returned to your net profit.")
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.onConfirmDelete() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onDismissDelete() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

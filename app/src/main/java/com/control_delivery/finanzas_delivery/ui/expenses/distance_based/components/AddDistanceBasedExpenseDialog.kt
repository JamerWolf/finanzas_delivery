package com.control_delivery.finanzas_delivery.ui.expenses.distance_based.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.control_delivery.finanzas_delivery.domain.model.DistanceBasedExpense
import com.control_delivery.finanzas_delivery.domain.model.DistanceExpenseType

@Composable
fun AddDistanceBasedExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (DistanceBasedExpense) -> Unit,
    expenseToEdit: DistanceBasedExpense? = null
) {
    val isEditing = expenseToEdit != null

    // Determine initial type
    val initialIsSavingsGoal = expenseToEdit?.type is DistanceExpenseType.SavingsGoal

    var description by remember { mutableStateOf(expenseToEdit?.description ?: "") }
    var isSavingsGoal by remember { mutableStateOf(initialIsSavingsGoal) }

    // PureDeduction fields
    var pricePerUnit by remember {
        mutableStateOf(
            (expenseToEdit?.type as? DistanceExpenseType.PureDeduction)?.pricePerUnit?.toString() ?: ""
        )
    }
    var kmPerUnit by remember {
        mutableStateOf(
            (expenseToEdit?.type as? DistanceExpenseType.PureDeduction)?.kmPerUnit?.toString() ?: ""
        )
    }

    // SavingsGoal fields
    var targetAmount by remember {
        mutableStateOf(
            (expenseToEdit?.type as? DistanceExpenseType.SavingsGoal)?.targetAmount?.toString() ?: ""
        )
    }
    var targetKm by remember {
        mutableStateOf(
            (expenseToEdit?.type as? DistanceExpenseType.SavingsGoal)?.targetKm?.toString() ?: ""
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isEditing) "Edit Distance Expense" else "New Distance Expense",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (e.g., Gasoline)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Type selector
                Text(text = "Expense Type", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isSavingsGoal,
                        onClick = { isSavingsGoal = false },
                        label = { Text("Pure Deduction") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    FilterChip(
                        selected = isSavingsGoal,
                        onClick = { isSavingsGoal = true },
                        label = { Text("Savings Goal") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    )
                }

                if (isSavingsGoal) {
                    // Savings Goal fields
                    OutlinedTextField(
                        value = targetAmount,
                        onValueChange = { targetAmount = it },
                        label = { Text("Target Amount (e.g., 55000)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = targetKm,
                        onValueChange = { targetKm = it },
                        label = { Text("Target KM (e.g., 1500)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                } else {
                    // Pure Deduction fields
                    OutlinedTextField(
                        value = pricePerUnit,
                        onValueChange = { pricePerUnit = it },
                        label = { Text("Price per Unit (e.g., 14000)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = kmPerUnit,
                        onValueChange = { kmPerUnit = it },
                        label = { Text("KM per Unit (e.g., 100)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val type = if (isSavingsGoal) {
                                val existingSavings = expenseToEdit?.type as? DistanceExpenseType.SavingsGoal
                                DistanceExpenseType.SavingsGoal(
                                    targetAmount = targetAmount.toLongOrNull() ?: 0L,
                                    targetKm = targetKm.toDoubleOrNull() ?: 0.0,
                                    accumulatedAmount = existingSavings?.accumulatedAmount ?: 0L,
                                    accumulatedKm = existingSavings?.accumulatedKm ?: 0.0
                                )
                            } else {
                                DistanceExpenseType.PureDeduction(
                                    pricePerUnit = pricePerUnit.toLongOrNull() ?: 0L,
                                    kmPerUnit = kmPerUnit.toDoubleOrNull() ?: 0.0
                                )
                            }

                            if (expenseToEdit != null) {
                                onConfirm(
                                    expenseToEdit.copy(
                                        description = description,
                                        type = type
                                    )
                                )
                            } else {
                                onConfirm(
                                    DistanceBasedExpense(
                                        description = description,
                                        type = type
                                    )
                                )
                            }
                        },
                        enabled = description.isNotBlank()
                                && if (isSavingsGoal) targetAmount.isNotBlank() && targetKm.isNotBlank()
                                   else pricePerUnit.isNotBlank() && kmPerUnit.isNotBlank()
                    ) {
                        Text(if (isEditing) "Update" else "Save")
                    }
                }
            }
        }
    }
}

package com.control_delivery.finanzas_delivery.ui.expenses.time_based.components

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
import com.control_delivery.finanzas_delivery.domain.model.ExpenseFrequency
import com.control_delivery.finanzas_delivery.domain.model.TimeBasedExpense
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTimeBasedExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (TimeBasedExpense) -> Unit,
    expenseToEdit: TimeBasedExpense? = null
) {
    val isEditing = expenseToEdit != null

    // Initialize state from expenseToEdit if editing
    var description by remember { mutableStateOf(expenseToEdit?.description ?: "") }
    var amount by remember { mutableStateOf(expenseToEdit?.amount?.toString() ?: "") }
    var selectedFrequencyType by remember {
        mutableStateOf(
            when (expenseToEdit?.frequency) {
                is ExpenseFrequency.Daily -> "Daily"
                is ExpenseFrequency.Weekly -> "Weekly"
                is ExpenseFrequency.Monthly -> "Monthly"
                is ExpenseFrequency.Yearly -> "Yearly"
                is ExpenseFrequency.Once -> "Daily"
                null -> "Daily"
            }
        )
    }
    var expanded by remember { mutableStateOf(false) }

    // Specific frequency states
    var selectedDayOfWeek by remember {
        mutableStateOf(
            (expenseToEdit?.frequency as? ExpenseFrequency.Weekly)?.dayOfWeek ?: DayOfWeek.MONDAY
        )
    }
    var selectedDayOfMonth by remember {
        mutableStateOf(
            (expenseToEdit?.frequency as? ExpenseFrequency.Monthly)?.dayOfMonth ?: 1
        )
    }
    var selectedMonth by remember {
        mutableStateOf(
            (expenseToEdit?.frequency as? ExpenseFrequency.Yearly)?.month ?: Month.JANUARY
        )
    }
    var selectedDayOfYear by remember {
        mutableStateOf(
            (expenseToEdit?.frequency as? ExpenseFrequency.Yearly)?.dayOfMonth ?: 1
        )
    }

    // Date Picker States
    var showMonthlyDatePicker by remember { mutableStateOf(false) }
    var showYearlyDatePicker by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    val zoneId = ZoneId.systemDefault()
    val startOfToday = today.atStartOfDay(zoneId).toInstant().toEpochMilli()

    val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM", Locale.getDefault())
    val monthDayFormatter = DateTimeFormatter.ofPattern("dd", Locale.getDefault())

    // Monthly Date Picker Logic
    if (showMonthlyDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showMonthlyDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()
                        selectedDayOfMonth = selectedDate.dayOfMonth
                    }
                    showMonthlyDatePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showMonthlyDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Yearly Date Picker Logic
    if (showYearlyDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showYearlyDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()
                        selectedDayOfYear = selectedDate.dayOfMonth
                        selectedMonth = selectedDate.month
                    }
                    showYearlyDatePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showYearlyDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
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
                    text = if (isEditing) "Edit Savings Goal" else "New Savings Goal",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (e.g., Rent)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Target Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Text(text = "Frequency", style = MaterialTheme.typography.labelLarge)

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedFrequencyType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        val types = listOf("Daily", "Weekly", "Monthly", "Yearly")
                        types.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedFrequencyType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Conditional Selectors based on Type
                when (selectedFrequencyType) {
                    "Weekly" -> {
                        Text(text = "Select Day", style = MaterialTheme.typography.labelMedium)
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(DayOfWeek.entries) { day ->
                                FilterChip(
                                    selected = selectedDayOfWeek == day,
                                    onClick = { selectedDayOfWeek = day },
                                    label = { Text(day.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                        }
                    }
                    "Monthly" -> {
                        Text(text = "Recurring Day", style = MaterialTheme.typography.labelMedium)
                        OutlinedCard(
                            onClick = { showMonthlyDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Day of month: $selectedDayOfMonth")
                                Text(text = "Change", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    "Yearly" -> {
                        Text(text = "Recurring Date", style = MaterialTheme.typography.labelMedium)
                        OutlinedCard(
                            onClick = { showYearlyDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val displayDate = LocalDate.of(2024, selectedMonth, selectedDayOfYear)
                                    .format(dateFormatter)
                                Text(text = "Every year on: $displayDate")
                                Text(text = "Change", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val target = amount.toLongOrNull() ?: 0L
                            val freq = when (selectedFrequencyType) {
                                "Daily" -> ExpenseFrequency.Daily
                                "Weekly" -> ExpenseFrequency.Weekly(selectedDayOfWeek)
                                "Monthly" -> ExpenseFrequency.Monthly(selectedDayOfMonth)
                                "Yearly" -> ExpenseFrequency.Yearly(selectedDayOfYear, selectedMonth)
                                else -> ExpenseFrequency.Daily
                            }

                            if (expenseToEdit != null) {
                                onConfirm(
                                    expenseToEdit.copy(
                                        description = description,
                                        amount = target,
                                        frequency = freq,
                                        nextDeadline = TimeBasedExpense.calculateDeadline(
                                            expenseToEdit.currentCycleStart, freq
                                        )
                                    )
                                )
                            } else {
                                onConfirm(
                                    TimeBasedExpense(
                                        description = description,
                                        amount = target,
                                        frequency = freq,
                                        startTimestamp = startOfToday
                                    )
                                )
                            }
                        },
                        enabled = description.isNotBlank() && amount.isNotBlank()
                    ) {
                        Text(if (isEditing) "Update Goal" else "Save Goal")
                    }
                }
            }
        }
    }
}

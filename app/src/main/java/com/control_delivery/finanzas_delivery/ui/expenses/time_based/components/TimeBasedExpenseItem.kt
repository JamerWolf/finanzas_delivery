package com.control_delivery.finanzas_delivery.ui.expenses.time_based.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.control_delivery.finanzas_delivery.domain.model.ExpenseFrequency
import com.control_delivery.finanzas_delivery.domain.model.TimeBasedExpense
import com.control_delivery.finanzas_delivery.ui.theme.Finanzas_deliveryTheme

@Composable
fun TimeBasedExpenseItem(
    modifier: Modifier = Modifier,
    viewModel: TimeBasedExpenseItemViewModel = hiltViewModel(),
    expense: TimeBasedExpense
) {
    viewModel.setUiState(expense)
    val state: TimeBasedExpenseItemUiState = viewModel.state

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title and Days Remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = state.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = state.daysLeftText,
                    style = MaterialTheme.typography.labelSmall,
                    color = state.daysLeftColor
                )
            }

            // Progress Bar and Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.weight(1f).height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                )
                Text(
                    text = state.progressText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Amounts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = state.savedAmountText,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = state.goalAmountText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimeBasedExpenseItemPreview() {
    Finanzas_deliveryTheme {
        TimeBasedExpenseItem(
            expense = TimeBasedExpense(
                description = "Example Expense",
                amount = 100.0,
                frequency = ExpenseFrequency.Daily,
                startTimestamp = System.currentTimeMillis()
            )
        )
    }
}

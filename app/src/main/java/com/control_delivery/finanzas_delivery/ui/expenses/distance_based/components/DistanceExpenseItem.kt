package com.control_delivery.finanzas_delivery.ui.expenses.distance_based.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.control_delivery.finanzas_delivery.ui.expenses.distance_based.DistanceExpenseItemUiState
import com.control_delivery.finanzas_delivery.ui.theme.Finanzas_deliveryTheme

@Composable
fun DistanceExpenseItem(
    state: DistanceExpenseItemUiState,
    onReset: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.description,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = state.costPerKmText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (state.isSavingsGoal) {
                // Money Progress
                SavingsProgressSection(
                    title = "Money Saved",
                    progress = state.moneyProgress,
                    progressText = state.moneyProgressText,
                    currentText = state.moneySavedText,
                    goalText = state.moneyGoalText
                )

                // KM Progress
                SavingsProgressSection(
                    title = "Physical Wear (KM)",
                    progress = state.kmProgress,
                    progressText = state.kmProgressText,
                    currentText = state.kmCurrentText,
                    goalText = state.kmGoalText
                )

                // Reset Button (Maintenance Performed)
                if (state.isGoalReached) {
                    Button(
                        onClick = onReset,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Perform Maintenance")
                    }
                }
            } else {
                // Pure Deduction Details (Gasoline)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = state.pricePerUnitText, style = MaterialTheme.typography.bodyMedium)
                    Text(text = state.kmPerUnitText, style = MaterialTheme.typography.bodyMedium)
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Footer
            Text(
                text = state.appliedToText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun SavingsProgressSection(
    title: String,
    progress: Float,
    progressText: String,
    currentText: String,
    goalText: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MaterialTheme.typography.labelMedium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
            )
            Text(
                text = progressText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = currentText, style = MaterialTheme.typography.bodySmall)
            Text(text = goalText, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DistanceExpenseItemGoalPreview() {
    Finanzas_deliveryTheme {
        DistanceExpenseItem(
            state = DistanceExpenseItemUiState(
                id = "1",
                description = "Engine Oil",
                costPerKmText = "Cost: $20/km",
                isSavingsGoal = true,
                moneyProgress = 0.45f,
                moneyProgressText = "45%",
                moneySavedText = "Saved: $45.000",
                moneyGoalText = "Goal: $100.000",
                kmProgress = 0.6f,
                kmProgressText = "60%",
                kmCurrentText = "Driven: 3.000 km",
                kmGoalText = "Service: 5.000 km",
                appliedToText = "Applies to: Pickup, Delivery"
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DistanceExpenseItemDeductionPreview() {
    Finanzas_deliveryTheme {
        DistanceExpenseItem(
            state = DistanceExpenseItemUiState(
                id = "2",
                description = "Gasoline",
                costPerKmText = "Cost: $100/km",
                isSavingsGoal = false,
                pricePerUnitText = "Unit Price: $15.000",
                kmPerUnitText = "Yield: 150 km/unit",
                appliedToText = "Applies to: Pickup, Delivery"
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

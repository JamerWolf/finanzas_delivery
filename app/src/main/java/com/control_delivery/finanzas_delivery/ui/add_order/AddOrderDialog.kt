package com.control_delivery.finanzas_delivery.ui.add_order

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AddOrderDialog (
    modifier: Modifier = Modifier,
    viewModel: AddOrderViewModel = hiltViewModel(),
    onDismiss: () -> Unit,

) {
    LaunchedEffect(Unit) {
        viewModel.resetState()
    }
    val uiState = viewModel.uiState

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
            shape = RoundedCornerShape(16.dp)) {
            Column(modifier = modifier
                              .fillMaxWidth()
                              .padding(16.dp),
                   verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(modifier = Modifier.padding(bottom = 8.dp),
                     text = "Add Order",
                     style = MaterialTheme.typography.headlineSmall
                )
                OutlinedTextField(modifier = Modifier.fillMaxWidth(),
                                  value = uiState.platform,
                                  onValueChange = { viewModel.onPlatformChange(it) },
                                  label = { Text("Platform (DIDI, RAPPI...)") },
                )
                OutlinedTextField(modifier = Modifier.fillMaxWidth(),
                                  value = uiState.address,
                                  onValueChange = { viewModel.onAddressChange(it)},
                                  label = { Text("Address delivery") },
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.amount,
                    onValueChange = { viewModel.onAmountChange(it) },
                    label = { Text("Delivery Value") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row (modifier = Modifier.fillMaxWidth(),
                     horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text(text = "Cancel")
                    }
                    Button(
                        onClick = { viewModel.saveOrder(onSuccess = onDismiss) },
                        enabled = uiState.isFormValid && !uiState.isSaving
                    ) {
                        if (uiState.isSaving) CircularProgressIndicator()
                        else Text("Confirm")
                    }
                }
            }

        }
    }
}

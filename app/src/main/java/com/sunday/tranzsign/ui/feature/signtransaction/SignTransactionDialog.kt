package com.sunday.tranzsign.ui.feature.signtransaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sunday.tranzsign.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignTransactionDialog(
    amountFormatted: String,
    isSigningInProgress: Boolean,
    onConfirm: (strategy: TransactionSigningStrategy) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStrategy by remember { mutableStateOf(TransactionSigningStrategy.PASSKEY) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.sign_transaction_label),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.to_withdraw_eth, amountFormatted),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isSigningInProgress) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Signing transaction...", style = MaterialTheme.typography.bodyMedium)
            } else {
                SigningStrategyList(
                    selectedStrategy = selectedStrategy,
                    onStrategySelected = { selectedStrategy = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onConfirm(selectedStrategy) },
                enabled = !isSigningInProgress, // Disable button while signing
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSigningInProgress) {
                    Text(stringResource(R.string.signing_in_progress_label))
                } else {
                    Text(
                        stringResource(
                            R.string.sign_with_strategy_label,
                            selectedStrategy.displayName
                        )
                    )
                }
            }
            TextButton(
                onClick = onDismiss,
                enabled = !isSigningInProgress,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.cancel_label))
            }
        }
    }
}

@Composable
private fun SigningStrategyList(
    selectedStrategy: TransactionSigningStrategy,
    onStrategySelected: (TransactionSigningStrategy) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(TransactionSigningStrategy.entries.toTypedArray()) { strategy ->
            StrategyItem(
                strategy = strategy,
                isSelected = strategy == selectedStrategy,
                onClick = { onStrategySelected(strategy) }
            )
        }
    }
}

@Composable
private fun StrategyItem(
    strategy: TransactionSigningStrategy,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = if (isSelected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = strategy.icon),
                contentDescription = strategy.displayName,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = strategy.displayName,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isSelected) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check_circle_24),
                    contentDescription = stringResource(R.string.selected_label),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

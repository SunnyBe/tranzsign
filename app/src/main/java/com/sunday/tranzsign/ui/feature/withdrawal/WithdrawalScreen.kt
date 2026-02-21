package com.sunday.tranzsign.ui.feature.withdrawal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sunday.tranzsign.R
import com.sunday.tranzsign.domain.entity.OperationType
import com.sunday.tranzsign.ui.component.StatusDialog
import com.sunday.tranzsign.ui.component.blurScrim
import com.sunday.tranzsign.ui.feature.signtransaction.SignTransactionDialog

@Composable
fun WithdrawalScreen(
    onNavigateToMain: () -> Unit,
    viewModel: WithdrawalViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WithdrawalEffect.NavigateToMain -> onNavigateToMain()
            }
        }
    }

    val isContentScrimmed = uiState.screenContent !is ScreenContent.Idle

    WithdrawalContent(
        uiState = uiState,
        isScrimmed = isContentScrimmed,
        onEvent = viewModel::onEvent
    )

    when (val content = uiState.screenContent) {
        is ScreenContent.Idle -> { /* No dialog */ }

        is ScreenContent.FetchingQuotation -> {
            AlertDialog(
                onDismissRequest = { /* Non-dismissible */ },
                title = { Text(text = stringResource(R.string.fetching_quote_title)) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.CircularProgressIndicator()
                    }
                },
                confirmButton = {} // No buttons, non-dismissible dialog
            )
        }

        is ScreenContent.ShowQuotation -> {
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(WithdrawalIntent.DismissDialog) },
                title = { Text(text = stringResource(R.string.confirm_quotation_title)) },
                text = {
                    Column {
                        Text(stringResource(R.string.confirm_withdrawal_details))
                        Spacer(modifier = Modifier.padding(8.dp))
                        Text(
                            stringResource(
                                R.string.amount_prefix,
                                uiState.quotationAmountFormatted
                            )
                        )
                        Text(
                            stringResource(
                                R.string.fee_prefix,
                                uiState.quotationFeeFormatted
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { viewModel.onEvent(WithdrawalIntent.ConfirmQuotation) }) {
                        Text(stringResource(R.string.confirm_label))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onEvent(WithdrawalIntent.DismissDialog) }) {
                        Text(stringResource(R.string.cancel_label))
                    }
                }
            )
        }

        is ScreenContent.ShowSignDialog -> {
            SignTransactionDialog(
                // The amount now comes from the quotation object.
                amountFormatted = uiState.amountToTransferFormatted,
                isSigningInProgress = false,
                onConfirm = { strategy ->
                    viewModel.onEvent(
                        WithdrawalIntent.SignTransaction(
                            strategy
                        )
                    )
                },
                onDismiss = { viewModel.onEvent(WithdrawalIntent.DismissDialog) }
            )
        }

        is ScreenContent.SigningInProgress -> {
            SignTransactionDialog(
                amountFormatted = uiState.amountToTransferFormatted,
                isSigningInProgress = true,
                onConfirm = {}, // Not applicable
                onDismiss = {}  // Not dismissible
            )
        }

        is ScreenContent.ShowSuccessDialog -> {
            StatusDialog(
                title = stringResource(R.string.success_label),
                message = content.message,
                onDismiss = { viewModel.onEvent(WithdrawalIntent.CompleteTransaction) }
            )
        }

        is ScreenContent.ShowErrorDialog -> {
            StatusDialog(
                title = stringResource(R.string.error_label),
                message = stringResource(id = content.messageRes),
                onDismiss = {
                    // If the error is critical (e.g., can't fetch balance), navigate back.
                    // Otherwise, just dismiss the dialog.
                    if (content.isCritical) {
                        onNavigateToMain()
                    } else {
                        viewModel.onEvent(WithdrawalIntent.DismissDialog)
                    }
                }
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WithdrawalContent(
    uiState: WithdrawalUiState,
    isScrimmed: Boolean,
    onEvent: (WithdrawalIntent) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.withdrawal_label)) }) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            WithdrawalFormContent(
                uiState = uiState,
                modifier = Modifier.blurScrim(active = isScrimmed),
                onEvent = onEvent
            )
        }
    }
}

@Composable
private fun WithdrawalFormContent(
    uiState: WithdrawalUiState,
    onEvent: (WithdrawalIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(
                R.string.available_balance_prefix,
                uiState.availableBalanceFormatted
            ),
            modifier = Modifier.fillMaxWidth()
        )
        AmountEntryField(
            amountInput = uiState.amountInput,
            remainingBalanceFormatted = uiState.remainingBalanceFormatted,
            isInsufficient = uiState.isInsufficientBalance,
            exceedsLimit = uiState.amountExceedsLimit,
            ethMaxLimitFormatted = uiState.ethMaxLimitFormatted,
            onAmountUpdate = { onEvent(WithdrawalIntent.AmountChanged(it)) }
        )
        Spacer(modifier = Modifier.weight(1f))
        CtaButton(
            text = stringResource(R.string.withdrawal_cta_label),
            onClick = { onEvent(WithdrawalIntent.RequestQuotation(OperationType.WITHDRAWAL)) },
            enabled = uiState.isCtaEnabled
        )
    }
}

@Composable
private fun AmountEntryField(
    amountInput: String,
    remainingBalanceFormatted: String,
    isInsufficient: Boolean,
    exceedsLimit: Boolean,
    ethMaxLimitFormatted: String,
    onAmountUpdate: (newAmount: String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = amountInput,
        onValueChange = onAmountUpdate,
        label = { Text(stringResource(R.string.amount_eth_label)) },
        placeholder = { Text("0.00") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        trailingIcon = {
            Text(
                stringResource(R.string.eth),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        singleLine = true,
        isError = isInsufficient || exceedsLimit,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    )

    Text(
        text = stringResource(R.string.remaining_prefix, remainingBalanceFormatted),
        color = if (isInsufficient) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 4.dp)
    )
    if (isInsufficient) {
        Text(
            text = stringResource(R.string.insufficient_balance_incl_gas),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    } else if (exceedsLimit) {
        Text(
            text = stringResource(R.string.amount_exceeds_limit_error, ethMaxLimitFormatted),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun CtaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(text)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewWithdrawalContent() {
    MaterialTheme {
        WithdrawalContent(
            uiState = WithdrawalUiState(amountInput = "0.5"),
            isScrimmed = false,
            onEvent = {}
        )
    }
}

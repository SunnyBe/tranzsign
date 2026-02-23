package com.sunday.tranzsign.ui.feature.withdrawal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunday.tranzsign.R
import com.sunday.tranzsign.domain.entity.AuthStrategy
import com.sunday.tranzsign.domain.entity.OperationType
import com.sunday.tranzsign.domain.entity.TransactionQuotation
import com.sunday.tranzsign.domain.repository.AccountRepository
import com.sunday.tranzsign.domain.service.MoneyFormatter
import com.sunday.tranzsign.domain.service.PrecisionMode
import com.sunday.tranzsign.domain.service.QuotationService
import com.sunday.tranzsign.domain.service.TransactionService
import com.sunday.tranzsign.domain.usecase.signtransaction.SignTransactionState
import com.sunday.tranzsign.domain.usecase.signtransaction.SignTransactionUseCase
import com.sunday.tranzsign.domain.usecase.signtransaction.SigningRequest
import com.sunday.tranzsign.ui.feature.signtransaction.TransactionAuthStrategy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.math.BigInteger
import java.math.RoundingMode
import javax.inject.Inject

@HiltViewModel
class WithdrawalViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val quotationService: QuotationService,
    private val signTransactionUseCase: SignTransactionUseCase,
    private val transactionService: TransactionService, // Rename to send transaction service
    private val moneyFormatter: MoneyFormatter
) : ViewModel() {

    private val _effect = Channel<WithdrawalEffect>()
    val effect = _effect.receiveAsFlow()

    private val _availableBalanceInWei = MutableStateFlow(BigInteger.ZERO)
    private val _quotation =
        MutableStateFlow<TransactionQuotation?>(null)

    private val _screenContent = MutableStateFlow<ScreenContent>(ScreenContent.Idle)
    private val _amountInput = MutableStateFlow("") // State for the text field

    val uiState: StateFlow<WithdrawalUiState> = combine(
        _availableBalanceInWei,
        _amountInput,
        _quotation,
        _screenContent
    ) { balanceWei, amountInput, quote, screenContent ->
        val amountInWei = amountInput.ethToWei()
        val remainingInWei = balanceWei - amountInWei - (quote?.feeInWei ?: BigInteger.ZERO)
        val isAmountPositive = amountInWei > BigInteger.ZERO
        val hasSufficientBalance = remainingInWei >= BigInteger.ZERO
        val exceedsLimit = amountInWei > MAX_WITHDRAWAL_IN_WEI
        val amountToTransferWei = quote?.amountInWei?.plus(quote.feeInWei) ?: BigInteger.ZERO

        WithdrawalUiState(
            amountToTransferFormatted = moneyFormatter.format(amountToTransferWei),
            availableBalanceFormatted = moneyFormatter.format(balanceWei),
            remainingBalanceFormatted = moneyFormatter.format(remainingInWei),
            amountInput = amountInput,
            isCtaEnabled = isAmountPositive && hasSufficientBalance && !exceedsLimit,
            isInsufficientBalance = isAmountPositive && !hasSufficientBalance,
            amountExceedsLimit = exceedsLimit,
            ethMaxLimitFormatted = moneyFormatter.format(MAX_WITHDRAWAL_IN_WEI),
            quotationAmountFormatted = quote?.let {
                moneyFormatter.format(
                    amountInWei = it.amountInWei,
                    precisionMode = PrecisionMode.Detail
                )
            } ?: "",
            quotationFeeFormatted = quote?.let { moneyFormatter.format(it.feeInWei) } ?: "",
            screenContent = screenContent
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = WithdrawalUiState()
    )

    init {
        observeBalance()
    }

    private fun observeBalance() {
        accountRepository.getEthWalletBalance()
            .onEach { newBalance ->
                _availableBalanceInWei.value = newBalance.balanceInWei
            }
            .catch {
                _screenContent.value = ScreenContent.ShowErrorDialog(
                    R.string.balance_fetch_error,
                    isCritical = true
                )
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(intent: WithdrawalIntent) {
        when (intent) {
            is WithdrawalIntent.AmountChanged -> handleAmountChanged(intent.amount)
            is WithdrawalIntent.RequestQuotation -> handleRequestQuotation(intent.operationType)
            is WithdrawalIntent.ConfirmQuotation -> handleConfirmQuotation(
                intent.quotation,
                intent.operationType
            )

            is WithdrawalIntent.SignTransaction -> handleSignTransaction(
                intent.signingRequest,
                intent.authStrategy
            )

            is WithdrawalIntent.CompleteTransaction -> handleCompleteTransaction()
            is WithdrawalIntent.DismissDialog -> dismissDialog()
        }
    }

    private fun handleAmountChanged(newAmount: String) {
        _amountInput.value = newAmount
        _quotation.value = null // Invalidate quotation
    }

    private fun handleRequestQuotation(operationType: OperationType) {
        if (uiState.value.isCtaEnabled.not()) return
        Timber.tag(LOG_TAG).i("Requesting Quotation")

        viewModelScope.launch {
            _screenContent.value = ScreenContent.FetchingQuotation

            try {
                val amountInWei = _amountInput.value.ethToWei()

                val quotation =
                    quotationService.getQuotation(amountInWei, operationType)
                _quotation.value = quotation
                _screenContent.value = ScreenContent.ShowQuotation(quotation, operationType)
            } catch (cause: Exception) {
                _screenContent.value = ScreenContent.ShowErrorDialog(R.string.quotation_fetch_error)
                Timber.tag(LOG_TAG).e(cause, "Failed to create Quotation")
            }
        }
    }

    private fun handleConfirmQuotation(
        quotation: TransactionQuotation?,
        operationType: OperationType
    ) {
        val currentQuotation = quotation ?: return
        val signingRequest = SigningRequest(
            quotationId = currentQuotation.id,
            challenge = currentQuotation.challenge,
            operationType = operationType
        )
        _screenContent.value = ScreenContent.ShowSignDialog(signingRequest)
    }

    private fun handleSignTransaction(
        signingRequest: SigningRequest,
        strategy: TransactionAuthStrategy
    ) {
        signTransactionUseCase(
            quotationId = signingRequest.quotationId,
            signingRequest = signingRequest,
            authStrategy = strategy.mapToDomain()
        )
            .onEach { signingState ->
                when (signingState) {
                    is SignTransactionState.InProgress -> {
                        _screenContent.value = ScreenContent.SigningInProgress
                    }

                    is SignTransactionState.Success -> {
                        _screenContent.value =
                            ScreenContent.SendingTransaction(
                                quotationId = signingState.result.quotationId,
                                signedChallenge = signingState.result.signedChallenge
                            )

                        // We wrap the submission in NonCancellable to ensure the transaction
                        // reaches the network even if the user navigates away or the ViewModel
                        // is cleared mid-broadcast.
                        // See README: "Transaction Integrity vs. Memory Management" for trade-offs.
                        withContext(NonCancellable) {
                            val result = runCatching {
                                transactionService.submit(
                                    signingState.result.quotationId,
                                    signingState.result.signedChallenge,
                                    strategy.mapToDomain()
                                )
                            }.getOrDefault(false)

                            // Memory Leak is expected here if the VM was cleared; we prioritize transaction integrity.
                            withContext(Dispatchers.Main) {
                                if (result) {
                                    _screenContent.value = ScreenContent.ShowSuccessDialog(
                                        R.string.withdrawal_success_message
                                    )
                                } else {
                                    _screenContent.value = ScreenContent.ShowErrorDialog(
                                        R.string.signing_failed_error,
                                        isCritical = false
                                    )
                                }
                            }
                        }
                    }

                    is SignTransactionState.Error -> {
                        _screenContent.value = ScreenContent.ShowErrorDialog(
                            R.string.signing_failed_error,
                            isCritical = false
                        )
                    }
                }

            }.launchIn(viewModelScope)
    }

    private fun handleCompleteTransaction() {
        viewModelScope.launch {
            _effect.send(WithdrawalEffect.NavigateToMain)
        }
    }

    private fun dismissDialog() {
        _amountInput.value = "" // Reset amount by updating the input state
        _quotation.value = null
        _screenContent.value = ScreenContent.Idle
    }

    private fun String.ethToWei(): BigInteger {
        val bigDecimal = this.toBigDecimalOrNull() ?: return BigInteger.ZERO
        return bigDecimal.setScale(18, RoundingMode.DOWN) // Truncate excess precision
            .movePointRight(18)
            .toBigIntegerExact() // Ensure no hidden fractions
    }

    private fun TransactionAuthStrategy.mapToDomain() = when (this) {
        TransactionAuthStrategy.PASSKEY -> AuthStrategy.PASSKEY
        TransactionAuthStrategy.OTP -> AuthStrategy.OTP
        TransactionAuthStrategy.BIOMETRIC -> AuthStrategy.BIOMETRIC
    }

    companion object {
        private const val LOG_TAG = "WithdrawalViewModel"
        private val MAX_WITHDRAWAL_IN_WEI = BigInteger("10000000000000000000") // 10 ETH in WEI
    }
}

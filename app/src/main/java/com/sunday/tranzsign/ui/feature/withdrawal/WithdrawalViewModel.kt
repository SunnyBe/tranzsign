package com.sunday.tranzsign.ui.feature.withdrawal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunday.tranzsign.R
import com.sunday.tranzsign.domain.entity.OperationType
import com.sunday.tranzsign.domain.entity.SigningStrategy
import com.sunday.tranzsign.domain.repository.AccountRepository
import com.sunday.tranzsign.domain.service.MoneyFormatter
import com.sunday.tranzsign.domain.service.QuotationService
import com.sunday.tranzsign.domain.usecase.signtransaction.SignTransactionUseCase
import com.sunday.tranzsign.domain.usecase.signtransaction.SigningState
import com.sunday.tranzsign.ui.feature.signtransaction.TransactionSigningStrategy
import dagger.hilt.android.lifecycle.HiltViewModel
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
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import javax.inject.Inject

@HiltViewModel
class WithdrawalViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val quotationService: QuotationService,
    private val signTransactionUseCase: SignTransactionUseCase,
    private val moneyFormatter: MoneyFormatter
) : ViewModel() {

    private val _effect = Channel<WithdrawalEffect>()
    val effect = _effect.receiveAsFlow()

    private val _availableBalanceInWei = MutableStateFlow(BigInteger.ZERO)
    private val _quotation =
        MutableStateFlow<com.sunday.tranzsign.domain.entity.TransactionQuotation?>(null)

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
        val exceedsLimit =
            amountInWei > MAX_WITHDRAWAL_ETH.movePointRight(ETH_DECIMALS).toBigInteger()
        val amountToTransferWei = quote?.amountInWei?.plus(quote.feeInWei) ?: BigInteger.ZERO

        WithdrawalUiState(
            amountToTransferFormatted = moneyFormatter.format(amountToTransferWei.toEth()),
            availableBalanceFormatted = moneyFormatter.format(balanceWei.toEth()),
            remainingBalanceFormatted = moneyFormatter.format(remainingInWei.toEth()),
            amountInput = amountInput, // Pass the input directly through
            isCtaEnabled = isAmountPositive && hasSufficientBalance && !exceedsLimit,
            isInsufficientBalance = isAmountPositive && !hasSufficientBalance,
            amountExceedsLimit = exceedsLimit,
            ethMaxLimitFormatted = MAX_WITHDRAWAL_ETH.toPlainString(),
            quotationAmountFormatted = quote?.let { moneyFormatter.format(it.amountInWei.toEth()) }
                ?: "",
            quotationFeeFormatted = quote?.let { moneyFormatter.format(it.feeInWei.toEth()) }
                ?: "",
            screenContent = screenContent
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = WithdrawalUiState()
    )

    init {
        observeBalance()
        observeSigningState()
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

    private fun observeSigningState() {
        signTransactionUseCase.state
            .onEach { signingState ->
                val screenContent = when (signingState) {
                    is SigningState.InProgress -> ScreenContent.SigningInProgress
                    is SigningState.Success -> ScreenContent.ShowSuccessDialog(signingState.message)
                    is SigningState.Error -> ScreenContent.ShowErrorDialog(
                        R.string.signing_failed_error,
                        isCritical = false
                    )

                    is SigningState.Idle -> return@onEach
                }
                _screenContent.value = screenContent
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(intent: WithdrawalIntent) {
        when (intent) {
            is WithdrawalIntent.AmountChanged -> handleAmountChanged(intent.amount)
            is WithdrawalIntent.RequestQuotation -> handleRequestQuotation(intent.operationType)
            is WithdrawalIntent.ConfirmQuotation -> handleConfirmQuotation()
            is WithdrawalIntent.SignTransaction -> handleSignTransaction(intent.strategy)
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
                _screenContent.value = ScreenContent.ShowQuotation(quotation)
            } catch (cause: Exception) {
                _screenContent.value = ScreenContent.ShowErrorDialog(R.string.quotation_fetch_error)
                Timber.tag(LOG_TAG).e(cause, "Failed to create Quotation")
            }
        }
    }

    private fun handleConfirmQuotation() {
        val currentQuotation = _quotation.value ?: return
        _screenContent.value = ScreenContent.ShowSignDialog(currentQuotation)
    }

    private fun handleSignTransaction(strategy: TransactionSigningStrategy) {
        val currentQuotation = _quotation.value ?: return

        viewModelScope.launch {
            signTransactionUseCase.execute(
                quotation = currentQuotation,
                strategy = strategy.mapToDomain(),
                operationType = OperationType.WITHDRAWAL
            )
        }
    }

    private fun handleCompleteTransaction() {
        viewModelScope.launch {
            _effect.send(WithdrawalEffect.NavigateToMain)
        }
    }

    private fun dismissDialog() {
        signTransactionUseCase.reset()
        _amountInput.value = "" // Reset amount by updating the input state
        _quotation.value = null
        _screenContent.value = ScreenContent.Idle
    }

    private fun BigInteger.toEth(): BigDecimal = this.toBigDecimal().movePointLeft(ETH_DECIMALS)

    private fun String.ethToWei(): BigInteger {
        val bigDecimal = this.toBigDecimalOrNull() ?: return BigInteger.ZERO
        return bigDecimal.setScale(18, RoundingMode.DOWN) // Truncate excess precision
            .movePointRight(18)
            .toBigIntegerExact() // Ensure no hidden fractions
    }

    private fun TransactionSigningStrategy.mapToDomain() = when (this) {
        TransactionSigningStrategy.PASSKEY -> SigningStrategy.PASSKEY
        TransactionSigningStrategy.OTP -> SigningStrategy.OTP
        TransactionSigningStrategy.BIOMETRIC -> SigningStrategy.BIOMETRIC
    }

    companion object {
        private const val LOG_TAG = "WithdrawalViewModel"
        private const val ETH_DECIMALS = 18
        private val MAX_WITHDRAWAL_ETH = BigDecimal("10.00")
    }
}

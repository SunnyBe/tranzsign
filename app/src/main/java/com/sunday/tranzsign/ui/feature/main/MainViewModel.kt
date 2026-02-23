package com.sunday.tranzsign.ui.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunday.tranzsign.R
import com.sunday.tranzsign.domain.repository.AccountRepository
import com.sunday.tranzsign.domain.repository.FeatureRepository
import com.sunday.tranzsign.domain.service.DateTimeFormatter
import com.sunday.tranzsign.domain.service.MoneyFormatter
import com.sunday.tranzsign.domain.service.PrecisionMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val featureRepository: FeatureRepository,
    private val moneyFormatter: MoneyFormatter,
    private val dateTimeFormatter: DateTimeFormatter
) : ViewModel() {
    private val _effect = Channel<MainEffect>()
    val effect = _effect.receiveAsFlow()

    val uiState: StateFlow<MainUiState> =
        combine(
            featureRepository.getAvailableFeatures(),
            accountRepository.getActiveUserAccount(),
            accountRepository.getEthWalletBalance()
        ) { features, userAccount, walletBalance ->
            val featuresUiState = features.map { feature ->
                when (feature.title) {
                    "withdrawal" -> FeatureUiState(
                        id = feature.id,
                        title = R.string.withdrawal_label,
                        iconRes = R.drawable.ic_currency_exchange_24,
                        isDisabled = feature.isDisabled
                    )

                    "transfer" -> FeatureUiState(
                        id = feature.id,
                        title = R.string.transfer_label,
                        iconRes = R.drawable.ic_swap_vertical_circle_24,
                        isDisabled = feature.isDisabled
                    )

                    "swap" -> FeatureUiState(
                        feature.id,
                        R.string.swap_label,
                        R.drawable.ic_swap_horizontal_circle_24,
                        isDisabled = feature.isDisabled
                    )

                    else -> {
                        Timber.tag(LOG_TAG).e("Unknown feature title: ${feature.title}")
                        FeatureUiState(
                            id = feature.id,
                            title = R.string.unknown_feature_label,
                            iconRes = R.drawable.ic_stop_24,
                            isDisabled = true,
                            isActive = false
                        )
                    }
                }
            }
            MainUiState(
                featureList = featuresUiState,
                walletBalanceUiState = WalletBalanceUiState(
                    introTitle = userAccount.displayName,
                    currentBalance = moneyFormatter.format(
                        amountInWei = walletBalance.balanceInWei,
                        precisionMode = PrecisionMode.Standard
                    ),
                    lastBalanceUpdate = dateTimeFormatter.formatFullDateTime(walletBalance.lastUpdatedMillis)
                )
            )
        }
            .onEach { uiState -> Timber.tag(LOG_TAG).d("UI_STATE: [$uiState]") }
            .catch { cause ->
                Timber.tag(LOG_TAG).e(cause, "unable to make uiState")
                _effect.send(MainEffect.ShowAlert(R.string.illegal_state_alert, true))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = MainUiState()
            )

    fun onEvent(event: MainIntent) {
        Timber.tag(LOG_TAG).d("Dispatching Event: [$event]")
        when (event) {
            is MainIntent.SelectFeature -> {
                viewModelScope.launch {
                    when (event.selectedId) {
                        0 -> _effect.send(MainEffect.NavigateToWithdrawal)
                        1, 2 -> _effect.send(MainEffect.ShowAlert(R.string.unavailable_alert))
                        else -> _effect.send(MainEffect.ShowAlert(R.string.illegal_state_alert))
                    }
                }
            }

            is MainIntent.RefreshBalance -> {
                viewModelScope.launch {
                    _effect.send(MainEffect.ShowAlert(R.string.unavailable_alert))
                }
            }
        }
    }

    companion object {
        private const val LOG_TAG = "MainViewModel"
    }
}

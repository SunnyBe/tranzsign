package com.sunday.tranzsign.ui.feature.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sunday.tranzsign.R
import com.sunday.tranzsign.ui.component.WalletBalanceScreen
import com.sunday.tranzsign.ui.navigation.AppDestinations
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun MainScreen(
    mainViewModel: MainViewModel = hiltViewModel(),
    onNavigateToFeature: (String) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        mainViewModel.effect.collectLatest { effect ->
            when (effect) {
                MainEffect.NavigateToWithdrawal -> onNavigateToFeature(AppDestinations.WITHDRAWAL_ROUTE)
                is MainEffect.ShowAlert -> {
                    val message = context.getString(effect.alertRes)
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    }

    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    MainContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onRefreshBalance = { mainViewModel.onEvent(MainIntent.RefreshBalance) },
        onFeatureSelect = { mainViewModel.onEvent(MainIntent.SelectFeature(it)) }
    )
}

@Composable
private fun MainContent(
    uiState: MainUiState,
    snackbarHostState: SnackbarHostState,
    onFeatureSelect: (Int) -> Unit,
    onRefreshBalance: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        MainFeatures(
            walletBalance = uiState.walletBalanceUiState,
            features = uiState.featureList,
            onFeatureSelect = onFeatureSelect,
            onRefreshBalance = onRefreshBalance,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun MainFeatures(
    walletBalance: WalletBalanceUiState,
    features: List<FeatureUiState>,
    onFeatureSelect: (Int) -> Unit,
    onRefreshBalance: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .verticalScroll(scrollState)
    ) {
        WalletBalanceScreen(
            introTitle = stringResource(R.string.intro_hi, walletBalance.introTitle),
            balance = walletBalance.currentBalance,
            lastUpdated = walletBalance.lastBalanceUpdate,
            onRefresh = onRefreshBalance
        )
        Spacer(modifier = Modifier.padding(32.dp))
        Text(
            text = stringResource(R.string.transactions_label),
            style = MaterialTheme.typography.titleLarge
        )
        features.forEach { feature ->
            FeatureItem(
                feature = feature,
                onClick = onFeatureSelect
            )
        }
    }
}

@Composable
private fun FeatureItem(
    feature: FeatureUiState,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(vertical = 8.dp, horizontal = 8.dp),
        onClick = { onClick(feature.id) },
        enabled = !feature.isDisabled
    ) {
        Icon(
            painter = painterResource(feature.iconRes),
            contentDescription = stringResource(feature.title)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(feature.title),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@PreviewScreenSizes
@Composable
private fun PreviewMainContent() {
    val testFeatures = listOf(
        FeatureUiState(
            id = 0,
            title = R.string.withdrawal_label,
            iconRes = R.drawable.ic_currency_exchange_24
        ),
        FeatureUiState(
            id = 1,
            title = R.string.transfer_label,
            iconRes = R.drawable.ic_swap_vertical_circle_24,
            isDisabled = true
        ),
        FeatureUiState(
            id = 2,
            title = R.string.swap_label,
            iconRes = R.drawable.ic_swap_horizontal_circle_24,
            isDisabled = true
        )
    )
    MaterialTheme {
        MainContent(
            uiState = MainUiState(
                featureList = testFeatures,
                walletBalanceUiState = WalletBalanceUiState(
                    introTitle = "Santi Carzola",
                    currentBalance = "ETH 0.200000001",
                    lastBalanceUpdate = "Feb 20, 2026, 3:03 AM"
                )
            ),
            snackbarHostState = SnackbarHostState(),
            onRefreshBalance = {},
            onFeatureSelect = {}
        )
    }
}
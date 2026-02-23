package com.sunday.tranzsign

import app.cash.turbine.test
import com.sunday.tranzsign.domain.entity.EthWalletBalance
import com.sunday.tranzsign.domain.repository.AccountRepository
import com.sunday.tranzsign.domain.service.MoneyFormatter
import com.sunday.tranzsign.domain.service.PrecisionMode
import com.sunday.tranzsign.domain.service.QuotationService
import com.sunday.tranzsign.domain.service.TransactionService
import com.sunday.tranzsign.domain.usecase.signtransaction.SignTransactionState
import com.sunday.tranzsign.domain.usecase.signtransaction.SignTransactionUseCase
import com.sunday.tranzsign.ui.feature.withdrawal.WithdrawalIntent
import com.sunday.tranzsign.ui.feature.withdrawal.WithdrawalViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@ExperimentalCoroutinesApi
class WithdrawalViewModelTest {
    private lateinit var accountRepository: AccountRepository
    private lateinit var quotationService: QuotationService
    private lateinit var signTransactionUseCase: SignTransactionUseCase
    private lateinit var transactionService: TransactionService
    private lateinit var moneyFormatter: MoneyFormatter

    private lateinit var viewModel: WithdrawalViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        accountRepository = mockk()
        quotationService = mockk(relaxed = true)
        signTransactionUseCase = mockk()
        transactionService = mockk(relaxed = true)
        moneyFormatter = mockk()

        coEvery { accountRepository.getEthWalletBalance() } returns flowOf(
            EthWalletBalance(
                balanceInWei = BigInteger("5000000000000000000"), // 5 ETH in WEI
                lastUpdatedMillis = System.currentTimeMillis()
            )
        )
        every { signTransactionUseCase(any(), any(), any()) } returns MutableStateFlow(
            SignTransactionState.InProgress
        )

        every { moneyFormatter.format(any(), any(), any()) } answers {
            val amount = firstArg<BigDecimal>()
            val precision = thirdArg<PrecisionMode>()
            val formattedAmount = amount.setScale(precision.max, RoundingMode.DOWN).toPlainString()
            "ETH $formattedAmount"
        }

        viewModel = WithdrawalViewModel(
            accountRepository = accountRepository,
            quotationService = quotationService,
            signTransactionUseCase = signTransactionUseCase,
            transactionService = transactionService,
            moneyFormatter = moneyFormatter
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onEvent AmountChanged with valid amount THEN uiState updates correctly`() = runTest {
        viewModel.uiState.test {
            // GIVEN: Skip the initial empty state and the state after balance is first loaded.
            // We only care about the state *after* our action.
            skipItems(2)

            // WHEN
            viewModel.onEvent(WithdrawalIntent.AmountChanged("1.5"))

            // THEN
            val latestState = awaitItem()
            assertEquals("1.5", latestState.amountInput)
            assertTrue(latestState.isCtaEnabled)
            assertFalse(latestState.isInsufficientBalance)
            assertFalse(latestState.amountExceedsLimit)
            assertEquals("ETH 3.50000000", latestState.remainingBalanceFormatted)
        }
    }

    @Test
    fun `onEvent AmountChanged with insufficient balance THEN Cta is disabled`() = runTest {
        // GIVEN
        coEvery { accountRepository.getEthWalletBalance() } returns flowOf(
            EthWalletBalance(
                balanceInWei = BigInteger("1000000000000000000"), // 1 ETH in WEI
                lastUpdatedMillis = System.currentTimeMillis()
            )
        )
        viewModel = WithdrawalViewModel(
            accountRepository = accountRepository,
            quotationService = quotationService,
            signTransactionUseCase = signTransactionUseCase,
            transactionService = transactionService,
            moneyFormatter = moneyFormatter
        )

        viewModel.uiState.test {
            // Skip initial states
            skipItems(2)

            // WHEN
            viewModel.onEvent(WithdrawalIntent.AmountChanged("1.5"))

            // THEN
            val latestState = awaitItem()
            assertEquals("1.5", latestState.amountInput)
            assertFalse(latestState.isCtaEnabled)
            assertTrue(latestState.isInsufficientBalance)
            assertEquals("ETH -0.50000000", latestState.remainingBalanceFormatted)
        }
    }

    @Test
    fun `onEvent AmountChanged with amount exceeding limit THEN Cta is disabled`() = runTest {
        viewModel.uiState.test {
            // Skip initial states
            skipItems(2)

            // WHEN
            viewModel.onEvent(WithdrawalIntent.AmountChanged("11"))

            // THEN
            val latestState = awaitItem()
            assertEquals("11", latestState.amountInput)
            assertFalse(latestState.isCtaEnabled)
            assertTrue(latestState.amountExceedsLimit)
        }
    }
}

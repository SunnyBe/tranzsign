package com.sunday.tranzsign

import app.cash.turbine.test
import com.sunday.tranzsign.domain.entity.EthWalletBalance
import com.sunday.tranzsign.domain.entity.OperationType
import com.sunday.tranzsign.domain.entity.TransactionQuotation
import com.sunday.tranzsign.domain.repository.AccountRepository
import com.sunday.tranzsign.domain.service.DateTimeFormatter
import com.sunday.tranzsign.domain.service.MoneyFormatter
import com.sunday.tranzsign.domain.service.PrecisionMode
import com.sunday.tranzsign.domain.service.QuotationService
import com.sunday.tranzsign.domain.service.TransactionService
import com.sunday.tranzsign.domain.usecase.signtransaction.SignTransactionState
import com.sunday.tranzsign.domain.usecase.signtransaction.SignTransactionUseCase
import com.sunday.tranzsign.ui.feature.withdrawal.ScreenContent
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
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigInteger
import java.math.RoundingMode
import java.util.Locale

@ExperimentalCoroutinesApi
class WithdrawalViewModelTest {
    private lateinit var accountRepository: AccountRepository
    private lateinit var quotationService: QuotationService
    private lateinit var signTransactionUseCase: SignTransactionUseCase
    private lateinit var transactionService: TransactionService
    private lateinit var moneyFormatter: MoneyFormatter
    private lateinit var timeFormatter: DateTimeFormatter

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
        timeFormatter = mockk()

        coEvery { accountRepository.getEthWalletBalance() } returns flowOf(
            EthWalletBalance(
                balanceInWei = BigInteger("5000000000000000000"), // 5 ETH in WEI
                lastUpdatedMillis = testDispatcher.scheduler.currentTime
            )
        )
        every { signTransactionUseCase(any(), any(), any()) } returns MutableStateFlow(
            SignTransactionState.InProgress
        )

        every { timeFormatter.formatRemainingTime(any()) } returns "00:59"

        every { moneyFormatter.format(any<BigInteger>(), any(), any()) } answers {
            val amountInWei = firstArg<BigInteger>()
            val precision = thirdArg<PrecisionMode>()

            // Simulate the real implementation's logic
            val amountInEth = amountInWei.toBigDecimal().movePointLeft(18)
            val numberFormat = java.text.NumberFormat.getNumberInstance(Locale.US)
            numberFormat.minimumFractionDigits = precision.min
            numberFormat.maximumFractionDigits = precision.max
            numberFormat.roundingMode = RoundingMode.DOWN

            val formattedNumber = numberFormat.format(amountInEth)
            "ETH $formattedNumber"
        }

        viewModel = WithdrawalViewModel(
            accountRepository = accountRepository,
            quotationService = quotationService,
            signTransactionUseCase = signTransactionUseCase,
            transactionService = transactionService,
            moneyFormatter = moneyFormatter,
            timeFormatter = timeFormatter
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
            expectMostRecentItem()

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
            moneyFormatter = moneyFormatter,
            timeFormatter = timeFormatter
        )

        viewModel.uiState.test {
            // Skip initial states
            expectMostRecentItem()

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
            expectMostRecentItem()

            // WHEN
            viewModel.onEvent(WithdrawalIntent.AmountChanged("11"))

            // THEN
            val latestState = awaitItem()
            assertEquals("11", latestState.amountInput)
            assertFalse(latestState.isCtaEnabled)
            assertTrue(latestState.amountExceedsLimit)
        }
    }

    @Test
    fun `requesting quotation THEN shows loading then shows quotation dialog`() = runTest {
        // GIVEN
        val mockQuotation = TransactionQuotation(
            id = "q_123",
            amountInWei = BigInteger("1000000000000000000"),
            feeInWei = BigInteger("10000000000000000"),
            expiresAt = testScheduler.currentTime + 60_000,
            challenge = "challenge_str"
        )
        coEvery { quotationService.getQuotation(any(), any()) } returns mockQuotation
        every { timeFormatter.formatRemainingTime(any()) } returns "01:00"

        viewModel.uiState.test {
            runCurrent()
            expectMostRecentItem() // Clear initial balance state

            // WHEN: Change amount
            viewModel.onEvent(WithdrawalIntent.AmountChanged("1.0"))
            awaitItem() // Consume state: AmountChanged

            // WHEN: Request Quotation
            viewModel.onEvent(WithdrawalIntent.RequestQuotation(OperationType.WITHDRAWAL))
            runCurrent()

            // THEN: Consume Fetching state
            assertTrue(awaitItem().screenContent is ScreenContent.FetchingQuotation)

            // THEN: Consume Show Quotation state
            val finalState = awaitItem()
            assertTrue(finalState.screenContent is ScreenContent.ShowQuotation)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when amount changes THEN current quotation and timer are cleared`() = runTest {
        // GIVEN
        coEvery { quotationService.getQuotation(any(), any()) } returns TransactionQuotation(
            id = "q_123",
            amountInWei = BigInteger("1000000000000000000"),
            feeInWei = BigInteger("10000000000000000"),
            expiresAt = testScheduler.currentTime + 60_000,
            challenge = "challenge_str"
        )

        viewModel.uiState.test {
            runCurrent()
            expectMostRecentItem()

            // Get into Quotation state
            viewModel.onEvent(WithdrawalIntent.AmountChanged("1.0"))
            awaitItem() // AmountChanged
            viewModel.onEvent(WithdrawalIntent.RequestQuotation(OperationType.WITHDRAWAL))
            awaitItem() // Fetching
            awaitItem() // ShowQuotation

            // WHEN amount changes
            viewModel.onEvent(WithdrawalIntent.AmountChanged("2.0"))

            // THEN screenContent must be Idle
            val latest = awaitItem()
            assertEquals("2.0", latest.amountInput)
            assertTrue(latest.screenContent is ScreenContent.Idle)

            cancelAndIgnoreRemainingEvents()
        }
    }
}

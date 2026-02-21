package com.sunday.tranzsign.domain.usecase.signtransaction

import app.cash.turbine.test
import com.sunday.tranzsign.domain.entity.OperationType
import com.sunday.tranzsign.domain.entity.SigningStrategy
import com.sunday.tranzsign.domain.entity.TransactionQuotation
import com.sunday.tranzsign.domain.service.TransactionService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.math.BigInteger

@ExperimentalCoroutinesApi
class SignTransactionUseCaseImplTest {

    private lateinit var transactionService: TransactionService
    private lateinit var useCase: SignTransactionUseCaseImpl

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        transactionService = mockk()
        useCase = SignTransactionUseCaseImpl(transactionService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `execute WHEN service succeeds THEN state emits InProgress then Success`() = runTest {
        // GIVEN
        val mockQuotation = createMockQuotation()
        // The transaction service now takes the quotation directly
        coEvery { transactionService.submit(quotation = mockQuotation, strategy = any()) } returns true

        // WHEN & THEN
        useCase.state.test {
            assertEquals(SigningState.Idle, awaitItem())

            // Execute the use case with the updated signature
            useCase.execute(mockQuotation, SigningStrategy.PASSKEY, OperationType.WITHDRAWAL)

            assertEquals(SigningState.InProgress, awaitItem())
            assertEquals(
                SigningState.Success("Transaction signed and submitted successfully."),
                awaitItem()
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `execute WHEN service fails THEN state emits InProgress then Error`() = runTest {
        // GIVEN
        val mockQuotation = createMockQuotation()
        coEvery { transactionService.submit(quotation = mockQuotation, strategy = any()) } returns false

        // WHEN & THEN
        useCase.state.test {
            assertEquals(SigningState.Idle, awaitItem())

            useCase.execute(mockQuotation, SigningStrategy.PASSKEY, OperationType.WITHDRAWAL)

            assertEquals(SigningState.InProgress, awaitItem())
            assertEquals(SigningState.Error("The transaction could not be signed."), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `reset WHEN called THEN state returns to Idle`() = runTest {
        // GIVEN - A non-idle state
        coEvery { transactionService.submit(any(), any()) } returns false
        useCase.execute(
            createMockQuotation(), SigningStrategy.PASSKEY,
            OperationType.WITHDRAWAL
        )

        // WHEN
        useCase.reset()

        // THEN
        useCase.state.test {
            assertEquals(SigningState.Idle, awaitItem())
        }
    }

    private fun createMockQuotation() = TransactionQuotation(
        id = "qid-123",
        amountInWei = BigInteger("1000000000000000000"), // 1 ETH in WEI
        feeInWei = BigInteger("10000000000000000"), // 0.01 ETH in WEI
        challenge = "challenge-string",
        expiresAt = System.currentTimeMillis() + 60000
    )
}

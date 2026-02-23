package com.sunday.tranzsign.data.source

import com.sunday.tranzsign.domain.entity.EthWalletBalance
import com.sunday.tranzsign.domain.entity.TransactionQuotation
import com.sunday.tranzsign.domain.entity.UserAccount
import com.sunday.tranzsign.domain.service.CoroutineDispatcherProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class InMemoryBackendService @Inject constructor(
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider
) : ApiService {
    private val _ethBalanceInWei = MutableStateFlow(BigInteger("20310000000000000000"))

    private var _lastQuotationTotalDebit: BigInteger = BigInteger.ZERO

    override fun getEthBalance(): Flow<EthWalletBalance> {
        return _ethBalanceInWei.asStateFlow()
            .map { walletBalanceWei ->
                EthWalletBalance(
                    balanceInWei = walletBalanceWei,
                    lastUpdatedMillis = System.currentTimeMillis()
                )
            }
    }

    override suspend fun getUserAccount(): UserAccount =
        withContext(coroutineDispatcherProvider.io) {
            UserAccount(
                id = UUID.randomUUID().toString(),
                username = "Santi Cazorla",
                displayName = "Santi",
                email = "santicazorla@arsenal.com"
            )
        }

    override suspend fun getWithdrawalQuotation(
        amount: String,
        operationType: String
    ): TransactionQuotation {
        Timber.tag(LOG_TAG)
            .i("Mock API(GET): getWithdrawalQuotation called with amount $amount for operation $operationType")
        delay(1500)
        val amountInWei =
            amount.toBigIntegerOrNull() ?: throw IllegalArgumentException("Invalid amount format")

        // The check should also be in WEI. 10 ETH = 10 * 10^18 WEI
        val limitInWei =
            BigDecimal("10").multiply(BigDecimal("1").movePointRight(18)).toBigInteger()
        if (amountInWei > limitInWei) throw IllegalStateException("Amount exceeds the maximum limit of 10 ETH")

        // Fee in WEI (a random value between 0.001 and 0.009 ETH) to simulate variability. 0.001 ETH = 10^15 WEI
        val feeInWei = BigDecimal(Random.nextInt(1, 10))
            .multiply(BigDecimal("1000000000000000")).toBigInteger()

        val totalDebit = amountInWei + feeInWei

        // Let's store the totalDebit to be used in submitWithdrawal, as we don't have a real backend to persist it.
        // In a real app, the quotationId would be used to look this up on the server.
        _lastQuotationTotalDebit = totalDebit

        return TransactionQuotation(
            id = UUID.randomUUID().toString(),
            amountInWei = amountInWei,
            feeInWei = feeInWei,
            challenge = "_mock_challenge_${System.currentTimeMillis()}",
            expiresAt = System.currentTimeMillis() + 60000
        )
    }

    override suspend fun submitTransaction(
        quotationId: String,
        signedChallenge: String,
        signingStrategy: String
    ): Boolean = withContext(coroutineDispatcherProvider.io) {
        Timber.tag(LOG_TAG)
            .i("Mock API(POST): submitWithdrawal called with quotationId $quotationId and signedChallenge $signedChallenge")
        delay(2000)
        // This simulates the server accepting the transaction.
        performDebit(_lastQuotationTotalDebit)
        _lastQuotationTotalDebit = BigInteger.ZERO // Clear after use
        true
    }

    private fun performDebit(amount: BigInteger) {
        Timber.tag(LOG_TAG)
            .i("Mock API(EXTERNAL): Performing debit of $amount WEI from balance")
        _ethBalanceInWei.update { currentBalance ->
            currentBalance.minus(amount)
        }
    }

    companion object {
        private const val LOG_TAG = "InMemoryBackendService"
    }
}

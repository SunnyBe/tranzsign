package com.sunday.tranzsign.data.service

import com.sunday.tranzsign.data.source.ApiService
import com.sunday.tranzsign.domain.entity.SigningStrategy
import com.sunday.tranzsign.domain.entity.TransactionQuotation
import com.sunday.tranzsign.domain.service.TransactionService
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionServiceImpl @Inject constructor(
    private val apiService: ApiService
) : TransactionService {
    override suspend fun submit(
        quotation: TransactionQuotation,
        strategy: SigningStrategy
    ): Boolean {
        Timber.tag("TransactionServiceImpl")
            .d("Submitting transaction with quotation ID ${quotation.id} using strategy ${strategy.name}")
        delay(2000)
        apiService.submitWithdrawal(
            quotationId = quotation.id,
            signedChallenge = quotation.challenge,
            signingStrategy = strategy.name
        )
        return true
    }
}
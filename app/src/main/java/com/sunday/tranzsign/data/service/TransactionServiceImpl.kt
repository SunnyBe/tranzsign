package com.sunday.tranzsign.data.service

import com.sunday.tranzsign.data.source.ApiService
import com.sunday.tranzsign.domain.entity.AuthStrategy
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
        quotationId: String,
        signedChallenge: String,
        authStrategy: AuthStrategy
    ): Boolean {
        Timber.tag("TransactionServiceImpl")
            .d("Submitting transaction with quotation ID $quotationId using strategy ${authStrategy.name}")
        delay(2000)
        return apiService.submitTransaction(
            quotationId = quotationId,
            signedChallenge = signedChallenge,
            signingStrategy = authStrategy.name
        )
    }
}
package com.sunday.tranzsign.data.source

import com.sunday.tranzsign.domain.entity.EthWalletBalance
import com.sunday.tranzsign.domain.entity.TransactionQuotation
import com.sunday.tranzsign.domain.entity.UserAccount
import kotlinx.coroutines.flow.Flow

/**
 * Defines the contract for all backend API communications.
 * This can be implemented by a real network client (like Retrofit) or a mock service.
 *
 * Not the best implementation for a real app, but for the sake of this project, it abstracts away the details of how data is fetched and allows for easy mocking in tests.
 */
interface ApiService {
    fun getEthBalance(): Flow<EthWalletBalance> // assumes websocket or polling for real-time updates
    suspend fun getUserAccount(): UserAccount
    suspend fun getWithdrawalQuotation(
        amount: String,
        operationType: String
    ): TransactionQuotation

    suspend fun submitWithdrawal(
        quotationId: String,
        signedChallenge: String,
        signingStrategy: String
    ): Boolean
}
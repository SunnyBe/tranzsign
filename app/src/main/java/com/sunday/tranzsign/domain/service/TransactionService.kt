package com.sunday.tranzsign.domain.service

import com.sunday.tranzsign.domain.entity.SigningStrategy
import com.sunday.tranzsign.domain.entity.TransactionQuotation

/**
 * Service responsible for submitting transactions based on a given quotation and signing strategy.
 * This service abstracts the logic of transaction submission, including signing and broadcasting
 * the transaction to the network.
 *
 * Transaction metadata like timestamp were skipped for simplicity, but can be added as needed in the future.
 */
interface TransactionService {
    suspend fun submit(
        quotation: TransactionQuotation,
        strategy: SigningStrategy
    ): Boolean
}
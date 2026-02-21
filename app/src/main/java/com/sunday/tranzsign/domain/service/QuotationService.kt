package com.sunday.tranzsign.domain.service

import com.sunday.tranzsign.domain.entity.OperationType
import com.sunday.tranzsign.domain.entity.TransactionQuotation
import java.math.BigInteger

/**
 * Service responsible for fetching transaction quotations based on the amount and operation type.
 * This service abstracts the logic of calculating fees and providing a quotation for a transaction.
 */
interface QuotationService {
    suspend fun getQuotation(
        amountWei: BigInteger,
        operationType: OperationType
    ): TransactionQuotation
}
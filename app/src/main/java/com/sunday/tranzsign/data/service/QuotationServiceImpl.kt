package com.sunday.tranzsign.data.service

import com.sunday.tranzsign.data.source.ApiService
import com.sunday.tranzsign.domain.entity.OperationType
import com.sunday.tranzsign.domain.entity.TransactionQuotation
import com.sunday.tranzsign.domain.service.QuotationService
import timber.log.Timber
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuotationServiceImpl @Inject constructor(
    private val apiService: ApiService
) : QuotationService {
    override suspend fun getQuotation(
        amountWei: BigInteger,
        operationType: OperationType
    ): TransactionQuotation {
        Timber.tag("QuotationServiceImpl")
            .d("Requesting Quotation with amount $amountWei for $operationType")
        return apiService.getWithdrawalQuotation(
            amount = amountWei.toString(),
            operationType = operationType.name
        )
    }
}

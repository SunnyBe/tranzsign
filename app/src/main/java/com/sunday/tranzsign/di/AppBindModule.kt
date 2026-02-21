package com.sunday.tranzsign.di

import com.sunday.tranzsign.data.repository.AccountRepositoryImpl
import com.sunday.tranzsign.data.repository.FeatureRepositoryImpl
import com.sunday.tranzsign.data.service.DateTimeFormatterImpl
import com.sunday.tranzsign.data.service.MoneyFormatterImpl
import com.sunday.tranzsign.data.service.QuotationServiceImpl
import com.sunday.tranzsign.data.service.TransactionServiceImpl
import com.sunday.tranzsign.data.source.ApiService
import com.sunday.tranzsign.data.source.InMemoryBackendService
import com.sunday.tranzsign.domain.repository.AccountRepository
import com.sunday.tranzsign.domain.repository.FeatureRepository
import com.sunday.tranzsign.domain.service.DateTimeFormatter
import com.sunday.tranzsign.domain.service.MoneyFormatter
import com.sunday.tranzsign.domain.service.QuotationService
import com.sunday.tranzsign.domain.service.TransactionService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindModule {

    @Binds
    @Singleton
    abstract fun bindAccountRepository(
        accountRepositoryImpl: AccountRepositoryImpl
    ): AccountRepository

    @Binds
    @Singleton
    abstract fun bindQuotationService(
        quotationServiceImpl: QuotationServiceImpl
    ): QuotationService

    @Binds
    @Singleton
    abstract fun bindTransactionService(
        transactionServiceImpl: TransactionServiceImpl
    ): TransactionService

    @Binds
    @Singleton
    abstract fun bindFeatureRepository(
        featureRepositoryImpl: FeatureRepositoryImpl
    ): FeatureRepository


    @Binds
    @Singleton
    abstract fun bindDateTimeFormatter(
        formatter: DateTimeFormatterImpl
    ): DateTimeFormatter

    @Binds
    @Singleton
    abstract fun bindMoneyFormatter(
        formatter: MoneyFormatterImpl
    ): MoneyFormatter

    @Binds
    @Singleton
    abstract fun bindApiService(
        apiService: InMemoryBackendService
    ): ApiService
}


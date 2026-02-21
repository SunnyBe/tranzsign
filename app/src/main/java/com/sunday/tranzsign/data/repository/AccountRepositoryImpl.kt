package com.sunday.tranzsign.data.repository

import com.sunday.tranzsign.data.source.InMemoryBackendService
import com.sunday.tranzsign.domain.entity.EthWalletBalance
import com.sunday.tranzsign.domain.entity.UserAccount
import com.sunday.tranzsign.domain.repository.AccountRepository
import com.sunday.tranzsign.domain.service.CoroutineDispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val apiService: InMemoryBackendService,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider
) : AccountRepository {

    override fun getEthWalletBalance(): Flow<EthWalletBalance> =
        apiService.getEthBalance().flowOn(coroutineDispatcherProvider.io)

    override fun getActiveUserAccount(): Flow<UserAccount> = flow {
        // BETTER: Make reactive with dedicated UserService. For simplicity, we just fetch once here.
        val userAccount = apiService.getUserAccount()
        emit(userAccount)
    }.flowOn(coroutineDispatcherProvider.io)
}
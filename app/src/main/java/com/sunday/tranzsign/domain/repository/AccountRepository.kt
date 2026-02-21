package com.sunday.tranzsign.domain.repository

import com.sunday.tranzsign.domain.entity.EthWalletBalance
import com.sunday.tranzsign.domain.entity.UserAccount
import kotlinx.coroutines.flow.Flow

/**
 * Manages user account information and Ethereum wallet balance.
 * Wallet and Account are assumed to be linked due to size of project, but in a larger app, these might be separate repositories.
 */
interface AccountRepository {
    fun getEthWalletBalance(): Flow<EthWalletBalance>
    fun getActiveUserAccount(): Flow<UserAccount>
}
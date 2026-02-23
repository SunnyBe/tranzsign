package com.sunday.tranzsign.domain.service

import com.sunday.tranzsign.domain.entity.AuthStrategy
import com.sunday.tranzsign.domain.usecase.signtransaction.SigningRequest

interface SignatureService {
    /**
     * Signs the provided challenge using the specified signing request and authentication strategy.
     * @param signingRequest The request containing the challenge and operation type to be signed.
     * @param authStrategy The strategy to be used for authentication during the signing process (e.g., biometric, password).
     * @return The signed challenge as a hex string.
     */
    suspend fun sign(signingRequest: SigningRequest, authStrategy: AuthStrategy): String
}
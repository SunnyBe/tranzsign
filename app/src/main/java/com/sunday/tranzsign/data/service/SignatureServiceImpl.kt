package com.sunday.tranzsign.data.service

import com.sunday.tranzsign.domain.entity.AuthStrategy
import com.sunday.tranzsign.domain.service.SignatureService
import com.sunday.tranzsign.domain.usecase.signtransaction.SigningRequest
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignatureServiceImpl @Inject constructor() : SignatureService {
    override suspend fun sign(signingRequest: SigningRequest, authStrategy: AuthStrategy): String {
        Timber.tag("SignatureServiceImpl")
            .d("Signing challenge: ${signingRequest.challenge} with strategy: ${authStrategy.name}")
        delay(5000) // Simulate signing delay

        // For demonstration purposes, we return a dummy signature.
        return "0xSignedChallengeFor:${signingRequest.challenge}:authStrategy:${authStrategy.name}"
    }
}
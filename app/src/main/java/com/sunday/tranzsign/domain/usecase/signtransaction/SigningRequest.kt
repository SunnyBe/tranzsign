package com.sunday.tranzsign.domain.usecase.signtransaction

import com.sunday.tranzsign.domain.entity.OperationType

/**
 * Represents a request to sign a transaction, containing the necessary information for the signing process.
 * @param quotationId The unique identifier of the transaction quotation associated with this signing request.
 * @param challenge The challenge string that needs to be signed, typically provided by the backend as part of the transaction signing flow.
 * @param operationType The type of operation being performed (e.g., withdrawal, transfer).
 */
data class SigningRequest(
    val quotationId: String,
    val challenge: String,
    val operationType: OperationType
)
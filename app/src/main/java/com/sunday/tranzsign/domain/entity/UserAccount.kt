package com.sunday.tranzsign.domain.entity

data class UserAccount(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String,
    val sessionId: String? = null
)
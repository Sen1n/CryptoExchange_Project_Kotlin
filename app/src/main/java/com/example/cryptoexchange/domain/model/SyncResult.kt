package com.example.cryptoexchange.domain.model

sealed interface SyncResult {
    data object Success : SyncResult
    data class NetworkError(val message: String) : SyncResult
    data class AuthError(val message: String) : SyncResult
    data class RateLimitError(val retryAfterSeconds: Long?) : SyncResult
    data class ApiError(val message: String) : SyncResult
}
package com.example.cryptoexchange.presentation.state

import com.example.cryptoexchange.domain.model.CryptoCurrency

data class CryptoUiState(
    val cryptos: List<CryptoCurrency> = emptyList(),
    val lastUpdatedMillis: Long? = null,
    val isRefreshing: Boolean = false,
    val isWaitingForNetwork: Boolean = false,
    val errorMessage: String? = null
) {
    val isInitialEmpty: Boolean
        get() = cryptos.isEmpty() && !isRefreshing
}
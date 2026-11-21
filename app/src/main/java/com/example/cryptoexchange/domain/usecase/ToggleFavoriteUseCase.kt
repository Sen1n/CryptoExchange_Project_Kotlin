package com.example.cryptoexchange.domain.usecase

import com.example.cryptoexchange.domain.repository.CryptoRepository

class ToggleFavoriteUseCase(
    private val repository: CryptoRepository
) {
    suspend operator fun invoke(cryptoId: Int) {
        repository.toggleFavorite(cryptoId)
    }
}
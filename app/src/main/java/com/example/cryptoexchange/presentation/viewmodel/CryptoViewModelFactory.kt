package com.example.cryptoexchange.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cryptoexchange.data.worker.CryptoSyncScheduler
import com.example.cryptoexchange.domain.usecase.ObserveCryptosUseCase
import com.example.cryptoexchange.domain.usecase.ToggleFavoriteUseCase

class CryptoViewModelFactory(
    private val observeCryptosUseCase: ObserveCryptosUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val syncScheduler: CryptoSyncScheduler
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CryptoViewModel::class.java)) {
            return CryptoViewModel(observeCryptosUseCase, toggleFavoriteUseCase, syncScheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
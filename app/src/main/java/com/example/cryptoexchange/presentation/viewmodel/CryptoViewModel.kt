package com.example.cryptoexchange.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptoexchange.data.worker.CryptoSyncScheduler
import com.example.cryptoexchange.domain.usecase.ObserveCryptosUseCase
import com.example.cryptoexchange.domain.usecase.ToggleFavoriteUseCase
import com.example.cryptoexchange.presentation.state.CryptoSortOrder
import com.example.cryptoexchange.presentation.state.CryptoUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CryptoViewModel(
    observeCryptosUseCase: ObserveCryptosUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val syncScheduler: CryptoSyncScheduler,
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(CryptoSortOrder.POPULAR)

    val uiState: StateFlow<CryptoUiState> = combine(
        observeCryptosUseCase(),
        syncScheduler.observeWorkSyncState(),
        _sortOrder
    ) { cryptos, workState, currentSort ->

        val processedCryptos = when (currentSort) {
            CryptoSortOrder.POPULAR -> cryptos.sortedBy { it.rank }
            CryptoSortOrder.FAVORITES -> cryptos.filter { it.isFavorite }.sortedBy { it.rank }
            CryptoSortOrder.PRICE_DESC -> cryptos.sortedByDescending { it.priceUsd ?: Double.MIN_VALUE }
            CryptoSortOrder.PRICE_ASC -> cryptos.sortedBy { it.priceUsd ?: Double.MAX_VALUE }
            CryptoSortOrder.GAINERS_24H -> cryptos.sortedByDescending { it.priceChangePercentage24h ?: Double.MIN_VALUE }
            CryptoSortOrder.LOSERS_24H -> cryptos.sortedBy { it.priceChangePercentage24h ?: Double.MAX_VALUE }
        }

        CryptoUiState(
            cryptos = processedCryptos,
            sortOrder = currentSort,
            lastUpdatedMillis = cryptos.maxOfOrNull { it.lastUpdatedMillis },
            isRefreshing = workState.isRunning,
            isWaitingForNetwork = workState.isWaitingForNetwork,
            errorMessage = workState.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CryptoUiState()
    )

    fun onSortOrderChange(newOrder: CryptoSortOrder) {
        _sortOrder.value = newOrder
    }

    fun onToggleFavorite(cryptoId: Int) {
        viewModelScope.launch {
            toggleFavoriteUseCase(cryptoId)
        }
    }

    fun onManualRefreshClick() {
        syncScheduler.triggerManualSync()
    }
}
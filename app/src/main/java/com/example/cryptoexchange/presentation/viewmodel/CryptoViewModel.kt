package com.example.cryptoexchange.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptoexchange.data.worker.CryptoSyncScheduler
import com.example.cryptoexchange.domain.usecase.ObserveCryptosUseCase
import com.example.cryptoexchange.presentation.state.CryptoSortOrder
import com.example.cryptoexchange.presentation.state.CryptoUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class CryptoViewModel(
    observeCryptosUseCase: ObserveCryptosUseCase,
    private val syncScheduler: CryptoSyncScheduler,
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(CryptoSortOrder.POPULAR)

    val uiState: StateFlow<CryptoUiState> = combine(
        observeCryptosUseCase(),
        syncScheduler.observeWorkSyncState(),
        _sortOrder
    ) { cryptos, workState, currentSort ->

        val sortedCryptos = when (currentSort) {
            CryptoSortOrder.POPULAR -> cryptos.sortedBy { it.rank }
            CryptoSortOrder.PRICE_DESC -> cryptos.sortedByDescending { it.priceUsd }
            CryptoSortOrder.PRICE_ASC -> cryptos.sortedBy { it.priceUsd }
        }

        CryptoUiState(
            cryptos = sortedCryptos,
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

    fun onManualRefreshClick() {
        syncScheduler.triggerManualSync()
    }
}
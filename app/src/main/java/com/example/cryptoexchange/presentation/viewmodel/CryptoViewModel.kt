package com.example.cryptoexchange.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptoexchange.data.worker.CryptoSyncScheduler
import com.example.cryptoexchange.domain.usecase.ObserveCryptosUseCase
import com.example.cryptoexchange.presentation.state.CryptoUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class CryptoViewModel(
    observeCryptosUseCase: ObserveCryptosUseCase,
    private val syncScheduler: CryptoSyncScheduler,
) : ViewModel() {

    val uiState: StateFlow<CryptoUiState> = combine(
        observeCryptosUseCase(),
        syncScheduler.observeWorkSyncState()
    ) { cryptos, workState ->
        CryptoUiState(
            cryptos = cryptos,
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

    fun onManualRefreshClick() {
        syncScheduler.triggerManualSync()
    }
}
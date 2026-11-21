package com.example.cryptoexchange

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cryptoexchange.presentation.ui.screen.CryptoListScreen
import com.example.cryptoexchange.presentation.ui.theme.CryptoExchangeTheme
import com.example.cryptoexchange.presentation.viewmodel.CryptoViewModel
import com.example.cryptoexchange.presentation.viewmodel.CryptoViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as CryptoApplication).appContainer
        val viewModelFactory = CryptoViewModelFactory(
            observeCryptosUseCase = appContainer.observeCryptosUseCase,
            toggleFavoriteUseCase = appContainer.toggleFavoriteUseCase,
            syncScheduler = appContainer.syncScheduler
        )

        setContent {
            CryptoExchangeTheme {
                val viewModel: CryptoViewModel = viewModel(factory = viewModelFactory)
                CryptoListScreen(viewModel = viewModel)
            }
        }
    }
}
package com.example.cryptoexchange.presentation.ui.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cryptoexchange.presentation.state.CryptoSortOrder
import com.example.cryptoexchange.presentation.ui.components.CryptoItemCard
import com.example.cryptoexchange.presentation.ui.components.SyncStatusBanner
import com.example.cryptoexchange.presentation.viewmodel.CryptoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoListScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Біржа криптовалют (Top-30)",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (!uiState.isRefreshing) {
                        viewModel.onManualRefreshClick()
                    }
                },
                icon = {
                    if (uiState.isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Оновити"
                        )
                    }
                },
                text = {
                    Text(text = if (uiState.isRefreshing) "Оновлення..." else "Оновити")
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SyncStatusBanner(
                lastUpdatedMillis = uiState.lastUpdatedMillis,
                isRefreshing = uiState.isRefreshing,
                isWaitingForNetwork = uiState.isWaitingForNetwork,
                errorMessage = uiState.errorMessage
            )

            SortFilterRow(
                selectedSortOrder = uiState.sortOrder,
                onSortOrderSelected = { newOrder ->
                    viewModel.onSortOrderChange(newOrder)
                }
            )

            LaunchedEffect(uiState.sortOrder) {
                if (uiState.cryptos.isNotEmpty()) {
                    listState.scrollToItem(0)
                }
            }

            when {
                uiState.cryptos.isEmpty() && uiState.isRefreshing -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Завантаження актуальних курсів...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                uiState.isInitialEmpty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Дані ще не завантажено.\nПідключіться до Інтернету та натисніть «Оновити».",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = uiState.cryptos,
                            key = { it.id }
                        ) { crypto ->
                            CryptoItemCard(crypto = crypto)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SortFilterRow(
    selectedSortOrder: CryptoSortOrder,
    onSortOrderSelected: (CryptoSortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CryptoSortOrder.entries.forEach { order ->
            FilterChip(
                selected = selectedSortOrder == order,
                onClick = { onSortOrderSelected(order) },
                label = {
                    Text(text = order.label)
                }
            )
        }
    }
}
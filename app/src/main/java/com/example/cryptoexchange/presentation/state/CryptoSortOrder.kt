package com.example.cryptoexchange.presentation.state

enum class CryptoSortOrder(val label: String) {
    POPULAR("Найпопулярніші"),
    FAVORITES("Обрані ★"),
    PRICE_DESC("Від найбільшої ціни"),
    PRICE_ASC("Від найменшої ціни"),
    GAINERS_24H("Лідери росту (24г)"),
    LOSERS_24H("Лідери падіння (24г)")
}
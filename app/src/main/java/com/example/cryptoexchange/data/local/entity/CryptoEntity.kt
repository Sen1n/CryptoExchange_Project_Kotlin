package com.example.cryptoexchange.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cryptocurrencies")
data class CryptoEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "symbol")
    val symbol: String,
    @ColumnInfo(name = "logo_url")
    val logoUrl: String?,
    @ColumnInfo(name = "price_usd")
    val priceUsd: Double?,
    @ColumnInfo(name = "price_change_24h")
    val priceChangePercentage24h: Double?,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    @ColumnInfo(name = "rank")
    val rank: Int,
    @ColumnInfo(name = "last_updated_millis")
    val lastUpdatedMillis: Long
)
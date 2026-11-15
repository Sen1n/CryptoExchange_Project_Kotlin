package com.example.cryptoexchange.data.mapper

import com.example.cryptoexchange.data.local.entity.CryptoEntity
import com.example.cryptoexchange.data.remote.dto.CryptoAssetDto
import com.example.cryptoexchange.domain.model.CryptoCurrency
import kotlin.math.abs

fun CryptoEntity.toDomain(): CryptoCurrency = CryptoCurrency(
    id = id,
    name = name,
    symbol = symbol,
    logoUrl = logoUrl,
    priceUsd = priceUsd,
    rank = rank,
    lastUpdatedMillis = lastUpdatedMillis,
)

fun CryptoAssetDto.toEntityOrNull(
    rank: Int,
    updatedAtMillis: Long,
): CryptoEntity? {
    val validSymbol = symbol?.takeIf { it.isNotBlank() }?.uppercase() ?: return null
    val validName = name?.takeIf { it.isNotBlank() } ?: validSymbol
    val validPrice = currentPrice?.takeIf { !it.isNaN() && !it.isInfinite() && (it >= 0.0) }
    val entityId = id?.hashCode()?.let { if (it == Int.MIN_VALUE) 0 else abs(it) } ?: rank

    return CryptoEntity(
        id = entityId,
        name = validName,
        symbol = validSymbol,
        logoUrl = image?.takeIf { it.isNotBlank() },
        priceUsd = validPrice,
        rank = rank,
        lastUpdatedMillis = updatedAtMillis,
    )
}

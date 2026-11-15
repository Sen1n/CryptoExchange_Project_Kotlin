package com.example.cryptoexchange.data.remote.api

import com.example.cryptoexchange.data.remote.dto.CryptoAssetDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CryptoApiService {
    @GET("coins/markets")
    suspend fun getTopCryptos(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1,
    ): Response<List<CryptoAssetDto>>
}

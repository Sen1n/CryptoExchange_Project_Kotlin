package com.example.cryptoexchange.di

import com.example.cryptoexchange.domain.usecase.ToggleFavoriteUseCase
import android.content.Context
import com.example.cryptoexchange.data.local.database.CryptoDatabase
import com.example.cryptoexchange.data.remote.api.CryptoApiService
import com.example.cryptoexchange.data.repository.CryptoRepositoryImpl
import com.example.cryptoexchange.data.worker.CryptoSyncScheduler
import com.example.cryptoexchange.domain.repository.CryptoRepository
import com.example.cryptoexchange.domain.usecase.ObserveCryptosUseCase
import com.example.cryptoexchange.domain.usecase.SyncCryptosUseCase
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val apiService: CryptoApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.coingecko.com/api/v3/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CryptoApiService::class.java)
    }

    private val database: CryptoDatabase by lazy {
        CryptoDatabase.getInstance(context)
    }

    val cryptoRepository: CryptoRepository by lazy {
        CryptoRepositoryImpl(
            apiService = apiService,
            cryptoDao = database.cryptoDao(),
        )
    }

    val observeCryptosUseCase: ObserveCryptosUseCase by lazy {
        ObserveCryptosUseCase(cryptoRepository)
    }

    val toggleFavoriteUseCase: ToggleFavoriteUseCase by lazy {
        ToggleFavoriteUseCase(cryptoRepository)
    }

    val syncCryptosUseCase: SyncCryptosUseCase by lazy {
        SyncCryptosUseCase(cryptoRepository)
    }

    val syncScheduler: CryptoSyncScheduler by lazy {
        CryptoSyncScheduler(context)
    }
}

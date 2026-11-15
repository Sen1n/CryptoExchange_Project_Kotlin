package com.example.cryptoexchange.data.repository

import com.example.cryptoexchange.data.local.dao.CryptoDao
import com.example.cryptoexchange.data.mapper.toDomain
import com.example.cryptoexchange.data.mapper.toEntityOrNull
import com.example.cryptoexchange.data.remote.api.CryptoApiService
import com.example.cryptoexchange.domain.model.CryptoCurrency
import com.example.cryptoexchange.domain.model.SyncResult
import com.example.cryptoexchange.domain.repository.CryptoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException

class CryptoRepositoryImpl(
    private val apiService: CryptoApiService,
    private val cryptoDao: CryptoDao,
) : CryptoRepository {

    override fun observeCryptos(): Flow<List<CryptoCurrency>> {
        return cryptoDao.observeAllSortedByRank().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncCryptos(): SyncResult {
        return try {
            val response = apiService.getTopCryptos()
            if (!response.isSuccessful) {
                return when (response.code()) {
                    401, 403 -> SyncResult.AuthError("Помилка доступу API (${response.code()})")
                    429 -> {
                        val retryAfter = response.headers()["Retry-After"]?.toLongOrNull()
                        SyncResult.RateLimitError(retryAfter)
                    }
                    in 500..599 -> SyncResult.NetworkError("Сервер тимчасово недоступний (${response.code()})")
                    else -> SyncResult.ApiError("Помилка API: HTTP ${response.code()}")
                }
            }

            val rawList = response.body()
            if (rawList.isNullOrEmpty()) {
                return SyncResult.ApiError("Отримано порожній або некоректний список активів")
            }

            val nowMillis = System.currentTimeMillis()
            val entities = rawList
                .take(30)
                .mapIndexedNotNull { index, dto ->
                    dto.toEntityOrNull(rank = index + 1, updatedAtMillis = nowMillis)
                }

            if (entities.isEmpty()) {
                return SyncResult.ApiError("Не вдалося розпізнати жодного валідного активу")
            }

            cryptoDao.replaceTopList(entities)
            SyncResult.Success
        } catch (e: IOException) {
            SyncResult.NetworkError("Відсутнє з'єднання або таймаут мережі")
        } catch (e: Exception) {
            SyncResult.ApiError("Помилка обробки даних: ${e.localizedMessage ?: "невідома помилка"}")
        }
    }
}

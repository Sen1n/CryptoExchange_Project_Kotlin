package com.example.cryptoexchange.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.cryptoexchange.CryptoApplication
import com.example.cryptoexchange.domain.model.SyncResult

class CryptoSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val appContainer = (applicationContext as CryptoApplication).appContainer
        return when (val result = appContainer.syncCryptosUseCase()) {
            is SyncResult.Success -> Result.success()

            is SyncResult.AuthError -> Result.failure(
                workDataOf(KEY_ERROR_MESSAGE to result.message)
            )

            is SyncResult.RateLimitError -> {
                if (runAttemptCount >= MAX_RETRY_ATTEMPTS) {
                    Result.failure(
                        workDataOf(KEY_ERROR_MESSAGE to "Перевищено ліміт запитів API (HTTP 429). Спробуйте пізніше.")
                    )
                } else {
                    Result.retry()
                }
            }

            is SyncResult.NetworkError -> {
                if (runAttemptCount >= MAX_RETRY_ATTEMPTS) {
                    Result.failure(
                        workDataOf(KEY_ERROR_MESSAGE to result.message)
                    )
                } else {
                    Result.retry()
                }
            }

            is SyncResult.ApiError -> Result.failure(
                workDataOf(KEY_ERROR_MESSAGE to result.message)
            )
        }
    }

    companion object {
        const val KEY_ERROR_MESSAGE = "key_error_message"
        private const val MAX_RETRY_ATTEMPTS = 3
    }
}
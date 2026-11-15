package com.example.cryptoexchange.data.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.concurrent.TimeUnit

data class WorkSyncState(
    val isRunning: Boolean = false,
    val isWaitingForNetwork: Boolean = false,
    val errorMessage: String? = null,
)

class CryptoSyncScheduler(context: Context) {

    private val workManager = WorkManager.getInstance(context.applicationContext)

    @Volatile
    private var lastManualSyncTimeMs: Long = 0L

    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun schedulePeriodicSync() {
        val periodicRequest = PeriodicWorkRequestBuilder<CryptoSyncWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(networkConstraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            UNIQUE_PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }

    fun triggerManualSync() {
        val currentTime = System.currentTimeMillis()
        if ((currentTime - lastManualSyncTimeMs) < COOLDOWN_MILLIS) {
            return
        }
        lastManualSyncTimeMs = currentTime

        val oneTimeRequest = OneTimeWorkRequestBuilder<CryptoSyncWorker>()
            .setConstraints(networkConstraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                15,
                TimeUnit.SECONDS
            )
            .build()

        // ExistingWorkPolicy.KEEP захищає від дублювання активної ручної задачі
        workManager.enqueueUniqueWork(
            UNIQUE_MANUAL_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            oneTimeRequest
        )
    }

    fun observeWorkSyncState(): Flow<WorkSyncState> {
        val manualFlow = workManager.getWorkInfosForUniqueWorkFlow(UNIQUE_MANUAL_WORK_NAME)
        val periodicFlow = workManager.getWorkInfosForUniqueWorkFlow(UNIQUE_PERIODIC_WORK_NAME)

        return combine(manualFlow, periodicFlow) { manualInfos, periodicInfos ->
            val activeManual = manualInfos.firstOrNull()
            val activePeriodic = periodicInfos.firstOrNull()

            val isRunning = activeManual?.state == WorkInfo.State.RUNNING ||
                    activeManual?.state == WorkInfo.State.ENQUEUED ||
                    activePeriodic?.state == WorkInfo.State.RUNNING

            val isWaitingForNetwork = activeManual?.state == WorkInfo.State.ENQUEUED

            val errorMessage = when {
                activeManual?.state == WorkInfo.State.FAILED ->
                    activeManual.outputData.getString(CryptoSyncWorker.KEY_ERROR_MESSAGE)
                        ?: "Не вдалося оновити дані"
                activePeriodic?.state == WorkInfo.State.FAILED ->
                    activePeriodic.outputData.getString(CryptoSyncWorker.KEY_ERROR_MESSAGE)
                else -> null
            }

            WorkSyncState(
                isRunning = isRunning,
                isWaitingForNetwork = isWaitingForNetwork,
                errorMessage = errorMessage
            )
        }
    }

    companion object {
        const val UNIQUE_PERIODIC_WORK_NAME = "crypto_periodic_sync_work"
        const val UNIQUE_MANUAL_WORK_NAME = "crypto_manual_sync_work"
        private const val COOLDOWN_MILLIS = 3_000L
    }
}
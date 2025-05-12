package com.simprints.infra.enrolment.records.repository.local.migration

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.REALM_DB_MIGRATION
import com.simprints.infra.logging.Simber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealmToRoomMigrationScheduler @Inject constructor(
    private val realmToRoomMigrationFlagsStore: RealmToRoomMigrationFlagsStore,
    private val workManager: WorkManager,
) {
    suspend fun scheduleMigrationWorkerIfNeeded() {
        val currentStatus = realmToRoomMigrationFlagsStore.getCurrentStatus()
        log("Current migration status for scheduling: $currentStatus")

        if (!realmToRoomMigrationFlagsStore.isMigrationGloballyEnabled() ||
            !realmToRoomMigrationFlagsStore.canRetry() ||
            currentStatus == MigrationStatus.COMPLETED
        ) {
            log(" Worker not scheduled. Migration is disabled, max retries reached or already completed.")
            workManager.cancelUniqueWork(RealmToRoomMigrationWorker.Companion.WORK_NAME)
            return
        }

        when (currentStatus) {
            MigrationStatus.FAILED -> {
                log("Migration status is FAILED and can retry. Scheduling worker.")
                enqueueMigrationWork()
            }
            MigrationStatus.NOT_STARTED,
            MigrationStatus.IN_PROGRESS,
            -> { // If app was killed during these states
                log("Migration status is $currentStatus. Scheduling worker to start.")
                enqueueMigrationWork()
            }
            else -> {
                // No action needed for COMPLETED or any other state
            }
        }
    }

    private fun enqueueMigrationWork() {
        val constraints = Constraints
            .Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresStorageNotLow(true)
            .setRequiresBatteryNotLow(true)
            .build()

        val migrationWorkRequest = OneTimeWorkRequestBuilder<RealmToRoomMigrationWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BACKOFF_DELAY_SECONDS, // Initial delay
                TimeUnit.SECONDS,
            ).addTag(RealmToRoomMigrationWorker.Companion.WORK_NAME)
            .build()

        workManager.enqueueUniqueWork(
            RealmToRoomMigrationWorker.Companion.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            migrationWorkRequest,
        )
        log("RealmToRoomMigrationWorker enqueued with policy KEEP.")
    }

    private fun log(message: String) {
        Simber.i(message, tag = REALM_DB_MIGRATION)
    }

    companion object {
        private const val BACKOFF_DELAY_SECONDS = 60L
    }
}

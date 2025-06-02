package com.simprints.infra.enrolment.records.repository.local.migration

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
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
        if (currentStatus == MigrationStatus.COMPLETED) {
            log("Worker not scheduled. Migration already completed.")
            workManager.cancelUniqueWork(RealmToRoomMigrationWorker.WORK_NAME)
            return
        }
        if (!realmToRoomMigrationFlagsStore.isMigrationGloballyEnabled()) {
            log("Worker not scheduled. Migration is globally disabled.")
            workManager.cancelUniqueWork(RealmToRoomMigrationWorker.WORK_NAME)
            return
        }
        if (!realmToRoomMigrationFlagsStore.canRetry()) {
            log("Worker not scheduled. Max retries reached.")
            workManager.cancelUniqueWork(RealmToRoomMigrationWorker.WORK_NAME)
            return
        }
        log("Scheduling RealmToRoomMigrationWorker...")
        enqueueMigrationWork()
    }

    private fun enqueueMigrationWork() {
        val constraints = Constraints
            .Builder()
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
        Simber.i("[RealmToRoomMigrationScheduler] $message", tag = REALM_DB_MIGRATION)
    }

    companion object {
        private const val BACKOFF_DELAY_SECONDS = 60L
    }
}

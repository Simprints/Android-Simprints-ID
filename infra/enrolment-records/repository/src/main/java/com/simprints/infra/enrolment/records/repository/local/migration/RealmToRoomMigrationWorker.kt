package com.simprints.infra.enrolment.records.repository.local.migration

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.simprints.core.DispatcherIO
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.repository.local.RealmEnrolmentRecordLocalDataSource
import com.simprints.infra.enrolment.records.repository.local.RoomEnrolmentRecordLocalDataSource
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.REALM_DB_MIGRATION
import com.simprints.infra.logging.Simber
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlin.time.measureTime
import com.simprints.infra.resources.R as IDR

@HiltWorker
internal class RealmToRoomMigrationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val realmDataSource: RealmEnrolmentRecordLocalDataSource,
    private val roomDataSource: RoomEnrolmentRecordLocalDataSource,
    private val realmToRoomMigrationFlagsStore: RealmToRoomMigrationFlagsStore,
    private val configRepo: ConfigRepository,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(appContext, workerParams) {
    companion object {
        const val BATCH_SIZE = 500
        const val WORK_NAME = "room-to-realm-migration-worker-v1"
    }

    override val tag: String
        get() = REALM_DB_MIGRATION.name

    lateinit var project: Project

    override suspend fun doWork(): Result = withContext(dispatcher) {
        project = configRepo.getProject()
        crashlyticsLog("[RealmToRoomMigrationWorker] MigrationWorker started.")
        try {
            showProgressNotification()
            // 1. Check if down sync is in progress to retry latter (no need to increase the retry count)
            if (realmToRoomMigrationFlagsStore.isDownSyncInProgress()) {
                realmToRoomMigrationFlagsStore.updateStatus(MigrationStatus.NOT_STARTED)
                crashlyticsLog("[RealmToRoomMigrationWorker] Down sync is in progress. pausing migration.")
                return@withContext retry()
            }

            realmToRoomMigrationFlagsStore.updateStatus(MigrationStatus.IN_PROGRESS)
            // 2. Check if realm data source is empty
            val recordsCount = realmDataSource.count(SubjectQuery())
            if (recordsCount > 0) {
                // 3. empty the room database
                roomDataSource.deleteAll()
                // 4. Process Records
                val time = measureTime {
                    processRecords()
                }
                // Log migration duration for debugging purposes
                // Do not modify this log format; it is required for reporting
                crashlyticsLog(
                    "[RealmToRoomMigrationWorker] Migration finished: $recordsCount records migrated in ${time.inWholeMilliseconds}ms",
                )
            } else {
                crashlyticsLog("[RealmToRoomMigrationWorker] No records to migrate. Skipping migration.")
            }
            realmToRoomMigrationFlagsStore.updateStatus(MigrationStatus.COMPLETED)
            realmToRoomMigrationFlagsStore.resetRetryCount()
            return@withContext success()
        } catch (e: Exception) {
            Simber.e("[RealmToRoomMigrationWorker] Migration failed: ${e.message}", e)
            realmToRoomMigrationFlagsStore.incrementRetryCount()
            realmToRoomMigrationFlagsStore.updateStatus(MigrationStatus.FAILED)
            roomDataSource.deleteAll()
            return@withContext fail(e, message = "Migration failed: ${e.message}")
        }
    }

    override val notificationDescription = IDR.string.notification_migration_description

    private suspend fun processRecords() {
        // log realm db info
        crashlyticsLog("[RealmToRoomMigrationWorker] ${realmDataSource.getLocalDBInfo()}")
        var index = 0
        realmDataSource
            .loadAllSubjectsInBatches(BATCH_SIZE)
            .collect { realmSubjectsBatch ->
                crashlyticsLog("[RealmToRoomMigrationWorker] Processing batch ${++index} ...")
                try {
                    val subjectCreationActions = realmSubjectsBatch.map {
                        SubjectAction.Creation(it)
                    }
                    roomDataSource.performActions(subjectCreationActions, project)
                } catch (e: Exception) {
                    Simber.e("Error processing batch $index: ${e.message}", e)
                    throw e
                }
            }
        // log room db info
        crashlyticsLog("[RealmToRoomMigrationWorker] ${roomDataSource.getLocalDBInfo()}")
    }
}

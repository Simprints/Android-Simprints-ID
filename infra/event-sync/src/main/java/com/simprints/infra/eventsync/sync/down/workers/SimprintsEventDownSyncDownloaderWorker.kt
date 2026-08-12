package com.simprints.infra.eventsync.sync.down.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.simprints.core.DispatcherBG
import com.simprints.infra.authstore.exceptions.RemoteDbNotSignedInException
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.enrolment.records.repository.local.migration.RealmToRoomMigrationFlagsStore
import com.simprints.infra.events.EventRepository
import com.simprints.infra.eventsync.event.remote.exceptions.TooManyRequestsException
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.OUTPUT_ESTIMATED_MAINTENANCE_TIME
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_RELOGIN_REQUIRED
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_TOO_MANY_REQUESTS
import com.simprints.infra.eventsync.sync.down.tasks.BaseEventDownSyncTask
import com.simprints.infra.eventsync.sync.down.tasks.SimprintsEventDownSyncTask
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher

@HiltWorker
internal class SimprintsEventDownSyncDownloaderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    eventDownSyncScopeRepository: EventDownSyncScopeRepository,
    syncCache: EventSyncCache,
    eventRepository: EventRepository,
    configRepository: ConfigRepository,
    @DispatcherBG dispatcher: CoroutineDispatcher,
    private val downSyncTask: SimprintsEventDownSyncTask,
    realmToRoomMigrationFlagsStore: RealmToRoomMigrationFlagsStore,
) : BaseEventDownSyncDownloaderWorker(
        context,
        params,
        eventDownSyncScopeRepository,
        syncCache,
        eventRepository,
        configRepository,
        dispatcher,
        realmToRoomMigrationFlagsStore,
    ) {
    override fun createDownSyncTask(): BaseEventDownSyncTask = downSyncTask

    override fun handleSyncException(
        t: Throwable,
        count: Int,
        max: Int?,
    ): Result {
        val outputData = Data.Builder()
            .putInt(OUTPUT_DOWN_SYNC, count)
            .putInt(OUTPUT_DOWN_MAX_SYNC, max ?: 0)

        when (t) {
            is BackendMaintenanceException -> {
                outputData
                    .putBoolean(OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE, true)
                    .putLong(OUTPUT_ESTIMATED_MAINTENANCE_TIME, t.estimatedOutage ?: 0L)
                Simber.i("Down-sync completed with recoverable issue", t, tag = tag)
            }

            is SyncCloudIntegrationException -> {
                outputData.putBoolean(OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION, true)
                Simber.i("Down-sync completed with recoverable issue", t, tag = tag)
            }

            is TooManyRequestsException -> {
                outputData.putBoolean(OUTPUT_FAILED_BECAUSE_TOO_MANY_REQUESTS, true)
                Simber.i("Down-sync completed with recoverable issue", t, tag = tag)
            }

            is RemoteDbNotSignedInException -> {
                outputData.putBoolean(OUTPUT_FAILED_BECAUSE_RELOGIN_REQUIRED, true)
                Simber.i("Down-sync completed with recoverable issue", t, tag = tag)
            }

            is IllegalArgumentException -> {
                Simber.i("Down-sync completed with recoverable issue", t, tag = tag)
            }

            else -> {
                Simber.e("Down-sync completed with unexpected issue", t, tag = tag)
            }
        }

        return success(outputData.build(), "Completed with down-sync error: ${t.message}")
    }
}

package com.simprints.infra.eventsync.sync.down.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.DispatcherBG
import com.simprints.core.tools.json.JsonHelper
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
    jsonHelper: JsonHelper,
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
    jsonHelper,
    eventRepository,
    configRepository,
    dispatcher,
    realmToRoomMigrationFlagsStore,
) {

    override fun createDownSyncTask(): BaseEventDownSyncTask = downSyncTask

    override fun handleSyncException(t: Throwable) = when (t) {
        is IllegalArgumentException -> fail(t, t.message)

        is BackendMaintenanceException -> fail(
            t,
            t.message,
            workDataOf(
                OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE to true,
                OUTPUT_ESTIMATED_MAINTENANCE_TIME to t.estimatedOutage,
            ),
        )

        is SyncCloudIntegrationException -> fail(t, t.message, workDataOf(OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION to true))
        is TooManyRequestsException -> fail(t, t.message, workDataOf(OUTPUT_FAILED_BECAUSE_TOO_MANY_REQUESTS to true))
        is RemoteDbNotSignedInException -> fail(t, t.message, workDataOf(OUTPUT_FAILED_BECAUSE_RELOGIN_REQUIRED to true))
        else -> retry(t)
    }
}

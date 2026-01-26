package com.simprints.infra.eventsync.sync.down.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.DispatcherBG
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.enrolment.records.repository.local.migration.RealmToRoomMigrationFlagsStore
import com.simprints.infra.events.EventRepository
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_COMMCARE_PERMISSION_MISSING
import com.simprints.infra.eventsync.sync.down.tasks.BaseEventDownSyncTask
import com.simprints.infra.eventsync.sync.down.tasks.CommCareEventSyncTask
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher

@HiltWorker
internal class CommCareEventSyncDownloaderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    eventDownSyncScopeRepository: EventDownSyncScopeRepository,
    syncCache: EventSyncCache,
    eventRepository: EventRepository,
    configRepository: ConfigRepository,
    @DispatcherBG dispatcher: CoroutineDispatcher,
    private val commCareSyncTask: CommCareEventSyncTask,
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
    override fun createDownSyncTask(): BaseEventDownSyncTask = commCareSyncTask

    override fun handleSyncException(t: Throwable) = when (t) {
        is IllegalArgumentException -> fail(t, t.message)
        is SecurityException -> fail(t, t.message, workDataOf(OUTPUT_FAILED_BECAUSE_COMMCARE_PERMISSION_MISSING to true))
        else -> retry(t)
    }
}

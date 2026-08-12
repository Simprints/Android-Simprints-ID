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
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_RELOGIN_REQUIRED
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_COMMCARE_PERMISSION_MISSING
import com.simprints.infra.eventsync.sync.down.tasks.BaseEventDownSyncTask
import com.simprints.infra.eventsync.sync.down.tasks.CommCareEventSyncTask
import com.simprints.infra.logging.Simber
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

    override fun handleSyncException(
        t: Throwable,
        count: Int,
        max: Int?,
    ): Result {
        val outputData = Data.Builder()
            .putInt(OUTPUT_DOWN_SYNC, count)
            .putInt(OUTPUT_DOWN_MAX_SYNC, max ?: 0)

        when (t) {
            is SecurityException -> {
                outputData.putBoolean(OUTPUT_FAILED_BECAUSE_COMMCARE_PERMISSION_MISSING, true)
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

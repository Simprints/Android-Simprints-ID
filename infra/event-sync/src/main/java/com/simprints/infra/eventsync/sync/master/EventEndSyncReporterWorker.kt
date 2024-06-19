package com.simprints.infra.eventsync.sync.master

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.simprints.core.DispatcherBG
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.scope.EventScopeEndCause
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * It's executed at the end of the sync, when all workers succeed (downloaders and uploaders).
 * It stores the "last successful timestamp"
 */
@HiltWorker
internal class EventEndSyncReporterWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncCache: EventSyncCache,
    private val eventRepository: EventRepository,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(appContext, params) {

    override val tag: String = EventEndSyncReporterWorker::class.java.simpleName

    override suspend fun doWork(): Result =
        withContext(dispatcher) {
            try {
                showProgressNotification()
                val syncId = inputData.getString(SYNC_ID_TO_MARK_AS_COMPLETED)
                crashlyticsLog("Start - Params: $syncId")

                inputData.getString(EVENT_DOWN_SYNC_SCOPE_TO_CLOSE)?.let { scopeId ->
                    eventRepository.closeEventScope(scopeId, EventScopeEndCause.WORKFLOW_ENDED)
                }

                inputData.getString(EVENT_UP_SYNC_SCOPE_TO_CLOSE)?.let { scopeId ->
                    eventRepository.closeEventScope(scopeId, EventScopeEndCause.WORKFLOW_ENDED)
                }

                if (!syncId.isNullOrEmpty()) {
                    syncCache.storeLastSuccessfulSyncTime(Date())
                    success()
                } else {
                    throw IllegalArgumentException("SyncId missed")
                }
            } catch (t: Throwable) {
                fail(t)
            }
        }

    companion object {

        const val SYNC_ID_TO_MARK_AS_COMPLETED = "SYNC_ID_TO_MARK_AS_COMPLETED"
        const val EVENT_DOWN_SYNC_SCOPE_TO_CLOSE = "EVENT_DOWN_SYNC_SCOPE_TO_CLOSE "
        const val EVENT_UP_SYNC_SCOPE_TO_CLOSE = "EVENT_UP_SYNC_SCOPE_TO_CLOSE "
    }
}

package com.simprints.infra.eventsync.sync.master

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.simprints.core.DispatcherBG
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.*

/**
 * It's executed at the end of the sync, when all workers succeed (downloaders and uploaders).
 * It stores the "last successful timestamp"
 */
@HiltWorker
internal class EventEndSyncReporterWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncCache: EventSyncCache,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(appContext, params) {

    override val tag: String = EventEndSyncReporterWorker::class.java.simpleName

    override suspend fun doWork(): Result =
        withContext(dispatcher) {
            try {
                val syncId = inputData.getString(SYNC_ID_TO_MARK_AS_COMPLETED)
                crashlyticsLog("Start - Params: $syncId")
                showProgressNotification()

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
    }
}

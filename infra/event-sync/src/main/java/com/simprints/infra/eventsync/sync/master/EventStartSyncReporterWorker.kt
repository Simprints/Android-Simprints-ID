package com.simprints.infra.eventsync.sync.master

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.simprints.core.DispatcherBG
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.logging.Simber
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * It's executed at the beginning of a sync and it sets in the output the unique sync id.
 * The PeopleSyncMasterWorker creates the unique id, but it can't set it as output because
 * it's a periodic worker.
 * The periodic workers transits immediately from SUCCESS to ENQUEUED for the next round.
 * When it's in ENQUEUED the outputData is erased, so we can't extract the uniqueId observing
 * PeopleStartSyncReporterWorker.
 */
@HiltWorker
internal class EventStartSyncReporterWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(appContext, params) {
    override val tag: String = "EventStartSyncReporter"

    override suspend fun doWork(): Result = withContext(dispatcher) {
        crashlyticsLog("Started")
        showProgressNotification()
        try {
            val syncId = inputData.getString(SYNC_ID_STARTED)
            Simber.d("Params: $syncId", tag = tag)
            success(inputData)
        } catch (t: Throwable) {
            fail(t)
        }
    }

    companion object {
        const val SYNC_ID_STARTED = "SYNC_ID_STARTED"
    }
}

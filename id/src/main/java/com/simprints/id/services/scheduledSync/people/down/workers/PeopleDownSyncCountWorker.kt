package com.simprints.id.services.scheduledSync.people.down.workers

import android.content.Context
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.down_sync_info.DownSyncScopeRepository
import com.simprints.id.data.db.down_sync_info.domain.DownSyncScope
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.services.scheduledSync.people.common.SimCoroutineWorker
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_LAST_SYNC_ID
import javax.inject.Inject

class CountWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params) {

    companion object {
        const val OUTPUT_COUNT_WORKER_DOWN = "OUTPUT_COUNT_WORKER_DOWN"
    }

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var personRepository: PersonRepository
    @Inject lateinit var downSyncScopeRepository: DownSyncScopeRepository

    override suspend fun doWork(): Result {
        return try {
            getComponent<CountWorker> { it.inject(this) }

            val downSyncScope = downSyncScopeRepository.getDownSyncScope()
            logMessageForCrashReport<CountWorker>("Sync - Preparing request for $downSyncScope")

            execute(downSyncScope)
        } catch (t: Throwable) {
            t.printStackTrace()
            logFailure<CountWorker>("Sync - Failed ${tags.firstOrNull { it.contains(TAG_LAST_SYNC_ID) }}", t)

            resultSetter.failure()
        }
    }

    private suspend fun execute(downSyncScope: DownSyncScope): Result {
        return try {
            val downCount = getUpCount(downSyncScope)

            logSuccess<CountWorker>("Sync - Executing task done for $downSyncScope ${tags.firstOrNull { it.contains(TAG_LAST_SYNC_ID) }}")

            resultSetter.success(workDataOf(
                OUTPUT_COUNT_WORKER_DOWN to JsonHelper.gson.toJson(downCount))
            )
        } catch (t: Throwable) {
            logFailure<CountWorker>("Sync - Failed on executing task for  $downSyncScope ${tags.firstOrNull { it.contains(TAG_LAST_SYNC_ID) }}", t)

            resultSetter.retry()
        }
    }

    private suspend fun getUpCount(syncScope: DownSyncScope) =
        personRepository.countToDownSync(syncScope)
}


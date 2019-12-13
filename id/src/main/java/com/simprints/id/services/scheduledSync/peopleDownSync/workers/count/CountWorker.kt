package com.simprints.id.services.scheduledSync.peopleDownSync.workers.count

import android.content.Context
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.down_sync_info.DownSyncScopeRepository
import com.simprints.id.data.db.down_sync_info.domain.DownSyncScope
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.SimCoroutineWorker
import javax.inject.Inject

class CountWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params) {

    companion object {
        const val COUNT_PROGRESS = "COUNT_PROGRESS"
    }

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var personRepository: PersonRepository
    @Inject lateinit var downSyncScopeRepository: DownSyncScopeRepository

    override suspend fun doWork(): Result {
        return try {
            getComponent<CountWorker> { it.inject(this) }

            val downSyncScope = downSyncScopeRepository.getDownSyncScope()
            logMessageForCrashReport<CountWorker>("Preparing request for $downSyncScope")

            execute(downSyncScope)
        } catch (t: Throwable) {
            t.printStackTrace()
            resultSetter.failure()
        }
    }

    private suspend fun execute(downSyncScope: DownSyncScope): Result {
        return try {
            val counts = getCounts(downSyncScope)
            logSuccess<CountWorker>("Executing task done for $downSyncScope")

            resultSetter.success(workDataOf(COUNT_PROGRESS to JsonHelper.gson.toJson(counts)))
        } catch (t: Throwable) {
            logFailure<CountWorker>("Failed on executing task for  $downSyncScope", t)

            resultSetter.retry()
        }
    }

    private suspend fun getCounts(syncScope: DownSyncScope) =
        personRepository.countToDownSync(syncScope)
}


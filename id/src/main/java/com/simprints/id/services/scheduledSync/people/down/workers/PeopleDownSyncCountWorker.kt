package com.simprints.id.services.scheduledSync.people.down.workers

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.reflect.TypeToken
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.common.models.PeopleCount
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncScope
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.services.scheduledSync.people.common.SimCoroutineWorker
import javax.inject.Inject

class PeopleDownSyncCountWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params) {

    companion object {
        const val OUTPUT_COUNT_WORKER_DOWN = "OUTPUT_COUNT_WORKER_DOWN"
    }

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var personRepository: PersonRepository
    @Inject lateinit var downSyncScopeRepository: PeopleDownSyncScopeRepository

    override suspend fun doWork(): Result {
        return try {
            getComponent<PeopleDownSyncCountWorker> { it.inject(this) }

            val downSyncScope = downSyncScopeRepository.getDownSyncScope()
            crashlyticsLog("Preparing count request for $downSyncScope")

            execute(downSyncScope)
        } catch (t: Throwable) {
            logFailure(t)
            resultSetter.failure()
        }
    }

    private suspend fun execute(downSyncScope: PeopleDownSyncScope): Result {
        return try {
            val downCount = getDownCount(downSyncScope)
            val output = JsonHelper.gson.toJson(downCount)

            logSuccess("Count done for $downSyncScope: $output}")

            resultSetter.success(workDataOf(
                OUTPUT_COUNT_WORKER_DOWN to output)
            )
        } catch (t: Throwable) {
            logFailure(t)
            resultSetter.retry()
        }
    }

    private suspend fun getDownCount(syncScope: PeopleDownSyncScope) =
        personRepository.countToDownSync(syncScope)

    private fun logFailure(t: Throwable) =
        logFailure<PeopleDownSyncCountWorker>(t)

    private fun logSuccess(message: String) =
        logSuccess<PeopleDownSyncCountWorker>(message)

    private fun crashlyticsLog(message: String) =
        crashReportLog<PeopleDownSyncCountWorker>(message)

}

fun WorkInfo.getDownCountsFromOutput(): List<PeopleCount>? {
    val outputJson = this.outputData.getString(PeopleDownSyncCountWorker.OUTPUT_COUNT_WORKER_DOWN)
    val listType = object : TypeToken<ArrayList<PeopleCount?>?>() {}.type
    return JsonHelper.gson.fromJson<List<PeopleCount>>(outputJson, listType)
}


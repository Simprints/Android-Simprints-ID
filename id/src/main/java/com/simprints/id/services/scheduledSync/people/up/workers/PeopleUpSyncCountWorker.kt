package com.simprints.id.services.scheduledSync.people.up.workers

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.common.models.PeopleCount
import com.simprints.id.data.db.people_sync.up.PeopleUpSyncScopeRepository
import com.simprints.id.data.db.people_sync.up.domain.PeopleUpSyncScope
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.services.scheduledSync.people.common.SimCoroutineWorker
import javax.inject.Inject

class PeopleUpSyncCountWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params) {

    companion object {
        const val OUTPUT_COUNT_WORKER_UP = "OUTPUT_COUNT_WORKER_UP"
    }

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var personRepository: PersonRepository
    @Inject lateinit var peopleUpSyncScopeRepository: PeopleUpSyncScopeRepository

    override suspend fun doWork(): Result {
        return try {
            getComponent<PeopleUpSyncCountWorker> { it.inject(this) }

            val upSyncScope = peopleUpSyncScopeRepository.getUpSyncScope()
            crashlyticsLog("Preparing upSync counter for $upSyncScope")

            execute(upSyncScope)
        } catch (t: Throwable) {
            logFailure(t)
            resultSetter.failure()
        }
    }

    private fun execute(upSyncScope: PeopleUpSyncScope): Result {
        val upCount = getUpCount(upSyncScope)
        val output = JsonHelper.gson.toJson(upCount)

        logSuccess("Up count task done for $upSyncScope: $output")

        return resultSetter.success(workDataOf(
            OUTPUT_COUNT_WORKER_UP to JsonHelper.gson.toJson(upCount))
        )
    }

    private fun getUpCount(syncScope: PeopleUpSyncScope) =
        PeopleCount(created = personRepository.count(PersonLocalDataSource.Query(toSync = true)))

    private fun logFailure(t: Throwable) =
        logFailure<PeopleUpSyncUploaderWorker>(t)

    private fun logSuccess(message: String) =
        logSuccess<PeopleUpSyncUploaderWorker>(message)

    private fun crashlyticsLog(message: String) =
        crashlyticsLog<PeopleUpSyncUploaderWorker>(message)

}

fun WorkInfo.getUpCountsFromOutput(): PeopleCount? {
    val outputJson = this.outputData.getString(PeopleUpSyncCountWorker.OUTPUT_COUNT_WORKER_UP)
    return JsonHelper.gson.fromJson<PeopleCount>(outputJson, PeopleCount::class.java)
}

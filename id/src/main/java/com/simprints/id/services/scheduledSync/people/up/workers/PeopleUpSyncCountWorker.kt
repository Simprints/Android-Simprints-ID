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
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_MASTER_SYNC_ID
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
            logMessageForCrashReport<PeopleUpSyncCountWorker>("Sync - Preparing request for $upSyncScope")

            execute(upSyncScope)
        } catch (t: Throwable) {
            t.printStackTrace()
            logFailure<PeopleUpSyncCountWorker>("Sync - Failed ${tags.firstOrNull { it.contains(TAG_MASTER_SYNC_ID) }}", t)

            resultSetter.failure()
        }
    }

    private fun execute(upSyncScope: PeopleUpSyncScope): Result {
        val upCount = getUpCount(upSyncScope)

        logSuccess<PeopleUpSyncCountWorker>("Sync - Executing task done for $upSyncScope ${tags.firstOrNull { it.contains(TAG_MASTER_SYNC_ID) }}")

        return resultSetter.success(workDataOf(
            OUTPUT_COUNT_WORKER_UP to JsonHelper.gson.toJson(upCount))
        )
    }

    private fun getUpCount(syncScope: PeopleUpSyncScope) =
        PeopleCount(created = personRepository.count(PersonLocalDataSource.Query(toSync = true)))
}

fun WorkInfo.getUpCountsFromOutput(): PeopleCount? {
    val outputJson = this.outputData.getString(PeopleUpSyncCountWorker.OUTPUT_COUNT_WORKER_UP)
    return JsonHelper.gson.fromJson<PeopleCount>(outputJson, PeopleCount::class.java)
}

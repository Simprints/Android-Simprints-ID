package com.simprints.id.services.scheduledSync.people.down.workers

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State.*
import androidx.work.WorkManager
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
import com.simprints.id.services.scheduledSync.people.common.TAG_MASTER_SYNC_ID
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncWorkerType.Companion.tagForType
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncWorkerType.DOWNLOADER
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncWorkerType.UPLOADER
import javax.inject.Inject

class PeopleDownSyncCountWorker(val context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params) {

    companion object {
        const val OUTPUT_COUNT_WORKER_DOWN = "OUTPUT_COUNT_WORKER_DOWN"
    }

    override val tag: String = PeopleDownSyncCountWorker::class.java.simpleName

    private val wm: WorkManager
        get() = WorkManager.getInstance(context)

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var personRepository: PersonRepository
    @Inject lateinit var downSyncScopeRepository: PeopleDownSyncScopeRepository

    override suspend fun doWork(): Result {
        return try {
            getComponent<PeopleDownSyncCountWorker> { it.inject(this) }

            val downSyncScope = downSyncScopeRepository.getDownSyncScope()
            crashlyticsLog("Start - Params: $downSyncScope")

            execute(downSyncScope)
        } catch (t: Throwable) {
            fail(t)
        }
    }

    private suspend fun execute(downSyncScope: PeopleDownSyncScope): Result {
        return try {
            val downCount = getDownCount(downSyncScope)
            val output = JsonHelper.gson.toJson(downCount)

            success(workDataOf(OUTPUT_COUNT_WORKER_DOWN to output), output)
        } catch (t: Throwable) {
            fail(t)

            if (isSyncStillRunning()) {
                retry(t)
            } else {
                //If sync is not running, the count is useless
                success()
            }
        }
    }

    private fun isSyncStillRunning(): Boolean {
        val masterSyncIdTag = this.tags.firstOrNull { it.contains(TAG_MASTER_SYNC_ID) }
            ?: return false

        val workers = wm.getWorkInfosByTag(masterSyncIdTag).get()
        return workers?.let {
            val downloaders = it.filter { it.tags.contains(tagForType(DOWNLOADER)) }
            val uploaders = it.filter { it.tags.contains(tagForType(UPLOADER)) }
            (downloaders + uploaders).any {
                listOf(RUNNING, SUCCEEDED, ENQUEUED).contains(it.state)
            }
        } ?: false
    }

    private suspend fun getDownCount(syncScope: PeopleDownSyncScope) =
        personRepository.countToDownSync(syncScope)

}

fun WorkInfo.getDownCountsFromOutput(): List<PeopleCount>? {
    val outputJson = this.outputData.getString(PeopleDownSyncCountWorker.OUTPUT_COUNT_WORKER_DOWN)
    val listType = object : TypeToken<ArrayList<PeopleCount?>?>() {}.type
    return JsonHelper.gson.fromJson<List<PeopleCount>>(outputJson, listType)
}


package com.simprints.id.services.scheduledSync.subjects.down.workers

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State.ENQUEUED
import androidx.work.WorkInfo.State.RUNNING
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.common.models.SubjectsCount
import com.simprints.id.data.db.subjects_sync.down.SubjectsDownSyncScopeRepository
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncScope
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.exceptions.safe.sync.SyncCloudIntegrationException
import com.simprints.id.services.scheduledSync.subjects.common.SimCoroutineWorker
import com.simprints.id.services.scheduledSync.subjects.common.TAG_MASTER_SYNC_ID
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsSyncWorkerType.Companion.tagForType
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsSyncWorkerType.DOWNLOADER
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsSyncWorkerType.UPLOADER
import com.simprints.id.tools.delegates.lazyVar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SubjectsDownSyncCountWorker(val context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params) {

    companion object {
        const val OUTPUT_COUNT_WORKER_DOWN = "OUTPUT_COUNT_WORKER_DOWN"
    }

    override val tag: String = SubjectsDownSyncCountWorker::class.java.simpleName

    var wm: WorkManager by lazyVar {
        WorkManager.getInstance(context)
    }

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var personRepository: SubjectRepository
    @Inject lateinit var downSyncScopeRepository: SubjectsDownSyncScopeRepository

    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            try {
                getComponent<SubjectsDownSyncCountWorker> { it.inject(this@SubjectsDownSyncCountWorker) }

                val downSyncScope = downSyncScopeRepository.getDownSyncScope()
                crashlyticsLog("Start - Params: $downSyncScope")

                execute(downSyncScope)
            } catch (t: Throwable) {
                fail(t)
            }
        }

    private suspend fun execute(downSyncScope: SubjectsDownSyncScope): Result {
        return try {

            val downCount = getDownCount(downSyncScope)
            val output = JsonHelper.gson.toJson(downCount)

            success(workDataOf(OUTPUT_COUNT_WORKER_DOWN to output), output)

        } catch (t: Throwable) {

            when {
                t is SyncCloudIntegrationException -> {
                    fail(t)
                }
                isSyncStillRunning() -> {
                    retry(t)
                }
                else -> {
                    t.printStackTrace()
                    success(message = "Succeed because count is not required any more.")
                }
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
                listOf(RUNNING, ENQUEUED).contains(it.state)
            }
        } ?: false
    }

    private suspend fun getDownCount(syncScope: SubjectsDownSyncScope) =
        personRepository.countToDownSync(syncScope)

}

fun WorkInfo.getDownCountsFromOutput(): SubjectsCount? {
    val outputJson = this.outputData.getString(SubjectsDownSyncCountWorker.OUTPUT_COUNT_WORKER_DOWN)
    return JsonHelper.gson.fromJson(outputJson, SubjectsCount::class.java)
}


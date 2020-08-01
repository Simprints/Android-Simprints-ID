package com.simprints.id.services.scheduledSync.subjects.up.workers

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.common.models.SubjectsCount
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.db.subjects_sync.up.SubjectsUpSyncScopeRepository
import com.simprints.id.data.db.subjects_sync.up.domain.SubjectsUpSyncScope
import com.simprints.id.services.scheduledSync.subjects.common.SimCoroutineWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SubjectsUpSyncCountWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params) {

    companion object {
        const val OUTPUT_COUNT_WORKER_UP = "OUTPUT_COUNT_WORKER_UP"
    }

    override val tag: String = SubjectsUpSyncCountWorker::class.java.simpleName

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var personRepository: SubjectRepository
    @Inject lateinit var subjectsUpSyncScopeRepository: SubjectsUpSyncScopeRepository
    @Inject lateinit var jsonHelper: JsonHelper

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            getComponent<SubjectsUpSyncCountWorker> { it.inject(this@SubjectsUpSyncCountWorker) }

            val upSyncScope = subjectsUpSyncScopeRepository.getUpSyncScope()
            crashlyticsLog("Start - $upSyncScope")

            execute(upSyncScope)
        } catch (t: Throwable) {
            fail(t)
        }
    }

    private suspend fun execute(upSyncScope: SubjectsUpSyncScope): Result {
        val upCount = getUpCount(upSyncScope)
        val output = jsonHelper.toJson(upCount)

        return success(workDataOf(
            OUTPUT_COUNT_WORKER_UP to jsonHelper.toJson(upCount)), "Total to upload: $output")

    }

    private suspend fun getUpCount(syncScope: SubjectsUpSyncScope) =
        SubjectsCount(created = personRepository.count(SubjectLocalDataSource.Query(toSync = true)))

}

fun WorkInfo.getUpCountsFromOutput(): SubjectsCount? {
    val outputJson = this.outputData.getString(SubjectsUpSyncCountWorker.OUTPUT_COUNT_WORKER_UP)
    return JsonHelper.jackson.readValue(outputJson, SubjectsCount::class.java)
}

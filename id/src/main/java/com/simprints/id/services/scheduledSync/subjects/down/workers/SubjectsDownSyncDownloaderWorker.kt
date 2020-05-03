package com.simprints.id.services.scheduledSync.subjects.down.workers

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.subjects_sync.down.SubjectsDownSyncScopeRepository
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncOperation
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.exceptions.safe.sync.SyncCloudIntegrationException
import com.simprints.id.exceptions.unexpected.MalformedDownSyncOperationException
import com.simprints.id.services.scheduledSync.subjects.common.SimCoroutineWorker
import com.simprints.id.services.scheduledSync.subjects.common.WorkerProgressCountReporter
import com.simprints.id.services.scheduledSync.subjects.down.workers.SubjectsDownSyncDownloaderWorker.Companion.OUTPUT_DOWN_SYNC
import com.simprints.id.services.scheduledSync.subjects.down.workers.SubjectsDownSyncDownloaderWorker.Companion.PROGRESS_DOWN_SYNC
import com.simprints.id.services.scheduledSync.subjects.master.internal.OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION
import com.simprints.id.services.scheduledSync.subjects.master.internal.SubjectsSyncCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class SubjectsDownSyncDownloaderWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params), WorkerProgressCountReporter {

    companion object {
        const val INPUT_DOWN_SYNC_OPS = "INPUT_DOWN_SYNC_OPS"
        const val PROGRESS_DOWN_SYNC = "PROGRESS_DOWN_SYNC"
        const val OUTPUT_DOWN_SYNC = "OUTPUT_DOWN_SYNC"
    }

    override val tag: String = SubjectsDownSyncDownloaderWorker::class.java.simpleName

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var downSyncScopeRepository: SubjectsDownSyncScopeRepository
    @Inject lateinit var personRepository: SubjectRepository
    @Inject lateinit var subjectsSyncCache: SubjectsSyncCache

    internal var subjectsDownSyncDownloaderTask: SubjectsDownSyncDownloaderTask = SubjectsDownSyncDownloaderTaskImpl()

    private val jsonForOp by lazy {
        inputData.getString(INPUT_DOWN_SYNC_OPS)
            ?: throw IllegalArgumentException("input required")
    }

    @ExperimentalCoroutinesApi
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            getComponent<SubjectsDownSyncDownloaderWorker> { it.inject(this@SubjectsDownSyncDownloaderWorker) }
            val downSyncOperation = extractSubSyncScopeFromInput()
            crashlyticsLog("Start - Params: $downSyncOperation")

            val count = subjectsDownSyncDownloaderTask.execute(
                this@SubjectsDownSyncDownloaderWorker.id.toString(),
                downSyncOperation,
                subjectsSyncCache,
                personRepository,
                this@SubjectsDownSyncDownloaderWorker,
                this)

            Timber.d("Downsync success : $count")
            success(workDataOf(OUTPUT_DOWN_SYNC to count), "Total downloaded: $0 for $downSyncOperation")
        } catch (t: Throwable) {
            retryOrFailIfCloudIntegrationErrorOrMalformedOperation(t)
        }
    }

    private fun retryOrFailIfCloudIntegrationErrorOrMalformedOperation(t: Throwable): Result {
        return if (t is SyncCloudIntegrationException || t is MalformedDownSyncOperationException) {
            fail(t, t.message, workDataOf(OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION to true))
        } else {
            retry(t)
        }
    }

    private suspend fun extractSubSyncScopeFromInput(): SubjectsDownSyncOperation {
        try {
            val op = JsonHelper.gson.fromJson(jsonForOp, SubjectsDownSyncOperation::class.java)
            return downSyncScopeRepository.refreshDownSyncOperationFromDb(op) ?: op
        } catch (t: Throwable) {
            throw MalformedDownSyncOperationException()
        }
    }

    override suspend fun reportCount(count: Int) {
        setProgress(
            workDataOf(PROGRESS_DOWN_SYNC to count)
        )
    }
}

fun WorkInfo.extractDownSyncProgress(subjectsSyncCache: SubjectsSyncCache): Int? {
    val progress = this.progress.getInt(PROGRESS_DOWN_SYNC, -1)
    val output = this.outputData.getInt(OUTPUT_DOWN_SYNC, -1)

    //When the worker is not running (e.g. ENQUEUED due to errors), the output and progress are cleaned.
    val cached = subjectsSyncCache.readProgress(id.toString())
    return maxOf(progress, output, cached)
}

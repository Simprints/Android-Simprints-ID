package com.simprints.id.services.scheduledSync.subjects.up.workers

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.exceptions.unexpected.SyncCloudIntegrationException
import com.simprints.id.services.scheduledSync.subjects.common.SimCoroutineWorker
import com.simprints.id.services.scheduledSync.subjects.common.WorkerProgressCountReporter
import com.simprints.id.services.scheduledSync.subjects.master.internal.OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION
import com.simprints.id.services.scheduledSync.subjects.master.internal.SubjectsSyncCache
import com.simprints.id.services.scheduledSync.subjects.up.workers.SubjectsUpSyncUploaderWorker.Companion.OUTPUT_UP_SYNC
import com.simprints.id.services.scheduledSync.subjects.up.workers.SubjectsUpSyncUploaderWorker.Companion.PROGRESS_UP_SYNC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

// TODO: uncomment userId when multitenancy is properly implemented
@InternalCoroutinesApi
class SubjectsUpSyncUploaderWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params), WorkerProgressCountReporter {

    override val tag: String = SubjectsUpSyncUploaderWorker::class.java.simpleName

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var personRepository: SubjectRepository
    @Inject lateinit var subjectsSyncCache: SubjectsSyncCache

    @ExperimentalCoroutinesApi
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            getComponent<SubjectsUpSyncUploaderWorker> { it.inject(this@SubjectsUpSyncUploaderWorker) }


            val workerId = this@SubjectsUpSyncUploaderWorker.id.toString()
            var count = subjectsSyncCache.readProgress(workerId)

            crashlyticsLog("Start")
            val totalUploaded = personRepository.performUploadWithProgress(this)
            while (!totalUploaded.isClosedForReceive) {
                totalUploaded.poll()?.let {
                    count += it.upSyncCountForBatch
                    subjectsSyncCache.saveProgress(workerId, count)
                    Timber.d("Upsync uploader count : $count for batch : $it")
                    reportCount(count)
                }
            }

            Timber.d("Upsync success : $count")
            success(workDataOf(OUTPUT_UP_SYNC to count), "Total uploaded: $count")
        } catch (t: Throwable) {
            t.printStackTrace()
            Timber.d("Upsync failed : ${t.printStackTrace()}")
            retryOrFailIfCloudIntegrationError(t)
        }
    }

    private fun retryOrFailIfCloudIntegrationError(t: Throwable): Result {
        return if (t is SyncCloudIntegrationException) {
            fail(t, t.message, workDataOf(OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION to true))
        } else {
            retry(t)
        }
    }

    override suspend fun reportCount(count: Int) {
        setProgress(
            workDataOf(PROGRESS_UP_SYNC to count)
        )
    }

    companion object {
        const val PROGRESS_UP_SYNC = "PROGRESS_UP_SYNC"
        const val OUTPUT_UP_SYNC = "OUTPUT_UP_SYNC"
    }
}

fun WorkInfo.extractUpSyncProgress(subjectsSyncCache: SubjectsSyncCache): Int? {
    val progress = this.progress.getInt(PROGRESS_UP_SYNC, -1)
    val output = this.outputData.getInt(OUTPUT_UP_SYNC, -1)

    //When the worker is not running (e.g. ENQUEUED due to errors), the output and progress are cleaned.
    val cached = subjectsSyncCache.readProgress(id.toString())
    return maxOf(progress, output, cached)
}

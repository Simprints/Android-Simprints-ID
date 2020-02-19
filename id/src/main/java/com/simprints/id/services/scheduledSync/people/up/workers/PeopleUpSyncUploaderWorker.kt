package com.simprints.id.services.scheduledSync.people.up.workers

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.people_sync.up.PeopleUpSyncScopeRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.safe.sync.SyncCloudIntegrationException
import com.simprints.id.services.scheduledSync.people.common.SimCoroutineWorker
import com.simprints.id.services.scheduledSync.people.common.WorkerProgressCountReporter
import com.simprints.id.services.scheduledSync.people.master.internal.OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import com.simprints.id.services.scheduledSync.people.up.workers.PeopleUpSyncUploaderWorker.Companion.OUTPUT_UP_SYNC
import com.simprints.id.services.scheduledSync.people.up.workers.PeopleUpSyncUploaderWorker.Companion.PROGRESS_UP_SYNC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.withContext
import javax.inject.Inject

// TODO: uncomment userId when multitenancy is properly implemented
@InternalCoroutinesApi
class PeopleUpSyncUploaderWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params), WorkerProgressCountReporter {

    override val tag: String = PeopleUpSyncUploaderWorker::class.java.simpleName

    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var personLocalDataSource: PersonLocalDataSource
    @Inject lateinit var personRemoteDataSource: PersonRemoteDataSource
    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var peopleUpSyncScopeRepository: PeopleUpSyncScopeRepository
    @Inject lateinit var peopleSyncCache: PeopleSyncCache

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            getComponent<PeopleUpSyncUploaderWorker> { it.inject(this@PeopleUpSyncUploaderWorker) }
            crashlyticsLog("Start")

            val task = PeopleUpSyncUploaderTask(
                loginInfoManager, personLocalDataSource, personRemoteDataSource,
                PATIENT_UPLOAD_BATCH_SIZE,
                peopleUpSyncScopeRepository,
                peopleSyncCache
            )

            val totalUploaded = task.execute(this@PeopleUpSyncUploaderWorker.id.toString(), this@PeopleUpSyncUploaderWorker)
            success(workDataOf(OUTPUT_UP_SYNC to totalUploaded), "Total uploaded: $totalUploaded")
        } catch (t: Throwable) {
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
        private const val PATIENT_UPLOAD_BATCH_SIZE = 80
        const val PROGRESS_UP_SYNC = "PROGRESS_UP_SYNC"
        const val OUTPUT_UP_SYNC = "OUTPUT_UP_SYNC"
    }
}

fun WorkInfo.extractUpSyncProgress(peopleSyncCache: PeopleSyncCache): Int? {
    val progress = this.progress.getInt(PROGRESS_UP_SYNC, -1)
    val output = this.outputData.getInt(OUTPUT_UP_SYNC, -1)

    //When the worker is not running (e.g. ENQUEUED due to errors), the output and progress are cleaned.
    val cached = peopleSyncCache.readProgress(id.toString())
    return maxOf(progress, output, cached)
}

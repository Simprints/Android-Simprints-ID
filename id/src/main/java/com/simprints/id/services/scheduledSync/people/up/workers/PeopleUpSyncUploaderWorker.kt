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
import com.simprints.id.exceptions.safe.sync.TransientSyncFailureException
import com.simprints.id.services.scheduledSync.people.common.SimCoroutineWorker
import com.simprints.id.services.scheduledSync.people.common.WorkerProgressCountReporter
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncProgressCache
import com.simprints.id.services.scheduledSync.people.up.workers.PeopleUpSyncUploaderWorker.Companion.OUTPUT_UP_SYNC
import com.simprints.id.services.scheduledSync.people.up.workers.PeopleUpSyncUploaderWorker.Companion.PROGRESS_UP_SYNC
import kotlinx.coroutines.InternalCoroutinesApi
import javax.inject.Inject

// TODO: uncomment userId when multitenancy is properly implemented
@InternalCoroutinesApi
class PeopleUpSyncUploaderWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params), WorkerProgressCountReporter {

    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var personLocalDataSource: PersonLocalDataSource
    @Inject lateinit var personRemoteDataSource: PersonRemoteDataSource
    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var peopleUpSyncScopeRepository: PeopleUpSyncScopeRepository
    @Inject lateinit var peopleSyncProgressCache: PeopleSyncProgressCache

    override suspend fun doWork(): Result {
        return try {
            getComponent<PeopleUpSyncUploaderWorker> { it.inject(this) }
            crashlyticsLog("Preparing upSync")

            val task = PeopleUpSyncUploaderTask(
                loginInfoManager, personLocalDataSource, personRemoteDataSource,
                PATIENT_UPLOAD_BATCH_SIZE,
                peopleUpSyncScopeRepository,
                peopleSyncProgressCache
            )

            val totalUploaded = task.execute(this.id.toString(), this)
            logSuccess("Upsync task done $totalUploaded")

            resultSetter.success(workDataOf(OUTPUT_UP_SYNC to totalUploaded))
        } catch (t: TransientSyncFailureException) {
            logFailure(t)
            resultSetter.retry()
        } catch (t: Throwable) {
            logFailure(t)
            resultSetter.failure()
        }
    }

    override suspend fun reportCount(count: Int) {
        setProgress(
            workDataOf(PROGRESS_UP_SYNC to count)
        )
    }

    private fun logFailure(t: Throwable) =
        logFailure<PeopleUpSyncUploaderWorker>(t)

    private fun logSuccess(message: String) =
        logSuccess<PeopleUpSyncUploaderWorker>(message)

    private fun crashlyticsLog(message: String) =
        crashReportLog<PeopleUpSyncUploaderWorker>(message)

    companion object {
        const val PATIENT_UPLOAD_BATCH_SIZE = 80
        const val PROGRESS_UP_SYNC = "PROGRESS_UP_SYNC"
        const val OUTPUT_UP_SYNC = "OUTPUT_UP_SYNC"
    }
}

fun WorkInfo.extractUpSyncProgress(progressCache: PeopleSyncProgressCache): Int? {
    val progress = this.progress.getInt(PROGRESS_UP_SYNC, -1)
    val output = this.outputData.getInt(OUTPUT_UP_SYNC, -1)

    //When the worker is not running (e.g. ENQUEUED due to errors), the output and progress are cleaned.
    val cached = progressCache.getProgress(id.toString())
    return maxOf(progress, output, cached)
}

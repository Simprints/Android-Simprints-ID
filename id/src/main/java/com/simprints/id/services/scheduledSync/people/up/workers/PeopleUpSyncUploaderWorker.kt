package com.simprints.id.services.scheduledSync.people.up.workers

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.db.people_sync.SyncStatusDatabase
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.safe.sync.TransientSyncFailureException
import com.simprints.id.services.scheduledSync.people.common.SimCoroutineWorker
import com.simprints.id.services.scheduledSync.people.common.WorkerProgressCountReporter
import com.simprints.id.services.scheduledSync.people.up.workers.PeopleUpSyncUploaderWorker.Companion.OUTPUT_UP_SYNC
import com.simprints.id.services.scheduledSync.people.up.workers.PeopleUpSyncUploaderWorker.Companion.PROGRESS_UP_SYNC
import kotlinx.coroutines.InternalCoroutinesApi
import timber.log.Timber
import javax.inject.Inject

// TODO: uncomment userId when multitenancy is properly implemented
@InternalCoroutinesApi
class PeopleUpSyncUploaderWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params), WorkerProgressCountReporter {

    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var personLocalDataSource: PersonLocalDataSource
    @Inject lateinit var personRemoteDataSource: PersonRemoteDataSource
    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase

    override suspend fun doWork(): Result {
        getComponent<PeopleUpSyncUploaderWorker> { it.inject(this) }
        logMessageForCrashReport("PeopleUpSyncUploaderWorker - running")

        val task = PeopleUpSyncUploaderTask(
            loginInfoManager, personLocalDataSource, personRemoteDataSource,
            loginInfoManager.getSignedInProjectIdOrEmpty(), /*userId, */PATIENT_UPLOAD_BATCH_SIZE,
            syncStatusDatabase.upSyncDao
        )

        return try {
            val peopleUploaded = task.execute(this)
            logMessageForCrashReport("PeopleUpSyncUploaderWorker - success")
            logSuccess<PeopleUpSyncUploaderWorker>("Sync - Upsync task down")

            resultSetter.success(workDataOf(OUTPUT_UP_SYNC to peopleUploaded))
        } catch (exception: TransientSyncFailureException) {
            Timber.e(exception)
            logFailure<PeopleUpSyncUploaderWorker>("Sync - Upsync task failed, but retry", exception)

            resultSetter.retry()
        } catch (throwable: Throwable) {
            Timber.e(throwable)
            logMessageForCrashReport("PeopleUpSyncUploaderWorker - failure")
            logFailure<PeopleUpSyncUploaderWorker>("Sync - Upsync task failed", throwable)

            crashReportManager.logExceptionOrSafeException(throwable)
            resultSetter.failure()
        }
    }


    private fun logMessageForCrashReport(message: String) =
        crashReportManager.logMessageForCrashReport(CrashReportTag.SYNC, CrashReportTrigger.NETWORK, message = message)

    override suspend fun reportCount(count: Int) {
        setProgress(
            workDataOf(PROGRESS_UP_SYNC to count)
        )
    }

    companion object {
        const val PATIENT_UPLOAD_BATCH_SIZE = 80
        const val PROGRESS_UP_SYNC = "PROGRESS_UP_SYNC"
        const val OUTPUT_UP_SYNC = "OUTPUT_UP_SYNC"
    }
}

fun WorkInfo.extractUpSyncProgress(): Int {
    val progress = this.progress.getInt(PROGRESS_UP_SYNC, -1)
    return if (progress < 0) {
        this.outputData.getInt(OUTPUT_UP_SYNC, -1)
    } else {
        progress
    }
}

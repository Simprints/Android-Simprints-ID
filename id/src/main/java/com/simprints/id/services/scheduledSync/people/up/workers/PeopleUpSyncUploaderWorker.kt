package com.simprints.id.services.scheduledSync.people.up.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.db.people_sync.SyncStatusDatabase
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.safe.sync.TransientSyncFailureException
import com.simprints.id.services.scheduledSync.people.common.SimCoroutineWorker
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import javax.inject.Inject

// TODO: uncomment userId when multitenancy is properly implemented
@InternalCoroutinesApi
class PeopleUpSyncUploaderWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params) {

    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var personLocalDataSource: PersonLocalDataSource
    @Inject lateinit var personRemoteDataSource: PersonRemoteDataSource
    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var newSyncStatusDatabase: SyncStatusDatabase

    override suspend fun doWork(): Result = coroutineScope {
        getComponent<PeopleUpSyncUploaderWorker> { it.inject(this) }
        logMessageForCrashReport("PeopleUpSyncUploaderWorker - running")

        val task = PeopleUpSyncUploaderTask(
            loginInfoManager, personLocalDataSource, personRemoteDataSource,
            loginInfoManager.getSignedInProjectIdOrEmpty(), /*userId, */PATIENT_UPLOAD_BATCH_SIZE,
            newSyncStatusDatabase.upSyncDao
        )

        return@coroutineScope try {
            task.execute()
            logMessageForCrashReport("PeopleUpSyncUploaderWorker - success")
            Result.success()
        } catch (exception: TransientSyncFailureException) {
            Timber.e(exception)
            Result.retry()
        } catch (throwable: Throwable) {
            Timber.e(throwable)
            logMessageForCrashReport("PeopleUpSyncUploaderWorker - failure")
            crashReportManager.logExceptionOrSafeException(throwable)
            Result.failure()
        }
    }


    private fun logMessageForCrashReport(message: String) =
        crashReportManager.logMessageForCrashReport(CrashReportTag.SYNC, CrashReportTrigger.NETWORK, message = message)

    companion object {
        const val PATIENT_UPLOAD_BATCH_SIZE = 80
    }
}

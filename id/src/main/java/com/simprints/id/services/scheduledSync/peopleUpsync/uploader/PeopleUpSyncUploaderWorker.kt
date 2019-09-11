package com.simprints.id.services.scheduledSync.peopleUpsync.uploader

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.db.syncstatus.SyncStatusDatabase
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.safe.sync.TransientSyncFailureException
import com.simprints.id.exceptions.unexpected.WorkerInjectionFailedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

// TODO: uncomment userId when multitenancy is properly implemented
@InternalCoroutinesApi
class PeopleUpSyncUploaderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var personLocaDataSource: PersonLocalDataSource
    @Inject lateinit var personRemoteDataSource: PersonRemoteDataSource
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var newSyncStatusDatabase: SyncStatusDatabase

    val projectId by lazy {
        inputData.getString(PROJECT_ID_KEY) ?: throw IllegalArgumentException("Project Id required")
    }

    /*val userId by lazy {
        inputData.getString(USER_ID_KEY) ?: throw IllegalArgumentException("User Id required")
    }*/

    override fun doWork(): Result {
        injectDependencies()
        logMessageForCrashReport("PeopleUpSyncUploaderWorker - running")

        val task = PeopleUpSyncUploaderTask(
            loginInfoManager, personLocaDataSource, personRemoteDataSource,
            projectId, /*userId, */PATIENT_UPLOAD_BATCH_SIZE,
            newSyncStatusDatabase.upSyncDao
        )

        return try {
            GlobalScope.launch(Dispatchers.IO) {
                task.execute()
            }
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

    private fun injectDependencies() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        } else {
            throw WorkerInjectionFailedException.forWorker<PeopleUpSyncUploaderWorker>()
        }
    }

    private fun logMessageForCrashReport(message: String) =
        crashReportManager.logMessageForCrashReport(CrashReportTag.SYNC, CrashReportTrigger.NETWORK, message = message)

    companion object {
        const val PATIENT_UPLOAD_BATCH_SIZE = 80
        const val PROJECT_ID_KEY = "projectId"
        const val USER_ID_KEY = "userId"
    }
}

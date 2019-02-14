package com.simprints.id.services.scheduledSync.peopleUpsync.uploader

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.data.analytics.crashReport.CrashReportManager
import com.simprints.id.data.analytics.crashReport.CrashReportTags
import com.simprints.id.data.analytics.crashReport.CrashTrigger
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.room.SyncStatusDatabase
import com.simprints.id.data.db.remote.people.RemotePeopleManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.safe.sync.TransientSyncFailureException
import com.simprints.id.exceptions.unexpected.WorkerInjectionFailedError
import timber.log.Timber
import javax.inject.Inject

// TODO: uncomment userId when multitenancy is properly implemented

class PeopleUpSyncUploaderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var localDbManager: LocalDbManager
    @Inject lateinit var remotePeopleManager: RemotePeopleManager
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
            loginInfoManager, localDbManager, remotePeopleManager,
            projectId, /*userId, */PATIENT_UPLOAD_BATCH_SIZE,
            newSyncStatusDatabase.upSyncDao
        )

        return try {
            task.execute()
            logMessageForCrashReport("PeopleUpSyncUploaderWorker - success")
            Result.success()
        } catch (exception: TransientSyncFailureException) {
            Timber.e(exception)
            Result.retry()
        } catch (throwable: Throwable) {
            Timber.e(throwable)
            logWarningToAnalytics("PeopleUpSyncUploaderWorker - failure")
            crashReportManager.logThrowable(throwable)
            Result.failure()
        }
    }

    private fun injectDependencies() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        } else {
            throw WorkerInjectionFailedError.forWorker<PeopleUpSyncUploaderWorker>()
        }
    }

    private fun logMessageForCrashReport(message: String) =
        crashReportManager.logInfo(CrashReportTags.SYNC, CrashTrigger.NETWORK, message)

    private fun logWarningToAnalytics(message: String) =
        crashReportManager.logWarning(CrashReportTags.SYNC, CrashTrigger.NETWORK, message)

    companion object {
        const val PATIENT_UPLOAD_BATCH_SIZE = 80
        const val PROJECT_ID_KEY = "projectId"
        const val USER_ID_KEY = "userId"
    }
}

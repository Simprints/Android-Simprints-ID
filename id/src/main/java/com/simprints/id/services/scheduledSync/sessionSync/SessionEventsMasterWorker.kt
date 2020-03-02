package com.simprints.id.services.scheduledSync.sessionSync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.session.domain.SessionEventsManager
import com.simprints.id.data.db.session.remote.RemoteSessionsManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.exceptions.unexpected.WorkerInjectionFailedException
import com.simprints.id.tools.TimeHelper
import timber.log.Timber
import javax.inject.Inject

class SessionEventsMasterWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var remoteSessionsManager: RemoteSessionsManager

    override suspend fun doWork(): Result {
        Timber.d("SessionEventsMasterWorker doWork()")
        injectDependencies()

        return try {
            val task = SessionEventsSyncMasterTask(
                loginInfoManager.getSignedInProjectIdOrEmpty(),
                sessionEventsManager,
                timeHelper,
                remoteSessionsManager.getSessionsApiClient(),
                crashReportManager
            )
            task.execute().blockingAwait()
            Result.success()
        } catch (e: NoSessionsFoundException) {
            Timber.d("No sessions found")
            Result.success()
        } catch (throwable: Throwable) {
            Timber.d("Sessions upload failed")
            Timber.e(throwable)
            crashReportManager.logExceptionOrSafeException(throwable)
            Result.failure()
        }
    }

    private fun injectDependencies() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        } else {
            throw WorkerInjectionFailedException.forWorker<SessionEventsMasterWorker>()
        }
    }
}

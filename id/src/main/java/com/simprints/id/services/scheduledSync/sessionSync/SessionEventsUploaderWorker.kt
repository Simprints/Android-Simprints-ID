package com.simprints.id.services.scheduledSync.sessionSync

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.data.analytics.crashes.CrashReportManager
import com.simprints.id.data.analytics.crashes.CrashReportTags
import com.simprints.id.data.analytics.crashes.CrashTrigger
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.remote.sessions.RemoteSessionsManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.unsafe.WorkerInjectionFailedError
import com.simprints.id.tools.TimeHelper
import timber.log.Timber
import javax.inject.Inject

class SessionEventsUploaderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var remoteSessionsManager: RemoteSessionsManager

    private val sessionsApiClient by lazy {
        remoteSessionsManager.getSessionsApiClient().blockingGet()
    }

    private val sessionIdsToUpload by lazy {
        inputData.getStringArray(SessionEventsSyncMasterTask.SESSIONS_IDS_KEY)?.toList()
            ?: throw IllegalArgumentException("Sessions ids required")
    }

    private val signedProjectId by lazy {
        inputData.getString(SessionEventsSyncMasterTask.PROJECT_ID_KEY)
            ?: throw IllegalArgumentException("Project Id required")
    }

    override fun doWork(): Result {
        injectDependencies()
        logMessageForCrashReport("SessionEventsUploaderWorker doWork()")

        return try {
            val task = SessionEventsUploaderTask(
                signedProjectId,
                sessionIdsToUpload,
                sessionEventsManager,
                timeHelper,
                sessionsApiClient
            )

            task.execute().blockingAwait()
            logMessageForCrashReport("SessionEventsUploaderWorker done()")

            Result.success()
        } catch (throwable: Throwable) {
            logWarningToAnalytics("SessionEventsUploaderWorker error() $throwable")
            Timber.e(throwable)
            crashReportManager.logThrowable(throwable)
            Result.failure()
        }
    }

    private fun injectDependencies() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        } else {
            throw WorkerInjectionFailedError.forWorker<SessionEventsUploaderWorker>()
        }
    }

    private fun logMessageForCrashReport(message: String) =
        crashReportManager.logInfo(CrashReportTags.SESSION, CrashTrigger.NETWORK, message)

    private fun logWarningToAnalytics(message: String) =
        crashReportManager.logWarning(CrashReportTags.SESSION, CrashTrigger.NETWORK, message)
}

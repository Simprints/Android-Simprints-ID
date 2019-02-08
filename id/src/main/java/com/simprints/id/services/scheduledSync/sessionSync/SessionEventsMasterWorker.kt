package com.simprints.id.services.scheduledSync.sessionSync

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.exceptions.unsafe.WorkerInjectionFailedError
import timber.log.Timber
import javax.inject.Inject

class SessionEventsMasterWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var analyticsManager: AnalyticsManager

    override fun doWork(): Result {
        Timber.d("SessionEventsMasterWorker doWork()")
        injectDependencies()

        return try {
            val task = SessionEventsSyncMasterTask(
                loginInfoManager.getSignedInProjectIdOrEmpty(),
                sessionEventsManager
            )
            task.execute().blockingAwait()
            Result.success()
        } catch (e: NoSessionsFoundException) {
            Timber.d("No sessions found")
            Result.success()
        } catch (throwable: Throwable) {
            Timber.e(throwable)
            analyticsManager.logThrowable(throwable)
            Result.failure()
        }
    }

    private fun injectDependencies() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        } else {
            throw WorkerInjectionFailedError.forWorker<SessionEventsMasterWorker>()
        }
    }
}

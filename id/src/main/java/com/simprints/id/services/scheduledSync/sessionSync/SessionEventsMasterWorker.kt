package com.simprints.id.services.scheduledSync.sessionSync

import androidx.work.Worker
import com.simprints.id.Application
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import timber.log.Timber
import javax.inject.Inject

class SessionEventsMasterWorker : Worker() {

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
            Result.SUCCESS
        } catch (throwable: Throwable) {
            Timber.e(throwable)
            analyticsManager.logThrowable(throwable)
            Result.FAILURE
        }
    }

    private fun injectDependencies() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        }
    }
}

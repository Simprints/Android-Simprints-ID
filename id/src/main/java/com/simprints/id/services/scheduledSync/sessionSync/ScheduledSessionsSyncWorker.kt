package com.simprints.id.services.scheduledSync.sessionSync

import android.annotation.SuppressLint
import androidx.work.Worker
import com.simprints.id.Application
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.tools.TimeHelper
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject

class ScheduledSessionsSyncWorker : Worker() {

    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var timeHelper: TimeHelper

    private val sessionsApiClient by lazy {
        remoteDbManager.getSessionsApiClient().blockingGet()
    }

    private val signedInProjectId by lazy {
        loginInfoManager.getSignedInProjectIdOrEmpty()
    }

    @SuppressLint("WrongThread")
    override fun doWork(): Result {
        val result = LinkedBlockingQueue<Result>()

        if (applicationContext is Application) {
            Timber.d("ScheduledSessionsSyncWorker - doWork")
            (applicationContext as Application).component.inject(this)
            uploadSessions(result)
        }

        return result.take()
    }

    private fun uploadSessions(result: LinkedBlockingQueue<Worker.Result>) {

        if (signedInProjectId.isNotEmpty()) {

            ScheduledSessionsTask(
                sessionEventsManager,
                timeHelper,
                sessionsApiClient,
                analyticsManager).syncSessions(signedInProjectId)

            .subscribeBy(onComplete = {
                Timber.d("ScheduledSessionsSyncWorker - onComplete")
                result.put(Result.SUCCESS)
            }, onError = {
                Timber.d("ScheduledSessionsSyncWorker - onError")
                result.put(Result.FAILURE)
            })
        } else {
            result.put(Result.SUCCESS)
        }
    }
}

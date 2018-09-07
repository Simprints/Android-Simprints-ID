package com.simprints.id.services.scheduledSync.sessionSync

import android.annotation.SuppressLint
import androidx.work.Worker
import com.simprints.id.Application
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.eventData.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventData.SessionEventsManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.exceptions.safe.session.SessionUploadFailureException
import com.simprints.id.tools.TimeHelper
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject

class ScheduledSessionsSync : Worker() {

    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var sessionEventsLocalDbManager: SessionEventsLocalDbManager

    @SuppressLint("WrongThread")
    override fun doWork(): Result {
        val result = LinkedBlockingQueue<Result>()

        if (applicationContext is Application) {
            Timber.d("ScheduledSessionsSync - doWork")
            (applicationContext as Application).component.inject(this)
            uploadSessions(result)
        }

        return result.take()
    }

    private fun uploadSessions(result: LinkedBlockingQueue<Worker.Result>) {
        val signedInProjectId = loginInfoManager.getSignedInProjectIdOrEmpty()

        if (signedInProjectId.isNotEmpty()) {
            sessionEventsLocalDbManager.loadSessions(signedInProjectId).subscribeBy(
                onSuccess = {sessionEvents ->
                    Timber.d(String.format("Session IDs: %s", sessionEvents.forEach { it.id + "\n" }))
                }
            )
            sessionEventsManager.syncSessions(signedInProjectId).subscribeBy(onComplete = {
                Timber.d("ScheduledSessionsSync - onComplete")

                result.put(Result.SUCCESS)
            }, onError = {
                Timber.d("ScheduledSessionsSync - onError")
                Timber.d(it)

                handleError(it, result)
            })
        }
    }

    private fun handleError(it: Throwable, result: LinkedBlockingQueue<Result>) =
        when (it) {
            is NoSessionsFoundException -> result.put(Result.SUCCESS)
            is SessionUploadFailureException -> result.put(Result.FAILURE)
            else -> {
                it.printStackTrace()
                analyticsManager.logThrowable(it)
                result.put(Result.FAILURE)
            }
        }
}

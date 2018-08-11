package com.simprints.id.services.scheduledSync.sessionSync

import androidx.work.Worker
import com.simprints.id.Application
import com.simprints.id.data.analytics.events.SessionApiInterface
import com.simprints.id.data.analytics.events.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.events.models.ArtificialTerminationEvent
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.network.SimApiClient
import com.simprints.id.tools.TimeHelper
import io.reactivex.Completable
import io.reactivex.rxkotlin.subscribeBy
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject

class ScheduledSessionsSync : Worker() {

    var apiClient: SimApiClient<SessionApiInterface>? = SimApiClient(SessionApiInterface::class.java, SessionApiInterface.baseUrl)
    @Inject lateinit var sessionEventsLocalDbManager: SessionEventsLocalDbManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var timeHelper: TimeHelper

    override fun doWork(): Result {
        val result = LinkedBlockingQueue<Result>()

        if (applicationContext is Application) {
            (applicationContext as Application).component.inject(this)
            uploadSessions(result)
        }

        return result.take()
    }

    private fun uploadSessions(result: LinkedBlockingQueue<Worker.Result>) {
        try {
            val signedInProjectId = loginInfoManager.signedInProjectId

            sessionEventsLocalDbManager.loadSessions(signedInProjectId).flatMap { sessions ->

                sessions.forEach {
                    it.relativeUploadTime = it.nowRelativeToStartTime(timeHelper)
                    it.addArtificialTerminationIfRequired(timeHelper, ArtificialTerminationEvent.Reason.TIMED_OUT)
                    it.closeIfRequired(timeHelper)
                    sessionEventsLocalDbManager.insertOrUpdateSessionEvents(it).blockingAwait()
                }

                apiClient?.api?.uploadSessions(signedInProjectId, sessions)
            }.flatMapCompletable {
                if(!it.isError) { //StopShip
                    sessionEventsLocalDbManager.deleteSessions(signedInProjectId)
                } else {
                    Completable.complete()
                }
            }.subscribeBy(onComplete = {
                result.put(Result.SUCCESS)
            }, onError = {
                result.put(Result.FAILURE)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

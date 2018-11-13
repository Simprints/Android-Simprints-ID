package com.simprints.id.services.scheduledSync.sessionSync

import android.annotation.SuppressLint
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventData.controllers.remote.SessionsRemoteInterface
import com.simprints.id.data.analytics.eventData.models.domain.events.ArtificialTerminationEvent
import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.exceptions.safe.session.SessionUploadFailureException
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.handleResult
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.HttpException
import retrofit2.adapter.rxjava2.Result

class ScheduledSessionsTask(private val sessionEventsManager: SessionEventsManager,
                            private val timeHelper: TimeHelper,
                            private val sessionApiClient: SessionsRemoteInterface) {

    /**
     * @throws NoSessionsFoundException
     * @throws SessionUploadFailureException
     */
    fun syncSessions(projectId: String): Completable =
        sessionEventsManager
            .loadSessions(projectId)
            .closeAnyOpenSessionsAndUpdateUploadTime()
            .takeAnyClosedSessionsReadyToBeUploaded()
            .uploadClosedSessionsIfAny(projectId)
            .handleResult(::uploadSessionSucceeded)
            .andThen {
                sessionEventsManager.deleteSessions(openSession = false).onErrorComplete()
            }


    @SuppressLint("CheckResult")
    private fun Single<ArrayList<SessionEvents>>.closeAnyOpenSessionsAndUpdateUploadTime(): Single<ArrayList<SessionEvents>> =
        this.flatMap { sessions ->
            sessions.forEach {
                forceSessionToCloseIfOpenAndNotInProgress(it, timeHelper)
                it.relativeUploadTime = it.nowRelativeToStartTime(timeHelper)
                sessionEventsManager.insertOrUpdateSessionEvents(it).blockingAwait()
            }
            Single.just(sessions)
        }

    @SuppressLint("CheckResult")
    private fun Single<ArrayList<SessionEvents>>.takeAnyClosedSessionsReadyToBeUploaded(): Single<ArrayList<SessionEvents>> =
        this.flatMap { sessions ->
            sessions.filter { it.isClosed() }.let { closedSessions ->
                if (closedSessions.isEmpty())
                    throw NoSessionsFoundException()

                Single.just(ArrayList(closedSessions))
            }
        }

    @SuppressLint("CheckResult")
    private fun Single<ArrayList<SessionEvents>>.uploadClosedSessionsIfAny(projectId: String): Single<Result<Void?>> =
        this.flatMap { sessions ->
            sessionApiClient.uploadSessions(projectId, hashMapOf("sessions" to sessions.toTypedArray()))
        }

    private fun uploadSessionSucceeded(e: HttpException) {
        if (e.response()?.code() != 201) {
            throw SessionUploadFailureException()
        }
    }

    private fun forceSessionToCloseIfOpenAndNotInProgress(session: SessionEvents, timeHelper: TimeHelper) {
        if (session.isOpen() && !session.isPossiblyInProgress(timeHelper)) {
            session.addArtificialTerminationIfRequired(timeHelper, ArtificialTerminationEvent.Reason.TIMED_OUT)
            session.closeIfRequired(timeHelper)
        }
    }
}

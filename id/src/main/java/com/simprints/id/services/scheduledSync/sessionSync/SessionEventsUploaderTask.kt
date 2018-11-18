package com.simprints.id.services.scheduledSync.sessionSync

import android.annotation.SuppressLint
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventData.controllers.remote.SessionsRemoteInterface
import com.simprints.id.data.analytics.eventData.models.domain.events.ArtificialTerminationEvent
import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.exceptions.safe.session.SessionUploadFailureException
import com.simprints.id.tools.TimeHelper
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.adapter.rxjava2.Result
import java.io.IOException

class SessionEventsUploaderTask(private val projectId: String,
                                private val sessionsIds: Array<String>,
                                private val sessionEventsManager: SessionEventsManager,
                                private val timeHelper: TimeHelper,
                                private val sessionApiClient: SessionsRemoteInterface) {


    /**
     * @throws NoSessionsFoundException
     * @throws SessionUploadFailureException
     */
    fun execute(): Completable =
        Single.just(sessionsIds)
            .loadSessionsFromDb()
            .closeOpenSessionsAndUpdateUploadTime()
            .filterClosedSessions()
            .uploadClosedSessionsOrThrowIfNoSessions()
            .checkUploadSucceed()
            .deleteSessionsFromDb()

    private fun Single<Array<String>>.loadSessionsFromDb(): Single<Array<SessionEvents>> =
        this.map {
            it.map { session -> sessionEventsManager.loadSessionById(session).blockingGet() }.toTypedArray()
        }

    @SuppressLint("CheckResult")
    private fun Single<Array<SessionEvents>>.closeOpenSessionsAndUpdateUploadTime(): Single<Array<SessionEvents>> =
        this.flatMap { sessions ->
            sessions.forEach {
                forceSessionToCloseIfOpenAndNotInProgress(it, timeHelper)
                it.relativeUploadTime = it.nowRelativeToStartTime(timeHelper)
                sessionEventsManager.insertOrUpdateSessionEvents(it).blockingAwait()
            }
            Single.just(sessions)
        }

    @SuppressLint("CheckResult")
    private fun Single<Array<SessionEvents>>.filterClosedSessions(): Single<Array<SessionEvents>> =
        this.flatMap { sessions ->
            Single.just(sessions.filter { it.isClosed() }.toTypedArray())
        }

    @SuppressLint("CheckResult")
    private fun Single<Array<SessionEvents>>.uploadClosedSessionsOrThrowIfNoSessions(): Single<Result<Void?>> =
        this.flatMap { sessions ->
            if (sessions.isEmpty())
                throw NoSessionsFoundException()

            sessionApiClient.uploadSessions(projectId, hashMapOf("sessions" to sessions))
        }


    private fun forceSessionToCloseIfOpenAndNotInProgress(session: SessionEvents, timeHelper: TimeHelper) {
        if (session.isOpen() && !session.isPossiblyInProgress(timeHelper)) {
            session.addArtificialTerminationIfRequired(timeHelper, ArtificialTerminationEvent.Reason.TIMED_OUT)
            session.closeIfRequired(timeHelper)
        }
    }

    private fun Single<out Result<Void?>>.checkUploadSucceed(): Completable =
        flatMapCompletable { result ->
            when {
                result.response()?.code() == 201 -> Completable.complete()
                result.response() == null -> throw IOException(result.error())
                else -> throw SessionUploadFailureException()
            }
        }

    private fun deleteSessions() {
        sessionsIds.forEach {
            sessionEventsManager.deleteSessions(sessionId = it, openSession = false).blockingGet()
        }
    }

    private fun Completable.deleteSessionsFromDb(): Completable =
        this.doOnError {
            if(it is SessionUploadFailureException) {
                deleteSessions()
            }
        }.doOnComplete {
            deleteSessions()
        }
}


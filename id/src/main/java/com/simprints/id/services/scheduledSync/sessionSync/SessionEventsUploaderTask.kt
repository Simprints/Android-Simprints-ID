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
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit

class SessionEventsUploaderTask(private val projectId: String,
                                private val sessionsIds: Array<String>,
                                private val sessionEventsManager: SessionEventsManager,
                                private val timeHelper: TimeHelper,
                                private val sessionApiClient: SessionsRemoteInterface) {

    companion object {
        const val DAYS_TO_KEEP_SESSIONS_IN_CASE_OF_ERROR = 31L
    }

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
            Timber.d("SessionEventsUploaderTask loadSessionsFromDb()")
            it.map { session -> sessionEventsManager.loadSessionById(session).blockingGet() }.toTypedArray()
        }

    @SuppressLint("CheckResult")
    private fun Single<Array<SessionEvents>>.closeOpenSessionsAndUpdateUploadTime(): Single<Array<SessionEvents>> =
        this.flatMap { sessions ->
            Timber.d("SessionEventsUploaderTask closeOpenSessionsAndUpdateUploadTime()")

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
            Timber.d("SessionEventsUploaderTask filterClosedSessions()")

            Single.just(sessions.filter { it.isClosed() }.toTypedArray())
        }

    @SuppressLint("CheckResult")
    private fun Single<Array<SessionEvents>>.uploadClosedSessionsOrThrowIfNoSessions(): Single<Result<Void?>> =
        this.flatMap { sessions ->
            if (sessions.isEmpty())
                throw NoSessionsFoundException()

            sessions.forEach { Timber.d("SessionEventsUploaderTask uploadClosedSessionsOrThrowIfNoSessions: ${it.id}") }
            sessionApiClient.uploadSessions(projectId, hashMapOf("sessions" to sessions))
        }

    private fun forceSessionToCloseIfOpenAndNotInProgress(session: SessionEvents, timeHelper: TimeHelper) {
        Timber.d("SessionEventsUploaderTask forceSessionToCloseIfOpenAndNotInProgress()")

        if (session.isOpen() && !session.isPossiblyInProgress(timeHelper)) {
            session.addArtificialTerminationIfRequired(timeHelper, ArtificialTerminationEvent.Reason.TIMED_OUT)
            session.closeIfRequired(timeHelper)
        }
    }

    private fun Single<out Result<Void?>>.checkUploadSucceed(): Completable =
        flatMapCompletable { result ->
            Timber.d("SessionEventsUploaderTask checkUploadSucceed()")

            when {
                result.response()?.code() == 201 -> Completable.complete()
                result.response() == null -> Completable.error(IOException(result.error()))
                else -> Completable.error(SessionUploadFailureException())
            }
        }

    private fun deleteSessions() {
        sessionsIds.forEach {
            sessionEventsManager.deleteSessions(sessionId = it, openSession = false).blockingGet()
        }
    }

    //To avoid db building up in case of errors, we delete session older 1 month.
    //So we have 1 month to fix any integration issues that comes up on the cloud.
    private fun deleteSessionsAfterAServerError() {
        sessionsIds.forEach {
            sessionEventsManager.deleteSessions(
                sessionId = it,
                openSession = false,
                startedBefore = timeHelper.nowMinus(DAYS_TO_KEEP_SESSIONS_IN_CASE_OF_ERROR, TimeUnit.DAYS)
            ).blockingGet()
        }
    }

    private fun Completable.deleteSessionsFromDb(): Completable =
        this.doOnError {
            Timber.d("SessionEventsUploaderTask deleteSessionsFromDb()")

            if (it is SessionUploadFailureException) {
                deleteSessionsAfterAServerError()
            }
        }.doOnComplete {
            deleteSessions()
        }
}

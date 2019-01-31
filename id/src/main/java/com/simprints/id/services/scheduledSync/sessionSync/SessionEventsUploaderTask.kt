package com.simprints.id.services.scheduledSync.sessionSync

import android.annotation.SuppressLint
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventData.controllers.remote.SessionsRemoteInterface
import com.simprints.id.data.analytics.eventData.models.domain.events.ArtificialTerminationEvent
import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.exceptions.safe.session.SessionUploadFailureException
import com.simprints.id.exceptions.safe.session.SessionUploadFailureRetryException
import com.simprints.id.tools.TimeHelper
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.adapter.rxjava2.Result
import timber.log.Timber

class SessionEventsUploaderTask(private val projectId: String,
                                private val sessionsIds: List<String>,
                                private val sessionEventsManager: SessionEventsManager,
                                private val timeHelper: TimeHelper,
                                private val sessionApiClient: SessionsRemoteInterface) {

    companion object {
        val SERVER_ERROR_CODES_NOT_WORTH_TO_RETRY = arrayListOf(400, 401, 403, 404)
        const val NUMBER_OF_ATTEMPTS_TO_RETRY_NETWORK_CALLS = 3L
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
            .checkUploadSucceedAndRetryIfNecessary()
            .deleteSessionsFromDb()

    private fun Single<List<String>>.loadSessionsFromDb(): Single<List<SessionEvents>> =
        this.map {
            Timber.d("SessionEventsUploaderTask loadSessionsFromDb()")
            it.map { session -> sessionEventsManager.loadSessionById(session).blockingGet() }
        }

    @SuppressLint("CheckResult")
    private fun Single<List<SessionEvents>>.closeOpenSessionsAndUpdateUploadTime(): Single<List<SessionEvents>> =
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
    private fun Single<List<SessionEvents>>.filterClosedSessions(): Single<List<SessionEvents>> =
        this.flatMap { sessions ->
            Timber.d("SessionEventsUploaderTask filterClosedSessions()")

            Single.just(sessions.filter { it.isClosed() })
        }

    @SuppressLint("CheckResult")
    private fun Single<List<SessionEvents>>.uploadClosedSessionsOrThrowIfNoSessions(): Single<Result<Void?>> =
        this.flatMap { sessions ->
            if (sessions.isEmpty())
                throw NoSessionsFoundException()

            sessions.forEach { Timber.d("SessionEventsUploaderTask uploadClosedSessionsOrThrowIfNoSessions: ${it.id}") }
            sessionApiClient.uploadSessions(projectId, hashMapOf("sessions" to sessions.toTypedArray()))
        }

    private fun forceSessionToCloseIfOpenAndNotInProgress(session: SessionEvents, timeHelper: TimeHelper) {
        Timber.d("SessionEventsUploaderTask forceSessionToCloseIfOpenAndNotInProgress()")

        if (session.isOpen() && !session.isPossiblyInProgress(timeHelper)) {
            session.addArtificialTerminationIfRequired(timeHelper, ArtificialTerminationEvent.Reason.TIMED_OUT)
            session.closeIfRequired(timeHelper)
        }
    }

    private fun Single<out Result<Void?>>.checkUploadSucceedAndRetryIfNecessary(): Completable =
        flatMapCompletable { result ->
            Timber.d("SessionEventsUploaderTask checkUploadSucceedAndRetryIfNecessary()")
            val response = result.response()
            when {
                response == null -> continueWithRetryException(result.error())
                isResponseASuccess(response.code()) -> continueWithSuccess()
                isResponseAnErrorThatIsWorthToRetry(response.code()) -> continueWithRetryException()
                else -> continueWithNoRetryException()
            }
        }.retry { counter, t ->
            counter < NUMBER_OF_ATTEMPTS_TO_RETRY_NETWORK_CALLS && t !is SessionUploadFailureException
        }

    private fun isResponseAnErrorThatIsWorthToRetry(code: Int) = !SERVER_ERROR_CODES_NOT_WORTH_TO_RETRY.contains(code)
    private fun isResponseASuccess(code: Int) = code == 201

    private fun continueWithSuccess() = Completable.complete()
    private fun continueWithRetryException(error: Throwable? = null) =
        Completable.error(
            error?.let {
                SessionUploadFailureRetryException(it)
            } ?: SessionUploadFailureRetryException())

    private fun continueWithNoRetryException() = Completable.error(SessionUploadFailureException())

    private fun deleteSessions() {
        sessionsIds.forEach {
            sessionEventsManager.deleteSessions(sessionId = it, openSession = false).blockingGet()
        }
    }

    private fun Completable.deleteSessionsFromDb(): Completable =
        this.doOnComplete {
            deleteSessions()
        }
}

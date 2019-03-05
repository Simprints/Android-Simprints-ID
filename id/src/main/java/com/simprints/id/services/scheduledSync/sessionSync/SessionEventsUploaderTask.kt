package com.simprints.id.services.scheduledSync.sessionSync

import android.annotation.SuppressLint
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.controllers.remote.SessionsRemoteInterface
import com.simprints.id.data.analytics.eventdata.models.domain.events.ArtificialTerminationEvent
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.data.analytics.eventdata.models.remote.session.ApiSessionEvents
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.exceptions.safe.session.SessionUploadFailureException
import com.simprints.id.exceptions.safe.session.SessionUploadFailureRetryException
import com.simprints.id.tools.TimeHelper
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.adapter.rxjava2.Result
import timber.log.Timber

class SessionEventsUploaderTask(private val sessionEventsManager: SessionEventsManager,
                                private val timeHelper: TimeHelper,
                                private val sessionApiClient: SessionsRemoteInterface) {

    companion object {
        val SERVER_ERROR_CODES_NOT_WORTH_TO_RETRY = arrayListOf(400, 401, 403, 404)
        const val NUMBER_OF_ATTEMPTS_TO_RETRY_NETWORK_CALLS = 3L
    }

    /**
     * @throws NoSessionsFoundException
     * @throws SessionUploadFailureException
     * @throws SessionUploadFailureRetryException
     */
    fun execute(projectId: String,
                sessions: List<SessionEvents>): Completable =
        Single.just(sessions)
            .closeOpenSessionsAndUpdateUploadTime()
            .filterClosedSessions()
            .uploadClosedSessionsOrThrowIfNoSessions(projectId)
            .deleteSessionsFromDb()

    @SuppressLint("CheckResult")
    internal fun Single<List<SessionEvents>>.closeOpenSessionsAndUpdateUploadTime(): Single<List<SessionEvents>> =
        this.flatMap { sessions ->
            Timber.d("SessionEventsUploaderTask closeOpenSessionsAndUpdateUploadTime()")

            sessions.forEach {
                forceSessionToCloseIfOpenAndNotInProgress(it, timeHelper)
                it.relativeUploadTime = it.nowRelativeToStartTime(timeHelper)
                sessionEventsManager.insertOrUpdateSessionEvents(it).blockingAwait()
            }
            Single.just(sessions)
        }

    private fun forceSessionToCloseIfOpenAndNotInProgress(session: SessionEvents, timeHelper: TimeHelper) {
        Timber.d("SessionEventsUploaderTask forceSessionToCloseIfOpenAndNotInProgress()")

        if (session.isOpen() && !session.isPossiblyInProgress(timeHelper)) {
            session.addArtificialTerminationIfRequired(timeHelper, ArtificialTerminationEvent.Reason.TIMED_OUT)
            session.closeIfRequired(timeHelper)
        }
    }

    @SuppressLint("CheckResult")
    internal fun Single<List<SessionEvents>>.filterClosedSessions(): Single<List<SessionEvents>> =
        this.flatMap { sessions ->
            Timber.d("SessionEventsUploaderTask filterClosedSessions()")

            Single.just(sessions.filter { it.isClosed() })
        }

    @SuppressLint("CheckResult")
    internal fun Single<List<SessionEvents>>.uploadClosedSessionsOrThrowIfNoSessions(projectId: String): Single<List<SessionEvents>> =
        this.flatMap { sessions ->
            sessions.forEach { Timber.d("SessionEventsUploaderTask uploadClosedSessionsOrThrowIfNoSessions: ${it.id}") }

            if (sessions.isEmpty())
                throw NoSessionsFoundException()

            sessionApiClient.uploadSessions(projectId, hashMapOf("sessions" to sessions.map { ApiSessionEvents(it) }.toTypedArray()))
                .checkUploadSucceedAndRetryIfNecessary()
                .andThen(Single.just(sessions))
        }

    internal fun Single<out Result<Void?>>.checkUploadSucceedAndRetryIfNecessary(): Completable =
        flatMapCompletable { result ->
            Timber.d("SessionEventsUploaderTask checkUploadSucceedAndRetryIfNecessary()")
            val response = result.response()
            when {
                response == null ->
                    continueWithRetryException(result.error() ?: Throwable("Sessions upload response is null"))
                isResponseASuccess(response.code()) ->
                    continueWithSuccess()
                isResponseAnErrorThatIsWorthToRetry(response.code()) ->
                    continueWithRetryException(Throwable("Sessions upload response code: ${response.code()}"))
                else ->
                    continueWithNoRetryException()
            }
        }.retry { counter, t ->
            counter < NUMBER_OF_ATTEMPTS_TO_RETRY_NETWORK_CALLS && t !is SessionUploadFailureException
        }

    private fun isResponseAnErrorThatIsWorthToRetry(code: Int) = !SERVER_ERROR_CODES_NOT_WORTH_TO_RETRY.contains(code)
    private fun isResponseASuccess(code: Int) = code == 201

    private fun continueWithSuccess() = Completable.complete()
    private fun continueWithRetryException(error: Throwable) = Completable.error(SessionUploadFailureRetryException(error))
    private fun continueWithNoRetryException() = Completable.error(SessionUploadFailureException())

    internal fun Single<List<SessionEvents>>.deleteSessionsFromDb(): Completable =
        this.flatMapCompletable { sessions ->
            Timber.d("SessionEventsUploaderTask deleteSessionsFromDb()")
            Completable.fromCallable {
                sessions.forEach { session ->
                    sessionEventsManager.deleteSessions(sessionId = session.id, openSession = false).blockingGet()
                }
            }
        }
}

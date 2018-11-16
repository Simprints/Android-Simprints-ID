package com.simprints.id.services.scheduledSync.sessionSync

import android.annotation.SuppressLint
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventData.controllers.remote.SessionsRemoteInterface
import com.simprints.id.data.analytics.eventData.models.domain.events.ArtificialTerminationEvent
import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.exceptions.safe.session.SessionUploadFailureException
import com.simprints.id.tools.TimeHelper
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.adapter.rxjava2.Result
import timber.log.Timber
import java.io.IOException

class ScheduledSessionsTask(private val sessionEventsManager: SessionEventsManager,
                            private val timeHelper: TimeHelper,
                            private val sessionApiClient: SessionsRemoteInterface,
                            private val analyticsManager: AnalyticsManager) {

    companion object {
        var BATCH_SIZE = 20
    }

    private var didAnUploadFail = false

    /**
     * @throws NoSessionsFoundException
     * @throws SessionUploadFailureException
     */
    fun syncSessions(projectId: String): Completable =
        sessionEventsManager.loadSessions(projectId)
            .createBatches()
            .closeOpenSessionsAndUpdateUploadTimeIfAny()
            .filterClosedSessionsIfAny()
            .uploadClosedSessionsIfAny(projectId)
            .checkUploadSucceed()
            .logErrorIfHappened()
            .onErrorReturn {
                if (it !is NoSessionsFoundException) {
                    didAnUploadFail = true
                }
                true //emmit null and swallow error
            }
            .ignoreElements()
            .andThen(Completable.create {
                sessionEventsManager.deleteSessions(openSession = false).blockingAwait()

                if (didAnUploadFail) {
                    it.onError(SessionUploadFailureException())
                } else {
                    it.onComplete()
                }
            })

    private fun Single<ArrayList<SessionEvents>>.createBatches(): Observable<ArrayList<SessionEvents>> =
        this.toObservable()
            .flatMap { Observable.fromIterable(it) }
            .buffer(BATCH_SIZE)
            .map { ArrayList(it) }

    @SuppressLint("CheckResult")
    private fun Observable<ArrayList<SessionEvents>>.closeOpenSessionsAndUpdateUploadTimeIfAny(): Observable<ArrayList<SessionEvents>> =
        this.flatMap { sessions ->
            sessions.forEach {
                forceSessionToCloseIfOpenAndNotInProgress(it, timeHelper)
                it.relativeUploadTime = it.nowRelativeToStartTime(timeHelper)
                sessionEventsManager.insertOrUpdateSessionEvents(it).blockingAwait()
            }
            Observable.just(sessions)
        }

    @SuppressLint("CheckResult")
    private fun Observable<ArrayList<SessionEvents>>.filterClosedSessionsIfAny(): Observable<ArrayList<SessionEvents>> =
        this.flatMap { sessions ->
            sessions.filter { it.isClosed() }.let { closedSessions ->
                if (closedSessions.isEmpty())
                    throw NoSessionsFoundException()

                Observable.just(ArrayList(closedSessions))
            }
        }

    @SuppressLint("CheckResult")
    private fun Observable<ArrayList<SessionEvents>>.uploadClosedSessionsIfAny(projectId: String): Observable<Result<Void?>> =
        this.flatMap { sessions ->
            sessionApiClient.uploadSessions(projectId, hashMapOf("sessions" to sessions.toTypedArray())).toObservable()
        }


    private fun forceSessionToCloseIfOpenAndNotInProgress(session: SessionEvents, timeHelper: TimeHelper) {
        if (session.isOpen() && !session.isPossiblyInProgress(timeHelper)) {
            session.addArtificialTerminationIfRequired(timeHelper, ArtificialTerminationEvent.Reason.TIMED_OUT)
            session.closeIfRequired(timeHelper)
        }
    }

    private fun Observable<out Result<Void?>>.checkUploadSucceed(): Observable<Boolean> =
        map { result ->
            when {
                result.response()?.code() == 201 -> true
                result.response() == null -> throw IOException(result.error())
                else -> throw SessionUploadFailureException()
            }
        }

    private fun Observable<Boolean>.logErrorIfHappened() =
        this.doOnError {
            Timber.e(it)
            if (it !is NoSessionsFoundException) {
                analyticsManager.logThrowable(it)
            }
        }
}

package com.simprints.id.services.scheduledSync.sessionSync

import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventData.controllers.remote.SessionsRemoteInterface
import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.tools.TimeHelper
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class SessionEventsSyncMasterTask(
    private val projectId: String,
    private val sessionEventsManager: SessionEventsManager,
    private val timeHelper: TimeHelper,
    private val sessionApi: SessionsRemoteInterface,
    private val analyticsManager: AnalyticsManager) {

    companion object {
        var BATCH_SIZE = 20
    }

    fun execute(): Completable =
        sessionEventsManager.loadSessions(projectId).map { it.toList() }
            .createBatches()
            .executeUploaderTask()

    internal fun Single<List<SessionEvents>>.createBatches(): Observable<List<SessionEvents>> =
        this.flattenAsObservable { it }
            .buffer(BATCH_SIZE)


    internal fun Observable<List<SessionEvents>>.executeUploaderTask(): Completable =
        this.concatMapCompletable {
            createUploadBatchTaskCompletable(it).doOnError { t ->
                    if (t !is NoSessionsFoundException) {
                        analyticsManager.logThrowable(t)
                    }
                }.onErrorComplete()
        }

    internal fun createUploadBatchTaskCompletable(sessions: List<SessionEvents>): Completable =
        SessionEventsUploaderTask(sessionEventsManager, timeHelper, sessionApi).execute(projectId, sessions)
}

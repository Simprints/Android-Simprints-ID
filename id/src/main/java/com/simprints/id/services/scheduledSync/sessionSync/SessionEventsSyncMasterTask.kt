package com.simprints.id.services.scheduledSync.sessionSync

import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.controllers.remote.SessionsRemoteInterface
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.tools.TimeHelper
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber

class SessionEventsSyncMasterTask(
    private val projectId: String,
    private val sessionEventsManager: SessionEventsManager,
    private val timeHelper: TimeHelper,
    private val sessionApi: SessionsRemoteInterface,
    private val crashReportManager: CrashReportManager) {

    companion object {
        var BATCH_SIZE = 20
    }

    fun execute(): Completable =
        loadSessionsToUpload()
            .createBatches()
            .executeUploaderTask()

    private fun loadSessionsToUpload() =
        sessionEventsManager.loadSessions(projectId).map { it.toList() }

    internal fun Single<List<SessionEvents>>.createBatches(): Observable<List<SessionEvents>> =
        this.flattenAsObservable { it }
            .buffer(BATCH_SIZE)


    internal fun Observable<List<SessionEvents>>.executeUploaderTask(): Completable =
        this.concatMapCompletable {
            createUploadBatchTaskCompletable(it).doOnError { t ->
                if (t !is NoSessionsFoundException) {
                    Timber.e(t)
                    crashReportManager.logExceptionOrThrowable(t)
                }
            }.onErrorComplete()
        }

    internal fun createUploadBatchTaskCompletable(sessions: List<SessionEvents>): Completable =
        SessionEventsUploaderTask(sessionEventsManager, timeHelper, sessionApi).execute(projectId, sessions)
}

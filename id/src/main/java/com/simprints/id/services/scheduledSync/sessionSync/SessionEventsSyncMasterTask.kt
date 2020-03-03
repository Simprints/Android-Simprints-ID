package com.simprints.id.services.scheduledSync.sessionSync

import com.simprints.core.tools.extentions.singleWithSuspend
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.domain.models.SessionQuery
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.data.db.session.remote.SessionsRemoteInterface
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.tools.TimeHelper
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.toList
import timber.log.Timber

class SessionEventsSyncMasterTask(
    private val projectId: String,
    private val sessionRepository: SessionRepository,
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
        singleWithSuspend{ sessionRepository.load(SessionQuery(projectId = projectId)).toList() }

    internal fun Single<List<SessionEvents>>.createBatches(): Observable<List<SessionEvents>> =
        this.flattenAsObservable { it }
            .buffer(BATCH_SIZE)


    internal fun Observable<List<SessionEvents>>.executeUploaderTask(): Completable =
        this.concatMapCompletable {
            createUploadBatchTaskCompletable(it).doOnError { t ->
                if (t !is NoSessionsFoundException) {
                    Timber.e(t)
                    crashReportManager.logExceptionOrSafeException(t)
                }
            }.onErrorComplete()
        }

    internal fun createUploadBatchTaskCompletable(sessions: List<SessionEvents>): Completable =
        SessionEventsUploaderTask(sessionRepository, timeHelper, sessionApi).execute(projectId, sessions)
}

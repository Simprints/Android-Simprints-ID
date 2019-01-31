package com.simprints.id.services.scheduledSync.sessionSync

import androidx.work.*
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventData.controllers.remote.SessionsRemoteInterface
import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.tools.TimeHelper
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.ArrayList

class SessionEventsSyncMasterTask(
    private val projectId: String,
    private val sessionEventsManager: SessionEventsManager,
    private val timeHelper: TimeHelper,
    private val sessionApi: SessionsRemoteInterface,
    private val analyticsManager: AnalyticsManager) {

    companion object {
        var BATCH_SIZE = 20
        const val SESSIONS_TO_UPLOAD_TAG = "SESSIONS_TO_UPLOAD_TAG"
        const val SESSIONS_IDS_KEY = "SESSIONS_IDS_KEY"
        const val PROJECT_ID_KEY: String = "PROJECT_ID_KEY"
    }

    fun execute(): Completable =
        sessionEventsManager.loadSessions(projectId)
            .createBatches()
            .executeUploaderTask()

    private fun Single<ArrayList<SessionEvents>>.createBatches(): Observable<List<SessionEvents>> =
        this.flattenAsObservable { it }
            .buffer(BATCH_SIZE)

    private fun Observable<List<SessionEvents>>.executeUploaderTask(): Completable =
        this.flatMapCompletable { sessions ->
            val uploadTask = SessionEventsUploaderTask(projectId, sessions, sessionEventsManager, timeHelper, sessionApi)
            uploadTask.execute()
        }.doOnError {
            if (it !is NoSessionsFoundException) {
                analyticsManager.logThrowable(it)
            }
        }.onErrorComplete()
}

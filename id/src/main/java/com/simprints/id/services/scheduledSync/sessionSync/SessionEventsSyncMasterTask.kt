package com.simprints.id.services.scheduledSync.sessionSync

import androidx.work.*
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class SessionEventsSyncMasterTask(
    private val projectId: String,
    private val sessionEventsManager: SessionEventsManager,
    private val getWorkManager: () -> WorkManager = WorkManager::getInstance) {

    companion object {
        var BATCH_SIZE = 20
        const val SESSIONS_TO_UPLOAD_TAG = "SESSIONS_TO_UPLOAD_TAG"
        const val SESSIONS_IDS_KEY = "SESSIONS_IDS_KEY"
        const val PROJECT_ID_KEY: String = "PROJECT_ID_KEY"
    }

    fun execute(): Completable =
        sessionEventsManager.loadSessions(projectId)
            .cancelPreviousUploadTasks()
            .createBatches()
            .registerOneTimeUploader()

    private fun Single<ArrayList<SessionEvents>>.cancelPreviousUploadTasks(): Single<List<SessionEvents>> =
        this.map {
            getWorkManager().cancelAllWorkByTag(SESSIONS_TO_UPLOAD_TAG)
            it
        }

    private fun Single<List<SessionEvents>>.createBatches(): Observable<List<SessionEvents>> =
        this.flattenAsObservable { it }
            .buffer(BATCH_SIZE)

    private fun Observable<List<SessionEvents>>.registerOneTimeUploader(): Completable =
        this.flatMapCompletable { sessions ->
            Completable.fromAction {
                getWorkManager().enqueue(buildWorkRequest(sessions.map { it.id }.toTypedArray()))
            }
        }

    private fun buildWorkRequest(sessionsIds: Array<String>): OneTimeWorkRequest =
        OneTimeWorkRequestBuilder<SessionEventsUploaderWorker>()
            .setConstraints(buildConstraints())
            .setInputData(buildWorkData(sessionsIds))
            .addTag(SESSIONS_TO_UPLOAD_TAG)
            .build()

    private fun buildConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun buildWorkData(sessionsIds: Array<String>) =
        workDataOf(
            SESSIONS_IDS_KEY to sessionsIds,
            PROJECT_ID_KEY to projectId)
}

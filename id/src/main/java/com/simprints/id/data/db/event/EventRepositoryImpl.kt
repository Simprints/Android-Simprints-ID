package com.simprints.id.data.db.event

import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.Event.EventLabel.SessionId
import com.simprints.id.data.db.event.domain.events.EventPayloadType.SESSION_CAPTURE
import com.simprints.id.data.db.event.domain.events.EventQuery
import com.simprints.id.data.db.event.domain.events.EventQuery.byType
import com.simprints.id.data.db.event.domain.events.session.SessionCaptureEvent
import com.simprints.id.data.db.event.domain.events.session.SessionCaptureEvent.SessionCapturePayload
import com.simprints.id.data.db.event.local.SessionLocalDataSource
import com.simprints.id.data.db.event.remote.SessionRemoteDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.ignoreException
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first

// Class to manage the current activeSession
open class EventRepositoryImpl(
    private val deviceId: String,
    private val appVersionName: String,
    private val projectId: String,
    private val sessionEventsSyncManager: SessionEventsSyncManager,
    private val sessionLocalDataSource: SessionLocalDataSource,
    private val sessionRemoteDataSource: SessionRemoteDataSource,
    private val preferencesManager: PreferencesManager,
    private val crashReportManager: CrashReportManager,
    private val timeHelper: TimeHelper
) : EventRepository {

    companion object {
        const val PROJECT_ID_FOR_NOT_SIGNED_IN = "NOT_SIGNED_IN"
        const val SESSION_BATCH_SIZE = 20
    }

    // as default, the manager tries to load the last open activeSession
    //
    override suspend fun getCurrentSession(): SessionCaptureEvent =
        reportExceptionIfNeeded {
            sessionLocalDataSource.load(byType(SESSION_CAPTURE))
                .filterIsInstance<SessionCaptureEvent>()
                .first { (it.payload as SessionCapturePayload).endTime == 0L }
        }

    override suspend fun createSession(libSimprintsVersionName: String) {
        reportExceptionIfNeeded {
            sessionLocalDataSource.create(appVersionName, libSimprintsVersionName, preferencesManager.language, deviceId)
        }
    }

    override suspend fun addEvent(event: Event) {
        ignoreException {
            reportExceptionIfNeeded {
                val session = sessionLocalDataSource.load(byType(SESSION_CAPTURE))
                    .filterIsInstance<SessionCaptureEvent>()
                    .first { (it.payload as SessionCapturePayload).endTime == 0L }

                event.labels.add(SessionId(session.id))
                sessionLocalDataSource.insertOrUpdate(event)
            }
        }
    }

    override suspend fun uploadSessions() {
        createBatchesFromLocalAndUploadSessions()
    }

    private suspend fun createBatchesFromLocalAndUploadSessions() {
        //STOPSHIP
//        loadSessionsToUpload()
//            .filterClosedSessions()
//            .createBatches()
//            .updateRelativeUploadTimeAndUploadSessions()
//            .deleteSessionsFromDb()
    }

//    private suspend fun loadSessionsToUpload() =
//        sessionLocalDataSource.load(SessionQuery(projectId = projectId)).also {
//            val open = sessionLocalDataSource.count(SessionQuery(projectId = projectId, openSession = true))
//            val close = sessionLocalDataSource.count(SessionQuery(projectId = projectId, openSession = false))
//            val total = sessionLocalDataSource.count(SessionQuery(projectId = projectId))
//            Timber.d("Preparing sessions for $projectId: total $total (open $open, close $close)")
//        }

//    private suspend fun Flow<SessionCaptureEvent>.createBatches() =
//        this.bufferedChunks(SESSION_BATCH_SIZE)
//
//    private suspend fun Flow<List<SessionCaptureEvent>>.updateRelativeUploadTimeAndUploadSessions(): Flow<List<SessionCaptureEvent>> {
//        this.collect {
//            val sessionsWithUpdatedRelativeUploadTime = it.updateRelativeUploadTime()
//            Timber.d("Uploading ${sessionsWithUpdatedRelativeUploadTime.size} sessions")
//            sessionRemoteDataSource.uploadSessions(projectId, sessionsWithUpdatedRelativeUploadTime)
//        }
//        return this
//    }
//
//    private suspend fun Flow<List<SessionCaptureEvent>>.deleteSessionsFromDb() {
//        this.collect { sessions ->
//            sessions.forEach {
//                sessionLocalDataSource.delete(SessionQuery(openSession = false))
//            }
//        }
//    }


    override suspend fun updateCurrentSession(updateBlock: suspend (SessionCaptureEvent) -> Unit) {
        reportExceptionIfNeeded {
            val sessionCaptureEvent = getCurrentCaptureSessionEvent()
            updateSession(sessionCaptureEvent.id, updateBlock)
        }
    }

    private suspend fun getCurrentCaptureSessionEvent() =
        sessionLocalDataSource.load(byType(SESSION_CAPTURE))
            .filterIsInstance<SessionCaptureEvent>()
            .first { (it.payload as SessionCapturePayload).endTime == 0L }

    override suspend fun updateSession(sessionId: String, updateBlock: suspend (SessionCaptureEvent) -> Unit) {
        reportExceptionIfNeeded {
            val sessionCaptureEvent = sessionLocalDataSource.load(EventQuery.byLabel(SessionId(sessionId))).first()
            updateBlock(sessionCaptureEvent as SessionCaptureEvent)
            sessionLocalDataSource.insertOrUpdate(sessionCaptureEvent)
        }
    }

    override suspend fun load(): List<Event> {
        TODO("Not yet implemented") //STOPSHIP
    }

    override suspend fun signOut() {
        //sessionLocalDataSource.delete(SessionQuery(openSession = false))
        sessionEventsSyncManager.cancelSyncWorkers()
    }

    private suspend fun <T> reportExceptionIfNeeded(block: suspend () -> T): T =
        try {
            block()
        } catch (t: Throwable) {
            crashReportManager.logExceptionOrSafeException(t)
            throw t
        }

    private fun List<SessionCaptureEvent>.updateRelativeUploadTime() = map { session ->
//        session.also {
//            it.relativeUploadTime = timeHelper.now() - it.startTime
//        }
    }

}

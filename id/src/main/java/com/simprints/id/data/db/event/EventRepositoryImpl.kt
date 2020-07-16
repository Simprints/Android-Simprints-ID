package com.simprints.id.data.db.event

import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.Event.EventLabel.SessionId
import com.simprints.id.data.db.event.domain.events.EventPayloadType.SESSION_CAPTURE
import com.simprints.id.data.db.event.domain.events.session.SessionCaptureEvent
import com.simprints.id.data.db.event.local.SessionLocalDataSource
import com.simprints.id.data.db.event.local.SessionLocalDataSource.EventQuery
import com.simprints.id.data.db.event.remote.SessionRemoteDataSource
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.ignoreException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList

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

    override suspend fun createSession(libSimprintsVersionName: String) {
        reportExceptionIfNeeded {
            sessionLocalDataSource.create(appVersionName, libSimprintsVersionName, preferencesManager.language, deviceId)
        }
    }

    override suspend fun addEventToCurrentSession(event: Event) {
        ignoreException {
            reportExceptionIfNeeded {
                val session = sessionLocalDataSource.getCurrentSessionCaptureEvent()
                session?.let {
                    event.labels.add(SessionId(session.id))
                    sessionLocalDataSource.insertOrUpdate(event)
                } ?: Throwable("Missing open session")
            }
        }
    }

    override suspend fun addEvent(event: Event) {
        ignoreException {
            reportExceptionIfNeeded {
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
            updateSession(getCurrentCaptureSessionEvent().id, updateBlock)
        }
    }

    override suspend fun getCurrentCaptureSessionEvent(): SessionCaptureEvent =
        sessionLocalDataSource.getCurrentSessionCaptureEvent()

    override suspend fun updateSession(sessionId: String, updateBlock: suspend (SessionCaptureEvent) -> Unit) {
        reportExceptionIfNeeded {
            val sessionCaptureEvent = sessionLocalDataSource.load(EventQuery(sessionId = sessionId)).first()
            updateBlock(sessionCaptureEvent as SessionCaptureEvent)
            sessionLocalDataSource.insertOrUpdate(sessionCaptureEvent)
        }
    }

    override suspend fun load(): List<Event> = sessionLocalDataSource.load().toList()

    override suspend fun signOut() {
        sessionLocalDataSource.load(EventQuery(eventPayloadType = SESSION_CAPTURE, endTime = LongRange(0, 0))).collect {
            sessionLocalDataSource.delete(EventQuery(sessionId = it.id))
        }
        sessionEventsSyncManager.cancelSyncWorkers()
    }

    private suspend fun <T> reportExceptionIfNeeded(block: suspend () -> T): T =
        try {
            block()
        } catch (t: Throwable) {
            crashReportManager.logExceptionOrSafeException(t)
            throw t
        }
}

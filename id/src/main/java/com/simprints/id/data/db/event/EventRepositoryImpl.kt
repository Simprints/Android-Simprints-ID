package com.simprints.id.data.db.event

import android.os.Build
import android.os.Build.VERSION
import androidx.annotation.VisibleForTesting
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.domain.models.session.DatabaseInfo
import com.simprints.id.data.db.event.domain.models.session.Device
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.event.local.EventLocalDataSource
import com.simprints.id.data.db.event.local.models.DbEventQuery
import com.simprints.id.data.db.event.remote.EventRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modality.toMode
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.ignoreException
import kotlinx.coroutines.flow.*

// Class to manage the current activeSession
open class EventRepositoryImpl(
    private val deviceId: String,
    private val appVersionName: String,
    private val loginInfoManager: LoginInfoManager,
    private val sessionEventsSyncManager: SessionEventsSyncManager,
    private val eventLocalDataSource: EventLocalDataSource,
    private val eventRemoteDataSource: EventRemoteDataSource,
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
            val count = eventLocalDataSource.count()
            val sessionCaptureEvent = SessionCaptureEvent(
                PROJECT_ID_FOR_NOT_SIGNED_IN,
                timeHelper.now(),
                preferencesManager.modalities.map { it.toMode() },
                appVersionName,
                libSimprintsVersionName,
                preferencesManager.language,
                Device(
                    VERSION.SDK_INT.toString(),
                    Build.MANUFACTURER + "_" + Build.MODEL,
                    deviceId),
                DatabaseInfo(count))

            closeAnyOpenSession()
            eventLocalDataSource.insertOrUpdate(sessionCaptureEvent)
        }
    }

    override suspend fun addEvent(sessionId: String, event: Event) {
        ignoreException {
            reportExceptionIfNeeded {
                val session = eventLocalDataSource.load(DbEventQuery(id = sessionId)).toList().firstOrNull()
                session.let {
                    event.labels = event.labels.appendLabelsForAllEvents().appendSessionId(sessionId)
                    eventLocalDataSource.insertOrUpdate(event)
                }
            }
        }
    }

    override suspend fun addEventToCurrentSession(event: Event) {
        ignoreException {
            reportExceptionIfNeeded {
                val session = getCurrentCaptureSessionEvent()
                addEvent(session.id, event)
            }
        }
    }

    override suspend fun downloadEvents(): Flow<DownloadEventProgress> =
        emptyFlow()

    override suspend fun uploadEvents(): Flow<OperationEventProgress> = flow {
        val batches = createBatchesWithCloseSessions()
        var currentProgress = OperationEventProgress(0, batches.sumBy { it.count })

        batches.forEach { batch ->
            val events: List<Event> = batch.sessionIds.fold(mutableListOf()) { acc, sessionId ->
                acc.addAll(eventLocalDataSource.load(DbEventQuery(sessionId = sessionId)).toList())
                acc
            }
            eventRemoteDataSource.post(loginInfoManager.getSignedInProjectIdOrEmpty(), events)
            events.forEach {
                eventLocalDataSource.delete(DbEventQuery(id = it.id))
            }

            currentProgress = currentProgress.copy(progress = currentProgress.progress + events.size)
            this.emit(currentProgress)
        }
    }

    @VisibleForTesting
    suspend fun createBatchesWithCloseSessions(): List<Batch> {
        val projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
        val sessionsAndCounts =
            eventLocalDataSource.load(DbEventQuery(projectId = projectId, type = SESSION_CAPTURE, endTime = LongRange(1, Long.MAX_VALUE))).map {
                it.id to eventLocalDataSource.count(DbEventQuery(sessionId = it.id))
            }

        return sessionsAndCounts.toList().fold(mutableListOf()) { batches, sessionAndCount ->
            val lastBatch = batches.lastOrNull()
            val lastBatchEventsCount = lastBatch?.sessionIds?.size ?: 0
            val sessionIdToAdd = sessionAndCount.first
            val eventsCountToAdd = sessionAndCount.second

            val hasLastBatchStillRoom = lastBatchEventsCount + eventsCountToAdd <= SESSION_BATCH_SIZE
            if (hasLastBatchStillRoom && lastBatch != null) {
                lastBatch.sessionIds.add(sessionAndCount.first)
                lastBatch.count += eventsCountToAdd
            } else {
                batches.add(Batch(mutableListOf(sessionIdToAdd), eventsCountToAdd))
            }
            batches
        }
    }

    override suspend fun getCurrentCaptureSessionEvent(): SessionCaptureEvent =
        reportExceptionIfNeeded {
            eventLocalDataSource.load(DbEventQuery(type = SESSION_CAPTURE, endTime = LongRange(0, 0))).first() as SessionCaptureEvent
        }


    override suspend fun loadEvents(sessionId: String): Flow<Event> =
        reportExceptionIfNeeded {
            eventLocalDataSource.load(DbEventQuery(sessionId = sessionId))
        }

    private suspend fun closeAnyOpenSession() {
        val openSessions = eventLocalDataSource.load(DbEventQuery(type = SESSION_CAPTURE, endTime = LongRange(0, 0))).map { it as SessionCaptureEvent }

        openSessions.collect { session ->
            val artificialTerminationEvent = ArtificialTerminationEvent(
                timeHelper.now(),
                NEW_SESSION
            )
            artificialTerminationEvent.labels = artificialTerminationEvent.labels.appendLabelsForAllEvents().appendSessionId(session.id)
            eventLocalDataSource.insertOrUpdate(artificialTerminationEvent)

            session.payload.endedAt = timeHelper.now()
            eventLocalDataSource.insertOrUpdate(session)
        }
    }

    override suspend fun signOut() {
        eventLocalDataSource.load(DbEventQuery(type = SESSION_CAPTURE, endTime = LongRange(0, 0))).collect {
            eventLocalDataSource.delete(DbEventQuery(sessionId = it.id))
        }
        sessionEventsSyncManager.cancelSyncWorkers()
    }

    private fun EventLabels.appendLabelsForAllEvents() =
        this.appendProjectIdLabel().appendDeviceIdLabel()

    private fun EventLabels.appendProjectIdLabel(): EventLabels {
        var projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
        if (projectId.isEmpty()) {
            projectId = PROJECT_ID_FOR_NOT_SIGNED_IN
        }
        return this.copy(projectId = projectId)
    }

    private fun EventLabels.appendDeviceIdLabel(): EventLabels = this.copy(deviceId = this@EventRepositoryImpl.deviceId)
    private fun EventLabels.appendSessionId(sessionId: String): EventLabels = this.copy(sessionId = sessionId)

    private suspend fun <T> reportExceptionIfNeeded(block: suspend () -> T): T =
        try {
            block()
        } catch (t: Throwable) {
            crashReportManager.logExceptionOrSafeException(t)
            throw t
        }

    data class Batch(val sessionIds: MutableList<String>, var count: Int)
}

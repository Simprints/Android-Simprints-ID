package com.simprints.id.data.db.event

import android.os.Build
import android.os.Build.VERSION
import androidx.annotation.VisibleForTesting
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.event.domain.EventCount
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.domain.models.session.DatabaseInfo
import com.simprints.id.data.db.event.domain.models.session.Device
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.event.local.EventLocalDataSource
import com.simprints.id.data.db.event.local.models.DbLocalEventQuery
import com.simprints.id.data.db.event.local.models.fromDomainToDb
import com.simprints.id.data.db.event.remote.EventRemoteDataSource
import com.simprints.id.data.db.events_sync.down.domain.RemoteEventQuery
import com.simprints.id.data.db.events_sync.down.domain.fromDomainToApi
import com.simprints.id.data.db.events_sync.up.domain.LocalEventQuery
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modality.toMode
import com.simprints.id.services.sync.sessionSync.SessionEventsSyncManager
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.ignoreException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
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

    private val signedInProject: String
        get() = if (loginInfoManager.getSignedInProjectIdOrEmpty().isEmpty()) {
            PROJECT_ID_FOR_NOT_SIGNED_IN
        } else {
            loginInfoManager.getSignedInProjectIdOrEmpty()
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
            sessionCaptureEvent.labels = sessionCaptureEvent.labels.appendLabelsForAllEvents()

            closeAnyOpenSession()
            eventLocalDataSource.insertOrUpdate(sessionCaptureEvent)
        }
    }

    override suspend fun addEvent(sessionId: String, event: Event) {
        ignoreException {
            reportExceptionIfNeeded {
                val session = eventLocalDataSource.load(DbLocalEventQuery(id = sessionId)).toList().firstOrNull()
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

    override suspend fun countEventsToUpload(query: LocalEventQuery): Int =
        eventLocalDataSource.count(query.fromDomainToDb())

    override suspend fun countEventsToDownload(query: RemoteEventQuery): List<EventCount> =
        eventRemoteDataSource.count(query.fromDomainToApi())


    override suspend fun downloadEvents(scope: CoroutineScope, query: RemoteEventQuery): ReceiveChannel<List<Event>> =
        eventRemoteDataSource.getEvents(query.fromDomainToApi(), scope)


    override suspend fun uploadEvents(): Flow<List<Event>> = flow {
        val batches = createBatchesWithCloseSessions()

        batches.forEach { batch ->
            val events = batch.sessionIds.map {
                eventLocalDataSource.load(DbLocalEventQuery(sessionId = it)).toList()
            }.flatten()

            eventRemoteDataSource.post(loginInfoManager.getSignedInProjectIdOrEmpty(), events)

            events.forEach {
                eventLocalDataSource.delete(DbLocalEventQuery(id = it.id))
            }

            this.emit(events)
        }
    }

    @VisibleForTesting
    suspend fun createBatchesWithCloseSessions(): List<Batch> {
        val sessionsAndCounts =
            loadCloseSessions().map {
                it.id to eventLocalDataSource.count(DbLocalEventQuery(sessionId = it.id))
            }

        return sessionsAndCounts.toList().fold(mutableListOf()) { batches, sessionAndCount ->
            val lastBatch = batches.lastOrNull()
            val eventsCountInTheLastBatch = lastBatch?.sessionIds?.size ?: 0
            val sessionIdToAdd = sessionAndCount.first
            val eventsCountToAdd = sessionAndCount.second

            val hasLastBatchStillRoom = eventsCountInTheLastBatch + eventsCountToAdd <= SESSION_BATCH_SIZE
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
            loadOpenSessions().first()
        }


    override suspend fun loadEvents(sessionId: String): Flow<Event> =
        reportExceptionIfNeeded {
            eventLocalDataSource.load(DbLocalEventQuery(sessionId = sessionId))
        }

    private suspend fun closeAnyOpenSession() {
        val openSessions = loadOpenSessions()

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
        eventLocalDataSource.load(DbLocalEventQuery(type = SESSION_CAPTURE, endTime = LongRange(0, 0))).collect {
            eventLocalDataSource.delete(DbLocalEventQuery(sessionId = it.id))
        }
        sessionEventsSyncManager.cancelSyncWorkers()
    }

    private suspend fun loadOpenSessions() =
        eventLocalDataSource.load(getDbQueryForOpenSession(signedInProject)).map { it as SessionCaptureEvent }

    private suspend fun loadCloseSessions() =
        eventLocalDataSource.load(getDbQueryForCloseSession(signedInProject)).map { it as SessionCaptureEvent }

    private fun getDbQueryForOpenSession(projectId: String) =
        DbLocalEventQuery(projectId = projectId, type = SESSION_CAPTURE, endTime = LongRange(0, 0))

    private fun getDbQueryForCloseSession(projectId: String) =
        DbLocalEventQuery(projectId = projectId, type = SESSION_CAPTURE, endTime = LongRange(1, Long.MAX_VALUE))

    private fun EventLabels.appendLabelsForAllEvents() =
        this.appendProjectIdLabel().appendDeviceIdLabel()

    private fun EventLabels.appendProjectIdLabel(): EventLabels {
        val projectId = if (signedInProject.isEmpty()) {
            PROJECT_ID_FOR_NOT_SIGNED_IN
        } else {
            signedInProject
        }

        return this.copy(projectId = projectId)
    }

    private fun EventLabels.appendDeviceIdLabel(): EventLabels = this.copy(deviceId = this@EventRepositoryImpl.deviceId)

    private fun EventLabels.appendSessionId(sessionId: String): EventLabels = this.copy(sessionId = sessionId)

    private suspend fun <T> reportExceptionIfNeeded(block: suspend () -> T): T =
        try {
            block()
        } catch (t: Throwable) {
            val events = eventLocalDataSource.load(DbLocalEventQuery()) //STOPSHIP

            crashReportManager.logExceptionOrSafeException(t)
            throw t
        }

    data class Batch(val sessionIds: MutableList<String>, var count: Int)
}

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
import com.simprints.id.data.db.event.domain.validators.SessionEventValidatorsFactory
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
import com.simprints.id.services.sync.events.common.SYNC_LOG_TAG
import com.simprints.id.tools.extensions.isClientAndCloudIntegrationIssue
import com.simprints.id.tools.ignoreException
import com.simprints.id.tools.time.TimeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
open class EventRepositoryImpl(
    private val deviceId: String,
    private val appVersionName: String,
    private val loginInfoManager: LoginInfoManager,
    private val eventLocalDataSource: EventLocalDataSource,
    private val eventRemoteDataSource: EventRemoteDataSource,
    private val preferencesManager: PreferencesManager,
    private val crashReportManager: CrashReportManager,
    private val timeHelper: TimeHelper,
    validatorsFactory: SessionEventValidatorsFactory
) : EventRepository {

    companion object {
        const val PROJECT_ID_FOR_NOT_SIGNED_IN = "NOT_SIGNED_IN"
        const val SESSION_BATCH_SIZE = 20
    }

    private val validators = validatorsFactory.build()

    private val currentProject: String
        get() = if (loginInfoManager.getSignedInProjectIdOrEmpty().isEmpty()) {
            PROJECT_ID_FOR_NOT_SIGNED_IN
        } else {
            loginInfoManager.getSignedInProjectIdOrEmpty()
        }


    override suspend fun createSession(libSimprintsVersionName: String) {
        reportExceptionIfNeeded {
            val count = eventLocalDataSource.count()
            val sessionCaptureEvent = SessionCaptureEvent(
                UUID.randomUUID().toString(),
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
            addEvent(sessionCaptureEvent)
        }
    }

    override suspend fun addEventToCurrentSession(event: Event) {
        ignoreException {
            reportExceptionIfNeeded {
                val session = getCurrentCaptureSessionEvent()
                addEvent(event, session.id)
            }
        }
    }

    override suspend fun addEvent(event: Event, sessionId: String) {
        ignoreException {
            reportExceptionIfNeeded {
                val session = eventLocalDataSource.load(DbLocalEventQuery(type = SESSION_CAPTURE, id = sessionId)).toList().firstOrNull() as SessionCaptureEvent

                session.let {
                    val eventsInSession = eventLocalDataSource.load(DbLocalEventQuery(sessionId = sessionId)).toList()
                    validators.forEach {
                        it.validate(eventsInSession, event)
                    }

                    event.labels = event.labels.appendSessionId(sessionId)
                    addEvent(event)
                }
            }
        }
    }

    override suspend fun addEvent(event: Event) {
        ignoreException {
            reportExceptionIfNeeded {
                event.labels = event.labels.appendLabelsForAllEvents()
                eventLocalDataSource.insertOrUpdate(event)
            }
        }
    }

    override suspend fun countEventsToUpload(query: LocalEventQuery): Int =
        createBatches(query).sumBy { it.events.size }

    override suspend fun countEventsToDownload(query: RemoteEventQuery): List<EventCount> =
        eventRemoteDataSource.count(query.fromDomainToApi())

    override suspend fun downloadEvents(scope: CoroutineScope, query: RemoteEventQuery): ReceiveChannel<Event> =
        eventRemoteDataSource.getEvents(query.fromDomainToApi(), scope)

    override suspend fun uploadEvents(query: LocalEventQuery): Flow<Int> = flow {
        val batches = createBatches(query)
        Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Uploading batches ${batches.size}")

        batches.forEach { batch ->
            val events = batch.events
            Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Uploading ${events.size} events in a batch")

            try {
                eventRemoteDataSource.post(currentProject, events)
                deleteEventsFromDb(events.map { it.id })
            } catch (t: Throwable) {
                Timber.d(t)
                if (t.isClientAndCloudIntegrationIssue()) {
                    deleteEventsFromDb(events.map { it.id })
                }
            }
            this.emit(events.size)
        }
    }

    private suspend fun deleteEventsFromDb(eventsIds: List<String>) {
        eventsIds.forEach {
            Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Deleting $it")
            eventLocalDataSource.delete(DbLocalEventQuery(id = it))
        }
    }

    @VisibleForTesting
    suspend fun createBatches(query: LocalEventQuery): List<Batch> {
        val events = createBatchesForEventsInSessions(query) + createBatchesForEventsNotInSessions(query)
        return events
    }

    private suspend fun createBatchesForEventsNotInSessions(query: LocalEventQuery): List<Batch> {
        val events = eventLocalDataSource.load(query.fromDomainToDb()).filter { it.labels.sessionId == null }.toList()
        return events.chunked(SESSION_BATCH_SIZE).map { Batch(it.toMutableList()) }
    }

    private suspend fun createBatchesForEventsInSessions(query: LocalEventQuery): List<Batch> {
        val sessionsToUpload = closeSessionsForQuery(query)

        return sessionsToUpload.fold(mutableListOf()) { batches, session ->
            val lastBatch = batches.lastOrNull()
            val eventsCountInTheLastBatch = lastBatch?.events?.size ?: 0
            val eventsToUpload = eventLocalDataSource.load(DbLocalEventQuery(sessionId = session.id)).toList()

            val hasLastBatchStillRoom = eventsCountInTheLastBatch + eventsToUpload.size <= SESSION_BATCH_SIZE
            if (hasLastBatchStillRoom && lastBatch != null) {
                Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Adding $eventsCountInTheLastBatch into an existing batch")

                lastBatch.events.addAll(eventsToUpload)
            } else {
                Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Creating new batch")

                batches.add(Batch(eventsToUpload.toMutableList()))
            }
            batches
        }
    }

    private suspend fun closeSessionsForQuery(query: LocalEventQuery) =
        eventLocalDataSource.load(
            query.copy(type = SESSION_CAPTURE, endTime = LongRange(1, Long.MAX_VALUE), projectId = currentProject).fromDomainToDb()).also {
            Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Loading sessions for query ${query.copy(type = SESSION_CAPTURE, endTime = LongRange(1, Long.MAX_VALUE), projectId = currentProject)}")
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
            artificialTerminationEvent.labels = artificialTerminationEvent.labels.appendSessionId(session.id)
            addEvent(artificialTerminationEvent)

            session.payload.endedAt = timeHelper.now()
            addEvent(session)
        }
    }

    override suspend fun signOut() {
        loadCloseSessions().collect {
            eventLocalDataSource.delete(DbLocalEventQuery(sessionId = it.id))
        }
    }

    private suspend fun loadOpenSessions() =
        eventLocalDataSource.load(getDbQueryForOpenSession().fromDomainToDb()).map { it as SessionCaptureEvent }

    private suspend fun loadCloseSessions() =
        eventLocalDataSource.load(getDbQueryForCloseSession().fromDomainToDb()).map { it as SessionCaptureEvent }

    private fun getDbQueryForOpenSession() =
        LocalEventQuery(type = SESSION_CAPTURE, endTime = LongRange(0, 0))

    private fun getDbQueryForCloseSession() =
        LocalEventQuery(type = SESSION_CAPTURE, endTime = LongRange(1, Long.MAX_VALUE))

    private fun EventLabels.appendLabelsForAllEvents() =
        this.appendProjectIdLabel().appendDeviceIdLabel()

    private fun EventLabels.appendProjectIdLabel(): EventLabels {
        return this.copy(projectId = currentProject)
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

    @VisibleForTesting
    data class Batch(val events: MutableList<Event>)
}

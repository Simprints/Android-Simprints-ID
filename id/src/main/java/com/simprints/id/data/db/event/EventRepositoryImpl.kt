package com.simprints.id.data.db.event

import android.os.Build
import android.os.Build.VERSION
import androidx.annotation.VisibleForTesting
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.event.domain.EventCount
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.TIMED_OUT
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.domain.models.isNotASubjectEvent
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
import com.simprints.id.exceptions.safe.sync.TryToUploadEventsForNotSignedProject
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
    validatorsFactory: SessionEventValidatorsFactory,
    override val libSimprintsVersionName: String
) : EventRepository {

    companion object {
        // When the sync starts, any open activeSession started GRACE_PERIOD ms
        // before it will be considered closed
        const val GRACE_PERIOD: Long = 1000 * 60 * 60 // 60 minutes

        const val PROJECT_ID_FOR_NOT_SIGNED_IN = "NOT_SIGNED_IN"
        const val SESSION_BATCH_SIZE = 20

        // Open session flag. When a session has an end time of 0, it means that the session is
        // open. We should not upload sessions with a 0 end time.
        val OPEN_SESSION_FLAG = LongRange(0, 0)
    }

    private val validators = validatorsFactory.build()

    private val currentProject: String
        get() = if (loginInfoManager.getSignedInProjectIdOrEmpty().isEmpty()) {
            PROJECT_ID_FOR_NOT_SIGNED_IN
        } else {
            loginInfoManager.getSignedInProjectIdOrEmpty()
        }


    override suspend fun createSession(libSimprintsVersion: String): SessionCaptureEvent {
        return reportExceptionIfNeeded {
            val sessionCount = eventLocalDataSource.count(DbLocalEventQuery(type = SESSION_CAPTURE))
            val sessionCaptureEvent = SessionCaptureEvent(
                UUID.randomUUID().toString(),
                currentProject,
                timeHelper.now(),
                preferencesManager.modalities.map { it.toMode() },
                appVersionName,
                libSimprintsVersionName,
                preferencesManager.language,
                Device(
                    VERSION.SDK_INT.toString(),
                    Build.MANUFACTURER + "_" + Build.MODEL,
                    deviceId),
                DatabaseInfo(sessionCount))

            closeSessionsAndAddArtificialTerminationEvent(loadOpenSessions(), NEW_SESSION)
            addEvent(sessionCaptureEvent)
            sessionCaptureEvent
        }
    }

    override suspend fun addEventToCurrentSession(event: Event) {
        ignoreException {
            reportExceptionIfNeeded {
                val session = getCurrentCaptureSessionEvent()
                addEventToSession(event, session)
            }
        }
    }

    override suspend fun addEventToSession(event: Event, session: SessionCaptureEvent) {
        ignoreException {
            reportExceptionIfNeeded {
                session.let {
                    val eventsInSession = eventLocalDataSource.load(DbLocalEventQuery(sessionId = session.id)).toList()
                    validators.forEach {
                        it.validate(eventsInSession, event)
                    }

                    event.labels = event.labels.copy(sessionId = session.id, projectId = session.payload.projectId)
                    addEvent(event)
                }
            }
        }
    }

    override suspend fun addEvent(event: Event) {
        ignoreException {
            reportExceptionIfNeeded {
                if (event.type.isNotASubjectEvent()) {
                    event.labels = event.labels.appendLabelsForAllSessionEvents()
                } else {
                    event.labels = event.labels.appendProjectIdLabel()
                }
                eventLocalDataSource.insertOrUpdate(event)
            }
        }
    }

    override suspend fun localCount(query: LocalEventQuery): Int =
        eventLocalDataSource.count(query.fromDomainToDb())

    override suspend fun countEventsToDownload(query: RemoteEventQuery): List<EventCount> =
        eventRemoteDataSource.count(query.fromDomainToApi())

    override suspend fun downloadEvents(scope: CoroutineScope, query: RemoteEventQuery): ReceiveChannel<Event> =
        eventRemoteDataSource.getEvents(query.fromDomainToApi(), scope)

    override suspend fun uploadEvents(query: LocalEventQuery): Flow<Int> = flow {
        Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Uploading")

        if (query.projectId != loginInfoManager.getSignedInProjectIdOrEmpty()) {
            throw TryToUploadEventsForNotSignedProject("Only events for the signed in project can be uploaded").also {
                crashReportManager.logException(it)
            }
        }

        val batches = createBatches(query)
        Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Uploading batches ${batches.size}")

        batches.forEach { batch ->
            val events = batch.events
            Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Uploading ${events.size} events in a batch")

            try {
                uploadEvents(events, query.projectId)

                deleteEventsFromDb(events.map { it.id })
            } catch (t: Throwable) {
                Timber.d(t)
                if (t.isClientAndCloudIntegrationIssue()) {
                    crashReportManager.logException(t)
                    //We do not delete subject events (pokodex) since they are important.
                    deleteEventsFromDb(events.filter { it.type.isNotASubjectEvent() }.map { it.id })
                }
            }
            this.emit(events.size)
        }
    }

    private suspend fun uploadEvents(events: MutableList<Event>, projectId: String) {
        events.filterIsInstance<SessionCaptureEvent>().forEach {
            it.payload.uploadedAt = timeHelper.now()
        }
        eventRemoteDataSource.post(projectId, events)
    }

    private suspend fun deleteEventsFromDb(eventsIds: List<String>) {
        eventsIds.forEach {
            Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Deleting $it")
            eventLocalDataSource.delete(DbLocalEventQuery(id = it))
        }
    }

    @VisibleForTesting
    suspend fun createBatches(query: LocalEventQuery): List<Batch> {
        Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Creating batches")
        return createBatchesForEventsNotInSessions(query) + createBatchesForEventsInSessions(query)
    }

    @Deprecated(
        "Before 2021.1.0, SID could have events not associated with a session in the db like " +
            "EnrolmentRecordCreationEvent that need to be uploaded. After 2021.1.0, SID doesn't generate " +
            "EnrolmentRecordCreationEvent anymore during an enrolment and the event is used only for the down-sync " +
            "(transformed to a subject). So this logic to batch the 'not-related with a session' events is unnecessary " +
            "from 2021.1.0, but it's still required during the migration from previous app versions since the DB may " +
            "still have EnrolmentRecordCreationEvents in the db to upload. Once all devices are on 2021.1.0, this logic" +
            "can be deleted."
    )
    private suspend fun createBatchesForEventsNotInSessions(query: LocalEventQuery): List<Batch> {
        val events = eventLocalDataSource.load(query.fromDomainToDb()).filter { it.labels.sessionId == null }.toList()
        Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Record events to upload")

        return events.chunked(SESSION_BATCH_SIZE).map { Batch(it.toMutableList()) }
    }

    private suspend fun createBatchesForEventsInSessions(query: LocalEventQuery): List<Batch> {

        // We don't upload unsigned sessions because the back-end would reject them.
        val sessionsToUpload =
            merge(loadCloseSessions(query), closeAndLoadOldOpenSessions(query)).filter { it.labels.projectId != PROJECT_ID_FOR_NOT_SIGNED_IN }

        Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Sessions to upload ${sessionsToUpload.count()}")

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

    private suspend fun closeAndLoadOldOpenSessions(query: LocalEventQuery): Flow<Event> {
        val queryForOldOpenSessions = query.copy(
            type = SESSION_CAPTURE,
            endTime = OPEN_SESSION_FLAG,
            startTime = LongRange(0, timeHelper.now() - GRACE_PERIOD),
            projectId = query.projectId).fromDomainToDb()

        Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Loading old open sessions for query $queryForOldOpenSessions")

        val events = eventLocalDataSource.load(queryForOldOpenSessions)
        return if (events.count() > 0) {
            closeSessionsAndAddArtificialTerminationEvent(events, TIMED_OUT)
            eventLocalDataSource.load(queryForOldOpenSessions)
        } else {
            events
        }

    }

    override suspend fun getCurrentCaptureSessionEvent(): SessionCaptureEvent =
        reportExceptionIfNeeded {
            loadOpenSessions().firstOrNull() ?: createSession()
        }


    override suspend fun loadEvents(sessionId: String): Flow<Event> =
        reportExceptionIfNeeded {
            eventLocalDataSource.load(DbLocalEventQuery(sessionId = sessionId))
        }

    private suspend fun closeSessionsAndAddArtificialTerminationEvent(openSessions: Flow<Event>,
                                                                      reason: ArtificialTerminationPayload.Reason) {
        openSessions.collect { sessionEvent ->
            val artificialTerminationEvent = ArtificialTerminationEvent(
                timeHelper.now(),
                reason
            )
            artificialTerminationEvent.labels = artificialTerminationEvent.labels.appendSessionId(sessionEvent.id)

            addEvent(artificialTerminationEvent)

            closeSession(sessionEvent)
            addEvent(sessionEvent)
        }
    }

    private fun closeSession(session: Event) {
        (session as SessionCaptureEvent).payload.endedAt = timeHelper.now()
    }

    private suspend fun loadOpenSessions(query: LocalEventQuery = LocalEventQuery()) =
        eventLocalDataSource.load(query.appendQueryForOpenSession().fromDomainToDb()).map { it as SessionCaptureEvent }

    private suspend fun loadCloseSessions(query: LocalEventQuery = LocalEventQuery()) =
        eventLocalDataSource.load(query.appendQueryForCloseSession().fromDomainToDb()).map { it as SessionCaptureEvent }

    private fun LocalEventQuery.appendQueryForOpenSession() =
        this.copy(type = SESSION_CAPTURE, endTime = OPEN_SESSION_FLAG)

    private fun LocalEventQuery.appendQueryForCloseSession() =
        this.copy(type = SESSION_CAPTURE, endTime = LongRange(1, Long.MAX_VALUE))

    private fun EventLabels.appendLabelsForAllSessionEvents() =
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
            Timber.d(t)
            crashReportManager.logExceptionOrSafeException(t)
            throw t
        }

    private fun checkQueryProjectIsIsSignedIn(projectId: String?) {
        if (projectId != loginInfoManager.getSignedInProjectIdOrEmpty()) {
            throw Throwable("Only events for the signed in project can be uploaded").also {
                crashReportManager.logException(it)
            }
        }
    }

    @VisibleForTesting
    data class Batch(val events: MutableList<Event>)
}

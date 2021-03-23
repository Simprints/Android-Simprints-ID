package com.simprints.id.data.db.event

import android.os.Build
import android.os.Build.VERSION
import androidx.annotation.VisibleForTesting
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.event.domain.EventCount
import com.simprints.id.data.db.event.domain.models.*
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.domain.models.session.DatabaseInfo
import com.simprints.id.data.db.event.domain.models.session.Device
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.event.domain.validators.SessionEventValidatorsFactory
import com.simprints.id.data.db.event.local.EventLocalDataSource
import com.simprints.id.data.db.event.remote.EventRemoteDataSource
import com.simprints.id.data.db.events_sync.down.domain.RemoteEventQuery
import com.simprints.id.data.db.events_sync.down.domain.fromDomainToApi
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modality.toMode
import com.simprints.id.exceptions.safe.sync.TryToUploadEventsForNotSignedProject
import com.simprints.id.services.sync.events.common.SYNC_LOG_TAG
import com.simprints.id.tools.extensions.isClientAndCloudIntegrationIssue
import com.simprints.id.tools.ignoreException
import com.simprints.id.tools.time.TimeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.*

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

    override suspend fun createSession(): SessionCaptureEvent {
        return reportExceptionIfNeeded {
            val sessionCount = eventLocalDataSource.count(type = SESSION_CAPTURE)
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

            closeSessionsAndAddArtificialTerminationEvent(NEW_SESSION)
            saveEvent(sessionCaptureEvent)
            sessionDataCache.currentSession = sessionCaptureEvent
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
                    val eventsInSession = eventLocalDataSource.loadAllFromSession(sessionId = session.id).toList()
                    validators.forEach {
                        it.validate(eventsInSession, event)
                    }

                    event.labels = event.labels.copy(sessionId = session.id, projectId = session.payload.projectId)
                    saveEvent(event)
                }
            }
        }
    }

    override suspend fun saveEvent(event: Event) {
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

    override suspend fun localCount(projectId: String): Int =
        eventLocalDataSource.count(projectId = projectId)

    override suspend fun localCount(projectId: String, type: EventType): Int =
        eventLocalDataSource.count(projectId = projectId, type = type)

    override suspend fun countEventsToDownload(query: RemoteEventQuery): List<EventCount> =
        eventRemoteDataSource.count(query.fromDomainToApi())

    override suspend fun downloadEvents(scope: CoroutineScope, query: RemoteEventQuery): ReceiveChannel<Event> =
        eventRemoteDataSource.getEvents(query.fromDomainToApi(), scope)

    override suspend fun uploadEvents(projectId: String): Flow<Int> = flow {
        Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Uploading")

        if (projectId != loginInfoManager.getSignedInProjectIdOrEmpty()) {
            throw TryToUploadEventsForNotSignedProject("Only events for the signed in project can be uploaded").also {
                crashReportManager.logException(it)
            }
        }

        val batches = createBatches(projectId)
        Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Uploading batches ${batches.size}")

        batches.forEach { batch ->
            val events = batch.events
            Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Uploading ${events.size} events in a batch")

            try {
                uploadEvents(events, projectId)
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

    override suspend fun deleteSessionEvents(sessionId: String) {
        try {
            eventLocalDataSource.deleteAllFromSession(sessionId = sessionId)
        } catch (t: Throwable) {
            Timber.e("Error deleting session from DB")
            Timber.e(t)
            crashReportManager.logException(t)
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
            eventLocalDataSource.delete(id = it)
        }
    }

    @VisibleForTesting
    suspend fun createBatches(projectId: String): List<Batch> {
        Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Creating batches")
        return createBatchesForEventsNotInSessions(projectId) + createBatchesForEventsInSessions(projectId)
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
    private suspend fun createBatchesForEventsNotInSessions(projectId: String): List<Batch> {
        val events = eventLocalDataSource.loadAllFromProject(projectId = projectId).filter { it.labels.sessionId == null }.toList()
        Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Record events to upload")

        return events.chunked(SESSION_BATCH_SIZE).map { Batch(it.toMutableList()) }
    }

    private suspend fun createBatchesForEventsInSessions(projectId: String): List<Batch> {

        // We don't upload unsigned sessions because the back-end would reject them.
        val sessionsToUpload = loadSessions(true)
            .filter { it.labels.projectId == projectId }
            .filter { it.labels.projectId != PROJECT_ID_FOR_NOT_SIGNED_IN }

        Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Sessions to upload ${sessionsToUpload.count()}")

        return sessionsToUpload.fold(mutableListOf()) { batches, session ->
            val lastBatch = batches.lastOrNull()
            val eventsCountInTheLastBatch = lastBatch?.events?.size ?: 0
            val eventsToUpload = eventLocalDataSource.loadAllFromSession(sessionId = session.id).toList()

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

    override suspend fun getCurrentCaptureSessionEvent(): SessionCaptureEvent =
        reportExceptionIfNeeded {
            loadSessions(false).firstOrNull() ?: createSession()
        }

    override suspend fun loadEventsFromSession(sessionId: String): Flow<Event> =
        reportExceptionIfNeeded {
            eventLocalDataSource.loadAllFromSession(sessionId = sessionId)
        }

    private suspend fun closeSessionsAndAddArtificialTerminationEvent(reason: ArtificialTerminationPayload.Reason) {
        loadSessions(false).collect { sessionEvent ->
            val artificialTerminationEvent = ArtificialTerminationEvent(
                timeHelper.now(),
                reason
            ).apply { labels = labels.appendSessionId(sessionEvent.id) }

            saveEvent(artificialTerminationEvent)

            sessionEvent.payload.endedAt = timeHelper.now()
            sessionEvent.payload.sessionIsClosed = true

            saveEvent(sessionEvent)
        }
    }

    private suspend fun loadSessions(isClosed: Boolean): Flow<SessionCaptureEvent> {
        return eventLocalDataSource.loadAllFromType(type = SESSION_CAPTURE)
            .map { it as SessionCaptureEvent }
            .filter { it.payload.sessionIsClosed == isClosed }
    }

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

    @VisibleForTesting
    data class Batch(val events: MutableList<Event>)
}

package com.simprints.id.data.db.event

import android.os.Build
import android.os.Build.VERSION
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag.SYNC
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger.DATABASE
import com.simprints.id.data.db.event.domain.EventCount
import com.simprints.id.data.db.event.domain.models.*
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.domain.models.session.DatabaseInfo
import com.simprints.id.data.db.event.domain.models.session.Device
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.event.domain.validators.SessionEventValidatorsFactory
import com.simprints.id.data.db.event.local.EventLocalDataSource
import com.simprints.id.data.db.event.local.SessionDataCache
import com.simprints.id.data.db.event.remote.EventRemoteDataSource
import com.simprints.id.data.db.events_sync.down.domain.RemoteEventQuery
import com.simprints.id.data.db.events_sync.down.domain.fromDomainToApi
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modality.toMode
import com.simprints.id.exceptions.safe.sync.TryToUploadEventsForNotSignedProject
import com.simprints.id.services.sync.events.common.SYNC_LOG_TAG
import com.simprints.id.tools.extensions.isClientAndCloudIntegrationIssue
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
    override val libSimprintsVersionName: String,
    private val sessionDataCache: SessionDataCache
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
        closeAllSessions(NEW_SESSION)

        return reportException {
            val sessionCount = eventLocalDataSource.count(type = SESSION_CAPTURE)
            val sessionCaptureEvent = SessionCaptureEvent(
                id = UUID.randomUUID().toString(),
                projectId = currentProject,
                createdAt = timeHelper.now(),
                modalities = preferencesManager.modalities.map { it.toMode() },
                appVersionName = appVersionName,
                libVersionName = libSimprintsVersionName,
                language = preferencesManager.language,
                device = Device(
                    VERSION.SDK_INT.toString(),
                    Build.MANUFACTURER + "_" + Build.MODEL,
                    deviceId
                ),
                databaseInfo = DatabaseInfo(sessionCount)
            )

            saveEvent(sessionCaptureEvent, sessionCaptureEvent)
            sessionDataCache.eventCache[sessionCaptureEvent.id] = sessionCaptureEvent
            sessionCaptureEvent
        }
    }

    override suspend fun addOrUpdateEvent(event: Event) {
        val startTime = System.currentTimeMillis()

        reportException {
            val session = getCurrentCaptureSessionEvent()

            validators.forEach {
                it.validate(sessionDataCache.eventCache.values.toList(), event)
            }

            sessionDataCache.eventCache[event.id] = event

            saveEvent(event, session)
        }

        val endTime = System.currentTimeMillis()
        Timber.v("Save event: ${event.type} = ${endTime - startTime}ms")
    }

    private suspend fun saveEvent(event: Event, session: SessionCaptureEvent) {
        checkAndUpdateLabels(event, session)
        eventLocalDataSource.insertOrUpdate(event)
    }

    private fun checkAndUpdateLabels(event: Event, session: SessionCaptureEvent) {
        event.labels = event.labels.copy(
            sessionId = session.id,
            projectId = session.payload.projectId
        )

        if (event.type.isNotASubjectEvent()) {
            event.labels = event.labels.copy(deviceId = deviceId)
        }
    }

    override suspend fun localCount(projectId: String): Int =
        eventLocalDataSource.count(projectId = projectId)

    override suspend fun localCount(projectId: String, type: EventType): Int =
        eventLocalDataSource.count(projectId = projectId, type = type)

    override suspend fun countEventsToDownload(query: RemoteEventQuery): List<EventCount> =
        eventRemoteDataSource.count(query.fromDomainToApi())

    override suspend fun downloadEvents(
        scope: CoroutineScope,
        query: RemoteEventQuery
    ): ReceiveChannel<Event> =
        eventRemoteDataSource.getEvents(query.fromDomainToApi(), scope)

    override suspend fun uploadEvents(projectId: String): Flow<Int> = flow {
        Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Uploading")

        if (projectId != loginInfoManager.getSignedInProjectIdOrEmpty()) {
            throw TryToUploadEventsForNotSignedProject("Only events for the signed in project can be uploaded").also {
                crashReportManager.logException(it)
            }
        }

        eventLocalDataSource.loadAllClosedSessionIds(projectId).forEach { sessionId ->
            // The events will include the SessionCaptureEvent event
            Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Uploading session $sessionId")
            eventLocalDataSource.loadAllFromSession(sessionId).let {
                attemptEventUpload(it, projectId)
                this.emit(it.size)
            }
        }

        Timber.tag(SYNC_LOG_TAG).d("[EVENT_REPO] Uploading abandoned events")
        eventLocalDataSource.loadAbandonedEvents(projectId).let {
            crashReportManager.logMessageForCrashReport(SYNC, DATABASE, message = "Abandoned Events: ${it.size}")
            attemptEventUpload(it, projectId)
            this.emit(it.size)
        }
    }


    override suspend fun deleteSessionEvents(sessionId: String) {
        reportException {
            eventLocalDataSource.deleteAllFromSession(sessionId = sessionId)
        }
    }

    private suspend fun attemptEventUpload(events: List<Event>, projectId: String) {
        try {
            uploadEvents(events, projectId)
            deleteEventsFromDb(events.map { it.id })
        } catch (t: Throwable) {
            Timber.w(t)
            if (t.isClientAndCloudIntegrationIssue()) {
                crashReportManager.logException(t)
                // We do not delete subject events (pokedex) since they are important.
                deleteEventsFromDb(events.filter { it.type.isNotASubjectEvent() }.map { it.id })
            }
        }
    }

    private suspend fun uploadEvents(events: List<Event>, projectId: String) {
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

    override suspend fun getCurrentCaptureSessionEvent(): SessionCaptureEvent = reportException {
        sessionDataCache.eventCache.values.toList().filterIsInstance<SessionCaptureEvent>()
            .firstOrNull()
            ?: loadSessions(false).firstOrNull()?.also { session ->
                loadEventsIntoCache(session.id)
            }
            ?: createSession()
    }

    override suspend fun getEventsFromSession(sessionId: String): Flow<Event> =
        reportException {
            if (sessionDataCache.eventCache.isEmpty()) {
                loadEventsIntoCache(sessionId)
            }

            return@reportException flow {
                sessionDataCache.eventCache.values.toList().forEach { emit(it) }
            }
        }

    /**
     * The reason is only used when we want to create an [ArtificialTerminationEvent].
     * If the session is closing for normal reasons (i.e. came to a normal end), then it should be `null`.
     */
    private suspend fun closeAllSessions(reason: Reason) {
        sessionDataCache.eventCache.clear()
        loadSessions(false).collect { closeSession(it, reason) }
    }

    override suspend fun closeCurrentSession(reason: Reason?) {
        closeSession(getCurrentCaptureSessionEvent(), reason)
        sessionDataCache.eventCache.clear()
    }

    /**
     * The reason is only used when we want to create an [ArtificialTerminationEvent].
     * If the session is closing for normal reasons (i.e. came to a normal end), then it should be `null`.
     */
    private suspend fun closeSession(sessionCaptureEvent: SessionCaptureEvent, reason: Reason?) {
        if (reason != null) {
            saveEvent(ArtificialTerminationEvent(timeHelper.now(), reason), sessionCaptureEvent)
        }

        sessionCaptureEvent.payload.endedAt = timeHelper.now()
        sessionCaptureEvent.payload.sessionIsClosed = true

        saveEvent(sessionCaptureEvent, sessionCaptureEvent)
    }

    private suspend fun loadSessions(isClosed: Boolean): Flow<SessionCaptureEvent> {
        return eventLocalDataSource.loadAllSessions(isClosed).map { it as SessionCaptureEvent }
    }

    private suspend fun loadEventsIntoCache(sessionId: String) {
        eventLocalDataSource.loadAllFromSession(sessionId).forEach {
            sessionDataCache.eventCache[it.id] = it
        }
    }

    private suspend fun <T> reportException(block: suspend () -> T): T =
        try {
            block()
        } catch (t: Throwable) {
            Timber.d(t)
            crashReportManager.logExceptionOrSafeException(t)
            throw t
        }

}

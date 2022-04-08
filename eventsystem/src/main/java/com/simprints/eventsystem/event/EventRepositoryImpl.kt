package com.simprints.eventsystem.event

import android.os.Build
import android.os.Build.VERSION
import com.simprints.core.analytics.CrashReportTag
import com.simprints.core.domain.modality.Modes
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.domain.EventCount
import com.simprints.eventsystem.event.domain.models.ArtificialTerminationEvent
import com.simprints.eventsystem.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason
import com.simprints.eventsystem.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.eventsystem.event.domain.models.isNotASubjectEvent
import com.simprints.eventsystem.event.domain.models.session.DatabaseInfo
import com.simprints.eventsystem.event.domain.models.session.Device
import com.simprints.eventsystem.event.domain.models.session.SessionCaptureEvent
import com.simprints.eventsystem.event.domain.validators.SessionEventValidatorsFactory
import com.simprints.eventsystem.event.local.EventLocalDataSource
import com.simprints.eventsystem.event.local.SessionDataCache
import com.simprints.eventsystem.event.remote.EventRemoteDataSource
import com.simprints.eventsystem.events_sync.down.domain.RemoteEventQuery
import com.simprints.eventsystem.events_sync.down.domain.fromDomainToApi
import com.simprints.eventsystem.exceptions.TryToUploadEventsForNotSignedProject
import com.simprints.eventsystem.exceptions.validator.DuplicateGuidSelectEventValidatorException
import com.simprints.logging.Simber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.util.UUID

open class EventRepositoryImpl(
    private val deviceId: String,
    private val appVersionName: String,
    private val loginInfoManager: LoginInfoManager,
    private val eventLocalDataSource: EventLocalDataSource,
    private val eventRemoteDataSource: EventRemoteDataSource,
    private val timeHelper: TimeHelper,
    validatorsFactory: SessionEventValidatorsFactory,
    override val libSimprintsVersionName: String,
    private val sessionDataCache: SessionDataCache,
    private val language: String,
    private val modalities: List<Modes>
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
                modalities = modalities,
                appVersionName = appVersionName,
                libVersionName = libSimprintsVersionName,
                language = language,
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

            val currentEvents = sessionDataCache.eventCache.values.toList()
            validators.forEach {
                it.validate(currentEvents, event)
            }

            sessionDataCache.eventCache[event.id] = event

            saveEvent(event, session)
        }

        val endTime = System.currentTimeMillis()
        Simber.v("Save event: ${event.type} = ${endTime - startTime}ms")
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

    /**
     * Note that only the IDs of the SessionCapture events of closed sessions are all held in
     * memory at once. Events are loaded in memory and uploaded session by session, ensuring the
     * memory usage stays low. It means that we do not exploit connectivity as aggressively as
     * possible (we could have a more complex system that always pre-fetches the next batch of
     * events while we upload the current one), but given the relatively small amount of data to
     * upload, and how critical this system is, we are happy to trade off speed for reliability
     * (through simplicity and low resource usage)
     */
    override suspend fun uploadEvents(projectId: String): Flow<Int> = flow {
        Simber.tag("SYNC").d("[EVENT_REPO] Uploading")

        if (projectId != loginInfoManager.getSignedInProjectIdOrEmpty()) {
            throw TryToUploadEventsForNotSignedProject("Only events for the signed in project can be uploaded").also {
                Simber.e(it)
            }
        }

        eventLocalDataSource.loadAllClosedSessionIds(projectId).forEach { sessionId ->
            // The events will include the SessionCaptureEvent event
            Simber.tag("SYNC").d("[EVENT_REPO] Uploading session $sessionId")
            eventLocalDataSource.loadAllFromSession(sessionId).let {
                attemptEventUpload(it, projectId)
                this.emit(it.size)
            }
        }

        Simber.tag("SYNC").d("[EVENT_REPO] Uploading old SubjectCreation events")
        eventLocalDataSource.loadOldSubjectCreationEvents(projectId).let {
            Simber.tag(CrashReportTag.SYNC.name).i("Old SubjectCreation: ${it.size}")
            if (it.isNotEmpty()) {
                attemptEventUpload(it, projectId)
                this.emit(it.size)
            }
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
            Simber.w(t)
            // We don't need to report http exceptions as cloud logs all of them.
            if (t !is HttpException) {
                Simber.e(t)
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
        Simber.tag("SYNC").d("[EVENT_REPO] Deleting ${eventsIds.count()} events")
        eventLocalDataSource.delete(eventsIds)
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
            // prevent crashlytics logging of duplicate guid-selection
            if (t is DuplicateGuidSelectEventValidatorException) Simber.d(t)
            else Simber.e(t)

            throw t
        }

}

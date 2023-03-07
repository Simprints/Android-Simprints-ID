package com.simprints.infra.events

import android.os.Build
import android.os.Build.VERSION
import com.simprints.core.DeviceID
import com.simprints.core.LibSimprintsVersionName
import com.simprints.core.PackageVersionName
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.events.event.domain.models.*
import com.simprints.infra.events.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason
import com.simprints.infra.events.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.infra.events.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.infra.events.event.domain.models.session.DatabaseInfo
import com.simprints.infra.events.event.domain.models.session.Device
import com.simprints.infra.events.event.domain.models.session.SessionCaptureEvent
import com.simprints.infra.events.domain.validators.SessionEventValidatorsFactory
import com.simprints.infra.events.event.local.EventLocalDataSource
import com.simprints.infra.events.event.local.SessionDataCache
import com.simprints.infra.events.exceptions.validator.DuplicateGuidSelectEventValidatorException
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.LoginManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal open class EventRepositoryImpl @Inject constructor(
    @DeviceID private val deviceId: String,
    @PackageVersionName private val appVersionName: String,
    @LibSimprintsVersionName override val libSimprintsVersionName: String,
    private val loginManager: LoginManager,
    private val eventLocalDataSource: EventLocalDataSource,
    private val timeHelper: TimeHelper,
    validatorsFactory: SessionEventValidatorsFactory,
    private val sessionDataCache: SessionDataCache,
    private val configManager: ConfigManager,
) : EventRepository {

    companion object {
        const val PROJECT_ID_FOR_NOT_SIGNED_IN = "NOT_SIGNED_IN"
    }

    private val validators = validatorsFactory.build()

    private val currentProject: String
        get() = loginManager.getSignedInProjectIdOrEmpty().ifEmpty {
            PROJECT_ID_FOR_NOT_SIGNED_IN
        }

    override suspend fun createSession(): SessionCaptureEvent {
        closeAllSessions(NEW_SESSION)

        return reportException {
            val projectConfiguration = configManager.getProjectConfiguration()
            val deviceConfiguration = configManager.getDeviceConfiguration()
            val sessionCount = eventLocalDataSource.count(type = SESSION_CAPTURE)
            val sessionCaptureEvent = SessionCaptureEvent(
                id = UUID.randomUUID().toString(),
                projectId = currentProject,
                createdAt = timeHelper.now(),
                modalities = projectConfiguration.general.modalities,
                appVersionName = appVersionName,
                libVersionName = libSimprintsVersionName,
                language = deviceConfiguration.language,
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
            projectId = session.payload.projectId,
            deviceId = deviceId,
        )
    }

    override suspend fun deleteSessionEvents(sessionId: String) = reportException {
        eventLocalDataSource.deleteAllFromSession(sessionId = sessionId)
    }

    override suspend fun getCurrentCaptureSessionEvent(): SessionCaptureEvent = reportException {
        cachedCaptureSessionEvent()
            ?: localCaptureSessionEvent()
            ?: createSession()
    }

    private fun cachedCaptureSessionEvent() =
        sessionDataCache.eventCache.values.toList().filterIsInstance<SessionCaptureEvent>()
            .maxByOrNull {
                it.payload.createdAt
            }

    private suspend fun localCaptureSessionEvent() =
        loadOpenedSessions().firstOrNull()?.also { session ->
            loadEventsIntoCache(session.id)
        }


    override suspend fun observeEventsFromSession(sessionId: String): Flow<Event> =
        reportException {
            if (sessionDataCache.eventCache.isEmpty()) {
                loadEventsIntoCache(sessionId)
            }

            return@reportException flow {
                sessionDataCache.eventCache.values.toList().forEach { emit(it) }
            }
        }

    override suspend fun getAllClosedSessionIds(projectId: String): List<String> =
        eventLocalDataSource.loadAllClosedSessionIds(projectId)

    override suspend fun getEventsFromSession(sessionId: String): List<Event> =
        eventLocalDataSource.loadAllFromSession(sessionId)

    override suspend fun getEventsJsonFromSession(sessionId: String): List<String> =
        eventLocalDataSource.loadAllEventJsonFromSession(sessionId)

    override suspend fun observeEventCount(projectId: String, type: EventType?): Flow<Int> =
        if (type != null) eventLocalDataSource.observeCount(projectId, type)
        else eventLocalDataSource.observeCount(projectId)

    /**
     * The reason is only used when we want to create an [ArtificialTerminationEvent].
     * If the session is closing for normal reasons (i.e. came to a normal end), then it should be `null`.
     */
    private suspend fun closeAllSessions(reason: Reason) {
        sessionDataCache.eventCache.clear()
        loadOpenedSessions().collect { closeSession(it, reason) }
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

    private suspend fun loadOpenedSessions(): Flow<SessionCaptureEvent> {
        return eventLocalDataSource.loadOpenedSessions().map { it as SessionCaptureEvent }
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

    override suspend fun removeLocationDataFromCurrentSession() {
        val currentCaptureSession = getCurrentCaptureSessionEvent()
        if (currentCaptureSession.payload.location != null) {
            currentCaptureSession.payload.location = null
            addOrUpdateEvent(currentCaptureSession)
        }
    }

    override suspend fun loadAll(): Flow<Event> = eventLocalDataSource.loadAll()

    override suspend fun delete(eventIds: List<String>) =
        eventLocalDataSource.delete(eventIds)

    override suspend fun deleteAll() = eventLocalDataSource.deleteAll()
}

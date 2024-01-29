package com.simprints.infra.events

import android.os.Build
import android.os.Build.VERSION
import com.simprints.core.DeviceID
import com.simprints.core.LibSimprintsVersionName
import com.simprints.core.PackageVersionName
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.events.domain.validators.SessionEventValidatorsFactory
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.session.DatabaseInfo
import com.simprints.infra.events.event.domain.models.session.Device
import com.simprints.infra.events.event.domain.models.session.SessionEndCause
import com.simprints.infra.events.event.domain.models.session.SessionScope
import com.simprints.infra.events.event.domain.models.session.SessionScopePayload
import com.simprints.infra.events.event.local.EventLocalDataSource
import com.simprints.infra.events.event.local.SessionDataCache
import com.simprints.infra.events.exceptions.validator.DuplicateGuidSelectEventValidatorException
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal open class EventRepositoryImpl @Inject constructor(
    @DeviceID private val deviceId: String,
    @PackageVersionName private val appVersionName: String,
    @LibSimprintsVersionName override val libSimprintsVersionName: String,
    private val authStore: AuthStore,
    private val eventLocalDataSource: EventLocalDataSource,
    private val timeHelper: TimeHelper,
    validatorsFactory: SessionEventValidatorsFactory,
    private val sessionDataCache: SessionDataCache,
    private val configRepository: ConfigRepository,
) : EventRepository {

    companion object {

        const val PROJECT_ID_FOR_NOT_SIGNED_IN = "NOT_SIGNED_IN"
    }

    private val validators = validatorsFactory.build()

    private val currentProject: String
        get() = authStore.signedInProjectId.ifEmpty {
            PROJECT_ID_FOR_NOT_SIGNED_IN
        }

    override suspend fun createSession(): SessionScope {
        closeAllSessions(SessionEndCause.NEW_SESSION)

        return reportException {
            val projectConfiguration = configRepository.getProjectConfiguration()
            val deviceConfiguration = configRepository.getDeviceConfiguration()
            val sessionCount = eventLocalDataSource.countSessions()

            val sessionScope = SessionScope(
                id = UUID.randomUUID().toString(),
                projectId = currentProject,
                createdAt = timeHelper.now(),
                endedAt = null,
                payload = SessionScopePayload(
                    sidVersion = appVersionName,
                    libSimprintsVersion = libSimprintsVersionName,
                    language = deviceConfiguration.language,
                    modalities = projectConfiguration.general.modalities,
                    device = Device(
                        VERSION.SDK_INT.toString(),
                        Build.MANUFACTURER + "_" + Build.MODEL,
                        deviceId
                    ),
                    databaseInfo = DatabaseInfo(sessionCount),
                    projectConfigurationUpdatedAt = projectConfiguration.updatedAt,
                )
            )

            sessionDataCache.sessionScope = sessionScope
            eventLocalDataSource.saveSessionScope(sessionScope)
            sessionScope
        }
    }

    /**
     * If the session is closing for normal reasons (i.e. came to a normal end), then it should be `null`.
     */
    private suspend fun closeAllSessions(reason: SessionEndCause?) {
        sessionDataCache.eventCache.clear()
        eventLocalDataSource.loadOpenedSessions().forEach { closeSession(it, reason) }
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

    override suspend fun hasOpenSession(): Boolean {
        val session = cachedSessionScope() ?: localSessionScopes()
        return session != null
    }

    override suspend fun closeCurrentSession(reason: SessionEndCause?) {
        closeSession(getCurrentSessionScope(), reason)
        sessionDataCache.eventCache.clear()
    }

    override suspend fun getCurrentSessionScope(): SessionScope = reportException {
        cachedSessionScope()
            ?: localSessionScopes()
            ?: createSession()
    }

    override suspend fun getAllClosedSessions(projectId: String): List<SessionScope> =
        eventLocalDataSource.loadClosedSessions(projectId)

    override suspend fun saveSessionScope(sessionScope: SessionScope) {
        if (sessionScope.id == sessionDataCache.sessionScope?.id) {
            sessionDataCache.sessionScope = sessionScope
        }
        eventLocalDataSource.saveSessionScope(sessionScope)
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

    private suspend fun loadEventsIntoCache(sessionId: String) {
        eventLocalDataSource.loadEventsInSession(sessionId).forEach {
            sessionDataCache.eventCache[it.id] = it
        }
    }

    override suspend fun getEventsFromSession(sessionId: String): List<Event> =
        eventLocalDataSource.loadEventsInSession(sessionId)

    override suspend fun getEventsJsonFromSession(sessionId: String): List<String> =
        eventLocalDataSource.loadEventJsonInSession(sessionId)

    override suspend fun observeEventCount(projectId: String, type: EventType?): Flow<Int> =
        if (type != null) eventLocalDataSource.observeEventCount(projectId, type)
        else eventLocalDataSource.observeEventCount(projectId)

    override suspend fun loadAll(): Flow<Event> = eventLocalDataSource.loadAllEvents()

    override suspend fun addOrUpdateEvent(event: Event) {
        val startTime = System.currentTimeMillis()

        reportException {
            val session = getCurrentSessionScope()

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

    private suspend fun saveEvent(event: Event, session: SessionScope) {
        checkAndUpdateLabels(event, session)
        eventLocalDataSource.saveEvent(event)
    }

    private fun checkAndUpdateLabels(event: Event, session: SessionScope) {
        event.labels = event.labels.copy(
            sessionId = session.id,
            projectId = session.projectId,
            deviceId = deviceId,
        )
    }

    override suspend fun removeLocationDataFromCurrentSession() {
        val sessionScope = getCurrentSessionScope()
        if (sessionScope.payload.location != null) {
            val updatedSessionScope = sessionScope.copy(
                payload = sessionScope.payload.copy(location = null)
            )
            saveSessionScope(updatedSessionScope)
        }
    }

    override suspend fun deleteSession(sessionId: String) = reportException {
        eventLocalDataSource.deleteSession(sessionId = sessionId)
        eventLocalDataSource.deleteEventsInSession(sessionId = sessionId)
    }

    private fun cachedSessionScope() = sessionDataCache.sessionScope

    private suspend fun localSessionScopes() =
        eventLocalDataSource.loadOpenedSessions()
            .firstOrNull()
            ?.also { session -> loadEventsIntoCache(session.id) }

    private suspend fun closeSession(sessionScope: SessionScope, reason: SessionEndCause?) {
        val maxTimestamp = eventLocalDataSource.loadEventsInSession(sessionScope.id)
            .takeIf { it.isNotEmpty() }
            ?.maxOf { event ->
                event.payload.let { payload ->
                    payload.endedAt.takeIf { it > 0 } ?: payload.createdAt
                }
            }
            ?: timeHelper.now()

        val updatedSessionScope = sessionScope.copy(
            endedAt = maxTimestamp,
            payload = sessionScope.payload.copy(
                endCause = reason ?: SessionEndCause.WORKFLOW_ENDED
            )
        )
        saveSessionScope(updatedSessionScope)
    }

    override suspend fun deleteAll() = eventLocalDataSource.deleteAll()
}

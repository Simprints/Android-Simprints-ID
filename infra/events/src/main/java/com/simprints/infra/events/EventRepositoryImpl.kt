package com.simprints.infra.events

import android.os.Build
import android.os.Build.VERSION
import com.simprints.core.DeviceID
import com.simprints.core.LibSimprintsVersionName
import com.simprints.core.PackageVersionName
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.scope.DatabaseInfo
import com.simprints.infra.events.event.domain.models.scope.Device
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.scope.EventScopeEndCause
import com.simprints.infra.events.event.domain.models.scope.EventScopePayload
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.events.event.domain.validators.SessionEventValidatorsFactory
import com.simprints.infra.events.event.local.EventLocalDataSource
import com.simprints.infra.events.exceptions.validator.DuplicateGuidSelectEventValidatorException
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.measureTime

@Singleton
internal open class EventRepositoryImpl @Inject constructor(
    @DeviceID private val deviceId: String,
    @PackageVersionName private val appVersionName: String,
    @LibSimprintsVersionName override val libSimprintsVersionName: String,
    private val authStore: AuthStore,
    private val eventLocalDataSource: EventLocalDataSource,
    private val timeHelper: TimeHelper,
    validatorsFactory: SessionEventValidatorsFactory,
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

    override suspend fun createEventScope(type: EventScopeType, scopeId: String?): EventScope {
        val eventScope = reportException {
            val projectConfiguration = configRepository.getProjectConfiguration()
            val deviceConfiguration = configRepository.getDeviceConfiguration()
            val sessionCount = eventLocalDataSource.countEventScopes(EventScopeType.SESSION)

            EventScope(
                id = scopeId ?: UUID.randomUUID().toString(),
                projectId = currentProject,
                type = type,
                createdAt = timeHelper.now(),
                endedAt = null,
                payload = EventScopePayload(
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
                    projectConfigurationId = projectConfiguration.id,
                )
            )
        }

        eventLocalDataSource.saveEventScope(eventScope)
        return eventScope
    }

    override suspend fun getEventScope(downSyncEventScopeId: String): EventScope? =
        eventLocalDataSource.loadEventScope(downSyncEventScopeId)

    override suspend fun closeEventScope(eventScope: EventScope, reason: EventScopeEndCause?) {
        if (eventScope.projectId == PROJECT_ID_FOR_NOT_SIGNED_IN) {
            eventLocalDataSource.deleteEventScope(scopeId = eventScope.id)
            eventLocalDataSource.deleteEventsInScope(scopeId = eventScope.id)
            return
        }

        val events = eventLocalDataSource.loadEventsInScope(eventScope.id)
        if (events.isEmpty()) {
            eventLocalDataSource.deleteEventScope(scopeId = eventScope.id)
            return
        }

        val maxTimestamp = eventLocalDataSource.loadEventsInScope(eventScope.id)
            .maxOf { event -> event.payload.let { it.endedAt ?: it.createdAt } }

        val updatedSessionScope = eventScope.copy(
            endedAt = maxTimestamp,
            payload = eventScope.payload.copy(
                endCause = reason ?: EventScopeEndCause.WORKFLOW_ENDED
            )
        )
        saveEventScope(updatedSessionScope)
    }

    override suspend fun closeEventScope(eventScopeId: String, reason: EventScopeEndCause?) {
        getEventScope(eventScopeId)?.let { closeEventScope(it, reason) }
    }

    override suspend fun closeAllOpenScopes(type: EventScopeType, reason: EventScopeEndCause?) {
        eventLocalDataSource.loadOpenedScopes(type)
            .forEach { eventScope -> closeEventScope(eventScope, reason) }
    }

    override suspend fun saveEventScope(eventScope: EventScope) {
        eventLocalDataSource.saveEventScope(eventScope)
    }

    override suspend fun getOpenEventScopes(type: EventScopeType): List<EventScope> =
        eventLocalDataSource.loadOpenedScopes(type)

    override suspend fun getClosedEventScopes(type: EventScopeType, limit: Int): List<EventScope> =
        eventLocalDataSource.loadClosedScopes(type, limit)

    override suspend fun getClosedEventScopesCount(type: EventScopeType): Int =
        eventLocalDataSource.countClosedEventScopes(type)

    override suspend fun deleteEventScope(scopeId: String) = reportException {
        eventLocalDataSource.deleteEventScope(scopeId = scopeId)
        eventLocalDataSource.deleteEventsInScope(scopeId = scopeId)
    }

    override suspend fun deleteEventScopes(scopeIds: List<String>) = reportException {
        eventLocalDataSource.deleteEventScopes(scopeIds = scopeIds)
        eventLocalDataSource.deleteEventsInScopes(scopeIds = scopeIds)
    }

    override suspend fun getEventsFromScope(scopeId: String): List<Event> =
        eventLocalDataSource.loadEventsInScope(scopeId)

    override suspend fun getEventsJsonFromScope(scopeId: String): List<String> =
        eventLocalDataSource.loadEventJsonInScope(scopeId)

    override suspend fun getAllEvents(): Flow<Event> = eventLocalDataSource.loadAllEvents()

    override suspend fun observeEventCount(type: EventType?): Flow<Int> =
        if (type != null) eventLocalDataSource.observeEventCount(type)
        else eventLocalDataSource.observeEventCount()

    override suspend fun addOrUpdateEvent(
        scope: EventScope,
        event: Event,
        scopeEvents: List<Event>?,
    ): Event {
        val duration = measureTime {
            reportException {
                val currentEvents = scopeEvents ?: getEventsFromScope(scope.id)
                validators.forEach {
                    it.validate(currentEvents, event)
                }

                event.scopeId = event.scopeId ?: scope.id
                event.projectId = scope.projectId

                eventLocalDataSource.saveEvent(event)
            }
        }
        Simber.v("Save session event: ${event.type} = ${duration.inWholeMilliseconds}ms")
        return event
    }

    override suspend fun deleteAll() = eventLocalDataSource.deleteAll()


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

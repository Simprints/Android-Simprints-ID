package com.simprints.infra.eventsync.sync.up.tasks

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.authstore.exceptions.RemoteDbNotSignedInException
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.canSyncAllDataToSimprints
import com.simprints.infra.config.store.models.canSyncAnalyticsDataToSimprints
import com.simprints.infra.config.store.models.canSyncBiometricDataToSimprints
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.BiometricReferenceCreationEvent
import com.simprints.infra.events.event.domain.models.EnrolmentEventV2
import com.simprints.infra.events.event.domain.models.EnrolmentEventV4
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.events.event.domain.models.upsync.EventUpSyncRequestEvent
import com.simprints.infra.eventsync.event.remote.ApiUploadEventsBody
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSource
import com.simprints.infra.eventsync.event.remote.models.session.ApiEventScope
import com.simprints.infra.eventsync.event.usecases.MapDomainEventScopeToApiUseCase
import com.simprints.infra.eventsync.exceptions.TryToUploadEventsForNotSignedProject
import com.simprints.infra.eventsync.status.up.EventUpSyncScopeRepository
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation.UpSyncState.COMPLETE
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation.UpSyncState.FAILED
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation.UpSyncState.RUNNING
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncResult
import com.simprints.infra.eventsync.sync.up.EventUpSyncProgress
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SYNC
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.NetworkConnectionException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import retrofit2.HttpException
import java.util.UUID
import javax.inject.Inject

internal class EventUpSyncTask @Inject constructor(
    private val authStore: AuthStore,
    private val eventUpSyncScopeRepo: EventUpSyncScopeRepository,
    private val eventRepository: EventRepository,
    private val eventRemoteDataSource: EventRemoteDataSource,
    private val mapDomainEventScopeToApiUseCase: MapDomainEventScopeToApiUseCase,
    private val timeHelper: TimeHelper,
    private val configManager: ConfigManager,
    private val jsonHelper: JsonHelper,
) {
    fun upSync(
        operation: EventUpSyncOperation,
        eventScope: EventScope,
    ): Flow<EventUpSyncProgress> = flow {
        if (operation.projectId != authStore.signedInProjectId) {
            throw TryToUploadEventsForNotSignedProject("Only events for the signed in project can be uploaded").also {
                Simber.e("Failed to upload events due to missing auth", it, tag = SYNC)
            }
        }

        val projectWithConfig = configManager.refreshProject(operation.projectId)
        val project = projectWithConfig.project
        val config = projectWithConfig.configuration
        var lastOperation = operation.copy()
        var isUsefulUpload = false

        try {
            lastOperation = lastOperation.copy(
                lastState = RUNNING,
                lastSyncTime = timeHelper.now().ms,
            )

            uploadEventScopeType(
                eventScope = eventScope,
                project = project,
                eventScopeTypeToUpload = EventScopeType.SESSION,
                batchSize = config.synchronization.up.simprints.batchSizes.sessions,
                eventFilter = { scopes ->
                    scopes.mapValues { (_, events) ->
                        events?.let { filterEventsToUpSync(it, config) }
                    }
                },
                createUpSyncContentContent = {
                    isUsefulUpload = it > 0
                    EventUpSyncRequestEvent.UpSyncContent(sessionCount = it)
                },
            ).collect { count ->
                lastOperation = lastOperation.copy(
                    lastState = RUNNING,
                    lastSyncTime = timeHelper.now().ms,
                )
                emitProgress(lastOperation, count)
            }
            uploadEventScopeType(
                eventScope = eventScope,
                project = project,
                eventScopeTypeToUpload = EventScopeType.DOWN_SYNC,
                batchSize = config.synchronization.up.simprints.batchSizes.downSyncs,
                createUpSyncContentContent = {
                    isUsefulUpload = it > 0
                    EventUpSyncRequestEvent.UpSyncContent(eventDownSyncCount = it)
                },
            ).collect { count ->
                lastOperation = lastOperation.copy(
                    lastState = RUNNING,
                    lastSyncTime = timeHelper.now().ms,
                )
                emitProgress(lastOperation, count)
            }
            uploadEventScopeType(
                eventScope = eventScope,
                project = project,
                eventScopeTypeToUpload = EventScopeType.UP_SYNC,
                batchSize = config.synchronization.up.simprints.batchSizes.upSyncs,
                createUpSyncContentContent = {
                    // Only tracking up-sync if there have been ay events in other scopes.
                    EventUpSyncRequestEvent.UpSyncContent(
                        eventUpSyncCount = if (isUsefulUpload) it else 0,
                    )
                },
            ).collect { count ->
                lastOperation = lastOperation.copy(
                    lastState = RUNNING,
                    lastSyncTime = timeHelper.now().ms,
                )
                emitProgress(lastOperation, count)
            }
            lastOperation = lastOperation.copy(
                lastState = COMPLETE,
                lastSyncTime = timeHelper.now().ms,
            )

            emitProgress(lastOperation, 0)
        } catch (t: Throwable) {
            if (t is RemoteDbNotSignedInException) {
                throw t
            }

            Simber.e("Failed to upload event scopes", t, tag = SYNC)
            lastOperation = lastOperation.copy(
                lastState = FAILED,
                lastSyncTime = timeHelper.now().ms,
            )

            emitProgress(lastOperation, 0)
        }
    }

    private suspend fun FlowCollector<EventUpSyncProgress>.emitProgress(
        lastOperation: EventUpSyncOperation,
        count: Int,
    ) {
        eventUpSyncScopeRepo.insertOrUpdate(lastOperation)
        this.emit(EventUpSyncProgress(lastOperation, count))
    }

    private fun uploadEventScopeType(
        eventScope: EventScope,
        project: Project,
        eventScopeTypeToUpload: EventScopeType,
        batchSize: Int,
        eventFilter: (Map<EventScope, List<Event>?>) -> Map<EventScope, List<Event>?> = { it },
        createUpSyncContentContent: (Int) -> EventUpSyncRequestEvent.UpSyncContent,
    ) = flow {
        Simber.d("Uploading event scope - $eventScopeTypeToUpload in batches of $batchSize", tag = SYNC)

        while (eventRepository.getClosedEventScopesCount(eventScopeTypeToUpload) > 0 && currentCoroutineContext().isActive) {
            val sessionScopes = getClosedScopesForType(eventScopeTypeToUpload, batchSize)

            // Re-emitting the number of uploaded corrupted events
            attemptInvalidEventUpload(
                project.id,
                sessionScopes.getCorruptedScopes(),
            ).collect { emit(it) }

            val scopesToUpload = sessionScopes
                .filterValues { it != null }
                .let(eventFilter)
                .map { (scope, events) -> mapDomainEventScopeToApiUseCase(scope, events.orEmpty(), project) }
            val uploadedScopes = mutableListOf<String>()

            scopesToUpload.takeIf { it.isNotEmpty() }?.apply {
                val requestId = UUID.randomUUID().toString()

                val requestStartTime = timeHelper.now()
                try {
                    val result = eventRemoteDataSource.post(
                        requestId,
                        project.id,
                        this.asApiUploadEventsBody(eventScopeTypeToUpload),
                    )
                    addRequestEvent(
                        requestId = requestId,
                        eventScope = eventScope,
                        startTime = requestStartTime,
                        result = result,
                        content = createUpSyncContentContent(this.size),
                    )
                    uploadedScopes.addAll(this.map { it.id })
                } catch (ex: Exception) {
                    handleFailedRequest(requestId, ex, eventScope, requestStartTime)
                }
            }

            Simber.d("Deleting ${uploadedScopes.size} session scopes", tag = SYNC)
            eventRepository.deleteEventScopes(uploadedScopes)
        }
    }

    private fun List<ApiEventScope>.asApiUploadEventsBody(eventScopeTypeToUpload: EventScopeType) = when (eventScopeTypeToUpload) {
        EventScopeType.SESSION -> ApiUploadEventsBody(sessions = this)
        EventScopeType.DOWN_SYNC -> ApiUploadEventsBody(eventDownSyncs = this)
        EventScopeType.UP_SYNC -> ApiUploadEventsBody(eventUpSyncs = this)
    }

    private fun Map<EventScope, List<Event>?>.getCorruptedScopes() = filterValues { it == null }.keys

    /**
     * Returns a map of closed event scopes with associated events.
     * If scope events are not un-marshal-able, the value will be null. Such scopes should be
     * uploaded as raw invalid events for further investigation.
     *
     * Additionally emits the number of events in each scope to be used for progress tracking.
     */
    private suspend fun FlowCollector<Int>.getClosedScopesForType(
        type: EventScopeType,
        limit: Int,
    ) = eventRepository.getClosedEventScopes(type, limit).associateWith {
        try {
            eventRepository
                .getEventsFromScope(it.id)
                .also { listOfEvents -> emit(listOfEvents.size) }
        } catch (ex: Exception) {
            if (ex is JsonParseException || ex is JsonMappingException) {
                Simber.i("Failed to un-marshal events", ex, tag = SYNC)
            } else {
                throw ex
            }
            null
        }
    }

    private suspend fun addRequestEvent(
        requestId: String,
        eventScope: EventScope,
        startTime: Timestamp,
        result: EventUpSyncResult,
        content: EventUpSyncRequestEvent.UpSyncContent,
    ) {
        if (content.sessionCount > 0 || content.eventDownSyncCount > 0 || content.eventUpSyncCount > 0) {
            eventRepository.addOrUpdateEvent(
                eventScope,
                EventUpSyncRequestEvent(
                    createdAt = startTime,
                    endedAt = timeHelper.now(),
                    requestId = requestId,
                    content = content,
                    responseStatus = result.status,
                ),
            )
        }
    }

    private suspend fun handleFailedRequest(
        requestId: String,
        ex: Exception,
        eventScope: EventScope,
        requestStartTime: Timestamp,
    ) {
        var result: EventUpSyncResult? = null
        when (ex) {
            is NetworkConnectionException -> Simber.i("Network connection error", ex, tag = SYNC)
            is HttpException -> {
                Simber.i("Network HTTP request error", ex, tag = SYNC)
                result = ex.response()?.let { EventUpSyncResult(it.code()) }
            }

            is RemoteDbNotSignedInException -> throw ex

            else -> {
                Simber.e("Unexpected network error", ex, tag = SYNC)
                // Propagate other exceptions to report failure to the caller.
                throw ex
            }
        }
        eventRepository.addOrUpdateEvent(
            eventScope,
            EventUpSyncRequestEvent(
                createdAt = requestStartTime,
                endedAt = timeHelper.now(),
                requestId = requestId,
                responseStatus = result?.status,
                errorType = ex.javaClass.simpleName,
            ),
        )
    }

    private fun filterEventsToUpSync(
        events: List<Event>,
        config: ProjectConfiguration,
    ) = when {
        config.canSyncAllDataToSimprints() -> events

        config.canSyncBiometricDataToSimprints() -> events.filter {
            it is EnrolmentEventV2 ||
                it is EnrolmentEventV4 ||
                it is PersonCreationEvent ||
                it is FingerprintCaptureBiometricsEvent ||
                it is FaceCaptureBiometricsEvent ||
                it is BiometricReferenceCreationEvent
        }

        config.canSyncAnalyticsDataToSimprints() -> events.filterNot {
            it is FingerprintCaptureBiometricsEvent || it is FaceCaptureBiometricsEvent
        }

        else -> emptyList()
    }

    private fun attemptInvalidEventUpload(
        projectId: String,
        corruptedScopes: Set<EventScope>,
    ) = flow {
        corruptedScopes.forEach { scope ->
            try {
                Simber.i("Uploading invalid events for session ${scope.id}", tag = SYNC)
                val scopeString = jsonHelper.toJson(scope)
                val eventJsons = eventRepository.getEventsJsonFromScope(scope.id)
                emit(eventJsons.size)

                eventRemoteDataSource.dumpInvalidEvents(
                    projectId,
                    listOf(scopeString) + eventJsons,
                )
                eventRepository.deleteEventScope(scope.id)
            } catch (t: Throwable) {
                when (t) {
                    // We don't need to report http exceptions as cloud logs all of them.
                    is NetworkConnectionException, is HttpException -> Simber.i("Failed to upload invalid events", t, tag = SYNC)
                    is RemoteDbNotSignedInException -> throw t
                    else -> Simber.e("Unexpected error while uploading invalid events", t, tag = SYNC)
                }
            }
        }
    }
}

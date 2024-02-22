package com.simprints.infra.eventsync.sync.up.tasks

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.canSyncAllDataToSimprints
import com.simprints.infra.config.store.models.canSyncAnalyticsDataToSimprints
import com.simprints.infra.config.store.models.canSyncBiometricDataToSimprints
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.EnrolmentEventV2
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
import com.simprints.infra.eventsync.exceptions.TryToUploadEventsForNotSignedProject
import com.simprints.infra.eventsync.status.up.EventUpSyncScopeRepository
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation.UpSyncState.COMPLETE
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation.UpSyncState.FAILED
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation.UpSyncState.RUNNING
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncResult
import com.simprints.infra.eventsync.sync.up.EventUpSyncProgress
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.NetworkConnectionException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

internal class EventUpSyncTask @Inject constructor(
    private val authStore: AuthStore,
    private val eventUpSyncScopeRepo: EventUpSyncScopeRepository,
    private val eventRepository: EventRepository,
    private val eventRemoteDataSource: EventRemoteDataSource,
    private val timeHelper: TimeHelper,
    private val configRepository: ConfigRepository,
    private val jsonHelper: JsonHelper,
) {

    fun upSync(
        operation: EventUpSyncOperation,
        eventScope: EventScope,
    ): Flow<EventUpSyncProgress> = flow {
        if (operation.projectId != authStore.signedInProjectId) {
            throw TryToUploadEventsForNotSignedProject("Only events for the signed in project can be uploaded").also {
                Simber.e(it)
            }
        }

        val config = configRepository.getProjectConfiguration()
        var lastOperation = operation.copy()
        var count = 0
        var isUsefulUpload = false

        try {
            lastOperation = lastOperation.copy(
                lastState = RUNNING,
                lastSyncTime = timeHelper.now().ms
            )

            uploadEventScopeType(
                eventScope = eventScope,
                projectId = operation.projectId,
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
            ).collect {
                count = it
                lastOperation = lastOperation.copy(
                    lastState = RUNNING,
                    lastSyncTime = timeHelper.now().ms
                )
                emitProgress(lastOperation, count)
            }
            uploadEventScopeType(
                eventScope = eventScope,
                projectId = operation.projectId,
                eventScopeTypeToUpload = EventScopeType.DOWN_SYNC,
                batchSize = config.synchronization.up.simprints.batchSizes.downSyncs,
                createUpSyncContentContent = {
                    isUsefulUpload = it > 0
                    EventUpSyncRequestEvent.UpSyncContent(eventDownSyncCount = it)
                },
            ).collect {
                count = it
                lastOperation = lastOperation.copy(
                    lastState = RUNNING,
                    lastSyncTime = timeHelper.now().ms
                )
                emitProgress(lastOperation, count)
            }
            uploadEventScopeType(
                eventScope = eventScope,
                projectId = operation.projectId,
                eventScopeTypeToUpload = EventScopeType.UP_SYNC,
                batchSize = config.synchronization.up.simprints.batchSizes.upSyncs,
                createUpSyncContentContent = {
                    // Only tracking up-sync if there have been ay events in other scopes.
                    EventUpSyncRequestEvent.UpSyncContent(
                        eventUpSyncCount = if (isUsefulUpload) it else 0
                    )
                },
            ).collect {
                count = it
                lastOperation = lastOperation.copy(
                    lastState = RUNNING,
                    lastSyncTime = timeHelper.now().ms
                )
                emitProgress(lastOperation, count)
            }
            lastOperation = lastOperation.copy(
                lastState = COMPLETE,
                lastSyncTime = timeHelper.now().ms
            )

            emitProgress(lastOperation, count)
        } catch (t: Throwable) {
            Simber.e(t)
            lastOperation = lastOperation.copy(
                lastState = FAILED,
                lastSyncTime = timeHelper.now().ms
            )

            emitProgress(lastOperation, count)
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
        projectId: String,
        eventScopeTypeToUpload: EventScopeType,
        batchSize: Int,
        eventFilter: (Map<EventScope, List<Event>?>) -> Map<EventScope, List<Event>?> = { it },
        createUpSyncContentContent: (Int) -> EventUpSyncRequestEvent.UpSyncContent,
    ) = flow {
        Simber.d("[EVENT_REPO] Uploading event scope - $eventScopeTypeToUpload in batches of $batchSize")
        val sessionScopes = getClosedScopesForType(eventScopeTypeToUpload)

        // Re-emitting the number of uploaded corrupted events
        attemptInvalidEventUpload(
            projectId,
            sessionScopes.getCorruptedScopes()
        ).collect { emit(it) }

        val scopesToUpload = sessionScopes
            .filterValues { it != null }
            .let(eventFilter)
            .map { (scope, events) -> ApiEventScope.fromDomain(scope, events.orEmpty()) }
        val uploadedScopes = mutableListOf<String>()

        scopesToUpload.chunked(batchSize).forEach { scope ->
            val requestStartTime = timeHelper.now()
            try {
                val result = eventRemoteDataSource.post(
                    projectId,
                    ApiUploadEventsBody(sessions = scope)
                )
                addRequestEvent(
                    eventScope = eventScope,
                    startTime = requestStartTime,
                    result = result,
                    content = createUpSyncContentContent(scope.size),
                )
                uploadedScopes.addAll(scope.map { it.id })
            } catch (ex: Exception) {
                handleFailedRequest(ex, eventScope, requestStartTime)
            }
        }

        Simber.d("[EVENT_REPO] Deleting ${uploadedScopes.size} session scopes")
        eventRepository.deleteEventScopes(uploadedScopes)
    }

    private fun Map<EventScope, List<Event>?>.getCorruptedScopes() =
        filterValues { it == null }.keys

    /**
     * Returns a map of closed event scopes with associated events.
     * If scope events are not un-marshal-able, the value will be null. Such scopes should be
     * uploaded as raw invalid events for further investigation.
     *
     * Additionally emits the number of events in each scope to be used for progress tracking.
     */
    private suspend fun FlowCollector<Int>.getClosedScopesForType(type: EventScopeType) =
        eventRepository.getClosedEventScopes(type).associateWith {
            try {
                eventRepository.getEventsFromScope(it.id)
                    .also { listOfEvents -> emit(listOfEvents.size) }
            } catch (ex: Exception) {
                if (ex is JsonParseException || ex is JsonMappingException) {
                    Simber.i("Failed to un-marshal events")
                    Simber.i(ex)
                } else {
                    throw ex
                }
                null
            }
        }

    private suspend fun addRequestEvent(
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
                    requestId = result.requestId,
                    content = content,
                    responseStatus = result.status,
                )
            )
        }
    }

    private suspend fun handleFailedRequest(
        ex: Exception,
        eventScope: EventScope,
        requestStartTime: Timestamp,
    ) {
        var result: EventUpSyncResult? = null
        when (ex) {
            is NetworkConnectionException -> Simber.i(ex)
            is HttpException -> {
                Simber.i(ex)
                result = ex.response()?.let {
                    EventUpSyncResult(
                        eventRemoteDataSource.getRequestId(it),
                        it.code()
                    )
                }
            }

            else -> {
                Simber.e(ex)
                // Propagate other exceptions to report failure to the caller.
                throw ex
            }
        }
        eventRepository.addOrUpdateEvent(
            eventScope,
            EventUpSyncRequestEvent(
                createdAt = requestStartTime,
                endedAt = timeHelper.now(),
                requestId = result?.requestId.orEmpty(),
                responseStatus = result?.status,
                errorType = ex.toString(),
            )
        )
    }

    private fun filterEventsToUpSync(
        events: List<Event>,
        config: ProjectConfiguration,
    ) = when {
        config.canSyncAllDataToSimprints() -> events

        config.canSyncBiometricDataToSimprints() -> events.filter {
            it is EnrolmentEventV2 ||
                it is PersonCreationEvent ||
                it is FingerprintCaptureBiometricsEvent ||
                it is FaceCaptureBiometricsEvent
        }

        config.canSyncAnalyticsDataToSimprints() -> events.filterNot {
            it is FingerprintCaptureBiometricsEvent || it is FaceCaptureBiometricsEvent
        }

        else -> emptyList()
    }

    private suspend fun attemptInvalidEventUpload(
        projectId: String,
        corruptedScopes: Set<EventScope>,
    ) = flow {
        corruptedScopes.forEach { scope ->
            try {
                Simber.i("Uploading invalid events for session ${scope.id}")
                val scopeString = jsonHelper.toJson(scope)
                val eventJsons = eventRepository.getEventsJsonFromScope(scope.id)
                emit(eventJsons.size)

                eventRemoteDataSource.dumpInvalidEvents(
                    projectId,
                    listOf(scopeString) + eventJsons
                )
                eventRepository.deleteEventScope(scope.id)
            } catch (t: Throwable) {
                when (t) {
                    // We don't need to report http exceptions as cloud logs all of them.
                    is NetworkConnectionException, is HttpException -> Simber.i(t)
                    else -> Simber.e(t)
                }
            }
        }
    }

}

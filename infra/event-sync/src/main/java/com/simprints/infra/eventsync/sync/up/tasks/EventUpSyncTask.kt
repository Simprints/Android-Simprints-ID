package com.simprints.infra.eventsync.sync.up.tasks

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.time.TimeHelper
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
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSource
import com.simprints.infra.eventsync.exceptions.TryToUploadEventsForNotSignedProject
import com.simprints.infra.eventsync.status.up.EventUpSyncScopeRepository
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation.UpSyncState.COMPLETE
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation.UpSyncState.FAILED
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation.UpSyncState.RUNNING
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

    fun upSync(operation: EventUpSyncOperation): Flow<EventUpSyncProgress> = flow {
        if (operation.projectId != authStore.signedInProjectId) {
            throw TryToUploadEventsForNotSignedProject("Only events for the signed in project can be uploaded").also {
                Simber.e(it)
            }
        }

        val config = configRepository.getProjectConfiguration()
        var lastOperation = operation.copy()
        var count = 0

        try {
            lastOperation = lastOperation.copy(lastState = RUNNING, lastSyncTime = timeHelper.now().ms)

            uploadEvents(projectId = operation.projectId, config).collect {
                count = it
                lastOperation =
                    lastOperation.copy(lastState = RUNNING, lastSyncTime = timeHelper.now().ms)
                emitProgress(lastOperation, count)
            }

            lastOperation =
                lastOperation.copy(lastState = COMPLETE, lastSyncTime = timeHelper.now().ms)

            emitProgress(lastOperation, count)
        } catch (t: Throwable) {
            Simber.e(t)
            lastOperation = lastOperation.copy(lastState = FAILED, lastSyncTime = timeHelper.now().ms)

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

    private fun uploadEvents(
        projectId: String,
        config: ProjectConfiguration,
    ) = flow {
        Simber.d("[EVENT_REPO] Uploading")
        try {
            val sessionScopes = eventRepository.getAllClosedSessions().associateWith {
                try {
                    eventRepository.getEventsFromSession(it.id)
                        .also { listOfEvents -> emit(listOfEvents.size) }
                } catch (ex: JsonParseException) {
                    Simber.i("Failed to un-marshal events")
                    Simber.e(ex)
                    null
                }
            }

            val corruptedScopes = sessionScopes.filterValues { it == null }.keys

            // Re-emitting the number of uploaded corrupted events
            attemptInvalidEventUpload(projectId, corruptedScopes).collect { emit(it) }

            val scopesToUpload = sessionScopes
                .filterValues { it != null }
                .mapValues { (_, events) ->
                    events?.let { filterEventsToUpSync(events, config) }.orEmpty()
                }
            if (scopesToUpload.isNotEmpty()) {
                eventRemoteDataSource.post(projectId, scopesToUpload)
            }

            Simber.d("[EVENT_REPO] Deleting ${scopesToUpload.size} session scopes")
            scopesToUpload.keys.forEach { eventRepository.deleteSession(it.id) }
        } catch (ex: Exception) {
            when (ex) {
                is JsonParseException, is JsonMappingException -> {
                    Simber.i("Failed to un-marshal events")
                    Simber.i(ex)
                }

                // We don't need to report http exceptions as cloud logs all of them.
                is NetworkConnectionException, is HttpException -> Simber.i(ex)
                else -> {
                    Simber.e(ex)
                    // Propagate other exceptions to report failure to the caller.
                    throw ex
                }
            }
        }
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
                val eventJsons = eventRepository.getEventsJsonFromSession(scope.id)
                emit(eventJsons.size)

                eventRemoteDataSource.dumpInvalidEvents(projectId, listOf(scopeString) + eventJsons)
                eventRepository.deleteSession(scope.id)
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

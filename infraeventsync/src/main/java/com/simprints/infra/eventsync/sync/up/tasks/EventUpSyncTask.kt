package com.simprints.infra.eventsync.sync.up.tasks

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.canSyncAllDataToSimprints
import com.simprints.infra.config.domain.models.canSyncAnalyticsDataToSimprints
import com.simprints.infra.config.domain.models.canSyncBiometricDataToSimprints
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.EnrolmentEventV2
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.session.SessionCaptureEvent
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSource
import com.simprints.infra.eventsync.exceptions.TryToUploadEventsForNotSignedProject
import com.simprints.infra.eventsync.status.up.EventUpSyncScopeRepository
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation.UpSyncState.*
import com.simprints.infra.eventsync.sync.common.SYNC_LOG_TAG
import com.simprints.infra.eventsync.sync.up.EventUpSyncProgress
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.LoginManager
import com.simprints.infra.network.exceptions.NetworkConnectionException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

internal class EventUpSyncTask @Inject constructor(
    private val loginManager: LoginManager,
    private val eventUpSyncScopeRepo: EventUpSyncScopeRepository,
    private val eventRepository: EventRepository,
    private val eventRemoteDataSource: EventRemoteDataSource,
    private val timeHelper: TimeHelper,
    private val configManager: ConfigManager,
) {

    fun upSync(operation: EventUpSyncOperation): Flow<EventUpSyncProgress> = flow {
        if (operation.projectId != loginManager.getSignedInProjectIdOrEmpty()) {
            throw TryToUploadEventsForNotSignedProject("Only events for the signed in project can be uploaded").also {
                Simber.e(it)
            }
        }

        val config = configManager.getProjectConfiguration()
        var lastOperation = operation.copy()
        var count = 0
        try {
            uploadEvents(
                projectId = operation.projectId,
                canSyncAllDataToSimprints = config.canSyncAllDataToSimprints(),
                canSyncBiometricDataToSimprints = config.canSyncBiometricDataToSimprints(),
                canSyncAnalyticsDataToSimprints = config.canSyncAnalyticsDataToSimprints()
            ).collect {
                Simber.tag(SYNC_LOG_TAG).d("[UP_SYNC_HELPER] Uploading $it events")
                count = it
                lastOperation = lastOperation.copy(lastState = RUNNING, lastSyncTime = timeHelper.now())
                emitProgress(lastOperation, count)
            }

            lastOperation = lastOperation.copy(lastState = COMPLETE, lastSyncTime = timeHelper.now())
            emitProgress(lastOperation, count)
        } catch (t: Throwable) {
            Simber.e(t)
            lastOperation = lastOperation.copy(lastState = FAILED, lastSyncTime = timeHelper.now())

            emitProgress(lastOperation, count)
        }
    }

    private suspend fun FlowCollector<EventUpSyncProgress>.emitProgress(
        lastOperation: EventUpSyncOperation,
        count: Int
    ) {
        eventUpSyncScopeRepo.insertOrUpdate(lastOperation)
        this.emit(EventUpSyncProgress(lastOperation, count))
    }

    /**
     * Note that only the IDs of the SessionCapture events of closed sessions are all held in
     * memory at once. Events are loaded in memory and uploaded session by session, ensuring the
     * memory usage stays low. It means that we do not exploit connectivity as aggressively as
     * possible (we could have a more complex system that always pre-fetches the next batch of
     * events while we upload the current one), but given the relatively small amount of data to
     * upload, and how critical this system is, we are happy to trade off speed for reliability
     * (through simplicity and low resource usage)
     */
    private fun uploadEvents(
        projectId: String,
        canSyncAllDataToSimprints: Boolean,
        canSyncBiometricDataToSimprints: Boolean,
        canSyncAnalyticsDataToSimprints: Boolean
    ): Flow<Int> = flow {
        Simber.d("[EVENT_REPO] Uploading")

        eventRepository.getAllClosedSessionIds(projectId).forEach { sessionId ->
            // The events will include the SessionCaptureEvent event
            Simber.d("[EVENT_REPO] Uploading session $sessionId")
            try {
                eventRepository.getEventsFromSession(sessionId).let {
                    attemptEventUpload(
                        it, projectId,
                        canSyncAllDataToSimprints,
                        canSyncBiometricDataToSimprints,
                        canSyncAnalyticsDataToSimprints
                    )
                    this.emit(it.size)
                }
            } catch (ex: Exception) {
                if (ex is JsonParseException || ex is JsonMappingException) {
                    attemptInvalidEventUpload(projectId, sessionId)?.let { this.emit(it) }
                } else {
                    Simber.i("Failed to un-marshal events for $sessionId")
                    Simber.e(ex)
                }
            }
        }
    }

    private suspend fun attemptEventUpload(
        events: List<Event>, projectId: String,
        canSyncAllDataToSimprints: Boolean,
        canSyncBiometricDataToSimprints: Boolean,
        canSyncAnalyticsDataToSimprints: Boolean
    ) {
        try {
            val filteredEvents = when {
                canSyncAllDataToSimprints -> {
                    events
                }
                canSyncBiometricDataToSimprints -> {
                    events.filter {
                        it is EnrolmentEventV2 || it is PersonCreationEvent || it is FingerprintCaptureBiometricsEvent || it is FaceCaptureBiometricsEvent
                    }
                }
                canSyncAnalyticsDataToSimprints -> {
                    events.filterNot {
                        it is FingerprintCaptureBiometricsEvent || it is FaceCaptureBiometricsEvent
                    }
                }
                else -> {
                    emptyList()
                }
            }
            uploadEvents(filteredEvents, projectId)
            deleteEventsFromDb(events.map { it.id })
        } catch (t: Throwable) {
            handleUploadException(t)
        }
    }

    private suspend fun attemptInvalidEventUpload(projectId: String, sessionId: String): Int? =
        try {
            Simber.i("Uploading invalid events for session $sessionId")
            eventRepository.getEventsJsonFromSession(sessionId).let {
                eventRemoteDataSource.dumpInvalidEvents(projectId, events = it)
                eventRepository.deleteSessionEvents(sessionId)
                it.size
            }
        } catch (t: Throwable) {
            handleUploadException(t)
            null
        }

    private fun handleUploadException(t: Throwable) {
        when (t) {
            is NetworkConnectionException -> Simber.i(t)
            // We don't need to report http exceptions as cloud logs all of them.
            is HttpException -> Simber.i(t)
            else -> Simber.e(t)
        }
    }

    private suspend fun deleteEventsFromDb(eventsIds: List<String>) {
        Simber.d("[EVENT_REPO] Deleting ${eventsIds.count()} events")
        eventRepository.delete(eventsIds)
    }

    private suspend fun uploadEvents(events: List<Event>, projectId: String) {
        events.filterIsInstance<SessionCaptureEvent>()
            .forEach { it.payload.uploadedAt = timeHelper.now() }
        eventRemoteDataSource.post(projectId, events)
    }
}

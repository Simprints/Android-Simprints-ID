package com.simprints.infra.events

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.events.event.domain.EventCount
import com.simprints.infra.events.event.domain.models.EnrolmentEventV2
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.session.SessionCaptureEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvent
import com.simprints.infra.events.events_sync.down.domain.RemoteEventQuery
import com.simprints.infra.events.events_sync.down.domain.fromDomainToApi
import com.simprints.infra.events.exceptions.TryToUploadEventsForNotSignedProject
import com.simprints.infra.events.remote.EventRemoteDataSource
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.LoginManager
import com.simprints.infra.network.exceptions.NetworkConnectionException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal open class EventSyncRepositoryImpl @Inject constructor(
        private val loginManager: LoginManager,
        private val eventRepository: EventRepository,
        private val eventRemoteDataSource: EventRemoteDataSource,
        private val timeHelper: TimeHelper,
) : EventSyncRepository {


    override suspend fun countEventsToUpload(projectId: String, type: EventType?): Flow<Int> = eventRepository
        .observeEventCount(projectId, type)

    /**
     * Note that only the IDs of the SessionCapture events of closed sessions are all held in
     * memory at once. Events are loaded in memory and uploaded session by session, ensuring the
     * memory usage stays low. It means that we do not exploit connectivity as aggressively as
     * possible (we could have a more complex system that always pre-fetches the next batch of
     * events while we upload the current one), but given the relatively small amount of data to
     * upload, and how critical this system is, we are happy to trade off speed for reliability
     * (through simplicity and low resource usage)
     */
    override fun uploadEvents(
        projectId: String,
        canSyncAllDataToSimprints: Boolean,
        canSyncBiometricDataToSimprints: Boolean,
        canSyncAnalyticsDataToSimprints: Boolean
    ): Flow<Int> = flow {
        Simber.d("[EVENT_REPO] Uploading")

        if (projectId != loginManager.getSignedInProjectIdOrEmpty()) {
            throw TryToUploadEventsForNotSignedProject("Only events for the signed in project can be uploaded").also {
                Simber.e(it)
            }
        }

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
                    attemptInvalidEventUpload(projectId, sessionId)
                        ?.let { this.emit(it) }
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


    override suspend fun countEventsToDownload(query: RemoteEventQuery): List<EventCount> = eventRemoteDataSource.count(query.fromDomainToApi())

    override suspend fun downloadEvents(
            scope: CoroutineScope,
            query: RemoteEventQuery
    ): ReceiveChannel<EnrolmentRecordEvent> = eventRemoteDataSource.getEvents(query.fromDomainToApi(), scope)

}

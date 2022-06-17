package com.simprints.eventsystem.event

import com.simprints.eventsystem.event.domain.EventCount
import com.simprints.eventsystem.event.domain.models.ArtificialTerminationEvent
import com.simprints.eventsystem.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.session.SessionCaptureEvent
import com.simprints.eventsystem.events_sync.down.domain.RemoteEventQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow


interface EventRepository {

    val libSimprintsVersionName: String

    suspend fun createSession(): SessionCaptureEvent

    /**
     * The reason is only used when we want to create an [ArtificialTerminationEvent].
     * If the session is closing for normal reasons (i.e. came to a normal end), then it should be `null`.
     */
    suspend fun closeCurrentSession(reason: Reason? = null)

    suspend fun getCurrentCaptureSessionEvent(): SessionCaptureEvent

    suspend fun getEventsFromSession(sessionId: String): Flow<Event>

    suspend fun addOrUpdateEvent(event: Event)

    fun uploadEvents(
        projectId: String,
        canSyncAllDataToSimprints: Boolean,
        canSyncBiometricDataToSimprints: Boolean,
        canSyncAnalyticsDataToSimprints: Boolean
    ): Flow<Int>

    suspend fun localCount(projectId: String): Int

    suspend fun localCount(projectId: String, type: EventType): Int

    suspend fun countEventsToDownload(query: RemoteEventQuery): List<EventCount>

    suspend fun downloadEvents(
        scope: CoroutineScope,
        query: RemoteEventQuery
    ): ReceiveChannel<Event>

    suspend fun deleteSessionEvents(sessionId: String)
}

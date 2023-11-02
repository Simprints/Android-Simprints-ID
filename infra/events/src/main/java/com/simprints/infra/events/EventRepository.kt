package com.simprints.infra.events

import com.simprints.infra.events.event.domain.models.ArtificialTerminationEvent
import com.simprints.infra.events.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.session.SessionCaptureEvent
import kotlinx.coroutines.flow.Flow


interface EventRepository {

    val libSimprintsVersionName: String

    suspend fun createSession(): SessionCaptureEvent

    /**
     * The reason is only used when we want to create an [ArtificialTerminationEvent].
     * If the session is closing for normal reasons (i.e. came to a normal end), then it should be `null`.
     */
    suspend fun closeCurrentSession(reason: Reason? = null)

    /**
     * Get current capture session event from event cache or from room db.
     * or create a new event if needed
     * @return SessionCaptureEvent
     */
    suspend fun getCurrentCaptureSessionEvent(): SessionCaptureEvent

    suspend fun observeEventsFromSession(sessionId: String): Flow<Event>

    suspend fun getAllClosedSessionIds(projectId: String): List<String>

    suspend fun getEventsFromSession(sessionId: String): List<Event>

    suspend fun getEventsJsonFromSession(sessionId: String): List<String>

    suspend fun observeEventCount(projectId: String, type: EventType?): Flow<Int>

    suspend fun loadAll(): Flow<Event>

    suspend fun addOrUpdateEvent(event: Event)

    suspend fun removeLocationDataFromCurrentSession()

    suspend fun deleteSessionEvents(sessionId: String)

    suspend fun delete(eventIds: List<String>)

    suspend fun deleteAll()

}

package com.simprints.infra.events

import com.simprints.infra.events.event.domain.models.ArtificialTerminationEvent
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.session.SessionEndCause
import com.simprints.infra.events.event.domain.models.session.SessionScope
import kotlinx.coroutines.flow.Flow


interface EventRepository {

    val libSimprintsVersionName: String

    suspend fun createSession(): SessionScope

    suspend fun hasOpenSession(): Boolean

    /**
     * The reason is only used when we want to create an [ArtificialTerminationEvent].
     * If the session is closing for normal reasons (i.e. came to a normal end), then it should be `null`.
     */
    suspend fun closeCurrentSession(reason: SessionEndCause? = null)

    /**
     * Get current capture session event from event cache or from room db.
     * or create a new event if needed
     * @return SessionCaptureEvent
     */
    suspend fun getCurrentSessionScope(): SessionScope

    suspend fun getAllClosedSessions(projectId: String): List<SessionScope>

    suspend fun saveSessionScope(sessionScope: SessionScope)

    suspend fun observeEventsFromSession(sessionId: String): Flow<Event>

    suspend fun getEventsFromSession(sessionId: String): List<Event>

    suspend fun getEventsJsonFromSession(sessionId: String): List<String>

    suspend fun observeEventCount(projectId: String, type: EventType?): Flow<Int>

    suspend fun loadAll(): Flow<Event>

    suspend fun addOrUpdateEvent(event: Event)

    suspend fun removeLocationDataFromCurrentSession()

    suspend fun deleteSession(sessionId: String)

    suspend fun deleteSessionEvents(sessionId: String)

    suspend fun delete(eventIds: List<String>)

    suspend fun deleteAll()

}

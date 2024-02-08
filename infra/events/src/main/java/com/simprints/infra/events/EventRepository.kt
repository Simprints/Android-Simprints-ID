package com.simprints.infra.events

import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.scope.SessionEndCause
import com.simprints.infra.events.event.domain.models.scope.EventScope
import kotlinx.coroutines.flow.Flow


interface EventRepository {

    val libSimprintsVersionName: String

    suspend fun createSession(): EventScope

    suspend fun hasOpenSession(): Boolean

    /**
     * If the session is closing for normal reasons (i.e. came to a normal end), then it should be `null`.
     */
    suspend fun closeCurrentSession(reason: SessionEndCause? = null)

    /**
     * Get current capture session event from event cache or from room db.
     * or create a new event if needed
     * @return SessionCaptureEvent
     */
    suspend fun getCurrentSessionScope(): EventScope

    suspend fun getAllClosedSessions(): List<EventScope>

    suspend fun saveSessionScope(eventScope: EventScope)

    suspend fun observeEventsFromSession(sessionId: String): Flow<Event>

    suspend fun getEventsFromSession(sessionId: String): List<Event>

    suspend fun getEventsJsonFromSession(sessionId: String): List<String>

    suspend fun observeEventCount(type: EventType?): Flow<Int>

    suspend fun loadAll(): Flow<Event>

    suspend fun addOrUpdateEvent(event: Event)

    suspend fun removeLocationDataFromCurrentSession()

    suspend fun deleteSession(sessionId: String)

    suspend fun deleteAll()

}

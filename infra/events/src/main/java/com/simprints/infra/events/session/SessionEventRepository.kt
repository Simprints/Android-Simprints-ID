package com.simprints.infra.events.session

import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.scope.EventScopeEndCause

/**
 * Event repository wrapper that provides session scope specific
 * features such as caching and pre-filled event fields.
 */
interface SessionEventRepository {
    suspend fun createSession(): EventScope

    suspend fun saveSessionScope(eventScope: EventScope)

    suspend fun hasOpenSession(): Boolean

    suspend fun getCurrentSessionScope(): EventScope

    suspend fun removeLocationDataFromCurrentSession()

    suspend fun getEventsInCurrentSession(): List<Event>

    suspend fun addOrUpdateEvent(event: Event)

    suspend fun closeCurrentSession(reason: EventScopeEndCause? = null)
}

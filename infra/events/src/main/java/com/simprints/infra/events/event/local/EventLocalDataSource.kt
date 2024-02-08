package com.simprints.infra.events.event.local

import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.scope.EventScope
import kotlinx.coroutines.flow.Flow

internal interface EventLocalDataSource {

    suspend fun saveEventScope(scope: EventScope)
    suspend fun countEventScopes(): Int
    suspend fun loadOpenedScopes(): List<EventScope>
    suspend fun loadClosedScopes(): List<EventScope>
    suspend fun deleteEventScope(sessionId: String)

    suspend fun saveEvent(event: Event)
    suspend fun observeEventCount(): Flow<Int>
    suspend fun observeEventCount(type: EventType): Flow<Int>
    suspend fun loadAllEvents(): Flow<Event>
    suspend fun loadEventJsonInSession(sessionId: String): List<String>
    suspend fun loadEventsInSession(sessionId: String): List<Event>
    suspend fun deleteEventsInSession(sessionId: String)

    suspend fun deleteAll()
}

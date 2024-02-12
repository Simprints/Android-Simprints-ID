package com.simprints.infra.events.event.local

import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import kotlinx.coroutines.flow.Flow

internal interface EventLocalDataSource {

    suspend fun saveEventScope(scope: EventScope)
    suspend fun countEventScopes(): Int
    suspend fun loadOpenedScopes(): List<EventScope>
    suspend fun loadOpenedScopes(type: EventScopeType): List<EventScope>
    suspend fun loadClosedScopes(): List<EventScope>
    suspend fun loadClosedScopes(type: EventScopeType): List<EventScope>
    suspend fun deleteEventScope(scopeId: String)

    suspend fun saveEvent(event: Event)
    suspend fun observeEventCount(): Flow<Int>
    suspend fun observeEventCount(type: EventType): Flow<Int>
    suspend fun loadAllEvents(): Flow<Event>
    suspend fun loadEventJsonInScope(scopeId: String): List<String>
    suspend fun loadEventsInScope(scopeId: String): List<Event>
    suspend fun deleteEventsInScope(scopeId: String)

    suspend fun deleteAll()
}

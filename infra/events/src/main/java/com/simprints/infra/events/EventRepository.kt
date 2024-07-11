package com.simprints.infra.events

import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.scope.EventScopeEndCause
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import kotlinx.coroutines.flow.Flow


interface EventRepository {

    val libSimprintsVersionName: String

    suspend fun createEventScope(type: EventScopeType, scopeId: String? = null): EventScope
    suspend fun getEventScope(downSyncEventScopeId: String): EventScope?
    suspend fun closeEventScope(eventScope: EventScope, reason: EventScopeEndCause?)
    suspend fun closeEventScope(eventScopeId: String, reason: EventScopeEndCause?)
    suspend fun closeAllOpenScopes(type: EventScopeType, reason: EventScopeEndCause?)
    suspend fun saveEventScope(eventScope: EventScope)
    suspend fun getOpenEventScopes(type: EventScopeType): List<EventScope>
    suspend fun getClosedEventScopes(type: EventScopeType, limit: Int): List<EventScope>
    suspend fun getClosedEventScopesCount(type: EventScopeType): Int
    suspend fun deleteEventScope(scopeId: String)
    suspend fun deleteEventScopes(scopeIds: List<String>)

    suspend fun getEventsFromScope(scopeId: String): List<Event>
    suspend fun getEventsJsonFromScope(scopeId: String): List<String>
    suspend fun getAllEvents(): Flow<Event>
    suspend fun observeEventCount(type: EventType?): Flow<Int>
    suspend fun addOrUpdateEvent(
        scope: EventScope,
        event: Event,
        scopeEvents: List<Event>? = null,
    ): Event
    suspend fun deleteAll()


}

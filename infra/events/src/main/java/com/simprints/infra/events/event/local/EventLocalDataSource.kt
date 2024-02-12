package com.simprints.infra.events.event.local

import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.session.SessionScope
import kotlinx.coroutines.flow.Flow

internal interface EventLocalDataSource {

    suspend fun saveSessionScope(scope: SessionScope)
    suspend fun countSessions(): Int
    suspend fun loadOpenedSessions(): List<SessionScope>
    suspend fun loadClosedSessions(): List<SessionScope>
    suspend fun deleteSession(sessionId: String)

    suspend fun saveEvent(event: Event)
    suspend fun observeEventCount(): Flow<Int>
    suspend fun observeEventCount(type: EventType): Flow<Int>
    suspend fun loadAllEvents(): Flow<Event>
    suspend fun loadEventJsonInSession(sessionId: String): List<String>
    suspend fun loadEventsInSession(sessionId: String): List<Event>
    suspend fun deleteEventsInSession(sessionId: String)

    suspend fun deleteAll()
}

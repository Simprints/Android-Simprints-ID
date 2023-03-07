package com.simprints.infra.events.event.local

import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType
import kotlinx.coroutines.flow.Flow

internal interface EventLocalDataSource {

    suspend fun count(projectId: String): Int
    suspend fun count(type: EventType): Int
    suspend fun observeCount(projectId: String): Flow<Int>
    suspend fun observeCount(projectId: String, type: EventType): Flow<Int>
    suspend fun loadAll(): Flow<Event>
    suspend fun loadAllEventJsonFromSession(sessionId: String): List<String>
    suspend fun loadAllFromSession(sessionId: String): List<Event>
    suspend fun loadOpenedSessions(): Flow<Event>
    suspend fun loadAllClosedSessionIds(projectId: String): List<String>
    suspend fun delete(ids: List<String>)
    suspend fun deleteAllFromSession(sessionId: String)
    suspend fun deleteAll()
    suspend fun insertOrUpdate(event: Event)
}

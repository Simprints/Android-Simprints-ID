package com.simprints.eventsystem.event.local

import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventType
import kotlinx.coroutines.flow.Flow

interface EventLocalDataSource {

    suspend fun count(projectId: String): Int
    suspend fun count(projectId: String, type: EventType): Int
    suspend fun count(type: EventType): Int
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

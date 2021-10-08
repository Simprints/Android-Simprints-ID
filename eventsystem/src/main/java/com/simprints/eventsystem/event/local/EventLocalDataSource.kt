package com.simprints.eventsystem.event.local

import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventType
import kotlinx.coroutines.flow.Flow

interface EventLocalDataSource {

    suspend fun count(projectId: String): Int
    suspend fun count(projectId: String, type: EventType): Int
    suspend fun count(type: EventType): Int
    suspend fun loadAll(): Flow<Event>
    suspend fun loadAllFromSession(sessionId: String): List<Event>
    suspend fun loadAllFromProject(projectId: String): Flow<Event>
    suspend fun loadAllSessions(isClosed: Boolean): Flow<Event>
    suspend fun loadAllClosedSessionIds(projectId: String): List<String>
    suspend fun delete(ids: List<String>)
    suspend fun deleteAllFromSession(sessionId: String)
    suspend fun deleteAll()
    suspend fun insertOrUpdate(event: Event)
    @Deprecated(message = "Can be removed once all projects are on 2021.1.0+")
    suspend fun loadOldSubjectCreationEvents(projectId: String): List<Event>

}

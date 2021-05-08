package com.simprints.id.data.db.event.local

import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import kotlinx.coroutines.flow.Flow

interface EventLocalDataSource {

    suspend fun count(projectId: String): Int
    suspend fun count(projectId: String, type: EventType): Int
    suspend fun count(type: EventType): Int
    suspend fun loadAll(): Flow<Event>
    suspend fun loadAllFromSession(sessionId: String): List<Event>
    suspend fun loadAllFromProject(projectId: String): Flow<Event>
    suspend fun loadAllSessions(isClosed: Boolean): Flow<Event>
    suspend fun loadOldestClosedSession(projectId: String): SessionCaptureEvent?
    suspend fun delete(id: String)
    suspend fun deleteAllFromSession(sessionId: String)
    suspend fun deleteAll()
    suspend fun insertOrUpdate(event: Event)
    suspend fun loadAbandonedEvents(projectId: String): List<Event>

}

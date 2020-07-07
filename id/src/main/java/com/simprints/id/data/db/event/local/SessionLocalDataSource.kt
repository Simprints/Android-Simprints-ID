package com.simprints.id.data.db.event.local

import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.SessionQuery
import com.simprints.id.data.db.event.domain.session.SessionEvents
import kotlinx.coroutines.flow.Flow

interface SessionLocalDataSource {

    suspend fun create(appVersionName: String,
                       libSimprintsVersionName: String,
                       language: String,
                       deviceId: String)

    suspend fun count(query: SessionQuery): Int
    suspend fun load(query: SessionQuery): Flow<SessionEvents>
    suspend fun delete(query: SessionQuery)
    suspend fun update(sessionId: String, updateBlock: (SessionEvents) -> Unit)

    suspend fun updateCurrentSession(updateBlock: (SessionEvents) -> Unit)
    suspend fun addEventToCurrentSession(event: Event)
}

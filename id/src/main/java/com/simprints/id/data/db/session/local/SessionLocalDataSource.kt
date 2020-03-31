package com.simprints.id.data.db.session.local

import com.simprints.id.data.db.session.domain.models.SessionQuery
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
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

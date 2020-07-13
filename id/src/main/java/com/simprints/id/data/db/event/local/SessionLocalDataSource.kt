package com.simprints.id.data.db.event.local

import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventQuery
import kotlinx.coroutines.flow.Flow

interface SessionLocalDataSource {

    suspend fun create(appVersionName: String,
                       libSimprintsVersionName: String,
                       language: String,
                       deviceId: String)

    suspend fun count(query: EventQuery): Int
    suspend fun load(query: EventQuery): Flow<Event>
    suspend fun delete(query: EventQuery)
    suspend fun insertOrUpdate(event: Event)

    suspend fun currentSessionId(): String?
}

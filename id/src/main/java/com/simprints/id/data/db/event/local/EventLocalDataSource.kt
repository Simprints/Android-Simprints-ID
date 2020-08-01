package com.simprints.id.data.db.event.local

import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.event.local.models.DbEventQuery
import kotlinx.coroutines.flow.Flow

interface EventLocalDataSource {

    suspend fun create(event: SessionCaptureEvent)

    suspend fun count(dbQuery: DbEventQuery = DbEventQuery()): Int
    suspend fun load(dbQuery: DbEventQuery = DbEventQuery()): Flow<Event>
    suspend fun delete(dbQuery: DbEventQuery = DbEventQuery())

    suspend fun insertOrUpdate(event: Event)
    suspend fun insertOrUpdateInCurrentSession(event: Event)

    suspend fun getCurrentSessionCaptureEvent(): SessionCaptureEvent
}

package com.simprints.id.data.db.event.local

import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.local.models.DbLocalEventQuery
import kotlinx.coroutines.flow.Flow

interface EventLocalDataSource {

    suspend fun count(dbQuery: DbLocalEventQuery = DbLocalEventQuery()): Int
    suspend fun load(dbQuery: DbLocalEventQuery = DbLocalEventQuery()): Flow<Event>
    suspend fun delete(dbQuery: DbLocalEventQuery = DbLocalEventQuery())
    suspend fun insertOrUpdate(event: Event)
}

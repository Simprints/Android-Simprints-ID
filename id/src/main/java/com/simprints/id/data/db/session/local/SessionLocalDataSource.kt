package com.simprints.id.data.db.session.local

import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import kotlinx.coroutines.flow.Flow

interface SessionLocalDataSource {

    data class Query(val id: String? = null,
                     val projectId: String? = null,
                     val openSession: Boolean? = null,
                     val startedBefore: Long? = null)

    suspend fun create(appVersionName: String,
               libSimprintsVersionName: String,
               language: String,
               deviceId: String)

    suspend fun count(query: Query): Int
    suspend fun load(query: Query): Flow<SessionEvents>
    suspend fun delete(query: Query)
    suspend fun updateCurrentSession(update: (SessionEvents) -> Unit)

    @Deprecated("Respect coroutines - use updateCurrentSession")
    fun addEventInBackground(event: Event)

    @Deprecated("gonna remove it soon")
    suspend fun insertOrUpdateSessionEvents(sessionEvents: SessionEvents)
}

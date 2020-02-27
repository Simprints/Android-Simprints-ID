package com.simprints.id.data.db.session.local

import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import io.reactivex.Completable
import kotlinx.coroutines.flow.Flow

interface SessionLocalDataSource {

    data class Query(val id: String? = null,
                     val projectId: String? = null,
                     val openSession: Boolean? = null,
                     val startedBefore: Long? = null)

    suspend fun create(sessionEvents: SessionEvents): Completable
    suspend fun load(query: Query): Flow<SessionEvents>
    suspend fun count(query: Query): Int
    suspend fun delete(query: Query)


    @Deprecated("gonna remove it soon")
    suspend fun insertOrUpdateSessionEvents(sessionEvents: SessionEvents)
}

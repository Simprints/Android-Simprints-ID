package com.simprints.id.data.db.subject.remote

import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.subjects_sync.down.domain.SyncEventQuery
import java.io.InputStream

interface EventRemoteDataSource {

    suspend fun count(query: SyncEventQuery): List<EventCount>

    suspend fun getStreaming(query: SyncEventQuery): InputStream

    suspend fun post(projectId: String, events: List<Event>)

}

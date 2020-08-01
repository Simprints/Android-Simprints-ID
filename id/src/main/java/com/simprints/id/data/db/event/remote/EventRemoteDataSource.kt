package com.simprints.id.data.db.event.remote

import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.event.domain.models.Event
import java.io.InputStream

interface EventRemoteDataSource {

    suspend fun count(query: ApiEventQuery): List<EventCount>

    suspend fun getStreaming(query: ApiEventQuery): InputStream

    suspend fun post(projectId: String, events: List<Event>)

}

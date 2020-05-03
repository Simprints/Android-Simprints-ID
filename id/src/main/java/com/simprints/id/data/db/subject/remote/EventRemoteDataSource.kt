package com.simprints.id.data.db.subject.remote

import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.subjects_sync.down.domain.EventQuery
import com.simprints.id.data.db.subject.domain.personevents.Events
import java.io.InputStream

interface EventRemoteDataSource {

    suspend fun count(query: EventQuery): List<EventCount>

    suspend fun getStreaming(query: EventQuery): InputStream

    suspend fun post(projectId: String, events: Events)

}

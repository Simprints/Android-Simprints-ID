package com.simprints.id.data.db.person.remote

import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.people_sync.down.domain.EventQuery
import com.simprints.id.data.db.person.remote.models.personevents.ApiEvents
import okhttp3.ResponseBody

interface EventRemoteDataSource {

    suspend fun count(query: EventQuery): List<EventCount>

    suspend fun get(query: EventQuery): ResponseBody

    suspend fun post(projectId: String, events: ApiEvents)

}

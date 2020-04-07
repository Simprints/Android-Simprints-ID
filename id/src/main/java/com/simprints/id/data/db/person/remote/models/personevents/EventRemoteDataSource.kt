package com.simprints.id.data.db.person.remote.models.personevents

import com.simprints.id.data.db.common.models.EventCount
import okhttp3.ResponseBody

interface EventRemoteDataSource {

    suspend fun count(query: ApiEventQuery): List<EventCount>

    suspend fun get(query: ApiEventQuery): ResponseBody

    suspend fun write(projectId: String, events: ApiEvents)

}

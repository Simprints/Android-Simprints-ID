package com.simprints.id.data.db.person.remote.models.personevents

import com.simprints.id.data.db.person.remote.models.personcounts.ApiEventCounts
import okhttp3.ResponseBody

interface EventRemoteDataSource {

    suspend fun count(query: ApiEventQuery): ApiEventCounts

    suspend fun get(query: ApiEventQuery): ResponseBody

    suspend fun write(projectId: String, events: ApiEvents)

}

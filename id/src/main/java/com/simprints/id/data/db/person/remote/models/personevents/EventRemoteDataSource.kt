package com.simprints.id.data.db.person.remote.models.personevents

import com.simprints.id.data.db.person.remote.models.personcounts.ApiEventCounts

interface EventRemoteDataSource {

    suspend fun count(query: String): ApiEventCounts

    suspend fun get(query: String): List<ApiEvent>

    suspend fun write(events: List<ApiEvent>)

}

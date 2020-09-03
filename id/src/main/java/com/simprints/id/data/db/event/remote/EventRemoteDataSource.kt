package com.simprints.id.data.db.event.remote

import com.simprints.id.data.db.event.domain.EventCount
import com.simprints.id.data.db.event.remote.models.ApiEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

interface EventRemoteDataSource {

    suspend fun count(query: ApiRemoteEventQuery): List<EventCount>

    suspend fun getEvents(query: ApiRemoteEventQuery, scope: CoroutineScope): ReceiveChannel<ApiEvent>

    suspend fun post(projectId: String, events: List<ApiEvent>)
}

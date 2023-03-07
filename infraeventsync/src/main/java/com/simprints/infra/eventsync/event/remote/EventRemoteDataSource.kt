package com.simprints.infra.eventsync.event.remote

import com.simprints.infra.events.event.domain.EventCount
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

internal interface EventRemoteDataSource {

    suspend fun count(query: ApiRemoteEventQuery): List<EventCount>

    suspend fun dumpInvalidEvents(projectId: String, events: List<String>)

    suspend fun getEvents(query: ApiRemoteEventQuery, scope: CoroutineScope): ReceiveChannel<EnrolmentRecordEvent>

    suspend fun post(projectId: String, events: List<Event>, acceptInvalidEvents: Boolean = true)
}

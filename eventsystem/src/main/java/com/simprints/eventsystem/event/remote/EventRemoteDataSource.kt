package com.simprints.eventsystem.event.remote

import com.simprints.eventsystem.event.domain.EventCount
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

internal interface EventRemoteDataSource {

    suspend fun count(query: ApiRemoteEventQuery): List<EventCount>

    suspend fun dumpInvalidEvents(projectId: String, events: List<String>)

    suspend fun getEvents(query: ApiRemoteEventQuery, scope: CoroutineScope): ReceiveChannel<EnrolmentRecordEvent>

    suspend fun post(projectId: String, events: List<Event>, acceptInvalidEvents: Boolean = true)
}

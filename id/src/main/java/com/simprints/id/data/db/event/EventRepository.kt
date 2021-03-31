package com.simprints.id.data.db.event

import com.simprints.id.Application
import com.simprints.id.data.db.event.domain.EventCount
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.events_sync.down.domain.RemoteEventQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow


interface EventRepository {

    val libSimprintsVersionName: String

    suspend fun createSession(): SessionCaptureEvent

    suspend fun closeAllSessions(reason: Reason? = null)

    suspend fun getCurrentCaptureSessionEvent(): SessionCaptureEvent

    suspend fun getEventsFromSession(sessionId: String): Flow<Event>

    suspend fun addOrUpdateEvent(event: Event)

    suspend fun uploadEvents(projectId: String): Flow<Int>

    suspend fun localCount(projectId: String): Int

    suspend fun localCount(projectId: String, type: EventType): Int

    suspend fun countEventsToDownload(query: RemoteEventQuery): List<EventCount>

    suspend fun downloadEvents(
        scope: CoroutineScope,
        query: RemoteEventQuery
    ): ReceiveChannel<Event>

    suspend fun deleteSessionEvents(sessionId: String)

    companion object {
        fun build(app: Application): EventRepository =
            app.component.getSessionEventsManager()
    }
}

package com.simprints.id.data.db.event

import com.simprints.id.Application
import com.simprints.id.data.db.event.domain.EventCount
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.events_sync.down.domain.RemoteEventQuery
import com.simprints.id.data.db.events_sync.up.domain.LocalEventQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow


interface EventRepository {

    suspend fun createSession(libSimprintsVersionName: String)

    suspend fun getCurrentCaptureSessionEvent(): SessionCaptureEvent

    suspend fun loadEvents(sessionId: String): Flow<Event>

    suspend fun addEventToCurrentSession(event: Event)

    suspend fun addEvent(sessionId: String, event: Event)

    suspend fun countEventsToUpload(query: LocalEventQuery): Int

    suspend fun uploadEvents(query: LocalEventQuery): Flow<List<Event>>

    suspend fun countEventsToDownload(query: RemoteEventQuery): List<EventCount>

    suspend fun downloadEvents(scope: CoroutineScope, query: RemoteEventQuery): ReceiveChannel<List<Event>>

    suspend fun signOut()

    companion object {
        fun build(app: Application): EventRepository =
            app.component.getSessionEventsManager()
    }
}

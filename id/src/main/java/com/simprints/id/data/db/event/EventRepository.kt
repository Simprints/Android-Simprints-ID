package com.simprints.id.data.db.event

import com.simprints.id.Application
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent

interface EventRepository {

    suspend fun createSession(libSimprintsVersionName: String)

    suspend fun load(): List<Event>
    suspend fun getCurrentCaptureSessionEvent(): SessionCaptureEvent

    suspend fun updateCurrentSession(updateBlock: suspend (SessionCaptureEvent) -> Unit)
    suspend fun updateSession(sessionId: String, updateBlock: suspend (SessionCaptureEvent) -> Unit)

    suspend fun addEventToCurrentSession(event: Event)
    suspend fun addEvent(event: Event)

    suspend fun signOut()

    suspend fun uploadSessions()

    companion object {
        fun build(app: Application): EventRepository =
            app.component.getSessionEventsManager()
    }
}

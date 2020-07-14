package com.simprints.id.data.db.event

import com.simprints.id.Application
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.session.SessionCaptureEvent

interface EventRepository {

    suspend fun createSession(libSimprintsVersionName: String)
    suspend fun getCurrentSession(): SessionCaptureEvent
    suspend fun updateCurrentSession(updateBlock: suspend (SessionCaptureEvent) -> Unit)
    suspend fun updateSession(sessionId: String, updateBlock: suspend (SessionCaptureEvent) -> Unit)
    suspend fun load(): List<Event>

    suspend fun addEvent(event: Event)

    suspend fun signOut()

    suspend fun uploadSessions()

    companion object {
        fun build(app: Application): EventRepository =
            app.component.getSessionEventsManager()
    }
}

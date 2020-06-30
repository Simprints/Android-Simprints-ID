package com.simprints.id.data.db.event

import com.simprints.id.Application
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.session.SessionCaptureEvent

interface SessionRepository {

    suspend fun createSession(libSimprintsVersionName: String)
    suspend fun getCurrentSession(): SessionCaptureEvent
    suspend fun updateCurrentSession(updateBlock: (SessionCaptureEvent) -> Unit)
    suspend fun updateSession(sessionId: String, updateBlock: (SessionCaptureEvent) -> Unit)

    fun addEventToCurrentSessionInBackground(event: Event)

    suspend fun signOut()

    suspend fun uploadSessions()

    companion object {
        fun build(app: Application): SessionRepository =
            app.component.getSessionEventsManager()
    }
}

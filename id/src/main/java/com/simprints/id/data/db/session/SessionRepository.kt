package com.simprints.id.data.db.session

import com.simprints.id.Application
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.session.SessionEvents

interface SessionRepository {

    suspend fun createSession(libSimprintsVersionName: String)
    suspend fun getCurrentSession(): SessionEvents
    suspend fun updateCurrentSession(updateBlock: (SessionEvents) -> Unit)
    suspend fun updateSession(sessionId: String, updateBlock: (SessionEvents) -> Unit)

    fun addEventToCurrentSessionInBackground(event: Event)

    suspend fun signOut()

    suspend fun startUploadingSessions()

    companion object {
        fun build(app: Application): SessionRepository =
            app.component.getSessionEventsManager()
    }
}

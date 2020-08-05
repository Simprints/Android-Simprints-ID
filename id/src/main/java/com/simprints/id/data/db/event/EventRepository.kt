package com.simprints.id.data.db.event

import com.simprints.id.Application
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import kotlinx.coroutines.flow.Flow

data class OperationEventProgress(val progress: Int, val total: Int)
data class DownloadEventProgress(val lastEvent: Event, val progress: Int, val total: Int)

interface EventRepository {

    suspend fun createSession(libSimprintsVersionName: String)

    suspend fun getCurrentCaptureSessionEvent(): SessionCaptureEvent

    suspend fun loadEvents(sessionId: String): Flow<Event>

    suspend fun addEventToCurrentSession(event: Event)

    suspend fun addEvent(sessionId: String, event: Event)

    suspend fun uploadEvents(): Flow<OperationEventProgress>

    suspend fun downloadEvents(): Flow<DownloadEventProgress>

    suspend fun signOut()

    companion object {
        fun build(app: Application): EventRepository =
            app.component.getSessionEventsManager()
    }
}

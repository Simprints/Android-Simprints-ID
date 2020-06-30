package com.simprints.id.data.db.event.remote

import com.simprints.id.data.db.event.domain.events.session.SessionCaptureEvent

interface SessionRemoteDataSource {

    suspend fun uploadSessions(projectId: String,
                       sessions: List<SessionCaptureEvent>)
}

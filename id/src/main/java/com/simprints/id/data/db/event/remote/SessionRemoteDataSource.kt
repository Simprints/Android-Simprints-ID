package com.simprints.id.data.db.event.remote

import com.simprints.id.data.db.event.domain.events.session.SessionEvent

interface SessionRemoteDataSource {

    suspend fun uploadSessions(projectId: String,
                       sessions: List<SessionEvent>)
}

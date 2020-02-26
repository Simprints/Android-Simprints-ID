package com.simprints.id.data.db.session.remote

import com.simprints.id.data.db.session.domain.models.session.SessionEvents

interface SessionsRemoteDataSource {

    suspend fun uploadSessions(projectId: String,
                       sessions: List<SessionEvents>)
}

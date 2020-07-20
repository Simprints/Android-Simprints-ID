package com.simprints.id.data.db.event.remote

import com.simprints.id.data.db.event.domain.models.Event


interface SessionRemoteDataSource {

    suspend fun uploadSessions(projectId: String,
                               sessions: List<Event>)
}

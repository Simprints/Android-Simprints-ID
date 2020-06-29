package com.simprints.id.data.db.event.remote

import com.simprints.id.data.db.event.domain.events.session.SessionEvent
import com.simprints.id.data.db.event.remote.session.ApiSessionEvents
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.network.SimApiClient
import com.simprints.id.network.SimApiClientFactory

class SessionRemoteDataSourceImpl(
    private val simApiClientFactory: SimApiClientFactory
) : SessionRemoteDataSource {

    override suspend fun uploadSessions(projectId: String,
                                        sessions: List<SessionEvent>) {
        if (sessions.isEmpty()) {
            throw NoSessionsFoundException()
        }

        executeCall("uploadSessionsBatch") { sessionsRemoteInterface ->
            sessionsRemoteInterface.uploadSessions(
                projectId,
                hashMapOf("sessions" to sessions.map { ApiSessionEvents(it) }.toTypedArray())
            )
        }
    }

    private suspend fun <T> executeCall(nameCall: String, block: suspend (SessionsRemoteInterface) -> T): T =
        with(getSessionsApiClient()) {
            executeCall(nameCall) {
                block(it)
            }
        }

    suspend fun getSessionsApiClient(): SimApiClient<SessionsRemoteInterface> =
        simApiClientFactory.buildClient(SessionsRemoteInterface::class)
}

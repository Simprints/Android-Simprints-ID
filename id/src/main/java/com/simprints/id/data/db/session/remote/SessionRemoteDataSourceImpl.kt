package com.simprints.id.data.db.session.remote

import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.data.db.session.remote.session.ApiSessionEvents
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.tools.utils.retrySimNetworkCalls

class SessionRemoteDataSourceImpl(private val remoteDbManager: RemoteDbManager,
                                  private val simApiClientFactory: SimApiClientFactory) : SessionRemoteDataSource {

    override suspend fun uploadSessions(projectId: String,
                                        sessions: List<SessionEvents>) {
        if (sessions.isEmpty()) {
            throw NoSessionsFoundException()
        }
        makeNetworkRequest({ sessionsRemoteInterface ->
            sessionsRemoteInterface.uploadSessions(projectId, hashMapOf("sessions" to sessions.map { ApiSessionEvents(it) }.toTypedArray()))
        }, "uploadSessionsBatch")
    }

    private suspend fun <T> makeNetworkRequest(block: suspend (client: SessionsRemoteInterface) -> T, traceName: String): T =
        retrySimNetworkCalls(getSessionsApiClient(), block, traceName)

    internal suspend fun getSessionsApiClient(): SessionsRemoteInterface {
        val token = remoteDbManager.getCurrentToken()
        return simApiClientFactory.build<SessionsRemoteInterface>(token).api
    }
}

package com.simprints.id.data.analytics.eventdata.controllers.remote

import com.simprints.core.network.SimApiClient
import com.simprints.id.data.db.common.RemoteDbManager


open class RemoteSessionsManagerImpl(private val remoteDbManager: RemoteDbManager) : RemoteSessionsManager {

    override suspend fun getSessionsApiClient(): SessionsRemoteInterface {
        val token = remoteDbManager.getCurrentToken()
        return buildSessionsApi(token)
    }

    private fun buildSessionsApi(authToken: String): SessionsRemoteInterface =
        SimApiClient(SessionsRemoteInterface::class.java, SessionsRemoteInterface.baseUrl, authToken).api
}

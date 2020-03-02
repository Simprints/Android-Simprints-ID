package com.simprints.id.data.db.session.remote

import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.data.db.common.RemoteDbManager


open class RemoteSessionsManagerImpl(private val remoteDbManager: RemoteDbManager,
                                     private val simApiClientFactory: SimApiClientFactory) : RemoteSessionsManager {

    override suspend fun getSessionsApiClient(): SessionsRemoteInterface {
        val token = remoteDbManager.getCurrentToken()
        return simApiClientFactory.build<SessionsRemoteInterface>(token).api
    }
}

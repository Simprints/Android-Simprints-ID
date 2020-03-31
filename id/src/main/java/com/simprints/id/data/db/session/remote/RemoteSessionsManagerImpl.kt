package com.simprints.id.data.db.session.remote

import com.simprints.core.network.SimApiClientFactory
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.secure.BaseUrlProvider


open class RemoteSessionsManagerImpl(
    private val remoteDbManager: RemoteDbManager,
    private val simApiClientFactory: SimApiClientFactory,
    private val baseUrlProvider: BaseUrlProvider
) : RemoteSessionsManager {

    override suspend fun getSessionsApiClient(): SessionsRemoteInterface {
        val baseUrl = baseUrlProvider.getApiBaseUrl()
        val token = remoteDbManager.getCurrentToken()
        return simApiClientFactory.build<SessionsRemoteInterface>(baseUrl, token).api
    }
}

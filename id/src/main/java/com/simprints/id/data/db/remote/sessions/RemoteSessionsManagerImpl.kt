package com.simprints.id.data.db.remote.sessions

import com.simprints.core.network.SimApiClient
import com.simprints.id.data.analytics.eventdata.controllers.remote.SessionsRemoteInterface
import com.simprints.id.data.db.remote.RemoteDbManager
import io.reactivex.Single


open class RemoteSessionsManagerImpl(private val remoteDbManager: RemoteDbManager) : RemoteSessionsManager {

    override fun getSessionsApiClient(): Single<SessionsRemoteInterface> =
        remoteDbManager.getCurrentToken().flatMap {
            Single.just(buildSessionsApi(it))
        }

    private fun buildSessionsApi(authToken: String): SessionsRemoteInterface =
        SimApiClient(SessionsRemoteInterface::class.java, SessionsRemoteInterface.baseUrl, authToken).api
}

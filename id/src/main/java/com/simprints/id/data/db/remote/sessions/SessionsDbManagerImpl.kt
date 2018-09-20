package com.simprints.id.data.db.remote.sessions

import com.simprints.id.data.analytics.eventData.SessionsRemoteInterface
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.network.SimApiClient

import io.reactivex.Single

open class SessionsDbManagerImpl(private val remoteDbManager: RemoteDbManager) : SessionsDbManager {

    override fun getSessionsApiClient(): Single<SessionsRemoteInterface> =
        remoteDbManager.getCurrentFirestoreToken().flatMap {
            Single.just(buildSessionsApi(it))
        }

    private fun buildSessionsApi(authToken: String): SessionsRemoteInterface = SimApiClient(SessionsRemoteInterface::class.java, SessionsRemoteInterface.baseUrl, authToken).api
}

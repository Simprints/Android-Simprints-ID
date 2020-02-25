package com.simprints.id.data.db.session.remote


interface RemoteSessionsManager {

    suspend fun getSessionsApiClient(): SessionsRemoteInterface
}

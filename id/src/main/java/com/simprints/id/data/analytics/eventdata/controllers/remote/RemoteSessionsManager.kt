package com.simprints.id.data.analytics.eventdata.controllers.remote


interface RemoteSessionsManager {

    suspend fun getSessionsApiClient(): SessionsRemoteInterface
}

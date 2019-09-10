package com.simprints.id.data.analytics.eventdata.controllers.remote

import io.reactivex.Single


interface RemoteSessionsManager {

    fun getSessionsApiClient(): Single<SessionsRemoteInterface>
}

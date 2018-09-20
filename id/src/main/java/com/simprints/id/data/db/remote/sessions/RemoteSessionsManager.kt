package com.simprints.id.data.db.remote.sessions

import com.simprints.id.data.analytics.eventData.SessionsRemoteInterface
import io.reactivex.Single


interface RemoteSessionsManager {

    fun getSessionsApiClient(): Single<SessionsRemoteInterface>
}

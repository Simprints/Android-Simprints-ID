package com.simprints.id.data.analytics.eventData.controllers.remote

import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents
import com.simprints.core.network.NetworkConstants
import io.reactivex.Single
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface SessionsRemoteInterface {
    companion object {
        var baseUrl = NetworkConstants.baseUrl
    }

    @POST("projects/{projectId}/sessions")
    fun uploadSessions(@Path("projectId") projectId: String,
                       @Body sessionsJson: HashMap<String, Array<SessionEvents>>): Single<Result<Void?>>
}

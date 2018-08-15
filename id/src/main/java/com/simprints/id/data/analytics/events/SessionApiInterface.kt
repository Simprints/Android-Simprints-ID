package com.simprints.id.data.analytics.events

import com.simprints.id.data.analytics.events.models.SessionEvents
import io.reactivex.Single
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface SessionApiInterface {
    companion object {
        const val baseUrl = "https://eventapi-v0.simprints-dev.appspot.com/" //NetworkConstants.baseUrl
    }

    @POST("projects/{projectId}/sessions")
    fun uploadSessions(@Path("projectId") projectId: String,
                       @Body sessionsJson: ArrayList<SessionEvents>): Single<Result<Unit>>
}

package com.simprints.id.data.db.event.remote

import com.simprints.id.network.SimRemoteInterface
import com.simprints.id.data.db.event.remote.session.ApiSessionCapture
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface SessionsRemoteInterface : SimRemoteInterface {

    @POST("projects/{projectId}/sessions")
    suspend fun uploadSessions(@Path("projectId") projectId: String,
                               @Body sessionsJson: HashMap<String, Array<ApiSessionCapture>>)
}

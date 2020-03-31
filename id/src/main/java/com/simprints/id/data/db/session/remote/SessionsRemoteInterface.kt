package com.simprints.id.data.db.session.remote

import com.simprints.core.network.SimRemoteInterface
import com.simprints.id.data.db.session.remote.session.ApiSessionEvents
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface SessionsRemoteInterface : SimRemoteInterface {

    @POST("projects/{projectId}/sessions")
    suspend fun uploadSessions(@Path("projectId") projectId: String,
                               @Body sessionsJson: HashMap<String, Array<ApiSessionEvents>>)
}

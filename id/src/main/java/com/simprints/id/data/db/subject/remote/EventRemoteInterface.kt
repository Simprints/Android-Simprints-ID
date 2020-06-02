package com.simprints.id.data.db.subject.remote

import com.simprints.id.data.db.subject.remote.models.ApiModes
import com.simprints.id.data.db.subject.remote.models.subjectcounts.ApiEventCount
import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEvents
import com.simprints.id.network.SimRemoteInterface
import okhttp3.ResponseBody
import retrofit2.http.*


@JvmSuppressWildcards
interface EventRemoteInterface : SimRemoteInterface {

    @GET("projects/{projectId}/events/count")
    suspend fun countEvents(
        @Path("projectId") projectId: String,
        @Query("l_moduleId") moduleIds: List<String>?,
        @Query("l_attendantId") attendantId: String?,
        @Query("l_subjectId") subjectId: String?,
        @Query("l_mode") modes: List<ApiModes>,
        @Query("lastEventId") lastEventId: String?,
        @Query("type") eventType: List<String>): List<ApiEventCount>

    @POST("projects/{projectId}/events")
    suspend fun uploadEvents(
        @Path("projectId") projectId: String,
        @Body events: ApiEvents
    )

    @GET("projects/{projectId}/events")
    suspend fun downloadEvents(
        @Path("projectId") projectId: String,
        @Query("l_moduleId") moduleIds: List<String>?,
        @Query("l_attendantId") attendantId: String?,
        @Query("l_subjectId") subjectId: String?,
        @Query("l_mode") modes: List<ApiModes>,
        @Query("lastEventId") lastEventId: String?,
        @Query("type") eventType: List<String>): ResponseBody
}

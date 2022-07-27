package com.simprints.eventsystem.event.remote

import com.simprints.eventsystem.event.remote.models.ApiEventCount
import com.simprints.infra.network.SimRemoteInterface
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
        @Query("lastEventId") lastEventId: String?
    ): List<ApiEventCount>

    @POST("projects/{projectId}/events")
    suspend fun uploadEvents(
        @Path("projectId") projectId: String,
        @Query("acceptInvalidEvents") acceptInvalidEvents: Boolean = true,
        @Body events: ApiUploadEventsBody
    )

    @GET("projects/{projectId}/events")
    suspend fun downloadEvents(
        @Path("projectId") projectId: String,
        @Query("l_moduleId") moduleIds: List<String>?,
        @Query("l_attendantId") attendantId: String?,
        @Query("l_subjectId") subjectId: String?,
        @Query("l_mode") modes: List<ApiModes>,
        @Query("lastEventId") lastEventId: String?
    ): ResponseBody

    @POST("projects/{projectId}/dump")
    suspend fun dumpInvalidEvents(
        @Path("projectId") projectId: String,
        @Query("type") type: String = "CORRUPTED_EVENTS",
        @Body events: List<String>,
    )

}

package com.simprints.eventsystem.event.remote

import com.simprints.core.network.SimRemoteInterface
import com.simprints.eventsystem.event.remote.models.ApiEventCount
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType
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
        @Query("type") eventType: List<ApiEventPayloadType>): List<ApiEventCount>

    @POST("projects/{projectId}/events")
    suspend fun uploadEvents(
        @Path("projectId") projectId: String,
        @Body events: ApiUploadEventsBody
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
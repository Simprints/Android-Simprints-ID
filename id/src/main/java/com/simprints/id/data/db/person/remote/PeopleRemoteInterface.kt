package com.simprints.id.data.db.person.remote

import com.simprints.core.network.SimRemoteInterface
import com.simprints.id.data.db.person.remote.models.ApiGetPerson
import com.simprints.id.data.db.person.remote.models.ApiModes
import com.simprints.id.data.db.person.remote.models.ApiPostPerson
import com.simprints.id.data.db.person.remote.models.personcounts.ApiEventCounts
import com.simprints.id.data.db.person.remote.models.personevents.ApiEvents
import okhttp3.ResponseBody
import retrofit2.http.*


@JvmSuppressWildcards
interface PeopleRemoteInterface : SimRemoteInterface {

    @GET("projects/{projectId}/patients")
    @Streaming
    suspend fun downSync(
        @Path("projectId") projectId: String,
        @Query("userId") userId: String?,
        @Query("moduleId") moduleId: String?,
        @Query("lastKnownPatientId") lastKnownPatientId: String?,
        @Query("lastKnownPatientUpdatedAt") lastKnownPatientUpdatedAt: Long?,
        @Query("mode") modes: PipeSeparatorWrapperForURLListParam<ApiModes> = PipeSeparatorWrapperForURLListParam(ApiModes.FINGERPRINT)): ResponseBody

    @POST("projects/{projectId}/patients")
    suspend fun uploadPeople(@Path("projectId") projectId: String,
                             @Body patientsJson: HashMap<String, List<ApiPostPerson>>)

    @GET("projects/{projectId}/patients/{patientId}")
    suspend fun requestPerson(
        @Path("patientId") patientId: String,
        @Path("projectId") projectId: String): ApiGetPerson

    @GET("projects/{projectId}/events/count")
    suspend fun requestRecordCount(
        @Path("projectId") projectId: String,
        @Query("l_LabelKey") vararg labels: String?,
        @Query("lastEventId") lastEventId: String?,
        @Query("type") eventType: Array<String>): ApiEventCounts

    @POST("projects/{projectId}/events")
    suspend fun postEnrolmentRecordEvents(
        @Path("projectId") projectId: String,
        @Body events: ApiEvents
    )

    @GET("projects/{projectId}/events")
    suspend fun downloadEnrolmentEvents(
        @Path("projectId") projectId: String,
        @Query("type") eventType: Array<String>,
        @Query("l_LabelKey") vararg labels: String,
        @Query("lastEventId") lastEventId: String): ResponseBody
}

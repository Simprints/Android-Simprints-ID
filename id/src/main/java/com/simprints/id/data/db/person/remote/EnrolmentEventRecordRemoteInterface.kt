package com.simprints.id.data.db.person.remote

import com.simprints.core.network.SimRemoteInterface
import com.simprints.id.data.db.person.remote.models.ApiGetPerson
import com.simprints.id.data.db.person.remote.models.ApiModes
import com.simprints.id.data.db.person.remote.models.ApiPostPerson
import com.simprints.id.data.db.person.remote.models.peopleoperations.request.ApiPeopleOperations
import com.simprints.id.data.db.person.remote.models.peopleoperations.response.ApiPeopleOperationsResponse
import com.simprints.id.data.db.person.remote.models.personcounts.ApiEventCount
import com.simprints.id.data.db.person.remote.models.personevents.ApiEvents
import okhttp3.ResponseBody
import retrofit2.http.*


@JvmSuppressWildcards
interface EnrolmentEventRecordRemoteInterface : SimRemoteInterface {


    //TODO: To be removed once EventRemoteDataSource is used in PersonRepository
    @GET("projects/{projectId}/patients")
    @Streaming
    suspend fun downSync(
        @Path("projectId") projectId: String,
        @Query("userId") userId: String?,
        @Query("moduleId") moduleId: String?,
        @Query("lastKnownPatientId") lastKnownPatientId: String?,
        @Query("lastKnownPatientUpdatedAt") lastKnownPatientUpdatedAt: Long?,
        @Query("mode") modes: PipeSeparatorWrapperForURLListParam<ApiModes> = PipeSeparatorWrapperForURLListParam(ApiModes.FINGERPRINT)): ResponseBody

    //TODO: To be removed once EventRemoteDataSource is used in PersonRepository
    @POST("projects/{projectId}/patients")
    suspend fun uploadPeople(@Path("projectId") projectId: String,
                             @Body patientsJson: HashMap<String, List<ApiPostPerson>>)

    //TODO: To be removed once EventRemoteDataSource is used in PersonRepository
    @GET("projects/{projectId}/patients/{patientId}")
    suspend fun requestPerson(
        @Path("patientId") patientId: String,
        @Path("projectId") projectId: String): ApiGetPerson

    //TODO: To be removed once EventRemoteDataSource is used in PersonRepository
    @POST("projects/{projectId}/patient-operations/count")
    suspend fun requestPeopleOperations(
        @Path("projectId") projectId: String,
        @Body operationsJson: ApiPeopleOperations): ApiPeopleOperationsResponse

    @GET("projects/{projectId}/events/count")
    suspend fun requestRecordCount(
        @Path("projectId") projectId: String,
        @Query("l_moduleId") moduleIds: List<String>?,
        @Query("l_attendantId") attendantId: String?,
        @Query("l_subjectId") subjectId: String?,
        @Query("l_mode") modes: List<ApiModes>,
        @Query("lastEventId") lastEventId: String?,
        @Query("type") eventType: List<String>): List<ApiEventCount>

    @POST("projects/{projectId}/events")
    suspend fun postEnrolmentRecordEvents(
        @Path("projectId") projectId: String,
        @Body events: ApiEvents
    )

    @GET("projects/{projectId}/events")
    suspend fun downloadEnrolmentEvents(
        @Path("projectId") projectId: String,
        @Query("l_moduleId") moduleIds: List<String>?,
        @Query("l_attendantId") attendantId: String?,
        @Query("l_subjectId") subjectId: String?,
        @Query("l_mode") modes: List<ApiModes>,
        @Query("lastEventId") lastEventId: String?,
        @Query("type") eventType: List<String>): ResponseBody
}

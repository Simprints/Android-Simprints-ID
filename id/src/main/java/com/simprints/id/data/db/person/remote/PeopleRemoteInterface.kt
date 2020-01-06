package com.simprints.id.data.db.person.remote

import com.simprints.core.network.NetworkConstants
import com.simprints.id.data.db.person.remote.models.ApiGetPerson
import com.simprints.id.data.db.person.remote.models.ApiModes
import com.simprints.id.data.db.person.remote.models.ApiPostPerson
import com.simprints.id.data.db.person.remote.models.peopleoperations.request.ApiPeopleOperations
import com.simprints.id.data.db.person.remote.models.peopleoperations.response.ApiPeopleOperationsResponse
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.*

@JvmSuppressWildcards
interface PeopleRemoteInterface {

    companion object {
        var baseUrl = NetworkConstants.baseUrl
    }

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
    fun uploadPeople(@Path("projectId") projectId: String,
                     @Body patientsJson: HashMap<String, List<ApiPostPerson>>): Single<Result<Void?>>

    @GET("projects/{projectId}/patients/{patientId}")
    fun requestPerson(
        @Path("patientId") patientId: String,
        @Path("projectId") projectId: String): Single<Response<ApiGetPerson>>

    @POST("projects/{projectId}/patient-operations/count")
    fun requestPeopleOperations(
        @Path("projectId") projectId: String,
        @Body operationsJson: ApiPeopleOperations): Single<Response<ApiPeopleOperationsResponse>>
}

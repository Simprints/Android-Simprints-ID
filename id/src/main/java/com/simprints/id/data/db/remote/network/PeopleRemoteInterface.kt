package com.simprints.id.data.db.remote.network

import com.simprints.id.data.db.remote.models.ApiPerson
import com.simprints.id.data.db.remote.models.PeopleCount
import com.simprints.core.network.NetworkConstants
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.*

interface PeopleRemoteInterface {

    companion object {
        var baseUrl = NetworkConstants.baseUrl
    }

    @GET("projects/{projectId}/patients")
    @Streaming
    fun downSync(
        @Path("projectId") projectId: String,
        @Query("userId") userId: String?,
        @Query("moduleId") moduleId: String?,
        @Query("lastKnownPatientId") lastKnownPatientId: String?,
        @Query("lastKnownPatientUpdatedAt") lastKnownPatientUpdatedAt: Long?): Single<ResponseBody>

    @POST("projects/{projectId}/patients")
    fun uploadPeople(@Path("projectId") projectId: String,
                     @Body patientsJson: HashMap<String, List<ApiPerson>>): Single<Result<Void?>>

    @GET("projects/{projectId}/patients/{patientId}")
    fun requestPerson(
        @Path("patientId") patientId: String,
        @Path("projectId") projectId: String): Single<Response<ApiPerson>>

    @GET("projects/{projectId}/patients/count")
    fun requestPeopleCount(
        @Path("projectId") projectId: String,
        @Query("userId") userId: String?,
        @Query("moduleId") moduleId: String?): Single<Response<PeopleCount>>
}

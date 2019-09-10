package com.simprints.id.data.db.person.remote

import com.simprints.core.network.NetworkConstants
import com.simprints.id.data.db.person.remote.models.ApiGetPerson
import com.simprints.id.data.db.person.remote.models.ApiModes
import com.simprints.id.data.db.person.remote.models.ApiModes.FINGERPRINT
import com.simprints.id.data.db.person.remote.models.ApiPeopleCount
import com.simprints.id.data.db.person.remote.models.ApiPostPerson
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
    fun downSync(
        @Path("projectId") projectId: String,
        @Query("userId") userId: String?,
        @Query("moduleId") moduleId: String?,
        @Query("lastKnownPatientId") lastKnownPatientId: String?,
        @Query("lastKnownPatientUpdatedAt") lastKnownPatientUpdatedAt: Long?,
        @Query("mode") modes: PipeSeparatorWrapperForURLListParam<ApiModes> = PipeSeparatorWrapperForURLListParam(FINGERPRINT)): Single<ResponseBody>

    @POST("projects/{projectId}/patients")
    fun uploadPeople(@Path("projectId") projectId: String,
                     @Body patientsJson: HashMap<String, List<ApiPostPerson>>): Single<Result<Void?>>

    @GET("projects/{projectId}/patients/{patientId}")
    fun requestPerson(
        @Path("patientId") patientId: String,
        @Path("projectId") projectId: String,
        @Query("mode") modes: PipeSeparatorWrapperForURLListParam<ApiModes> = PipeSeparatorWrapperForURLListParam(FINGERPRINT)): Single<Response<ApiGetPerson>>

    @GET("projects/{projectId}/patients/count")
    fun requestPeopleCount(
        @Path("projectId") projectId: String,
        @Query("userId") userId: String?,
        @Query("moduleId") moduleId: PipeSeparatorWrapperForURLListParam<String>?, //moduleId = module1|module2|
        @Query("mode") modes: PipeSeparatorWrapperForURLListParam<ApiModes> = PipeSeparatorWrapperForURLListParam(FINGERPRINT)): Single<Response<List<ApiPeopleCount>>>
}

package com.simprints.id.data.db.remote.network

import com.simprints.id.BuildConfig
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.sync.models.PeopleCount
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.*

interface PeopleRemoteInterface {

    companion object {
        private const val apiVersion = "2018-2-0"
        var baseUrl = "https://$apiVersion-dot-sync-manager-dot-${BuildConfig.GCP_PROJECT}.appspot.com"
    }

    @GET("/patients")
    @Streaming
    fun downSync(
        @Query("projectId") projectId: String,
        @QueryMap(encoded = true) syncParams: DownSyncParams,
        @Query("batchSize") batchSize: Int = 5000): Single<ResponseBody>

    @POST("/patients")
    fun uploadPeople(@Body patientsJson: HashMap<String, ArrayList<fb_Person>>): Single<Result<Unit>>

    @GET("/patients/{projectId}/{patientId}")
    fun person(
        @Path("patientId") patientId: String,
        @Path("projectId") projectId: String): Single<Response<fb_Person>>

    @GET("/patient-counts")
    fun peopleCount(
        @QueryMap(encoded = true) syncParams: Map<String, String>): Single<Response<PeopleCount>>
}

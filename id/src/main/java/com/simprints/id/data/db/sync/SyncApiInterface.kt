package com.simprints.id.data.db.sync

import com.simprints.id.BuildConfig
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.sync.model.PatientsCount
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.*

interface SyncApiInterface {

    companion object {
        private const val apiVersion = "2018-1-0-dev3"
        var baseUrl = "https://$apiVersion-dot-sync-manager-dot-${BuildConfig.GCP_PROJECT}.appspot.com"
        private const val apiKey: String = "AIzaSyAORPo9YH-TBw0F1ch8BMP9IGkNElgon6s"
    }

    @GET("/patients")
    @Streaming
    fun downSync(
        @Query("lastSync") date: Long, //Date in ms
        @QueryMap(encoded = true) syncParams: Map<String, String>, //projectId, userId, moduleId
        @Query("batchSize") batchSize: Int = 5000,
        @Query("api_key") key: String = SyncApiInterface.apiKey): Single<ResponseBody>

    @POST("/patients")
    fun upSync(@Body patientsJson: String,
               @Query("api_key") key: String = SyncApiInterface.apiKey): Completable

    @GET("/patients")
    fun getPatient(
        @Query("patientId") patientId: String,
        @Query("api_key") key: String = SyncApiInterface.apiKey): Single<fb_Person>

    @GET("/patient-counts")
    fun patientsCount(
        @QueryMap(encoded = true) syncParams: Map<String, String>,
        @Query("api_key") key: String = SyncApiInterface.apiKey): Single<PatientsCount>
}

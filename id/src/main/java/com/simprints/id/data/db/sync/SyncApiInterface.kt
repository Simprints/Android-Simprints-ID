package com.simprints.id.data.db.sync

import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.sync.model.PatientsCount
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.*

interface SyncApiInterface {

    companion object {
        private const val apiVersion = "2018-1-0-dev0"
        var baseUrl = "https://$apiVersion-dot-sync-manager-dot-simprints-dev.appspot.com"
    }

    @GET("/patients")
    @Streaming
    fun downSync(
        @Query("lastSync") date: Long, //Date in ms
        @QueryMap(encoded = true) syncParams: Map<String, String>, //projectId, userId, moduleId
        @Query("batchSize") batchSize: Int = 5000): Single<ResponseBody>

    @POST("/patients")
    fun upSync(@Body patientsJson: String): Completable

    @GET("/patients")
    fun getPatient(
        @Query("patientId") patientId: String): Single<fb_Person>

    @GET("/patient-counts")
    fun patientsCount(
        @QueryMap(encoded = true) syncParams: Map<String, String>): Single<PatientsCount>
}

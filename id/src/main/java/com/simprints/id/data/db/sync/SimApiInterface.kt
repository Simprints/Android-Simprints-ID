package com.simprints.id.data.db.sync

import com.simprints.id.data.db.remote.models.fb_Person
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.*

interface SimApiInterface {

    companion object {
        private const val apiVersion = "2018-1-0-dev6"
        var baseUrl = "https://sync-manager-dot-simprints-dev.appspot.com"
        private const val apiKey: String = "AIzaSyAORPo9YH-TBw0F1ch8BMP9IGkNElgon6s"
    }

    @GET("/patients")
    @Streaming
    fun downSync(
        @Query("key") key: String,
        @Query("lastSync") date: Long, //Date in ms
        @QueryMap(encoded = true) syncParams: Map<String, String>,
        @Query("batchSize") batchSize: Int = 5000): Single<ResponseBody>

    @POST("/patients")
    fun upSync(@Query("key") key: String, @Body patientsJson: String): Completable

    @GET("/patients")
    fun getPatient(
        @Query("key") key: String,
        @Query("patientId") patientId: String): Single<fb_Person>

    @GET("/patient-counts")
    fun patientsCount(
        @Query("key") key: String,
        @QueryMap(encoded = true) syncParams: Map<String, String>): Single<Int>
}

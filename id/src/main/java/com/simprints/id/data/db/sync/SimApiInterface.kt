package com.simprints.id.data.db.sync

import com.simprints.id.libdata.models.firebase.fb_Person
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.*
import java.util.*

interface SimApiInterface {

    @GET("/patients")
    @Streaming
    fun downSync(
        @Query("key") key: String,
        @Query("lastSync") date: Date,
        @QueryMap(encoded = true) syncParams: Map<String, String>,
        @Query("batchSize") batchSize: Int = 5000): Single<ResponseBody>

    @POST("/patients")
    fun upSync(@Body patientsJson: String): Completable

    @GET("/patients")
    fun getPatient(
        @Query("key") key: String,
        @Query("patientId") patientId: String): Single<fb_Person>

    @GET("/patient-counts")
    fun patientsCount(
        @Query("key") key: String,
        @QueryMap(encoded = true) syncParams: Map<String, String>): Single<Int>
}

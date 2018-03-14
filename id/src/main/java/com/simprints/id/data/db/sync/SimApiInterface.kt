package com.simprints.id.data.db.sync

import com.simprints.id.libdata.models.firebase.fb_Person
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
        @Query("projectId") projectId: String,
        @Query("moduleId") moduleId: String? = null,
        @Query("userId") userId: String? = null,
        @Query("batchSize") batchSize: Int = 5000): Single<ResponseBody>

    @POST("/patients")
    fun upSync(@Body patientsJson: String): Single<Unit>

    @GET("/patients")
    fun getPatient(
        @Query("key") key: String,
        @Query("patientId") patientId: String): Single<fb_Person>

    @GET("/patient-counts")
    fun patientsCount(
        @Query("key") key: String,
        @Query("projectId") project: String,
        @Query("moduleId") moduleId: String? = null,
        @Query("userId") userId: String? = null): Single<Int>
}

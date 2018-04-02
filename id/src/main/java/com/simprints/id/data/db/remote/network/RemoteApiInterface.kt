package com.simprints.id.data.db.remote.network

import com.simprints.id.BuildConfig
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.sync.model.PeopleCount
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.*

interface RemoteApiInterface {

    companion object {
        private const val apiVersion = "2018-1-0-dev4"
        var baseUrl = "https://$apiVersion-dot-sync-manager-dot-${BuildConfig.GCP_PROJECT}.appspot.com"
    }

    @GET("/patients")
    @Streaming
    fun downSync(
        @Query("lastSync") date: Long, //Date in ms
        @QueryMap(encoded = true) syncParams: Map<String, String>, //projectId, userId, moduleId
        @Query("batchSize") batchSize: Int = 5000): Single<ResponseBody>

    @POST("/patients")
    fun uploadPeople(@Body patientsJson: HashMap<String, ArrayList<fb_Person>>): Completable

    @GET("/patients")
    fun downloadPeople(
        @Query("patientId") patientId: String,
        @Query("projectId") projectId: String): Single<ArrayList<fb_Person>>

    @GET("/patient-counts")
    fun peopleCount(
        @QueryMap(encoded = true) syncParams: Map<String, String>): Single<PeopleCount>
}

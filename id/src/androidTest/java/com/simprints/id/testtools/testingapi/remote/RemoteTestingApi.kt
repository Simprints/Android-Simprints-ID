package com.simprints.id.testtools.testingapi.remote

import com.simprints.id.BuildConfig
import com.simprints.id.network.SimRemoteInterface
import com.simprints.id.testtools.testingapi.models.*
import io.reactivex.Single
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.*

interface RemoteTestingApi : SimRemoteInterface {

    companion object {
        const val baseUrl = "https://dev.simprints-apis.com/androidapi/v2/"
        const val apiKey: String = BuildConfig.ANDROID_AUTH_API_KEY
    }

    @POST("projects")
    fun createProject(@Body testProjectCreationParameters: TestProjectCreationParameters,
                      @Query("key") key: String = apiKey): Single<TestProject>

    @DELETE("projects/{projectId}")
    fun deleteProject(@Path("projectId") projectId: String,
                      @Query("key") key: String = apiKey): Single<Result<Void?>>

    @POST("tokens")
    fun generateFirebaseToken(@Body testFirebaseTokenParameters: TestFirebaseTokenParameters,
                              @Query("key") key: String = apiKey): Single<TestFirebaseToken>

    @GET("projects/{projectId}/events/count")
    fun getEventCount(@Path("projectId") projectId: String,
                      @Query("key") key: String = apiKey): Single<TestEventCount>
}

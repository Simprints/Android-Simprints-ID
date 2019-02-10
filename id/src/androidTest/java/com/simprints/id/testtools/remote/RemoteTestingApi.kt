package com.simprints.id.testtools.remote

import com.simprints.id.BuildConfig
import com.simprints.id.testtools.models.*
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.*

interface RemoteTestingApi {

    companion object {
        const val baseUrl = "https://androidtestapi-v1-dot-simprints-dev.appspot.com/"
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

    @GET("projects/{projectId}/sessions")
    fun getSessionSignatures(@Path("projectId") projectId: String,
                             @Query("key") key: String = apiKey): Observable<TestSessionSignature>

    @GET("projects/{projectId}/sessions/count")
    fun getSessionCount(@Path("projectId") projectId: String,
                        @Query("key") key: String = apiKey): Single<TestSessionCount>
}

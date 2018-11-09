package com.simprints.id.testTools.remote

import com.simprints.id.testTools.models.*
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.*

interface RemoteTestingInterface {

    companion object {
        var baseUrl = "https://androidapitest-v1-dot-simprints-dev.appspot.com/"
    }

    @POST("projects")
    fun createProject(@Body testProjectCreationParameters: TestProjectCreationParameters): Single<TestProject>

    @DELETE("projects/{projectId}")
    fun deleteProject(@Path("projectId") projectId: String): Single<Result<Void?>>

    @POST("tokens")
    fun getFirebaseToken(@Body testFirebaseTokenParameters: TestFirebaseTokenParameters): Single<TestFirebaseToken>

    @GET("projects/{projectId}/sessions")
    fun getSessionSignatures(@Path("projectId") projectId: String): Observable<TestSessionSignature>

    @GET("projects/{projectId}/sessions/count")
    fun getSessionCount(@Path("projectId") projectId: String): Single<TestSessionCount>
}

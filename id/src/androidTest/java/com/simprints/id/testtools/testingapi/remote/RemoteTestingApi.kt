package com.simprints.id.testtools.testingapi.remote

import com.simprints.id.testtools.testingapi.models.*
import com.simprints.infra.network.SimRemoteInterface
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RemoteTestingApi : SimRemoteInterface {

    companion object {
        const val baseUrl = "https://dev.simprints-apis.com/androidapi/v2/"
    }

    @POST("projects")
    fun createProject(@Body testProjectCreationParameters: TestProjectCreationParameters): Single<TestProject>

    @POST("tokens")
    fun generateFirebaseToken(@Body testFirebaseTokenParameters: TestFirebaseTokenParameters): Single<TestFirebaseToken>

    @GET("projects/{projectId}/events/count")
    fun getEventCount(@Path("projectId") projectId: String): Single<TestEventCount>
}

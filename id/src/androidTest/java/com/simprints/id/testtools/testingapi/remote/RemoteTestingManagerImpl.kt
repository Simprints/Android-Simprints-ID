package com.simprints.id.testtools.testingapi.remote

import android.content.Context
import com.simprints.id.testtools.testingapi.exceptions.TestingRemoteApiError
import com.simprints.id.testtools.testingapi.models.*
import com.simprints.infra.network.apiclient.SimApiClientImpl
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * This class wraps [RemoteTestingApi] and makes all the calls to the cloud blocking.
 */
class RemoteTestingManagerImpl(ctx: Context) : RemoteTestingManager {

    private val remoteTestingApi = SimApiClientImpl(
        RemoteTestingApi::class,
        ctx,
        RemoteTestingApi.baseUrl,
        "",
        "Test",
        null,
    )

    override suspend fun createTestProject(testProjectCreationParameters: TestProjectCreationParameters): TestProject =
        remoteTestingApi.executeCall { it.createProject(testProjectCreationParameters) }
            .blockingGetOnDifferentThread { TestingRemoteApiError("Failed to create project", it) }

    override suspend fun generateFirebaseToken(
        projectId: String,
        userId: String
    ): TestFirebaseToken =
        generateFirebaseToken(TestFirebaseTokenParameters(projectId = projectId, userId = userId))

    override suspend fun generateFirebaseToken(testFirebaseTokenParameters: TestFirebaseTokenParameters): TestFirebaseToken =
        remoteTestingApi.executeCall { it.generateFirebaseToken(testFirebaseTokenParameters) }
            .blockingGetOnDifferentThread {
                TestingRemoteApiError(
                    "Failed to build firebase token",
                    it
                )
            }

    override suspend fun getEventCount(projectId: String): TestEventCount =
        remoteTestingApi.executeCall { it.getEventCount(projectId) }
            .blockingGetOnDifferentThread { TestingRemoteApiError("Failed to build session count") }

    private inline fun <reified T> Single<T>.blockingGetOnDifferentThread(wrapError: (Throwable) -> Throwable): T =
        try {
            this.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .blockingGet()
        } catch (e: Throwable) {
            e.printStackTrace()
            throw wrapError(e)
        }
}

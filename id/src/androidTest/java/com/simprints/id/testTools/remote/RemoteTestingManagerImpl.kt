package com.simprints.id.testTools.remote

import com.simprints.id.testTools.exceptions.TestingRemoteApiError
import com.simprints.id.testTools.models.*
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * This class wraps [RemoteTestingApi] and makes all the calls to the cloud blocking.
 */
class RemoteTestingManagerImpl : RemoteTestingManager {

    private val remoteTestingApi = TestingApiClient(
        RemoteTestingApi::class.java,
        RemoteTestingApi.baseUrl)
        .api

    override fun createTestProject(): TestProject =
        createTestProject(TestProjectCreationParameters())

    override fun createTestProject(testProjectCreationParameters: TestProjectCreationParameters): TestProject =
        remoteTestingApi.createProject(testProjectCreationParameters)
            .blockingGetOnDifferentThread { TestingRemoteApiError("Failed to create project", it) }

    override fun deleteTestProject(projectId: String) {
        remoteTestingApi.deleteProject(projectId)
            .blockingGetOnDifferentThread { TestingRemoteApiError("Failed to delete project", it) }
    }

    override fun getFirebaseToken(projectId: String, userId: String): TestFirebaseToken =
        getFirebaseToken(TestFirebaseTokenParameters(projectId = projectId, userId = userId))

    override fun getFirebaseToken(testFirebaseTokenParameters: TestFirebaseTokenParameters): TestFirebaseToken =
        remoteTestingApi.getFirebaseToken(testFirebaseTokenParameters)
            .blockingGetOnDifferentThread { TestingRemoteApiError("Failed to get firebase token", it) }

    override fun getSessionSignatures(projectId: String): List<TestSessionSignature> =
        remoteTestingApi.getSessionSignatures(projectId)
            .toList()
            .blockingGetOnDifferentThread { TestingRemoteApiError("Failed to get session signatures", it) }

    override fun getSessionCount(projectId: String): TestSessionCount =
        remoteTestingApi.getSessionCount(projectId)
            .blockingGetOnDifferentThread { TestingRemoteApiError("Failed to get session count") }

    private inline fun <reified T> Single<T>.blockingGetOnDifferentThread(wrapError: (Throwable) -> Throwable): T =
        try {
            this.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .blockingGet()
        } catch (e: Throwable) {
            throw wrapError(e)
        }
}

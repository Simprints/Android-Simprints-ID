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

    companion object {
        private const val RETRY_ATTEMPTS = 3L
    }

    private val remoteTestingApi = TestingApiClient(
        RemoteTestingApi::class.java,
        RemoteTestingApi.baseUrl)
        .api

    override fun createTestProject(testProjectCreationParameters: TestProjectCreationParameters): TestProject =
        remoteTestingApi.createProject(testProjectCreationParameters)
            .retry(RETRY_ATTEMPTS)
            .blockingGetOnDifferentThread { TestingRemoteApiError("Failed to create project", it) }

    override fun deleteTestProject(projectId: String) {
        remoteTestingApi.deleteProject(projectId)
            .retry(RETRY_ATTEMPTS)
            .blockingGetOnDifferentThread { TestingRemoteApiError("Failed to delete project", it) }
    }

    override fun generateFirebaseToken(projectId: String, userId: String): TestFirebaseToken =
        generateFirebaseToken(TestFirebaseTokenParameters(projectId = projectId, userId = userId))

    override fun generateFirebaseToken(testFirebaseTokenParameters: TestFirebaseTokenParameters): TestFirebaseToken =
        remoteTestingApi.generateFirebaseToken(testFirebaseTokenParameters)
            .retry(RETRY_ATTEMPTS)
            .blockingGetOnDifferentThread { TestingRemoteApiError("Failed to get firebase token", it) }

    override fun getSessionSignatures(projectId: String): List<TestSessionSignature> =
        remoteTestingApi.getSessionSignatures(projectId)
            .retry(RETRY_ATTEMPTS)
            .toList()
            .blockingGetOnDifferentThread { TestingRemoteApiError("Failed to get session signatures", it) }

    override fun getSessionCount(projectId: String): TestSessionCount =
        remoteTestingApi.getSessionCount(projectId)
            .retry(RETRY_ATTEMPTS)
            .blockingGetOnDifferentThread { TestingRemoteApiError("Failed to get session count") }

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

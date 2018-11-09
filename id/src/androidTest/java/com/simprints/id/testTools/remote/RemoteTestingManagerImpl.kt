package com.simprints.id.testTools.remote

import com.simprints.id.network.SimApiClient
import com.simprints.id.testTools.models.*
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Makes all the calls to the cloud blocking
 */
class RemoteTestingManagerImpl : RemoteTestingManager {

    private val remoteTestingApi = SimApiClient(
        RemoteTestingApi::class.java,
        RemoteTestingApi.baseUrl)
        .api

    override fun createTestProject(projectId: String, userId: String): TestProject =
        createTestProject(TestProjectCreationParameters(projectId, userId))

    override fun createTestProject(testProjectCreationParameters: TestProjectCreationParameters): TestProject =
        remoteTestingApi.createProject(testProjectCreationParameters)
            .blockingGetOnDifferentThread()

    override fun deleteTestProject(projectId: String) {
        remoteTestingApi.deleteProject(projectId)
            .blockingGetOnDifferentThread()
    }

    override fun getFirebaseToken(projectId: String, userId: String): TestFirebaseToken =
        getFirebaseToken(TestFirebaseTokenParameters(projectId, userId))

    override fun getFirebaseToken(testFirebaseTokenParameters: TestFirebaseTokenParameters): TestFirebaseToken =
        remoteTestingApi.getFirebaseToken(testFirebaseTokenParameters)
            .blockingGetOnDifferentThread()

    override fun getSessionSignatures(projectId: String): List<TestSessionSignature> =
        remoteTestingApi.getSessionSignatures(projectId)
            .toList()
            .blockingGetOnDifferentThread()

    override fun getSessionCount(projectId: String): TestSessionCount =
        remoteTestingApi.getSessionCount(projectId)
            .blockingGetOnDifferentThread()

    private inline fun <reified T> Single<T>.blockingGetOnDifferentThread() =
        this.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .blockingGet()
}

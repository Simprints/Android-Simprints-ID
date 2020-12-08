package com.simprints.id.testtools.testingapi.remote

import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType.*
import com.simprints.id.testtools.testingapi.exceptions.TestingRemoteApiError
import com.simprints.id.testtools.testingapi.models.*
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
        RemoteTestingApi::class,
        RemoteTestingApi.baseUrl,
        JsonHelper()).api

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
            .blockingGetOnDifferentThread { TestingRemoteApiError("Failed to build firebase token", it) }

    override fun getEventCount(projectId: String): TestEventCount =
        remoteTestingApi.getEventCount(projectId)
            .retry(RETRY_ATTEMPTS)
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


    // Never invoked, but used to enforce that every test class has a test
    fun enforceThatAnyTestHasATest() {
        val type: ApiEventPayloadType? = null
        when (type) {
            EnrolmentRecordCreation, EnrolmentRecordDeletion, EnrolmentRecordMove, Callout, Callback, ArtificialTermination,
            Authentication, Consent, Enrolment, Authorization, FingerprintCapture, OneToOneMatch,
            OneToManyMatch, PersonCreation, AlertScreen, GuidSelection, ConnectivitySnapshot, Refusal, CandidateRead,
            ScannerConnection, Vero2InfoSnapshot, ScannerFirmwareUpdate, InvalidIntent, SuspiciousIntent, IntentParsing,
            CompletionCheck, SessionCapture, FaceOnboardingComplete, FaceFallbackCapture, FaceCapture,
            FaceCaptureConfirmation, FaceCaptureRetry,
            null -> {
                // ADD TEST FOR NEW EVENT IN THIS CLASS
            }
        }
    }
}

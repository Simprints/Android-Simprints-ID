package com.simprints.id.testtools.testingapi.remote

import android.content.Context
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.*
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


    // Never invoked, but used to enforce that every test class has a test
    fun enforceThatAnyTestHasATest() {
        val type: ApiEventPayloadType? = null
        when (type) {
            Callout, Callback, ArtificialTermination,
            Authentication, Consent, Enrolment, Authorization, FingerprintCapture, OneToOneMatch,
            OneToManyMatch, PersonCreation, AlertScreen, GuidSelection, ConnectivitySnapshot, Refusal, CandidateRead,
            ScannerConnection, Vero2InfoSnapshot, ScannerFirmwareUpdate, InvalidIntent, SuspiciousIntent, IntentParsing,
            CompletionCheck, SessionCapture, FaceOnboardingComplete, FaceFallbackCapture, FaceCapture,
            FaceCaptureConfirmation, FingerprintCaptureBiometrics, FaceCaptureBiometrics,
            null -> {
                // ADD TEST FOR NEW EVENT IN THIS CLASS
            }
        }
    }
}

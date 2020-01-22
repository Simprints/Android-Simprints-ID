package com.simprints.clientapi.activities.baserequest

import com.google.common.truth.Truth
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.clientrequests.builders.ClientRequestBuilder
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.domain.requests.EnrollRequest
import com.simprints.clientapi.domain.responses.*
import com.simprints.clientapi.exceptions.RootedDeviceException
import com.simprints.clientapi.tools.DeviceManager
import com.simprints.testtools.common.syntax.*
import com.simprints.testtools.unit.BaseUnitTestConfig
import io.reactivex.Completable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class RequestPresenterTest {

    private val projectIdField = "some_project_id"
    private val moduleIdField = "some_module_id"
    private val userIdField = "some_user_id"
    private val metadataField = "some_metadata"
    private val extraField = mapOf("extraField" to "someExtraField")

    @Before
    fun setUp() {
        BaseUnitTestConfig()
            .rescheduleRxMainThread()
            .coroutinesMainThread()
    }

    @Test
    fun givenAnIntentWithExtraKeys_validateAndSendRequest_suspiciousIntentEventShouldBeAdded() {
        val clientApiSessionEventsManagerMock = mock<ClientApiSessionEventsManager>().apply {
            whenever(this) { addSuspiciousIntentEvent(anyNotNull()) } thenReturn Completable.complete()
        }
        val requestBuilder = mock<ClientRequestBuilder>().apply {
            whenever(this) { build() } thenReturn EnrollRequest(projectIdField, moduleIdField, userIdField, metadataField, extraField)
        }

        val presenter = ImplRequestPresenter(mock(), clientApiSessionEventsManagerMock, mock(), mock())
        presenter.validateAndSendRequest(requestBuilder)

        verifyOnce(clientApiSessionEventsManagerMock) {
            addSuspiciousIntentEvent(argThat {
                Truth.assertThat(it).isEqualTo(extraField)
            })
        }
    }

    @Test
    fun givenAnIntentWithNoExtraKeys_validateAndSendRequest_suspiciousIntentEventShouldNotBeAdded() {
        val clientApiSessionEventsManagerMock: ClientApiSessionEventsManager = mock()
        val requestBuilder = mock<ClientRequestBuilder>().apply {
            whenever(this) { build() } thenReturn EnrollRequest(projectIdField, moduleIdField, userIdField, metadataField, emptyMap())
        }

        val presenter = ImplRequestPresenter(mock(), clientApiSessionEventsManagerMock, mock(), mock())
        presenter.validateAndSendRequest(requestBuilder)

        verifyNever(clientApiSessionEventsManagerMock) { addSuspiciousIntentEvent(anyNotNull()) }
    }

    @Test
    fun withRootedDevice_shouldLogException() = runBlockingTest {
        val mockDeviceManager = mock<DeviceManager>()
        val exception = RootedDeviceException()
        whenever(mockDeviceManager) { checkIfDeviceIsRooted() } thenThrow exception
        val mockCrashReportManager = mock<ClientApiCrashReportManager>()
        val presenter = ImplRequestPresenter(
            mock(),
            mock(),
            mockDeviceManager,
            mockCrashReportManager
        )

        presenter.start()

        verifyOnce(mockCrashReportManager) {
            logExceptionOrSafeException(exception)
        }
    }

    @Test
    fun withRootedDevice_shouldShowAlertScreen() = runBlockingTest {
        val mockDeviceManager = mock<DeviceManager>()
        whenever(mockDeviceManager) { checkIfDeviceIsRooted() } thenThrow RootedDeviceException()
        val mockView = mock<RequestContract.RequestView>()
        val presenter = ImplRequestPresenter(mockView, mock(), mockDeviceManager, mock())

        presenter.start()

        verifyOnce(mockView) {
            handleClientRequestError(ClientApiAlert.ROOTED_DEVICE)
        }
    }

}

class ImplRequestPresenter(
    view: RequestContract.RequestView,
    clientApiSessionEventsManager: ClientApiSessionEventsManager,
    deviceManager: DeviceManager,
    crashReportManager: ClientApiCrashReportManager
) : RequestPresenter(view, clientApiSessionEventsManager, deviceManager, crashReportManager) {

    override suspend fun processRequest() {}
    override fun handleEnrollResponse(enroll: EnrollResponse) {}
    override fun handleIdentifyResponse(identify: IdentifyResponse) {}
    override fun handleVerifyResponse(verify: VerifyResponse) {}
    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) {}
    override fun handleConfirmationResponse(response: ConfirmationResponse) {}
    override fun handleResponseError(errorResponse: ErrorResponse) {}

}

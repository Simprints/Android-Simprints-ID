package com.simprints.clientapi.activities.baserequest

import com.google.common.truth.Truth
import com.simprints.clientapi.clientrequests.builders.ClientRequestBuilder
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.domain.requests.EnrollRequest
import com.simprints.clientapi.domain.responses.*
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

        val presenter = ImplRequestPresenter(mock(), clientApiSessionEventsManagerMock, mock())
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

        val presenter = ImplRequestPresenter(mock(), clientApiSessionEventsManagerMock, mock())
        presenter.validateAndSendRequest(requestBuilder)

        verifyNever(clientApiSessionEventsManagerMock) { addSuspiciousIntentEvent(anyNotNull()) }
    }

    @Test
    fun givenARootedDevice_shouldShowToast() = runBlockingTest {
        val mockView = mock<RequestContract.RequestView>()
        val mockDeviceManager = mock<DeviceManager>()
        val presenter = ImplRequestPresenter(mockView, mock(), mockDeviceManager)

        whenever { mockDeviceManager.isDeviceRooted() } thenReturn true

        presenter.start()

        verifyOnce(mockView) { handleRootedDevice() }
    }

    @Test
    fun givenANonRootedDevice_shouldNotShowToast() = runBlockingTest {
        val mockView = mock<RequestContract.RequestView>()
        val mockDeviceManager = mock<DeviceManager>()
        val presenter = ImplRequestPresenter(mockView, mock(), mockDeviceManager)

        whenever { mockDeviceManager.isDeviceRooted() } thenReturn false

        presenter.start()

        verifyNever(mockView) { handleRootedDevice() }
    }
}

class ImplRequestPresenter(
    view: RequestContract.RequestView,
    clientApiSessionEventsManager: ClientApiSessionEventsManager,
    deviceManager: DeviceManager
) : RequestPresenter(view, clientApiSessionEventsManager, deviceManager) {

    override fun handleEnrollResponse(enroll: EnrollResponse) {}
    override fun handleIdentifyResponse(identify: IdentifyResponse) {}
    override fun handleVerifyResponse(verify: VerifyResponse) {}
    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) {}
    override fun handleConfirmationResponse(response: ConfirmationResponse) {}
    override fun handleResponseError(errorResponse: ErrorResponse) {}

}

package com.simprints.clientapi.activities.baserequest

import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.clientrequests.builders.ClientRequestBuilder
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.domain.requests.EnrolRequest
import com.simprints.clientapi.domain.responses.*
import com.simprints.clientapi.exceptions.RootedDeviceException
import com.simprints.clientapi.tools.DeviceManager
import com.simprints.testtools.unit.BaseUnitTestConfig
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class RequestPresenterTest {

    private val projectIdField = "some_project_id"
    private val moduleIdField = "some_module_id"
    private val userIdField = "some_user_id"
    private val metadataField = "{\"key\": \"some_metadata\"}"
    private val extraField = mapOf("extraField" to "someExtraField")

    lateinit var clientApiSessionEventsManagerMock: ClientApiSessionEventsManager


    @Before
    fun setUp() {
        BaseUnitTestConfig()
            .coroutinesMainThread()

        clientApiSessionEventsManagerMock = mockk(relaxed = true)
    }

    @Test
    fun givenAnIntentWithExtraKeys_validateAndSendRequest_suspiciousIntentEventShouldBeAdded() {
        runBlockingTest {
            val requestBuilder = mockk<ClientRequestBuilder>(relaxed = true).apply {
                every { this@apply.build() } returns EnrolRequest(projectIdField, userIdField, moduleIdField, metadataField, extraField)
            }

            val presenter = ImplRequestPresenter(
                mockk(relaxed = true),
                clientApiSessionEventsManagerMock,
                mockk(relaxed = true)
            )
            presenter.validateAndSendRequest(requestBuilder)

            coVerify(exactly = 1) {
                clientApiSessionEventsManagerMock.addSuspiciousIntentEvent(extraField)
            }
        }
    }

    @Test
    fun givenAnIntentWithNoExtraKeys_validateAndSendRequest_suspiciousIntentEventShouldNotBeAdded() {
        runBlockingTest {
            val requestBuilder = mockk<ClientRequestBuilder>().apply {
                every { this@apply.build() } returns EnrolRequest(projectIdField, userIdField, moduleIdField, metadataField, emptyMap())
            }

            val presenter = ImplRequestPresenter(
                mockk(relaxed = true),
                clientApiSessionEventsManagerMock,
                mockk(relaxed = true)
            )
            presenter.validateAndSendRequest(requestBuilder)

            coVerify(exactly = 0) {
                clientApiSessionEventsManagerMock.addSuspiciousIntentEvent(any())
            }
        }
    }

    @Test
    fun withRootedDevice_shouldLogException() = runBlockingTest {
        val mockDeviceManager = mockk<DeviceManager>(relaxed = true)
        val exception = RootedDeviceException()
        every { mockDeviceManager.checkIfDeviceIsRooted() } throws exception
        val presSpy = spyk(ImplRequestPresenter(
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockDeviceManager
        ))
        val presenter = ImplRequestPresenter(
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockDeviceManager
        )

        val mockPresenter = mockk<RequestPresenter>(relaxed = true)
        mockPresenter.start()

        presenter.start()

        verify(exactly = 1) {
            presSpy
        }
    }

    @Test
    fun withRootedDevice_shouldShowAlertScreen() = runBlockingTest {
        val mockDeviceManager = mockk<DeviceManager>(relaxed = true)
        every { mockDeviceManager.checkIfDeviceIsRooted() } throws RootedDeviceException()
        val mockView = mockk<RequestContract.RequestView>(relaxed = true)
        val presenter = ImplRequestPresenter(mockView, mockk(relaxed = true), mockDeviceManager)

        presenter.start()

        verify { mockView.handleClientRequestError(ClientApiAlert.ROOTED_DEVICE) }
    }

}

class ImplRequestPresenter(
    view: RequestContract.RequestView,
    clientApiSessionEventsManager: ClientApiSessionEventsManager,
    deviceManager: DeviceManager
) : RequestPresenter(view, clientApiSessionEventsManager, deviceManager) {

    override suspend fun start() {
        runIfDeviceIsNotRooted {}
    }

    override fun handleEnrolResponse(enrol: EnrolResponse) {}
    override fun handleIdentifyResponse(identify: IdentifyResponse) {}
    override fun handleVerifyResponse(verify: VerifyResponse) {}
    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) {}
    override fun handleConfirmationResponse(response: ConfirmationResponse) {}
    override fun handleResponseError(errorResponse: ErrorResponse) {}

}

package com.simprints.clientapi.activities.baserequest

import com.simprints.clientapi.clientrequests.builders.ClientRequestBuilder
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.domain.requests.EnrolRequest
import com.simprints.clientapi.domain.responses.ConfirmationResponse
import com.simprints.clientapi.domain.responses.EnrolResponse
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.clientapi.domain.responses.IdentifyResponse
import com.simprints.clientapi.domain.responses.RefusalFormResponse
import com.simprints.clientapi.domain.responses.VerifyResponse
import com.simprints.clientapi.errors.ClientApiAlert
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.events.sampledata.createIdentificationCalloutEvent
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.exceptions.RootedDeviceException
import com.simprints.testtools.unit.BaseUnitTestConfig
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RequestPresenterTest {

    private val projectIdField = "some_project_id"
    private val moduleIdField = "some_module_id".asTokenizableRaw()
    private val userIdField = "some_user_id".asTokenizableRaw()
    private val metadataField = "{\"key\": \"some_metadata\"}"
    private val extraField = mapOf("extraField" to "someExtraField")

    private lateinit var clientApiSessionEventsManagerMock: ClientApiSessionEventsManager


    @Before
    fun setUp() {
        BaseUnitTestConfig()
            .coroutinesMainThread()

        clientApiSessionEventsManagerMock = mockk(relaxed = true)
    }

    @Test
    fun givenAnIntentWithExtraKeys_validateAndSendRequest_suspiciousIntentEventShouldBeAdded() {
        runTest {
            val requestBuilder = mockk<ClientRequestBuilder>(relaxed = true).apply {
                every { this@apply.build() } returns EnrolRequest(
                    projectIdField,
                    userIdField,
                    moduleIdField,
                    metadataField,
                    extraField
                )
            }

            val presenter = ImplRequestPresenter(
                view = mockk(relaxed = true),
                clientApiSessionEventsManager = clientApiSessionEventsManagerMock,
                rootManager = mockk(relaxed = true),
                configManager = mockk(relaxed = true),
                sessionEventsManager = mockk(relaxed = true),
                tokenizationProcessor = mockk(relaxed = true)
            )
            presenter.validateAndSendRequest(requestBuilder)

            coVerify(exactly = 1) {
                clientApiSessionEventsManagerMock.addSuspiciousIntentEvent(extraField)
            }
        }
    }

    @Test
    fun givenAnIntentWithNoExtraKeys_validateAndSendRequest_suspiciousIntentEventShouldNotBeAdded() {
        runTest {
            val requestBuilder = mockk<ClientRequestBuilder>().apply {
                every { this@apply.build() } returns EnrolRequest(
                    projectIdField,
                    userIdField,
                    moduleIdField,
                    metadataField,
                    emptyMap()
                )
            }

            val presenter = ImplRequestPresenter(
                view = mockk(relaxed = true),
                clientApiSessionEventsManager = clientApiSessionEventsManagerMock,
                rootManager = mockk(relaxed = true),
                configManager = mockk(relaxed = true),
                sessionEventsManager = mockk(relaxed = true),
                tokenizationProcessor = mockk(relaxed = true)
            )
            presenter.validateAndSendRequest(requestBuilder)

            coVerify(exactly = 0) {
                clientApiSessionEventsManagerMock.addSuspiciousIntentEvent(any())
            }
        }
    }

    @Test
    fun withRootedDevice_shouldShowAlertScreen() = runTest {
        val mockDeviceManager = mockk<SecurityManager>(relaxed = true)
        every { mockDeviceManager.checkIfDeviceIsRooted() } throws RootedDeviceException()
        val mockView = mockk<RequestContract.RequestView>(relaxed = true)
        val presenter = ImplRequestPresenter(
            view = mockView,
            clientApiSessionEventsManager = mockk(relaxed = true),
            rootManager = mockDeviceManager,
            configManager = mockk(relaxed = true),
            sessionEventsManager = mockk(relaxed = true),
            tokenizationProcessor = mockk(relaxed = true)
        )

        presenter.start()

        verify { mockView.handleClientRequestError(ClientApiAlert.ROOTED_DEVICE) }
    }

    @Test
    fun `when decryptTokenizedFields is called, tokenized fields of the event are decrypted`() {
        val mockDeviceManager = mockk<SecurityManager>(relaxed = true)
        every { mockDeviceManager.checkIfDeviceIsRooted() } throws RootedDeviceException()
        val mockView = mockk<RequestContract.RequestView>(relaxed = true)
        val tokenizationProcessor = mockk<TokenizationProcessor>(relaxed = true)
        val presenter = ImplRequestPresenter(
            view = mockView,
            clientApiSessionEventsManager = mockk(relaxed = true),
            rootManager = mockDeviceManager,
            configManager = mockk(relaxed = true),
            sessionEventsManager = mockk(relaxed = true),
            tokenizationProcessor = tokenizationProcessor
        )
        val userId = "userId".asTokenizableEncrypted()
        val moduleId = "moduleId".asTokenizableRaw()
        val event = createIdentificationCalloutEvent().let {
            it.copy(payload = it.payload.copy(userId = userId, moduleId = moduleId))
        }


        presenter.decryptTokenizedFields(events = listOf(event), project = mockk()).first()

        verify {
            tokenizationProcessor.decrypt(
                encrypted = userId,
                tokenKeyType = TokenKeyType.AttendantId,
                project = any()
            )
        }
        verify(exactly = 0) {
            tokenizationProcessor.decrypt(
                encrypted = moduleId.value.asTokenizableEncrypted(),
                tokenKeyType = TokenKeyType.ModuleId,
                project = any()
            )
        }
    }

}

class ImplRequestPresenter(
    view: RequestContract.RequestView,
    clientApiSessionEventsManager: ClientApiSessionEventsManager,
    rootManager: SecurityManager,
    configManager: ConfigManager,
    sessionEventsManager: ClientApiSessionEventsManager,
    tokenizationProcessor: TokenizationProcessor
) : RequestPresenter(
    view = view,
    eventsManager = clientApiSessionEventsManager,
    rootManager = rootManager,
    configManager = configManager,
    sessionEventsManager = sessionEventsManager,
    tokenizationProcessor = tokenizationProcessor
) {

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

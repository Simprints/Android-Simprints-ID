package com.simprints.clientapi.activities.baserequest

import com.google.gson.Gson
import com.nhaarman.mockito_kotlin.argThat
import com.simprints.clientapi.clientrequests.builders.ClientRequestBuilder
import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.SuspiciousIntentEvent
import com.simprints.clientapi.domain.requests.EnrollRequest
import com.simprints.clientapi.domain.requests.IntegrationInfo
import com.simprints.clientapi.domain.responses.*
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.testtools.common.syntax.*
import org.junit.Test

class RequestPresenterTest {

    private val projectIdField = Pair("projectId", "someProjectId")
    private val moduleIdField = Pair("moduleId", "someModuleId")
    private val userIdField = Pair("userId", "someUserId")
    private val metadataField = Pair("metadata", "someMetadata")
    private val extraField = Pair("extraField", "someExtraField")

    private val enrolIntentFields = mapOf(projectIdField, moduleIdField, userIdField)
    private val enrolRequest = EnrollRequest(projectIdField.second, moduleIdField.second, userIdField.second, metadataField.second, emptyMap(), mock())

    @Test
    fun givenAnIntentWithExtraKeys_validateAndSendRequest_suspiciousIntentEventShouldBeAdded() {
        val clientApiSessionEventsManagerMock: ClientApiSessionEventsManager = mock()
        val enrolExtractorMock = mockEnrolExtractorToHaveAnExtraField()
        val view = mock<RequestContract.RequestView>().apply {
            whenever(this) { enrollExtractor } thenReturn enrolExtractorMock
        }

        val presenter = ImplRequestPresenter(view, mock(), clientApiSessionEventsManagerMock, mock(), emptyMap())
        presenter.processEnrollRequest()

        verifyOnce(clientApiSessionEventsManagerMock) {
            addSessionEvent(argThat {
                this is SuspiciousIntentEvent &&
                    with(Gson()) { toJson(unexpectedExtras) == toJson(mapOf(extraField)) }
            })
        }
    }

    @Test
    fun givenAnIntentWithExtraEmptyKeyAndValue_validateAndSendRequest_suspiciousIntentEventShouldNotBeAdded() {
        val clientApiSessionEventsManagerMock: ClientApiSessionEventsManager = mock()
        val requestBuilder = mockClientBuilderToReturnAnEnrolRequest()
        val view = mockViewToReturnIntentFields(enrolIntentFields.plus("" to ""))

        val presenter = ImplRequestPresenter(view, mock(), clientApiSessionEventsManagerMock, mock(), emptyMap())
        presenter.validateAndSendRequest(requestBuilder)

        verifyNever(clientApiSessionEventsManagerMock) { addSessionEvent(anyNotNull()) }
    }

    @Test
    fun givenAnIntentWithNoExtraKeys_validateAndSendRequest_suspiciousIntentEventShouldNotBeAdded() {
        val clientApiSessionEventsManagerMock: ClientApiSessionEventsManager = mock()
        val requestBuilder = mockClientBuilderToReturnAnEnrolRequest()
        val view = mockViewToReturnIntentFields(enrolIntentFields)

        val presenter = ImplRequestPresenter(view, mock(), clientApiSessionEventsManagerMock, mock(), emptyMap())
        presenter.validateAndSendRequest(requestBuilder)

        verifyNever(clientApiSessionEventsManagerMock) { addSessionEvent(anyNotNull()) }
    }

    private fun mockViewToReturnIntentFields(intentFields: Map<String, String>): RequestContract.RequestView =
        mock<RequestContract.RequestView>().apply {
            whenever(this) { getIntentExtras() } thenReturn intentFields
        }

    private fun mockClientBuilderToReturnAnEnrolRequest(): ClientRequestBuilder =
        mock<ClientRequestBuilder>().apply {
            whenever(this) { build() } thenReturn enrolRequest
        }

    private fun mockEnrolExtractorToHaveAnExtraField(): EnrollExtractor =
        mock<EnrollExtractor>().apply {
            whenever(this) { getProjectId() } thenReturn "project_id"
            whenever(this) { getUserId() } thenReturn "user_id"
            whenever(this) { getModuleId() } thenReturn "module_id"
            whenever(this) { getUnknownExtras() } thenReturn mapOf(extraField)
            whenever(this) { getMetadata() } thenReturn "metadata"
        }
}

class ImplRequestPresenter(view: RequestContract.RequestView,
                           timeHelper: ClientApiTimeHelper,
                           clientApiSessionEventsManager: ClientApiSessionEventsManager,
                           integrationInfo: IntegrationInfo,
                           override val domainErrorToCallingAppResultCode: Map<ErrorResponse.Reason, Int>) :
    RequestPresenter(
        view,
        timeHelper,
        clientApiSessionEventsManager,
        integrationInfo) {

    override fun handleEnrollResponse(enroll: EnrollResponse) {}
    override fun handleIdentifyResponse(identify: IdentifyResponse) {}
    override fun handleVerifyResponse(verify: VerifyResponse) {}
    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) {}
    override fun handleResponseError(errorResponse: ErrorResponse) {}
}

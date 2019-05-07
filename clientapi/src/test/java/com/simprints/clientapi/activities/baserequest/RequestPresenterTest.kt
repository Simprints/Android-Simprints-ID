package com.simprints.clientapi.activities.baserequest

import com.google.gson.Gson
import com.nhaarman.mockito_kotlin.argThat
import com.simprints.clientapi.clientrequests.builders.ClientRequestBuilder
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.SuspiciousIntentEvent
import com.simprints.clientapi.domain.requests.EnrollRequest
import com.simprints.clientapi.domain.responses.EnrollResponse
import com.simprints.clientapi.domain.responses.IdentifyResponse
import com.simprints.clientapi.domain.responses.RefusalFormResponse
import com.simprints.clientapi.domain.responses.VerifyResponse
import com.simprints.clientapi.tools.json.GsonBuilder
import com.simprints.clientapi.tools.json.GsonBuilderImpl
import com.simprints.testtools.common.syntax.*
import org.junit.Test

class RequestPresenterTest {

    private val projectIdField = Pair("projectId", "someProjectId")
    private val moduleIdField = Pair("moduleId", "someModuleId")
    private val userIdField = Pair("userId", "someUserId")
    private val metadataField = Pair("metadata", "someMetadata")
    private val extraField = Pair("extraField", "someExtraField")

    private val enrolIntentFields = mapOf(projectIdField, moduleIdField, userIdField)
    private val enrolIntentFieldsWithExtra = enrolIntentFields.plus(extraField)
    private val enrolRequest = EnrollRequest(projectIdField.second, moduleIdField.second, userIdField.second, metadataField.second)

    @Test
    fun givenAnIntentWithExtraKeys_validateAndSendRequest_suspiciousIntentEventShouldBeAdded() {
        val clientApiSessionEventsManagerMock: ClientApiSessionEventsManager = mock()
        val requestBuilder = mockClientBuilderToReturnAnEnrolRequest()
        val view = mockViewToReturnIntentFields(enrolIntentFieldsWithExtra)

        val presenter = ImplRequestPresenter(view, clientApiSessionEventsManagerMock, GsonBuilderImpl())
        presenter.validateAndSendRequest(requestBuilder)

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

        val presenter = ImplRequestPresenter(view, clientApiSessionEventsManagerMock, GsonBuilderImpl())
        presenter.validateAndSendRequest(requestBuilder)

        verifyNever(clientApiSessionEventsManagerMock) { addSessionEvent(anyNotNull()) }
    }

    @Test
    fun givenAnIntentWithNoExtraKeys_validateAndSendRequest_suspiciousIntentEventShouldNotBeAdded() {
        val clientApiSessionEventsManagerMock: ClientApiSessionEventsManager = mock()
        val requestBuilder = mockClientBuilderToReturnAnEnrolRequest()
        val view = mockViewToReturnIntentFields(enrolIntentFields)

        val presenter = ImplRequestPresenter(view, clientApiSessionEventsManagerMock, GsonBuilderImpl())
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
}

class ImplRequestPresenter(view: RequestContract.RequestView,
                           clientApiSessionEventsManager: ClientApiSessionEventsManager,
                           gsonBuilder: GsonBuilder) : RequestPresenter(view, clientApiSessionEventsManager, gsonBuilder) {

    override fun handleEnrollResponse(enroll: EnrollResponse) {}
    override fun handleIdentifyResponse(identify: IdentifyResponse) {}
    override fun handleVerifyResponse(verify: VerifyResponse) {}
    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) {}
    override fun handleResponseError() {}
}

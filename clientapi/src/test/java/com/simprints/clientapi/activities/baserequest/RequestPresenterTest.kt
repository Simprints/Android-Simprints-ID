package com.simprints.clientapi.activities.baserequest

import com.simprints.clientapi.clientrequests.builders.ClientRequestBuilder
import com.simprints.clientapi.domain.requests.EnrollRequest
import com.simprints.clientapi.domain.requests.IdentifyRequest
import com.simprints.clientapi.domain.responses.EnrollResponse
import com.simprints.clientapi.domain.responses.IdentifyResponse
import com.simprints.clientapi.domain.responses.RefusalFormResponse
import com.simprints.clientapi.domain.responses.VerifyResponse
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import org.junit.Assert.*
import org.junit.Test

class RequestPresenterTest {

    private val PROJECTID_FIELD = Pair("projectId", "someProjectId")
    private val MODULEID_FIELD = Pair("moduleId", "someModuleId")
    private val USERID_FIELD = Pair("userId", "someUserId")
    private val METADATA_FIELD = Pair("metadata", "someMetadata")
    private val EXTRA_FIELD = Pair("extraField", "someExtraField")

    private val enrolIntentFields = mapOf(PROJECTID_FIELD, MODULEID_FIELD, USERID_FIELD)
    private val enrolIntentFieldsWithExtra = enrolIntentFields.plus(EXTRA_FIELD)
    private val enrolRequest = EnrollRequest(PROJECTID_FIELD.second, MODULEID_FIELD.second, USERID_FIELD.second, METADATA_FIELD.second)

    @Test
    fun givenAnIntentWithExtraKeys_validateAndSendRequest_suspiciousEventShouldBeAdded() {

        val view = mock<RequestContract.RequestView>().apply {
            whenever(this) { getIntentExtras() } thenReturn enrolIntentFieldsWithExtra
        }
        val requestBuilder = mock<ClientRequestBuilder>().apply {
            whenever(this) { build() } thenReturn enrolRequest
        }

        val presenter = ImplRequestPresenter()
        presenter.validateAndSendRequest(requestBuilder)


    }
}

class ImplRequestPresenter: RequestPresenter(mock(), mock(), mock()) {
    override fun handleEnrollResponse(enroll: EnrollResponse) {}
    override fun handleIdentifyResponse(identify: IdentifyResponse) {}
    override fun handleVerifyResponse(verify: VerifyResponse) {}
    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) {}
    override fun handleResponseError() {}
}

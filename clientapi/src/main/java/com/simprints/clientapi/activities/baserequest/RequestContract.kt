package com.simprints.clientapi.activities.baserequest

import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.clientrequests.builders.ClientRequestBuilder
import com.simprints.clientapi.clientrequests.extractors.*
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.responses.*

interface RequestContract {

    interface RequestView {
        val intentAction: String?
        val extras: Map<String, Any?>?
        val presenter: Presenter
        val enrolExtractor: EnrolExtractor
        val verifyExtractor: VerifyExtractor
        val identifyExtractor: IdentifyExtractor
        val confirmIdentityExtractor: ConfirmIdentityExtractor
        val enrolLastBiometricsExtractor: EnrolLastBiometricsExtractor

        fun sendSimprintsRequest(request: BaseRequest)
        fun handleClientRequestError(clientApiAlert: ClientApiAlert)

        /**
         * This is not being used for CommCare because of CoSync.
         * The method [com.simprints.clientapi.activities.commcare.CommCareContract.View.returnErrorToClient]
         * is functionally the same as this, but you can pass a nullable eventJson that won't be included in the response
         * if it's null.
         */
        fun returnErrorToClient(errorResponse: ErrorResponse, flowCompletedCheck: Boolean, sessionId: String)
    }

    interface Presenter {
        suspend fun start()
        suspend fun processEnrolRequest()
        suspend fun processIdentifyRequest()
        suspend fun processVerifyRequest()
        suspend fun processConfirmIdentityRequest()
        suspend fun processEnrolLastBiometrics()
        fun handleEnrolResponse(enrol: EnrolResponse)
        fun handleIdentifyResponse(identify: IdentifyResponse)
        fun handleVerifyResponse(verify: VerifyResponse)
        fun handleRefusalResponse(refusalForm: RefusalFormResponse)
        fun handleResponseError(errorResponse: ErrorResponse)
        suspend fun validateAndSendRequest(builder: ClientRequestBuilder)
        fun handleConfirmationResponse(response: ConfirmationResponse)
    }

}

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
        val enrollExtractor: EnrollExtractor
        val verifyExtractor: VerifyExtractor
        val identifyExtractor: IdentifyExtractor
        val confirmIdentityExtractor: ConfirmIdentityExtractor
        val enrolLastBiometricsExtractor: EnrolLastBiometricsExtractor

        fun sendSimprintsRequest(request: BaseRequest)
        fun handleClientRequestError(clientApiAlert: ClientApiAlert)
        fun returnErrorToClient(errorResponse: ErrorResponse, flowCompletedCheck: Boolean, sessionId: String)
    }

    interface Presenter {
        suspend fun start()
        suspend fun processEnrollRequest()
        suspend fun processIdentifyRequest()
        suspend fun processVerifyRequest()
        suspend fun processConfirmIdentityRequest()
        suspend fun processEnrolLastBiometrics()
        fun handleEnrollResponse(enroll: EnrollResponse)
        fun handleIdentifyResponse(identify: IdentifyResponse)
        fun handleVerifyResponse(verify: VerifyResponse)
        fun handleRefusalResponse(refusalForm: RefusalFormResponse)
        fun handleResponseError(errorResponse: ErrorResponse)
        suspend fun validateAndSendRequest(builder: ClientRequestBuilder)
        fun handleConfirmationResponse(response: ConfirmationResponse)
    }

}

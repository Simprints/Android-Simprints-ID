package com.simprints.clientapi.activities.baserequest

import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.clientrequests.builders.ClientRequestBuilder
import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentityExtractor
import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor
import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.confirmations.BaseConfirmation
import com.simprints.clientapi.domain.responses.*

interface RequestContract {

    interface RequestView {
        val action: String?
        val extras: Map<String, Any?>?
        val presenter: Presenter
        val enrollExtractor: EnrollExtractor
        val verifyExtractor: VerifyExtractor
        val identifyExtractor: IdentifyExtractor
        val confirmIdentityExtractor: ConfirmIdentityExtractor

        fun sendSimprintsRequest(request: BaseRequest)
        fun sendSimprintsConfirmation(request: BaseConfirmation)
        fun handleClientRequestError(clientApiAlert: ClientApiAlert)
        fun returnErrorToClient(errorResponse: ErrorResponse, flowCompletedCheck: Boolean, sessionId: String)
    }

    interface Presenter {
        suspend fun start()
        suspend fun processEnrollRequest()
        suspend fun processIdentifyRequest()
        suspend fun processVerifyRequest()
        suspend fun processConfirmIdentityRequest()
        fun handleEnrollResponse(enroll: EnrollResponse)
        fun handleIdentifyResponse(identify: IdentifyResponse)
        fun handleVerifyResponse(verify: VerifyResponse)
        fun handleRefusalResponse(refusalForm: RefusalFormResponse)
        fun handleResponseError(errorResponse: ErrorResponse)
        suspend fun validateAndSendRequest(builder: ClientRequestBuilder)
        fun handleConfirmationResponse(response: ConfirmationResponse)
    }

}

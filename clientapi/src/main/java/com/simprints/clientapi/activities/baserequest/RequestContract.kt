package com.simprints.clientapi.activities.baserequest

import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.clientrequests.builders.ClientRequestBuilder
import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentifyExtractor
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

        val confirmIdentifyExtractor: ConfirmIdentifyExtractor

        fun sendSimprintsRequest(request: BaseRequest)

        fun sendSimprintsConfirmationAndFinish(request: BaseConfirmation)

        fun handleClientRequestError(clientApiAlert: ClientApiAlert)

        fun returnErrorToClient(errorResponse: ErrorResponse, skipCheck: Boolean)
    }

    interface Presenter {

        fun processEnrollRequest()

        fun processIdentifyRequest()

        fun processVerifyRequest()

        fun processConfirmIdentifyRequest()

        fun handleEnrollResponse(enroll: EnrollResponse)

        fun handleIdentifyResponse(identify: IdentifyResponse)

        fun handleVerifyResponse(verify: VerifyResponse)

        fun handleRefusalResponse(refusalForm: RefusalFormResponse)

        fun handleResponseError(errorResponse: ErrorResponse)

        fun validateAndSendRequest(builder: ClientRequestBuilder)

    }

}

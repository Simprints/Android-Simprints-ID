package com.simprints.clientapi.activities.baserequest

import com.simprints.clientapi.clientrequests.builders.ClientRequestBuilder
import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor
import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.simprintsrequests.requests.ClientApiAppRequest
import com.simprints.clientapi.simprintsrequests.requests.ClientApiAppConfirmation
import com.simprints.clientapi.simprintsrequests.responses.ClientApiEnrollResponse
import com.simprints.clientapi.simprintsrequests.responses.ClientApiIdentifyResponse
import com.simprints.clientapi.simprintsrequests.responses.ClientApiRefusalFormResponse
import com.simprints.clientapi.simprintsrequests.responses.ClientApiVerifyResponse


interface RequestContract {

    interface RequestView {

        val presenter: Presenter

        val enrollExtractor: EnrollExtractor

        val verifyExtractor: VerifyExtractor

        val identifyExtractor: IdentifyExtractor

        val confirmIdentifyExtractor: ConfirmIdentifyExtractor

        fun sendSimprintsRequest(request: ClientApiAppRequest)

        fun sendSimprintsConfirmationAndFinish(request: ClientApiAppConfirmation)

        fun handleClientRequestError(exception: Exception)

        fun returnIntentActionErrorToClient()

    }

    interface Presenter {

        fun processEnrollRequest()

        fun processIdentifyRequest()

        fun processVerifyRequest()

        fun processConfirmIdentifyRequest()

        fun handleEnrollResponse(enroll: ClientApiEnrollResponse)

        fun handleIdentifyResponse(identify: ClientApiIdentifyResponse)

        fun handleVerifyResponse(verify: ClientApiVerifyResponse)

        fun handleRefusalResponse(refusalForm: ClientApiRefusalFormResponse)

        fun handleResponseError()

        fun validateAndSendRequest(builder: ClientRequestBuilder)

    }

}

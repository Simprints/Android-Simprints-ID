package com.simprints.clientapi.activities.baserequest

import com.simprints.clientapi.clientrequests.builders.ClientRequestBuilder
import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor
import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.simprintsrequests.requests.SimprintsIdRequest
import com.simprints.clientapi.simprintsrequests.responses.EnrollResponse
import com.simprints.clientapi.simprintsrequests.responses.IdentificationResponse
import com.simprints.clientapi.simprintsrequests.responses.RefusalFormResponse
import com.simprints.clientapi.simprintsrequests.responses.VerifyResponse


interface RequestContract {

    interface RequestView {

        val presenter: Presenter

        val enrollExtractor: EnrollExtractor

        val verifyExtractor: VerifyExtractor

        val identifyExtractor: IdentifyExtractor

        val confirmIdentifyExtractor: ConfirmIdentifyExtractor

        fun sendSimprintsRequest(request: SimprintsIdRequest)

        fun handleClientRequestError(exception: Exception)

        fun returnIntentActionErrorToClient()

    }

    interface Presenter {

        fun processEnrollRequest()

        fun processIdentifyRequest()

        fun processVerifyRequest()

        fun processConfirmIdentifyRequest()

        fun handleEnrollResponse(enroll: EnrollResponse)

        fun handleIdentifyResponse(identify: IdentificationResponse)

        fun handleVerifyResponse(verify: VerifyResponse)

        fun handleRefusalResponse(refusalForm: RefusalFormResponse)

        fun handleResponseError()

        fun validateAndSendRequest(builder: ClientRequestBuilder)

    }

}

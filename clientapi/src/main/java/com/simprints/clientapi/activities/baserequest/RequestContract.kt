package com.simprints.clientapi.activities.baserequest

import com.simprints.clientapi.clientrequests.builders.ClientRequestBuilder
import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor
import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.models.domain.confirmations.BaseConfirmation
import com.simprints.clientapi.models.domain.requests.BaseRequest
import com.simprints.clientapi.models.domain.responses.EnrollResponse
import com.simprints.clientapi.models.domain.responses.IdentifyResponse
import com.simprints.clientapi.models.domain.responses.RefusalFormResponse
import com.simprints.clientapi.models.domain.responses.VerifyResponse


interface RequestContract {

    interface RequestView {

        val presenter: Presenter

        val enrollExtractor: EnrollExtractor

        val verifyExtractor: VerifyExtractor

        val identifyExtractor: IdentifyExtractor

        val confirmIdentifyExtractor: ConfirmIdentifyExtractor

        fun sendSimprintsRequest(request: BaseRequest)

        fun sendSimprintsConfirmationAndFinish(request: BaseConfirmation)

        fun handleClientRequestError(exception: Exception)

        fun returnIntentActionErrorToClient()

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

        fun handleResponseError()

        fun validateAndSendRequest(builder: ClientRequestBuilder)

    }

}

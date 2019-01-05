package com.simprints.clientapi.activities

import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor
import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest


interface ClientRequestView {

    val enrollExtractor: EnrollExtractor

    val verifyExtractor: VerifyExtractor

    val identifyExtractor: IdentifyExtractor

    val confirmIdentifyExtractor: ConfirmIdentifyExtractor

    fun sendSimprintsRequest(request: SimprintsIdRequest)

    fun handleClientRequestError(exception: Exception)

    fun returnIntentActionErrorToClient()

}

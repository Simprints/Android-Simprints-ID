package com.simprints.clientapi.activities

import com.simprints.clientapi.clientrequests.extractors.EnrollmentExtractor
import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest


interface ClientRequestView {

    val enrollmentExtractor: EnrollmentExtractor

    val verifyExtractor: VerifyExtractor

    val identifyExtractor: IdentifyExtractor

    fun sendSimprintsRequest(request: SimprintsIdRequest)

    fun handleClientRequestError(exception: Exception)

    fun returnIntentActionErrorToClient()

}

package com.simprints.clientapi.activities

import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor
import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.simprintsrequests.SimprintsActionRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacySimprintsActionRequest


interface ClientRequestView {

    val enrollExtractor: EnrollExtractor

    val verifyExtractor: VerifyExtractor

    val identifyExtractor: IdentifyExtractor

    fun sendSimprintsActionRequest(request: SimprintsActionRequest)

    fun sendLegacySimprintsActionRequest(request: LegacySimprintsActionRequest)

    fun handleClientRequestError(exception: Exception)

    fun returnIntentActionErrorToClient()

}

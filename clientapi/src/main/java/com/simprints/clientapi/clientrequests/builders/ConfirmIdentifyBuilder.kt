package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentifyExtractor
import com.simprints.clientapi.clientrequests.validators.ConfirmIdentifyValidator
import com.simprints.clientapi.simprintsrequests.requests.ConfirmIdentifyRequest
import com.simprints.clientapi.simprintsrequests.requests.SimprintsIdRequest
import com.simprints.clientapi.simprintsrequests.requests.legacy.LegacyConfirmIdentifyRequest
import com.simprints.clientapi.simprintsrequests.requests.legacy.LegacySimprintsIdRequest


class ConfirmIdentifyBuilder(val extractor: ConfirmIdentifyExtractor,
                             validator: ConfirmIdentifyValidator) :
    ClientRequestBuilder(extractor, validator) {

    override fun buildSimprintsRequest(): SimprintsIdRequest = ConfirmIdentifyRequest(
        projectId = extractor.getProjectId(),
        sessionId = extractor.getSessionId(),
        selectedGuid = extractor.getSelectedGuid()
    )

    override fun buildLegacySimprintsRequest(): LegacySimprintsIdRequest = LegacyConfirmIdentifyRequest(
        legacyApiKey = extractor.getLegacyApiKey(),
        sessionId = extractor.getSessionId(),
        selectedGuid = extractor.getSelectedGuid()
    )

}

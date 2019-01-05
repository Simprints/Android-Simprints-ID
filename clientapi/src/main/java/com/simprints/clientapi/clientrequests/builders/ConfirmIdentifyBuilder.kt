package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentifyExtractor
import com.simprints.clientapi.clientrequests.validators.ConfirmIdentifyValidator
import com.simprints.clientapi.simprintsrequests.ConfirmIdentifyRequest
import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyConfirmIdentifyRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacySimprintsIdRequest


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

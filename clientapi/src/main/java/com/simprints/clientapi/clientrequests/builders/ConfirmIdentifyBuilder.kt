package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentifyExtractor
import com.simprints.clientapi.clientrequests.validators.ConfirmIdentifyValidator
import com.simprints.clientapi.simprintsrequests.requests.ClientApiBaseRequest
import com.simprints.clientapi.simprintsrequests.requests.ClientApiConfirmIdentifyRequest


class ConfirmIdentifyBuilder(val extractor: ConfirmIdentifyExtractor,
                             validator: ConfirmIdentifyValidator) :
    ClientRequestBuilder(validator) {

    override fun buildSimprintsRequest(): ClientApiBaseRequest = ClientApiConfirmIdentifyRequest(
        projectId = extractor.getProjectId(),
        sessionId = extractor.getSessionId(),
        selectedGuid = extractor.getSelectedGuid()
    )

}

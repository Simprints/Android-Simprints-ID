package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentityExtractor
import com.simprints.clientapi.clientrequests.validators.ConfirmIdentityValidator
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.IdentityConfirmationRequest


class ConfirmIdentifyBuilder(val extractor: ConfirmIdentityExtractor,
                             validator: ConfirmIdentityValidator) :
    ClientRequestBuilder(validator) {

    override fun buildAppRequest(): BaseRequest = IdentityConfirmationRequest(
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        sessionId = extractor.getSessionId(),
        selectedGuid = extractor.getSelectedGuid(),
        unknownExtras = extractor.getUnknownExtras()
    )
}

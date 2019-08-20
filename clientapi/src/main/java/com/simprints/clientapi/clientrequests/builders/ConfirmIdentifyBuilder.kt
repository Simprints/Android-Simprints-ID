package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentityExtractor
import com.simprints.clientapi.clientrequests.validators.ConfirmIdentityValidator
import com.simprints.clientapi.domain.ClientBase
import com.simprints.clientapi.domain.requests.confirmations.IdentityConfirmation


class ConfirmIdentifyBuilder(val extractor: ConfirmIdentityExtractor,
                             validator: ConfirmIdentityValidator) :
    ClientRequestBuilder(validator) {

    override fun buildAppRequest(): ClientBase = IdentityConfirmation(
        projectId = extractor.getProjectId(),
        sessionId = extractor.getSessionId(),
        selectedGuid = extractor.getSelectedGuid(),
        unknownExtras = extractor.getUnknownExtras()
    )
}

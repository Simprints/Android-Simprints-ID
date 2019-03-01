package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentifyExtractor
import com.simprints.clientapi.clientrequests.validators.ConfirmIdentifyValidator
import com.simprints.clientapi.domain.ClientBase
import com.simprints.clientapi.domain.confirmations.IdentifyConfirmation


class ConfirmIdentifyBuilder(val extractor: ConfirmIdentifyExtractor,
                             validator: ConfirmIdentifyValidator) :
    ClientRequestBuilder(validator) {

    override fun buildAppRequest(): ClientBase = IdentifyConfirmation(
        projectId = extractor.getProjectId(),
        sessionId = extractor.getSessionId(),
        selectedGuid = extractor.getSelectedGuid()
    )

}

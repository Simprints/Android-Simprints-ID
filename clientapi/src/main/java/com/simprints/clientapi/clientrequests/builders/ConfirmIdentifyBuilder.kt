package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentifyExtractor
import com.simprints.clientapi.clientrequests.validators.ConfirmIdentifyValidator
import com.simprints.clientapi.domain.ClientBase
import com.simprints.clientapi.domain.requests.ExtraRequestInfo
import com.simprints.clientapi.domain.requests.IntegrationInfo
import com.simprints.clientapi.domain.requests.confirmations.IdentifyConfirmation


class ConfirmIdentifyBuilder(val extractor: ConfirmIdentifyExtractor,
                             validator: ConfirmIdentifyValidator,
                             private val integrationInfo: IntegrationInfo) :
    ClientRequestBuilder(validator) {

    override fun buildAppRequest(): ClientBase = IdentifyConfirmation(
        projectId = extractor.getProjectId(),
        sessionId = extractor.getSessionId(),
        selectedGuid = extractor.getSelectedGuid(),
        extra = ExtraRequestInfo(integrationInfo)
    )
}

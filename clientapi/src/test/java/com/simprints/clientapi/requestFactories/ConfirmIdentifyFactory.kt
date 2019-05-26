package com.simprints.clientapi.requestFactories

import com.simprints.clientapi.clientrequests.builders.ConfirmIdentifyBuilder
import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentifyExtractor
import com.simprints.clientapi.clientrequests.validators.ConfirmIdentifyValidator
import com.simprints.clientapi.domain.requests.ExtraRequestInfo
import com.simprints.clientapi.domain.requests.IntegrationInfo
import com.simprints.clientapi.domain.requests.confirmations.BaseConfirmation
import com.simprints.clientapi.domain.requests.confirmations.IdentifyConfirmation
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever

object ConfirmIdentifyFactory : RequestFactory() {

    override fun getValidSimprintsRequest(integrationInfo: IntegrationInfo): BaseConfirmation =
        IdentifyConfirmation(
            projectId = MOCK_PROJECT_ID,
            sessionId = MOCK_SESSION_ID,
            selectedGuid = MOCK_SELECTED_GUID,
            extra = ExtraRequestInfo(integrationInfo)
        )

    override fun getValidator(extractor: ClientRequestExtractor): ConfirmIdentifyValidator =
        ConfirmIdentifyValidator(extractor as ConfirmIdentifyExtractor)

    override fun getBuilder(extractor: ClientRequestExtractor): ConfirmIdentifyBuilder =
        ConfirmIdentifyBuilder(extractor as ConfirmIdentifyExtractor, getValidator(extractor), mock())

    override fun getMockExtractor(): ConfirmIdentifyExtractor {
        val mockConfirmIdentifyExtractor = mock<ConfirmIdentifyExtractor>()
        setMockDefaultExtractor(mockConfirmIdentifyExtractor)
        whenever(mockConfirmIdentifyExtractor) { getSessionId() } thenReturn MOCK_SESSION_ID
        whenever(mockConfirmIdentifyExtractor) { getSelectedGuid() } thenReturn MOCK_SELECTED_GUID
        return mockConfirmIdentifyExtractor
    }
}

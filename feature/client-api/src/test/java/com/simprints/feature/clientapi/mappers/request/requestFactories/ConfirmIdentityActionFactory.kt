package com.simprints.feature.clientapi.mappers.request.requestFactories

import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.mappers.request.builders.ConfirmIdentifyRequestBuilder
import com.simprints.feature.clientapi.mappers.request.extractors.ActionRequestExtractor
import com.simprints.feature.clientapi.mappers.request.extractors.ConfirmIdentityRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.ConfirmIdentityValidator
import io.mockk.every
import io.mockk.mockk

internal object ConfirmIdentityActionFactory : RequestActionFactory() {

    override fun getValidSimprintsRequest() = ActionRequest.ConfirmActionRequest(
        packageName = MOCK_PACKAGE,
        projectId = MOCK_PROJECT_ID,
        userId = MOCK_USER_ID,
        sessionId = MOCK_SESSION_ID,
        selectedGuid = MOCK_SELECTED_GUID,
        unknownExtras = emptyMap()
    )

    override fun getValidator(extractor: ActionRequestExtractor): ConfirmIdentityValidator =
        ConfirmIdentityValidator(extractor as ConfirmIdentityRequestExtractor, MOCK_SESSION_ID, true)

    override fun getBuilder(extractor: ActionRequestExtractor): ConfirmIdentifyRequestBuilder =
        ConfirmIdentifyRequestBuilder(MOCK_PACKAGE, extractor as ConfirmIdentityRequestExtractor, getValidator(extractor))

    override fun getMockExtractor(): ConfirmIdentityRequestExtractor {
        val mockConfirmIdentifyExtractor = mockk<ConfirmIdentityRequestExtractor>()
        setMockDefaultExtractor(mockConfirmIdentifyExtractor)
        every { mockConfirmIdentifyExtractor.getSessionId() } returns MOCK_SESSION_ID
        every { mockConfirmIdentifyExtractor.getSelectedGuid() } returns MOCK_SELECTED_GUID
        return mockConfirmIdentifyExtractor
    }
}
